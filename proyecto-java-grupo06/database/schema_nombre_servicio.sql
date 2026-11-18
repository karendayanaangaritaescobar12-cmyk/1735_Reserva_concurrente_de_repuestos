USE carmotors;

-- Agrega el nombre sin eliminar las órdenes existentes.
SET @existe_nombre = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'servicios'
      AND column_name = 'nombre_servicio'
);
SET @sql_nombre = IF(@existe_nombre = 0,
    'ALTER TABLE servicios ADD COLUMN nombre_servicio VARCHAR(100)',
    'SELECT ''La columna nombre_servicio ya existe'' AS resultado');
PREPARE actualizar_nombre FROM @sql_nombre;
EXECUTE actualizar_nombre;
DEALLOCATE PREPARE actualizar_nombre;