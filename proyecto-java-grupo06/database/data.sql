-- Datos mínimos de demostración del módulo de órdenes.
INSERT INTO servicio_estados (id, nombre) VALUES
    (1, 'Pendiente'), (2, 'En Proceso'), (3, 'Completado'), (4, 'Facturado'), (5, 'Entregado');
INSERT INTO mantenimiento_tipos (id, nombre) VALUES (1, 'Preventivo'), (2, 'Correctivo');
INSERT INTO clientes (id, nombre, documento_identidad, email) VALUES
    (1, 'Cliente de prueba Andres', 'TEST-ANDRES', 'andres.prueba@example.com');
INSERT INTO vehiculos (id, cliente_id, placa, marca, modelo) VALUES (1, 1, 'TST001', 'Renault', 'Logan');
INSERT INTO tecnicos (id, nombre) VALUES (1, 'Tecnico de prueba Andres');
INSERT INTO repuestos (id, nombre, precio_venta, cantidad_stock, stock_minimo) VALUES
    (1, 'Filtro de aceite de prueba', 25000.00, 5, 1),
    (2, 'Bujia de prueba', 18000.00, 3, 1);
