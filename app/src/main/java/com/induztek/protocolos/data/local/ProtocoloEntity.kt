package com.induztek.protocolos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import java.time.LocalDateTime

@Entity(
    tableName = "protocolos",
    foreignKeys = [
        ForeignKey(
            entity = EquipoEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("equipoId")]
)
data class ProtocoloEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val equipoId: Long,
    val fechaHora: LocalDateTime,
    val mediciones: List<Medicion>,
    val estado: EstadoProtocolo,
    val observaciones: String = ""
)

fun ProtocoloEntity.toDomain() = Protocolo(
    id = id,
    equipoId = equipoId,
    fechaHora = fechaHora,
    mediciones = mediciones,
    estado = estado,
    observaciones = observaciones
)

fun Protocolo.toEntity() = ProtocoloEntity(
    id = id,
    equipoId = equipoId,
    fechaHora = fechaHora,
    mediciones = mediciones,
    estado = estado,
    observaciones = observaciones
)
