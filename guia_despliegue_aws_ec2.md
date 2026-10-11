# Guía de Despliegue en la Nube (AWS EC2) - Bank Legacy Batch

Este documento detalla el procedimiento técnico paso a paso para el aprovisionamiento, configuración, resolución de dependencias y despliegue en producción de la arquitectura de microservicios Bank Legacy Batch en la nube de Amazon Web Services (AWS) utilizando contenedores Docker.

---

## 1. Requisitos previos y herramientas

- **Cuenta de AWS:** Acceso con permisos para crear y modificar instancias EC2, volúmenes EBS y Security Groups.
- **Cliente SSH / Terminal:** MobaXterm, PuTTY o consola nativa de terminal con soporte para llaves criptográficas `.pem`.
- **Código fuente:** Repositorio en GitHub con la solución completa (`bank-legacy-batch`, `eureka_server`, `config_server` y `docker-compose.yml`).
- **Postman:** Para la ejecución de pruebas de integración contra la IP pública del servidor.

---

## 2. Aprovisionamiento de la infraestructura en AWS EC2

### 2.1 Lanzamiento de la instancia

1. Inicia sesión en la consola de administración de AWS y navega a **EC2 > Instances > Launch instances**.
2. **Nombre de la instancia:** `bank-legacy-batch-prod`.
3. **Imagen del sistema operativo (AMI):** Ubuntu Server 24.04 LTS o Ubuntu Server 22.04 LTS (64-bit x86).
4. **Tipo de instancia:** `t2.medium` (2 vCPU, 4 GiB de RAM). Esta capacidad es necesaria para soportar simultáneamente la JVM de los microservicios, el broker Kafka, Zookeeper y MySQL.
5. **Par de llaves (Key pair):** Selecciona una llave existente o genera una nueva en formato `.pem` para autenticación SSH.

### 2.2 Configuración del almacenamiento (EBS)

- **Tamaño del volumen raíz:** Modifica el valor predeterminado de 8 GiB a **20 GiB** tipo **gp3**.

> **Nota técnica:** Las imágenes base de JDK (`eclipse-temurin`), Kafka, Zookeeper y MySQL consumen más de 6 GiB desempaquetadas. Un disco de 8 GiB genera errores críticos de tipo `ResourceExhausted: no space left on device` al compilar los contenedores.

### 2.3 Reglas de red y grupos de seguridad (Security Groups)

Configura un grupo de seguridad con las siguientes reglas de entrada (Inbound Rules):

| Tipo       | Protocolo | Puerto | Origen                  | Propósito                                      |
|------------|-----------|--------|-------------------------|------------------------------------------------|
| SSH        | TCP       | 22     | Mi IP (o `0.0.0.0/0`)   | Administración remota por consola              |
| Custom TCP | TCP       | 8443   | `0.0.0.0/0` (cualquier lugar) | Entrada segura HTTPS al BFF (`bank-legacy-batch`) |

---

## 3. Preparación y configuración del sistema operativo

Conéctate a la instancia mediante SSH utilizando la IP pública asignada:

```bash
ssh -i "tu-llave.pem" ubuntu@<IP_PUBLICA_EC2>
```

### 3.1 Actualización del sistema e instalación de dependencias

Actualiza los índices de paquetes e instala OpenJDK 17, Git, Docker y el plugin oficial de Docker Compose V2:

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-17-jdk git docker.io docker-compose-plugin
```

### 3.2 Gestión de privilegios de Docker

Para ejecutar comandos de Docker sin requerir `sudo`:

```bash
sudo usermod -aG docker ubuntu
sudo reboot
```

Espera aproximadamente 30 segundos y vuelve a conectarte por terminal para que los cambios de grupo tomen efecto.

### 3.3 Verificación del almacenamiento

Confirma que el sistema de archivos reconoce la totalidad del volumen expandido:

```bash
df -h | grep "/dev/root"
```

El valor en la columna `Size` debe reflejar aproximadamente `19G` o `20G`.

---

## 4. Obtención y normalización del código fuente

### 4.1 Clonación del repositorio

Clona el proyecto en el directorio home del usuario `ubuntu`:

```bash
cd ~
git clone https://github.com/Alex07091207/bank-legacy-batch.git
cd bank-legacy-batch
```

### 4.2 Normalización de nombres de archivos (Linux vs. Windows)

Los entornos de desarrollo basados en Windows no distinguen mayúsculas de minúsculas, mientras que los entornos basados en Linux (como Ubuntu) sí lo hacen. Si las referencias en el Dockerfile apuntan a `src/main/resources/data/`, debes garantizar que la carpeta exista en minúsculas en el servidor:

```bash
# Garantizar la existencia de la carpeta en minúsculas
mkdir -p bank-legacy-batch/src/main/resources/data

