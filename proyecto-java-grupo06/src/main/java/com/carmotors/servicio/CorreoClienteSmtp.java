package com.carmotors.servicio;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public final class CorreoClienteSmtp implements OrdenServicioServicio.ObservadorCliente {
    private final Session sesion;
    private final String usuario;
    private final String clave;
    private final String remitente;

    public CorreoClienteSmtp(Map<String, String> entorno) {
        usuario = requerido(entorno, "CARMOTORS_SMTP_USER");
        clave = requerido(entorno, "CARMOTORS_SMTP_PASSWORD");
        remitente = entorno.getOrDefault("CARMOTORS_SMTP_FROM", usuario);
        String puerto = entorno.getOrDefault("CARMOTORS_SMTP_PORT", "587");
        boolean tlsImplicito = "465".equals(puerto);
        Properties configuracion = new Properties();
        configuracion.setProperty("mail.smtp.host", requerido(entorno, "CARMOTORS_SMTP_HOST"));
        configuracion.setProperty("mail.smtp.port", puerto);
        configuracion.setProperty("mail.smtp.auth", "true");
        configuracion.setProperty("mail.smtp.ssl.enable", Boolean.toString(tlsImplicito));
        configuracion.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        configuracion.setProperty("mail.smtp.starttls.enable", Boolean.toString(!tlsImplicito));
        configuracion.setProperty("mail.smtp.starttls.required", Boolean.toString(!tlsImplicito));
        configuracion.setProperty("mail.smtp.connectiontimeout", "5000");
        configuracion.setProperty("mail.smtp.timeout", "5000");
        configuracion.setProperty("mail.smtp.writetimeout", "5000");
        sesion = Session.getInstance(configuracion);
    }

    @Override
    public void notificar(OrdenServicioServicio.AvisoCliente aviso) {
        try {
            String utf8 = StandardCharsets.UTF_8.name();
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente, true));
            mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(aviso.correo(), true));
            mensaje.setSubject("CarMotors | Orden " + aviso.orden(), utf8);
            mensaje.setText("Hola,%n%nTu orden %d quedó en estado «%s».%n%nCarMotors%n"
                    .formatted(aviso.orden(), aviso.estado()), utf8);
            Transport.send(mensaje, usuario, clave);
        } catch (MessagingException e) {
            throw new IllegalStateException("El servidor SMTP no pudo enviar el aviso.", e);
        }
    }

    private static String requerido(Map<String, String> entorno, String nombre) {
        String valor = entorno.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Configura " + nombre + " para activar los avisos por correo.");
        }
        return valor;
    }
}
