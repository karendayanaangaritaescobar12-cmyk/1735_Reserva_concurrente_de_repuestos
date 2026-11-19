package com.carmotors.dao;

import com.carmotors.modelo.RepuestoOrden;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepuestoOrdenDao {
    public void insertar(Connection c, int orden, int repuesto, int cantidad) throws SQLException {
        String sql = "INSERT INTO servicios_repuestos "
                + "(servicio_id, repuesto_id, cantidad_usada, precio_unitario_venta, reserva_pendiente) "
                + "SELECT ?, id, ?, precio_venta, TRUE FROM repuestos WHERE id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, orden);
            p.setInt(2, cantidad);
            p.setInt(3, repuesto);
            if (p.executeUpdate() != 1) throw new SQLException("No se registró el repuesto de la orden.");
        }
    }

    public List<RepuestoOrden> listar(Connection c, int orden) throws SQLException {
        return listar(c, orden, false);
    }

    public List<RepuestoOrden> listarPendientes(Connection c, int orden) throws SQLException {
        return listar(c, orden, true);
    }

    private List<RepuestoOrden> listar(Connection c, int orden, boolean soloPendientes) throws SQLException {
        String sql = "SELECT repuesto_id, cantidad_usada, precio_unitario_venta "
                + "FROM servicios_repuestos WHERE servicio_id = ?"
                + (soloPendientes ? " AND reserva_pendiente = TRUE" : "") + " ORDER BY repuesto_id";
        List<RepuestoOrden> resultado = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, orden);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) resultado.add(new RepuestoOrden(r.getInt(1), r.getInt(2), r.getBigDecimal(3)));
            }
        }
        return List.copyOf(resultado);
    }

    public void confirmarReservas(Connection c, int orden) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE servicios_repuestos "
                + "SET reserva_pendiente = FALSE WHERE servicio_id = ? AND reserva_pendiente = TRUE")) {
            p.setInt(1, orden);
            p.executeUpdate();
        }
    }
}
