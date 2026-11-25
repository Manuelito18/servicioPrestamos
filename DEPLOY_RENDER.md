# Despliegue de PrestaControl en Render.com via Docker Hub

Guia completa para publicar **prestamoservice** en Render.com usando una imagen de Docker Hub, **sin GitHub**.

---

## 1. Datos reales del proyecto

| Dato | Valor |
| :--- | :--- |
| GroupId | `com.fernandez_terrones` |
| ArtifactId | `prestamoservice` |
| Version | `1.0.0` |
| Package base | `com.fernandez_terrones.prestamoservice` |
| Clase principal | `com.fernandez_terrones.prestamoservice.PrestamoServiceApplication` |
| JAR generado | `target/prestamoservice-1.0.0.jar` |
| Java | 17 |
| Spring Boot | 3.5.16 |
| Base de datos | PostgreSQL |
| Tablas | `usuario`, `prestamo` |
| Puerto local | `8080` (en Render se lee de `PORT`) |
| Imagen Docker | `docker.io/<usuario_dockerhub>/prestamoservice:latest` |
| Nombre sugerido en Render | `prestamoservice` |

Endpoints desplegados:

| Metodo | Ruta | Acceso |
| :--- | :--- | :--- |
| POST | `/auth/register` | Publico |
| POST | `/auth/login` | Publico |
| GET | `/prestamos` | Bearer JWT |
| GET | `/prestamos/{id}` | Bearer JWT |
| POST | `/prestamos` | Bearer JWT |
| PUT | `/prestamos/{id}` | Bearer JWT |
| DELETE | `/prestamos/{id}` | Bearer JWT |
| GET | `/prestamos/resumen` | Bearer JWT |

---

## 2. Requisitos previos

