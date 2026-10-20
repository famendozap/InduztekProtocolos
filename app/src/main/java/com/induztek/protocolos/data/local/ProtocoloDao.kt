package com.induztek.protocolos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(protocolo: ProtocoloEntity): Long

    @Update
    suspend fun actualizar(protocolo: ProtocoloEntity)

    @Query("SELECT * FROM protocolos WHERE equipoId = :equipoId ORDER BY fechaHora DESC")
    fun observarHistorialPorEquipo(equipoId: Long): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos WHERE id = :protocoloId")
    suspend fun obtenerPorId(protocoloId: Long): ProtocoloEntity?
}
