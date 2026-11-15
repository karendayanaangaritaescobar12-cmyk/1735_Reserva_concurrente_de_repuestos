package com.carmotors.servicio;

import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.modelo.Cliente;
import com.carmotors.modelo.Vehiculo;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

public class RegistroClienteServicio {
    private final ClienteDao clientes;
    private final VehiculoDao vehiculos;

    public RegistroClienteServicio(ClienteDao clientes, VehiculoDao vehiculos) {
        this.clientes = clientes;
        this.vehiculos = vehiculos;
    }

    public void registrarCliente(String nombre, String documento, String telefono, String email) throws SQLException {
        String correo = obligatorio(email, 100, "Correo").toLowerCase(Locale.ROOT);
        if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
        Cliente cliente = new Cliente(obligatorio(nombre, 255, "Nombre"),
                obligatorio(documento, 20, "Documento"), opcional(telefono, 20, "Teléfono"), correo);
        try {
            clientes.insertar(cliente);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El documento o correo ya está registrado.", e);
        }
    }

    public void registrarVehiculo(String documento, String placa, String marca, String modelo) throws SQLException {
        Vehiculo vehiculo = new Vehiculo(obligatorio(documento, 20, "Documento"),
                obligatorio(placa, 10, "Placa").toUpperCase(Locale.ROOT),
                obligatorio(marca, 50, "Marca"), obligatorio(modelo, 50, "Modelo"));
        try {
            if (!vehiculos.insertar(vehiculo)) {
                throw new IllegalArgumentException("Primero registra al cliente.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("La placa ya está registrada o el cliente dejó de estar disponible.", e);
        }
    }

    private static String obligatorio(String valor, int limite, String campo) {
        String limpio = opcional(valor, limite, campo);
        if (limpio == null) throw new IllegalArgumentException(campo + " es obligatorio.");
        return limpio;
    }

    private static String opcional(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.strip();
        if (limpio.length() > limite) {
            throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
        }
        return limpio;
    }
}
