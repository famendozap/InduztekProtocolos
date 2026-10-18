# Induztek Protocolos (MVP)

App Android (Kotlin, Jetpack Compose, Material 3, MVVM, Room) para registrar pruebas eléctricas
en terreno y consultar el historial de cada equipo. Caso DSY1105.

## Cómo abrirlo
1. Descomprime el zip. Abre en Android Studio **esta carpeta** (la que contiene `settings.gradle.kts`, `gradlew` y `app/`).
2. Espera el *Gradle Sync* (la primera vez descarga dependencias; requiere internet). Gradle JDK: 17.
3. Ejecuta el módulo `app` en un emulador o dispositivo (API 26+).

## Qué incluye hoy
- Lista de equipos, alta de equipo (TAG único por instalación), historial por equipo y registro de pruebas.
- Mediciones con valor numérico validado (coma o punto decimal).
- Los protocolos quedan siempre en "Registrado en terreno" (formalizar requiere roles: pendiente).

## Pendiente respecto del caso
Roles y login, visitas/OT y acta, catálogo de pruebas por tipo de equipo, cálculo automático de resultados,
instrumentos y calibración, historial de estados, backend Spring Boot + Retrofit y sincronización.
