package com.induztek.protocolos.model

data class Usuario(
    val id: Long = 0,
    val username: String,
    val nombre: String,
    val rol: Rol,
    val fotoBytes: ByteArray? = null,
    val token: String? = null,
    val preguntaSeguridad: String? = null,
    val respuestaSeguridad: String? = null,
    // Datos específicos por Rol:
    // TÉCNICO
    val secCertificacion: String? = null,
    val especialidadTecnica: String? = null,
    // INGENIERO
    val rutColegiatura: String? = null,
    val registroProfesional: String? = null,
    // JEFE
    val departamentoSucursal: String? = null,
    val codigoAutorizacion: String? = null,
    // CLIENTE
    val empresaNombre: String? = null,
    val rutEmpresa: String? = null,
    val direccionEmpresa: String? = null,
    val telefonoContacto: String? = null
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
