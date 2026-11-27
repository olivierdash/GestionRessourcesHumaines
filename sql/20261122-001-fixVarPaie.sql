DO $$
DECLARE
    v_kind "char";
BEGIN
    SELECT c.relkind
      INTO v_kind
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
     WHERE c.relname = 'variable_paie'
       AND n.nspname = current_schema();

    IF v_kind = 'v' THEN
        EXECUTE 'DROP VIEW variable_paie';
    ELSIF v_kind = 'm' THEN
        EXECUTE 'DROP MATERIALIZED VIEW variable_paie';
    ELSIF v_kind = 'r' THEN
        EXECUTE 'DROP TABLE variable_paie CASCADE';
    END IF;
END $$;
