package com.carmotors.vista;

import java.util.Scanner;

public class MenuPrincipal {
    private final MenuClientes menuClientes;

    public MenuPrincipal(MenuClientes menuClientes) {
        this.menuClientes = menuClientes;
    }

    public void ejecutar() {
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                System.out.println("CarMotors");
                System.out.println("1. Clientes y vehículos");
                System.out.println("0. Salir");
                System.out.print("Opción: ");
                if (!entrada.hasNextLine()) {
                    return;
                }
                switch (entrada.nextLine().trim()) {
                    case "1":
                        menuClientes.ejecutar(entrada);
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