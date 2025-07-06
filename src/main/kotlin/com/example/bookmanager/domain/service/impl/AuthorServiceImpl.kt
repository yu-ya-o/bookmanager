package com.example.bookmanager.domain.service.impl

import com.example.bookmanager.domain.dto.AuthorDto
import com.example.bookmanager.domain.service.AuthorService
import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.AuthorRepositoryImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者サービスの実装クラス
 */
@Service
class AuthorServiceImpl(
    private val authorRepositoryImpl: AuthorRepositoryImpl
) : AuthorService {

    /**
     * 著者を新規に登録します。
     *
     * @param dto 登録する著者情報
     * @return 登録された著者情報
     */
    @Transactional
    override fun create(dto: AuthorDto): AuthorDto {
        return authorRepositoryImpl.insert(
            dto.name,
            dto.birthdate
        )
    }

    /**
     * 指定されたIDの著者を更新します。
     *
     * @param id 更新対象の著者ID
     * @param dto 更新する著者情報
     * @return 更新後の著者情報
     */
    @Transactional
    override fun update(id: Long, dto: AuthorDto): AuthorDto {
        // 著者の存在チェック
        authorRepositoryImpl.findById(id) ?: throw InvalidInputException("著者が存在しません: id=$id")

        // 著者の更新
        return authorRepositoryImpl.update(
            id,
            dto.name,
            dto.birthdate
        )
    }
}
