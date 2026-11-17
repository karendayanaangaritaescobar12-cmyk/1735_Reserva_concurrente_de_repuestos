USE carmotors;

-- Datos de prueba de los reportes gerenciales.
-- Requiere schema.sql, schema_ordenes.sql y data.sql.
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
