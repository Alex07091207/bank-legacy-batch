# Proyecto Semana 6 - Implementación de Microservicios y Seguridad en la Nube con Spring Cloud

**Desarrollo Backend III - PBY2203**

Este repositorio contiene la evolución del sistema `bank-legacy-batch` hacia una arquitectura de microservicios distribuida en la nube utilizando Spring Cloud. Se han implementado patrones de diseño avanzados para garantizar la escalabilidad, el descubrimiento dinámico de servicios, la configuración centralizada y la tolerancia a fallos.

## 1. Arquitectura del Sistema

La solución está dividida en tres proyectos independientes (microservicios) que interactúan entre sí:

### 1.1 Config Server (`config-server`)

* **Puerto:** `8888`
* **Función:** Servidor centralizado de configuración. Permite que los microservicios obtengan sus credenciales, puertos y propiedades desde un único punto (utilizando el perfil `native` para lectura local).

### 1.2 Service Discovery (`eureka-server`)

* **Puerto:** `8761`
* **Función:** Servidor de descubrimiento de servicios basado en Netflix Eureka. Mantiene un registro dinámico de todos los microservicios activos en la red.

### 1.3 Microservicio de Negocio (`bank-legacy-batch`)

* **Puerto:** `8443` (HTTPS)
* **Función:** Expone los datos bancarios migrados a través del patrón Backend for Frontend (BFF). Se conecta automáticamente al Config Server al arrancar y se registra en Eureka.

## 2. Tecnologías y Patrones Implementados

* **Spring Cloud Config:** Para la externalización y centralización de la configuración (`application.yml`).
* **Spring Cloud Netflix Eureka:** Para el registro y descubrimiento automático del microservicio cliente.
* **Resilience4j (Circuit Breaker):** Implementación de tolerancia a fallos. Si la base de datos MySQL presenta una interrupción o latencia excesiva, el sistema no colapsa, sino que intercepta el fallo y devuelve una respuesta de contingencia (Fallback method) manteniendo un código de estado `200 OK`.
* **Spring Security:** Seguridad perimetral con autenticación HTTP Basic Auth y validación de canales mediante cabeceras HTTP (`X-Canal`).

## 3. Instrucciones de Ejecución

El orden de encendido es **estrictamente obligatorio** para el correcto funcionamiento del ecosistema.

### 3.1 Paso 1: Iniciar el Servidor de Configuración

Abrir una terminal en la carpeta `config-server` y ejecutar:

```bash
.\mvnw spring-boot:run
```

**Verificar en:** [http://localhost:8888/bank-legacy-batch/default](http://localhost:8888/bank-legacy-batch/default)

### 3.2 Paso 2: Iniciar el Servidor Eureka

Abrir una terminal en la carpeta `eureka-server` y ejecutar:

```bash
.\mvnw spring-boot:run
```

**Verificar dashboard en:** [http://localhost:8761](http://localhost:8761)

### 3.3 Paso 3: Iniciar el Microservicio Principal

Abrir una terminal en la carpeta `bank-legacy-batch` y ejecutar:

```bash
.\mvnw spring-boot:run
```

**Verificar** que la instancia `BANK-LEGACY-BATCH` aparezca con estado `UP` en el dashboard de Eureka.

## 4. Pruebas y Endpoints (Postman)

Todas las peticiones deben realizarse bajo **HTTPS** al puerto `8443`. Se debe deshabilitar la validación de certificados SSL en Postman si se utilizan certificados autofirmados.

### 4.1 Prueba Exitosa de API (200 OK)

| Parámetro | Valor |
|-----------|-------|
| **Endpoint** | `GET https://localhost:8443/bff/web/cuenta/101` |
| **Headers** | `X-Canal: WEB` |
| **Authorization** | Basic Auth |
| **Username** | `web_client` |
| **Password** | `WebPass2026!` |
| **Resultado Esperado** | JSON con la información consolidada de la cuenta |

### 4.2 Prueba de Seguridad (401 Unauthorized)

* Realizar la petición anterior enviando credenciales incorrectas o sin el header de autorización.
* **Resultado Esperado:** Código HTTP `401 Unauthorized`.

### 4.3 Prueba de Tolerancia a Fallos (Circuit Breaker)

* Detener el servicio de la base de datos (MySQL/WampServer).
* Enviar la misma petición exitosa del paso 4.1.
* **Resultado Esperado:** El servidor responde con código `200 OK` pero el cuerpo del JSON indica:
  * `"nombre": "Servicio Degradado"`
  * `"estado": "SISTEMA EN CONTINGENCIA"`

## 5. Información del Autor

* **Alumno:** Alexander Diaz
* **Asignatura:** Desarrollo Backend III (PBY2203)
* **Institución:** Duoc UC
