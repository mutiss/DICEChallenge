package com.mutissx.dicechallenge.core.data

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import kotlinx.coroutines.CancellationException

suspend inline fun <T> safeDbCall(block: suspend () -> T): Result<T, DataError.Local> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toLocalError())
    }
