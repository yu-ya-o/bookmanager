package com.example.bookmanager.domain.repository

import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.dto.PublishedStatus

/**
 * 書籍リポジトリインターフェース
 */
interface BookRepository {
    /**
     * 書籍を新規登録します。
     */
    fun insert(
        title: String,
        price: Int,
        publishStatus: PublishedStatus,
        authorIds: List<Long>
    ): BookDto

    /**
     * 指定されたIDの書籍情報を更新します。
     */
    fun update(
        id: Long,
        title: String,
        price: Int,
        publishStatus: PublishedStatus,
        authorIds: List<Long>
    ): BookDto

    /**
     * 書籍IDを指定して、書籍情報を1件取得します。
     */
    fun findById(id: Long): BookDto?

    /**
     * 指定した著者IDに紐づく書籍一覧を取得します。
     */
    fun findBooksByAuthorId(authorId: Long): List<BookDto>
}
