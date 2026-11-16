param([string]$JavaHome = $env:JAVA_HOME, [string]$MySqlJar, [switch]$Integracion)
$ErrorActionPreference = 'Stop'
$proyectoOrdenes = Split-Path $PSScriptRoot -Parent
$javacOrdenes = if ($JavaHome) { Join-Path $JavaHome 'bin\javac.exe' } else { 'javac' }
$javaOrdenes = if ($JavaHome) { Join-Path $JavaHome 'bin\java.exe' } else { 'java' }
$destinoOrdenes = Join-Path $proyectoOrdenes 'target\ordenes-dia2'
New-Item -ItemType Directory -Force -Path $destinoOrdenes | Out-Null
$relativosOrdenes = @(
    'modelo/EstadoServicio.java', 'modelo/OrdenServicio.java',
    'dao/EstadoServicioDao.java', 'dao/OrdenServicioDao.java', 'dao/TecnicoOrdenDao.java',
    'servicio/OrdenServicioServicio.java'
)
$fuentesOrdenes = @($relativosOrdenes | ForEach-Object { Join-Path $proyectoOrdenes "src/main/java/com/carmotors/$_" })
$fuentesOrdenes += Join-Path $proyectoOrdenes 'src/test/java/com/carmotors/OrdenesDia2Prueba.java'
& $javacOrdenes --release 17 -encoding UTF-8 -d $destinoOrdenes @fuentesOrdenes
if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion del dia 2.' }
$classpathOrdenes = $destinoOrdenes
$argumentosOrdenes = @()
if ($Integracion) {
    if (!$MySqlJar -or !(Test-Path -LiteralPath $MySqlJar)) { throw 'Indica -MySqlJar con MySQL Connector/J.' }
    $classpathOrdenes += [IO.Path]::PathSeparator + $MySqlJar
    $argumentosOrdenes = @('--mysql')
}
& $javaOrdenes -cp $classpathOrdenes com.carmotors.OrdenesDia2Prueba @argumentosOrdenes
if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas del dia 2.' }
