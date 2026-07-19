package com.mutissx.dicechallenge.core.data

import android.database.sqlite.SQLiteFullException
import com.mutissx.dicechallenge.core.domain.DataError
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalErrorMapperTest {

    @Test
    fun `given a SQLiteFullException, when toLocalError is called, then returns DISK_FULL`() {
        assertEquals(DataError.Local.DISK_FULL, SQLiteFullException().toLocalError())
    }

    @Test
    fun `given an unrelated throwable, when toLocalError is called, then returns UNKNOWN`() {
        assertEquals(DataError.Local.UNKNOWN, RuntimeException("boom").toLocalError())
    }
}
