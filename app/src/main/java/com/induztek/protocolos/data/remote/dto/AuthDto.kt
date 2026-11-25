package com.induztek.protocolos.data.remote.dto

import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario

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
) {
    fun toUsuario() = Usuario(
        id = id,
        username = username,
        nombre = nombre,
        rol = Rol.valueOf(rol),
        token = token
    )
}

data class RegistroRequestDto(
    val username: String,
    val nombre: String,
    val contrasena: String,
    val rol: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String,
    val foto: ByteArray?,
    val secCertificacion: String?,
    val especialidadTecnica: String?,
    val rutColegiatura: String?,
    val registroProfesional: String?,
    val departamentoSucursal: String?,
    val codigoAutorizacion: String?,
    val empresaNombre: String?,
    val rutEmpresa: String?,
    val direccionEmpresa: String?,
    val telefonoContacto: String?
)

data class UsuarioResponseDto(
    val id: Long,
    val username: String,
    val nombre: String,
    val rol: String,
    val foto: ByteArray?,
    val preguntaSeguridad: String?,
    val secCertificacion: String?,
    val especialidadTecnica: String?,
    val rutColegiatura: String?,
    val registroProfesional: String?,
    val departamentoSucursal: String?,
    val codigoAutorizacion: String?,
    val empresaNombre: String?,
    val rutEmpresa: String?,
    val direccionEmpresa: String?,
    val telefonoContacto: String?
) {
    fun toUsuario() = Usuario(
        id = id,
        username = username,
        nombre = nombre,
        rol = Rol.valueOf(rol),
        fotoBytes = foto,
        preguntaSeguridad = preguntaSeguridad,
        secCertificacion = secCertificacion,
        especialidadTecnica = especialidadTecnica,
        rutColegiatura = rutColegiatura,
        registroProfesional = registroProfesional,
        departamentoSucursal = departamentoSucursal,
        codigoAutorizacion = codigoAutorizacion,
        empresaNombre = empresaNombre,
        rutEmpresa = rutEmpresa,
        direccionEmpresa = direccionEmpresa,
        telefonoContacto = telefonoContacto
    )
}

data class RecuperarRequestDto(
    val username: String,
    val respuestaSeguridad: String,
    val nuevaContrasena: String
)

data class CambiarContrasenaRequestDto(
    val contrasenaActual: String,
    val nuevaContrasena: String
)

data class PerfilRequestDto(
    val nombre: String,
    val foto: ByteArray?,
    val secCertificacion: String?,
    val especialidadTecnica: String?,
    val rutColegiatura: String?,
    val registroProfesional: String?,
    val departamentoSucursal: String?,
    val codigoAutorizacion: String?,
    val empresaNombre: String?,
    val rutEmpresa: String?,
    val direccionEmpresa: String?,
    val telefonoContacto: String?
)
