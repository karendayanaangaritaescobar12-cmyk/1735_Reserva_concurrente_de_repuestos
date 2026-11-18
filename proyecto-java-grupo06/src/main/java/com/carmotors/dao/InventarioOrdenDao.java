package com.carmotors.dao;

import com.carmotors.modelo.RepuestoOrden;
import java.sql.*;

/** Movimientos de inventario dentro de la transacción de una orden. */
public class InventarioOrdenDao {
    public boolean reservar(Connection c, int repuesto, int cantidad) throws SQLException {
        String sql = "UPDATE repuestos SET cantidad_reservada = cantidad_reservada + ? "
                + "WHERE id = ? AND cantidad_stock - cantidad_reservada >= ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, cantidad);
            p.setInt(2, repuesto);
            p.setInt(3, cantidad);
            return p.executeUpdate() == 1;
        }
    }

    public void descontarReserva(Connection c, RepuestoOrden repuesto) throws SQLException {
        String sql = "UPDATE repuestos SET cantidad_stock = cantidad_stock - ?, "
                + "cantidad_reservada = cantidad_reservada - ? WHERE id = ? AND cantidad_reservada >= ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, repuesto.cantidad());
            p.setInt(2, repuesto.cantidad());
            p.setInt(3, repuesto.repuestoId());
            p.setInt(4, repuesto.cantidad());
            if (p.executeUpdate() != 1) throw new SQLException("Reserva insuficiente para completar la orden.");
        }
    }
}
