package com.carmotors.servicio;

import com.carmotors.dao.*;
import com.carmotors.modelo.OrdenServicio;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/** Creación, consulta y asignación de técnicos a órdenes de servicio. */
public class OrdenServicioServicio {
    @FunctionalInterface
    public interface Conexiones { Connection abrir() throws SQLException; }

    private final Conexiones conexiones;
    private final OrdenServicioDao ordenes = new OrdenServicioDao();
    private final EstadoServicioDao estados = new EstadoServicioDao();
    private final TecnicoOrdenDao tecnicos = new TecnicoOrdenDao();

    public OrdenServicioServicio(Conexiones conexiones) {
        this.conexiones = java.util.Objects.requireNonNull(conexiones);
    }

    public int crear(int vehiculo, int tipo, String problema, BigDecimal manoObra) throws SQLException {
        if (vehiculo <= 0 || tipo <= 0) throw new IllegalArgumentException("Vehículo y tipo deben ser positivos.");
        if (problema == null || problema.isBlank()) throw new IllegalArgumentException("Describe el problema.");
        if (manoObra == null || manoObra.signum() < 0 || manoObra.scale() > 2
                || manoObra.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("Mano de obra inválida.");
        try (Connection c = conexiones.abrir()) {
            return ordenes.insertar(c, new OrdenServicio(vehiculo, tipo, problema.strip(), manoObra,
                    estados.idPorNombre(c, "Pendiente")));
        }
    }

    public OrdenServicio consultar(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Orden inválida.");
        try (Connection c = conexiones.abrir()) { return ordenes.buscar(c, id, false); }
    }

    public List<OrdenServicio> listar() throws SQLException {
        try (Connection c = conexiones.abrir()) { return ordenes.listar(c); }
    }

    public List<Integer> tecnicosDe(int orden) throws SQLException {
        if (orden <= 0) throw new IllegalArgumentException("Orden inválida.");
        try (Connection c = conexiones.abrir()) {
            ordenes.buscar(c, orden, false);
            return tecnicos.listar(c, orden);
        }
    }

    public void asignarTecnico(int orden, int tecnico) throws SQLException {
        if (orden <= 0 || tecnico <= 0) throw new IllegalArgumentException("Orden y técnico deben ser positivos.");
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            try {
                ordenes.buscar(c, orden, true);
                if (!tecnicos.listar(c, orden).contains(tecnico)) tecnicos.asignar(c, orden, tecnico);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
}
