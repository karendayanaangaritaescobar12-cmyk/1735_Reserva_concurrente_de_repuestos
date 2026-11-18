USE carmotors;

CREATE TABLE IF NOT EXISTS tecnicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicios_tecnicos (
    servicio_id INT NOT NULL,
    tecnico_id INT NOT NULL,
    PRIMARY KEY (servicio_id, tecnico_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
) ENGINE=InnoDB;
