-- Crear tabla partidos
CREATE TABLE IF NOT EXISTS partidos (
    id_partido INT AUTO_INCREMENT PRIMARY KEY,
    fecha_partido DATE NOT NULL,
    rival VARCHAR(100) NOT NULL,
    resultado VARCHAR(10),
    local_visitante ENUM('Local', 'Visitante') DEFAULT 'Local',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Modificar tabla estadisticas_partido para añadir FK a partidos
-- Primero verificar si la columna id_partido ya existe
ALTER TABLE estadisticas_partido
ADD COLUMN id_partido INT AFTER id_jugador;

-- Añadir la clave foránea
ALTER TABLE estadisticas_partido
ADD CONSTRAINT fk_estadisticas_partido
FOREIGN KEY (id_partido) REFERENCES partidos(id_partido) ON DELETE CASCADE;

-- Eliminar columnas que ahora están en la tabla partidos
ALTER TABLE estadisticas_partido
DROP COLUMN fecha_partido,
DROP COLUMN rival,
DROP COLUMN resultado;
