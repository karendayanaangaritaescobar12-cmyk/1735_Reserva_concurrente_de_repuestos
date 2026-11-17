package com.carmotors.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Una fábrica compartida; cada operación obtiene su propia conexión. */
public final class ConexionFactory {
    private static final ConexionFactory INSTANCIA = new ConexionFactory();

    private ConexionFactory() { }

    public static ConexionFactory getInstancia() { return INSTANCIA; }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(variable("CARMOTORS_DB_URL"),
                variable("CARMOTORS_DB_USER"), variable("CARMOTORS_DB_PASSWORD"));
    }

    private String variable(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null) throw new IllegalStateException("Configura la variable " + nombre);
        return valor;
    }
}
