package com.induztek.protocolos.data.remote.dto

data class LoginRequestDto(
    val username: String,
    val contrasena: String
)

data class LoginResponseDto(
    val id: Long,
    val username: String,
    val nombre: String,
    val rol: String,
    val token: String
)
