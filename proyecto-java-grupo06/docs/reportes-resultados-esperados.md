# Reportes gerenciales — resultados esperados

Módulo de reportes de CarMotors.

## Archivos

| Archivo | Para qué sirve |
|---|---|
| `servicio/ReporteServicio.java` | Calcula los 3 reportes con Streams (día 2) |
| `dao/ReporteDao.java` | Lee de MySQL las órdenes, técnicos y repuestos (día 2) |
| `controlador/ReporteControlador.java` | Recibe las fechas que escribe el usuario y llama al servicio (día 3) |
| `modelo/DatosFactura.java` | Datos que se le entregan a facturación (día 3) |
| `dao/FacturaDao.java` | Lee de MySQL los datos de la factura de una orden (día 3) |
| `database/data.sql` | Datos de prueba. Se puede ejecutar varias veces |
| `test/.../ReportesDia2Prueba.java` y `ReportesDia3Prueba.java` | Pruebas |

## Reglas

- El periodo incluye el día inicial y el día final completos.
- Se usa la fecha en que se recibió la orden.
- Una orden está **terminada** si su estado es `Completado`, `Facturado` o `Entregado`.
- Una orden sin tipo o sin estado sale como `Sin tipo` / `Sin estado`.
- Solo se factura una orden en estado `Completado`.

## Datos de prueba

| Orden | Fecha | Tipo | Estado | Técnicos | Repuestos | Mano de obra |
|---|---|---|---|---|---|---|
| Reporte: cambio de aceite | 2026-10-05 | Preventivo | Entregado | Luis | — | 45.000 |
| Reporte: cambio de pastillas | 2026-10-12 | Correctivo | Facturado | Luis, Ana | 2 pastillas × 95.000 | 80.000 |
| Reporte: batería descargada | 2026-10-20 | Correctivo | Completado | Ana | 1 batería × 380.000 | 30.000 |
| Reporte: revisión general | 2026-11-03 | Preventivo | En Proceso | Luis | 1 pastilla × 95.000 | 60.000 |
| Reporte: ruido en la suspensión | 2026-11-10 | Correctivo | Pendiente | Mario | — | 50.000 |

## Resultados esperados (del 2026-10-01 al 2026-11-30)

**Servicios por tipo:** Correctivo 3, Preventivo 2

**Servicios por estado:** 1 de cada uno (Completado, En Proceso, Entregado, Facturado, Pendiente)

**Servicios por mes:** octubre 3, noviembre 2

**Productividad por técnico**

| Técnico | Asignadas | Terminadas | % |
|---|---|---|---|
| Ana Torres | 2 | 2 | 100 % |
| Luis Pérez | 3 | 2 | 67 % |
| Mario Díaz | 1 | 0 | 0 % |

**Consumo de repuestos**

| Repuesto | Unidades | Valor | Órdenes |
|---|---|---|---|
| Pastillas de freno | 3 | 285.000 | 2 |
| Batería 12V | 1 | 380.000 | 1 |

**Factura de "Reporte: batería descargada"**: repuestos 380.000 + mano de obra 30.000 = **410.000** (sin impuestos)

## Casos límite que se prueban

| Caso | Qué debe pasar |
|---|---|
| Periodo sin órdenes | Los reportes salen vacíos, sin errores |
| Un solo día | Solo salen las órdenes de ese día |
| Orden a las 23:59 del último día | Se incluye |
| Orden sin tipo, estado ni técnicos | Sale como "Sin tipo" / "Sin estado" y no cambia la productividad |
| Fecha vacía, mes 13 o formato `01/10/2026` | Se rechaza con un mensaje claro |
| Fecha inicial después de la final | Se rechaza |
| Factura sin repuestos | El subtotal es solo la mano de obra |
| Factura de una orden Pendiente o ya Facturada | No se permite |

## Datos que recibe facturación

`ReporteControlador.datosFactura(numeroDeOrden)` devuelve un `DatosFactura` con:

- número y estado de la orden
- nombre y documento del cliente
- placa del vehículo
- lista de repuestos usados (nombre, cantidad, precio)
- `getTotalRepuestos()` → va en la columna `total_repuestos`
- `getManoObra()` → va en la columna `total_mano_obra`
- `getSubtotal()` → repuestos + mano de obra, la base para calcular los impuestos

Facturación calcula los impuestos y el total final, y después cambia la orden a `Facturado`.

**Para acordar con el equipo:** hoy nadie guarda en la tabla `servicios_repuestos` los repuestos que se usan desde la app. Sin eso, el consumo y la factura solo muestran los datos de prueba.

## Cómo probar

```powershell
# Sin MySQL
.\scripts\verificar-reportes.ps1

# Con MySQL (en una base nueva: schema.sql y data.sql)
$env:CARMOTORS_DB_PASSWORD = "tu_clave"
.\scripts\verificar-reportes.ps1 -Integracion -MySqlJar C:\ruta\mysql-connector-j-8.4.0.jar
```
