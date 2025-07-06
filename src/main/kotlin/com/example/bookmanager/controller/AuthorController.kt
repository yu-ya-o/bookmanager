package com.example.bookmanager.controller

import com.example.bookmanager.domain.dto.AuthorDto
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.service.impl.AuthorServiceImpl
import com.example.bookmanager.domain.service.impl.BookServiceImpl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authors")
class AuthorController(
    private val authorServiceImpl: AuthorServiceImpl,
    private val bookServiceImpl: BookServiceImpl
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody author: AuthorDto): AuthorDto {
        return authorServiceImpl.create(author)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody author: AuthorDto): AuthorDto {
        return authorServiceImpl.update(id, author)
    }

    @GetMapping("/{id}/books")
    fun getBooksByAuthor(@PathVariable id: Long): List<BookDto> {
        return bookServiceImpl.getBooksByAuthorId(id)
    }
}