package com.example.bookmanager.controller

import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.service.impl.BookServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.Mockito.`when`

@WebMvcTest(BookController::class)
class BookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var bookServiceImpl: BookServiceImpl

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createValidBookDto(): BookDto {
        return BookDto(
            id = null,
            title = "テストタイトル",
            price = 1500,
            publishedStatus = PublishedStatus.PUBLISHED,
            authorIds = listOf(1L)
        )
    }

    @Test
    fun `POST books - 正常系`() {
        val request = createValidBookDto()
        val response = request.copy(id = 1L)

        `when`(bookServiceImpl.create(request)).thenReturn(response)

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("テストタイトル"))
    }

    @Test
    fun `POST books - バリデーションエラー（タイトル空）`() {
        val invalidRequest = createValidBookDto().copy(title = "")

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("must not be blank"))
    }

    @Test
    fun `POST books - バリデーションエラー（価格0未満）`() {
        val invalidRequest = createValidBookDto().copy(price = -1)

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.price").value("must be greater than or equal to 0"))
    }

    @Test
    fun `PUT books - 正常系`() {
        val request = createValidBookDto()
        val updated = request.copy(id = 1L, title = "更新タイトル")

        `when`(bookServiceImpl.update(1L, request)).thenReturn(updated)

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("更新タイトル"))
    }

    @Test
    fun `PUT books - バリデーションエラー（著者が空）`() {
        val invalidRequest = createValidBookDto().copy(authorIds = emptyList())

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.authorIds").value("著者は1人以上必要です"))
    }
}