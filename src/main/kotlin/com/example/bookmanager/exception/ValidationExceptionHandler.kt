package com.example.bookmanager.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "不正な値です") }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors)
    }

    @ExceptionHandler(InvalidInputException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidInputException(ex: InvalidInputException): ResponseEntity<Map<String, String>> {
        val body = mapOf("error" to (ex.message ?: "不正な値です"))
        return ResponseEntity.badRequest().body(body)
    }

}