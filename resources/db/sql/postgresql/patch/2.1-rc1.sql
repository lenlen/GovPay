ALTER TABLE psp ALTER COLUMN ragione_sociale TYPE VARCHAR(70);
ALTER TABLE rpt ADD COLUMN cod_transazione_rpt VARCHAR(36);
ALTER TABLE rpt ADD COLUMN cod_transazione_rt VARCHAR(36);
ALTER TABLE rr ADD COLUMN cod_transazione_rr VARCHAR(36);
ALTER TABLE rr ADD COLUMN cod_transazione_er VARCHAR(36);
