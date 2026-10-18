package com.induztek.protocolos.model

import java.time.LocalDateTime

data class Visita(
    val id: Long = 0,
    val cliente: String,
    val instalacion: String,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    val tecnicoAsignado: String,
    val estado: String = "EN_PROCESO" // EN_PROCESO, FINALIZADA, CANCELADA
)
