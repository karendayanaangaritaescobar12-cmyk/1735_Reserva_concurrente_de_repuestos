package com.carmotors.vista;

import com.carmotors.controlador.OrdenServicioControlador;
import java.util.Scanner;

public class MenuPrincipal {
    private final MenuClientes menuClientes;

    private final OrdenServicioControlador ordenes;

    public MenuPrincipal(MenuClientes menuClientes, OrdenServicioControlador ordenes) {
        this.menuClientes = menuClientes;
        this.ordenes = ordenes;
    }

    public void ejecutar() {
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                System.out.println("CarMotors");
                System.out.println("1. Clientes y vehículos");
                System.out.println("2. Órdenes de servicio");
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
                    case "0":
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        }
    }
}