package com.example.bookmanager.controller

import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.service.impl.BookServiceImpl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookServiceImpl: BookServiceImpl) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody book: BookDto): BookDto {
        return bookServiceImpl.create(book)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody book: BookDto): BookDto {
        return bookServiceImpl.update(id, book)
    }
}
