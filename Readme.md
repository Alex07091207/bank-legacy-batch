# Bank Legacy Batch - Arquitectura de Microservicios Cloud-Native

Este proyecto implementa una solución desacoplada basada en una arquitectura de microservicios para el ecosistema bancario (BancoXYZ / Bank Legacy). Desarrollado con **Java 17** y **Spring Boot 3.3.3**, empaquetado y orquestado en contenedores mediante **Docker Compose**.

---

## 🏗 Arquitectura del Sistema

La solución integra componentes bajo el estándar *12-Factor App* y principios *Cloud-Native*:

* **BFF (Backend for Frontend) (`bank-legacy-batch`)**: Microservicio central que expone APIs seguras sobre HTTPS (puerto `8443`). Implementa validación de tokens OAuth 2.0 (JWT), control de acceso por cabeceras (`X-Canal`, `X-Operacion`), tolerancia a fallos y emisión asíncrona de eventos transaccionales.
* **Config Server (`config-server`)**: Servidor centralizado de configuración dinámica externa (puerto `8888`).
* **Eureka Server (`eureka-server`)**: Plataforma de registro y descubrimiento dinámico de microservicios (puerto `8761`).
* **Base de Datos (`mysql-db`)**: Instancia MySQL 8.0 en contenedor dedicada a la persistencia transaccional (mapeo `3307:3306`).
* **Broker de Eventos (`kafka` y `zookeeper`)**: Infraestructura de mensajería asíncrona para la publicación y consumo de eventos transaccionales (puertos `9092` y `2181`).

---

## 🛠 Requisitos Previos

* Docker y Docker Compose
* Java Development Kit (JDK) 17
* Postman (para pruebas de integración)

---

## 🚀 Despliegue y Ejecución

### 1. Compilación de artefactos

En los directorios raíz de `config_server` y `bank-legacy-batch`, compilar los empaquetados ignorando las pruebas unitarias:

```bash
./mvnw clean package -Dmaven.test.skip=true
```

### 2. Orquestación con Docker Compose

En la raíz del proyecto (donde se ubica `docker-compose.yml`):

```bash
docker-compose up --build -d
```

### 3. Verificación de contenedores

Comprobar que los 6 contenedores se encuentren en estado `Up`:

```bash
docker ps
```

---

## 🔒 Autenticación y Seguridad (OAuth 2.0 / JWT)

La API opera como un **OAuth 2.0 Resource Server** validando criptográficamente tokens JWT emitidos por Auth0.

* **Issuer URI:** `https://dev-hqp0nydclleepom6.us.auth0.com/`
* **Acceso no autenticado:** Retorna `401 Unauthorized` de forma inmediata.
* **Acceso autenticado:** Requiere cabecera HTTP: `Authorization: Bearer <JWT_TOKEN>`.

---

## 📡 Endpoints del BFF

### 1. Consulta de Cuenta / Saldo (Canal Web)

* **Método:** `GET`
* **URL:** `https://localhost:8443/bff/web/cuenta/101`
* **Headers:**
  * `Authorization: Bearer <TOKEN_AUTH0>`
  * `X-Canal: WEB`

### 2. Consulta de Saldo (Canal ATM)

* **Método:** `GET`
* **URL:** `https://localhost:8443/bff/atm/cuenta/101/saldo`
* **Headers:**
  * `Authorization: Bearer <TOKEN_AUTH0>`
  * `X-Canal: ATM`
  * `X-Operacion: CONSULTAR_SALDO`

### 3. Retiro de Fondos con Evento Asíncrono (Canal ATM)

* **Método:** `POST`
* **URL:** `https://localhost:8443/bff/atm/cuenta/101/retiro`
* **Headers:**
  * `Authorization: Bearer <TOKEN_AUTH0>`
  * `X-Canal: ATM`
  * `X-Operacion: RETIRO`
* **Body (raw JSON):**

```json
{
  "monto": 500
}
```

---

## 📨 Verificación de Mensajería Asíncrona (Apache Kafka)

El BFF actúa como productor enviando eventos al tópico `transacciones-topic` al ejecutar operaciones de débito. Para validar la recepción del evento desde la terminal del contenedor:

```bash
docker exec -it bank-legacy-batch-kafka-1 kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transacciones-topic --from-beginning
```

**Salida esperada:**

```text
Retiro de 500 procesado exitosamente para la cuenta 101
```

---

## 🛡️ Tolerancia a Fallos (Resilience4j - Circuit Breaker)

El servicio cuenta con la instancia `bankServiceCB` que intercepta fallos de conectividad hacia la base de datos o servicios dependientes mediante un `fallbackMethod`.

**Prueba de simulación de fallo:**

1. Detener el contenedor de base de datos:
```bash
   docker stop mysql-db
```
2. Ejecutar la petición `POST` de retiro en Postman.
3. El sistema no colapsa (no genera HTTP 500), devolviendo una respuesta controlada con estado `HTTP 200 OK`:

```json
   {
     "cuentaId": 101,
     "montoRetirado": 500,
     "saldoRestante": 0,
     "estado": "FALLIDO",
     "mensaje": "Servicio no disponible. Intente mas tarde."
   }
```
4. Restaurar el servicio:
```bash
   docker start mysql-db
```

---

## 🛑 Detención de la Infraestructura

Para detener todos los servicios y desmontar las redes virtuales:

```bash
docker-compose down
```

---

**Autor:** Alexander Augusto Diaz Hernandez
**Asignatura:** Arquitectura de Microservicios
**Entrega:** Experiencia 3 / Semana 8