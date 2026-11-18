package com.carmotors.vista;

import com.carmotors.controlador.OrdenServicioControlador;
import java.util.Scanner;

public class MenuPrincipal {
    private final MenuClientes menuClientes;

    private final OrdenServicioControlador ordenes;
    private final MenuInventario inventario;
    private final MenuReportes reportes;

    public MenuPrincipal(MenuClientes menuClientes, OrdenServicioControlador ordenes,
                         MenuInventario inventario, MenuReportes reportes) {
        this.menuClientes = menuClientes;
        this.ordenes = ordenes;
        this.inventario = inventario;
        this.reportes = reportes;
    }

    public void ejecutar() {
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                System.out.println("CarMotors");
                System.out.println("1. Clientes y vehículos");
                System.out.println("2. Órdenes de servicio");
                System.out.println("3. Inventario y proveedores");
                System.out.println("4. Consultar datos de factura");
                System.out.println("5. Reportes gerenciales");
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
                        inventario.ejecutar(entrada);
                        break;
                    case "4":
                        reportes.consultarFactura(entrada);
                        break;
                    case "5":
                        reportes.ejecutar(entrada);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }
}