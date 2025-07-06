package com.example.bookmanager.infra.repository

import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.repository.BookRepository
import com.example.bookmanager.exception.InvalidInputException
import org.example.bookmanager.db.jooq.gen.tables.BookAuthors.BOOK_AUTHORS
import org.example.bookmanager.db.jooq.gen.tables.Books.BOOKS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

/**
 * 書籍リポジトリの実装クラス
 * JOOQを用いて、books / book_authorsテーブルへのCRUD操作を担当する。
 */
@Repository
class BookRepositoryImpl(
    private val dslContext: DSLContext
) : BookRepository {

    /**
     * 書籍と著者の紐付け情報を含めて、新しい書籍を登録する。
     *
     * @param title 書籍タイトル
     * @param price 価格（整数）
     * @param publishStatus 出版ステータス
     * @param authorIds 紐付ける著者ID一覧
     * @return 登録された書籍のDTO
     */
    override fun insert(title: String, price: Int, publishStatus: PublishedStatus, authorIds: List<Long>): BookDto {
        // 書籍を登録する
        val bookRecord = dslContext.newRecord(BOOKS).apply {
            this.title = title
            this.price = price
            this.publishStatus = publishStatus.name
        }
        bookRecord.store()

        // 書籍と著者の更新紐づけを登録する
        val bookAuthorRecords = authorIds.map { authorId ->
            dslContext.newRecord(BOOK_AUTHORS).apply {
                this.bookId = bookRecord.id
                this.authorId = authorId
            }
        }
        dslContext.batchInsert(bookAuthorRecords).execute()

        return BookDto(
            id = bookRecord.id,
            title = bookRecord.title,
            price = bookRecord.price,
            publishedStatus = PublishedStatus.valueOf(bookRecord.publishStatus),
            authorIds = authorIds
        )
    }

    /**
     * 書籍情報と著者紐付けを更新する。
     * 書籍が存在しない場合は例外をスロー。
     *
     * @param id 書籍ID
     * @param title タイトル
     * @param price 価格
     * @param publishStatus 出版ステータス
     * @param authorIds 紐付ける著者ID一覧
     * @return 更新後の書籍DTO
     * @throws InvalidInputException 書籍が存在しない場合
     */
    override fun update(id: Long, title: String, price: Int, publishStatus: PublishedStatus, authorIds: List<Long>): BookDto {
        // 書籍を更新する
        val bookRecord = dslContext.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne() ?: throw InvalidInputException("書籍が存在しません: id=$id")

        bookRecord.apply {
            this.title = title
            this.price = price
            this.publishStatus = publishStatus.name
            this.store()  // UPDATE 実行
        }

        // 書籍と著者の紐付けを更新する（delete insert）
        dslContext.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(id))
            .execute()

        val bookAuthorRecords = authorIds.map { authorId ->
            dslContext.newRecord(BOOK_AUTHORS).apply {
                this.bookId = id
                this.authorId = authorId
            }
        }
        dslContext.batchInsert(bookAuthorRecords).execute()

        return BookDto(
            id = bookRecord.id,
            title = bookRecord.title,
            price = bookRecord.price,
            publishedStatus = PublishedStatus.valueOf(bookRecord.publishStatus),
            authorIds = authorIds
        )
    }

    /**
     * 書籍IDを指定して1件取得する。
     *
     * @param id 書籍ID
     * @return 該当する書籍DTO。存在しない場合はnull。
     */
    override fun findById(id: Long): BookDto? {
        val record = dslContext.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne()

        val authorIds = dslContext.selectFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(id))
            .fetch(BOOK_AUTHORS.AUTHOR_ID)

        return record?.let {
            BookDto(
                id = record.id,
                title = record.title,
                price = record.price,
                publishedStatus = PublishedStatus.valueOf(record.publishStatus),
                authorIds = authorIds
            )
        }
    }

    /**
     * 著者IDを指定して、該当する全書籍を取得する。
     *
     * @param authorId 著者ID
     * @return 該当著者に紐付く書籍一覧
     */
    override fun findBooksByAuthorId(authorId: Long): List<BookDto> {
        // 書籍情報を取得
        val bookRecords = dslContext.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLISH_STATUS)
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch()

        // 各書籍に対して著者ID一覧を取得
        return bookRecords.map { record ->
            val bookId = record.get(BOOKS.ID)!!

            val authorIds = dslContext.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)

            BookDto(
                id = bookId,
                title = record.get(BOOKS.TITLE)!!,
                price = record.get(BOOKS.PRICE)!!,
                publishedStatus = PublishedStatus.valueOf(record.get(BOOKS.PUBLISH_STATUS)!!),
                authorIds = authorIds
            )
        }
    }


}