package com.carmotors.servicio;

import com.carmotors.dao.*;
import com.carmotors.modelo.OrdenServicio;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Creación, consulta y asignación de técnicos a órdenes de servicio. */
public class OrdenServicioServicio {
    @FunctionalInterface
    public interface Conexiones { Connection abrir() throws SQLException; }

    public record AvisoCliente(int orden, String correo, String estado) { }

    @FunctionalInterface
    public interface ObservadorCliente { void notificar(AvisoCliente aviso); }

    private static final System.Logger LOG = System.getLogger(OrdenServicioServicio.class.getName());
    private final List<ObservadorCliente> observadores = new CopyOnWriteArrayList<>();
    private final Conexiones conexiones;
    private final OrdenServicioDao ordenes = new OrdenServicioDao();
    private final EstadoServicioDao estados = new EstadoServicioDao();
    private final TecnicoOrdenDao tecnicos = new TecnicoOrdenDao();

    public OrdenServicioServicio(Conexiones conexiones) {
        this.conexiones = java.util.Objects.requireNonNull(conexiones);
    }

    public void suscribir(ObservadorCliente observador) {
        observadores.add(java.util.Objects.requireNonNull(observador));
    }

    public boolean completar(int id) throws SQLException {
        return avanzarEstado(id, false);
    }

    public boolean entregar(int id) throws SQLException {
        return avanzarEstado(id, true);
    }

    private boolean avanzarEstado(int id, boolean entregar) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Orden inválida.");
        AvisoCliente aviso;
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            try {
                // El bloqueo de la fila cubre la lectura y la escritura entre hilos y procesos.
                OrdenServicio orden = ordenes.buscar(c, id, true);
                int completado = estados.idPorNombre(c, "Completado");
                int facturado = estados.idPorNombre(c, "Facturado");
                int entregado = estados.idPorNombre(c, "Entregado");
                int actual = orden.getEstadoId();
                boolean terminado = actual == completado || actual == facturado || actual == entregado;
                if (entregar ? actual == entregado : terminado) {
                    c.commit();
                    return false;
                }
                if (entregar && !terminado) {
                    throw new IllegalArgumentException("Completa la orden antes de registrar la entrega.");
                }
                LocalDateTime fecha = orden.getFechaEntrega();
                if (entregar && fecha == null) fecha = LocalDateTime.now();
                String correo = ordenes.correoCliente(c, orden.getVehiculoId());
                ordenes.actualizarEstado(c, id, entregar ? entregado : completado, fecha);
                aviso = new AvisoCliente(id, correo, entregar ? "Entregado" : "Completado");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
        if (aviso.correo() != null) {
            for (ObservadorCliente observador : observadores) {
                try {
                    observador.notificar(aviso);
                } catch (RuntimeException e) {
                    LOG.log(System.Logger.Level.WARNING, "Falló el aviso de la orden " + id, e);
                }
            }
        }
        return true;
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
