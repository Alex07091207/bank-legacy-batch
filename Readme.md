# Bank Legacy Batch & BFF Architecture

## Desarrollo Backend III - PBY2203

Proyecto desarrollado para las actividades de **Semana 2, 3, 4 y 5 (Experiencia 2)** de Desarrollo Backend III.

El proyecto integra una solución completa de modernización de sistemas legacy:

1. **Módulo Batch (Semanas 2 y 3):** Migración, validación y cálculo de datos financieros masivos mediante **Spring Batch** con particionamiento multihilo y tolerancia a fallos.
2. **Capa BFF (Semana 5):** Exposición de servicios desacoplados bajo el patrón **Backend for Frontend (BFF)** sobre **Spring Boot** y **Spring Security**, optimizando las respuestas y el consumo de recursos para tres canales cliente: **Web**, **Mobile** y **ATM (Cajeros)**.

---

## 1. Arquitectura BFF y Optimización por Canal

Se implementaron controladores, servicios y DTOs específicos e independientes para evitar sobrecargar a los dispositivos y garantizar tiempos de respuesta reducidos:

| Canal | Endpoint | Payload / Campos Retornados | Estrategia de Optimización |
| :--- | :--- | :--- | :--- |
| **Web** | `GET https://localhost:8443/bff/web/cuenta/{id}` | 5 campos: `nombre`, `saldo`, `tipo`, `saldoFinal`, `estado` | Provee la información consolidada necesaria para interfaces de escritorio complejas y auditoría. |
| **Mobile** | `GET https://localhost:8443/bff/mobile/cuenta/{id}` | 2 campos: `nombre`, `saldo` | Payload ultraligero que prioriza la velocidad de carga y minimiza el consumo de datos móviles. |
| **ATM** | `GET https://localhost:8443/bff/atm/cuenta/{id}/saldo` | 1 campo: `saldo` | Respuesta mínima enfocada exclusivamente en la operación crítica inmediata en pantalla de cajero. |

---

## 2. Seguridad y Control de Acceso

La capa BFF implementa un modelo de seguridad perimetral de dos niveles:

### A. Cifrado en Tránsito (HTTPS / SSL)

* Comunicación segura obligatoria bajo **HTTPS** en el puerto `8443`.
* Configurado mediante certificado de identidad en formato PKCS12 (`keystore.p12` ubicado en `src/main/resources/`).

### B. Autenticación y Autorización por Rol (Spring Security)

Autenticación HTTP Basic Auth configurada en `SecurityConfig.java` con usuarios y roles desacoplados:

* **Canal Web:** Usuario `web_client` | Rol `ROLE_WEB` | Acceso a `/bff/web/**`
* **Canal Mobile:** Usuario `mobile_client` | Rol `ROLE_MOBILE` | Acceso a `/bff/mobile/**`
* **Canal ATM:** Usuario `atm_client` | Rol `ROLE_ATM` | Acceso a `/bff/atm/**`
* **Credenciales de prueba:** Contraseñas protegidas mediante codificación BCrypt.

### C. Validación de Metadatos (Headers)

* **`X-Canal` (Obligatorio en todos los canales):** Valida la identidad del canal solicitante (`WEB`, `MOBILE`, `ATM`) mediante `BffSecurityService`.
* **`X-Operacion` (Obligatorio en ATM):** Exige la especificación de la acción transaccional (`CONSULTAR_SALDO`).

---

## 3. Estructura del Proyecto

