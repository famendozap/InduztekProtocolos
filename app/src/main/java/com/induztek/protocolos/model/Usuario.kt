package com.induztek.protocolos.model

data class Usuario(
    val id: Long,
    val username: String,
    val nombre: String,
    val rol: Rol,
    val fotoBytes: ByteArray? = null,
    val token: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Usuario
        return id == other.id && username == other.username && nombre == other.nombre && rol == other.rol
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + rol.hashCode()
        return result
    }
}
