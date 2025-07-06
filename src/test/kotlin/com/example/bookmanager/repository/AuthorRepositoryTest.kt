package com.example.bookmanager.repository

import com.example.bookmanager.exception.InvalidInputException
import com.example.bookmanager.infra.repository.AuthorRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import org.example.bookmanager.db.jooq.gen.tables.Authors.AUTHORS
import org.example.bookmanager.db.jooq.gen.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthorRepositoryTest {

    private lateinit var dslContext: DSLContext
    private lateinit var authorRepositoryImpl: AuthorRepositoryImpl

    @BeforeEach
    fun setUp() {
        dslContext = mockk(relaxed = true)
        authorRepositoryImpl = AuthorRepositoryImpl(dslContext)
    }

    @Test
    fun `findById - 著者が存在する`() {
        val id = 1L
        val record = mockk<AuthorsRecord> {
            every { this@mockk.id } returns id
            every { name } returns "テスト著者"
            every { birthDate } returns LocalDate.of(1990, 1, 1)
        }
        every { dslContext.selectFrom(AUTHORS).where(AUTHORS.ID.eq(id)).fetchOne() } returns record

        val result = authorRepositoryImpl.findById(id)

        assertEquals(id, result?.id)
        assertEquals("テスト著者", result?.name)
        assertEquals(LocalDate.of(1990, 1, 1), result?.birthdate)
    }

    @Test
    fun `findById - 著者が存在しない`() {
        every { dslContext.selectFrom(AUTHORS).where(AUTHORS.ID.eq(123L)).fetchOne() } returns null

        val result = authorRepositoryImpl.findById(123L)

        assertNull(result)
    }

    @Test
    fun `findExistingIds - 一部存在する`() {
        val ids = listOf(1L, 2L, 3L)
        every {
            dslContext.select(AUTHORS.ID).from(AUTHORS).where(AUTHORS.ID.`in`(ids)).fetch(AUTHORS.ID)
        } returns listOf(1L, 3L)

        val result = authorRepositoryImpl.findExistingIds(ids)

        assertEquals(listOf(1L, 3L), result)
    }

    @Test
    fun `insert - 著者を登録`() {
        val name = "新規著者"
        val birthdate = LocalDate.of(1980, 5, 10)
        val record = mockk<AuthorsRecord>(relaxed = true)

        every { dslContext.newRecord(AUTHORS) } returns record
        every { record.store() } returns 1
        every { record.id } returns 100L
        every { record.name } returns name
        every { record.birthDate } returns birthdate

        val result = authorRepositoryImpl.insert(name, birthdate)

        assertEquals(100L, result.id)
        assertEquals(name, result.name)
        assertEquals(birthdate, result.birthdate)
    }

    @Test
    fun `update - 著者が存在する`() {
        val id = 1L
        val name = "更新後の名前"
        val birthdate = LocalDate.of(1992, 2, 2)
        val record = mockk<AuthorsRecord>(relaxed = true)

        every { dslContext.selectFrom(AUTHORS).where(AUTHORS.ID.eq(id)).fetchOne() } returns record
        every { record.store() } returns 1
        every { record.id } returns id
        every { record.name } returns name
        every { record.birthDate } returns birthdate

        val result = authorRepositoryImpl.update(id, name, birthdate)

        assertEquals(id, result.id)
        assertEquals(name, result.name)
        assertEquals(birthdate, result.birthdate)
    }

    @Test
    fun `update - 著者が存在しない`() {
        val id = 999L
        every { dslContext.selectFrom(AUTHORS).where(AUTHORS.ID.eq(id)).fetchOne() } returns null

        val ex = assertThrows<InvalidInputException> {
            authorRepositoryImpl.update(id, "無名", LocalDate.of(2000, 1, 1))
        }

        assertEquals("著者が存在しません: id=$id", ex.message)
    }
}
