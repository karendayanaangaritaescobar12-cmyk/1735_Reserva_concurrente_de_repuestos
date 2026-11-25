package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.controlador.ClienteControlador;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.controlador.ReporteControlador;
import com.carmotors.dao.FacturaDao;
import com.carmotors.dao.ReporteDao;
import com.carmotors.servicio.ReporteServicio;
import com.carmotors.servicio.OrdenServicioServicio;
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
        OrdenServicioServicio ordenes = new OrdenServicioServicio(ConexionFactory.getInstancia()::abrir);
        ordenes.suscribir(aviso -> System.out.printf("Aviso para %s | Orden %d | Estado %s%n",
                aviso.correo(), aviso.orden(), aviso.estado()));
        ReporteDao reporteDao = new ReporteDao();
        ReporteControlador reportes = new ReporteControlador(new ReporteServicio(reporteDao),
                new FacturaDao(), reporteDao);
        new MenuPrincipal(new MenuClientes(clientes), new OrdenServicioControlador(ordenes), reportes).ejecutar();
    }
}
