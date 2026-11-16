param([string]$JavaHome = $env:JAVA_HOME, [string]$MySqlJar, [switch]$Integracion)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$javac = if ($JavaHome) { Join-Path $JavaHome 'bin\javac.exe' } else { 'javac' }
$java = if ($JavaHome) { Join-Path $JavaHome 'bin\java.exe' } else { 'java' }
$destino = Join-Path $proyecto 'target\reportes-dia2'
New-Item -ItemType Directory -Force -Path $destino | Out-Null
$relativos = @(
    'config/ConexionFactory.java',
    'modelo/ServicioReporte.java', 'modelo/AsignacionTecnico.java', 'modelo/RepuestoUsado.java',
    'modelo/ProductividadTecnico.java', 'modelo/ConsumoRepuesto.java',
    'dao/FuenteReportes.java', 'dao/ReporteDao.java',
    'servicio/ReporteServicio.java'
)
$fuentes = @($relativos | ForEach-Object { Join-Path $proyecto "src/main/java/com/carmotors/$_" })
$fuentes += Join-Path $proyecto 'src/test/java/com/carmotors/ReportesDia2Prueba.java'
& $javac --release 17 -encoding UTF-8 -d $destino @fuentes
if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion de reportes.' }
$classpath = $destino
$argumentos = @()
if ($Integracion) {
    if (!$MySqlJar -or !(Test-Path -LiteralPath $MySqlJar)) { throw 'Indica -MySqlJar con MySQL Connector/J.' }
    $classpath += [IO.Path]::PathSeparator + $MySqlJar
    $argumentos = @('--mysql')
}
& $java -cp $classpath com.carmotors.ReportesDia2Prueba @argumentos
if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas de reportes.' }
