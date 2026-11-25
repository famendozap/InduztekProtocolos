package com.induztek.protocolos.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.TipoEquipo
import com.induztek.protocolos.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek_sqlite.db"
        private const val VERSION_BD = 6

        private const val TABLA_USUARIOS = "usuarios"
        private const val US_ID = "id"
        private const val US_USERNAME = "username"
        private const val US_NOMBRE = "nombre"
        private const val US_CONTRASENA = "contrasena"
        private const val US_ROL = "rol"
        private const val US_FOTO = "fotoBytes"
        private const val US_PREGUNTA_SEG = "preguntaSeguridad"
        private const val US_RESPUESTA_SEG = "respuestaSeguridad"
        private const val US_SEC_CERT = "secCertificacion"
        private const val US_ESPECIALIDAD = "especialidadTecnica"
        private const val US_RUT_COLEGIATURA = "rutColegiatura"
        private const val US_REG_PROFESIONAL = "registroProfesional"
        private const val US_DEPTO = "departamentoSucursal"
        private const val US_COD_AUT = "codigoAutorizacion"
        private const val US_EMP_NOMBRE = "empresaNombre"
        private const val US_EMP_RUT = "rutEmpresa"
        private const val US_EMP_DIR = "direccionEmpresa"
        private const val US_EMP_TEL = "telefonoContacto"

        private const val TABLA_EQUIPOS = "equipos"
        private const val EQ_ID = "id"
        private const val EQ_CODIGO = "codigo"
        private const val EQ_TIPO = "tipo"
        private const val EQ_INSTALACION = "instalacionCliente"
        private const val EQ_MARCA = "marca"
        private const val EQ_MODELO = "modelo"
        private const val EQ_SERIE = "numeroSerie"
        private const val EQ_POTENCIA = "potenciaKVA"
        private const val EQ_TENSION = "tensionNominalKV"
        private const val EQ_CLIENTE_ID = "clienteId"
        private const val EQ_CLIENTE_NOMBRE = "clienteNombre"
        private const val EQ_GPS = "ubicacionGPS"

        private const val TABLA_PROTOCOLOS = "protocolos"
        private const val PR_ID = "id"
        private const val PR_EQUIPO_ID = "equipoId"
        private const val PR_FECHA_HORA = "fechaHora"
        private const val PR_MEDICIONES_JSON = "medicionesJson"
        private const val PR_ESTADO = "estado"
        private const val PR_OBSERVACIONES = "observaciones"
        private const val PR_FOTO_EVIDENCIA = "fotoEvidenciaBytes"
        private const val PR_TECNICO_NOM = "tecnicoNombre"
        private const val PR_INGENIERO_NOM = "ingenieroNombre"
        private const val PR_TEMP_AMB = "temperaturaAmbiente"
        private const val PR_HUMEDAD = "humedadRelativa"
        private const val PR_INSTRUMENTOS = "instrumentosUsados"
        private const val PR_RESULTADO_GEN = "resultadoGeneral"
        private const val PR_GPS = "ubicacionGPS"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_USUARIOS (" +
                "$US_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$US_USERNAME TEXT NOT NULL UNIQUE, " +
                "$US_NOMBRE TEXT NOT NULL, " +
                "$US_CONTRASENA TEXT NOT NULL, " +
                "$US_ROL TEXT NOT NULL, " +
                "$US_FOTO BLOB, " +
                "$US_PREGUNTA_SEG TEXT, " +
                "$US_RESPUESTA_SEG TEXT, " +
                "$US_SEC_CERT TEXT, " +
                "$US_ESPECIALIDAD TEXT, " +
                "$US_RUT_COLEGIATURA TEXT, " +
                "$US_REG_PROFESIONAL TEXT, " +
                "$US_DEPTO TEXT, " +
                "$US_COD_AUT TEXT, " +
                "$US_EMP_NOMBRE TEXT, " +
                "$US_EMP_RUT TEXT, " +
                "$US_EMP_DIR TEXT, " +
                "$US_EMP_TEL TEXT)"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT OR IGNORE INTO $TABLA_USUARIOS (" +
                "$US_USERNAME, $US_NOMBRE, $US_CONTRASENA, $US_ROL, $US_PREGUNTA_SEG, $US_RESPUESTA_SEG, " +
                "$US_SEC_CERT, $US_ESPECIALIDAD, $US_RUT_COLEGIATURA, $US_REG_PROFESIONAL, $US_DEPTO, $US_COD_AUT, " +
                "$US_EMP_NOMBRE, $US_EMP_RUT, $US_EMP_DIR, $US_EMP_TEL) VALUES " +
                "('tecnico', 'Juan Técnico', 'Induztek#2025', 'TECNICO', '¿Ciudad de nacimiento?', 'santiago', 'SEC-Clase-A-8841', 'Transformadores y Alta Tensión', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                "('ingeniero', 'Ana Ingeniera', 'Induztek#2025', 'INGENIERO', '¿Ciudad de nacimiento?', 'santiago', NULL, NULL, 'CIP-992314', 'Reg. Sup. N° 45', NULL, NULL, NULL, NULL, NULL, NULL), " +
                "('jefe', 'Carlos Jefe', 'Induztek#2025', 'JEFE', '¿Ciudad de nacimiento?', 'santiago', NULL, NULL, NULL, NULL, 'Supervisión Central Santiago', 'AUT-JEFE-2025', NULL, NULL, NULL, NULL), " +
                "('cliente', 'Empresa Cliente Lampa', 'Induztek#2025', 'CLIENTE', '¿Ciudad de nacimiento?', 'santiago', NULL, NULL, NULL, NULL, NULL, NULL, 'Minera & Ind. Lampa S.A.', '76.543.210-9', 'Av. La Montaña 450, Sala Eléctrica N°2, Lampa', '+56 9 8765 4321')"

        private const val SQL_CREAR_EQUIPOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_EQUIPOS (" +
                "$EQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$EQ_CODIGO TEXT NOT NULL, " +
                "$EQ_TIPO TEXT NOT NULL, " +
                "$EQ_INSTALACION TEXT NOT NULL, " +
                "$EQ_MARCA TEXT, " +
                "$EQ_MODELO TEXT, " +
                "$EQ_SERIE TEXT, " +
                "$EQ_POTENCIA REAL, " +
                "$EQ_TENSION REAL, " +
                "$EQ_CLIENTE_ID INTEGER, " +
                "$EQ_CLIENTE_NOMBRE TEXT, " +
                "$EQ_GPS TEXT)"

        private const val SQL_CREAR_PROTOCOLOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_PROTOCOLOS (" +
                "$PR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PR_EQUIPO_ID INTEGER NOT NULL, " +
                "$PR_FECHA_HORA TEXT NOT NULL, " +
                "$PR_MEDICIONES_JSON TEXT NOT NULL, " +
                "$PR_ESTADO TEXT NOT NULL, " +
                "$PR_OBSERVACIONES TEXT, " +
                "$PR_FOTO_EVIDENCIA BLOB, " +
                "$PR_TECNICO_NOM TEXT, " +
                "$PR_INGENIERO_NOM TEXT, " +
                "$PR_TEMP_AMB REAL, " +
                "$PR_HUMEDAD REAL, " +
                "$PR_INSTRUMENTOS TEXT, " +
                "$PR_RESULTADO_GEN TEXT, " +
                "$PR_GPS TEXT, " +
                "FOREIGN KEY($PR_EQUIPO_ID) REFERENCES $TABLA_EQUIPOS($EQ_ID))"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        try {
            db.setForeignKeyConstraintsEnabled(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(SQL_CREAR_USUARIOS)
            db.execSQL(SQL_PRECARGAR_USUARIOS)
            db.execSQL(SQL_CREAR_EQUIPOS)
            db.execSQL(SQL_CREAR_PROTOCOLOS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion < 6) {
                db.execSQL("DROP TABLE IF EXISTS $TABLA_PROTOCOLOS")
                db.execSQL("DROP TABLE IF EXISTS $TABLA_EQUIPOS")
                db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
                onCreate(db)
            } else {
                asegurarColumnas(db)
            }
        } catch (e: Exception) {
            onCreate(db)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        asegurarColumnas(db)
    }

    private fun asegurarColumnas(db: SQLiteDatabase) {
        try {
            db.execSQL(SQL_CREAR_USUARIOS)
            db.execSQL(SQL_CREAR_EQUIPOS)
            db.execSQL(SQL_CREAR_PROTOCOLOS)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val colsUsuarios = obtenerNombresColumnas(db, TABLA_USUARIOS)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_PREGUNTA_SEG, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_RESPUESTA_SEG, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_SEC_CERT, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_ESPECIALIDAD, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_RUT_COLEGIATURA, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_REG_PROFESIONAL, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_DEPTO, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_COD_AUT, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_EMP_NOMBRE, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_EMP_RUT, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_EMP_DIR, "TEXT", colsUsuarios)
            agregarColumnaSiNoExiste(db, TABLA_USUARIOS, US_EMP_TEL, "TEXT", colsUsuarios)

            val colsEquipos = obtenerNombresColumnas(db, TABLA_EQUIPOS)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_MARCA, "TEXT", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_MODELO, "TEXT", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_SERIE, "TEXT", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_POTENCIA, "REAL", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_TENSION, "REAL", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_CLIENTE_ID, "INTEGER", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_CLIENTE_NOMBRE, "TEXT", colsEquipos)
            agregarColumnaSiNoExiste(db, TABLA_EQUIPOS, EQ_GPS, "TEXT", colsEquipos)

            val colsProtocolos = obtenerNombresColumnas(db, TABLA_PROTOCOLOS)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_TECNICO_NOM, "TEXT", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_INGENIERO_NOM, "TEXT", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_TEMP_AMB, "REAL", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_HUMEDAD, "REAL", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_INSTRUMENTOS, "TEXT", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_RESULTADO_GEN, "TEXT", colsProtocolos)
            agregarColumnaSiNoExiste(db, TABLA_PROTOCOLOS, PR_GPS, "TEXT", colsProtocolos)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            db.execSQL(SQL_PRECARGAR_USUARIOS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun obtenerNombresColumnas(db: SQLiteDatabase, tabla: String): Set<String> {
        val columnas = mutableSetOf<String>()
        try {
            db.rawQuery("PRAGMA table_info($tabla)", null).use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx != -1) columnas.add(cursor.getString(nameIdx))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return columnas
    }

    private fun agregarColumnaSiNoExiste(db: SQLiteDatabase, tabla: String, columna: String, tipo: String, columnasExistentes: Set<String>) {
        if (!columnasExistentes.contains(columna)) {
            try {
                db.execSQL("ALTER TABLE $tabla ADD COLUMN $columna $tipo")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun registrarUsuario(
        username: String,
        nombre: String,
        contrasena: String,
        rol: String,
        fotoBytes: ByteArray? = null,
        preguntaSeguridad: String? = null,
        respuestaSeguridad: String? = null,
        secCertificacion: String? = null,
        especialidadTecnica: String? = null,
        rutColegiatura: String? = null,
        registroProfesional: String? = null,
        departamentoSucursal: String? = null,
        codigoAutorizacion: String? = null,
        empresaNombre: String? = null,
        rutEmpresa: String? = null,
        direccionEmpresa: String? = null,
        telefonoContacto: String? = null
    ): Long {
        val userClean = username.trim().lowercase()
        // Verificar primero si el nombre de usuario ya existe en la base de datos
        if (obtenerUsuarioPorUsername(userClean) != null) {
            return -2L // Código especial: El usuario ya existe
        }

        return try {
            val valores = ContentValues().apply {
                put(US_USERNAME, userClean)
                put(US_NOMBRE, nombre.trim())
                put(US_CONTRASENA, contrasena)
                put(US_ROL, rol)
                put(US_PREGUNTA_SEG, preguntaSeguridad ?: "¿Ciudad de nacimiento?")
                put(US_RESPUESTA_SEG, respuestaSeguridad?.trim()?.lowercase() ?: "santiago")
                if (fotoBytes != null) put(US_FOTO, fotoBytes)

                put(US_SEC_CERT, secCertificacion)
                put(US_ESPECIALIDAD, especialidadTecnica)
                put(US_RUT_COLEGIATURA, rutColegiatura)
                put(US_REG_PROFESIONAL, registroProfesional)
                put(US_DEPTO, departamentoSucursal)
                put(US_COD_AUT, codigoAutorizacion)
                put(US_EMP_NOMBRE, empresaNombre)
                put(US_EMP_RUT, rutEmpresa)
                put(US_EMP_DIR, direccionEmpresa)
                put(US_EMP_TEL, telefonoContacto)
            }
            val id = writableDatabase.insert(TABLA_USUARIOS, null, valores)
            if (id == -1L) {
                Log.e("InduztekDbHelper", "Error al insertar en usuarios para username: $userClean")
            }
            id
        } catch (e: Exception) {
            Log.e("InduztekDbHelper", "Excepción al registrar usuario $userClean", e)
            -1L
        }
    }

    fun autenticarUsuario(username: String, contrasena: String): Usuario? {
        val usernameLimpio = username.trim().lowercase()
        return try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_USUARIOS WHERE $US_USERNAME = ? AND $US_CONTRASENA = ?",
                arrayOf(usernameLimpio, contrasena)
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    leerUsuario(cursor)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun obtenerUsuarioPorUsername(username: String): Usuario? {
        val usernameLimpio = username.trim().lowercase()
        return try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_USUARIOS WHERE $US_USERNAME = ? LIMIT 1",
                arrayOf(usernameLimpio)
            ).use { cursor ->
                if (cursor.moveToNext()) leerUsuario(cursor) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun obtenerClientes(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_USUARIOS WHERE $US_ROL = ? ORDER BY $US_NOMBRE ASC",
                arrayOf(Rol.CLIENTE.name)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    lista.add(leerUsuario(cursor))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    fun recuperarContrasena(username: String, respuestaSeguridad: String, nuevaContrasena: String): Boolean {
        val usernameLimpio = username.trim().lowercase()
        val respuestaLimpia = respuestaSeguridad.trim().lowercase()
        return try {
            readableDatabase.rawQuery(
                "SELECT $US_ID, $US_RESPUESTA_SEG FROM $TABLA_USUARIOS WHERE $US_USERNAME = ?",
                arrayOf(usernameLimpio)
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    val respGuardada = cursor.getString(cursor.getColumnIndexOrThrow(US_RESPUESTA_SEG)) ?: ""
                    if (respGuardada.trim().lowercase() == respuestaLimpia) {
                        val userId = cursor.getLong(cursor.getColumnIndexOrThrow(US_ID))
                        val cv = ContentValues().apply { put(US_CONTRASENA, nuevaContrasena) }
                        writableDatabase.update(TABLA_USUARIOS, cv, "$US_ID = ?", arrayOf(userId.toString())) > 0
                    } else false
                } else false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cambiarContrasena(userId: Long, contrasenaActual: String, nuevaContrasena: String): Boolean {
        return try {
            readableDatabase.rawQuery(
                "SELECT $US_ID FROM $TABLA_USUARIOS WHERE $US_ID = ? AND $US_CONTRASENA = ?",
                arrayOf(userId.toString(), contrasenaActual)
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    val cv = ContentValues().apply { put(US_CONTRASENA, nuevaContrasena) }
                    writableDatabase.update(TABLA_USUARIOS, cv, "$US_ID = ?", arrayOf(userId.toString())) > 0
                } else false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizarPerfil(usuario: Usuario): Boolean {
        return try {
            val cv = ContentValues().apply {
                put(US_NOMBRE, usuario.nombre.trim())
                if (usuario.fotoBytes != null) put(US_FOTO, usuario.fotoBytes)
                put(US_SEC_CERT, usuario.secCertificacion)
                put(US_ESPECIALIDAD, usuario.especialidadTecnica)
                put(US_RUT_COLEGIATURA, usuario.rutColegiatura)
                put(US_REG_PROFESIONAL, usuario.registroProfesional)
                put(US_DEPTO, usuario.departamentoSucursal)
                put(US_COD_AUT, usuario.codigoAutorizacion)
                put(US_EMP_NOMBRE, usuario.empresaNombre)
                put(US_EMP_RUT, usuario.rutEmpresa)
                put(US_EMP_DIR, usuario.direccionEmpresa)
                put(US_EMP_TEL, usuario.telefonoContacto)
            }
            writableDatabase.update(TABLA_USUARIOS, cv, "$US_ID = ?", arrayOf(usuario.id.toString())) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun leerUsuario(cursor: Cursor): Usuario {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(US_ID))
        val user = cursor.getString(cursor.getColumnIndexOrThrow(US_USERNAME))
        val nom = cursor.getString(cursor.getColumnIndexOrThrow(US_NOMBRE))
        val rolStr = cursor.getString(cursor.getColumnIndexOrThrow(US_ROL))
        val fotoIndex = cursor.getColumnIndex(US_FOTO)
        val fotoBytes = if (fotoIndex != -1 && !cursor.isNull(fotoIndex)) cursor.getBlob(fotoIndex) else null

        val pregIndex = cursor.getColumnIndex(US_PREGUNTA_SEG)
        val pregSeg = if (pregIndex != -1 && !cursor.isNull(pregIndex)) cursor.getString(pregIndex) else null
        val respIndex = cursor.getColumnIndex(US_RESPUESTA_SEG)
        val respSeg = if (respIndex != -1 && !cursor.isNull(respIndex)) cursor.getString(respIndex) else null

        val getStringOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getString(idx) else null
        }

        val rol = try { Rol.valueOf(rolStr) } catch (e: Exception) { Rol.TECNICO }

        return Usuario(
            id = id,
            username = user,
            nombre = nom,
            rol = rol,
            fotoBytes = fotoBytes,
            preguntaSeguridad = pregSeg,
            respuestaSeguridad = respSeg,
            secCertificacion = getStringOrNull(US_SEC_CERT),
            especialidadTecnica = getStringOrNull(US_ESPECIALIDAD),
            rutColegiatura = getStringOrNull(US_RUT_COLEGIATURA),
            registroProfesional = getStringOrNull(US_REG_PROFESIONAL),
            departamentoSucursal = getStringOrNull(US_DEPTO),
            codigoAutorizacion = getStringOrNull(US_COD_AUT),
            empresaNombre = getStringOrNull(US_EMP_NOMBRE),
            rutEmpresa = getStringOrNull(US_EMP_RUT),
            direccionEmpresa = getStringOrNull(US_EMP_DIR),
            telefonoContacto = getStringOrNull(US_EMP_TEL)
        )
    }

    fun insertarEquipo(equipo: Equipo): Long {
        return try {
            val valores = ContentValues().apply {
                put(EQ_CODIGO, equipo.codigo)
                put(EQ_TIPO, equipo.tipo.name)
                put(EQ_INSTALACION, equipo.instalacionCliente)
                put(EQ_MARCA, equipo.marca)
                put(EQ_MODELO, equipo.modelo)
                put(EQ_SERIE, equipo.numeroSerie)
                if (equipo.potenciaKVA != null) put(EQ_POTENCIA, equipo.potenciaKVA)
                if (equipo.tensionNominalKV != null) put(EQ_TENSION, equipo.tensionNominalKV)
                if (equipo.clienteId != null) put(EQ_CLIENTE_ID, equipo.clienteId)
                if (equipo.clienteNombre != null) put(EQ_CLIENTE_NOMBRE, equipo.clienteNombre)
                if (equipo.ubicacionGPS != null) put(EQ_GPS, equipo.ubicacionGPS)
            }
            writableDatabase.insert(TABLA_EQUIPOS, null, valores)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun listarEquipos(): List<Equipo> {
        val lista = mutableListOf<Equipo>()
        try {
            readableDatabase.rawQuery("SELECT * FROM $TABLA_EQUIPOS ORDER BY $EQ_CODIGO ASC", null).use { cursor ->
                while (cursor.moveToNext()) {
                    lista.add(leerEquipo(cursor))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    fun buscarEquipo(codigo: String, instalacion: String): Equipo? {
        try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_EQUIPOS WHERE $EQ_CODIGO = ? AND $EQ_INSTALACION = ? LIMIT 1",
                arrayOf(codigo, instalacion)
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    return leerEquipo(cursor)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun obtenerEquipoPorId(id: Long): Equipo? {
        try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_EQUIPOS WHERE $EQ_ID = ?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    return leerEquipo(cursor)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun leerEquipo(cursor: Cursor): Equipo {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(EQ_ID))
        val codigo = cursor.getString(cursor.getColumnIndexOrThrow(EQ_CODIGO))
        val tipoStr = cursor.getString(cursor.getColumnIndexOrThrow(EQ_TIPO))
        val instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQ_INSTALACION))

        val getStringOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getString(idx) else null
        }
        val getDoubleOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getDouble(idx) else null
        }
        val getLongOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        }

        val tipo = try {
            TipoEquipo.valueOf(tipoStr)
        } catch (e: Exception) {
            TipoEquipo.TRANSFORMADOR
        }

        return Equipo(
            id = id,
            codigo = codigo,
            tipo = tipo,
            instalacionCliente = instalacion,
            marca = getStringOrNull(EQ_MARCA) ?: "Genérica",
            modelo = getStringOrNull(EQ_MODELO) ?: "Estándar",
            numeroSerie = getStringOrNull(EQ_SERIE) ?: "N/A",
            potenciaKVA = getDoubleOrNull(EQ_POTENCIA),
            tensionNominalKV = getDoubleOrNull(EQ_TENSION),
            clienteId = getLongOrNull(EQ_CLIENTE_ID),
            clienteNombre = getStringOrNull(EQ_CLIENTE_NOMBRE),
            ubicacionGPS = getStringOrNull(EQ_GPS)
        )
    }

    fun insertarProtocolo(protocolo: Protocolo): Long {
        return try {
            val valores = ContentValues().apply {
                put(PR_EQUIPO_ID, protocolo.equipoId)
                put(PR_FECHA_HORA, protocolo.fechaHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                put(PR_MEDICIONES_JSON, medicionesAJson(protocolo.mediciones))
                put(PR_ESTADO, protocolo.estado.name)
                put(PR_OBSERVACIONES, protocolo.observaciones)
                if (protocolo.fotoEvidenciaBytes != null) put(PR_FOTO_EVIDENCIA, protocolo.fotoEvidenciaBytes)
                put(PR_TECNICO_NOM, protocolo.tecnicoNombre)
                if (protocolo.ingenieroNombre != null) put(PR_INGENIERO_NOM, protocolo.ingenieroNombre)
                if (protocolo.temperaturaAmbiente != null) put(PR_TEMP_AMB, protocolo.temperaturaAmbiente)
                if (protocolo.humedadRelativa != null) put(PR_HUMEDAD, protocolo.humedadRelativa)
                if (protocolo.instrumentosUsados != null) put(PR_INSTRUMENTOS, protocolo.instrumentosUsados)
                put(PR_RESULTADO_GEN, protocolo.resultadoGeneral)
                if (protocolo.ubicacionGPS != null) put(PR_GPS, protocolo.ubicacionGPS)
            }
            writableDatabase.insert(TABLA_PROTOCOLOS, null, valores)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun actualizarProtocolo(protocolo: Protocolo): Int {
        return try {
            val valores = ContentValues().apply {
                put(PR_ESTADO, protocolo.estado.name)
                put(PR_OBSERVACIONES, protocolo.observaciones)
                if (protocolo.fotoEvidenciaBytes != null) put(PR_FOTO_EVIDENCIA, protocolo.fotoEvidenciaBytes)
                if (protocolo.ingenieroNombre != null) put(PR_INGENIERO_NOM, protocolo.ingenieroNombre)
                put(PR_RESULTADO_GEN, protocolo.resultadoGeneral)
            }
            writableDatabase.update(
                TABLA_PROTOCOLOS,
                valores,
                "$PR_ID = ?",
                arrayOf(protocolo.id.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun listarProtocolosPorEquipo(equipoId: Long): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_PROTOCOLOS WHERE $PR_EQUIPO_ID = ? ORDER BY $PR_FECHA_HORA DESC",
                arrayOf(equipoId.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    lista.add(leerProtocolo(cursor))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    fun listarTodosLosProtocolos(): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        try {
            readableDatabase.rawQuery(
                "SELECT * FROM $TABLA_PROTOCOLOS ORDER BY $PR_FECHA_HORA DESC", null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    lista.add(leerProtocolo(cursor))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    private fun leerProtocolo(cursor: Cursor): Protocolo {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(PR_ID))
        val equipoId = cursor.getLong(cursor.getColumnIndexOrThrow(PR_EQUIPO_ID))
        val fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(PR_FECHA_HORA))
        val jsonStr = cursor.getString(cursor.getColumnIndexOrThrow(PR_MEDICIONES_JSON))
        val estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(PR_ESTADO))
        val observaciones = cursor.getString(cursor.getColumnIndexOrThrow(PR_OBSERVACIONES)) ?: ""
        val fotoIndex = cursor.getColumnIndex(PR_FOTO_EVIDENCIA)
        val fotoEvidenciaBytes = if (fotoIndex != -1 && !cursor.isNull(fotoIndex)) cursor.getBlob(fotoIndex) else null

        val getStringOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getString(idx) else null
        }
        val getDoubleOrNull = { colName: String ->
            val idx = cursor.getColumnIndex(colName)
            if (idx != -1 && !cursor.isNull(idx)) cursor.getDouble(idx) else null
        }

        val fechaHora = try {
            LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }

        val estado = try {
            EstadoProtocolo.valueOf(estadoStr)
        } catch (e: Exception) {
            EstadoProtocolo.REGISTRADO_EN_TERRENO
        }

        val mediciones = jsonAMediciones(jsonStr)

        return Protocolo(
            id = id,
            equipoId = equipoId,
            fechaHora = fechaHora,
            mediciones = mediciones,
            estado = estado,
            observaciones = observaciones,
            fotoEvidenciaBytes = fotoEvidenciaBytes,
            tecnicoNombre = getStringOrNull(PR_TECNICO_NOM) ?: "",
            ingenieroNombre = getStringOrNull(PR_INGENIERO_NOM),
            temperaturaAmbiente = getDoubleOrNull(PR_TEMP_AMB),
            humedadRelativa = getDoubleOrNull(PR_HUMEDAD),
            instrumentosUsados = getStringOrNull(PR_INSTRUMENTOS),
            resultadoGeneral = getStringOrNull(PR_RESULTADO_GEN) ?: "CONFORME",
            ubicacionGPS = getStringOrNull(PR_GPS)
        )
    }

    private fun medicionesAJson(mediciones: List<Medicion>): String {
        val array = JSONArray()
        for (m in mediciones) {
            val obj = JSONObject().apply {
                put("nombre", m.nombre)
                put("valor", m.valor)
                put("unidad", m.unidad)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun jsonAMediciones(jsonStr: String): List<Medicion> {
        val lista = mutableListOf<Medicion>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                lista.add(
                    Medicion(
                        nombre = obj.getString("nombre"),
                        valor = obj.getString("valor"),
                        unidad = obj.getString("unidad")
                    )
                )
            }
        } catch (e: Exception) {

        }
        return lista
    }
}
