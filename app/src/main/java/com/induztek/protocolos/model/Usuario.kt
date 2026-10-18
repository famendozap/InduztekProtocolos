package com.induztek.protocolos.model

data class Usuario(
    val id: Long,
    val username: String,
    val nombre: String,
    val rol: Rol,
    val token: String? = null // Para el JWT más adelante
)
