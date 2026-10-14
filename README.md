# 🍽️ Restaurant System

Sistema backend modular para la gestión integral de restaurantes basado en una arquitectura de microservicios con **Spring Boot 4**, **Spring Security**, autenticación sin estado mediante **JWT**, migraciones versionadas con **Flyway** y persistencia relacional en **PostgreSQL 18**.

---

## 🏗️ Arquitectura y Microservicios

El repositorio está estructurado como un proyecto multi-módulo de Maven (`com.unipoli:restaurant-system`), donde cada microservicio opera de forma desacoplada con su propia base de datos, esquema Flyway y reglas de negocio:

| Microservicio | Puerto | Base de Datos | Descripción |
| :--- | :---: | :---: | :--- |
| **`auth-service`** | `8081` | `authdb` | Registro de usuarios, autenticación y emisión de JWT (BCrypt, HS256). |
| **`table-service`** | `8082` | `tabledb` | Gestión de mesas y estados del salón (`FREE`, `OCCUPIED`, `RESERVED`, etc.). |
| **`menu-service`** | `8083` | `menudb` | Catálogo de platos, categorías, precios y disponibilidad. |
| **`employee-service`** | `8084` | `employeedb` | Administración de personal, turnos (`MORNING`, `AFTERNOON`, `NIGHT`, `SPLIT`) y roles. |
| **`inventory-service`** | `8085` | `inventorydb` | Ingredientes, recetas por plato, alertas de stock mínimo y deducción idempotente por orden. |
| **`order-service`** | `8086` | `orderdb` | Órdenes (`DINE_IN` y `DELIVERY`), cálculo de IVA (16%), propinas y orquestación con Cocina e Inventario. |
| **`kitchen-service`** | `8087` | `kitchendb` | Cola de comandas para cocina, ciclo de preparación (`PENDING` -> `PREPARING` -> `READY`) y tiempos estimados. |
| **`payment-service`** | `8088` | `paymentdb` | Procesamiento de pagos (`CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `TRANSFER`), cálculo de cambio e idempotencia. |
| **`delivery-service`** | `8089` | `deliverydb` | Integración de agregadores externos (Rappi, Uber Eats, DiDi Food), comisiones y webhook asegurado. |
| **`report-service`** | `8090` | `reportdb` | Proyecciones analíticas de ventas consolidadas, ingresos netos, comisiones, ticket promedio y alertas de inventario. |

---

## 🛠️ Tecnologías y Requisitos

- **Java**: 25 (OpenJDK `/usr/lib/jvm/java-25-openjdk`)
- **Framework**: Spring Boot 4.1.1 (Spring Framework 7)
- **Build Tool**: Apache Maven 3.9+
- **Base de Datos**: PostgreSQL 18 administrado mediante Podman Compose
- **Migraciones de BD**: Flyway Database Migrations (`spring.jpa.hibernate.ddl-auto=validate`)
- **Seguridad**: Spring Security con filtros JWT sin estado (`io.jsonwebtoken:jjwt-api:0.12.6`)
- **Pruebas**: JUnit 5, Mockito (`@MockitoBean`), Spring Boot Test & WebMvcTest

---

## 🔐 Configuración de Variables de Entorno

Por motivos de seguridad, las credenciales de PostgreSQL y las claves JWT **no están codificadas en duro en el código fuente**. Deben definirse mediante variables de entorno antes de ejecutar cada servicio.

Puedes consultar la plantilla base completa en [`.env.example`](.env.example).

### Resumen de Variables Clave

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SERVER_PORT` | Puerto HTTP del servicio individual | Definido por cada microservicio (8081 - 8090) |
| `JWT_SECRET` | Clave secreta HMAC-SHA256 (mínimo 256 bits) | Clave de desarrollo definida en `.env.example` |
| `DELIVERY_WEBHOOK_SECRET` | Token secreto para autenticar webhooks de delivery | `restaurant_delivery_default_webhook_secret_key_2026` |
| `DB_*_URL` | URLs JDBC individuales para cada microservicio | `jdbc:postgresql://localhost:5432/<dbname>` |
| `DB_*_USERNAME` | Usuarios independientes de cada servicio | `<service>_user` |
| `DB_*_PASSWORD` | Contraseñas de acceso a PostgreSQL | `<service>_pass` |

---

## 🚀 Puesta en Marcha

### 1. Iniciar Infraestructura de Base de Datos con Podman Compose

El proyecto incluye un archivo `compose.yaml` y el script `init-scripts/01-init-databases-and-users.sh` que inicializa automáticamente los 10 usuarios y las 10 bases de datos independientes en PostgreSQL 18:

```bash
# Iniciar la base de datos en segundo plano
podman compose up -d

# Comprobar el estado del contenedor
podman compose ps
```

### 2. Compilar y Ejecutar Pruebas de Todo el Reactor

Desde el directorio raíz:

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
mvn clean test
```

### 3. Ejecutar un Microservicio Específico

```bash
# Ejemplo: ejecutar order-service
cd order-service
mvn spring-boot:run
```

---

## 📡 Endpoints Principales por Microservicio

### 1. `auth-service` (Puerto: `8081`)
- `POST /auth/register`: Registro de usuario y encriptación de password con BCrypt.
- `POST /auth/login`: Autenticación y generación de token Bearer JWT.

### 2. `table-service` (Puerto: `8082`)
- `GET /api/v1/tables`: Listado y filtrado de mesas.
- `POST /api/v1/tables`: Creación de mesas (`ADMIN`).
- `PUT /api/v1/tables/{tableId}/status`: Cambio de estado (`FREE`, `OCCUPIED`, `RESERVED`, `OUT_OF_SERVICE`).

### 3. `menu-service` (Puerto: `8083`)
- `GET /api/v1/menu/dishes`: Catálogo de platos y disponibilidad.
- `POST /api/v1/menu/dishes`: Creación de platos (`ADMIN`).
- `GET /api/v1/menu/categories`: Listado de categorías gastronómicas.

### 4. `employee-service` (Puerto: `8084`)
- `GET /api/v1/employees`: Listado de empleados activos/inactivos o filtrados por turno (`MORNING`, `AFTERNOON`, `NIGHT`, `SPLIT`).
- `POST /api/v1/employees`: Alta de nuevo empleado (`ADMIN`).
- `PUT /api/v1/employees/{id}`: Modificación de datos y turno (`ADMIN`).
- `DELETE /api/v1/employees/{id}`: Baja lógica/desactivación de empleado (`ADMIN`).

### 5. `inventory-service` (Puerto: `8085`)
- `GET /api/v1/inventory/ingredients`: Listado de stock de ingredientes.
- `POST /api/v1/inventory/ingredients`: Alta de insumo/ingrediente.
- `POST /api/v1/inventory/movements`: Registro de movimiento (`IN`, `OUT`, `ADJUSTMENT`).
- `GET /api/v1/inventory/alerts`: Alertas en tiempo real de insumos por debajo del stock mínimo.
- `POST /api/v1/inventory/recipes`: Asociación de ingredientes y gramajes requeridos por plato.
- `POST /api/v1/inventory/deduct-for-order`: Deducción idempotente de ingredientes al generarse una orden.

### 6. `order-service` (Puerto: `8086`)
- `POST /api/v1/orders`: Creación y validación de órdenes (`DINE_IN` y `DELIVERY`).
- `GET /api/v1/orders`: Listado de órdenes filtrables por estado (`CREATED`, `IN_KITCHEN`, `READY`, `PAID`, `CANCELLED`).
- `PUT /api/v1/orders/{id}/cancel`: Cancelación controlada de órdenes.
- `POST /api/v1/orders/{id}/events/order-ready`: Notificación de orden lista desde cocina.
- `POST /api/v1/orders/{id}/events/payment-completed`: Notificación de pago exitoso desde pasarela.

### 7. `kitchen-service` (Puerto: `8087`)
- `POST /api/v1/kitchen/orders`: Recepción idempotente de comandas emitidas por `order-service`.
- `GET /api/v1/kitchen/orders/active`: Comandas en cola activa para cocineros y chefs.
- `PUT /api/v1/kitchen/orders/{id}/status`: Transición de estado (`PENDING` -> `PREPARING` -> `READY`) con cálculo de duración real.

### 8. `payment-service` (Puerto: `8088`)
- `POST /api/v1/payments`: Procesamiento de pagos (Efectivo con cálculo de cambio, Tarjeta de Crédito/Débito con enmascaramiento y autorización, Transferencia).
- `GET /api/v1/payments/{id}`: Consulta de comprobante de pago.
- `GET /api/v1/payments/order/{orderId}`: Consulta de pagos vinculados a una orden.

### 9. `delivery-service` (Puerto: `8089`)
- `POST /api/v1/delivery/webhooks/orders`: Webhook asegurado con `X-Delivery-Webhook-Secret` para Rappi (18% comisión), DiDi Food (15% comisión) y Uber Eats (20% comisión).
- `GET /api/v1/delivery/orders/{id}`: Detalle del pedido de plataforma externa y comisiones calculadas.
- `PUT /api/v1/delivery/orders/{id}/status`: Actualización del flujo de entrega (`PICKED_UP`, `DELIVERED`, `CANCELLED`).

### 10. `report-service` (Puerto: `8090`)
- `POST /api/v1/reports/sync/order`: Sincronización idempotente de proyecciones de venta originadas por órdenes pagadas.
- `GET /api/v1/reports/sales`: Reporte consolidado de ventas por rango de fechas (total ventas, impuestos, propinas, comisiones de apps, ingreso neto, ticket promedio, desglose por canal y plataforma).
- `GET /api/v1/reports/inventory`: Reporte de alertas de inventario crítico consumido de `inventory-service`.

---

## 🛡️ Seguridad y CORS

- Todas las peticiones protegidas requieren la cabecera HTTP:
  ```http
  Authorization: Bearer <TOKEN_JWT>
  ```
- El webhook de agregadores externos en `delivery-service` se valida mediante:
  ```http
  X-Delivery-Webhook-Secret: <SECRET>
  ```
- Los orígenes permitidos por CORS están configurados por defecto para soportar `http://localhost:5500` y `http://127.0.0.1:5500`.

---

## 📄 Licencia

Este proyecto es de código abierto bajo la licencia MIT.
