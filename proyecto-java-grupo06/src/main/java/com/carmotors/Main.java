package com.carmotors;

import com.carmotors.controlador.ClienteControlador;
import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.HistorialClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.servicio.HistorialClienteServicio;
import com.carmotors.servicio.RegistroClienteServicio;
import com.carmotors.vista.MenuClientes;
import com.carmotors.vista.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        RegistroClienteServicio registro = new RegistroClienteServicio(new ClienteDao(), new VehiculoDao());
        HistorialClienteServicio consultas = new HistorialClienteServicio(new HistorialClienteDao());
        ClienteControlador clientes = new ClienteControlador(registro, consultas);
        new MenuPrincipal(new MenuClientes(clientes)).ejecutar();
    }
}
