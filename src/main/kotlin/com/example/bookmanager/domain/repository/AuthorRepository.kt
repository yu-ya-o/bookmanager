package com.example.bookmanager.domain.repository

import com.example.bookmanager.domain.dto.AuthorDto
import java.time.LocalDate

/**
 * 著者リポジトリインターフェース
 */
interface AuthorRepository {
    /**
     * 指定された ID の著者情報を取得します。
     */
    fun findById(id: Long): AuthorDto?

    /**
     * 指定された ID 群のうち、データソースに存在する著者IDを返します。
     */
    fun findExistingIds(ids: Collection<Long>): List<Long>

    /**
     * 著者を新規登録します。
     */
    fun insert(name: String, birthdate: LocalDate): AuthorDto

    /**
     * 指定された ID の著者情報を更新します。
     */
    fun update(id: Long, name: String, birthdate: LocalDate): AuthorDto
}