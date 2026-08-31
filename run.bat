@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "src\main\resources\db.properties" (
  echo.
  echo  ATENCAO: banco nao configurado.
  echo  Copie "src\main\resources\db.properties.example" para
  echo  "src\main\resources\db.properties" e preencha suas credenciais.
  echo.
  pause
  exit /b 1
)

echo [1/3] Compilando...
call mvn -q package
if errorlevel 1 (
  echo Falha no build. Verifique se o JDK 17+ e o Maven estao instalados.
  pause
  exit /b 1
)

echo [2/3] Criando as tabelas no banco (se ainda nao existirem)...
java -cp "target\rockysoul-java-1.0-SNAPSHOT.jar" br.com.rockysoulup.database.SchemaSetup

echo [3/3] Iniciando o sistema...
java -jar "target\rockysoul-java-1.0-SNAPSHOT.jar"
pause