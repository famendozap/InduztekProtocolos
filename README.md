# Induztek Protocolos

Aplicación Móvil para la Formalización Inmediata de Protocolos de Prueba Eléctrica en Terreno.
Desarrollada para Induztek Ingeniería SpA (Caso DSY1105 - Duoc UC).

## 🚀 Características Implementadas

- **Base de Datos SQLite Nativa:** Administrada mediante `InduztekDbHelper` (`SQLiteOpenHelper`) con tablas de usuarios, equipos y protocolos.
- **Autenticación y Registro:** Inicio de sesión estricto desde SQLite y pantalla de registro de nuevas cuentas.
- **Gestión de Roles (P-01 al P-04):**
  - **Técnico (`tecnico` / `1234`):** Registro de pruebas en terreno y equipos.
  - **Ingeniero (`ingeniero` / `1234`):** Creación y formalización oficial de protocolos.
  - **Jefe Técnico (`jefe` / `1234`):** Gestión completa de la operación.
  - **Cliente (`cliente` / `1234`):** Vista en modo lectura para consultar instalaciones y estado de protocolos.
- **Cálculos Automáticos y Pruebas:**
  - **Índice de Polarización (IP):** Cálculo automático con diagnóstico de estado.
  - **Relación de Transformación (TTR):** Variación de voltaje con alerta automática cuando supera el 25%.
- **Paneles de Estadísticas en Tiempo Real:** Gráficos de barras y gráficos circulares creados dinámicamente con `Canvas` en Jetpack Compose.
- **Respaldo Inmediato y Visitas:** Pantalla de Acta de Recepción Técnica.
- **Estructura Backend Retrofit:** Cliente `RetrofitClient` e `InduztekApiService` configurados con DTOs para comunicación REST.
- **Pruebas Unitarias:** 11 unit tests aprobados con JUnit en `CalculosPruebasTest`, `InstrumentoTest` y `MedicionTest`.

## 🛠️ Tecnologías Utilizadas
- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material Design 3
- **Arquitectura:** MVVM (Model-View-ViewModel) + Flow / StateFlow
- **Persistencia:** SQLite (`SQLiteOpenHelper`)
- **Red:** Retrofit 2 + Gson + OkHttp Interceptor
- **Testing:** JUnit 4
