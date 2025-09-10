package com.example.leofindit.http.Data

import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.Result
import com.example.leofindit.http.setup.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private const val ApiURL : String = ""

class RemoteAPI(
    private val httpClient : HttpClient
) {
     suspend fun searchAPI(): Result<List<ResultDto>, DataError.Remote> {
        return safeCall {
            httpClient.get (
                urlString = ApiURL
            ) {
                parameter("fields","")
            }.body()
        }
    }
}