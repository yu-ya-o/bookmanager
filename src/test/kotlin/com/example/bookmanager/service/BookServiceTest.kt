package com.example.bookmanager.service

import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.domain.service.impl.BookServiceImpl
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.AuthorRepositoryImpl
import com.example.bookmanager.infra.repository.BookRepositoryImpl
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BookServiceTest {

    private lateinit var bookRepositoryImpl: BookRepositoryImpl
    private lateinit var authorRepositoryImpl: AuthorRepositoryImpl
    private lateinit var bookServiceImpl: BookServiceImpl

    @BeforeEach
    fun setup() {
        bookRepositoryImpl = mockk()
        authorRepositoryImpl = mockk()
        bookServiceImpl = BookServiceImpl(bookRepositoryImpl, authorRepositoryImpl)
    }

    private fun createDto(
        id: Long? = null,
        title: String = "タイトル",
        price: Int = 1200,
        publishedStatus: PublishedStatus = PublishedStatus.PUBLISHED,
        authorIds: List<Long> = listOf(1L, 2L)
    ) = BookDto(id, title, price, publishedStatus, authorIds)

    @Test
    fun `createBook - 正常系`() {
        val dto = createDto()
        every { authorRepositoryImpl.findExistingIds(dto.authorIds) } returns dto.authorIds
        every {
            bookRepositoryImpl.insert(dto.title, dto.price, dto.publishedStatus, dto.authorIds)
        } returns dto

        val result = bookServiceImpl.create(dto)

        assertEquals(dto, result)
        verify { bookRepositoryImpl.insert(dto.title, dto.price, dto.publishedStatus, dto.authorIds) }
    }

    @Test
    fun `createBook - 著者が存在しないとき例外`() {
        val dto = createDto(authorIds = listOf(1L, 2L, 99L))
        every { authorRepositoryImpl.findExistingIds(dto.authorIds) } returns listOf(1L, 2L)

        val exception = assertThrows(InvalidInputException::class.java) {
            bookServiceImpl.create(dto)
        }

        assertEquals("著者が存在しません: id=[99]", exception.message)
    }

    @Test
    fun `updateBook - 正常系`() {
        val id = 10L
        val oldDto = createDto(id = id, publishedStatus = PublishedStatus.UNPUBLISHED, authorIds = listOf(1L))
        val newDto = createDto(id = id, publishedStatus = PublishedStatus.PUBLISHED, authorIds = listOf(1L))

        every { bookRepositoryImpl.findById(id) } returns oldDto
        every {
            bookRepositoryImpl.update(id, newDto.title, newDto.price, newDto.publishedStatus, newDto.authorIds)
        } returns newDto

        val result = bookServiceImpl.update(id, newDto)

        assertEquals(newDto, result)
    }

    @Test
    fun `updateBook - 書籍が存在しない`() {
        every { bookRepositoryImpl.findById(123L) } returns null

        val dto = createDto()

        val ex = assertThrows(InvalidInputException::class.java) {
            bookServiceImpl.update(123L, dto)
        }

        assertEquals("書籍が存在しません: id=123", ex.message)
    }

    @Test
    fun `updateBook - 出版済みから未出版に変更しようとしたら例外`() {
        val id = 5L
        val oldDto = createDto(id = id, publishedStatus = PublishedStatus.PUBLISHED)
        val newDto = createDto(id = id, publishedStatus = PublishedStatus.UNPUBLISHED)

        every { bookRepositoryImpl.findById(id) } returns oldDto

        val ex = assertThrows(InvalidInputException::class.java) {
            bookServiceImpl.update(id, newDto)
        }

        assertEquals("出版済みの書籍を未出版に変更することはできません", ex.message)
    }

    @Test
    fun `updateBook - 著者の変更時に存在しない著者が含まれていると例外`() {
        val id = 7L
        val oldDto = createDto(id = id, authorIds = listOf(1L))
        val newDto = createDto(id = id, authorIds = listOf(1L, 99L))

        every { bookRepositoryImpl.findById(id) } returns oldDto
        every { authorRepositoryImpl.findExistingIds(newDto.authorIds) } returns listOf(1L)

        val ex = assertThrows(InvalidInputException::class.java) {
            bookServiceImpl.update(id, newDto)
        }

        assertEquals("著者が存在しません: id=[99]", ex.message)
    }
}
