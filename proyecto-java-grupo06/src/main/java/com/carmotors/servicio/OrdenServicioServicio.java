package com.carmotors.servicio;

import com.carmotors.dao.*;
import com.carmotors.modelo.EstadoServicio;
import com.carmotors.modelo.OrdenServicio;
import com.carmotors.modelo.RepuestoOrden;
import com.carmotors.modelo.Tecnico;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** Gestiona las órdenes de servicio, sus técnicos y su ciclo de vida. */
public class OrdenServicioServicio {
    private static final List<String> CICLO = List.of(
            "Pendiente", "En Proceso", "Completado", "Facturado", "Entregado");

    @FunctionalInterface
    public interface Conexiones { Connection abrir() throws SQLException; }

    private final Conexiones conexiones;
    private final OrdenServicioDao ordenes = new OrdenServicioDao();
    private final EstadoServicioDao estados = new EstadoServicioDao();
    private final TecnicoOrdenDao tecnicos = new TecnicoOrdenDao();
    private final RepuestoOrdenDao repuestos = new RepuestoOrdenDao();
    private final InventarioOrdenDao inventario = new InventarioOrdenDao();

    public OrdenServicioServicio(Conexiones conexiones) {
        this.conexiones = java.util.Objects.requireNonNull(conexiones);
    }

    public int crear(int vehiculo, int tipo, String nombreServicio, String problema,
                     BigDecimal manoObra) throws SQLException {
        nombreServicio = ValidacionRegistro.nombreServicio(nombreServicio);
        return crearOrden(vehiculo, tipo, nombreServicio.strip(), problema, manoObra);
    }

    public int crear(int vehiculo, int tipo, String problema, BigDecimal manoObra) throws SQLException {
        return crearOrden(vehiculo, tipo, null, problema, manoObra);
    }

    private int crearOrden(int vehiculo, int tipo, String nombreServicio, String problema,
                           BigDecimal manoObra) throws SQLException {
        if (vehiculo <= 0 || tipo <= 0) throw new IllegalArgumentException("Vehículo y tipo deben ser positivos.");
        problema = ValidacionRegistro.textoConLetras(problema, 65535, "Descripción del problema");
        if (manoObra == null || manoObra.signum() < 0 || manoObra.scale() > 2
                || manoObra.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("Mano de obra inválida.");
        try (Connection c = conexiones.abrir()) {
            OrdenServicio orden = new OrdenServicio(vehiculo, tipo, problema.strip(), manoObra,
                    estados.idPorNombre(c, "Pendiente"));
            orden.setNombreServicio(nombreServicio);
            return ordenes.insertar(c, orden);
        }
    }

    public OrdenServicio consultar(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Orden inválida.");
        try (Connection c = conexiones.abrir()) { return ordenes.buscar(c, id, false); }
    }

    public List<OrdenServicio> listar() throws SQLException {
        try (Connection c = conexiones.abrir()) { return ordenes.listar(c); }
    }

    public List<EstadoServicio> listarEstados() throws SQLException {
        try (Connection c = conexiones.abrir()) { return estados.listar(c); }
    }

    public EstadoServicio cambiarEstado(int id, int estadoId) throws SQLException {
        if (id <= 0 || estadoId <= 0) {
            throw new IllegalArgumentException("Orden y estado deben ser positivos.");
        }
        return enTransaccion(c -> {
            OrdenServicio orden = ordenes.buscar(c, id, true);
            EstadoServicio actual = estados.buscar(c, orden.getEstadoId());
            EstadoServicio destino = estados.buscar(c, estadoId);
            int paso = CICLO.indexOf(actual.getNombre());
            if (paso < 0 || paso + 1 == CICLO.size() || !CICLO.get(paso + 1).equals(destino.getNombre())) {
                throw new IllegalArgumentException("Cambio de estado inválido de " + actual.getNombre()
                        + " a " + destino.getNombre() + ".");
            }
            LocalDateTime entrega = "Entregado".equals(destino.getNombre()) ? LocalDateTime.now() : null;
            if ("Completado".equals(destino.getNombre())) {
                for (RepuestoOrden repuesto : repuestos.listarPendientes(c, id)) {
                    inventario.descontarReserva(c, repuesto);
                }
                repuestos.confirmarReservas(c, id);
            }
            if (!ordenes.actualizarEstado(c, id, actual.getId(), destino.getId(), entrega)) {
                throw new IllegalStateException("La orden cambió. Consulta su estado y vuelve a intentarlo.");
            }
            return destino;
        });
    }

    public List<RepuestoOrden> repuestosDe(int orden) throws SQLException {
        if (orden <= 0) throw new IllegalArgumentException("Orden inválida.");
        try (Connection c = conexiones.abrir()) {
            ordenes.buscar(c, orden, false);
            return repuestos.listar(c, orden);
        }
    }

    public void asignarRepuesto(int orden, int repuesto, int cantidad) throws SQLException {
        if (orden <= 0 || repuesto <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException("Orden, repuesto y cantidad deben ser positivos.");
        }
        enTransaccion(c -> {
            OrdenServicio actual = ordenes.buscar(c, orden, true);
            String estado = estados.buscar(c, actual.getEstadoId()).getNombre();
            if (!"Pendiente".equals(estado) && !"En Proceso".equals(estado)) {
                throw new IllegalArgumentException("Solo se pueden asignar repuestos a órdenes pendientes o en proceso.");
            }
            if (repuestos.listar(c, orden).stream().anyMatch(item -> item.repuestoId() == repuesto)) {
                throw new IllegalArgumentException("Ese repuesto ya está asignado a la orden.");
            }
            if (!inventario.reservar(c, repuesto, cantidad)) {
                throw new IllegalArgumentException("Repuesto inexistente o stock disponible insuficiente.");
            }
            repuestos.insertar(c, orden, repuesto, cantidad);
            return null;
        });
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
        enTransaccion(c -> {
            ordenes.buscar(c, orden, true);
            if (!tecnicos.listar(c, orden).contains(tecnico)) tecnicos.asignar(c, orden, tecnico);
            return null;
        });
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar(Connection c) throws SQLException;
    }

    private <T> T enTransaccion(Operacion<T> operacion) throws SQLException {
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    public int registrarTecnico(String nombre) throws SQLException {
        nombre = ValidacionRegistro.nombrePersona(nombre, "Nombre del técnico");
        try (Connection conexion = conexiones.abrir()) {
            return new TecnicoDao().insertar(conexion, nombre.trim());
        }
    }

    public List<Tecnico> listarTecnicos() throws SQLException {
        try (Connection conexion = conexiones.abrir()) {
            return new TecnicoDao().listar(conexion);
        }
    }

    public List<String> serviciosSugeridos() {
        return List.of("Cambio de aceite", "Alineación", "Balanceo", "Revisión de frenos",
                "Cambio de batería", "Revisión del sistema eléctrico");
    }
}
