USE carmotors;

INSERT INTO tecnicos (nombre) VALUES ('Luis Pérez'), ('Ana Torres'), ('Mario Díaz');
SET @luis  = (SELECT id FROM tecnicos WHERE nombre = 'Luis Pérez' ORDER BY id DESC LIMIT 1);
SET @ana   = (SELECT id FROM tecnicos WHERE nombre = 'Ana Torres' ORDER BY id DESC LIMIT 1);
SET @mario = (SELECT id FROM tecnicos WHERE nombre = 'Mario Díaz' ORDER BY id DESC LIMIT 1);

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

SET @pastillas = (SELECT id FROM repuestos WHERE nombre = 'Pastillas de freno' LIMIT 1);
SET @bateria   = (SELECT id FROM repuestos WHERE nombre = 'Batería 12V' LIMIT 1);

-- Órdenes de servicio
INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
VALUES (@v1, @preventivo, '2026-10-05 08:00', '2026-10-06 17:00', 'Reporte: cambio de aceite', 45000, @entregado);
SET @s1 = LAST_INSERT_ID();

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
VALUES (@v2, @correctivo, '2026-10-12 09:30', '2026-10-14 12:00', 'Reporte: cambio de pastillas', 80000, @facturado);
SET @s2 = LAST_INSERT_ID();

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
VALUES (@v3, @correctivo, '2026-10-20 14:00', NULL, 'Reporte: batería descargada', 30000, @completado);
SET @s3 = LAST_INSERT_ID();

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
VALUES (@v1, @preventivo, '2026-11-03 10:00', NULL, 'Reporte: revisión general', 60000, @proceso);
SET @s4 = LAST_INSERT_ID();

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id)
VALUES (@v2, @correctivo, '2026-11-10 16:45', NULL, 'Reporte: ruido en la suspensión', 50000, @pendiente);
SET @s5 = LAST_INSERT_ID();

-- Técnicos asignados
INSERT INTO servicios_tecnicos (servicio_id, tecnico_id) VALUES
    (@s1, @luis), (@s2, @luis), (@s2, @ana), (@s3, @ana), (@s4, @luis), (@s5, @mario);

-- Repuestos usados
INSERT INTO servicios_repuestos (servicio_id, repuesto_id, cantidad_usada, precio_unitario_venta) VALUES
    (@s2, @pastillas, 2, 95000),
    (@s3, @bateria,   1, 380000),
    (@s4, @pastillas, 1, 95000);
