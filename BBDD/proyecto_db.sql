-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Servidor: mariadb:3306
-- Tiempo de generación: 18-11-2025 a las 15:26:27
-- Versión del servidor: 12.0.2-MariaDB-ubu2404
-- Versión de PHP: 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `proyecto_db`
--
CREATE DATABASE IF NOT EXISTS `proyecto_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;
USE `proyecto_db`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuracion`
--

DROP TABLE IF EXISTS `configuracion`;
CREATE TABLE `configuracion` (
  `id_config` int(11) NOT NULL DEFAULT 1,
  `nombre_club` varchar(100) NOT NULL,
  `localidad` varchar(100) NOT NULL,
  `presidente` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefono` varchar(15) NOT NULL,
  `escudo` blob DEFAULT NULL COMMENT 'Imagen del escudo en formato binario',
  `temporada_actual` varchar(10) NOT NULL COMMENT 'Formato: 2024/25',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ;

--
-- Volcado de datos para la tabla `configuracion`
--

INSERT INTO `configuracion` (`id_config`, `nombre_club`, `localidad`, `presidente`, `email`, `telefono`, `escudo`, `temporada_actual`, `created_at`, `updated_at`) VALUES
(1, 'CD Cádiz Base', 'Cádiz', 'Manuel López García', 'info@cdcadizbase.com', '+34 956 111 222', NULL, '2024/25', '2025-11-18 13:16:43', '2025-11-18 15:23:08');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estadisticas_partido`
--

DROP TABLE IF EXISTS `estadisticas_partido`;
CREATE TABLE `estadisticas_partido` (
  `id_estadistica` int(11) NOT NULL,
  `goles` int(11) DEFAULT 0,
  `asistencias` int(11) DEFAULT 0,
  `paradas` int(11) DEFAULT NULL COMMENT 'Solo para porteros',
  `recuperaciones` int(11) DEFAULT NULL COMMENT 'Para defensas y medios',
  `tarjetas_amarillas` int(11) DEFAULT 0,
  `tarjetas_rojas` int(11) DEFAULT 0,
  `minutos_jugados` int(11) NOT NULL COMMENT 'Minutos que jugó (0-90+)',
  `valoracion` decimal(3,1) NOT NULL COMMENT 'Valoración 0.0-10.0',
  `observaciones` text DEFAULT NULL
) ;

--
-- Volcado de datos para la tabla `estadisticas_partido`
--

INSERT INTO `estadisticas_partido` (`id_estadistica`, `goles`, `asistencias`, `paradas`, `recuperaciones`, `tarjetas_amarillas`, `tarjetas_rojas`, `minutos_jugados`, `valoracion`, `observaciones`) VALUES
(1, 0, 0, 6, 2, 0, 0, 90, 7.5, NULL),
(2, 0, 0, NULL, 5, 0, 0, 90, 7.8, NULL),
(3, 0, 1, NULL, 4, 1, 0, 90, 7.6, NULL),
(4, 0, 0, NULL, 6, 0, 0, 90, 7.7, NULL),
(5, 1, 0, NULL, 3, 0, 0, 90, 8.0, NULL),
(6, 0, 1, NULL, 4, 0, 0, 90, 7.9, NULL),
(7, 0, 1, NULL, 5, 0, 0, 90, 8.1, NULL),
(8, 1, 0, NULL, NULL, 0, 0, 90, 8.2, NULL),
(9, 1, 1, NULL, NULL, 0, 0, 90, 8.5, NULL),
(10, 0, 0, NULL, NULL, 0, 0, 60, 6.8, NULL),
(11, 0, 0, NULL, NULL, 0, 0, 30, 6.5, NULL),
(12, 0, 0, 5, 1, 0, 0, 90, 6.8, NULL),
(13, 0, 0, NULL, 4, 1, 0, 90, 7.0, NULL),
(14, 0, 0, NULL, 3, 0, 0, 90, 6.9, NULL),
(15, 0, 1, NULL, 5, 0, 0, 90, 7.4, NULL),
(16, 1, 0, NULL, 4, 0, 0, 90, 7.8, NULL),
(17, 0, 1, NULL, 3, 0, 0, 90, 7.5, NULL),
(18, 0, 0, NULL, 4, 0, 0, 90, 7.2, NULL),
(19, 1, 1, NULL, NULL, 0, 0, 90, 8.0, NULL),
(20, 0, 1, NULL, NULL, 0, 0, 90, 7.6, NULL),
(21, 0, 0, NULL, NULL, 0, 0, 70, 6.8, NULL),
(22, 0, 0, NULL, NULL, 0, 0, 20, 6.3, NULL),
(23, 0, 0, 2, 1, 0, 0, 90, 7.5, NULL),
(24, 0, 1, NULL, 4, 0, 0, 90, 8.0, NULL),
(25, 1, 0, NULL, 5, 0, 0, 90, 8.3, NULL),
(26, 0, 1, NULL, 3, 0, 0, 90, 8.1, NULL),
(27, 1, 1, NULL, 5, 0, 0, 90, 9.0, NULL),
(28, 0, 2, NULL, 4, 0, 0, 90, 8.8, NULL),
(29, 0, 0, NULL, 4, 0, 0, 90, 7.9, NULL),
(30, 2, 0, NULL, NULL, 0, 0, 90, 9.5, NULL),
(31, 0, 1, NULL, NULL, 0, 0, 90, 8.2, NULL),
(32, 1, 0, NULL, NULL, 0, 0, 60, 8.0, NULL),
(33, 0, 0, NULL, NULL, 0, 0, 30, 7.3, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `jugadores`
--

DROP TABLE IF EXISTS `jugadores`;
CREATE TABLE `jugadores` (
  `id_jugador` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `dorsal` int(11) NOT NULL,
  `posicion` enum('POR','DEF','MED','DEL') NOT NULL COMMENT 'POR=Portero, DEF=Defensa, MED=Medio, DEL=Delantero',
  `altura` decimal(5,2) DEFAULT NULL COMMENT 'Altura en centímetros',
  `peso` decimal(5,2) DEFAULT NULL COMMENT 'Peso en kilogramos',
  `categoria` varchar(20) NOT NULL COMMENT 'Benjamín, Alevín, Infantil, Cadete',
  `foto` blob DEFAULT NULL COMMENT 'Fotografía del jugador',
  `estado` enum('Activo','Lesionado','Inactivo') DEFAULT 'Activo',
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='Jugadores del club';

--
-- Volcado de datos para la tabla `jugadores`
--

INSERT INTO `jugadores` (`id_jugador`, `nombre`, `apellidos`, `fecha_nacimiento`, `dorsal`, `posicion`, `altura`, `peso`, `categoria`, `foto`, `estado`, `created_at`) VALUES
(1, 'Lucas', 'Martínez', '2012-03-15', 1, 'POR', 145.00, 38.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(2, 'Sergio', 'García', '2012-05-10', 2, 'DEF', 148.00, 42.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(3, 'Daniel', 'Jiménez', '2012-07-18', 3, 'DEF', 150.00, 44.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(4, 'Javier', 'Sánchez', '2013-01-25', 4, 'DEF', 142.00, 38.00, 'Alevín', NULL, 'Activo', '2025-11-18 15:23:08'),
(5, 'Mario', 'López', '2012-06-20', 6, 'MED', 147.00, 41.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(6, 'Adrián', 'Hernández', '2012-12-05', 8, 'MED', 145.00, 39.50, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(7, 'Hugo', 'Álvarez', '2013-03-18', 10, 'MED', 141.00, 36.50, 'Alevín', NULL, 'Activo', '2025-11-18 15:23:08'),
(8, 'Diego', 'Moreno', '2012-04-28', 7, 'DEL', 149.00, 43.50, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(9, 'Marcos', 'Navarro', '2012-09-15', 9, 'DEL', 151.00, 45.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(10, 'Jorge', 'Gil', '2013-02-10', 11, 'DEL', 143.00, 39.00, 'Alevín', NULL, 'Activo', '2025-11-18 15:23:08'),
(11, 'Álvaro', 'Castro', '2012-06-19', 17, 'DEL', 149.00, 42.50, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08'),
(12, 'Raúl', 'Molina', '2012-11-12', 19, 'DEL', 145.00, 40.00, 'Infantil', NULL, 'Activo', '2025-11-18 15:23:08');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `jugadores_partidos`
--

DROP TABLE IF EXISTS `jugadores_partidos`;
CREATE TABLE `jugadores_partidos` (
  `id_jugador` int(11) NOT NULL,
  `id_partido` int(11) NOT NULL,
  `id_estadistica` int(11) NOT NULL COMMENT 'Referencia a las estadísticas de esta participación',
  `convocado` tinyint(1) DEFAULT 1,
  `titular` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='Tabla intermedia N:M - Vincula jugadores con partidos y sus estadísticas';

--
-- Volcado de datos para la tabla `jugadores_partidos`
--

INSERT INTO `jugadores_partidos` (`id_jugador`, `id_partido`, `id_estadistica`, `convocado`, `titular`) VALUES
(1, 1, 1, 1, 1),
(1, 2, 12, 1, 1),
(1, 3, 23, 1, 1),
(2, 1, 2, 1, 1),
(2, 2, 13, 1, 1),
(2, 3, 24, 1, 1),
(3, 1, 3, 1, 1),
(3, 2, 14, 1, 1),
(3, 3, 25, 1, 1),
(4, 1, 4, 1, 1),
(4, 2, 15, 1, 1),
(4, 3, 26, 1, 1),
(5, 1, 5, 1, 1),
(5, 2, 16, 1, 1),
(5, 3, 27, 1, 1),
(6, 1, 6, 1, 1),
(6, 2, 17, 1, 1),
(6, 3, 28, 1, 1),
(7, 1, 7, 1, 1),
(7, 2, 18, 1, 1),
(7, 3, 29, 1, 1),
(8, 1, 8, 1, 1),
(8, 2, 19, 1, 1),
(8, 3, 30, 1, 1),
(9, 1, 9, 1, 1),
(9, 2, 20, 1, 1),
(9, 3, 31, 1, 1),
(10, 1, 10, 1, 1),
(10, 2, 21, 1, 1),
(10, 3, 32, 1, 1),
(11, 1, 11, 1, 1),
(11, 3, 33, 1, 1),
(12, 2, 22, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `partidos`
--

DROP TABLE IF EXISTS `partidos`;
CREATE TABLE `partidos` (
  `id_partido` int(11) NOT NULL,
  `fecha_partido` date NOT NULL,
  `rival` varchar(100) NOT NULL,
  `resultado` varchar(10) NOT NULL COMMENT 'Formato: "X-Y"',
  `local_visitante` enum('LOCAL','VISITANTE') NOT NULL DEFAULT 'LOCAL',
  `observaciones` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='Partidos disputados';

--
-- Volcado de datos para la tabla `partidos`
--

INSERT INTO `partidos` (`id_partido`, `fecha_partido`, `rival`, `resultado`, `local_visitante`, `observaciones`, `created_at`) VALUES
(1, '2024-09-08', 'Real Betis', '3-1', 'LOCAL', 'Victoria', '2025-11-18 15:23:08'),
(2, '2024-09-15', 'Sevilla FC', '2-2', 'VISITANTE', 'Empate', '2025-11-18 15:23:08'),
(3, '2024-09-22', 'Granada CF', '4-0', 'LOCAL', 'Goleada', '2025-11-18 15:23:08');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `configuracion`
--
ALTER TABLE `configuracion`
  ADD PRIMARY KEY (`id_config`);

--
-- Indices de la tabla `estadisticas_partido`
--
ALTER TABLE `estadisticas_partido`
  ADD PRIMARY KEY (`id_estadistica`);

--
-- Indices de la tabla `jugadores`
--
ALTER TABLE `jugadores`
  ADD PRIMARY KEY (`id_jugador`),
  ADD UNIQUE KEY `unique_dorsal` (`dorsal`),
  ADD KEY `idx_posicion` (`posicion`),
  ADD KEY `idx_categoria` (`categoria`);

--
-- Indices de la tabla `jugadores_partidos`
--
ALTER TABLE `jugadores_partidos`
  ADD PRIMARY KEY (`id_jugador`,`id_partido`),
  ADD UNIQUE KEY `unique_estadistica` (`id_estadistica`) COMMENT 'Cada estadística solo puede estar en una participación',
  ADD KEY `idx_jugador` (`id_jugador`),
  ADD KEY `idx_partido` (`id_partido`);

--
-- Indices de la tabla `partidos`
--
ALTER TABLE `partidos`
  ADD PRIMARY KEY (`id_partido`),
  ADD UNIQUE KEY `unique_partido` (`fecha_partido`,`rival`),
  ADD KEY `idx_fecha` (`fecha_partido`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `estadisticas_partido`
--
ALTER TABLE `estadisticas_partido`
  MODIFY `id_estadistica` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `jugadores`
--
ALTER TABLE `jugadores`
  MODIFY `id_jugador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT de la tabla `partidos`
--
ALTER TABLE `partidos`
  MODIFY `id_partido` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `jugadores_partidos`
--
ALTER TABLE `jugadores_partidos`
  ADD CONSTRAINT `jugadores_partidos_ibfk_1` FOREIGN KEY (`id_jugador`) REFERENCES `jugadores` (`id_jugador`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `jugadores_partidos_ibfk_2` FOREIGN KEY (`id_partido`) REFERENCES `partidos` (`id_partido`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `jugadores_partidos_ibfk_3` FOREIGN KEY (`id_estadistica`) REFERENCES `estadisticas_partido` (`id_estadistica`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
