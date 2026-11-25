package com.carmotors.servicio;

import com.carmotors.dao.FuenteReportes;
import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.ConsumoRepuesto;
import com.carmotors.modelo.ProductividadTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reportes gerenciales calculados con Streams y lambdas.
 * Todos reciben un periodo [desde, hasta] (ambos incluidos) sobre la fecha de recepción de la orden.
 */
public class ReporteServicio {
    /** Estados en los que el técnico ya terminó el trabajo. */
    public static final Set<String> ESTADOS_TERMINADOS = Set.of("Completado", "Facturado", "Entregado");

    private final FuenteReportes fuente;

    public ReporteServicio(FuenteReportes fuente) {
        this.fuente = Objects.requireNonNull(fuente);
    }

    // ---------- 1. Servicios solicitados ----------

    public Map<String, Long> serviciosPorTipo(LocalDate desde, LocalDate hasta) throws SQLException {
        return contar(serviciosDelPeriodo(desde, hasta), ServicioReporte::getTipo);
    }

    public Map<String, Long> serviciosPorEstado(LocalDate desde, LocalDate hasta) throws SQLException {
        return contar(serviciosDelPeriodo(desde, hasta), ServicioReporte::getEstado);
    }

    public Map<YearMonth, Long> serviciosPorMes(LocalDate desde, LocalDate hasta) throws SQLException {
        return contar(serviciosDelPeriodo(desde, hasta), s -> YearMonth.from(s.getFechaRecepcion()));
    }

    // ---------- 2. Productividad por técnico ----------

    /** Ordenado de más a menos órdenes terminadas. */
    public List<ProductividadTecnico> productividadPorTecnico(LocalDate desde, LocalDate hasta) throws SQLException {
        Map<Integer, ServicioReporte> servicios = serviciosDelPeriodo(desde, hasta).stream()
                .collect(Collectors.toMap(ServicioReporte::getId, Function.identity()));

        Map<Integer, List<AsignacionTecnico>> porTecnico = fuente.asignaciones().stream()
                .filter(a -> servicios.containsKey(a.getServicioId()))
                .collect(Collectors.groupingBy(AsignacionTecnico::getTecnicoId));

        return porTecnico.values().stream()
                .map(asignaciones -> new ProductividadTecnico(
                        asignaciones.get(0).getTecnicoId(),
                        asignaciones.get(0).getTecnico(),
                        asignaciones.size(),
                        asignaciones.stream()
                                .map(a -> servicios.get(a.getServicioId()))
                                .filter(ReporteServicio::terminado)
                                .count()))
                .sorted(Comparator.comparingLong(ProductividadTecnico::getCompletadas).reversed()
                        .thenComparing(ProductividadTecnico::getTecnico))
                .toList();
    }

    // ---------- 3. Consumo de repuestos ----------

    /** Ordenado de más a menos unidades usadas. */
    public List<ConsumoRepuesto> consumoRepuestos(LocalDate desde, LocalDate hasta) throws SQLException {
        Set<Integer> idsDelPeriodo = serviciosDelPeriodo(desde, hasta).stream()
                .map(ServicioReporte::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<RepuestoUsado>> porRepuesto = fuente.repuestosUsados().stream()
                .filter(u -> idsDelPeriodo.contains(u.getServicioId()))
                .collect(Collectors.groupingBy(RepuestoUsado::getRepuestoId));

        return porRepuesto.values().stream()
                .map(usos -> new ConsumoRepuesto(
                        usos.get(0).getRepuestoId(),
                        usos.get(0).getRepuesto(),
                        usos.stream().mapToLong(RepuestoUsado::getCantidad).sum(),
                        usos.stream().map(RepuestoUsado::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                        usos.stream().map(RepuestoUsado::getServicioId).distinct().count()))
                .sorted(Comparator.comparingLong(ConsumoRepuesto::getUnidades).reversed()
                        .thenComparing(ConsumoRepuesto::getRepuesto))
                .toList();
    }

    // ---------- Auxiliares ----------

    private List<ServicioReporte> serviciosDelPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        if (desde == null || hasta == null) throw new IllegalArgumentException("Indica las fechas del periodo.");
        if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial es posterior a la final.");
        return fuente.servicios(desde, hasta);
    }

    private static <K> Map<K, Long> contar(List<ServicioReporte> servicios, Function<ServicioReporte, K> clave) {
        return servicios.stream().collect(Collectors.groupingBy(clave, TreeMap::new, Collectors.counting()));
    }

    private static boolean terminado(ServicioReporte servicio) {
        return ESTADOS_TERMINADOS.contains(servicio.getEstado());
    }
}
