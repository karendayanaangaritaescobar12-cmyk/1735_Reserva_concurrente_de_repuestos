package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.RepuestoUsado;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Busca en MySQL los datos que necesita la factura de una orden. Día 3 - Keiler. */
public class FacturaDao {

    /** Devuelve los datos de la orden, o null si la orden no existe. */
    public DatosFactura buscar(int servicioId) throws SQLException {
        // 1. Datos de la orden, el cliente y el vehículo
        String sqlOrden = "SELECT e.nombre AS estado, c.nombre AS cliente, c.documento_identidad, "
                + "v.placa, s.costo_mano_obra "
                + "FROM servicios s "
                + "LEFT JOIN servicio_estados e ON e.id = s.estado_id "
                + "LEFT JOIN vehiculos v ON v.id = s.vehiculo_id "
                + "LEFT JOIN clientes c ON c.id = v.cliente_id "
                + "WHERE s.id = ?";

        // 2. Repuestos que se usaron en esa orden
        String sqlRepuestos = "SELECT r.id, r.nombre, sr.cantidad_usada, sr.precio_unitario_venta "
                + "FROM servicios_repuestos sr "
                + "JOIN repuestos r ON r.id = sr.repuesto_id "
                + "WHERE sr.servicio_id = ? "
                + "ORDER BY r.nombre";

        try (Connection conexion = ConexionFactory.getInstancia().abrir()) {
            String estado, cliente, documento, placa;
            BigDecimal manoObra;

            try (PreparedStatement ps = conexion.prepareStatement(sqlOrden)) {
                ps.setInt(1, servicioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null; // la orden no existe
                    }
                    estado = rs.getString("estado");
                    cliente = rs.getString("cliente");
                    documento = rs.getString("documento_identidad");
                    placa = rs.getString("placa");
                    manoObra = rs.getBigDecimal("costo_mano_obra");
                    if (manoObra == null) {
                        manoObra = BigDecimal.ZERO;
                    }
                }
            }

            List<RepuestoUsado> repuestos = new ArrayList<>();
            try (PreparedStatement ps = conexion.prepareStatement(sqlRepuestos)) {
                ps.setInt(1, servicioId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        repuestos.add(new RepuestoUsado(servicioId, rs.getInt("id"), rs.getString("nombre"),
                                rs.getInt("cantidad_usada"), rs.getBigDecimal("precio_unitario_venta")));
                    }
                }
            }

            return new DatosFactura(servicioId, estado, cliente, documento, placa, manoObra, repuestos);
        }
    }
}