```
bank-legacy-batch
│
├── Evidencias
│   ├── Evidencia de BFF WEB (200 ok).png
│   ├── Evidencia BFF por dispositivo movil (200 ok).png
│   ├── Evidencia BFF ATM (200 ok).png
│   ├── Evidencia de autorización WEB (401 debido a que se ingreso mal el usuario).png
│   ├── Evidencia de autorización ATM (401 debido a que se ingreso mal el username).png
│   ├── Evidencia de autorización WEB (Ingresamos usuario erroneo).png
│   └── (Evidencias previas Batch...)
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.banco.bank_legacy_batch
│   │   │       ├── bff
│   │   │       │   ├── controller
│   │   │       │   │   ├── WebBffController.java
│   │   │       │   │   ├── MobileBffController.java
│   │   │       │   │   └── AtmBffController.java
│   │   │       │   ├── dto
│   │   │       │   │   ├── WebCuentaResponse.java
│   │   │       │   │   ├── MobileCuentaResponse.java
│   │   │       │   │   └── AtmCuentaResponse.java
│   │   │       │   ├── repository
│   │   │       │   │   └── CuentaBffRepository.java
│   │   │       │   └── service
│   │   │       │       ├── WebBffService.java
│   │   │       │       ├── MobileBffService.java
│   │   │       │       ├── AtmBffService.java
│   │   │       │       └── BffSecurityService.java
│   │   │       │
│   │   │       ├── config
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── BatchConfig.java
│   │   │       │   └── JobRunner.java
│   │   │       │
│   │   │       ├── model
│   │   │       │   ├── Transaccion.java
│   │   │       │   ├── Interes.java
│   │   │       │   └── CuentaAnual.java
│   │   │       │
│   │   │       └── BankLegacyBatchApplication.java
│   │   │
│   │   └── resources
│   │       ├── data/
│   │       ├── application.properties
│   │       ├── keystore.p12
│   │       └── schema.sql
```

---

## 4. Procesos Batch Legacy (Base de Datos)

El sistema procesa y carga la información en la base de datos `bank_batch`:

* **transaccionesJob:** Procesa `transacciones.csv` hacia `transacciones_procesadas`.
* **interesesJob:** Procesa `intereses.csv` hacia `intereses_procesados` (tabla base consumida por la capa BFF).
* **cuentasAnualesJob:** Procesa `cuentas_anuales.csv` hacia `cuentas_anuales_procesadas`.

---

## 5. Instrucciones de Ejecución y Pruebas

### Requisitos previos

* Java 17 o superior.
* MySQL Server activo con esquema `bank_batch`.
* Postman (con opción SSL certificate verification desactivada para certificados autofirmados).

### Pasos de inicialización

**Iniciar la aplicación:**

```powershell
.\mvnw.cmd spring-boot:run
```

La API quedará disponible bajo HTTPS en `https://localhost:8443/`.

### Matriz de Pruebas en Postman

#### Canal Web

* **URL:** `GET https://localhost:8443/bff/web/cuenta/101`
* **Auth:** Basic Auth (`web_client` / `WebPass2026!`)
* **Headers:** `X-Canal: WEB`

#### Canal Mobile

* **URL:** `GET https://localhost:8443/bff/mobile/cuenta/101`
* **Auth:** Basic Auth (`mobile_client` / `MobilePass2026!`)
* **Headers:** `X-Canal: MOBILE`

#### Canal ATM

* **URL:** `GET https://localhost:8443/bff/atm/cuenta/101/saldo`
* **Auth:** Basic Auth (`atm_client` / `AtmPass2026!`)
* **Headers:** `X-Canal: ATM` y `X-Operacion: CONSULTAR_SALDO`

---

## 6. Evidencias de Evaluación

La carpeta `Evidencias/` incluye el registro visual de las pruebas realizadas:

* Respuestas correctas (200 OK) demostrando las cargas de respuesta optimizadas por canal.
* Pruebas negativas de seguridad (401 Unauthorized) ante intentos de acceso con credenciales no registradas o inválidas.
* Validación de obligatoriedad de cabeceras de canal y operación.

---

## Autoría

* **Autor:** Alexander Diaz
* **Asignatura:** Desarrollo Backend III - PBY2203
* **Institución:** DUOC UC
* **Repositorio GitHub:** https://github.com/Alex07091207/bank-legacy-batch.git
