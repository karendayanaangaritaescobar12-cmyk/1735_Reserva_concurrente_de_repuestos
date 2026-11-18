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

CREATE TABLE repuesto_tipos (
    id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255) NOT NULL, contacto VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE repuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL, marca VARCHAR(100),
    repuesto_tipo_id INT, proveedor_id INT,
    precio_compra DECIMAL(10,2), precio_venta DECIMAL(10,2) NOT NULL,
    cantidad_stock INT NOT NULL, stock_minimo INT NOT NULL,
    cantidad_reservada INT NOT NULL DEFAULT 0,
    CHECK (cantidad_reservada >= 0 AND cantidad_reservada <= cantidad_stock),
    FOREIGN KEY (repuesto_tipo_id) REFERENCES repuesto_tipos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB;

CREATE TABLE servicios_repuestos (
    servicio_id INT NOT NULL, repuesto_id INT NOT NULL,
    cantidad_usada INT NOT NULL CHECK (cantidad_usada > 0),
    precio_unitario_venta DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (servicio_id, repuesto_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (repuesto_id) REFERENCES repuestos(id)
) ENGINE=InnoDB;
