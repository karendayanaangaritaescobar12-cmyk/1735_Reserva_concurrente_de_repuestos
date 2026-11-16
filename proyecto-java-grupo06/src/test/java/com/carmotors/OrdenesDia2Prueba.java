package com.carmotors;

import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.sql.*;

public class OrdenesDia2Prueba {
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
        System.out.println("OK: 11 validaciones del dia 2 sin BD.");
        if (args.length == 0 || !args[0].equals("--mysql")) return;
        var servicio = new OrdenServicioServicio(() -> DriverManager.getConnection(
                System.getenv("CARMOTORS_DB_URL"), System.getenv("CARMOTORS_DB_USER"), System.getenv("CARMOTORS_DB_PASSWORD")));
        int id = servicio.crear(1, 1, "Cambio de aceite", new BigDecimal("50000.00"));
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
        System.out.println("OK: creacion, consulta y asignacion con MySQL.");
    }

    private static void rechazar(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch (IllegalArgumentException esperado) { return; }
        throw new AssertionError("Se aceptó una entrada inválida");
    }
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
