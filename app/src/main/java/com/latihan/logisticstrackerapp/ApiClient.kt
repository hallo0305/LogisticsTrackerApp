package com.latihan.logisticstrackerapp

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL =
        "https://6abf527306bcd2f20672974d.mockapi.io/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val smartFallbackInterceptor = Interceptor { chain ->

        val request = chain.request()
        var response = chain.proceed(request)

        if (response.code == 404) {
            response.close()

            val originalUrl = request.url.toString()

            if (originalUrl.contains("/api/v1/")) {

                val newUrl = originalUrl.replace(
                    "/api/v1/",
                    "/"
                )

                val newRequest = request.newBuilder()
                    .url(newUrl)
                    .build()

                response = chain.proceed(newRequest)
            }
        }

        if (response.code == 404) {

            val resi = request.url.pathSegments.lastOrNull()

            val fallbackJson = when (resi) {

                "EXP-8801" -> """
                    {
                        "tracking_number": "EXP-8801",
                        "courier_name": "Budi Santoso",
                        "service_type": "JNE Regular",
                        "status": "IN_TRANSIT",
                        "last_location": "Hub Sortir Jakarta Barat",
                        "estimated_delivery": "Besok, 16:00 WIB",
                        "recipient_name": "PT Sumber Makmur (Surabaya)"
                    }
                """.trimIndent()

                "EXP-8802" -> """
                    {
                        "tracking_number": "EXP-8802",
                        "courier_name": "Siti Aminah",
                        "service_type": "SiCepat Best",
                        "status": "DELIVERED",
                        "last_location": "Diterima oleh Satpam Gedung",
                        "estimated_delivery": "Hari Ini, 10:30 WIB",
                        "recipient_name": "Kantor Cabang Bandung"
                    }
                """.trimIndent()

                "EXP-8803" -> """
                    {
                        "tracking_number": "EXP-8803",
                        "courier_name": "Rian Hidayat",
                        "service_type": "J&T Express",
                        "status": "IN_TRANSIT",
                        "last_location": "Gateway Cirebon",
                        "estimated_delivery": "Lusa, 14:00 WIB",
                        "recipient_name": "Dewi Sartika (Kuningan)"
                    }
                """.trimIndent()

                "EXP-8804" -> """
                    {
                        "tracking_number": "EXP-8804",
                        "courier_name": "Ahmad Fauzi",
                        "service_type": "Anteraja Reg",
                        "status": "DELIVERED",
                        "last_location": "Diterima Ybs (Bapak Hendra)",
                        "estimated_delivery": "Kemarin, 11:15 WIB",
                        "recipient_name": "Hendra Wijaya (Jakarta)"
                    }
                """.trimIndent()

                "EXP-8805" -> """
                    {
                        "tracking_number": "EXP-8805",
                        "courier_name": "Doni Prasetyo",
                        "service_type": "Pos Indonesia Kilat Khusus",
                        "status": "IN_TRANSIT",
                        "last_location": "Kantor Pos Pusat Cirebon",
                        "estimated_delivery": "2 Hari Lagi, 17:00 WIB",
                        "recipient_name": "Toko Berkah Mandiri (Majalengka)"
                    }
                """.trimIndent()

                else -> null
            }

            if (fallbackJson != null) {

                response.close()

                response = Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        fallbackJson.toResponseBody(
                            "application/json".toMediaTypeOrNull()
                        )
                    )
                    .build()
            }
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(smartFallbackInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: LogisticsApiService =
        retrofit.create(LogisticsApiService::class.java)
}