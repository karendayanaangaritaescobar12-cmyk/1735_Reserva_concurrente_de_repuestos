CREATE TABLE IF NOT EXISTS lotes (
                                     id                  INT AUTO_INCREMENT PRIMARY KEY,
                                     codigo_lote         VARCHAR(50)  NOT NULL UNIQUE,
    repuesto_id         INT          NOT NULL,
    proveedor_id        INT          NOT NULL,
    fecha_ingreso       DATE         NOT NULL,
    fecha_caducidad     DATE         NULL,
    cantidad_recibida   INT          NOT NULL,
    cantidad_disponible INT          NOT NULL,
    FOREIGN KEY (repuesto_id)  REFERENCES repuestos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
    );

CREATE TABLE IF NOT EXISTS evaluaciones_proveedor (
                                                      id                       INT AUTO_INCREMENT PRIMARY KEY,
                                                      proveedor_id             INT  NOT NULL,
                                                      fecha_evaluacion         DATE NOT NULL,
                                                      calificacion_calidad     TINYINT NOT NULL,
                                                      calificacion_puntualidad TINYINT NOT NULL,
                                                      observaciones            TEXT,
                                                      FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
    );

CREATE INDEX idx_lotes_repuesto ON lotes(repuesto_id);
CREATE INDEX idx_lotes_caducidad ON lotes(fecha_caducidad);
CREATE INDEX idx_evaluaciones_proveedor ON evaluaciones_proveedor(proveedor_id);