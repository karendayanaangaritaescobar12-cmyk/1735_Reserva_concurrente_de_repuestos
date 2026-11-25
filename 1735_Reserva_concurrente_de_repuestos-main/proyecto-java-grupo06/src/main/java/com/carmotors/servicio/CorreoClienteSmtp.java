package com.carmotors.servicio;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public final class CorreoClienteSmtp implements OrdenServicioServicio.ObservadorCliente {
    private final Session sesion;
    private final String usuario;
    private final String clave;
    private final InternetAddress remitente;

    public CorreoClienteSmtp(Map<String, String> entorno) {
        String servidor = requerido(entorno, "CARMOTORS_SMTP_HOST").strip();
        if (servidor.contains("/") || servidor.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("CARMOTORS_SMTP_HOST debe contener solo el nombre del servidor.");
        }
        usuario = requerido(entorno, "CARMOTORS_SMTP_USER").strip();
        clave = requerido(entorno, "CARMOTORS_SMTP_PASSWORD");
        try {
            remitente = direccion(entorno.getOrDefault("CARMOTORS_SMTP_FROM", usuario));
        } catch (AddressException e) {
            throw new IllegalArgumentException("Configura una dirección válida en CARMOTORS_SMTP_FROM.");
        }
        int puerto = puertoSmtp(entorno.getOrDefault("CARMOTORS_SMTP_PORT", "587"));
        boolean tlsImplicito = puerto == 465;
        Properties configuracion = new Properties();
        configuracion.setProperty("mail.smtp.host", servidor);
        configuracion.setProperty("mail.smtp.port", Integer.toString(puerto));
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
            mensaje.setFrom(remitente);
            mensaje.setRecipient(Message.RecipientType.TO, direccion(aviso.correo()));
            mensaje.setSubject("CarMotors | Orden " + aviso.orden(), utf8);
            mensaje.setText("Hola,%n%nTu orden %d quedó en estado «%s».%n%nCarMotors%n"
                    .formatted(aviso.orden(), aviso.estado()), utf8);
            Transport.send(mensaje, usuario, clave);
        } catch (AddressException e) {
            throw new IllegalArgumentException("La dirección de correo del cliente es inválida.");
        } catch (MessagingException e) {
            throw new IllegalStateException("El servidor SMTP no pudo enviar el aviso.", e);
        }
    }

    private static InternetAddress direccion(String valor) throws AddressException {
        InternetAddress direccion = new InternetAddress(valor.strip(), true);
        if (direccion.isGroup()) {
            throw new AddressException("Se requiere una sola dirección de correo.");
        }
        direccion.validate();
        return direccion;
    }

    private static int puertoSmtp(String valor) {
        try {
            int puerto = Integer.parseInt(valor.strip());
            if (puerto >= 1 && puerto <= 65535) return puerto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CARMOTORS_SMTP_PORT debe ser un número entre 1 y 65535.");
        }
        throw new IllegalArgumentException("CARMOTORS_SMTP_PORT debe ser un número entre 1 y 65535.");
    }

    private static String requerido(Map<String, String> entorno, String nombre) {
        String valor = entorno.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Configura " + nombre + " para activar los avisos por correo.");
        }
        return valor;
    }
}
