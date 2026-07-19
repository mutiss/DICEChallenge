package com.mutissx.dicechallenge.core.data

import android.database.sqlite.SQLiteFullException
import com.mutissx.dicechallenge.core.domain.DataError

fun Throwable.toLocalError(): DataError.Local = when (this) {
    is SQLiteFullException -> DataError.Local.DISK_FULL
    else -> DataError.Local.UNKNOWN
}
