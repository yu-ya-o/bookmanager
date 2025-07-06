package com.example.bookmanager.domain.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class BookDto(
    val id: Long?,

    @field:NotBlank
    val title: String,

    @field:NotNull
    @field:Min(0)
    val price: Int,

    @field:NotNull
    val publishedStatus: PublishedStatus,

    @field:NotNull
    @field:Size(min = 1, message = "著者は1人以上必要です")
    val authorIds: List<Long>
)

