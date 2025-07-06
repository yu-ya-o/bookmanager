package com.example.bookmanager.domain.service

import com.example.bookmanager.domain.dto.AuthorDto

/**
 * 著者サービスインターフェース
 */
interface AuthorService {
    /**
     * 著者を新規作成します。
     */
    fun create(dto: AuthorDto): AuthorDto

    /**
     * 指定されたIDの著者を更新します。
     */
    fun update(id: Long, dto: AuthorDto): AuthorDto
}
