-- Migración de integración: ejecutar UNA VEZ después del schema.sql del equipo.
-- MySQL 8+, tablas InnoDB. No cambia los identificadores de los catálogos existentes.
CREATE TABLE tecnicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE servicios_tecnicos (
    servicio_id INT NOT NULL,
    tecnico_id INT NOT NULL,
    PRIMARY KEY (servicio_id, tecnico_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
) ENGINE=InnoDB;
