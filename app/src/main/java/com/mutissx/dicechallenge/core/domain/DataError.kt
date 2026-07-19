package com.mutissx.dicechallenge.core.domain

sealed interface DataError {
    enum class Network : DataError {
        SERVICE_UNAVAILABLE, // 5xx
        CLIENT_ERROR,        // 4xx
        UNAUTHORIZED,        // 401
        NOT_FOUND,           // 404
        NO_INTERNET,         // IOException
        REQUEST_TIMEOUT,     // SocketTimeoutException
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,   // SQLiteFullException
        UNKNOWN
    }
}