# SCOUTEO — Documentación Técnica del Proyecto

**Autor:** Alberto Pérez Oncina  
**Ciclo:** Desarrollo de Aplicaciones Multiplataforma (DAM) — 2.º Curso  
**Curso académico:** 2024–2025  
**Tecnología principal:** JavaFX + API REST (Google Cloud Run)

---

## Índice

1. [Introducción y motivación](#1-introducción-y-motivación)
2. [Descripción general de la aplicación](#2-descripción-general-de-la-aplicación)
3. [Stack tecnológico](#3-stack-tecnológico)
4. [Arquitectura del sistema](#4-arquitectura-del-sistema)
5. [Estructura del proyecto](#5-estructura-del-proyecto)
6. [Modelo de datos](#6-modelo-de-datos)
   - [6.1 Usuario](#61-usuario)
   - [6.2 Jugador](#62-jugador)
   - [6.3 Equipo](#63-equipo)
   - [6.4 Partido](#64-partido)
   - [6.5 Alineación](#65-alineación)
   - [6.6 Entrenamiento y Asistencia](#66-entrenamiento-y-asistencia)
   - [6.7 Objetivo](#67-objetivo)
   - [6.8 Historial Médico](#68-historial-médico)
   - [6.9 Evento de Partido](#69-evento-de-partido)
   - [6.10 Estadística de Partido y JugadorPartido](#610-estadística-de-partido-y-jugadorpartido)
   - [6.11 Configuración](#611-configuración)
7. [Capa de acceso a datos — DAOs](#7-capa-de-acceso-a-datos--daos)
   - [7.1 Patrón DAO y comunicación con la API](#71-patrón-dao-y-comunicación-con-la-api)
   - [7.2 UsuarioDAO](#72-usuariodao)
   - [7.3 JugadorDAO](#73-jugadordao)
   - [7.4 EquipoDAO](#74-equipodao)
   - [7.5 PartidoDAO](#75-partidodao)
   - [7.6 AlineacionDAO y JugadorPartidoDAO](#76-alineaciondao-y-jugadorpartidodao)
   - [7.7 EntrenamientoDAO](#77-entrenamientodao)
   - [7.8 HistorialMedicoDAO](#78-historialmedicodao)
   - [7.9 ObjetivoDAO](#79-objetivodao)
   - [7.10 EventoPartidoDAO](#710-eventopartidodao)
   - [7.11 RankingDAO](#711-rankingdao)
   - [7.12 EstadisticaPartidoDAO](#712-estadisticapartidodao)
   - [7.13 ConfiguracionDAO y ClubesDAO](#713-configuraciondao-y-clubesdao)
8. [Utilidades del sistema](#8-utilidades-del-sistema)
   - [8.1 ApiClient](#81-apiclient)
   - [8.2 SesionUsuario](#82-sesiónusuario)
   - [8.3 ConexionBD](#83-conexionbd)
9. [Capa de presentación — Controladores y FXML](#9-capa-de-presentación--controladores-y-fxml)
   - [9.1 Main.java y arranque de la aplicación](#91-mainjava-y-arranque-de-la-aplicación)
   - [9.2 LoginController](#92-logincontroller)
   - [9.3 DashboardController](#93-dashboardcontroller)
   - [9.4 ListadoJugadoresController](#94-listadojugadorescontroller)
   - [9.5 FormJugadorController](#95-formjugadorcontroller)
   - [9.6 EquiposController](#96-equiposcontroller)
   - [9.7 PartidosController y FormPartidoController](#97-partidoscontroller-y-formpartidocontroller)
   - [9.8 ConvocatoriasController](#98-convocatoriascontroller)
   - [9.9 EntrenamientosController](#99-entrenamientoscontroller)
   - [9.10 HistorialMedicoController](#910-historialmedicocontroller)
   - [9.11 ObjetivosController](#911-objetivoscontroller)
   - [9.12 RankingController](#912-rankingcontroller)
   - [9.13 EstadisticasJugadorController](#913-estadisticasjugadorcontroller)
   - [9.14 GraficosRendimientoController](#914-graficosrendimientocontroller)
   - [9.15 InformesController](#915-informescontroller)
   - [9.16 InfoClubController y ConfiguracionController](#916-infoclubcontroller-y-configuracioncontroller)
   - [9.17 LoadingController](#917-loadingcontroller)
10. [Componentes de interfaz personalizados](#10-componentes-de-interfaz-personalizados)
11. [Flujo de autenticación](#11-flujo-de-autenticación)
12. [Roles de usuario y permisos](#12-roles-de-usuario-y-permisos)
13. [Generación de informes con JasperReports](#13-generación-de-informes-con-jasperreports)
14. [Configuración y despliegue](#14-configuración-y-despliegue)
15. [Conclusiones y trabajo futuro](#15-conclusiones-y-trabajo-futuro)

---

## 1. Introducción y motivación

Cuando empecé a pensar en qué proyecto de fin de ciclo hacer, me di cuenta de que en el mundo del fútbol base hay una necesidad bastante real que nadie está cubriendo bien: los clubes pequeños no tienen ninguna herramienta decente para gestionar sus equipos. Usan grupos de WhatsApp para las convocatorias, hojas de Excel para los partidos y cuadernos en papel para las lesiones. Visto eso, me pareció que había una oportunidad clara.

Así nació **Scouteo**: una aplicación de escritorio desarrollada en **JavaFX** que permite a clubes de fútbol gestionar jugadores, partidos, entrenamientos, historial médico, estadísticas individuales y colectivas, y generar informes en PDF. Todo esto conectado a una **API REST** desplegada en **Google Cloud Run** para que los datos sean accesibles desde cualquier instalación del programa.

La idea era construir algo funcional de verdad, no un proyecto de juguete. Por eso integré autenticación con JWT, tres roles de usuario distintos, generación de PDF con JasperReports y gráficos con TilesFX. No todo ha sido fácil —hay cosas que he tenido que refactorizar tres veces hasta que me han quedado bien— pero el resultado final es una aplicación que cualquier club podría usar.

---

## 2. Descripción general de la aplicación

Scouteo es una aplicación de escritorio multiplataforma orientada a la **gestión deportiva de clubes de fútbol**. Sus funcionalidades principales son:

- **Gestión de jugadores:** alta, edición, baja, foto de perfil y estado (activo/lesionado/cedido).
- **Gestión de equipos:** creación de equipos por categoría y temporada, asignación de entrenadores.
- **Partidos:** registro de resultados, estadísticas generales del equipo y estadísticas individuales por jugador.
- **Convocatorias:** selección de jugadores convocados y alineación para cada partido.
- **Entrenamientos:** programación y registro de asistencias.
- **Historial médico:** control de lesiones con fecha estimada de recuperación.
- **Objetivos:** seguimiento de metas individuales y colectivas con porcentaje de progreso.
- **Ranking:** clasificación dinámica de goleadores y asistentes con filtros por posición y equipo.
- **Estadísticas y gráficos:** estadísticas individuales acumuladas y gráficos de rendimiento interactivos.
- **Informes PDF:** generación de reportes con JasperReports para directivos y entrenadores.
- **Gestión de club:** configuración de datos del club y administración de usuarios.

La aplicación soporta tres **roles de usuario**: directiva (acceso total), entrenador (acceso a sus equipos) y jugador (acceso a sus propios datos).

---

## 3. Stack tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | JDK 24 |
| Interfaz gráfica | JavaFX | 25 |
| Componentes UI avanzados | ControlsFX | 11.2.1 |
| Gráficos e indicadores | TilesFX | 21.0.9 |
| Construcción del proyecto | Gradle | 8.12 |
| Comunicación HTTP | HttpClient (nativo Java 11+) | — |
| Serialización JSON | Gson | 2.10.1 |
| Hash de contraseñas | BCrypt (favre-lib) | 0.10.2 |
| Generación de reportes | JasperReports | 7.0.3 |
| Base de datos (backend) | Supabase (PostgreSQL) | — |
| ORM (backend) | Spring Data JPA / Hibernate | — |
| Backend API | REST en Google Cloud Run | — |
| Autenticación | JWT (generado por la API) | — |

Para el **build** uso Gradle con el plugin de JavaFX, lo que hace que compilar y ejecutar sea tan sencillo como `./gradlew run`. Las dependencias de JasperReports son las más pesadas del proyecto —tiran de un montón de librerías transitivas— pero merece la pena por la calidad de los PDF que genera.

---

## 4. Arquitectura del sistema

La arquitectura de Scouteo sigue el patrón **MVC (Modelo-Vista-Controlador)** propio de JavaFX, pero adaptado para trabajar contra una API REST en lugar de conectarse directamente a la base de datos desde el cliente.

```
┌─────────────────────────────────────────────────────┐
│                   CLIENTE (JavaFX)                   │
│                                                     │
│  ┌──────────┐    ┌──────────────┐    ┌───────────┐ │
│  │  FXML    │◄──►│  Controller  │◄──►│    DAO    │ │
│  │ (Vista)  │    │  (Lógica UI) │    │ (Acceso   │ │
│  └──────────┘    └──────────────┘    │  a datos) │ │
│                                      └─────┬─────┘ │
│                         ┌──────────────────┘        │
│                         ▼                           │
│                  ┌─────────────┐                    │
│                  │  ApiClient  │                    │
│                  │  (Singleton)│                    │
│                  └──────┬──────┘                    │
└─────────────────────────┼───────────────────────────┘
                          │ HTTPS + JWT
                          ▼
┌─────────────────────────────────────────────────────┐
│              BACKEND (Google Cloud Run)              │
│                    API REST                          │
│                                                     │
│  /api/auth, /api/jugadores, /api/partidos, ...      │
│                         │                           │
│                         ▼                           │
│                    Base de datos                    │
│            Supabase (PostgreSQL en nube)             │
└─────────────────────────────────────────────────────┘
```

El cliente **nunca** habla directamente con la base de datos en producción. Toda la comunicación pasa por la API REST, que valida el token JWT en cada petición. Esto tiene varias ventajas: la lógica de negocio está centralizada en el servidor, es más fácil escalar, y la seguridad es mejor porque las credenciales de la base de datos nunca están en el cliente.

La clase `ConexionBD` existe en el cliente pero es código **legacy**: su propio método `isConexionValida()` delega en `ApiClient`, por lo que en la práctica no abre ninguna conexión directa a la base de datos. Toda la persistencia real está en el servidor.

---

## 5. Estructura del proyecto

```
Scouteo_inicios/
├── app/
│   ├── build.gradle                          ← Configuración Gradle
│   └── src/main/
│       ├── java/com/javafx/scouteo/
│       │   ├── Main.java                     ← Punto de entrada
│       │   ├── controller/                   ← 20 controladores FXML
│       │   │   ├── LoginController.java
│       │   │   ├── DashboardController.java
│       │   │   ├── ListadoJugadoresController.java
│       │   │   ├── FormJugadorController.java
│       │   │   ├── EquiposController.java
│       │   │   ├── PartidosController.java
│       │   │   ├── FormPartidoController.java
│       │   │   ├── ConvocatoriasController.java
│       │   │   ├── EntrenamientosController.java
│       │   │   ├── HistorialMedicoController.java
│       │   │   ├── ObjetivosController.java
│       │   │   ├── RankingController.java
│       │   │   ├── EstadisticasJugadorController.java
│       │   │   ├── GraficosRendimientoController.java
│       │   │   ├── FormEstadisticaController.java
│       │   │   ├── FormEstadisticasPartidoController.java
│       │   │   ├── InformesController.java
│       │   │   ├── InfoClubController.java
│       │   │   ├── ConfiguracionController.java
│       │   │   ├── LoadingController.java
│       │   │   └── ModalConfirmacionController.java
│       │   ├── dao/                          ← 13 clases DAO
│       │   │   ├── UsuarioDAO.java
│       │   │   ├── JugadorDAO.java
│       │   │   ├── EquipoDAO.java
│       │   │   ├── PartidoDAO.java
│       │   │   ├── JugadorPartidoDAO.java
│       │   │   ├── EstadisticaPartidoDAO.java
│       │   │   ├── EntrenamientoDAO.java
│       │   │   ├── HistorialMedicoDAO.java
│       │   │   ├── ObjetivoDAO.java
│       │   │   ├── EventoPartidoDAO.java
│       │   │   ├── RankingDAO.java
│       │   │   ├── ConfiguracionDAO.java
│       │   │   └── ClubesDAO.java
│       │   ├── model/                        ← 13 entidades
│       │   │   ├── Usuario.java
│       │   │   ├── Jugador.java
│       │   │   ├── Equipo.java
│       │   │   ├── Partido.java
│       │   │   ├── Alineacion.java
│       │   │   ├── JugadorPartido.java
│       │   │   ├── EstadisticaPartido.java
│       │   │   ├── Entrenamiento.java
│       │   │   ├── AsistenciaEntrenamiento.java
│       │   │   ├── Objetivo.java
│       │   │   ├── HistorialMedico.java
│       │   │   ├── EventoPartido.java
│       │   │   └── Configuracion.java
│       │   ├── util/                         ← Utilidades core
│       │   │   ├── ApiClient.java
│       │   │   ├── ConexionBD.java
│       │   │   └── SesionUsuario.java
│       │   └── utils/                        ← Componentes UI
│       │       ├── FootballLoadingPane.java
│       │       ├── StageUtils.java
│       │       └── TooltipUtils.java
│       └── resources/
│           ├── views/                        ← 20 archivos FXML
│           ├── images/                       ← Iconos y recursos gráficos
│           ├── reports/                      ← Plantillas .jrxml
│           ├── scouteo.css                   ← Estilos globales
│           └── bbdd.properties               ← Configuración BD local
├── BBDD/                                     ← Scripts SQL
├── installer/                                ← Generador de instalador
└── DOCUMENTACION.md                          ← Este archivo
```

---

## 6. Modelo de datos

El modelo de datos de Scouteo tiene 13 entidades que representan todos los conceptos del dominio deportivo. Cada una de ellas es un POJO (Plain Old Java Object) con sus getters y setters correspondientes. Las instancias de estos modelos son lo que circula entre controladores y DAOs.

### 6.1 Usuario

Representa a cualquier persona que puede iniciar sesión en el sistema. Los usuarios pueden tener tres roles: `directiva`, `entrenador` o `jugador`.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| email | String | Identificador de login |
| passwordHash | String | Contraseña hasheada con BCrypt |
| clubId | int | Club al que pertenece |
| rol | String | directiva / entrenador / jugador |
| nombre | String | Nombre del usuario |
| apellidos | String | Apellidos |
| telefono | String | Teléfono de contacto |
| fotoUrl | String | URL de la foto de perfil |
| activo | boolean | Si la cuenta está activa |
| ultimoAcceso | LocalDateTime | Último inicio de sesión |

### 6.2 Jugador

Es la entidad central de la aplicación. Un jugador puede estar vinculado a un usuario del sistema (si usa la app) o simplemente existir como registro en el club.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| equipoId | int | Equipo al que pertenece |
| usuarioId | Integer | Usuario asociado (puede ser null) |
| nombre | String | Nombre del jugador |
| apellidos | String | Apellidos |
| fechaNacimiento | LocalDate | Fecha de nacimiento |
| dorsal | int | Número de camiseta |
| posicion | String | Posición en el campo |
| piernaDominante | String | derecha / izquierda / ambas |
| alturaCm | int | Altura en centímetros |
| pesoKg | double | Peso en kilogramos |
| fotoUrl | String | URL de la foto |
| estado | String | activo / lesionado / cedido / baja |
| edad | int | Calculado a partir de fechaNacimiento |

### 6.3 Equipo

Representa un equipo dentro de un club. Un club puede tener varios equipos (infantil, cadete, juvenil, senior...).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| nombre | String | Nombre del equipo |
| categoria | String | Categoría (juvenil, senior...) |
| temporada | String | Ej: "2024-2025" |
| campo | String | Campo habitual |
| escudoUrl | String | URL del escudo |
| activo | boolean | Si el equipo está activo |
| clubId | int | Club propietario |

### 6.4 Partido

Registra toda la información de un partido, tanto datos generales como estadísticas colectivas del equipo.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| equipoId | int | Equipo que jugó el partido |
| fechaHora | LocalDateTime | Fecha y hora del partido |
| rival | String | Nombre del equipo rival |
| tipo | String | local / visitante / neutral |
| campo | String | Campo donde se jugó |
| superficie | String | Tipo de superficie |
| competicion | String | Liga, copa, amistoso... |
| temporada | String | Temporada del partido |
| formacion | String | Formación táctica (ej: 4-3-3) |
| golesFavor | int | Goles marcados |
| golesContra | int | Goles encajados |
| posesionPct | double | Porcentaje de posesión |
| tirosTotales | int | Total de tiros |
| tirosAPuerta | int | Tiros entre los tres palos |
| corners | int | Saques de esquina |
| faltas | int | Faltas cometidas |
| notas | String | Observaciones del entrenador |

### 6.5 Alineación

Recoge la participación individual de cada jugador en un partido concreto: minutos jugados, goles, tarjetas y valoración.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| partidoId | int | Partido asociado |
| jugadorId | int | Jugador asociado |
| minutoEntrada | int | Minuto en que entró |
| minutoSalida | int | Minuto en que salió (90 si jugó entero) |
| posicionPartido | String | Posición jugada en ese partido |
| goles | int | Goles marcados |
| asistencias | int | Asistencias dadas |
| tarjetasAmarillas | int | Número de amarillas |
| tarjetasRojas | int | Número de rojas |
| paradas | int | Solo porteros |
| golesEncajados | int | Solo porteros |
| porteriaCero | boolean | Si mantuvo la portería a cero |
| valoracion | double | Valoración del 1 al 10 |
| notaEntrenador | String | Nota del entrenador sobre el jugador |

### 6.6 Entrenamiento y Asistencia

`Entrenamiento` registra las sesiones de trabajo planificadas. `AsistenciaEntrenamiento` registra si cada jugador asistió o no a cada sesión.

**Entrenamiento:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| equipoId | int | Equipo al que pertenece |
| fechaHora | LocalDateTime | Fecha y hora |
| duracionMin | int | Duración en minutos |
| tipo | String | Físico, técnico, táctico... |
| intensidad | String | Baja / media / alta |
| objetivos | String | Objetivos de la sesión |
| observaciones | String | Notas adicionales |

**AsistenciaEntrenamiento:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| entrenamientoId | int | Entrenamiento asociado |
| jugadorId | int | Jugador asociado |
| estado | String | asistio / falta_justificada / falta_injustificada |
| motivo | String | Motivo de la falta (si aplica) |

### 6.7 Objetivo

Permite definir metas tanto para el equipo como para jugadores individuales, con seguimiento de progreso.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| equipoId | int | Equipo al que aplica |
| jugadorId | Integer | Jugador específico (null = objetivo de equipo) |
| descripcion | String | Descripción del objetivo |
| fechaInicio | LocalDate | Fecha de inicio |
| fechaLimite | LocalDate | Fecha límite |
| prioridad | String | Alta / media / baja |
| estado | String | pendiente / en_progreso / completado / cancelado |
| progresoPct | int | Porcentaje de avance (0–100) |
| observaciones | String | Notas adicionales |

### 6.8 Historial Médico

Registra las lesiones de cada jugador con sus fechas y observaciones.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| jugadorId | int | Jugador lesionado |
| tipoLesion | String | Tipo de lesión |
| zonaAfectada | String | Parte del cuerpo |
| fechaLesion | LocalDate | Cuándo ocurrió |
| fechaRecuperacionEst | LocalDate | Fecha estimada de alta |
| fechaAlta | LocalDate | Fecha real de alta médica |
| observaciones | String | Notas del médico o fisio |

### 6.9 Evento de Partido

Registra eventos que ocurren durante un partido (goles, tarjetas, cambios, lesiones) con el minuto exacto.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Clave primaria |
| partidoId | int | Partido donde ocurrió |
| minuto | int | Minuto del evento |
| tipo | String | gol / tarjeta / cambio / lesion |
| jugadorId | int | Jugador protagonista |
| jugador2Id | Integer | Segundo jugador (en cambios: el que entra) |
| descripcion | String | Descripción del evento |

### 6.10 Estadística de Partido y JugadorPartido

`JugadorPartido` es la tabla de unión entre jugadores y partidos. `EstadisticaPartido` recoge el detalle estadístico de esa participación.

**JugadorPartido:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| idJugadorPartido | int | Clave primaria |
| idJugador | int | Jugador |
| idPartido | int | Partido |
| idEstadistica | int | Estadística asociada |
| titular | boolean | Si fue titular |
| convocado | boolean | Si fue convocado |

**EstadisticaPartido:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| idEstadistica | int | Clave primaria |
| goles | int | Goles marcados |
| asistencias | int | Asistencias dadas |
| paradas | int | Paradas del portero |
| recuperaciones | int | Balones recuperados |
| tarjetasAmarillas | int | Tarjetas amarillas |
| tarjetasRojas | int | Tarjetas rojas |
| minutosJugados | int | Total de minutos |
| valoracion | double | Valoración del 1 al 10 |
| observaciones | String | Notas adicionales |

### 6.11 Configuración

Almacena los datos generales del club: nombre, localidad, presidente, contacto y temporada actual.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| idConfig | int | Clave primaria |
| clubId | int | Club al que corresponde |
| nombreClub | String | Nombre oficial del club |
| localidad | String | Ciudad o municipio |
| presidente | String | Nombre del presidente |
| email | String | Email de contacto del club |
| telefono | String | Teléfono del club |
| escudo | String | URL del escudo |
| temporadaActual | String | Ej: "2024-2025" |

---

## 7. Capa de acceso a datos — DAOs

### 7.1 Patrón DAO y comunicación con la API

El patrón **DAO (Data Access Object)** separa la lógica de acceso a datos de la lógica de negocio. En Scouteo todos los DAOs (excepto `ClubesDAO`) se comunican con la API REST a través de `ApiClient`, que encapsula las llamadas HTTP.

El flujo típico de una operación es:

```
Controller llama a DAO
    → DAO llama a ApiClient.get("/endpoint")
    → ApiClient añade el JWT al header y ejecuta la petición HTTP
    → Recibe JSON de respuesta
    → ApiClient.fromJson() deserializa el JSON con Gson
    → DAO devuelve el objeto Java al Controller
```

Para las operaciones que tardan en responder, los controladores lanzan las llamadas al DAO en un **Thread separado** para no bloquear el hilo de la interfaz gráfica (JavaFX FX Application Thread). El resultado se devuelve con `Platform.runLater()`.

### 7.2 UsuarioDAO

Gestiona la autenticación y la consulta de usuarios del sistema.

| Método | Descripción |
|--------|-------------|
| `autenticar(email, pass)` | Llama a `/auth/login` y devuelve el token JWT junto con los datos del usuario |
| `obtenerPorId(id)` | Obtiene los datos de un usuario por su ID |
| `existeEmail(email)` | Comprueba si ya existe un usuario con ese email |
| `insertar(usuario)` | Registra un nuevo usuario |
| `obtenerEntrenadoresPorClub(clubId)` | Lista todos los entrenadores del club |

### 7.3 JugadorDAO

Es el DAO más completo, con operaciones de CRUD y consultas especializadas para la gestión de la plantilla.

| Método | Descripción |
|--------|-------------|
| `insertar(jugador)` | Da de alta un nuevo jugador |
| `obtenerTodos()` | Lista todos los jugadores del sistema |
| `obtenerPorEquipo(equipoId)` | Lista los jugadores de un equipo |
| `obtenerActivosPorEquipo(equipoId)` | Solo los jugadores en estado activo |
| `obtenerPorId(id)` | Ficha completa de un jugador |
| `contarPorEquipo(equipoId)` | Número de jugadores en el equipo |
| `obtenerDistribucionPorPosicion(equipoId)` | Cuántos jugadores hay por posición |
| `cambiarEstado(id, estado)` | Cambia el estado del jugador (lesionado, cedido...) |
| `vincularUsuario(jugadorId, usuarioId)` | Asocia un usuario de la app a un jugador |
| `dorsalDisponible(equipoId, dorsal)` | Comprueba si un número de dorsal está libre |

### 7.4 EquipoDAO

Gestiona los equipos del club y la asignación de entrenadores.

| Método | Descripción |
|--------|-------------|
| `insertar(equipo)` | Crea un nuevo equipo |
| `obtenerTodos()` | Lista todos los equipos |
| `obtenerPorClub(clubId)` | Equipos de un club concreto |
| `obtenerPorEntrenador(entrenadorId)` | Equipos asignados a un entrenador |
| `obtenerPorId(id)` | Datos de un equipo concreto |
| `actualizar(equipo)` | Edita los datos del equipo |
| `eliminar(id)` | Elimina un equipo |
| `asignarEntrenador(equipoId, usuarioId)` | Asigna un entrenador a un equipo |
| `obtenerEntrenadoresPorEquipo(equipoId)` | Lista los entrenadores del equipo |
| `quitarEntrenador(equipoId, usuarioId)` | Desvincula un entrenador |

### 7.5 PartidoDAO

Gestiona el registro de partidos y sus estadísticas colectivas.

| Método | Descripción |
|--------|-------------|
| `insertar(partido)` | Registra un nuevo partido |
| `obtenerTodos()` | Lista todos los partidos |
| `obtenerPorEquipo(equipoId)` | Partidos de un equipo concreto |
| `obtenerPorId(id)` | Datos completos de un partido |
| `contarResultados(equipoId)` | Victorias, empates y derrotas del equipo |
| `contarGolesEquipo(equipoId)` | Goles a favor y en contra |
| `actualizar(partido)` | Edita los datos del partido |
| `eliminar(id)` | Elimina un partido |

### 7.6 AlineacionDAO y JugadorPartidoDAO

`JugadorPartidoDAO` gestiona la relación muchos a muchos entre jugadores y partidos, incluyendo sus estadísticas individuales.

| Método | Descripción |
|--------|-------------|
| `insertarConEstadistica(jugadorPartido, estadistica)` | Registra la participación de un jugador en un partido junto con su estadística |
| `obtenerPorJugador(jugadorId)` | Todos los partidos de un jugador |
| `obtenerPorPartido(partidoId)` | Todos los jugadores que participaron en un partido |
| `obtenerPorJugadorYPartido(jugadorId, partidoId)` | La participación concreta de un jugador en un partido |
| `actualizar(jugadorPartido)` | Actualiza los datos de participación |
| `eliminar(id)` | Elimina un registro de participación |

### 7.7 EntrenamientoDAO

Gestiona las sesiones de entrenamiento y el registro de asistencias de los jugadores.

| Método | Descripción |
|--------|-------------|
| `insertar(entrenamiento)` | Registra una nueva sesión |
| `obtenerPorEquipo(equipoId)` | Sesiones de un equipo |
| `actualizar(entrenamiento)` | Edita los datos de la sesión |
| `eliminar(id)` | Elimina una sesión |
| `obtenerAsistenciaPorEntrenamiento(entrenamientoId)` | Lista de asistencias de una sesión |
| `registrarAsistencia(asistencia)` | Registra o actualiza la asistencia de un jugador |

### 7.8 HistorialMedicoDAO

Controla el historial de lesiones de los jugadores.

| Método | Descripción |
|--------|-------------|
| `insertar(historial)` | Registra una nueva lesión |
| `obtenerPorJugador(jugadorId)` | Historial completo de un jugador |
| `obtenerPorEquipo(equipoId)` | Lesiones activas o históricas del equipo |
| `actualizar(historial)` | Actualiza los datos de una lesión |
| `eliminar(id)` | Elimina un registro |
| `contarLesionesActivas(equipoId)` | Número de jugadores lesionados ahora mismo |

### 7.9 ObjetivoDAO

Gestiona los objetivos del equipo y de los jugadores con su seguimiento.

| Método | Descripción |
|--------|-------------|
| `insertar(objetivo)` | Crea un nuevo objetivo |
| `obtenerPorEquipo(equipoId)` | Objetivos del equipo |
| `obtenerPorJugador(jugadorId)` | Objetivos de un jugador |
| `obtenerTodosPorEquipoOJugadores(equipoId)` | Objetivos del equipo y de todos sus jugadores |
| `actualizar(objetivo)` | Edita el objetivo (incluyendo el progreso) |
| `eliminar(id)` | Elimina un objetivo |

### 7.10 EventoPartidoDAO

Registra y consulta los eventos que ocurren durante un partido.

| Método | Descripción |
|--------|-------------|
| `obtenerPorPartido(partidoId)` | Todos los eventos de un partido ordenados por minuto |
| `insertar(evento)` | Registra un nuevo evento |
| `eliminar(id)` | Elimina un evento |

### 7.11 RankingDAO

Genera rankings dinámicos de jugadores según distintos criterios estadísticos.

| Método | Descripción |
|--------|-------------|
| `obtenerRanking(ordenarPor, posicionFiltro, clubId, equipoId)` | Genera el ranking con los filtros indicados. `ordenarPor` puede ser "goles", "asistencias", "valoracion", etc. |

Este DAO es especialmente interesante porque con un único método y distintos parámetros genera clasificaciones muy diferentes, delegando la lógica de ordenación en el backend.

### 7.12 EstadisticaPartidoDAO

Gestiona las estadísticas individuales de partido. A diferencia de los demás, usa un **caché en memoria** para evitar peticiones redundantes a la API.

| Método | Descripción |
|--------|-------------|
| `obtenerPorId(id)` | Obtiene una estadística por ID (primero busca en caché) |
| `actualizar(estadistica)` | Actualiza la estadística y limpia el caché |
| `eliminar(id)` | Elimina la estadística |
| `validar(estadistica)` | Valida que los datos son coherentes antes de guardar |

### 7.13 ConfiguracionDAO y ClubesDAO

`ConfiguracionDAO` gestiona los datos del club configurables desde la interfaz. `ClubesDAO` gestiona la creación inicial de un club comunicándose con el endpoint `/api/clubes` de la API REST, que a su vez escribe en Supabase.

**ConfiguracionDAO:**

| Método | Descripción |
|--------|-------------|
| `obtener(clubId)` | Carga la configuración del club |
| `actualizar(config)` | Guarda los cambios |
| `inicializar(clubId)` | Crea una configuración por defecto |
| `validar(config)` | Comprueba que los datos obligatorios están completos |

**ClubesDAO:**

| Método | Descripción |
|--------|-------------|
| `insertar(nombre)` | Crea un nuevo club en la base de datos local |

---

## 8. Utilidades del sistema

### 8.1 ApiClient

`ApiClient` es el componente más importante de toda la capa de infraestructura. Es un **Singleton** que encapsula toda la comunicación HTTP con el backend.

**URL base:** `https://scouteo-api-612681319622.europe-west1.run.app/api`

Los métodos que expone son:

| Método | Descripción |
|--------|-------------|
| `get(endpoint)` | Petición GET al endpoint indicado |
| `post(endpoint, body)` | Petición POST con cuerpo JSON |
| `put(endpoint, body)` | Petición PUT con cuerpo JSON |
| `delete(endpoint)` | Petición DELETE |
| `isDisponible()` | Comprueba si la API responde (útil al arrancar) |
| `fromJson(json, clase)` | Deserializa un JSON en un objeto Java con Gson |
| `fromJsonList(json, clase)` | Deserializa un JSON array en una List de Java |

Internamente usa el `HttpClient` nativo de Java (disponible desde Java 11), lo que evita añadir dependencias externas para las peticiones HTTP.

Dos aspectos clave de la implementación:

**Autenticación JWT:** Antes de cada petición, `ApiClient` añade el header `Authorization: Bearer <token>` con el JWT almacenado en `SesionUsuario`. Esto es transparente para los DAOs.

**Adaptadores de fecha:** Gson por defecto no sabe cómo serializar `LocalDate` y `LocalDateTime`. Por eso he registrado adaptadores personalizados que convierten estos tipos a String ISO-8601 y viceversa.

### 8.2 SesionUsuario

`SesionUsuario` es otro Singleton que actúa como el **contexto global de la sesión**. Cualquier parte de la aplicación puede consultarlo para saber quién está logueado y con qué permisos.

| Campo / Método | Descripción |
|----------------|-------------|
| `usuarioActual` | Objeto `Usuario` con todos los datos del usuario logueado |
| `jwtToken` | Token JWT para las peticiones a la API |
| `equipoIdSesion` | ID del equipo activo en la sesión (para entrenadores y jugadores) |
| `jugadorIdSesion` | ID del jugador si el usuario tiene rol jugador |
| `iniciarSesion(usuario, token)` | Establece la sesión |
| `cerrarSesion()` | Limpia todos los datos de sesión |
| `haySesionActiva()` | Comprueba si hay un usuario logueado |
| `esDirectiva()` | Devuelve true si el rol es "directiva" |
| `esEntrenador()` | Devuelve true si el rol es "entrenador" |
| `esJugador()` | Devuelve true si el rol es "jugador" |

### 8.3 ConexionBD

`ConexionBD` es código **legacy** del cliente. En las primeras versiones del proyecto la aplicación conectaba directamente a una base de datos local, pero tras migrar toda la persistencia a la API REST (que usa Supabase en el backend), esta clase quedó sin uso real.

Su método más significativo, `isConexionValida()`, ni siquiera abre una conexión JDBC: simplemente delega en `ApiClient.isDisponible()` para comprobar si el servidor responde.

El archivo `bbdd.properties` sigue existiendo en el classpath por compatibilidad, pero en producción no tiene efecto sobre el funcionamiento de la aplicación.

**Stack real del backend (Scouteo-API):**

```properties
# application-prod.properties (Spring Boot en Cloud Run)
spring.datasource.url=${SPRING_DATASOURCE_URL}          # URL de Supabase
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Las credenciales de Supabase se inyectan como variables de entorno en el despliegue de Cloud Run, nunca están hardcodeadas en el código.

---

## 9. Capa de presentación — Controladores y FXML

En JavaFX la interfaz se define en archivos **FXML** (que son básicamente XML describiendo la jerarquía de componentes visuales) y cada FXML tiene asociado un **controlador** Java que contiene toda la lógica de la pantalla. El vínculo entre ambos se declara en el propio FXML con el atributo `fx:controller`.

### 9.1 Main.java y arranque de la aplicación

`Main.java` es el punto de entrada de toda la aplicación. Al arrancar:

1. Inicializa el `Stage` principal de JavaFX.
2. Carga `Login.fxml` como primera pantalla.
3. Aplica `scouteo.css` a la escena.
4. Fija el tamaño de la ventana a 900×600 px y la centra en pantalla.
5. Establece el icono de la aplicación con `StageUtils.setAppIcon()`.

La ventana no es redimensionable por diseño, para garantizar que los layouts se ven correctamente en todas las instalaciones.

### 9.2 LoginController

Es la puerta de entrada al sistema. Gestiona dos flujos:

**Inicio de sesión:** El usuario introduce email y contraseña. El controlador llama a `UsuarioDAO.autenticar()`, que envía las credenciales a la API. Si la respuesta es correcta, el JWT y los datos del usuario se guardan en `SesionUsuario` y la pantalla navega al Dashboard.

**Registro de directiva:** Nuevo usuario de tipo directiva que crea un club nuevo. El flujo pasa por `ClubesDAO` para crear el club y por `UsuarioDAO` para registrar el usuario.

Para no bloquear la UI durante la petición HTTP, la llamada al DAO se ejecuta en un Thread separado, mostrando un indicador de carga mientras se espera la respuesta.

### 9.3 DashboardController

Es el controlador principal de la aplicación una vez que el usuario está logueado. Actúa como **contenedor de navegación**: tiene un panel lateral con los botones del menú y un área central donde se cargan los distintos módulos como secciones de la misma pantalla.

Funcionalidades clave:

- **Selector de equipo:** En la parte superior hay un ComboBox que permite al usuario cambiar el equipo activo. Cuando cambia, actualiza `SesionUsuario.equipoIdSesion` y recarga el contenido actual.
- **Overlay de carga:** Usa `FootballLoadingPane` para mostrar una animación mientras se cargan datos del servidor.
- **Estadísticas rápidas:** En la pantalla de inicio del dashboard muestra KPIs del equipo (jugadores activos, próximo partido, lesionados, etc.).
- **Control de visibilidad por rol:** Los entrenadores solo ven las secciones a las que tienen acceso; la directiva lo ve todo.

### 9.4 ListadoJugadoresController

Muestra la plantilla del equipo seleccionado en una tabla con filtros de búsqueda.

- La tabla usa `TableView` de JavaFX con columnas para nombre, dorsal, posición, edad y estado.
- El filtro de búsqueda usa un `TextField` enlazado a un `FilteredList` sobre los datos de la tabla.
- Los botones de acción abren `FormJugador.fxml` en modo creación o edición según corresponda.
- Hay un botón de cambio de estado rápido para marcar jugadores como lesionados o activos sin abrir el formulario.

### 9.5 FormJugadorController

Formulario de alta y edición de jugadores. Funciona en dos modos:

- **Modo creación:** Todos los campos vacíos, el botón guarda con `JugadorDAO.insertar()`.
- **Modo edición:** Los campos se precargan con los datos del jugador existente, el botón guarda con `JugadorDAO.actualizar()`.

El formulario valida los campos antes de enviar: el dorsal no puede estar duplicado en el equipo (consulta a `JugadorDAO.dorsalDisponible()`), la fecha de nacimiento debe ser coherente, etc.

### 9.6 EquiposController

Pantalla de gestión de equipos, accesible principalmente para la directiva. Muestra una tabla con todos los equipos del club y permite crear, editar y eliminar equipos. También permite asignar y desasignar entrenadores a cada equipo desde un diálogo auxiliar.

### 9.7 PartidosController y FormPartidoController

`PartidosController` muestra el historial de partidos del equipo en una tabla con el resultado (victoria/derrota/empate) destacado visualmente con colores. Permite filtrar por competición y temporada.

`FormPartidoController` gestiona el formulario de un partido. Es uno de los formularios más complejos de la aplicación porque además de los datos básicos del partido incluye:

- Un apartado para registrar estadísticas colectivas del equipo (posesión, tiros, corners...).
- Un selector de convocados con alineación táctica.
- Un registro de eventos del partido (goles, tarjetas, cambios) con minuto exacto.

### 9.8 ConvocatoriasController

Permite seleccionar los jugadores convocados para un partido y definir si son titulares o suplentes. Muestra la lista de jugadores disponibles (activos y sin lesión) con checkboxes para la convocatoria y botones de radio para titular/suplente.

### 9.9 EntrenamientosController

Gestiona las sesiones de entrenamiento. La pantalla tiene dos secciones: una tabla de sesiones programadas y, al seleccionar una sesión, una lista de todos los jugadores del equipo donde se puede registrar la asistencia de cada uno mediante un ComboBox (asistió / falta justificada / falta injustificada).

### 9.10 HistorialMedicoController

Muestra el historial de lesiones. Se puede filtrar por jugador concreto o ver todas las lesiones del equipo. El formulario de alta de lesión incluye el tipo de lesión, la zona afectada y las fechas de lesión y alta estimada.

Los jugadores con lesión activa (fecha de alta no registrada) aparecen destacados en rojo.

### 9.11 ObjetivosController

Gestiona los objetivos del equipo y de los jugadores individuales. Cada objetivo muestra una barra de progreso visual basada en el campo `progresoPct`. Los objetivos se pueden filtrar por estado (pendiente, en progreso, completado, cancelado) y por jugador.

### 9.12 RankingController

Genera clasificaciones dinámicas de jugadores. El usuario puede elegir el criterio de ordenación (goles, asistencias, valoración media, minutos jugados...) y filtrar por posición. La tabla se actualiza en tiempo real al cambiar los filtros.

### 9.13 EstadisticasJugadorController

Muestra las estadísticas acumuladas de un jugador a lo largo de la temporada: partidos jugados, goles, asistencias, tarjetas, valoración media, minutos totales, etc. También muestra el rendimiento partido a partido en una tabla secundaria.

### 9.14 GraficosRendimientoController

Presenta datos estadísticos de forma visual usando los componentes de **TilesFX**. Incluye gráficos de tiles para KPIs (goles por partido, % victorias, valoración media del equipo) y gráficos de barras/líneas para la evolución a lo largo de la temporada.

TilesFX es una librería de JavaFX que proporciona tiles animados tipo dashboard, muy útiles para representar métricas deportivas de forma atractiva.

### 9.15 InformesController

Permite generar informes en PDF usando **JasperReports**. Los tipos de informe disponibles son:

- Informe de plantilla (ficha de todos los jugadores).
- Informe de partido (resultado, estadísticas y alineación).
- Informe de temporada (estadísticas acumuladas del equipo).

Las plantillas están definidas en archivos `.jrxml` ubicados en `src/main/resources/reports/`. JasperReports compila estas plantillas, las rellena con los datos obtenidos de los DAOs y genera el PDF final, que se puede guardar en disco o previsualizar.

### 9.16 InfoClubController y ConfiguracionController

`InfoClubController` muestra de forma read-only los datos del club: nombre, localidad, presidente y contacto.

`ConfiguracionController` permite editar esos mismos datos si el usuario tiene rol directiva. También permite cambiar la temporada activa, que afecta a los filtros de toda la aplicación.

### 9.17 LoadingController

Pantalla de carga (splash screen) que se muestra mientras la aplicación se inicializa y comprueba la disponibilidad de la API. Muestra el logo de Scouteo y una barra de progreso animada. Si la API no responde, informa al usuario y permite continuar en modo offline (con funcionalidad limitada).

---

## 10. Componentes de interfaz personalizados

La carpeta `utils/` contiene componentes reutilizables que uso en varios controladores.

**FootballLoadingPane:** Es un overlay que cubre toda la pantalla cuando se está esperando una respuesta del servidor. Tiene una animación temática de fútbol (un balón que rueda) y un texto de estado. Lo uso en el Dashboard y en cualquier operación que tarde más de un instante.

**StageUtils:** Métodos estáticos de utilidad para las ventanas:
- `setAppIcon(stage)`: establece el icono de la aplicación en el Stage.
- `centerStage(stage)`: centra la ventana en la pantalla.
- `cambiarEscena(stage, fxml)`: navega a otra pantalla cargando el FXML indicado.

**TooltipUtils:** Simplifica la creación y el estilo de tooltips personalizados para botones e iconos, con un tiempo de aparición reducido respecto al default de JavaFX.

---

## 11. Flujo de autenticación

El sistema de autenticación combina **BCrypt** para el hash de contraseñas y **JWT** para mantener la sesión autenticada entre peticiones. El proceso completo es:

**Paso 1 — El usuario introduce sus credenciales**

`LoginController` recoge el email y la contraseña del formulario.

**Paso 2 — Petición a la API**

`UsuarioDAO.autenticar()` llama a `ApiClient.post("/auth/login", {email, password})`. La contraseña viaja por HTTPS (nunca en texto plano sobre HTTP).

**Paso 3 — Respuesta del servidor**

El backend verifica la contraseña contra el hash BCrypt almacenado. Si es correcta, devuelve un JSON con:
- Token JWT firmado
- Datos del usuario (id, nombre, rol, clubId, equipoId, jugadorId)

**Paso 4 — Almacenamiento en sesión**

`SesionUsuario.iniciarSesion()` guarda el token y los datos del usuario en memoria.

**Paso 5 — Todas las peticiones posteriores**

`ApiClient` añade automáticamente `Authorization: Bearer <token>` a cada petición. El backend valida el token en cada llamada. Si el token ha expirado, devuelve 401 y la aplicación redirige al login.

**Cierre de sesión**

`SesionUsuario.cerrarSesion()` limpia todos los datos en memoria y la aplicación navega de vuelta al login.

---

## 12. Roles de usuario y permisos

Scouteo tiene tres roles con distintos niveles de acceso:

| Funcionalidad | Directiva | Entrenador | Jugador |
|---------------|-----------|------------|---------|
| Ver todos los equipos | Sí | No (solo los suyos) | No |
| Crear / eliminar equipos | Sí | No | No |
| Gestionar jugadores | Sí | Sí (su equipo) | No |
| Ver sus propios datos | Sí | Sí | Sí |
| Registrar partidos | Sí | Sí (su equipo) | No |
| Ver estadísticas | Sí | Sí (su equipo) | Sí (las suyas) |
| Generar informes | Sí | Sí | No |
| Configurar el club | Sí | No | No |
| Gestionar usuarios | Sí | No | No |

La lógica de visibilidad de los menús del dashboard se basa en `SesionUsuario.esDirectiva()`, `SesionUsuario.esEntrenador()` y `SesionUsuario.esJugador()`. Los entrenadores no ven la sección de gestión de equipos ni de usuarios; los jugadores solo acceden a su ficha y estadísticas.

---

## 13. Generación de informes con JasperReports

JasperReports es la librería que uso para generar los documentos PDF. Funciona con un ciclo de vida en tres fases:

**Fase 1 — Diseño (`.jrxml`):** El informe se diseña en un archivo XML que define el layout, los campos de datos, los estilos y los gráficos. Estos archivos están en `src/main/resources/reports/`.

**Fase 2 — Compilación (`.jasper`):** JasperReports compila el `.jrxml` a un formato binario optimizado `.jasper`. Esto se hace en tiempo de ejecución la primera vez que se genera el informe.

**Fase 3 — Relleno y exportación:** Se llama a `JasperFillManager.fillReport()` pasando el compilado y un `Map` con los datos (o un `JRDataSource` con la lista de registros). El resultado es un `JasperPrint` que se exporta a PDF con `JasperExportManager.exportReportToPdfFile()`.

El flujo en `InformesController` es:

```java
// 1. Cargar y compilar la plantilla
InputStream template = getClass().getResourceAsStream("/reports/informe_plantilla.jrxml");
JasperReport report = JasperCompileManager.compileReport(template);

// 2. Preparar los datos
List<Jugador> jugadores = jugadorDAO.obtenerPorEquipo(equipoId);
JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(jugadores);

// 3. Rellenar el informe
Map<String, Object> params = new HashMap<>();
params.put("CLUB_NOMBRE", sesion.getNombreClub());
JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

// 4. Exportar a PDF
JasperExportManager.exportReportToPdfFile(print, rutaDestino);
```

---

## 14. Configuración y despliegue

**Requisitos previos:**
- JDK 24 instalado y en el PATH.
- Cuenta en Supabase con la base de datos del proyecto ya creada (solo para quien despliegue la API; el cliente no necesita acceso directo).
- Conexión a Internet para acceder a la API en Google Cloud Run.

**Ejecución en desarrollo:**

```bash
./gradlew run
```

Gradle descarga automáticamente todas las dependencias y ejecuta la aplicación. Las dependencias de JavaFX se gestionan con el plugin `org.openjfx.javafxplugin`.

**Generación del instalador:**

La carpeta `installer/` contiene los scripts para generar el instalador de la aplicación. El proceso usa `jpackage` (incluido en el JDK 14+) para crear un instalador nativo para Windows.

```bash
./gradlew jpackage
```

El instalador resultante se deposita en `installer-output/`.

**Variables de configuración:**

La URL de la API está definida en `ApiClient.java` y apunta a la instancia de Google Cloud Run. El archivo `bbdd.properties` es legacy y no afecta al funcionamiento en producción. Las credenciales de Supabase se gestionan como variables de entorno en el servidor de la API.

---

## 15. Conclusiones y trabajo futuro

Desarrollar Scouteo ha sido el proyecto más ambicioso que he afrontado en el ciclo. Ha habido partes técnicas que me han costado bastante —la integración con JasperReports, los adaptadores de fecha de Gson y la gestión de hilos para no bloquear la interfaz de JavaFX— pero el resultado final me parece sólido y funcional.

Las cosas que creo que han salido especialmente bien son la arquitectura en capas (separar bien los modelos, los DAOs y los controladores ha hecho que el código sea mucho más mantenible), el sistema de roles (es limpio y se puede extender fácilmente) y la integración con la API REST (que hace que la aplicación pueda usarse desde varias instalaciones compartiendo los mismos datos).

Como trabajo futuro, hay varias líneas interesantes:

- **Modo offline completo:** Ahora mismo si la API no responde la funcionalidad es muy limitada. Sería útil implementar una caché local que sincronice con el servidor cuando vuelva la conexión.
- **Módulo de chat o notificaciones:** Para que los entrenadores puedan comunicarse con los jugadores directamente desde la aplicación.
- **Aplicación móvil:** Una app Android o iOS para que los jugadores puedan ver su ficha, las convocatorias y los entrenamientos desde el teléfono.
- **Dashboard con IA:** Usar modelos de machine learning para predecir el rendimiento de los jugadores o sugerir alineaciones basándose en el histórico de partidos.
- **Integración con wearables:** Conectar con dispositivos de seguimiento físico para importar datos de entrenamiento automáticamente.

En definitiva, Scouteo es una aplicación con la que me siento orgulloso. He aprendido muchísimo durante el desarrollo y creo que refleja bien lo que hemos trabajado durante los dos años del ciclo.

---

*Documentación generada el 11 de mayo de 2026.*  
*Alberto Pérez Oncina — DAM 2.º curso*
