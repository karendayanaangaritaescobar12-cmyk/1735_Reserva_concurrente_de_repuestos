USE carmotors;

-- Catálogos y datos de prueba
INSERT IGNORE INTO repuesto_tipos (nombre) VALUES ('Mecánico'), ('Eléctrico');
INSERT IGNORE INTO servicio_estados (nombre)
VALUES ('Pendiente'), ('En Proceso'), ('Completado'), ('Facturado'), ('Entregado');
INSERT IGNORE INTO mantenimiento_tipos (nombre) VALUES ('Preventivo'), ('Correctivo');

INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'Cliente de prueba', 'DEMO01', NULL, 'cliente@example.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = 'DEMO01' OR email = 'cliente@example.com');

INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'ABC123', 'Marca de prueba', 'Modelo de prueba'
FROM clientes c
WHERE c.documento_identidad = 'DEMO01'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'ABC123');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT v.id, t.id, CURRENT_DATE - INTERVAL 8 MONTH, CURRENT_DATE - INTERVAL 7 MONTH,
       'Cambio de aceite de prueba', 45000, e.id
FROM vehiculos v
JOIN mantenimiento_tipos t ON t.nombre = 'Preventivo'
JOIN servicio_estados e ON e.nombre = 'Entregado'
WHERE v.placa = 'ABC123'
  AND NOT EXISTS (SELECT 1 FROM servicios s
                  WHERE s.vehiculo_id = v.id AND s.descripcion_problema = 'Cambio de aceite de prueba');

-- Clientes
INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'Carlos Gómez', '1090111222', '3001234567', 'carlos@correo.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = '1090111222' OR email = 'carlos@correo.com');

INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'sebas rosales', '1092942229', '3001214567', 'sebasrosales@correo.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = '1092942229' OR email = 'sebasrosales@correo.com');

-- Vehículos
INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'jadshfkjdf', 'bugati', '1'
FROM clientes c
WHERE c.email = 'carlos@correo.com'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'jadshfkjdf');

INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'dfmslkfm', 'ferrari', '3'
FROM clientes c
WHERE c.email = 'sebasrosales@correo.com'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'dfmslkfm');

-- Proveedores y repuestos
INSERT INTO proveedores (nombre, contacto)
SELECT 'Repuestos El Motor', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'Repuestos El Motor');

INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Pastillas de freno', t.id, p.id, 60000, 95000, 10, 3
FROM repuesto_tipos t
JOIN proveedores p ON p.nombre = 'Repuestos El Motor'
WHERE t.nombre = 'Mecánico'
  AND NOT EXISTS (SELECT 1 FROM repuestos r WHERE r.nombre = 'Pastillas de freno' AND r.proveedor_id = p.id);

INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Batería 12V', t.id, p.id, 280000, 380000, 6, 2
FROM repuesto_tipos t
JOIN proveedores p ON p.nombre = 'Repuestos El Motor'
WHERE t.nombre = 'Eléctrico'
  AND NOT EXISTS (SELECT 1 FROM repuestos r WHERE r.nombre = 'Batería 12V' AND r.proveedor_id = p.id);

