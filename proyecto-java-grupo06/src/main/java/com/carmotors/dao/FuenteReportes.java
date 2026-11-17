package com.carmotors.dao;

import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import java.sql.SQLException;
import java.util.List;

/**
 * Datos crudos para los reportes. Los cálculos se hacen con Streams en ReporteServicio;
 * esta interfaz permite probarlos con datos en memoria, sin MySQL.
 */
public interface FuenteReportes {
    List<ServicioReporte> servicios() throws SQLException;

    List<AsignacionTecnico> asignaciones() throws SQLException;

    List<RepuestoUsado> repuestosUsados() throws SQLException;
}
