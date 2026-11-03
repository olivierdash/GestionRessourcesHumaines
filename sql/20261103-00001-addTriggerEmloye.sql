CREATE SEQUENCE IF NOT EXISTS matricule_seq START 1;

CREATE OR REPLACE FUNCTION set_matricule_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.matricule IS NULL THEN
        NEW.matricule := 'EMP-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || LPAD(nextval('matricule_seq')::text, 4, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_set_matricule ON employer;

CREATE TRIGGER trg_set_matricule
BEFORE INSERT ON employer
FOR EACH ROW
EXECUTE FUNCTION set_matricule_on_insert();