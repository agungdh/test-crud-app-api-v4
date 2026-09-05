-- New rows get updated_at = now() even on direct SQL inserts.
-- (V1 is already applied, so this alters instead of editing V1.)
ALTER TABLE IF EXISTS person ALTER COLUMN updated_at SET DEFAULT now();

-- Backfill rows created before this default existed.
UPDATE person SET updated_at = created_at WHERE updated_at IS NULL;