- Cuenta en [Docker Hub](https://hub.docker.com/).
- Docker Desktop instalado y con `buildx` disponible.
- Cuenta en [Render.com](https://render.com/).
- `curl` y, opcionalmente, `jq` para verificar el API.
- Opcional para probar en local: JDK 17, Maven 3.9+ y PostgreSQL.

Verifica las herramientas:

```bash
docker --version
docker buildx version
curl --version
```

---

## 3. Compilar y validar en local (opcional pero recomendado)

```bash
cd prestamoservice
mvn clean package -DskipTests
```

El artefacto queda en `target/prestamoservice-1.0.0.jar`.

Para ejecutarlo en local necesitas un PostgreSQL y estas variables:

```bash
export PORT=8080
export DB_URL="jdbc:postgresql://localhost:5432/prestamoservice"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
export JWT_SECRET="cambia-este-secreto-por-uno-de-al-menos-32-caracteres"
export JWT_EXPIRATION=86400000

java -jar target/prestamoservice-1.0.0.jar
```

Hibernate crea/actualiza las tablas automaticamente gracias a `spring.jpa.hibernate.ddl-auto=update`.

---

## 4. Crear la base de datos PostgreSQL en Render

1. En el dashboard de Render pulsa **New +** → **Postgres**.
2. Completa:
   - **Name:** `prestamoservice-db`
   - **Database:** `prestamoservice`
   - **User:** `prestamoservice`
   - **Region:** la misma region que usaras para el Web Service (por ejemplo `Oregon (US West)`).
   - **Plan:** `Free` (o el que corresponda).
3. Pulsa **Create Database** y espera a que el estado sea **Available**.
4. En la seccion **Connections** copia estos datos:
   - **Hostname** (ej. `dpg-d1abc2def3-xxxx-a`)
   - **Port** (`5432`)
   - **Database** (`prestamoservice`)
   - **Username** (`prestamoservice`)
   - **Password**
   - **Internal Database URL**
   - **External Database URL**

### Convertir la URL de Render a formato JDBC

Render entrega algo asi:

```text
postgresql://prestamoservice:MI_PASSWORD@dpg-d1abc2def3-xxxx-a/prestamoservice
```

El driver de PostgreSQL necesita el prefijo `jdbc:` y el puerto explicito:

```text
jdbc:postgresql://dpg-d1abc2def3-xxxx-a:5432/prestamoservice
```

- **Conexion interna** (Web Service y base de datos en la **misma region**): usa el hostname interno terminado en `-a`, sin SSL.
- **Conexion externa** (desde tu PC o desde otra region): usa el hostname externo y agrega `?sslmode=require`:

```text
jdbc:postgresql://dpg-d1abc2def3-xxxx-a.oregon-postgres.render.com:5432/prestamoservice?sslmode=require
```

> Importante: dentro de Render usa siempre la **Internal Database URL**. La externa es mas lenta y puede no estar disponible segun la configuracion de red.

---

## 5. Generar el secreto JWT

`JWT_SECRET` debe tener **al menos 32 caracteres** (256 bits para HS256). Genera uno aleatorio:

```bash
openssl rand -base64 48
```

Ejemplo de salida valida (no uses esta en produccion):

```text
Q2hlY2tvdXQyMDI2UHJlc3RhQ29udHJvbFNlY3JldG9TdXBlclNlZ3VybzEyMzQ1Ng==
```

Guarda el valor: lo usaras en las variables de entorno de Render.

---

## 6. Construir y publicar la imagen en Docker Hub

### 6.1 Iniciar sesion

```bash
docker login
```

### 6.2 Construir para linux/amd64 y publicar

Render ejecuta contenedores `linux/amd64`. Aunque estes en Windows, macOS ARM o Linux, usa `buildx`:

```bash
docker buildx build --platform linux/amd64 \
  -t <usuario_dockerhub>/prestamoservice:latest \
  --push .
```

Ejemplo concreto si tu usuario de Docker Hub es `manuelito`:

```bash
docker buildx build --platform linux/amd64 \
  -t manuelito/prestamoservice:latest \
  --push .
```

La imagen final usa dos etapas:

1. `maven:3.9-eclipse-temurin-17-alpine` compila el JAR.
2. `eclipse-temurin:17-jre-alpine` solo ejecuta el JAR con un usuario sin privilegios.

### 6.3 Verificar la imagen publicada

```bash
docker buildx imagetools inspect <usuario_dockerhub>/prestamoservice:latest
```

Debe mostrar `linux/amd64` entre las plataformas.

### 6.4 Probar la imagen en local (opcional)

Si tienes PostgreSQL local:

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/prestamoservice" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="postgres" \
  -e JWT_SECRET="un-secreto-de-al-menos-32-caracteres-para-probar" \
  -e JWT_EXPIRATION=86400000 \
  <usuario_dockerhub>/prestamoservice:latest
```

Si apuntas a la base externa de Render:

```bash
-e DB_URL="jdbc:postgresql://dpg-d1abc2def3-xxxx-a.oregon-postgres.render.com:5432/prestamoservice?sslmode=require"
```

---

## 7. Crear el Web Service en Render con la imagen existente

1. En Render pulsa **New +** → **Web Service**.
2. Elige **Deploy an existing image from a registry**.
3. Completa:
   - **Image URL:** `docker.io/<usuario_dockerhub>/prestamoservice:latest`
   - **Name:** `prestamoservice`
   - **Region:** la misma de la base de datos.
   - **Instance Type:** `Free` (o el que corresponda).
4. Si el repositorio de Docker Hub es **privado**, agrega las credenciales de Docker Hub cuando Render las solicite. Para simplificar, puedes publicar la imagen como publica.
5. En **Environment Variables** agrega exactamente:

| Variable | Valor |
| :--- | :--- |
| `PORT` | `8080` |
| `DB_URL` | `jdbc:postgresql://dpg-d1abc2def3-xxxx-a:5432/prestamoservice` |
| `DB_USERNAME` | `prestamoservice` |
| `DB_PASSWORD` | La password real de la base en Render |
| `JWT_SECRET` | El secreto generado en el paso 5 |
| `JWT_EXPIRATION` | `86400000` |

Notas:

- `JWT_EXPIRATION` esta en **milisegundos**. `86400000` = 24 horas.
- No definas `SPRING_DATASOURCE_*`: la aplicacion lee exclusivamente `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
- `PORT=8080` coincide con el `EXPOSE 8080` del Dockerfile. Render tambien inyecta su propio `PORT`; el entrypoint usa `${PORT:-8080}`, por lo que funciona en ambos casos.
- **Health Check Path:** dejalo vacio. Las rutas del API responden `401` sin token y un health check HTTP las interpretaria como fallo. Render verificara el puerto por TCP.
- No configures Build Command ni Start Command: la imagen ya trae su `ENTRYPOINT`.

6. Pulsa **Create Web Service** y espera a que el estado sea **Live**.

La URL publica tendra la forma:

```text
https://prestamoservice-xxxx.onrender.com
```

---

## 8. Verificar el despliegue con curl

Define la URL base:

```bash
export BASE_URL="https://prestamoservice-xxxx.onrender.com"
```

### 8.1 Registrar prestamista

```bash
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Juan Perez","email":"juan@mail.com","password":"Password123"}' | jq
```

Respuesta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 8.2 Iniciar sesion y capturar el token

```bash
TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@mail.com","password":"Password123"}' | jq -r '.token')

echo "$TOKEN"
```

### 8.3 Crear un prestamo

```bash
curl -s -X POST "$BASE_URL/prestamos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "Carlos Gomez",
    "dni": "72458910",
    "monto": 1000.00,
    "interes": 15.00,
    "fechaVencimiento": "2026-12-25",
    "pagado": false
  }' | tee prestamo.json | jq

PRESTAMO_ID=$(jq -r '.id' prestamo.json)
```

Respuesta esperada:

```json
{
  "id": 1,
  "cliente": "Carlos Gomez",
  "dni": "72458910",
  "monto": 1000.00,
  "interes": 15.00,
  "totalCobrar": 1150.00,
  "fechaPrestamo": "2026-11-25",
  "fechaVencimiento": "2026-12-25",
  "pagado": false
}
```

### 8.4 Listar prestamos

```bash
curl -s "$BASE_URL/prestamos" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 8.5 Obtener un prestamo

```bash
curl -s "$BASE_URL/prestamos/$PRESTAMO_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 8.6 Actualizar / marcar como pagado

```bash
curl -s -X PUT "$BASE_URL/prestamos/$PRESTAMO_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "Carlos Gomez",
    "dni": "72458910",
    "monto": 1000.00,
    "interes": 15.00,
    "fechaVencimiento": "2026-12-25",
    "pagado": true
  }' | jq
