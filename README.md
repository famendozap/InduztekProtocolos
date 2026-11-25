# Induztek Protocolos

Aplicación Móvil para la Gestión y Formalización Inmediata de Protocolos de Prueba Eléctrica en Terreno.
Desarrollada para Induztek Ingeniería SpA (Proyecto Sumativa 3 - Duoc UC).

---

## 🚀 Funcionalidades Implementadas (Sumativa 3)

### 🔑 1. Recuperación de Clave, Modificación de Perfil y Cambio de Contraseña
- **Recuperar Clave:** Flujo con pregunta de seguridad y validación de nueva clave.
- **Modificar Perfil:** Actualización de nombre completo, foto de usuario (cámara o galería) y atributos específicos del rol.
- **Cambiar Contraseña:** Pantalla dedicada que solicita la contraseña actual, valida la nueva clave con las 6 reglas de seguridad y actualiza las credenciales en la base de datos.

### 🛡️ 2. Seguridad de Contraseñas (Más de 5 Reglas con Checklist Visual)
- **Confirmación de Clave:** Campo obligatorio de repetición en registro, recuperación y cambio de clave.
- **Visualizador Dinámico de Reglas (`ReglasContrasenaCard`):** Indicadores visuales en tiempo real con íconos animados (verde/rojo) que verifican:
  1. Longitud mínima de al menos 8 caracteres.
  2. Al menos una letra mayúscula (`A-Z`).
  3. Al menos una letra minúscula (`a-z`).
  4. Al menos un número (`0-9`).
  5. Al menos un carácter especial/símbolo (`@#$%^&*()_+-=[]{}|;:'",.<>?/`).
  6. Sin espacios en blanco y coincidencia estricta entre clave y repetición.

### 👥 3. Registro Adaptado por Rol
El formulario de registro ajusta sus campos según el rol seleccionado:
- **TÉCNICO:** Certificación SEC (ej. *SEC-Clase-A-1234*) y Especialidad Técnica (*Alta Tensión / Transformadores*).
- **INGENIERO:** RUT / N° Colegiatura CIP (*CIP-992314*) y Registro Profesional / Firma (*Reg. Sup. N° 45*).
- **JEFE TÉCNICO:** Departamento / Sucursal (*Supervisión Central Santiago*) y Código de Autorización (*AUT-JEFE-2025*).
- **CLIENTE:** Nombre de Empresa (*Minera & Ind. Lampa S.A.*), RUT Empresa (*76.543.210-9*), Dirección y Teléfono de contacto.

### 🏢 4. Autocompletado de Cliente y Dirección al Crear Equipo
- Al agregar un nuevo equipo, se despliega una lista de **Clientes registrados**.
- Al seleccionar un cliente, la **dirección / instalación se autocompleta automáticamente** con la ubicación registrada de la empresa.

### 🔐 5. Control Estricto de Permisos por Rol
- **Solo el Ingeniero formaliza:** Únicamente el usuario con rol `INGENIERO` visualiza y puede accionar el botón *"Formalizar Protocolo"*.
- **El que formaliza no crea protocolos:** El rol `INGENIERO` no crea ni registra pruebas en terreno. La creación de nuevos protocolos está reservada exclusivamente para el `TÉCNICO`.
- **Cliente solo consulta:** El rol `CLIENTE` cuenta con una vista de lectura exclusiva para revisar equipos y protocolos de sus instalaciones.

### 📋 6. Información Ampliada en Equipos y Protocolos
- **Equipos:** Marca, modelo, N° de serie, potencia (kVA), tensión nominal (kV), cliente asociado y coordenadas GPS.
- **Protocolos:** Técnico de terreno responsable, ingeniero formalizador, condiciones ambientales (temperatura °C, humedad % HR), instrumentos utilizados (ej. *Megger MIT515*, *Omicron TTR*), resultado general (*CONFORME*, *CON OBSERVACIONES*, *NO CONFORME*) y coordenadas GPS.

---

## 📱 Recursos Nativos e Integración de Permisos

1. **Cámara (`android.permission.CAMERA`):** Captura de foto para credencial de usuario y evidencia fotográfica en terreno del protocolo.
2. **Galería (`ActivityResultContracts.GetContent()`):** Selección de fotos de perfil y evidencias desde el almacenamiento de imágenes del dispositivo.
3. **Ubicación GPS (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`):** Captura de coordenadas GPS en tiempo real para geolocalizar la planta o equipo eléctrico.
4. **Exportación y Compartición de Acta en PDF (`android.graphics.pdf.PdfDocument` + `FileProvider`):** Generación oficial del documento PDF del protocolo con opción de compartir mediante `Intent.ACTION_SEND`.

---

## 🎨 Calidad Técnica y UX

- **Validación por Campo:** Todos los formularios integran `isError` y `supportingText` conducidos por `StateFlow` desde los ViewModels (`AuthViewModel` e `InduztekViewModel`).
- **Diseño M3 y Centrado:** Interfaz pulida basada en tarjetas Material Design 3, espaciado uniforme y alineación adaptable.
- **Animaciones en Compose:** Animaciones de transición en `NavHost`, `AnimatedVisibility` para avisos/errores y `animateColorAsState` para estados de reglas y badges.

---

## Pruebas unitarias

El proyecto usa **JUnit 4**. Las pruebas cubren cálculos eléctricos, validación de contraseñas y validaciones de los ViewModels.

```bash
./gradlew :app:testDebugUnitTest
```

---

## Conexión con el backend

El inicio de sesión, registro, recuperación/cambio de contraseña, edición de perfil, equipos y protocolos usan la API REST. Registra las cuentas desde la app después de iniciar el backend; no hay cuentas remotas precargadas.

Inicia primero los microservicios siguiendo el `README.md` del backend.

- **Emulador de Android Studio:** usa por defecto `http://10.0.2.2:8080/`.
- **Teléfono físico:** agrega `API_BASE_URL=http://<IP-LAN-DE-TU-PC>:8080/` a `gradle.properties` en la raíz del proyecto. El teléfono y el PC deben estar en la misma red y el firewall de Windows debe permitir conexiones entrantes al puerto `8080`.

### 🖥️ Presentación en otro computador (Configuración de Entorno y Túnel ADB)

Para presentar o ejecutar la aplicación en otro computador, necesitarás configurar su entorno:

- `API_BASE_URL=http://127.0.0.1:8080/` puede ir incluido en el proyecto (en `gradle.properties`).
- El túnel ADB es local a cada computador y debes crearlo allí también ejecutando los siguientes comandos en PowerShell:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb reverse tcp:8080 tcp:8080
& $adb reverse --list
```

La app requiere permiso de Internet y permite HTTP sin TLS para pruebas locales. No uses HTTP para una publicación en Internet.

Para compilar y ejecutar las pruebas en Windows:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Stack Android: Android Gradle Plugin 9.4.1, Gradle 9.6.0, Kotlin 2.2.10, KSP 2.2.10-2.0.2 y SDK 37.

---

## 🛠️ Tecnologías Utilizadas
- **Lenguaje:** Kotlin 2.2.10
- **UI:** Jetpack Compose + Material Design 3
- **Arquitectura:** MVVM (Model-View-ViewModel) + Flow / StateFlow
- **Persistencia local:** SQLite nativo (`SQLiteOpenHelper`)
- **Nativo Android:** `PdfDocument`, `FileProvider`, `LocationManager`, `CameraX` / `TakePicturePreview`, Photo Picker
- **Red:** Retrofit 2.11 + Gson + OkHttp 4.12
- **Testing:** JUnit 4