# Migrar archivos si el repositorio clonó una carpeta 'Data' con mayúscula
if [ -d "bank-legacy-batch/src/main/resources/Data" ]; then
    cp -a bank-legacy-batch/src/main/resources/Data/. bank-legacy-batch/src/main/resources/data/
    rm -rf bank-legacy-batch/src/main/resources/Data
fi
```

---

## 5. Compilación y construcción de contenedores

Para minimizar el consumo de CPU y memoria RAM durante el build, compila los paquetes `.jar` omitiendo la ejecución de las suites de pruebas unitarias.

### 5.1 Empaquetado con Maven Wrapper

```bash
# 1. Compilar microservicio principal (BFF + Batch)
cd ~/bank-legacy-batch/bank-legacy-batch
chmod +x mvnw
./mvnw clean package -Dmaven.test.skip=true

# 2. Compilar Eureka Server
cd ~/bank-legacy-batch/eureka_server
chmod +x mvnw
./mvnw clean package -Dmaven.test.skip=true

# 3. Compilar Config Server
cd ~/bank-legacy-batch/config_server
chmod +x mvnw
./mvnw clean package -Dmaven.test.skip=true
```

### 5.2 Despliegue con Docker Compose V2

Vuelve a la raíz donde se ubica el archivo `docker-compose.yml` y construye/levanta los contenedores en modo *detached* (segundo plano):

```bash
cd ~/bank-legacy-batch
docker compose up --build -d
```

---

## 6. Monitoreo, verificación y pruebas en producción

### 6.1 Inspección del estado de los contenedores

Verifica que los 6 contenedores estén en estado `Up`:

```bash
docker ps
```

Contenedores esperados en ejecución:

- `bank-legacy-batch` (expuesto en `0.0.0.0:8443->8443/tcp`)
- `eureka-server` (expuesto en `0.0.0.0:8761->8761/tcp`)
- `config-server` (expuesto en `0.0.0.0:8888->8888/tcp`)
- `mysql-db` (expuesto en `0.0.0.0:3307->3306/tcp`)
- `bank-legacy-batch-kafka-1` (expuesto en `0.0.0.0:9092->9092/tcp`)
- `bank-legacy-batch-zookeeper-1` (expuesto en `0.0.0.0:2181->2181/tcp`)

### 6.2 Revisión de logs de inicialización

Confirma que el BFF se haya conectado satisfactoriamente a Eureka y a MySQL:

```bash
docker logs -f bank-legacy-batch
```

### 6.3 Ejecución de pruebas de integración con Postman

- **Método:** `GET`
- **URL:** `https://<IP_PUBLICA_EC2>:8443/bff/atm/cuenta/137/saldo`
- **Configuración SSL:** En Postman, ve a **Settings > General** y desactiva **SSL certificate verification** (debido a que se utiliza un certificado autofirmado `keystore.p12`).
- **Headers obligatorios:**
  - `X-Canal`: `ATM`
  - `X-Operacion`: `CONSULTA_SALDO` (o el string exacto validado por `BffSecurityService.java`)
  - `Authorization`: `Bearer <TOKEN_JWT_VALIDO>`
- **Resultado esperado:** Código HTTP `200 OK` con el payload JSON del saldo consultado.

---

## 7. Troubleshooting (resolución de problemas comunes)

### Error `ResourceExhausted: no space left on device`

- **Causa:** El volumen EBS se quedó sin bloques disponibles durante la extracción de capas Docker.
- **Solución:** Amplía el volumen EBS a 20 GiB desde la consola de AWS (**EC2 > Volumes > Modify Volume**) y reinicia la máquina con `sudo reboot`.

### Error `COPY failed: file not found in build context: /src/main/resources/data`

- **Causa:** Diferencia entre mayúsculas y minúsculas (`Data` vs `data`) o directorio no rastreado por Git.
- **Solución:** Crea la carpeta explícitamente con `mkdir -p bank-legacy-batch/src/main/resources/data` antes de ejecutar `docker compose up --build`.

### Error `HTTP 500 Internal Server Error` con causa `Operación no autorizada`

- **Causa:** El encabezado `X-Operacion` no coincide exactamente con el valor esperado en el controlador del BFF.
- **Solución:** Revisa el método de entrada en `AtmBffController.java` y envía el valor string idéntico en los headers de Postman.
