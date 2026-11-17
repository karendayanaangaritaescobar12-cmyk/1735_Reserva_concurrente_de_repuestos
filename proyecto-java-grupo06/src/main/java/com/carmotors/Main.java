package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.servicio.OrdenServicioServicio;
import com.carmotors.vista.MenuPrincipal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var servicio = new OrdenServicioServicio(ConexionFactory.getInstancia()::abrir);
        try (Scanner entrada = new Scanner(System.in)) {
            new MenuPrincipal(new OrdenServicioControlador(servicio), entrada, System.out).mostrar();
        }
    }
}
