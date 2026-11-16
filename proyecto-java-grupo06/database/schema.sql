CREATE DATABASE IF NOT EXISTS carmotors CHARACTER SET utf8mb4;
USE carmotors;

CREATE TABLE IF NOT EXISTS repuesto_tipos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);
CREATE TABLE IF NOT EXISTS servicio_estados (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);
CREATE TABLE IF NOT EXISTS mantenimiento_tipos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);


CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS vehiculos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    placa VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    contacto VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS repuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    repuesto_tipo_id INT,
    proveedor_id INT,
    precio_compra DECIMAL(10, 2),
    precio_venta DECIMAL(10, 2),
    cantidad_stock INT NOT NULL,
    stock_minimo INT NOT NULL,
    FOREIGN KEY (repuesto_tipo_id) REFERENCES repuesto_tipos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
);

CREATE TABLE IF NOT EXISTS servicios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id INT,
    mantenimiento_tipo_id INT,
    fecha_recepcion DATETIME NOT NULL,
    fecha_entrega DATETIME,
    descripcion_problema TEXT,
    costo_mano_obra DECIMAL(10, 2),
    estado_id INT,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (mantenimiento_tipo_id) REFERENCES mantenimiento_tipos(id),
    FOREIGN KEY (estado_id) REFERENCES servicio_estados(id)
);

CREATE TABLE IF NOT EXISTS servicios_repuestos ( -- Tabla de unión para la relación muchos-a-muchos
    servicio_id INT,
    repuesto_id INT,
    cantidad_usada INT NOT NULL,
    precio_unitario_venta DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (servicio_id, repuesto_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (repuesto_id) REFERENCES repuestos(id)
);

CREATE TABLE IF NOT EXISTS facturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    servicio_id INT,
    fecha_emision DATETIME NOT NULL,
    total_repuestos DECIMAL(10, 2),
    total_mano_obra DECIMAL(10, 2),
    impuestos DECIMAL(10, 2),
    total_final DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id)
);
