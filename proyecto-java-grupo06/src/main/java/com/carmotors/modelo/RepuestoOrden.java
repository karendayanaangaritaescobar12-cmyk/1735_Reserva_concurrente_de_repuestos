package com.carmotors.modelo;

import java.math.BigDecimal;

public record RepuestoOrden(int repuestoId, int cantidad, BigDecimal precioUnitario) { }
