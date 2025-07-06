
package com.example.bookmanager.repository

import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.BookRepositoryImpl
import io.mockk.*
import org.example.bookmanager.db.jooq.gen.tables.BookAuthors.BOOK_AUTHORS
import org.example.bookmanager.db.jooq.gen.tables.Books.BOOKS
import org.example.bookmanager.db.jooq.gen.tables.records.BookAuthorsRecord
import org.example.bookmanager.db.jooq.gen.tables.records.BooksRecord
import org.jooq.DSLContext
import org.jooq.Record4
import org.jooq.TableRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jooq.Result
import org.jooq.SQLDialect
import kotlin.collections.emptyList
import org.jooq.impl.DSL

class BookRepositoryTest {

    private lateinit var dslContext: DSLContext
    private lateinit var bookRepositoryImpl: BookRepositoryImpl

    @BeforeEach
    fun setUp() {
        dslContext = mockk(relaxed = true)
        bookRepositoryImpl = BookRepositoryImpl(dslContext)
    }

    @Test
    fun `insert - 正常に書籍と著者紐付け登録される`() {
        val title = "テスト本"
        val price = 2000
        val status = PublishedStatus.PUBLISHED
        val authorIds = listOf(1L, 2L)
        val bookId = 123L

        val mockBookRecord = mockk<BooksRecord>(relaxed = true)
        every { mockBookRecord.id } returns bookId
        every { mockBookRecord.title } returns title
        every { mockBookRecord.price } returns price
        every { mockBookRecord.publishStatus } returns status.name
        every { dslContext.newRecord(BOOKS) } returns mockBookRecord

        val mockAuthorRecords = authorIds.map {
            val r = mockk<BookAuthorsRecord>(relaxed = true)
            every { r.bookId = bookId } just Runs
            every { r.authorId = it } just Runs
            r
        }
        every { dslContext.newRecord(BOOK_AUTHORS) } returnsMany mockAuthorRecords

        val mockBatch = mockk<org.jooq.Batch>(relaxed = true)
        every { mockBatch.execute() } returns IntArray(authorIds.size) { 1 }
        every { dslContext.batchInsert(mockAuthorRecords) } returns mockBatch

        val result = bookRepositoryImpl.insert(title, price, status, authorIds)

        assertEquals(bookId, result.id)
        assertEquals(title, result.title)
        assertEquals(price, result.price)
        assertEquals(status, result.publishedStatus)
        assertEquals(authorIds, result.authorIds)
    }

    @Test
    fun `insert - 著者IDが空`() {
        val title = "Test Book"
        val price = 2000
        val status = PublishedStatus.PUBLISHED
        val authorIds = emptyList<Long>()

        val mockBookRecord = mockk<BooksRecord>(relaxed = true)
        every { mockBookRecord.id } returns 10L
        every { mockBookRecord.title } returns title
        every { mockBookRecord.price } returns price
        every { mockBookRecord.publishStatus } returns status.name
        every { dslContext.newRecord(BOOKS) } returns mockBookRecord
        every { mockBookRecord.store() } returns 1
        every { dslContext.batchInsert(any<Collection<TableRecord<*>>>()) } returns mockk {
            every { execute() } returns intArrayOf(1)
        }

        val result = bookRepositoryImpl.insert(title, price, status, authorIds)

        assertEquals(0, result.authorIds.size)
    }

    @Test
    fun `update - 正常に書籍情報更新`() {
        val id = 1L
        val newTitle = "Updated Title"
        val newPrice = 3000
        val newStatus = PublishedStatus.PUBLISHED
        val newAuthorIds = listOf(1L, 2L)

        val mockBookRecord = mockk<BooksRecord>(relaxed = true)
        every { mockBookRecord.id } returns id
        every { mockBookRecord.title } returns newTitle
        every { mockBookRecord.price } returns newPrice
        every { mockBookRecord.publishStatus } returns newStatus.name
        every { dslContext.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOne() } returns mockBookRecord
        every { mockBookRecord.store() } returns 1
        every { dslContext.deleteFrom(BOOK_AUTHORS).where(BOOK_AUTHORS.BOOK_ID.eq(id)).execute() } returns 2
        val mockAuthorRecords = newAuthorIds.map { mockk<BookAuthorsRecord>(relaxed = true) }
        every { dslContext.newRecord(BOOK_AUTHORS) } returnsMany mockAuthorRecords
        every { dslContext.batchInsert(mockAuthorRecords) } returns mockk {
            every { execute() } returns IntArray(newAuthorIds.size) { 1 }
        }

        val result = bookRepositoryImpl.update(id, newTitle, newPrice, newStatus, newAuthorIds)

        assertEquals(newTitle, result.title)
        assertEquals(newPrice, result.price)
        assertEquals(newStatus, result.publishedStatus)
    }

