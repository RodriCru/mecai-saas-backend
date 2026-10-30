-- Email único entre registros activos (borrado lógico), en users y en tenants.
-- Un registro con is_deleted = true no bloquea que su email se vuelva a registrar.
-- Hibernate no puede declarar índices parciales, por eso va en SQL.

-- 1. Quitar las restricciones únicas anteriores, si las tablas ya existían:
--    users:   unique (email, tenant_id)
--    tenants: unique (email)
--    Hibernate les pone nombres generados (ukXXXX), por eso se buscan en el catálogo en vez de asumir el nombre.
--    Estas tablas no tienen otra restricción única, así que se quitan todas las de tipo 'u'.
DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT conrelid::regclass AS tabla, conname
        FROM pg_constraint
        WHERE contype = 'u'
          AND conrelid IN (to_regclass('users'), to_regclass('tenants'))
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', r.tabla, r.conname);
    END LOOP;
END $$;

-- 2. Índices únicos parciales.
CREATE UNIQUE INDEX IF NOT EXISTS users_email_active_uk
    ON users (email)
    WHERE is_deleted = false;

CREATE UNIQUE INDEX IF NOT EXISTS tenants_email_active_uk
    ON tenants (email)
    WHERE is_deleted = false;
