# Instrucciones de levantamiento local: bank-legacy-batch

## Prerrequisitos locales

- Java Development Kit (JDK) 17
- Docker y Docker Compose V2
- Git
- Postman (con verificación SSL desactivada para pruebas locales)

---

## Paso 1: Descarga y compilación del código

1. Clona el repositorio oficial en tu máquina local:

```bash
git clone https://github.com/Alex07091207/bank-legacy-batch.git
cd bank-legacy-batch
```

2. Compila el microservicio principal y sus servidores de soporte omitiendo las pruebas unitarias para agilizar el proceso:

```bash
./mvnw clean package "-Dmaven.test.skip=true"
```

> **Nota:** Repite este comando dentro de las carpetas `eureka_server` y `config_server` si sus Dockerfiles requieren los archivos `.jar` precompilados.

---

## Paso 2: Levantamiento de la infraestructura

1. Ubícate en la raíz del proyecto donde reside el archivo `docker-compose.yml`.
2. Construye y levanta los servicios en segundo plano:

```bash
docker compose up --build -d
```

3. Verifica el estado de los contenedores asegurándote de que el BFF, Eureka, Config Server, MySQL, Kafka y Zookeeper estén activos (Estado: `Up`):

```bash
docker ps
```

4. Monitorea el arranque del microservicio principal para asegurar que se conectó a la base de datos y a Eureka:

```bash
docker logs -f bank-legacy-batch
```

---

## Paso 3: Ejecución de pruebas en Postman

1. Abre Postman. Ve a **Settings > General** y asegúrate de apagar (OFF) la opción **SSL certificate verification**.
2. Configura una nueva petición **GET** hacia la URL:

   ```
   https://localhost:8443/bff/atm/cuenta/137/saldo
   ```

3. En la pestaña **Headers**, configura estrictamente los siguientes valores para pasar la validación del `BffSecurityService`:
   - `X-Canal`: `ATM`
   - `X-Operacion`: `[VALOR_EXACTO_ESPERADO_POR_TU_JAVA]` (revisa la línea 27 de `AtmBffController.java`)
   - `Authorization`: `Bearer [TU_TOKEN_JWT]`

4. Haz clic en **Send**. Debes recibir una respuesta HTTP **200 OK** con el cuerpo JSON indicando el saldo.
