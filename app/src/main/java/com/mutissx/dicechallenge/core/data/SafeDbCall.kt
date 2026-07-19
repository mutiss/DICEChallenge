package com.mutissx.dicechallenge.core.data

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> safeDbCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline block: suspend () -> T
): Result<T, DataError.Local> =
    try {
        Result.Success(withContext(dispatcher) { block() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toLocalError())
    }
