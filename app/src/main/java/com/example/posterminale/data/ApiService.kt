package com.example.posterminale.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import okhttp3.ResponseBody

interface ApiService {
    @POST("transaction")
    @Headers("Content-Type: application/octet-stream")
    suspend fun sendPacket(@Body body: okhttp3.RequestBody): Response<ResponseBody>
}