    @Test
    fun `update - 書籍が存在しない`() {
        val id = 999L
        every { dslContext.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOne() } returns null

        val ex = assertThrows<InvalidInputException>("Expected InvalidInputException to be thrown") {
            bookRepositoryImpl.update(id, "t", 1000, PublishedStatus.PUBLISHED, listOf(1L))
        }

        assertTrue(ex.message!!.contains("書籍が存在しません"))
    }

    @Test
    fun `findById - 書籍が存在する`() {
        val id = 1L
        val mockBookRecord = mockk<BooksRecord>(relaxed = true)
        every { mockBookRecord.id } returns id
        every { mockBookRecord.title } returns "タイトル"
        every { mockBookRecord.price } returns 1500
        every { mockBookRecord.publishStatus } returns PublishedStatus.PUBLISHED.name
        every { dslContext.selectFrom(BOOKS).where(BOOKS.ID.eq(id)).fetchOne() } returns mockBookRecord
        every { dslContext.selectFrom(BOOK_AUTHORS).where(BOOK_AUTHORS.BOOK_ID.eq(id)).fetch(BOOK_AUTHORS.AUTHOR_ID) } returns listOf(1L, 2L)

        val result = bookRepositoryImpl.findById(id)

        assertNotNull(result)
        assertEquals(id, result!!.id)
        assertEquals("タイトル", result.title)
    }

    @Test
    fun `findById - 書籍が存在しない`() {
        every { dslContext.selectFrom(BOOKS).where(BOOKS.ID.eq(123L)).fetchOne() } returns null

        val result = bookRepositoryImpl.findById(123L)

        assertNull(result)
    }

    @Test
    fun `findBooksByAuthorId - 書籍が複数見つかる`() {
        val authorId = 1L

        // DSL.using(...) は jOOQ の RecordFactory にアクセスするために必要
        val dsl = DSL.using(SQLDialect.DEFAULT)

        val record1: Record4<Long, String, Int, String> = dsl.newRecord(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISH_STATUS
        ).apply {
            setValue(BOOKS.ID, 1L)
            setValue(BOOKS.TITLE, "タイトル")
            setValue(BOOKS.PRICE, 1500)
            setValue(BOOKS.PUBLISH_STATUS, PublishedStatus.PUBLISHED.name)
        }

        val record2: Record4<Long, String, Int, String> = dsl.newRecord(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISH_STATUS
        ).apply {
            setValue(BOOKS.ID, 2L)
            setValue(BOOKS.TITLE, "タイトル2")
            setValue(BOOKS.PRICE, 2000)
            setValue(BOOKS.PUBLISH_STATUS, PublishedStatus.UNPUBLISHED.name)
        }

        // Result を明示的に生成
        val result: Result<Record4<Long, String, Int, String>> =
            dsl.newResult(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLISH_STATUS)
        result.addAll(listOf(record1, record2))

        // fetch() の戻り値にこの Result を使う
        every {
            dslContext.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLISH_STATUS)
                .from(BOOKS)
                .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetch()
        } returns result

        // 著者 ID のモック
        every {
            dslContext.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(1L))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)
        } returns listOf(1L)

        every {
            dslContext.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(2L))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)
        } returns listOf(1L)

        // 実行
        val books = bookRepositoryImpl.findBooksByAuthorId(authorId)

        // 検証
        assertEquals(2, books.size)
        assertEquals("タイトル", books[0].title)
        assertEquals(PublishedStatus.PUBLISHED, books[0].publishedStatus)
        assertEquals(listOf(1L), books[0].authorIds)
        assertEquals("タイトル2", books[1].title)
        assertEquals(PublishedStatus.UNPUBLISHED, books[1].publishedStatus)
        assertEquals(listOf(1L), books[1].authorIds)
    }



    @Test
    fun `findBooksByAuthorId - 書籍が見つからない`() {
        val authorId = 999L
        val resultMock = mockk<Result<Record4<Long, String, Int, String>>>(relaxed = true)
        every { resultMock.isEmpty() } returns true
        every { resultMock.iterator() } returns mutableListOf<Record4<Long, String, Int, String>>().iterator()

        every {
            dslContext.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLISH_STATUS)
                .from(BOOKS)
                .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetch()
        } returns resultMock

        val result = bookRepositoryImpl.findBooksByAuthorId(authorId)

        assertTrue(result.isEmpty())
    }
}
