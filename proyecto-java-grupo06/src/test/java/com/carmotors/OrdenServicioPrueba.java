package com.carmotors;

import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.vista.MenuPrincipal;

public class OrdenServicioPrueba {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }

    public static void main(String[] args) throws Exception {
        OrdenServicioServicio sinBD = new OrdenServicioServicio(() -> { throw new AssertionError("No debe abrir BD"); });
        rechazar(() -> sinBD.crear(0, 1, "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 0, "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, " ", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, "Problema", null));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("-1")));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("1.001")));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("100000000")));
        rechazar(() -> sinBD.consultar(0));
        rechazar(() -> sinBD.tecnicosDe(0));
        rechazar(() -> sinBD.asignarTecnico(0, 1));
        rechazar(() -> sinBD.asignarTecnico(1, 0));
        rechazar(() -> sinBD.cambiarEstado(0, 1));
        rechazar(() -> sinBD.cambiarEstado(1, 0));
        rechazar(() -> sinBD.asignarRepuesto(1, 1, 0));
        System.out.println("OK: 14 validaciones sin BD.");
        if (args.length == 0) return;
        boolean h2 = args[0].equals("--h2");
        if (!h2 && !args[0].equals("--mysql")) throw new IllegalArgumentException("Usa --h2 o --mysql");
        String url = h2 ? "jdbc:h2:mem:ordenes;MODE=MySQL;DB_CLOSE_DELAY=-1" : System.getenv("CARMOTORS_DB_URL");
        String usuario = h2 ? "sa" : System.getenv("CARMOTORS_DB_USER");
        String clave = h2 ? "" : System.getenv("CARMOTORS_DB_PASSWORD");
        if (h2) {
            try (Connection c = DriverManager.getConnection(url, usuario, clave);
                 Statement s = c.createStatement()) {
                for (String archivo : new String[]{"schema.sql", "data.sql"}) {
                    String sql = Files.readString(Path.of("database", archivo));
                    for (String sentencia : sql.split(";")) {
                        if (!sentencia.isBlank()) s.execute(sentencia);
                    }
                }
            }
        }
        var servicio = new OrdenServicioServicio(() -> DriverManager.getConnection(url, usuario, clave));
        int id = servicio.crear(1, 1, "Cambio de aceite", new BigDecimal("50000.00"));
        int repuesto;
        try (Connection c = DriverManager.getConnection(url, usuario, clave);
             PreparedStatement p = c.prepareStatement("INSERT INTO repuestos "
                     + "(nombre, precio_venta, cantidad_stock, stock_minimo) VALUES ('Filtro prueba', 25000, 5, 1)",
                     Statement.RETURN_GENERATED_KEYS)) {
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new AssertionError("Falta ID del repuesto de prueba");
                repuesto = r.getInt(1);
            }
        }
        try {
            var orden = servicio.consultar(id);
            comprobar(orden.getVehiculoId() == 1 && orden.getEstadoId() == 1, "Datos de creación incorrectos");
            comprobar(orden.getDescripcionProblema().equals("Cambio de aceite"), "Descripción incorrecta");
            comprobar(orden.getCostoManoObra().compareTo(new BigDecimal("50000.00")) == 0, "Costo incorrecto");
            comprobar(servicio.listar().stream().anyMatch(o -> o.getId() == id), "Falta orden en listado");
            servicio.asignarTecnico(id, 1);
            servicio.asignarTecnico(id, 1);
            comprobar(servicio.tecnicosDe(id).size() == 1, "Técnico duplicado");
            rechazar(() -> servicio.consultar(Integer.MAX_VALUE));
            try {
                servicio.asignarTecnico(id, Integer.MAX_VALUE);
                throw new AssertionError("Se asignó un técnico inexistente");
            } catch (SQLIntegrityConstraintViolationException esperado) { /* FK y rollback */ }
            comprobar(servicio.tecnicosDe(id).size() == 1, "La asignación fallida alteró los técnicos");
            rechazar(() -> servicio.cambiarEstado(id, Integer.MAX_VALUE));
            rechazar(() -> servicio.cambiarEstado(id, 3));
            comprobar(servicio.consultar(id).getEstadoId() == 1, "El salto alteró el estado");
            var buffer = new ByteArrayOutputStream();
            String acciones = "abc\n2\nno-numero\n6\n" + id + "\n" + repuesto + "\n2\n2\n"
                    + id + "\n3\n4\n" + id + "\n1\n5\n" + id + "\n2\n0\n";
            try (var entrada = new Scanner(acciones);
                 var salida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                new MenuPrincipal(new OrdenServicioControlador(servicio), entrada, salida).mostrar();
            }
            String consola = buffer.toString(StandardCharsets.UTF_8);
            comprobar(consola.contains("Selecciona una opción") && consola.contains("Ingresa un número válido"),
                    "La consola no maneja entradas inválidas");
            comprobar(consola.contains("Cambio de aceite") && consola.contains("Técnicos: [1]")
                    && consola.contains("Técnico asignado.") && consola.contains("Nuevo estado: En Proceso"),
                    "Falló el flujo MVC de consulta, asignación o cambio de estado");
            comprobar(consola.contains("Repuesto reservado.") && consola.contains("Repuesto " + repuesto),
                    "Falló la reserva o consulta de repuestos por consola");
            comprobarStock(url, usuario, clave, repuesto, 5, 2);
            comprobar(servicio.repuestosDe(id).get(0).precioUnitario().compareTo(new BigDecimal("25000")) == 0,
                    "No se guardó el precio unitario");
            rechazar(() -> servicio.asignarRepuesto(id, repuesto, 1));
            int otraOrden = servicio.crear(1, 1, "Prueba de disponibilidad", BigDecimal.ZERO);
            try {
                rechazar(() -> servicio.asignarRepuesto(otraOrden, repuesto, 4));
                comprobar(servicio.repuestosDe(otraOrden).isEmpty(), "La reserva fallida dejó una asignación");
            } finally {
                try (Connection c = DriverManager.getConnection(url, usuario, clave)) {
                    eliminar(c, "servicios_repuestos", "servicio_id", otraOrden);
                    eliminar(c, "servicios", "id", otraOrden);
                }
            }
            comprobar(servicio.tecnicosDe(id).size() == 1, "La consola duplicó la asignación");
            comprobar(servicio.consultar(id).getEstadoId() == 2, "La consola no persistió el estado");
            rechazar(() -> servicio.cambiarEstado(id, 1));
            rechazar(() -> servicio.cambiarEstado(id, 2));
            ajustarReserva(url, usuario, clave, repuesto, 0);
            try {
                servicio.cambiarEstado(id, 3);
                throw new AssertionError("Se completó la orden con la reserva inconsistente");
            } catch (SQLException esperado) {
                comprobar(servicio.consultar(id).getEstadoId() == 2, "El descuento fallido cambió el estado");
                comprobarStock(url, usuario, clave, repuesto, 5, 0);
            }
            ajustarReserva(url, usuario, clave, repuesto, 2);
            for (int estado : new int[]{3, 4, 5}) {
                servicio.cambiarEstado(id, estado);
                var actualizada = servicio.consultar(id);
                comprobar(actualizada.getEstadoId() == estado, "No se persistió el estado " + estado);
                comprobar((actualizada.getFechaEntrega() != null) == (estado == 5), "Fecha de entrega incorrecta");
                comprobarStock(url, usuario, clave, repuesto, 3, 0);
                if (estado == 3) rechazar(() -> servicio.cambiarEstado(id, 3));
            }
            rechazar(() -> servicio.asignarRepuesto(id, repuesto, 1));
            rechazar(() -> servicio.cambiarEstado(id, 4));
            System.out.println("OK: creación, consulta, asignaciones, reserva, descuento único y ciclo de estados con "
                    + (h2 ? "H2 (modo MySQL)." : "MySQL."));
        } finally {
            try (Connection c = DriverManager.getConnection(url, usuario, clave)) {
                eliminar(c, "servicios_repuestos", "servicio_id", id);
                eliminar(c, "servicios_tecnicos", "servicio_id", id);
                eliminar(c, "servicios", "id", id);
                eliminar(c, "repuestos", "id", repuesto);
            }
        }
    }

    private static void eliminar(Connection c, String tabla, String columna, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("DELETE FROM " + tabla + " WHERE " + columna + " = ?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    private static void comprobarStock(String url, String usuario, String clave, int repuesto, int stock, int reservado)
            throws SQLException {
        try (Connection c = DriverManager.getConnection(url, usuario, clave);
             PreparedStatement p = c.prepareStatement(
                     "SELECT cantidad_stock, cantidad_reservada FROM repuestos WHERE id = ?")) {
            p.setInt(1, repuesto);
            try (ResultSet r = p.executeQuery()) {
                comprobar(r.next() && r.getInt(1) == stock && r.getInt(2) == reservado,
                        "Stock físico o reservado incorrecto");
            }
        }
    }

    private static void ajustarReserva(String url, String usuario, String clave, int repuesto, int cantidad)
            throws SQLException {
        try (Connection c = DriverManager.getConnection(url, usuario, clave);
             PreparedStatement p = c.prepareStatement("UPDATE repuestos SET cantidad_reservada = ? WHERE id = ?")) {
            p.setInt(1, cantidad);
            p.setInt(2, repuesto);
            comprobar(p.executeUpdate() == 1, "No se pudo ajustar la reserva de prueba");
        }
    }

    private static void rechazar(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch (IllegalArgumentException esperado) { return; }
        throw new AssertionError("Se aceptó una entrada inválida");
    }
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
