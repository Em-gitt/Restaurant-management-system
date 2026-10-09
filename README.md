# 🍽️ Restaurant System

Sistema backend modular para la gestión integral de restaurantes basado en una arquitectura de microservicios con **Spring Boot**, **Spring Security**, autenticación sin estado mediante **JWT** y persistencia en **PostgreSQL**.

---

## 🏗️ Arquitectura y Microservicios

El repositorio está estructurado como un proyecto multi-módulo de Maven (`com.unipoli:restaurant-system`):

| Microservicio | Puerto por Defecto | Base de Datos | Descripción |
| :--- | :---: | :---: | :--- |
| **`auth-service`** | `8081` | `authdb` | Registro, autenticación de usuarios y generación de JWT con BCrypt. |
| **`table-service`** | `8082` | `tabledb` | Gestión de mesas y actualización de estados del restaurante. |
| **`menu-service`** | `8083` | `menudb` | Catálogo de platos, categorías y menú interactivo. |

---

## 🛠️ Tecnologías Requeridas

- **Java**: 21 o 25 (OpenJDK recomendado)
- **Framework**: Spring Boot 4.x
- **Build Tool**: Apache Maven 3.9+
- **Base de Datos**: PostgreSQL 14+
- **Seguridad**: Spring Security 6+ y JJWT (`io.jsonwebtoken:jjwt-api:0.12.6`)

---

## 🔐 Configuración de Variables de Entorno

Por motivos de seguridad, las contraseñas de PostgreSQL y las claves JWT **no se almacenan en el código fuente**. Deben definirse mediante variables de entorno antes de ejecutar cada servicio.

Puedes consultar la plantilla base en [`.env.example`](.env.example).

### Variables Disponibles

| Variable | Descripción | Valor por defecto |
| :--- | :--- | :--- |
| `SERVER_PORT` | Puerto HTTP del servicio | `8081` (auth), `8082` (table), `8083` (menu) |
| `DB_AUTH_URL` | URL JDBC de auth-service | `jdbc:postgresql://localhost:5432/authdb` |
| `DB_AUTH_USERNAME` | Usuario PostgreSQL de auth | `postgres` |
| `DB_AUTH_PASSWORD` | Contraseña PostgreSQL de auth | *(vacío por defecto)* |
| `DB_TABLE_URL` | URL JDBC de table-service | `jdbc:postgresql://localhost:5432/tabledb` |
| `DB_TABLE_USERNAME` | Usuario PostgreSQL de table | `table_user` |
| `DB_TABLE_PASSWORD` | Contraseña PostgreSQL de table | *(vacío por defecto)* |
| `DB_MENU_URL` | URL JDBC de menu-service | `jdbc:postgresql://localhost:5432/menudb` |
| `DB_MENU_USERNAME` | Usuario PostgreSQL de menu | `menu_user` |
| `DB_MENU_PASSWORD` | Contraseña PostgreSQL de menu | *(vacío por defecto)* |
| `JWT_SECRET` | Clave secreta HMAC-SHA256 (mín. 256 bits) | Clave por defecto de desarrollo |
| `JWT_EXPIRATION` | Tiempo de vida del token en milisegundos | `3600000` (1 hora) |

---

## 🚀 Puesta en Marcha

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/restaurant-system.git
cd restaurant-system
```

### 2. Configurar bases de datos en PostgreSQL
Crea las bases de datos requeridas en tu instancia local o remota de PostgreSQL:
```sql
CREATE DATABASE authdb;
CREATE DATABASE tabledb;
CREATE DATABASE menudb;
```

### 3. Compilar el proyecto multi-módulo
Desde la raíz del proyecto:
```bash
mvn clean install -DskipTests
```

### 4. Ejecutar cada microservicio

#### Opción A: Exportando variables en tu terminal
```bash
export DB_AUTH_PASSWORD="tu_password_postgres"
export JWT_SECRET="tu_clave_secreta_jwt_muy_segura_de_al_menos_256_bits_aqui"

# Ejecutar auth-service
cd auth-service
./mvnw spring-boot:run
```

#### Opción B: Pasando propiedades directamente a Spring Boot
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password=tu_password --jwt.secret=tu_secreto"
```

---

## 📡 Endpoints Principales

### Auth Service (`/auth`)
- `POST /auth/register`: Registro de nuevos usuarios.
- `POST /auth/login`: Autenticación y obtención de token Bearer JWT.

### Table Service (`/api/v1/tables`)
- `GET /api/v1/tables`: Lista de mesas y sus estados. *(Requiere cabecera `Authorization: Bearer <TOKEN>`)*
- `PUT /api/v1/tables/{tableId}/status`: Cambio de estado de la mesa. *(Requiere `Authorization: Bearer <TOKEN>`)*

### Menu Service (`/api/v1/menu`)
- `GET /api/v1/menu/dishes`: Catálogo completo de platos. *(Requiere `Authorization: Bearer <TOKEN>`)*
- `GET /api/v1/menu/categories`: Listado de categorías. *(Requiere `Authorization: Bearer <TOKEN>`)*
- `POST /api/v1/menu/dishes`: Creación de un plato. *(Requiere `Authorization: Bearer <TOKEN>`)*

---

## 🛡️ Seguridad

- Todos los tokens deben enviarse en la cabecera HTTP:
  ```http
  Authorization: Bearer <TU_JWT_TOKEN>
  ```
- CORS viene configurado por defecto para admitir clientes en `http://localhost:5500` y `http://127.0.0.1:5500`.

---

## 📄 Licencia

Este proyecto es de código abierto bajo la licencia MIT.
