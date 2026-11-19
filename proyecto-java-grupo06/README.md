# Sistema de Gestión para el Taller Automotriz CarMotors

CarMotors gestiona clientes, vehículos y órdenes de servicio de un taller automotriz. El menú principal también permite abrir inventario, reportes y la consulta de datos de factura.

## Tecnologías

Java 17, MySQL 8, JDBC, Maven y consola.

## Funcionalidades de órdenes

- Crear órdenes para un vehículo y un tipo de mantenimiento registrados.
- Consultar una orden, listar órdenes y consultar sus técnicos.
- Asignar técnicos existentes sin duplicar la misma asignación.
- Validar identificadores positivos, descripción obligatoria y mano de obra entre
  0 y 99999999.99, con hasta dos decimales. Las órdenes nacen en estado Pendiente.
- Confirmar o revertir la transacción de asignación de técnicos.

- Cambiar de estado con la opción 7 del menú de órdenes. El ciclo avanza de Pendiente a En Proceso, Completado, Facturado y Entregado. Al entregar se registra la fecha.
- Reservar repuestos con la opción 8 mientras la orden está Pendiente o En Proceso. Al completar la orden se descuentan esas reservas del stock en la misma transacción. La consulta de la orden muestra los repuestos y sus precios.

## Estado de los demás módulos

- **Clientes y vehículos:** registro, consulta del historial y recordatorios de mantenimiento preventivo desde la consola.
- **Inventario y proveedores:** catálogo, lotes, evaluaciones, alertas y reservas implementados en el servicio y el controlador. El módulo está conectado al menú. El flujo de asignación y consumo de repuestos de las órdenes está disponible en su propio menú.
- **Reportes y facturación:** reportes de servicios, productividad y consumo de repuestos, además del subtotal de una orden completada. El módulo está conectado al menú. Queda pendiente integrar la emisión de facturas.

## Base de datos

`database/schema.sql` crea las tablas necesarias para órdenes: clientes y vehículos,
tipos de mantenimiento, estados, servicios, técnicos y la relación servicios-técnicos.
Un vehículo pertenece a un cliente; cada servicio referencia vehículo, tipo y estado.
Un servicio puede tener varios técnicos y un técnico puede atender varios servicios.

![Diagrama entidad-relación del equipo](docs/diagrama-er.png)

La carpeta database contiene solo schema.sql y data.sql. schema.sql incluye las 15 tablas del proyecto, lotes, evaluaciones, reservas y el nombre del servicio. data.sql incluye los catálogos y los datos de prueba de clientes, órdenes, técnicos y reportes.

## Instalación y ejecución

Requisitos: JDK 17 o superior, Maven y MySQL Server 8.

1. Clonar el repositorio privado usando su URL y entrar en `proyecto-java-grupo06`:
   ```powershell
   git clone <URL-del-repositorio>
   cd proyecto-java-grupo6/proyecto-java-grupo06
   ```
2. Abrir el cliente MySQL desde el directorio del proyecto y ejecutar los scripts sobre una instalación de prueba nueva:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/data.sql;
   ```
   En una base existente, vuelve a ejecutar solo `database/schema.sql` para agregar las columnas de reservas sin modificar las asignaciones históricas.
3. Configurar la conexión en la terminal PowerShell:
   ```powershell
   $env:CARMOTORS_DB_URL = 'jdbc:mysql://localhost:3306/carmotors'
   $env:CARMOTORS_DB_USER = 'tu_usuario'
   $env:CARMOTORS_DB_PASSWORD = 'tu_clave'
   ```
4. Compilar y ejecutar:
   ```powershell
   mvn clean compile exec:java
   ```
   Los datos de ejemplo incluyen vehículo, tipo de mantenimiento y técnico con ID 1.

## Pruebas

Compila las clases de prueba con `mvn test-compile` y ejecuta sus métodos `main` desde el IDE. Las clases ejecutables son `com.carmotors.OrdenServicioPrueba`, `com.carmotors.ReportesDia2Prueba` y `com.carmotors.ReportesDia3Prueba`.

- Sin argumentos, las pruebas cubren validaciones de órdenes, cálculos de reportes y datos de facturación sin abrir conexiones.
- Con `--mysql`, usa una base exclusiva de pruebas y las variables de conexión indicadas arriba. Los datos de reportes están incluidos en `database/data.sql`. Consulta los [resultados esperados](docs/reportes-resultados-esperados.md).
- Ejecuta `OrdenServicioPrueba --h2` desde la carpeta del proyecto con H2 2.3.232 en el classpath. El modo en memoria comprueba transiciones, reservas, rollback y descuento de inventario.

Se verificó la compilación con compatibilidad Java 17 y las pruebas ejecutables sin base de datos, incluidos los reintentos de campos inválidos. La ejecución contra MySQL queda pendiente.

## Decisiones de diseño

- MVC: `MenuPrincipal` recoge y presenta datos; `OrdenServicioControlador` delega
  las peticiones; `OrdenServicio` representa la entidad.
- Servicio: `OrdenServicioServicio` concentra validaciones y transacciones.
- DAO: cada DAO de órdenes contiene el SQL de su entidad o relación.
- Singleton: `ConexionFactory` ofrece una única fábrica, con conexiones independientes.
- Factory: la fábrica encapsula la creación de conexiones JDBC.
- Inyección de dependencias: servicio, controlador y vista reciben sus colaboradores.
  La interfaz funcional `Conexiones` permite probar validaciones sin una base real.
- Responsabilidad única: presentación, coordinación, negocio y persistencia se separan.
  La adquisición de conexiones depende de una abstracción en el servicio.
- Lambdas y Stream: búsqueda de estados del catálogo y recorrido de órdenes.

## Autores

Grupo 6. Nini, Yeimer, Andrés y Keiler.

Las pruebas RegistroCorreccionPrueba y ValidacionesFormularioPrueba verifican los formularios sin MySQL, incluidos correo, nombre, documento, teléfono, IDs, fechas y costos.
