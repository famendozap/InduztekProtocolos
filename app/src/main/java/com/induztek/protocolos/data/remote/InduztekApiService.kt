package com.induztek.protocolos.data.remote

import com.induztek.protocolos.data.remote.dto.LoginRequestDto
import com.induztek.protocolos.data.remote.dto.LoginResponseDto
import com.induztek.protocolos.data.remote.dto.CambiarContrasenaRequestDto
import com.induztek.protocolos.data.remote.dto.EquipoRequestDto
import com.induztek.protocolos.data.remote.dto.EquipoResponseDto
import com.induztek.protocolos.data.remote.dto.FormalizarRequestDto
import com.induztek.protocolos.data.remote.dto.PerfilRequestDto
import com.induztek.protocolos.data.remote.dto.ProtocoloRequestDto
import com.induztek.protocolos.data.remote.dto.ProtocoloResponseDto
import com.induztek.protocolos.data.remote.dto.RecuperarRequestDto
import com.induztek.protocolos.data.remote.dto.RegistroRequestDto
import com.induztek.protocolos.data.remote.dto.UsuarioResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InduztekApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/auth/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequestDto): Response<UsuarioResponseDto>

    @POST("api/auth/recuperar")
    suspend fun recuperarContrasena(@Body request: RecuperarRequestDto): Response<Unit>

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(@Query("rol") rol: String? = null): Response<List<UsuarioResponseDto>>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Response<UsuarioResponseDto>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Body request: PerfilRequestDto
    ): Response<UsuarioResponseDto>

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Long,
        @Body request: CambiarContrasenaRequestDto
    ): Response<Unit>

    @GET("api/equipos")
    suspend fun obtenerEquipos(): Response<List<EquipoResponseDto>>

    @POST("api/equipos")
    suspend fun crearEquipo(@Body equipo: EquipoRequestDto): Response<EquipoResponseDto>

    @GET("api/protocolos")
    suspend fun obtenerProtocolos(): Response<List<ProtocoloResponseDto>>

    @GET("api/protocolos/equipo/{equipoId}")
    suspend fun obtenerProtocolosPorEquipo(
        @Path("equipoId") equipoId: Long
    ): Response<List<ProtocoloResponseDto>>

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Body protocolo: ProtocoloRequestDto): Response<ProtocoloResponseDto>

    @PUT("api/protocolos/{id}/formalizar")
    suspend fun formalizarProtocolo(
        @Path("id") id: Long,
        @Body request: FormalizarRequestDto
    ): Response<ProtocoloResponseDto>
}