```

### 8.7 Resumen de cartera

```bash
curl -s "$BASE_URL/prestamos/resumen" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Respuesta esperada:

```json
{
  "capitalPrestado": 1000.00,
  "totalPorCobrar": 1150.00,
  "gananciaEsperada": 150.00
}
```

### 8.8 Eliminar un prestamo

```bash
curl -i -X DELETE "$BASE_URL/prestamos/$PRESTAMO_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Debe responder `HTTP/1.1 204 No Content`.

### 8.9 Validar el 401 sin token

```bash
curl -i "$BASE_URL/prestamos"
```

Respuesta esperada:

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json
```

```json
{
  "timestamp": "2026-11-25T15:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "No autorizado: debe enviar un token JWT valido en la cabecera Authorization: Bearer <token>",
  "path": "/prestamos"
}
```

### 8.10 Validar aislamiento entre usuarios

Registra un segundo prestamista:

```bash
TOKEN2=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Maria Lopez","email":"maria@mail.com","password":"Password123"}' | jq -r '.token')
```

Intenta leer el prestamo de Juan con el token de Maria:

```bash
curl -i "$BASE_URL/prestamos/$PRESTAMO_ID" \
  -H "Authorization: Bearer $TOKEN2"
```

Debe responder `404 Not Found` (el recurso no pertenece al usuario del token).

