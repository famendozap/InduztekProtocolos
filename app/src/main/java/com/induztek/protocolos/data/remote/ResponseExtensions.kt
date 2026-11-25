package com.induztek.protocolos.data.remote

import retrofit2.HttpException
import retrofit2.Response

internal class EmptyApiResponseException : IllegalStateException("La API respondió sin contenido.")

internal fun <T : Any> Response<T>.requireBody(): T {
    if (!isSuccessful) throw HttpException(this)
    return body() ?: throw EmptyApiResponseException()
}
