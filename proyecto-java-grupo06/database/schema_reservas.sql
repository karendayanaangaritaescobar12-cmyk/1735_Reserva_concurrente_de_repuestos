CREATE TABLE IF NOT EXISTS reservas_repuesto (
                                                 id             INT AUTO_INCREMENT PRIMARY KEY,
                                                 repuesto_id    INT NOT NULL,
                                                 servicio_id    INT NOT NULL,
                                                 cantidad       INT NOT NULL,
                                                 estado         VARCHAR(20) NOT NULL DEFAULT 'RESERVADO',
    fecha_reserva  DATETIME NOT NULL,
    FOREIGN KEY (repuesto_id) REFERENCES repuestos(id)
    );

CREATE INDEX idx_reservas_servicio ON reservas_repuesto(servicio_id);
CREATE INDEX idx_reservas_estado ON reservas_repuesto(estado);