package com.carmotors.servicio;

import com.carmotors.excepcion.CampoInvalidoException;

final class ValidacionRegistro {
    private ValidacionRegistro() {}

    static String obligatorio(String valor, int limite, String campo) {
        String limpio = opcional(valor, limite, campo);
        if (limpio == null) {
            throw new CampoInvalidoException(campo, campo + " es obligatorio.");
        }
        return limpio;
    }

    static String opcional(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > limite) {
            throw new CampoInvalidoException(campo, campo + " admite hasta " + limite + " caracteres.");
        }
        return limpio;
    }
}
