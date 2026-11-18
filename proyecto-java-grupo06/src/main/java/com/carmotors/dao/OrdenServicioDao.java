package com.carmotors.dao;

import com.carmotors.modelo.OrdenServicio;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** La conexión pertenece a la transacción que abre la capa de servicio. */
public class OrdenServicioDao {
    public int insertar(Connection c, OrdenServicio orden) throws SQLException {
        String sql = "INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, "
                + "descripcion_problema, costo_mano_obra, estado_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, orden.getVehiculoId());
            p.setInt(2, orden.getMantenimientoTipoId());
            p.setObject(3, orden.getFechaRecepcion());
            p.setString(4, orden.getDescripcionProblema());
            p.setBigDecimal(5, orden.getCostoManoObra());
            p.setInt(6, orden.getEstadoId());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No se obtuvo el identificador de la orden.");
                return r.getInt(1);
            }
        }
    }

    public OrdenServicio buscar(Connection c, int id, boolean bloquear) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM servicios WHERE id = ?"
                + (bloquear ? " FOR UPDATE" : ""))) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Orden inexistente: " + id);
                return mapear(r);
            }
        }
    }

    public List<OrdenServicio> listar(Connection c) throws SQLException {
        List<OrdenServicio> resultado = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM servicios ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) resultado.add(mapear(r));
        }
        return List.copyOf(resultado);
    }

    public boolean actualizarEstado(Connection c, int id, int estadoActual, int estadoNuevo,
                                    LocalDateTime fechaEntrega) throws SQLException {
        String sql = "UPDATE servicios SET estado_id = ?, fecha_entrega = COALESCE(?, fecha_entrega) "
                + "WHERE id = ? AND estado_id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, estadoNuevo);
            p.setObject(2, fechaEntrega, Types.TIMESTAMP);
            p.setInt(3, id);
            p.setInt(4, estadoActual);
            return p.executeUpdate() == 1;
        }
    }

    private OrdenServicio mapear(ResultSet r) throws SQLException {
        OrdenServicio orden = new OrdenServicio(r.getInt("vehiculo_id"), r.getInt("mantenimiento_tipo_id"),
                r.getString("descripcion_problema"), r.getBigDecimal("costo_mano_obra"), r.getInt("estado_id"));
        orden.setId(r.getInt("id"));
        orden.setFechaRecepcion(r.getObject("fecha_recepcion", LocalDateTime.class));
        orden.setFechaEntrega(r.getObject("fecha_entrega", LocalDateTime.class));
        return orden;
    }
}
