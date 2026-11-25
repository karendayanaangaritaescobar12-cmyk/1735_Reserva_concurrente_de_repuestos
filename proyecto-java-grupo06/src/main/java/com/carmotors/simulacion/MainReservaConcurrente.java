package com.carmotors.simulacion;

import com.carmotors.config.ConexionFactory;
import com.carmotors.dao.RepuestoOrdenDao;
import com.carmotors.servicio.OrdenServicioServicio;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Demostración independiente. Ejecutar exclusivamente en una base de pruebas. */
public final class MainReservaConcurrente {
    private static final ConexionFactory DB = ConexionFactory.getInstancia();

    public static void main(String[] args) throws Exception {
        int repuesto, ordenA, ordenB;
        String nombre = "Filtro de Aceite XY-123 SIM " + UUID.randomUUID();
        // Crear registros exclusivos de esta ejecución, sin alterar stock existente.
        try (Connection c = DB.abrir()) {
            c.setAutoCommit(false);
            try {
                repuesto = insertar(c, "INSERT INTO repuestos (nombre, precio_compra, precio_venta, cantidad_stock, stock_minimo) VALUES (?, 10000, 15000, 1, 0)", nombre);
                ordenA = crearOrden(c, "Simulacion tecnico A");
                ordenB = crearOrden(c, "Simulacion tecnico B");
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
        System.out.println("Iniciando simulación: " + nombre);
        int inicial = disponible(repuesto);
        System.out.println("Disponibilidad inicial: " + inicial);
        if (inicial != 1) throw new IllegalStateException("Se requiere exactamente una unidad disponible.");
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger(), rechazos = new AtomicInteger();
        Thread a = tecnico("Hilo-Técnico-A", ordenA, repuesto, listos, salida, exitos, rechazos);
        Thread b = tecnico("Hilo-Técnico-B", ordenB, repuesto, listos, salida, exitos, rechazos);
        a.start(); b.start();
        listos.await();
        salida.countDown();
        a.join(); b.join();
        int finalDisponible = disponible(repuesto);
        System.out.println("Disponibilidad final del repuesto: " + finalDisponible);
        System.out.printf("Reservas exitosas: %d | Rechazadas: %d%n", exitos.get(), rechazos.get());
        int reservasA = new OrdenServicioServicio(DB::abrir).repuestosDe(ordenA).size();
        int reservasB = new OrdenServicioServicio(DB::abrir).repuestosDe(ordenB).size();
        System.out.printf("Reservas persistidas: orden %d = %d, orden %d = %d%n", ordenA, reservasA, ordenB, reservasB);
        if (exitos.get() != 1 || rechazos.get() != 1 || finalDisponible != 0
                || reservasA + reservasB != 1) {
            throw new IllegalStateException("La simulación no cumplió el resultado esperado.");
        }
        System.out.println("Simulación superada.");
    }

    private static Thread tecnico(String nombre, int orden, int repuesto, CountDownLatch listos,
                                  CountDownLatch salida, AtomicInteger exitos, AtomicInteger rechazos) {
        return new Thread(() -> {
            System.out.println(nombre + ": Intentando reservar para orden " + orden);
            listos.countDown();
            try {
                salida.await();
                // Instancias distintas: el bloqueo Java debe ser compartido por ambas.
                new OrdenServicioServicio(DB::abrir).agregarRepuesto(orden, repuesto, 1);
                exitos.incrementAndGet();
                System.out.println(nombre + ": ¡Reserva exitosa! Orden " + orden + ".");
            } catch (IllegalArgumentException e) {
                if (e.getMessage().startsWith("Stock insuficiente")) {
                    rechazos.incrementAndGet();
                    System.out.println(nombre + ": Reserva rechazada. " + e.getMessage());
                } else {
                    System.err.println(nombre + ": Error inesperado: " + e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SQLException e) {
                System.err.println(nombre + ": Error de base de datos: " + e);
            }
        }, nombre);
    }

    private static int crearOrden(Connection c, String nombre) throws SQLException {
        return insertar(c, "INSERT INTO servicios (nombre_servicio, fecha_recepcion, costo_mano_obra, estado_id) VALUES (?, NOW(), 0, (SELECT id FROM servicio_estados WHERE nombre = 'Pendiente'))", nombre);
    }

    private static int insertar(Connection c, String sql, String nombre) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, nombre);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No se obtuvo ID de prueba");
                return r.getInt(1);
            }
        }
    }

    private static int disponible(int repuesto) throws SQLException {
        RepuestoOrdenDao dao = new RepuestoOrdenDao();
        try (Connection c = DB.abrir()) {
            c.setAutoCommit(false);
            int cantidad = dao.bloquear(c, repuesto).stock() - dao.totalReservado(c, repuesto);
            c.commit();
            return cantidad;
        }
    }
}
