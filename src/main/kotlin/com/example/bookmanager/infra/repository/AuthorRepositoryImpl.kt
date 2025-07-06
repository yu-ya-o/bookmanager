package com.example.bookmanager.infra.repository

import com.example.bookmanager.domain.dto.AuthorDto
import com.example.bookmanager.domain.repository.AuthorRepository
import com.example.bookmanager.exception.InvalidInputException
import org.example.bookmanager.db.jooq.gen.tables.Authors.AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 著者リポジトリの実装クラス
 * JOOQを用いて、authorsテーブルへのCRUD操作を担当する。
 */
@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext
) : AuthorRepository {

    /**
     * 指定したIDの著者情報を取得する。
     *
     * @param id 著者ID
     * @return 該当する著者が存在すればそのDTO、存在しなければnull
     */
    override fun findById(id: Long): AuthorDto? {
        val record = dslContext.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()

        return record?.let {
            AuthorDto(
                id = it.id,
                name = it.name,
                birthdate = it.birthDate
            )
        }
    }

    /**
     * 指定されたIDリストのうち、DBに存在するIDのみを返す。
     *
     * @param ids 検索対象の著者IDリスト
     * @return DBに存在する著者IDのリスト
     */
    override fun findExistingIds(ids: Collection<Long>): List<Long> {
        return dslContext.select(AUTHORS.ID)
            .from(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch(AUTHORS.ID)
    }

    /**
     * 著者を新規登録する。
     *
     * @param name 著者名
     * @param birthdate 生年月日
     * @return 登録された著者のDTO
     */
    override fun insert(name: String, birthdate: LocalDate): AuthorDto {
        val record = dslContext.newRecord(AUTHORS).apply {
            this.name = name
            this.birthDate = birthdate
        }
        record.store() // INSERT 実行

        return AuthorDto(
            id = record.id,
            name = record.name,
            birthdate = record.birthDate
        )
    }

    /**
     * 指定されたIDの著者情報を更新する。
     * 該当する著者が存在しない場合は例外をスローする。
     *
     * @param id 著者ID
     * @param name 更新後の著者名
     * @param birthdate 更新後の生年月日
     * @return 更新後の著者DTO
     * @throws InvalidInputException 著者が存在しない場合
     */
    override fun update(id: Long, name: String, birthdate: LocalDate): AuthorDto {
        val record = dslContext.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne() ?: throw InvalidInputException("著者が存在しません: id=$id")

        record.apply {
            this.name = name
            this.birthDate = birthdate
            this.store()  // UPDATE 実行
        }

        return AuthorDto(
            id = record.id,
            name = record.name,
            birthdate = record.birthDate
        )
    }
}