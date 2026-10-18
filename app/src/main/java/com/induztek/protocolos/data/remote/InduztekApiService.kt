package com.induztek.protocolos.data.remote

import com.induztek.protocolos.data.remote.dto.LoginRequestDto
import com.induztek.protocolos.data.remote.dto.LoginResponseDto
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Protocolo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InduztekApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/equipos")
    suspend fun obtenerEquipos(): Response<List<Equipo>>

    @POST("api/equipos")
    suspend fun crearEquipo(@Body equipo: Equipo): Response<Equipo>

    @GET("api/protocolos/equipo/{equipoId}")
    suspend fun obtenerProtocolosPorEquipo(@Path("equipoId") equipoId: Long): Response<List<Protocolo>>

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Body protocolo: Protocolo): Response<Protocolo>
}
