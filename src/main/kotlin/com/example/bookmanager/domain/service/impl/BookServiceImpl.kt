package com.example.bookmanager.domain.service.impl

import com.example.bookmanager.domain.dto.PublishedStatus
import com.example.bookmanager.domain.dto.BookDto
import com.example.bookmanager.domain.service.BookService
import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.AuthorRepositoryImpl
import com.example.bookmanager.infra.repository.BookRepositoryImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍サービスの実装クラス
 */
@Service
class BookServiceImpl(
    private val bookRepositoryImpl: BookRepositoryImpl,
    private val authorRepositoryImpl: AuthorRepositoryImpl
) : BookService {

    /**
     * 書籍を新規に登録します。
     *  - 指定された著者IDの存在確認を行います。
     *
     * @param dto 登録する書籍の情報
     * @return 登録された書籍の情報
     */
    @Transactional
    override fun create(dto: BookDto): BookDto {
        // 著者の存在チェック
        val existingIds = authorRepositoryImpl.findExistingIds(dto.authorIds)

        // 著者が未登録であればエラー
        val notFoundIds = dto.authorIds - existingIds
        if (notFoundIds.isNotEmpty()) {
            throw InvalidInputException("著者が存在しません: id=$notFoundIds")
        }

        return bookRepositoryImpl.insert(
            dto.title,
            dto.price,
            dto.publishedStatus,
            dto.authorIds
        )
    }

    /**
     * 書籍情報を更新します。
     * - 出版済み→未出版の変更はできません。
     * - 著者情報を変更する場合は、存在確認を行います。
     *
     * @param id 更新対象の書籍ID
     * @param dto 更新後の書籍情報
     * @return 更新された書籍の情報
     */
    @Transactional
    override fun update(id: Long, dto: BookDto): BookDto {
        // 書籍情報を取得する
        val book = bookRepositoryImpl.findById(id) ?: throw InvalidInputException("書籍が存在しません: id=$id")

        // 出版済み → 未出版 への変更は禁止
        if (book.publishedStatus == PublishedStatus.PUBLISHED &&
            dto.publishedStatus == PublishedStatus.UNPUBLISHED) {
            throw InvalidInputException("出版済みの書籍を未出版に変更することはできません")
        }

        // 著者の更新がある場合、著者の存在チェック
        if (book.authorIds.sorted() != dto.authorIds.sorted()) {
            val existingIds = authorRepositoryImpl.findExistingIds(dto.authorIds)

            // 著者が未登録であればエラー
            val notFoundIds = dto.authorIds - existingIds
            if (notFoundIds.isNotEmpty()) {
                throw InvalidInputException("著者が存在しません: id=$notFoundIds")
            }
        }

        return bookRepositoryImpl.update(
            id,
            dto.title,
            dto.price,
            dto.publishedStatus,
            dto.authorIds
        )
    }

    /**
     * 指定された著者IDに紐づく書籍一覧を取得します。
     *
     * @param authorId 著者ID
     * @return 書籍一覧
     */
    override fun getBooksByAuthorId(authorId: Long): List<BookDto> {
        return bookRepositoryImpl.findBooksByAuthorId(authorId)
    }
}
