package com.example.bookmanager.domain.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class AuthorDto(
    val id: Long?,

    @field:NotBlank(message = "名前は空にできません")
    val name: String,

    @field:NotNull
    @field:Past(message = "生年月日は過去の日付である必要があります")
    val birthdate: LocalDate
)
