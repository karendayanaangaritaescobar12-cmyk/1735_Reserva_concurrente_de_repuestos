package com.carmotors.vista;

import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.modelo.OrdenServicio;
import com.carmotors.modelo.Tecnico;
import java.util.List;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Scanner;

/** Consola del módulo de órdenes de servicio. */
public class MenuOrdenes {
    private final OrdenServicioControlador controlador;
    private final Scanner entrada;
    private final PrintStream salida;

    public MenuOrdenes(OrdenServicioControlador controlador, Scanner entrada, PrintStream salida) {
        this.controlador = controlador;
        this.entrada = entrada;
        this.salida = salida;
    }

    public void mostrar() {
        while (true) {
            salida.println("CarMotors - Órdenes de servicio");
            salida.println("1. Crear  2. Consultar  3. Listar  4. Asignar técnico  5. Registrar técnico  6. Listar técnicos  0. Volver");
            if (!entrada.hasNextLine()) return;
            String opcion = entrada.nextLine().strip();
            if (opcion.equals("0")) return;
            try {
                switch (opcion) {
                    case "1" -> {
                        int vehiculo = entero("ID del vehículo: ");
                        int tipo = entero("ID del tipo de mantenimiento: ");
                        String problema = leer("Descripción del problema: ");
                        BigDecimal costo = new BigDecimal(leer("Costo de mano de obra (ej. 50000.00): "));
                        salida.println("Orden creada: " + controlador.crear(vehiculo, tipo, problema, costo));
                    }
                    case "2" -> {
                        int id = entero("ID de la orden: ");
                        imprimir(controlador.consultar(id));
                        salida.println("Técnicos: " + controlador.tecnicosDe(id));
                    }
                    case "3" -> {
                        var ordenes = controlador.listar();
                        if (ordenes.isEmpty()) salida.println("No hay órdenes registradas.");
                        ordenes.forEach(this::imprimir);
                    }
                    case "4" -> {
                        controlador.asignarTecnico(entero("ID de la orden: "), entero("ID del técnico: "));
                        salida.println("Técnico asignado.");
                    }
                    case "5" -> {
                        int id = controlador.registrarTecnico(leer("Nombre del técnico: "));
                        salida.println("Técnico registrado con ID: " + id);
                    }
                    case "6" -> {
                        List<Tecnico> tecnicos = controlador.listarTecnicos();
                        if (tecnicos.isEmpty()) {
                            salida.println("No hay técnicos registrados.");
                        }
                        for (Tecnico tecnico : tecnicos) {
                            salida.println(tecnico.getId() + " | " + tecnico.getNombre());
                        }
                    }
                    default -> salida.println("Selecciona una opción del menú.");
                }
            } catch (NumberFormatException e) {
                salida.println("Ingresa un número válido.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                salida.println(e.getMessage());
            } catch (SQLException e) {
                salida.println("No se pudo realizar la operación. Código MySQL: " + e.getErrorCode()
                        + " | Estado SQL: " + e.getSQLState());
                switch (e.getErrorCode()) {
                    case 1045:
                        salida.println("Revisa el usuario y la contraseña de la conexión.");
                        break;
                    case 1049:
                        salida.println("La base de datos configurada no existe.");
                        break;
                    case 1146:
                        salida.println("Falta una tabla. Ejecuta los scripts de estructura en la base configurada.");
                        break;
                    case 1452:
                        salida.println("Un ID relacionado no existe. Comprueba vehículo, mantenimiento, orden y técnico según la operación.");
                        break;
                    case 1062:
                        salida.println("Ya existe un registro con esos datos únicos.");
                        break;
                    default:
                        salida.println("Revisa la conexión y el esquema de la base. Estado SQL: " + e.getSQLState());
                }
            }
        }
    }

    private String leer(String mensaje) {
        salida.print(mensaje);
        if (!entrada.hasNextLine()) throw new IllegalStateException("Entrada finalizada.");
        return entrada.nextLine().strip();
    }

    private int entero(String mensaje) { return Integer.parseInt(leer(mensaje)); }

    private void imprimir(OrdenServicio orden) {
        salida.printf("Orden %d | Vehículo %d | Tipo %d | Estado %d | Recepción %s | Entrega %s | Mano de obra %s | %s%n",
                orden.getId(), orden.getVehiculoId(), orden.getMantenimientoTipoId(), orden.getEstadoId(),
                orden.getFechaRecepcion(), orden.getFechaEntrega(), orden.getCostoManoObra(), orden.getDescripcionProblema());
    }
}
