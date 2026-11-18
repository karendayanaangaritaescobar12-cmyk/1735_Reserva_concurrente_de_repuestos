package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.controlador.ClienteControlador;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.servicio.OrdenServicioServicio;
import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.HistorialClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.servicio.HistorialClienteServicio;
import com.carmotors.servicio.RegistroClienteServicio;
import com.carmotors.servicio.RegistroVehiculoServicio;
import com.carmotors.vista.MenuClientes;
import com.carmotors.vista.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        RegistroClienteServicio registro = new RegistroClienteServicio(new ClienteDao());
        HistorialClienteServicio consultas = new HistorialClienteServicio(new HistorialClienteDao());
        ClienteControlador clientes = new ClienteControlador(registro, new RegistroVehiculoServicio(new VehiculoDao()), consultas);
        OrdenServicioServicio ordenes = new OrdenServicioServicio(ConexionFactory.getInstancia()::abrir);
        new MenuPrincipal(new MenuClientes(clientes), new OrdenServicioControlador(ordenes)).ejecutar();
    }
}
