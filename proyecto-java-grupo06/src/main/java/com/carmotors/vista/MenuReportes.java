package com.carmotors.vista;

import com.carmotors.controlador.ReporteControlador;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.RepuestoUsado;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuReportes {
    private final ReporteControlador reportes;

    public MenuReportes(ReporteControlador reportes) {
        this.reportes = reportes;
    }

    public void ejecutar(Scanner entrada) {
        while (true) {
            System.out.println("Reportes gerenciales");
            System.out.println("1. Servicios más solicitados");
            System.out.println("2. Productividad por técnico");
            System.out.println("3. Consumo de repuestos");
            System.out.println("0. Volver");
            try {
                String opcion = leer(entrada, "Opción");
                if (opcion.equals("0")) {
                    return;
                }
                if (!opcion.equals("1") && !opcion.equals("2") && !opcion.equals("3")) {
                    System.out.println("Opción inválida.");
                    continue;
                }
                String desde = leer(entrada, "Desde (AAAA-MM-DD)");
                String hasta = leer(entrada, "Hasta (AAAA-MM-DD)");
                switch (opcion) {
                    case "1":
                        mostrar(reportes.serviciosPorTipo(desde, hasta).entrySet());
                        break;
                    case "2":
                        mostrar(reportes.productividadPorTecnico(desde, hasta));
                        break;
                    case "3":
                        mostrar(reportes.consumoRepuestos(desde, hasta));
                        break;
                }
            } catch (NoSuchElementException e) {
                return;
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de MySQL. Código: " + e.getErrorCode());
            }
        }
    }

    public void consultarFactura(Scanner entrada) {
        try {
            String id = leer(entrada, "ID de la orden (0 para volver)");
            if (id.equals("0")) {
                return;
            }
            DatosFactura factura = reportes.datosFactura(Integer.parseInt(id));
            System.out.println(factura);
            System.out.println("Documento: " + factura.getDocumento());
            for (RepuestoUsado repuesto : factura.getRepuestos()) {
                System.out.printf("%s | %d × %s = %s%n", repuesto.getRepuesto(), repuesto.getCantidad(),
                        repuesto.getPrecioUnitario(), repuesto.getSubtotal());
            }
        } catch (NoSuchElementException e) {
            return;
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un ID válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de MySQL. Código: " + e.getErrorCode());
        }
    }

    private void mostrar(Iterable<?> resultados) {
        if (!resultados.iterator().hasNext()) {
            System.out.println("No hay resultados para ese período.");
        }
        for (Object resultado : resultados) {
            System.out.println(resultado);
        }
    }

    private String leer(Scanner entrada, String campo) {
        System.out.print(campo + ": ");
        return entrada.nextLine().trim();
    }
}
