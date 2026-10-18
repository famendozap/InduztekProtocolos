package com.induztek.protocolos.data.local

import androidx.room.TypeConverter
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.TipoEquipo
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Conversores para que Room pueda guardar tipos que no son primitivos:
 * fecha/hora, enums y la lista de mediciones de un protocolo.
 */
class Converters {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.format(dateFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateFormatter) }

    @TypeConverter
    fun fromTipoEquipo(value: TipoEquipo): String = value.name

    @TypeConverter
    fun toTipoEquipo(value: String): TipoEquipo = TipoEquipo.valueOf(value)

    @TypeConverter
    fun fromEstadoProtocolo(value: EstadoProtocolo): String = value.name

    @TypeConverter
    fun toEstadoProtocolo(value: String): EstadoProtocolo = EstadoProtocolo.valueOf(value)

    @TypeConverter
    fun fromMediciones(mediciones: List<Medicion>): String {
        val array = JSONArray()
        mediciones.forEach { medicion ->
            val obj = JSONObject()
            obj.put("nombre", medicion.nombre)
            obj.put("valor", medicion.valor)
            obj.put("unidad", medicion.unidad)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toMediciones(value: String): List<Medicion> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            Medicion(
                nombre = obj.getString("nombre"),
                valor = obj.getString("valor"),
                unidad = obj.optString("unidad", "")
            )
        }
    }
}
