package com.carmotors.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Una orden de servicio tal como la necesitan los reportes. */
public class ServicioReporte {
    private final int id;
    private final String tipo;
    private final String estado;
    private final LocalDateTime fechaRecepcion;

    public ServicioReporte(int id, String tipo, String estado, LocalDateTime fechaRecepcion) {
        this.id = id;
        this.tipo = Objects.requireNonNullElse(tipo, "Sin tipo");
        this.estado = Objects.requireNonNullElse(estado, "Sin estado");
        this.fechaRecepcion = Objects.requireNonNull(fechaRecepcion);
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
}
