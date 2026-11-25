package com.carmotors.vista;

import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.controlador.ReporteControlador;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;

public class MenuPrincipal {
    private final MenuClientes menuClientes;

    private final OrdenServicioControlador ordenes;
    private final ReporteControlador reportes;

    public MenuPrincipal(MenuClientes menuClientes, OrdenServicioControlador ordenes, ReporteControlador reportes) {
        this.menuClientes = menuClientes;
        this.ordenes = ordenes;
        this.reportes = reportes;
    }

    public void ejecutar() {
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                System.out.println("CarMotors");
                System.out.println("1. Clientes y vehículos");
                System.out.println("2. Órdenes de servicio");
                System.out.println("3. Reporte de servicios completados");
                System.out.println("0. Salir");
                System.out.print("Opción: ");
                if (!entrada.hasNextLine()) {
                    return;
                }
                switch (entrada.nextLine().trim()) {
                    case "1":
                        menuClientes.ejecutar(entrada);
                        break;
                    case "2":
                        new MenuOrdenes(ordenes, entrada, System.out).mostrar();
                        break;
                    case "3":
                        mostrarCompletados(entrada);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }

    private void mostrarCompletados(Scanner entrada) {
        System.out.print("Fecha de recepción inicial (AAAA-MM-DD): ");
        if (!entrada.hasNextLine()) return;
        String desde = entrada.nextLine();
        System.out.print("Fecha de recepción final (AAAA-MM-DD): ");
        if (!entrada.hasNextLine()) return;
        String hasta = entrada.nextLine();
        try {
            String reporte = reportes.completados(desde, hasta);
            System.out.print(reporte);
            System.out.print("Ruta del archivo .txt (Enter para volver): ");
            if (!entrada.hasNextLine()) return;
            String ruta = entrada.nextLine().strip();
            if (!ruta.isEmpty()) {
                Path destino = Path.of(ruta);
                reportes.exportarCompletados(reporte, destino);
                System.out.println("Reporte guardado en " + destino.toAbsolutePath());
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("No se pudo consultar el reporte. Revisa la conexión a la base de datos.");
        } catch (IOException e) {
            System.out.println("No se pudo guardar el reporte: " + e.getMessage());
        }
    }
}
