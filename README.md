# RememberMeat

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Datos-Room-3DDC84)
![minSdk](https://img.shields.io/badge/minSdk-26-informational)

Aplicación Android para apuntar los productos que las tiendas rebajan cuando se acerca su fecha de caducidad y llevar la cuenta atrás de cada uno.

Muchos supermercados bajan el precio de lo que no se vende cuando le queda poco plazo. La idea es sencilla: vas a la tienda, haces una foto al producto (si quieres), anotas su precio y su fecha de caducidad, y la app se encarga de recordártelo.

Es un proyecto personal y funciona **sin cuenta, sin servidor y sin conexión**: todo se guarda en el propio móvil.



## Funciones

- **Apuntar productos** con nombre, tienda, precio, fecha de caducidad (con calendario) y foto opcional.
- **Cuenta atrás visual:** cada producto muestra los días que le quedan, con un color según la urgencia y una barra que se llena a medida que se acerca la fecha.
- **Lista agrupada por urgencia:** caducados, hoy y mañana, esta semana y más adelante, con una tarjeta destacada para lo próximo en caducar.
- **Avisos diarios** a las 9:00 de lo que caduca hoy o mañana, en una sola notificación.
- **Editar y borrar:** se edita pulsando la tarjeta y se borra deslizándola (con confirmación) o desde el formulario de edición.
- **Tema claro y oscuro**, con paleta propia en lugar del color dinámico del sistema.

## Tecnologías

| Área | Elección |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material 3 |
| Base de datos | Room (con KSP) |
| Tareas en segundo plano | WorkManager |
| Imágenes | Coil 3 |
| Cámara | App de cámara del sistema (`ActivityResultContracts.TakePicture`) + `FileProvider` |
| Arquitectura | UI en Compose → `ViewModel` → DAO de Room |

## Decisiones de diseño

- **Precios en céntimos (`Int`)**, no en `Double`, para evitar errores de redondeo. El texto que escribe el usuario se convierte con `BigDecimal`.
- **Fechas como `LocalDate`**, guardadas en Room como día de época (`Long`) mediante un `TypeConverter`. Esto permite ordenar y filtrar por caducidad directamente en SQL.
- **Fotos como ficheros, no como BLOB:** se guardan en el almacenamiento privado de la app y en la base de datos solo se guarda la ruta. Mantiene la base de datos pequeña y las consultas rápidas.
- **Sin permiso de cámara:** se delega en la app de cámara del sistema, así que no hace falta pedir `CAMERA`.
- **Sin capa de repositorio:** para una app local de una sola entidad, el `ViewModel` habla directamente con el DAO.
- **Avisos con WorkManager** en lugar de alarmas exactas: no exige permisos especiales, a cambio de que la hora del aviso pueda retrasarse algo con el ahorro de batería.

## Estructura del proyecto

```
app/src/main/java/com/example/remembermeat/
├── dao/            # ProductDao (consultas de Room)
├── database/       # AppDatabase
├── entity/         # Product y Converters
├── view/           # Pantallas Compose: lista, tarjetas y formulario
├── viewmodel/      # ProductViewModel
├── ui/theme/       # Colores, tipografía y tema
├── ExpiryUtils.kt  # Días restantes, textos y conversión de precios
├── Worker.kt       # ExpiryWorker: notificación diaria
├── RememberMeatApp.kt  # Canal de notificaciones y programación del aviso
└── MainActivity.kt
```

## Requisitos

- Android Studio reciente (el proyecto usa Android Gradle Plugin 9).
- JDK 17 o superior (el que incluye Android Studio sirve).
- Dispositivo o emulador con **Android 8.0 (API 26)** o superior.

## Cómo ejecutarlo

```bash
git clone https://github.com/Driusito/RememberMeat.git
cd RememberMeat
```

1. Abre la carpeta en Android Studio y espera a que termine la sincronización de Gradle.
2. Elige un dispositivo o emulador y pulsa **Run**.

También se puede compilar desde la terminal:

```bash
./gradlew assembleDebug
```

En Android 13 o superior, la app pide permiso para mostrar notificaciones al abrirse por primera vez.

## Cómo funcionan los avisos

Al arrancar, la app programa una tarea periódica con WorkManager que se ejecuta cada día a las 9:00. La tarea busca los productos que caducan **hoy o mañana** y muestra una sola notificación con la lista.

Un producto que caduca mañana avisa dos veces: el día antes (como "mañana") y el propio día (como "hoy"). Así, si apuntas algo por la tarde que caduca al día siguiente, también recibes el recordatorio.

## Ideas pendientes

- Pantalla de detalle con la foto en grande.
- Historial de productos comprados o caducados.
- Ajustes del aviso: hora y días de antelación.
- Icono propio para la notificación.

## Autor

Andrés Jesús Jurado Suárez · [GitHub](https://github.com/Driusito) · [Portfolio](https://driusito.github.io/portfolio)

