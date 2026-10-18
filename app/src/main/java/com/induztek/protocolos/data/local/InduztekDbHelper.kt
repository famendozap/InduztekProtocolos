package com.induztek.protocolos.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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

/**
 * Helper de SQLite nativo (SQLiteOpenHelper) para la persisencia local.
 * Gestiona la base de datos "induztek_sqlite.db" con tablas "usuarios", "equipos" y "protocolos".
 * Incluye precarga de usuarios semilla (tecnico, ingeniero, jefe, cliente) y claves foraneas activas.
 */
class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek_sqlite.db"
        private const val VERSION_BD = 2

        // Tabla Usuarios
        private const val TABLA_USUARIOS = "usuarios"
        private const val US_ID = "id"
        private const val US_USERNAME = "username"
        private const val US_NOMBRE = "nombre"
        private const val US_CONTRASENA = "contrasena"
        private const val US_ROL = "rol"

        // Tabla Equipos
        private const val TABLA_EQUIPOS = "equipos"
        private const val EQ_ID = "id"
        private const val EQ_CODIGO = "codigo"
        private const val EQ_TIPO = "tipo"
        private const val EQ_INSTALACION = "instalacionCliente"

        // Tabla Protocolos
        private const val TABLA_PROTOCOLOS = "protocolos"
        private const val PR_ID = "id"
        private const val PR_EQUIPO_ID = "equipoId"
        private const val PR_FECHA_HORA = "fechaHora"
        private const val PR_MEDICIONES_JSON = "medicionesJson"
        private const val PR_ESTADO = "estado"
        private const val PR_OBSERVACIONES = "observaciones"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_USUARIOS (" +
                "$US_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$US_USERNAME TEXT NOT NULL UNIQUE, " +
                "$US_NOMBRE TEXT NOT NULL, " +
                "$US_CONTRASENA TEXT NOT NULL, " +
                "$US_ROL TEXT NOT NULL)"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT OR IGNORE INTO $TABLA_USUARIOS ($US_USERNAME, $US_NOMBRE, $US_CONTRASENA, $US_ROL) VALUES " +
                "('tecnico', 'Juan Técnico', '1234', 'TECNICO'), " +
                "('ingeniero', 'Ana Ingeniera', '1234', 'INGENIERO'), " +
                "('jefe', 'Carlos Jefe', '1234', 'JEFE'), " +
                "('cliente', 'Empresa Cliente', '1234', 'CLIENTE')"

        private const val SQL_CREAR_EQUIPOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_EQUIPOS (" +
                "$EQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$EQ_CODIGO TEXT NOT NULL, " +
                "$EQ_TIPO TEXT NOT NULL, " +
                "$EQ_INSTALACION TEXT NOT NULL)"

        private const val SQL_CREAR_PROTOCOLOS =
            "CREATE TABLE IF NOT EXISTS $TABLA_PROTOCOLOS (" +
                "$PR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PR_EQUIPO_ID INTEGER NOT NULL, " +
                "$PR_FECHA_HORA TEXT NOT NULL, " +
                "$PR_MEDICIONES_JSON TEXT NOT NULL, " +
                "$PR_ESTADO TEXT NOT NULL, " +
                "$PR_OBSERVACIONES TEXT, " +
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
            db.execSQL(SQL_CREAR_USUARIOS)
            db.execSQL(SQL_PRECARGAR_USUARIOS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- CRUD USUARIOS ---

    fun registrarUsuario(username: String, nombre: String, contrasena: String, rol: String): Long {
        return try {
            val valores = ContentValues().apply {
                put(US_USERNAME, username.trim().lowercase())
                put(US_NOMBRE, nombre.trim())
                put(US_CONTRASENA, contrasena)
                put(US_ROL, rol)
            }
            writableDatabase.insert(TABLA_USUARIOS, null, valores)
        } catch (e: Exception) {
            e.printStackTrace()
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
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(US_ID))
                    val user = cursor.getString(cursor.getColumnIndexOrThrow(US_USERNAME))
                    val nom = cursor.getString(cursor.getColumnIndexOrThrow(US_NOMBRE))
                    val rolStr = cursor.getString(cursor.getColumnIndexOrThrow(US_ROL))
                    val rol = try { Rol.valueOf(rolStr) } catch (e: Exception) { Rol.TECNICO }
                    Usuario(id = id, username = user, nombre = nom, rol = rol)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- CRUD EQUIPOS ---

    fun insertarEquipo(equipo: Equipo): Long {
        return try {
            val valores = ContentValues().apply {
                put(EQ_CODIGO, equipo.codigo)
                put(EQ_TIPO, equipo.tipo.name)
                put(EQ_INSTALACION, equipo.instalacionCliente)
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

        val tipo = try {
            TipoEquipo.valueOf(tipoStr)
        } catch (e: Exception) {
            TipoEquipo.TRANSFORMADOR
        }

        return Equipo(id = id, codigo = codigo, tipo = tipo, instalacionCliente = instalacion)
    }

    // --- CRUD PROTOCOLOS ---

    fun insertarProtocolo(protocolo: Protocolo): Long {
        return try {
            val valores = ContentValues().apply {
                put(PR_EQUIPO_ID, protocolo.equipoId)
                put(PR_FECHA_HORA, protocolo.fechaHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                put(PR_MEDICIONES_JSON, medicionesAJson(protocolo.mediciones))
                put(PR_ESTADO, protocolo.estado.name)
                put(PR_OBSERVACIONES, protocolo.observaciones)
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
            observaciones = observaciones
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
            // Ignorar error si el json es nulo o inválido
        }
        return lista
    }
}
