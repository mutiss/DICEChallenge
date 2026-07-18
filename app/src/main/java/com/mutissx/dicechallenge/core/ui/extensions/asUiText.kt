package com.mutissx.dicechallenge.core.ui.extensions

import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.ui.UiText

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.no_internet)
        DataError.Network.NOT_FOUND -> UiText.StringResource(R.string.not_found)
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.server_error)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.unauthorized)
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.timed_out)
        DataError.Network.CLIENT_ERROR,
        DataError.Network.UNKNOWN -> UiText.StringResource(R.string.unknown_error)
    }
}