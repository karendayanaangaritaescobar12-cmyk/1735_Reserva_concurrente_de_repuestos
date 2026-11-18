# Sistema de Gestión para el Taller Automotriz CarMotors

CarMotors organiza la información del taller. Esta entrega documenta el módulo de
órdenes de servicio; los archivos de los demás integrantes se conservan para su integración.

## Tecnologías

Java 17, MySQL 8, JDBC, Maven y consola.

## Funcionalidades de órdenes

- Crear órdenes para un vehículo y un tipo de mantenimiento registrados.
- Consultar una orden, listar órdenes y consultar sus técnicos.
- Asignar técnicos existentes sin duplicar la misma asignación.
- Validar identificadores positivos, descripción obligatoria y mano de obra entre
  0 y 99999999.99, con hasta dos decimales. Las órdenes nacen en estado Pendiente.
- Confirmar o revertir la transacción de asignación de técnicos.
- Cambiar de estado desde la opción 5 de la consola. El ciclo avanza en este orden,
  Pendiente → En Proceso → Completado → Facturado → Entregado. Al entregar la orden
  se registra la fecha de entrega.

- Asignar repuestos desde la opción 6 mientras la orden está Pendiente o En Proceso.
  La reserva reduce la disponibilidad y conserva el precio unitario en la orden.
  Al pasar a Completado, el descuento del stock físico y el cambio de estado se
  confirman en la misma transacción. La emisión de facturas corresponde a su módulo.

## Base de datos

`database/schema.sql` crea las tablas necesarias para órdenes: clientes y vehículos,
tipos de mantenimiento, estados, servicios, técnicos y la relación servicios-técnicos.
Un vehículo pertenece a un cliente; cada servicio referencia vehículo, tipo y estado.
Un servicio puede tener varios técnicos y repuestos. La tabla `servicios_repuestos`
guarda la cantidad y el precio de cada asignación. El stock disponible se calcula
restando `cantidad_reservada` de `cantidad_stock`.

![Diagrama entidad-relación del equipo](docs/diagrama-er.png)

Los SQL principales cubren este módulo y sus dependencias. La integración del esquema
de inventario y las cantidades finales de datos corresponde a la entrega del equipo.

## Instalación y ejecución

Requisitos: JDK 17 o superior, Maven y MySQL Server 8.

1. Clonar el repositorio privado usando su URL y entrar en `proyecto-java-grupo06`:
   ```powershell
   git clone <URL-del-repositorio>
   cd proyecto-java-grupo6/proyecto-java-grupo06
   ```
2. Crear una base nueva y ejecutar, en este orden, desde el cliente MySQL:
   ```sql
   CREATE DATABASE carmotors CHARACTER SET utf8mb4;
   USE carmotors;
   SOURCE database/schema.sql;
   SOURCE database/data.sql;
   ```
   Estos scripts se ejecutan una vez sobre una base nueva; no son una migración
   para una base que ya contiene las tablas.
3. Configurar la conexión en la terminal PowerShell:
   ```powershell
   $env:CARMOTORS_DB_URL = 'jdbc:mysql://localhost:3306/carmotors'
   $env:CARMOTORS_DB_USER = 'tu_usuario'
   $env:CARMOTORS_DB_PASSWORD = 'tu_clave'
   ```
4. Compilar y ejecutar:
   ```powershell
   mvn -Pordenes clean compile exec:java
   ```
   Los datos de ejemplo incluyen vehículo, tipo de mantenimiento, técnico y repuesto con ID 1.

El perfil `ordenes` compila únicamente esta parte. La compilación general queda
pendiente de corregir errores previos en inventario/proveedores: `EvaluacionProveedor.java`
declara otra clase y el servicio de inventario referencia un `RepuestoDao` inexistente.

## Pruebas del módulo

`src/test/java/com/carmotors/OrdenServicioPrueba.java` es una prueba ejecutable desde
su método `main` en el IDE. Sin argumentos comprueba 14 entradas inválidas sin abrir
conexiones. No se ejecuta automáticamente con `mvn test`.

Con el argumento `--mysql`, utiliza las variables de conexión y los datos de ejemplo
de `schema.sql` y `data.sql`. Debe ejecutarse sobre una base exclusiva de pruebas;
crea órdenes y un repuesto temporal para las pruebas, y elimina esos datos al terminar.

También admite `--h2` si se agrega el JAR H2 2.3.232 al classpath de la prueba en el
IDE. Este modo crea una base temporal en memoria, ejecuta ambos SQL y comprueba
creación, consulta, listado, asignación sin duplicados, rollback por técnico inexistente,
el ciclo completo de estados, el rechazo de transiciones inválidas, la fecha de entrega,
la reserva por consola y el descuento único de inventario.
También comprueba el cambio de estado por consola mediante el controlador. H2 se usa solo para comprobar el
módulo; la aplicación mantiene su conexión MySQL.

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

Grupo 6. Los nombres completos deben completarse con los datos de los integrantes.
