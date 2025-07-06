package com.example.bookmanager.controller

import com.example.bookmanager.domain.dto.AuthorDto
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.domain.service.impl.AuthorServiceImpl
import com.example.bookmanager.domain.service.impl.BookServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@WebMvcTest(AuthorController::class)
class AuthorControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var authorServiceImpl: AuthorServiceImpl

    @MockitoBean
    lateinit var bookServiceImpl: BookServiceImpl

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createValidAuthorDto(): AuthorDto {
        return AuthorDto(
            id = null,
            name = "テスト著者",
            birthdate = LocalDate.of(1990, 1, 1)
        )
    }

    @Test
    fun `POST authors - 正常系`() {
        val request = createValidAuthorDto()
        val response = request.copy(id = 1L)

        `when`(authorServiceImpl.create(request)).thenReturn(response)

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("テスト著者"))
    }

    @Test
    fun `POST authors - バリデーションエラー（名前が空）`() {
        val invalidRequest = createValidAuthorDto().copy(name = "")

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.name").value("名前は空にできません"))
    }

    @Test
    fun `POST authors - バリデーションエラー（生年月日が未来）`() {
        val invalidRequest = createValidAuthorDto().copy(birthdate = LocalDate.now().plusDays(1))

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.birthdate").value("生年月日は過去の日付である必要があります"))
    }

    @Test
    fun `PUT authors - 正常系`() {
        val request = createValidAuthorDto()
        val response = request.copy(id = 1L, name = "更新著者")

        `when`(authorServiceImpl.update(1L, request)).thenReturn(response)

        mockMvc.perform(
            put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("更新著者"))
    }

    @Test
    fun `GET authors - 著者に紐づく書籍一覧取得`() {
        val books = listOf(
            BookDto(1L, "タイトル1", 1500, PublishedStatus.PUBLISHED, listOf(1L)),
            BookDto(2L, "タイトル2", 1800, PublishedStatus.UNPUBLISHED, listOf(1L))
        )

        `when`(bookServiceImpl.getBooksByAuthorId(1L)).thenReturn(books)

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("タイトル1"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].title").value("タイトル2"))
    }
}
