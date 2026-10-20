package com.induztek.protocolos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(equipo: EquipoEntity): Long

    @Query("SELECT * FROM equipos ORDER BY codigo ASC")
    fun observarTodos(): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id = :equipoId")
    suspend fun obtenerPorId(equipoId: Long): EquipoEntity?

    @Query("SELECT * FROM equipos WHERE codigo = :codigo AND instalacionCliente = :instalacion LIMIT 1")
    suspend fun buscarPorCodigoEnInstalacion(codigo: String, instalacion: String): EquipoEntity?
}
