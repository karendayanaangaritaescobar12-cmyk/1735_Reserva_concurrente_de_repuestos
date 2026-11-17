param([string]$JavaHome = $env:JAVA_HOME, [string]$MySqlJar, [switch]$Integracion)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$javac = if ($JavaHome) { Join-Path $JavaHome 'bin\javac.exe' } else { 'javac' }
$java = if ($JavaHome) { Join-Path $JavaHome 'bin\java.exe' } else { 'java' }
$destino = Join-Path $proyecto 'target\reportes'
New-Item -ItemType Directory -Force -Path $destino | Out-Null
$relativos = @(
    'config/ConexionFactory.java',
    'modelo/ServicioReporte.java', 'modelo/AsignacionTecnico.java', 'modelo/RepuestoUsado.java',
    'modelo/ProductividadTecnico.java', 'modelo/ConsumoRepuesto.java',
    'modelo/DatosFactura.java',
    'dao/FuenteReportes.java', 'dao/ReporteDao.java', 'dao/FacturaDao.java',
    'servicio/ReporteServicio.java',
    'controlador/ReporteControlador.java'
)
$fuentes = @($relativos | ForEach-Object { Join-Path $proyecto "src/main/java/com/carmotors/$_" })
$fuentes += Join-Path $proyecto 'src/test/java/com/carmotors/ReportesDia2Prueba.java'
$fuentes += Join-Path $proyecto 'src/test/java/com/carmotors/ReportesDia3Prueba.java'
& $javac --release 17 -encoding UTF-8 -d $destino @fuentes
if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion de reportes.' }
$classpath = $destino
$argumentos = @()
if ($Integracion) {
    if (!$MySqlJar -or !(Test-Path -LiteralPath $MySqlJar)) { throw 'Indica -MySqlJar con MySQL Connector/J.' }
    $classpath += [IO.Path]::PathSeparator + $MySqlJar
    $argumentos = @('--mysql')
}
foreach ($prueba in @('com.carmotors.ReportesDia2Prueba', 'com.carmotors.ReportesDia3Prueba')) {
    & $java '-Dstdout.encoding=UTF-8' -cp $classpath $prueba @argumentos
    if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas: $prueba" }
}
