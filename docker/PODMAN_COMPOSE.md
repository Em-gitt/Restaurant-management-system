# Gestión de Infraestructura PostgreSQL con Podman Compose

Este documento describe la arquitectura, puesta en marcha y administración del servicio de base de datos PostgreSQL utilizando **Podman Compose** para el ecosistema de microservicios de **Restaurant System**.

---

## 🏗️ Arquitectura de Datos

- **Motor:** PostgreSQL 18 (`docker.io/library/postgres:18`)
- **Contenedor:** `restaurant-postgres`
- **Volumen Persistente:** `restaurant-system_postgres_data` (administrado por Compose)
- **Puerto:** `5432:5432`
- **Mecanismo de Inicialización:** Montaje de solo lectura de `../init-scripts` en `/docker-entrypoint-initdb.d`

### Catálogo de Bases de Datos y Roles

Se provisionan automáticamente 10 bases de datos independientes con usuarios dedicados y permisos sobre el esquema `public`:

| Base de Datos | Usuario | Microservicio / Estado |
| :--- | :--- | :--- |
| `authdb` | `auth_user` | `auth-service` (Migrado con Flyway V1) |
| `menudb` | `menu_user` | `menu-service` (Migrado con Flyway V1) |
| `tabledb` | `table_user` | `table-service` (Migrado con Flyway V1) |
| `orderdb` | `order_user` | *Futuro servicio (Base vacía)* |
| `inventorydb` | `inventory_user` | *Futuro servicio (Base vacía)* |
| `employeedb` | `employee_user` | *Futuro servicio (Base vacía)* |
| `kitchendb` | `kitchen_user` | *Futuro servicio (Base vacía)* |
| `paymentdb` | `payment_user` | *Futuro servicio (Base vacía)* |
| `deliverydb` | `delivery_user` | *Futuro servicio (Base vacía)* |
| `reportdb` | `report_user` | *Futuro servicio (Base vacía)* |

---

## 🚀 Comandos de Administración con Podman Compose

Todos los comandos deben ejecutarse desde la raíz del proyecto donde se ubica `compose.yaml`:

### 1. Iniciar el servicio en segundo plano
```bash
podman compose up -d
```

### 2. Verificar el estado del contenedor
```bash
podman compose ps
```

### 3. Visualizar registros (logs) en tiempo real
```bash
podman compose logs -f postgres
```

### 4. Pausar o detener temporalmente el contenedor (sin remover)
```bash
podman compose stop
```

### 5. Reanudar el contenedor detenido
```bash
podman compose start
```

### 6. Desmontar el stack de Compose (preserva los datos en el volumen)
```bash
podman compose down
```

> [!CAUTION]
> **NUNCA** utilices la bandera `-v` (`podman compose down -v`) a menos que desees destruir intencionalmente todos los volúmenes y datos del sistema.

### 7. Conectarse a una base de datos mediante `psql` dentro del contenedor
```bash
# Conectarse a authdb con el usuario auth_user:
podman exec -it restaurant-postgres psql -U auth_user -d authdb

# Conectarse como superusuario postgres:
podman exec -it restaurant-postgres psql -U postgres
```

---

## 🧹 Limpieza del Contenedor y Volumen Anteriores

El contenedor manual preexistente `auth-postgres` fue detenido de forma segura para liberar el puerto 5432 y no interfiere con el nuevo entorno.

Una vez que hayas verificado la nueva infraestructura y desees eliminar definitivamente los recursos residuales del entorno anterior, ejecuta:

```bash
# 1. Eliminar el contenedor anterior 'auth-postgres'
podman rm auth-postgres

# 2. Eliminar el volumen anterior no utilizado 'auth-postgres-data'
podman volume rm auth-postgres-data
```
