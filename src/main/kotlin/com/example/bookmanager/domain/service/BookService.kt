package com.example.bookmanager.domain.service

import com.example.bookmanager.domain.dto.BookDto

/**
 * 書籍サービスインターフェース
 */
interface BookService {
    /**
     * 書籍を新規に登録します。
     */
    fun create(dto: BookDto): BookDto

    /**
     * 指定されたIDの書籍情報を更新します。
     */
    fun update(id: Long, dto: BookDto): BookDto

    /**
     * 指定された著者IDに紐づく書籍一覧を取得します。
     */
    fun getBooksByAuthorId(authorId: Long): List<BookDto>
}
