package com.example.bookmanager.service

import com.example.bookmanager.domain.service.impl.AuthorServiceImpl
import com.example.bookmanager.domain.dto.AuthorDto
import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.AuthorRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthorServiceTest {

    private lateinit var authorRepositoryImpl: AuthorRepositoryImpl
    private lateinit var authorServiceImpl: AuthorServiceImpl

    @BeforeEach
    fun setUp() {
        authorRepositoryImpl = mockk()
        authorServiceImpl = AuthorServiceImpl(authorRepositoryImpl)
    }

    private fun createDto(
        id: Long? = null,
        name: String = "テスト著者",
        birthdate: LocalDate = LocalDate.of(1990, 1, 1)
    ) = AuthorDto(id, name, birthdate)

    @Test
    fun `createAuthor - 正常系`() {
        val request = createDto()
        val saved = request.copy(id = 1L)

        every { authorRepositoryImpl.insert(request.name, request.birthdate) } returns saved

        val result = authorServiceImpl.create(request)

        assertEquals(saved, result)
        verify { authorRepositoryImpl.insert(request.name, request.birthdate) }
    }

    @Test
    fun `updateAuthor - 正常系`() {
        val id = 1L
        val request = createDto()
        val updated = request.copy(id = id, name = "更新著者")

        every { authorRepositoryImpl.findById(id) } returns request
        every { authorRepositoryImpl.update(id, request.name, request.birthdate) } returns updated

        val result = authorServiceImpl.update(id, request)

        assertEquals(updated, result)
        verify { authorRepositoryImpl.findById(id) }
        verify { authorRepositoryImpl.update(id, request.name, request.birthdate) }
    }

    @Test
    fun `updateAuthor - 著者が存在しない`() {
        val id = 999L
        val request = createDto()

        every { authorRepositoryImpl.findById(id) } returns null

        val ex = assertThrows(InvalidInputException::class.java) {
            authorServiceImpl.update(id, request)
        }

        assertEquals("著者が存在しません: id=$id", ex.message)
    }
}
