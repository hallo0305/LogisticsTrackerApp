package com.latihan.logisticstrackerapp
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface LogisticsApiService {
    // 1. Endpoint simulasi mengambil data pelacakan resi spesifik
    @GET("api/v1/tracking/{resi}")
    suspend fun getTrackingDetail(
        @Path("resi") resiNumber: String
    ): Response<TrackingResponse>
    // 2. Endpoint pelacakan dengan query parameter filter
    @GET("api/v1/tracking/search")
    suspend fun searchTracking(
        @Query("query") keyword: String
    ): Response<List<TrackingResponse>>
}