---

## 9. Actualizar la aplicacion desplegada

1. Cambia el codigo.
2. Vuelve a compilar y publicar la imagen:

```bash
docker buildx build --platform linux/amd64 \
  -t <usuario_dockerhub>/prestamoservice:latest \
  --push .
```

3. En Render entra al servicio `prestamoservice` → **Manual Deploy** → **Deploy latest image**.

Recomendacion: ademas de `:latest`, publica una etiqueta de version para poder volver atras:

```bash
docker buildx build --platform linux/amd64 \
  -t <usuario_dockerhub>/prestamoservice:1.0.0 \
  -t <usuario_dockerhub>/prestamoservice:latest \
  --push .
```

---

## 10. Solucion de problemas

| Sintoma | Causa probable | Solucion |
| :--- | :--- | :--- |
| El deploy falla y el log dice `Could not resolve placeholder 'DB_URL'` | Falta una variable de entorno | Define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `JWT_EXPIRATION`. |
| `JWT_SECRET debe tener al menos 32 caracteres` | Secreto demasiado corto | Genera uno con `openssl rand -base64 48`. |
| `Connection refused` o `UnknownHostException` hacia la BD | URL interna usada fuera de Render, o distinta region | Usa la **Internal Database URL** y la misma region; desde fuera usa la externa con `?sslmode=require`. |
| `The connection attempt failed` con SSL | Falta `sslmode=require` en conexion externa | Agrega `?sslmode=require` al final del `DB_URL`. |
| La app arranca pero Render marca el servicio caido | El puerto no coincide | Revisa el log: debe decir `Tomcat started on port 8080`. Manten `PORT=8080` o deja que Render inyecte el suyo. |
| `exec format error` al iniciar el contenedor | Imagen construida para otra arquitectura | Reconstruye con `--platform linux/amd64`. |
| Siempre responde 401 con un token recien creado | Header mal formado o secreto distinto entre despliegues | Usa `Authorization: Bearer <token>` y no cambies `JWT_SECRET` sin volver a iniciar sesion. |
| `403` o `404` al acceder a un prestamo | El prestamo pertenece a otro usuario | Es el comportamiento esperado: el filtrado es por usuario del JWT. |
| Error 500 al arrancar por la base | Credenciales incorrectas o base inexistente | Verifica usuario, password y nombre de la base en Render. |
| Primera peticion tarda ~30-60 s | El plan Free suspende el servicio por inactividad | Es normal; la instancia despierta con la primera peticion. |
| La base gratuita desaparece | Render elimina las bases Free tras el periodo de prueba vigente | Revisa la politica actual de Render o usa un plan pago. |
| No se crean las tablas | `ddl-auto` no es `update` o el usuario no tiene permisos | Confirma `spring.jpa.hibernate.ddl-auto=update` y los permisos del usuario de la BD. |

---

## 11. Checklist final

- [ ] `mvn clean package -DskipTests` termina con `BUILD SUCCESS`.
- [ ] Imagen publicada en Docker Hub como `linux/amd64`.
- [ ] PostgreSQL creado en Render y estado **Available**.
- [ ] Variables `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION` configuradas.
- [ ] Web Service creado con **Deploy an existing image from a registry**.
- [ ] Servicio en estado **Live**.
- [ ] `POST /auth/register` y `POST /auth/login` devuelven `token`.
- [ ] `POST /prestamos` devuelve `201` y un `totalCobrar` correcto.
- [ ] `GET /prestamos/resumen` devuelve las tres cifras.
- [ ] `GET /prestamos` sin token devuelve `401` en JSON.
- [ ] Un segundo usuario no puede ver prestamos del primero (`404`).
- [ ] En Postman, `{{base_url}}` apunta a la URL de Render y el flujo Register/Login guarda `{{token}}` automaticamente.
