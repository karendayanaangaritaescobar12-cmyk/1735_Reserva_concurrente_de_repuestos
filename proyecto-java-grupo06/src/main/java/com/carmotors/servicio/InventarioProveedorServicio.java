package com.carmotors.servicio;

import com.carmotors.dao.EvaluacionProveedorDao;
import com.carmotors.dao.LoteDao;
import com.carmotors.dao.ProveedorDao;
import com.carmotors.dao.RepuestoDao;
import com.carmotors.modelo.EvaluacionProveedor;
import com.carmotors.modelo.Lote;
import com.carmotors.modelo.Proveedor;
import com.carmotors.modelo.Repuesto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;

/** Servicio inventario/proveedores. Día 2: catálogo, lotes, evaluación. Yeimer */
public class InventarioProveedorServicio {

    private final ProveedorDao proveedores;
    private final RepuestoDao repuestos;
    private final LoteDao lotes;
    private final EvaluacionProveedorDao evaluaciones;

    public InventarioProveedorServicio(ProveedorDao proveedores, RepuestoDao repuestos,
                                       LoteDao lotes, EvaluacionProveedorDao evaluaciones) {
        this.proveedores = proveedores;
        this.repuestos = repuestos;
        this.lotes = lotes;
        this.evaluaciones = evaluaciones;
    }

    public int registrarProveedor(String nombre, String contacto) throws SQLException {
        Proveedor proveedor = new Proveedor(0, obligatorio(nombre, 255, "Nombre del proveedor"),
                opcional(contacto, 255, "Contacto"));
        try {
            return proveedores.insertar(proveedor);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El proveedor ya existe o los datos no son válidos.", e);
        }
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedores.listarTodos();
    }

    public Proveedor obtenerProveedor(int id) throws SQLException {
        return proveedores.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor id=" + id + " no encontrado."));
    }

    public int registrarRepuesto(String nombre, String marca, int tipoId, int proveedorId,
                                 BigDecimal precioCompra, BigDecimal precioVenta,
                                 int stock, int stockMinimo) throws SQLException {
        if (precioCompra == null || precioCompra.signum() < 0)
            throw new IllegalArgumentException("Precio de compra inválido.");
        if (precioVenta == null || precioVenta.signum() < 0)
            throw new IllegalArgumentException("Precio de venta inválido.");
        if (stock < 0 || stockMinimo < 0)
            throw new IllegalArgumentException("Stock y stock mínimo no pueden ser negativos.");
        obtenerProveedor(proveedorId);
        Repuesto repuesto = new Repuesto(0, obligatorio(nombre, 255, "Nombre del repuesto"),
                opcional(marca, 100, "Marca"), tipoId, proveedorId,
                precioCompra, precioVenta, stock, stockMinimo);
        try {
            return repuestos.insertar(repuesto);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("No se pudo registrar el repuesto (revisa tipo y proveedor).", e);
        }
    }

    /** Catálogo por proveedor. */
    public List<Repuesto> catalogoPorProveedor(int proveedorId) throws SQLException {
        obtenerProveedor(proveedorId);
        return repuestos.listarPorProveedor(proveedorId);
    }

    public List<Repuesto> listarRepuestos() throws SQLException {
        return repuestos.listarTodos();
    }

    public Repuesto obtenerRepuesto(int id) throws SQLException {
        return repuestos.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Repuesto id=" + id + " no encontrado."));
    }

    public int registrarLote(String codigoLote, int repuestoId, int proveedorId,
                             LocalDate fechaIngreso, LocalDate fechaCaducidad,
                             int cantidadRecibida) throws SQLException {
        if (cantidadRecibida <= 0)
            throw new IllegalArgumentException("La cantidad recibida debe ser mayor a cero.");
        if (fechaIngreso == null) fechaIngreso = LocalDate.now();
        obtenerRepuesto(repuestoId);
        obtenerProveedor(proveedorId);
        Lote lote = new Lote(obligatorio(codigoLote, 50, "Código de lote"),
                repuestoId, proveedorId, fechaIngreso, fechaCaducidad, cantidadRecibida);
        try {
            int id = lotes.insertar(lote);
            repuestos.sumarStock(repuestoId, cantidadRecibida); // actualiza stock del repuesto
            return id;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El código de lote ya existe o los datos no son válidos.", e);
        }
    }

    public List<Lote> lotesPorRepuesto(int repuestoId) throws SQLException {
        return lotes.listarPorRepuesto(repuestoId);
    }

    public List<Lote> lotesPorProveedor(int proveedorId) throws SQLException {
        return lotes.listarPorProveedor(proveedorId);
    }

    public int registrarEvaluacion(int proveedorId, int calidad, int puntualidad,
                                   String observaciones) throws SQLException {
        if (calidad < 1 || calidad > 5)
            throw new IllegalArgumentException("Calificación de calidad debe estar entre 1 y 5.");
        if (puntualidad < 1 || puntualidad > 5)
            throw new IllegalArgumentException("Calificación de puntualidad debe estar entre 1 y 5.");
        obtenerProveedor(proveedorId);
        EvaluacionProveedor evaluacion = new EvaluacionProveedor(
                proveedorId, LocalDate.now(), calidad, puntualidad,
                opcional(observaciones, 500, "Observaciones"));
        return evaluaciones.insertar(evaluacion);
    }

    public List<EvaluacionProveedor> evaluacionesDe(int proveedorId) throws SQLException {
        obtenerProveedor(proveedorId);
        return evaluaciones.listarPorProveedor(proveedorId);
    }

    private static String obligatorio(String valor, int limite, String campo) {
        String limpio = opcional(valor, limite, campo);
        if (limpio == null) throw new IllegalArgumentException(campo + " es obligatorio.");
        return limpio;
    }

    private static String opcional(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.strip();
        if (limpio.length() > limite)
            throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
        return limpio;
    }
}