-- Tablas necesarias para el módulo de órdenes y sus relaciones.
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL, telefono VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;
CREATE TABLE vehiculos (
    id INT AUTO_INCREMENT PRIMARY KEY, cliente_id INT,
    placa VARCHAR(10) UNIQUE NOT NULL, marca VARCHAR(50), modelo VARCHAR(50),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
) ENGINE=InnoDB;
CREATE TABLE mantenimiento_tipos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL) ENGINE=InnoDB;
CREATE TABLE servicio_estados (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL) ENGINE=InnoDB;
CREATE TABLE servicios (
    id INT AUTO_INCREMENT PRIMARY KEY, vehiculo_id INT, mantenimiento_tipo_id INT,
    fecha_recepcion DATETIME NOT NULL, fecha_entrega DATETIME, descripcion_problema TEXT,
    costo_mano_obra DECIMAL(10,2), estado_id INT,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (mantenimiento_tipo_id) REFERENCES mantenimiento_tipos(id),
    FOREIGN KEY (estado_id) REFERENCES servicio_estados(id)
) ENGINE=InnoDB;

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
