package com.carmotors.vista;

import com.carmotors.controlador.InventarioControlador;
import com.carmotors.modelo.EvaluacionProveedor;
import com.carmotors.modelo.Lote;
import com.carmotors.modelo.Proveedor;
import com.carmotors.modelo.Repuesto;
import com.carmotors.modelo.ReservaRepuesto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MenuInventario {

    private final InventarioControlador controlador;

    public MenuInventario(InventarioControlador controlador) {
        this.controlador = controlador;
    }

    public void ejecutar(Scanner entrada) {
        while (true) {
            System.out.println();
            System.out.println("=== Inventario y Proveedores ===");
            System.out.println("1. Registrar proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Registrar repuesto");
            System.out.println("4. Listar repuestos");
            System.out.println("5. Catálogo por proveedor");
            System.out.println("6. Registrar lote");
            System.out.println("7. Lotes por repuesto");
            System.out.println("8. Registrar evaluación de proveedor");
            System.out.println("9. Ver evaluaciones de un proveedor");
            System.out.println("10. Alertas (stock bajo / caducidad)");
            System.out.println("11. Reservar repuesto (prueba)");
            System.out.println("12. Descontar stock de un servicio (prueba)");
            System.out.println("13. Ver reservas de un servicio");
            System.out.println("0. Volver");
            System.out.print("Opción: ");

            if (!entrada.hasNextLine()) {
                return;
            }
            String opcion = entrada.nextLine().trim();

            try {
                switch (opcion) {
                    case "1":
                        registrarProveedor(entrada);
                        break;
                    case "2":
                        listarProveedores();
                        break;
                    case "3":
                        registrarRepuesto(entrada);
                        break;
                    case "4":
                        listarRepuestos();
                        break;
                    case "5":
                        catalogo(entrada);
                        break;
                    case "6":
                        registrarLote(entrada);
                        break;
                    case "7":
                        lotesPorRepuesto(entrada);
                        break;
                    case "8":
                        registrarEvaluacion(entrada);
                        break;
                    case "9":
                        verEvaluaciones(entrada);
                        break;
                    case "10":
                        mostrarAlertas();
                        break;
                    case "11":
                        reservar(entrada);
                        break;
                    case "12":
                        descontar(entrada);
                        break;
                    case "13":
                        verReservas(entrada);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        }
    }

    private void registrarProveedor(Scanner entrada) throws SQLException {
        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();
        System.out.print("Contacto: ");
        String contacto = entrada.nextLine();
        int id = controlador.registrarProveedor(nombre, contacto);
        System.out.println("Proveedor registrado con id=" + id);
    }

    private void listarProveedores() throws SQLException {
        List<Proveedor> lista = controlador.listarProveedores();
        if (lista.isEmpty()) {
            System.out.println("No hay proveedores.");
            return;
        }
        for (Proveedor p : lista) {
            System.out.println(p);
        }
    }

    private void registrarRepuesto(Scanner entrada) throws SQLException {
        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();
        System.out.print("Marca: ");
        String marca = entrada.nextLine();
        System.out.print("Id tipo de repuesto: ");
        int tipoId = leerEntero(entrada);
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Precio compra: ");
        BigDecimal compra = leerDecimal(entrada);
        System.out.print("Precio venta: ");
        BigDecimal venta = leerDecimal(entrada);
        System.out.print("Stock inicial: ");
        int stock = leerEntero(entrada);
        System.out.print("Stock mínimo: ");
        int minimo = leerEntero(entrada);

        int id = controlador.registrarRepuesto(nombre, marca, tipoId, proveedorId, compra, venta, stock, minimo);
        System.out.println("Repuesto registrado con id=" + id);
    }

    private void listarRepuestos() throws SQLException {
        List<Repuesto> lista = controlador.listarRepuestos();
        if (lista.isEmpty()) {
            System.out.println("No hay repuestos.");
            return;
        }
        for (Repuesto r : lista) {
            System.out.println(r);
        }
    }

    private void catalogo(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        List<Repuesto> lista = controlador.catalogo(proveedorId);
        if (lista.isEmpty()) {
            System.out.println("Ese proveedor no tiene repuestos en catálogo.");
            return;
        }
        System.out.println("Catálogo del proveedor " + proveedorId + ":");
        for (Repuesto r : lista) {
            System.out.println(r);
        }
    }

    private void registrarLote(Scanner entrada) throws SQLException {
        System.out.print("Código de lote: ");
        String codigo = entrada.nextLine();
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Fecha ingreso (YYYY-MM-DD, vacío = hoy): ");
        LocalDate ingreso = leerFechaOpcional(entrada);
        System.out.print("Fecha caducidad (YYYY-MM-DD, vacío = sin caducidad): ");
        LocalDate caducidad = leerFechaOpcional(entrada);
        System.out.print("Cantidad recibida: ");
        int cantidad = leerEntero(entrada);

        int id = controlador.registrarLote(codigo, repuestoId, proveedorId, ingreso, caducidad, cantidad);
        System.out.println("Lote registrado con id=" + id + " (stock del repuesto actualizado).");
    }

    private void lotesPorRepuesto(Scanner entrada) throws SQLException {
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);

        List<Lote> lista = controlador.lotesPorRepuesto(repuestoId);
        if (lista.isEmpty()) {
            System.out.println("No hay lotes para ese repuesto.");
            return;
        }
        for (Lote l : lista) {
            System.out.println(l);
        }
    }

    private void registrarEvaluacion(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Calidad (1-5): ");
        int calidad = leerEntero(entrada);
        System.out.print("Puntualidad (1-5): ");
        int puntualidad = leerEntero(entrada);
        System.out.print("Observaciones: ");
        String obs = entrada.nextLine();
        int id = controlador.registrarEvaluacion(proveedorId, calidad, puntualidad, obs);
        System.out.println("Evaluación registrada con id=" + id);
    }

    private void verEvaluaciones(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        List<EvaluacionProveedor> lista = controlador.evaluaciones(proveedorId);
        if (lista.isEmpty()) {
            System.out.println("Sin evaluaciones.");
            return;
        }
        for (EvaluacionProveedor e : lista) {
            System.out.println(e);
        }
    }

    private void mostrarAlertas() throws SQLException {
        List<String> lineas = controlador.alertas();
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }

    private void reservar(Scanner entrada) throws SQLException {
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);
        System.out.print("Cantidad: ");
        int cantidad = leerEntero(entrada);
        System.out.print("Id servicio (prueba, ej. 100): ");
        int servicioId = leerEntero(entrada);
        ReservaRepuesto r = controlador.reservar(repuestoId, cantidad, servicioId);
        System.out.println("Reserva creada: " + r);
    }

    private void descontar(Scanner entrada) throws SQLException {
        System.out.print("Id servicio: ");
        int servicioId = leerEntero(entrada);
        controlador.descontarStock(servicioId);
        System.out.println("Stock descontado para el servicio " + servicioId);
    }

    private void verReservas(Scanner entrada) throws SQLException {
        System.out.print("Id servicio: ");
        int servicioId = leerEntero(entrada);
        List<ReservaRepuesto> lista = controlador.reservasDeServicio(servicioId);
        if (lista.isEmpty()) {
            System.out.println("No hay reservas para ese servicio.");
            return;
        }
        for (ReservaRepuesto r : lista) {
            System.out.println(r);
        }
    }

    private int leerEntero(Scanner entrada) {
        String texto = entrada.nextLine().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debes ingresar un número entero válido.");
        }
    }

    private BigDecimal leerDecimal(Scanner entrada) {
        String texto = entrada.nextLine().trim().replace(',', '.');
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debes ingresar un número decimal válido.");
        }
    }

    private LocalDate leerFechaOpcional(Scanner entrada) {
        String texto = entrada.nextLine().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida. Usa formato YYYY-MM-DD.");
        }
    }
}