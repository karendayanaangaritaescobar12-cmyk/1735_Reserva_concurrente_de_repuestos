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

INSERT INTO clientes (nombre, documento_identidad, email)
SELECT 'Cliente de prueba Andres', 'TEST-ANDRES', 'andres.prueba@example.com'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE documento_identidad = 'TEST-ANDRES');
INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT id, 'TST001', 'Renault', 'Logan' FROM clientes
WHERE documento_identidad = 'TEST-ANDRES'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'TST001');
INSERT INTO tecnicos (nombre)
SELECT 'Tecnico de prueba Andres'
WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Tecnico de prueba Andres');


-- Datos de prueba de los reportes gerenciales.
-- Las tablas y los datos previos se crean en schema.sql y en las secciones anteriores.
-- Se puede ejecutar varias veces: no duplica técnicos, órdenes, asignaciones ni repuestos usados.
-- Resultados esperados: docs/reportes-resultados-esperados.md

-- Técnicos
INSERT INTO tecnicos (nombre)
SELECT 'Luis Pérez' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Luis Pérez');
INSERT INTO tecnicos (nombre)
SELECT 'Ana Torres' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Ana Torres');
INSERT INTO tecnicos (nombre)
SELECT 'Mario Díaz' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Mario Díaz');

SET @luis  = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Luis Pérez');
SET @ana   = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Ana Torres');
SET @mario = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Mario Díaz');

SET @preventivo = (SELECT id FROM mantenimiento_tipos WHERE nombre = 'Preventivo');
SET @correctivo = (SELECT id FROM mantenimiento_tipos WHERE nombre = 'Correctivo');
SET @pendiente  = (SELECT id FROM servicio_estados WHERE nombre = 'Pendiente');
SET @proceso    = (SELECT id FROM servicio_estados WHERE nombre = 'En Proceso');
SET @completado = (SELECT id FROM servicio_estados WHERE nombre = 'Completado');
SET @facturado  = (SELECT id FROM servicio_estados WHERE nombre = 'Facturado');
SET @entregado  = (SELECT id FROM servicio_estados WHERE nombre = 'Entregado');

SET @v1 = (SELECT id FROM vehiculos WHERE placa = 'ABC123');
SET @v2 = (SELECT id FROM vehiculos WHERE placa = 'jadshfkjdf');
SET @v3 = (SELECT id FROM vehiculos WHERE placa = 'dfmslkfm');

SET @pastillas = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Pastillas de freno');
SET @bateria   = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Batería 12V');

-- Órdenes de servicio (la descripción "Reporte: ..." identifica cada orden de prueba)
INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT @v1, @preventivo, '2026-10-05 08:00', '2026-10-06 17:00', 'Reporte: cambio de aceite', 45000, @entregado
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: cambio de aceite');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT @v2, @correctivo, '2026-10-12 09:30', '2026-10-14 12:00', 'Reporte: cambio de pastillas', 80000, @facturado
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: cambio de pastillas');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT @v3, @correctivo, '2026-10-20 14:00', NULL, 'Reporte: batería descargada', 30000, @completado
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: batería descargada');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT @v1, @preventivo, '2026-11-03 10:00', NULL, 'Reporte: revisión general', 60000, @proceso
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: revisión general');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT @v2, @correctivo, '2026-11-10 16:45', NULL, 'Reporte: ruido en la suspensión', 50000, @pendiente
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: ruido en la suspensión');

SET @s1 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: cambio de aceite');
SET @s2 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: cambio de pastillas');
SET @s3 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: batería descargada');
SET @s4 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: revisión general');
SET @s5 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: ruido en la suspensión');

-- Técnicos asignados (la llave primaria evita duplicados; IGNORE salta los que ya existen)
INSERT IGNORE INTO servicios_tecnicos (servicio_id, tecnico_id) VALUES
    (@s1, @luis), (@s2, @luis), (@s2, @ana), (@s3, @ana), (@s4, @luis), (@s5, @mario);

-- Repuestos usados (misma idea: llave primaria servicio_id + repuesto_id)
INSERT IGNORE INTO servicios_repuestos (servicio_id, repuesto_id, cantidad_usada, precio_unitario_venta) VALUES
    (@s2, @pastillas, 2, 95000),
    (@s3, @bateria,   1, 380000),
    (@s4, @pastillas, 1, 95000);
