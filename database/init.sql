DROP TABLE IF EXISTS upozorenja, dnevnik_prehrane, aktivnosti, biljeske, tjelesni_parametri, korisnici CASCADE;
DROP FUNCTION IF EXISTS procesuiraj_unos_parametara CASCADE;
DROP EXTENSION IF EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE korisnici (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ime VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    datum_rodenja DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE tjelesni_parametri (
    id SERIAL PRIMARY KEY,
    korisnik_id UUID NOT NULL REFERENCES korisnici(id) ON DELETE CASCADE,
    tezina DECIMAL(5,2) CHECK (tezina > 0),
    visina DECIMAL(5,2) CHECK (visina > 0),
    tlak_sys INT CHECK (tlak_sys > 0),
    tlak_dia INT CHECK (tlak_dia > 0),
    period_vazenja TSTZRANGE NOT NULL DEFAULT tstzrange(NOW(), NULL, '[)'),
    zabiljezeno_u TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX ix_parametri_period ON tjelesni_parametri USING GIST (period_vazenja);

CREATE TABLE dnevnik_prehrane (
    id SERIAL PRIMARY KEY,
    korisnik_id UUID NOT NULL REFERENCES korisnici(id) ON DELETE CASCADE,
    datum DATE DEFAULT CURRENT_DATE,
    detalji_obroka JSONB NOT NULL,
    CHECK (detalji_obroka ? 'ukupno_kcal')
);

CREATE TABLE upozorenja (
    id SERIAL PRIMARY KEY,
    korisnik_id UUID REFERENCES korisnici(id) ON DELETE CASCADE,
    poruka TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE OR REPLACE FUNCTION procesuiraj_unos_parametara()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE tjelesni_parametri
    SET period_vazenja = tstzrange(lower(period_vazenja), lower(NEW.period_vazenja), '[)')
    WHERE korisnik_id = NEW.korisnik_id
      AND upper(period_vazenja) IS NULL;

    IF NEW.tlak_sys > 140 OR NEW.tlak_dia > 90 THEN
        INSERT INTO upozorenja(korisnik_id, poruka)
        VALUES (NEW.korisnik_id, 'UPOZORENJE: Detektiran visok krvni tlak (' || NEW.tlak_sys || '/' || NEW.tlak_dia || ')');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_procesuiraj_parametre
BEFORE INSERT ON tjelesni_parametri
FOR EACH ROW EXECUTE FUNCTION procesuiraj_unos_parametara();

CREATE TABLE aktivnosti (
    id SERIAL PRIMARY KEY,
    korisnik_id UUID NOT NULL REFERENCES korisnici(id) ON DELETE CASCADE,
    naziv VARCHAR(100) NOT NULL,
    trajanje_min INT CHECK (trajanje_min > 0),
    kalorije_brojano INT CHECK (kalorije_brojano >= 0),
    datum TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE biljeske (
    id SERIAL PRIMARY KEY,
    korisnik_id UUID NOT NULL REFERENCES korisnici(id) ON DELETE CASCADE,
    naslov VARCHAR(255),
    sadrzaj TEXT,
    datum TIMESTAMPTZ DEFAULT NOW()
);

CREATE VIEW korisnici_zadnji_parametri AS
SELECT DISTINCT ON (k.id) 
       k.id, k.ime, k.email, k.datum_rodenja,
       tp.tezina, tp.visina, tp.tlak_sys, tp.tlak_dia, tp.zabiljezeno_u
FROM korisnici k
LEFT JOIN tjelesni_parametri tp ON k.id = tp.korisnik_id
ORDER BY k.id, tp.zabiljezeno_u DESC;


INSERT INTO korisnici (id, ime, email, datum_rodenja)
VALUES ('11111111-1111-1111-1111-111111111111', 'Student Demo', 'student@unipu.hr', '1998-05-01')
ON CONFLICT DO NOTHING;

INSERT INTO tjelesni_parametri (korisnik_id, tezina, visina, tlak_sys, tlak_dia, zabiljezeno_u, period_vazenja)
VALUES (
    '11111111-1111-1111-1111-111111111111', 
    85.0, 180, 120, 80, 
    NOW() - INTERVAL '1 month', 
    tstzrange(NOW() - INTERVAL '1 month', NULL, '[)')
);

INSERT INTO tjelesni_parametri (korisnik_id, tezina, visina, tlak_sys, tlak_dia, zabiljezeno_u, period_vazenja)
VALUES (
    '11111111-1111-1111-1111-111111111111', 
    82.0, 180, 118, 78, 
    NOW(), 
    tstzrange(NOW(), NULL, '[)')
);

INSERT INTO dnevnik_prehrane (korisnik_id, datum, detalji_obroka)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    CURRENT_DATE,
    '{
        "ukupno_kcal": 450, 
        "popis_namirnica": [
            {"naziv": "Zobene pahuljice", "kalorije": 200},
            {"naziv": "Whey protein", "kalorije": 120},
            {"naziv": "Borovnice", "kalorije": 130}
        ]
    }'::jsonb
);

INSERT INTO aktivnosti (korisnik_id, naziv, trajanje_min, kalorije_brojano, datum)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Jutarnje trčanje',
    30,
    350,
    NOW()
);

INSERT INTO biljeske (korisnik_id, naslov, sadrzaj)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Cilj projekta',
    'Demonstrirati rad s temporalnim i polustrukturiranim podacima u PostgreSQL-u.'
);