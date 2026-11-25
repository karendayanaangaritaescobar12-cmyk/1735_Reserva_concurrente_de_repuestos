package com.carmotors.modelo;

import java.time.LocalDateTime;

public record ServicioCompletado(int id, String placa, String tipo, String estado,
                                LocalDateTime recepcion, LocalDateTime entrega, String problema) {
    @Override
    public String toString() {
        return "Orden %d | Placa %s | Tipo %s | %s | Recepción %s | Entrega %s | %s"
                .formatted(id, placa, tipo, estado, recepcion,
                        entrega == null ? "Pendiente de registro" : entrega, problema);
    }
}
