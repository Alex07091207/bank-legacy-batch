# Bank Legacy Batch

## Desarrollo Backend III - PBY2203

Proyecto desarrollado para la actividad de **Semana 2** de Desarrollo Backend III.

El objetivo es modernizar tres procesos batch pertenecientes a un sistema legacy del Banco XYZ utilizando **Spring Batch**, incorporando procesamiento concurrente multihilo, políticas personalizadas de tolerancia a fallos y procesamiento por bloques (chunks).

---

## Objetivo del proyecto

Implementar un sistema de migración de procesos batch utilizando Spring Batch capaz de leer información desde archivos CSV, validar y transformar los datos en paralelo, gestionar errores de parseo y formato, y almacenar los resultados en una base de datos MySQL.

El proyecto implementa tres procesos principales:

1. **Reporte de transacciones diarias:** Detección de anomalías y procesamiento concurrente.
2. **Cálculo de intereses mensuales:** Cálculo de intereses y actualización de saldos finales por tipo de cuenta.
3. **Generación de estados de cuenta anuales:** Consolidación de información anual para auditoría.

---

## Tecnologías utilizadas

- Java 17+
- Spring Boot 3.5.3
- Spring Batch 5.2.2
- Maven
- MySQL
- MySQL Workbench
- Git / GitHub

---

## Estructura del proyecto

```text
bank-legacy-batch
│
├── Evidencias
│   ├── Screenshot_Transacciones_Consola.png
│   ├── Screenshot_Transacciones_DB.png
│   ├── Screenshot_Intereses_Consola.png
│   ├── Screenshot_Intereses_DB.png
│   ├── Screenshot_CuentasAnuales_Consola.png
│   └── Screenshot_CuentasAnuales_DB.png
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.banco.bank_legacy_batch
│   │   │       ├── config
│   │   │       │   ├── BatchConfig.java
│   │   │       │   ├── TransaccionesJobConfig.java
│   │   │       │   ├── InteresesJobConfig.java
│   │   │       │   └── CuentasAnualesJobConfig.java
│   │   │       │
│   │   │       ├── model
│   │   │       │   ├── Transaccion.java
│   │   │       │   ├── Interes.java
│   │   │       │   └── CuentaAnual.java
│   │   │       │
│   │   │       ├── processor
│   │   │       │   ├── TransaccionProcessor.java
│   │   │       │   ├── InteresProcessor.java
│   │   │       │   └── CuentaAnualProcessor.java
│   │   │       │
│   │   │       ├── policy
│   │   │       │   └── CustomSkipPolicy.java
│   │   │       │
│   │   │       ├── Exception
│   │   │       │   └── DatoInvalidoException.java
│   │   │       │
│   │   │       └── BankLegacyBatchApplication.java
│   │   │
│   │   └── resources
│   │       ├── data
│   │       │   ├── transacciones.csv
│   │       │   ├── intereses.csv
│   │       │   └── cuentas_anuales.csv
│   │       ├── application.properties
│   │       └── schema.sql
```

---

## Procesos Batch Implementados

### 1. Reporte de Transacciones Diarias
* **Job:** `transaccionesJob`
* **Entrada:** `src/main/resources/data/transacciones.csv`
* **Salida:** Tabla `transacciones_procesadas`
* **Lógica:** Valida campos obligatorios, montos negativos y tipos de transacción. Registra observaciones y estados (`PROCESADO` o `PROCESADO_CON_OBSERVACIONES`).

### 2. Cálculo de Intereses Mensuales
* **Job:** `interesesJob`
* **Entrada:** `src/main/resources/data/intereses.csv`
* **Salida:** Tabla `intereses_procesados`
* **Lógica:** Aplica tasas de interés según el tipo de producto (ahorro, préstamo, hipoteca), calcula el interés generado y el saldo final acumulado.

### 3. Generación de Estados de Cuenta Anuales
* **Job:** `cuentasAnualesJob`
* **Entrada:** `src/main/resources/data/cuentas_anuales.csv`
* **Salida:** Tabla `cuentas_anuales_procesadas`
* **Lógica:** Compila movimientos anuales para auditoría y validación contable.

---

## Mejoras de Arquitectura (Semana 2)

### 1. Procesamiento Concurrente (Multithreading)
Se configuró un `ThreadPoolTaskExecutor` en `BatchConfig.java` con **3 hilos de ejecución paralela**:
* `corePoolSize`: 3
* `maxPoolSize`: 3
* Prefijo de hilos: `Batch-Hilo-`
* Lectores configurados con `.saveState(false)` para permitir la lectura concurrente thread-safe.

### 2. Política de Tolerancia a Fallos (`CustomSkipPolicy`)
Se implementó una política `SkipPolicy` personalizada para:
* Capturar y registrar en logs/consola advertencias detalladas de líneas corruptas (`FlatFileParseException`).
* Permitir saltar hasta 10 registros defectuosos por Job sin abortar la ejecución del lote.

### 3. Procesamiento por Chunks
Los steps procesan los registros en bloques de **5 elementos** (`.chunk(5)`), optimizando el uso de memoria y transacciones a la base de datos.

---

## Configuración y Seguridad de Base de Datos

El proyecto utiliza **MySQL** (`bank_batch`). Por seguridad, las credenciales no se almacenan en texto plano en el repositorio:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bank_batch
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.batch.jdbc.initialize-schema=always
spring.sql.init.mode=always
spring.batch.job.enabled=true
```

---

## Instrucciones de Ejecución

### Requisitos previos
* Java 17 o superior.
* MySQL Server en ejecución.
* Maven / Maven Wrapper.

### 1. Base de Datos
Crear la base de datos en MySQL:
```sql
CREATE DATABASE bank_batch;
```

### 2. Variable de Entorno
Configurar la variable `DB_PASSWORD` en el IDE o sistema operativo con la contraseña de MySQL local.

### 3. Ejecutar los Jobs
Configurar la propiedad `spring.batch.job.name` en `application.properties`:
* **Transacciones:** `spring.batch.job.name=transaccionesJob`
* **Intereses:** `spring.batch.job.name=interesesJob`
* **Cuentas Anuales:** `spring.batch.job.name=cuentasAnualesJob`

O mediante línea de comandos:
```powershell
# Ejecutar Transacciones
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.batch.job.name=transaccionesJob run.id=1"

# Ejecutar Intereses
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.batch.job.name=interesesJob run.id=2"

# Ejecutar Cuentas Anuales
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.batch.job.name=cuentasAnualesJob run.id=3"
```

---

## Verificación de Resultados

Consultar las tablas procesadas en MySQL Workbench:

```sql
USE bank_batch;

SELECT * FROM transacciones_procesadas;
SELECT * FROM intereses_procesados;
SELECT * FROM cuentas_anuales_procesadas;
```
---

## Autoría

* **Alexander Diaz y Kevin Lovera**
* **Asignatura:** Desarrollo Backend III - PBY2203
* **Institución:** DUOC UC
* **Repositorio GitHub:** `https://github.com/Kevinlovera/bank-legacy-batch`