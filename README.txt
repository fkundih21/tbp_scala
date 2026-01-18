PROJEKT: SCALA - Aplikacija za praćenje zdravlja
STUDENT: Fran Kundih
KOLEGIJ: Teorija baza podataka (TBP)
---------------------------------------------------------
REPOZITORIJ KODA: https://github.com/fkundih21/tbp_scala
LICENCA: GPL v3 (GNU General Public License)
---------------------------------------------------------
OPIS:
Aplikacija koristi mikroservisnu arhitekturu (Docker) s PostgreSQL bazom 
(Temporalne značajke + JSONB + Triggeri), .NET backendom i Android kotlin frontendom.

---------------------------------------------------------
UPUTE ZA POKRETANJE (BACKEND + BAZA)

Preduvjet: Instaliran "Docker Desktop" na računalu.

Korak 1:
Otpakirajte ovu arhivu u proizvoljnu mapu.

Korak 2:
Otvorite terminal (Command Prompt ili PowerShell) unutar te mape.

Korak 3:
Pokrenite naredbu:
   docker-compose up --build

Korak 4:
Pričekajte da se proces završi.
- Baza podataka je spremna kad piše: "database system is ready to accept connections".
- Backend je spreman kad piše: "Now listening on: http://[::]:8080".

Sustav je sada aktivan:
- API je dostupan na: http://localhost:5059
- Baza je dostupna na portu: 5432 (User: postgres, Pass: password, DB: health_app)

NAPOMENA:
Baza podataka se automatski puni inicijalnim podacima (Seed Data) putem skripte
koja se nalazi u mapi "database/init.sql". Nije potrebna ručna intervencija.
---------------------------------------------------------
KAKO POKRENUTI MOBILNU APLIKACIJU:

1. Otvorite mapu "frontend" u Android Studiu.
2. Pokrenite aplikaciju na Android Emulatoru.
3. Aplikacija se automatski spaja na bazu.
   Korisnik je unaprijed kreiran (Seed data) pa nije potrebna registracija.
   Odmah ćete vidjeti testne podatke (težina, prehrana, bilješke).

---------------------------------------------------------
TEHNIČKE NAPOMENE:
- Baza se automatski inicijalizira skriptom "database/init.sql".
- Ako imate problema s pokretanjem, pokušajte "docker-compose down -v" 
  kako bi se očistili stari volumeni baze.