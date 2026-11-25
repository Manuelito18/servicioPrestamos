# Servicio de Préstamos (`servicioPrestamos`)

API / servicio backend desarrollado en Java para la gestión y administración de préstamos.

## Despliegue

El servicio está desplegado en Render y disponible en:

- **URL base:** [https://prestamoservice-ntgf.onrender.com/](https://prestamoservice-ntgf.onrender.com/)

> **Nota:** El servicio está en el plan gratuito de Render, la primera petición tras un periodo de inactividad puede tardar unos segundos mientras la instancia se reactiva.

## Tecnologías

- **Lenguaje:** Java
- **Framework:** Spring Boot *(ajusta si usas otro)*
- **Gestor de dependencias:** Maven / Gradle *(deja el que uses)*
- **Base de datos:** PostgreSQL / SQLite / MySQL *(ajusta según corresponda)*
- **Despliegue:** Render

## Instalación y ejecución local

1. Clona el repositorio:
   ```bash
   git clone git@github.com:Manuelito18/servicioPrestamos.git
   cd servicioPrestamos
   ```

2. Configura las variables de entorno necesarias (por ejemplo, en `application.properties` o un archivo `.env`):
   ```properties
   PORT=8080
   DATABASE_URL=tu_url_de_base_de_datos
   ```

3. Compila y ejecuta el proyecto:
   ```bash
   # Con Maven
   ./mvnw spring-boot:run

   # O con Gradle
   ./gradlew bootRun
   ```

## Endpoints principales

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/prestamos` | Lista todos los préstamos |
| `GET` | `/api/prestamos/{id}` | Obtiene un préstamo por ID |
| `POST` | `/api/prestamos` | Registra un nuevo préstamo |
| `PUT` | `/api/prestamos/{id}` | Actualiza un préstamo existente |
| `DELETE` | `/api/prestamos/{id}` | Elimina un préstamo |

## Autor

- **Manuel** — [@Manuelito18](https://github.com/Manuelito18)

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo `LICENSE` para más detalles.
