package com.mutissx.dicechallenge.data.remote

import com.mutissx.dicechallenge.core.data.toNetworkError
import com.mutissx.dicechallenge.core.domain.DataError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class NetworkErrorMapperTest {

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun `given a SocketTimeoutException, when toNetworkError is called, then returns REQUEST_TIMEOUT`() {
        assertEquals(DataError.Network.REQUEST_TIMEOUT, SocketTimeoutException().toNetworkError())
    }

    @Test
    fun `given a plain IOException, when toNetworkError is called, then returns NO_INTERNET`() {
        assertEquals(DataError.Network.NO_INTERNET, IOException().toNetworkError())
    }

    @Test
    fun `given a 401 HttpException, when toNetworkError is called, then returns UNAUTHORIZED`() {
        assertEquals(DataError.Network.UNAUTHORIZED, httpException(401).toNetworkError())
    }

    @Test
    fun `given a 404 HttpException, when toNetworkError is called, then returns NOT_FOUND`() {
        assertEquals(DataError.Network.NOT_FOUND, httpException(404).toNetworkError())
    }

    @Test
    fun `given a 4xx HttpException other than 401 or 404, when toNetworkError is called, then returns CLIENT_ERROR`() {
        assertEquals(DataError.Network.CLIENT_ERROR, httpException(400).toNetworkError())
    }

    @Test
    fun `given a 5xx HttpException, when toNetworkError is called, then returns SERVICE_UNAVAILABLE`() {
        assertEquals(DataError.Network.SERVICE_UNAVAILABLE, httpException(503).toNetworkError())
    }

    @Test
    fun `given an unrelated throwable, when toNetworkError is called, then returns UNKNOWN`() {
        assertEquals(DataError.Network.UNKNOWN, RuntimeException("boom").toNetworkError())
    }
}
