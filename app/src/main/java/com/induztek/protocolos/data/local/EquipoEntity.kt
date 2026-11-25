package com.induztek.protocolos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.TipoEquipo

@Entity(
    tableName = "equipos",
    indices = [Index(value = ["instalacionCliente", "codigo"], unique = true)]
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacionCliente: String
)

fun EquipoEntity.toDomain() = Equipo(
    id = id,
    codigo = codigo,
    tipo = tipo,
    instalacionCliente = instalacionCliente
)

fun Equipo.toEntity() = EquipoEntity(
    id = id,
    codigo = codigo,
    tipo = tipo,
    instalacionCliente = instalacionCliente
)
