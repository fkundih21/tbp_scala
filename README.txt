PROJEKT: SCALA - Aplikacija za praćenje zdravlja
STUDENT: Tvoje Ime
KOLEGIJ: Teorija baza podataka
---------------------------------------------------------

OPIS:
Aplikacija koristi mikroservisnu arhitekturu (Docker) s PostgreSQL bazom 
(Temporalne značajke + JSONB + Triggeri), .NET backendom i Android kotlin frontendom.

---------------------------------------------------------
KAKO POKRENUTI SUSTAV (BAZA + BACKEND):

Preduvjet: Instaliran Docker Desktop.

1. Otvorite terminal/cmd u korijenskoj mapi projekta.
2. Pokrenite naredbu:
   docker-compose up --build

3. Pričekajte da se ispiše "database system is ready to accept connections" 
   i "Now listening on: http://[::]:8080".
   
   Sustav je sada podignut.
   - Baza je na portu: 5432
   - API je na portu: 5059 (localhost:5059/swagger za testiranje)

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