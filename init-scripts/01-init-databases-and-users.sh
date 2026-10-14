#!/bin/bash
set -e

# Lista de configuraciones en formato: "base_de_datos:usuario:contraseña"
SERVICES=(
    "authdb:auth_user:${DB_AUTH_PASSWORD:-auth_pass}"
    "menudb:menu_user:${DB_MENU_PASSWORD:-menu_pass}"
    "tabledb:table_user:${DB_TABLE_PASSWORD:-table_pass}"
    "orderdb:order_user:${DB_ORDER_PASSWORD:-order_pass}"
    "inventorydb:inventory_user:${DB_INVENTORY_PASSWORD:-inventory_pass}"
    "employeedb:employee_user:${DB_EMPLOYEE_PASSWORD:-employee_pass}"
    "kitchendb:kitchen_user:${DB_KITCHEN_PASSWORD:-kitchen_pass}"
    "paymentdb:payment_user:${DB_PAYMENT_PASSWORD:-payment_pass}"
    "deliverydb:delivery_user:${DB_DELIVERY_PASSWORD:-delivery_pass}"
    "reportdb:report_user:${DB_REPORT_PASSWORD:-report_pass}"
)

echo ">>> Iniciando creación automática de bases de datos y usuarios para restaurant-system..."

for entry in "${SERVICES[@]}"; do
    IFS=":" read -r db user password <<< "$entry"
    echo ">>> Procesando: base de datos '$db' y usuario '$user'..."

    # 1. Crear usuario si no existe
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        DO \$\$
        BEGIN
            IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$user') THEN
                CREATE ROLE "$user" WITH LOGIN PASSWORD '$password';
            ELSE
                ALTER ROLE "$user" WITH PASSWORD '$password';
            END IF;
        END
        \$\$;
EOSQL

    # 2. Crear base de datos si no existe con propietario el usuario
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE "$db" OWNER "$user"'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL

    # 3. Otorgar permisos y asegurar que el usuario sea propietario del esquema public en su base de datos
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" <<-EOSQL
        GRANT ALL PRIVILEGES ON DATABASE "$db" TO "$user";
        ALTER SCHEMA public OWNER TO "$user";
        GRANT ALL ON SCHEMA public TO "$user";
EOSQL

    echo ">>> Base de datos '$db' configurada exitosamente con dueño '$user'."
done

echo ">>> Inicialización completa de todas las bases de datos y usuarios."
