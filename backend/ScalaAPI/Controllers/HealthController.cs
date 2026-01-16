using Dapper;
using Microsoft.AspNetCore.Mvc;
using Npgsql;

namespace ScalaAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class HealthController : ControllerBase
{
    private readonly IConfiguration _config;

    public HealthController(IConfiguration config)
    {
        _config = config;
    }

    private NpgsqlConnection GetConnection()
    {
        return new NpgsqlConnection(_config.GetConnectionString("DefaultConnection"));
        
    }
    
    [HttpGet("parametri")]
    public async Task<IActionResult> GetParametre()
    {
        using var conn = GetConnection();
        var sql = "SELECT id, tezina, visina, tlak_sys, tlak_dia, zabiljezeno_u FROM tjelesni_parametri ORDER BY zabiljezeno_u DESC";
            
        var podaci = await conn.QueryAsync(sql);
        return Ok(podaci);
    }
    
    [HttpPost("parametri")]
    public async Task<IActionResult> DodajParametre([FromBody] NoviParametriRequest req)
    {
        using var conn = GetConnection();
            
        var sql = @"
                INSERT INTO tjelesni_parametri (korisnik_id, tezina, visina, tlak_sys, tlak_dia)
                VALUES (@KorisnikId, @Tezina, @Visina, @TlakSys, @TlakDia)
                RETURNING id;";

        try 
        {
            var noviId = await conn.ExecuteScalarAsync<int>(sql, new 
            {
                req.KorisnikId,
                req.Tezina,
                req.Visina,
                req.TlakSys,
                req.TlakDia
            });

            return Ok(new { message = "Spremljeno!", id = noviId });
        }
        catch (Exception ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }
    
    [HttpPost("prehrana")]
    public async Task<IActionResult> DodajPrehranu([FromBody] UnosPrehraneRequest req)
    {
        using var conn = GetConnection();

        var jsonObjekt = new 
        {
            ukupno_kcal = req.UkupnoKcal,
            popis_namirnica = req.Namirnice
        };

        string jsonString = System.Text.Json.JsonSerializer.Serialize(jsonObjekt);

        var sql = @"
        INSERT INTO dnevnik_prehrane (korisnik_id, detalji_obroka, datum)
        VALUES (@KorisnikId, @JsonPodaci::jsonb, CURRENT_DATE)
        RETURNING id;";

        try 
        {
            var noviId = await conn.ExecuteScalarAsync<int>(sql, new 
            {
                req.KorisnikId,
                JsonPodaci = jsonString
            });

            return Ok(new { message = "Dnevnik prehrane spremljen!", id = noviId });
        }
        catch (Exception ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }
    [HttpGet("prehrana/danas")]
    public async Task<IActionResult> GetDanasnjaPrehrana([FromQuery] Guid korisnikId)
    {
        using var conn = GetConnection();

        var sql = @"
        SELECT detalji_obroka 
        FROM dnevnik_prehrane 
        WHERE korisnik_id = @KorisnikId AND datum = CURRENT_DATE";

        var jsonRezultati = await conn.QueryAsync<string>(sql, new { KorisnikId = korisnikId });

        int ukupnoKcalDanas = 0;
        var sveNamirniceDanas = new List<Namirnica>();

        var options = new System.Text.Json.JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        };

        foreach (var json in jsonRezultati)
        {
            try 
            {
                var podaciIzBaze = System.Text.Json.JsonSerializer.Deserialize<DbJsonModel>(json, options);
            
                if (podaciIzBaze != null)
                {
                    ukupnoKcalDanas += podaciIzBaze.ukupno_kcal;
                    if (podaciIzBaze.popis_namirnica != null)
                    {
                        sveNamirniceDanas.AddRange(podaciIzBaze.popis_namirnica);
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Greška kod parsiranja JSON-a: " + ex.Message);
            }
        }

        return Ok(new 
        { 
            UkupnoKcal = ukupnoKcalDanas,
            Namirnice = sveNamirniceDanas
        });
    }
    
    [HttpGet("upozorenja")]
    public async Task<IActionResult> GetUpozorenja([FromQuery] Guid korisnikId)
    {
        using var conn = GetConnection();
    
        var sql = @"
        SELECT poruka, created_at as Datum 
        FROM upozorenja 
        WHERE korisnik_id = @KorisnikId 
          AND created_at > NOW() - INTERVAL '1 DAY'
        ORDER BY created_at DESC";

        var upozorenja = await conn.QueryAsync(sql, new { KorisnikId = korisnikId });
        return Ok(upozorenja);
    }
    
    [HttpPost("aktivnost")]
    public async Task<IActionResult> DodajAktivnost([FromBody] NovaAktivnostRequest req)
    {
        using var conn = GetConnection();
        var sql = @"
        INSERT INTO aktivnosti (korisnik_id, naziv, trajanje_min, kalorije_brojano)
        VALUES (@KorisnikId, @Naziv, @Trajanje, @Kalorije)";
        
        await conn.ExecuteAsync(sql, req);
        return Ok(new { message = "Aktivnost spremljena!" });
    }
    [HttpGet("aktivnost/danas")]
    public async Task<IActionResult> GetDanasnjeAktivnosti([FromQuery] Guid korisnikId)
    {
        using var conn = GetConnection();
    
        var sql = @"
        SELECT id, naziv, trajanje_min as Trajanje, kalorije_brojano as Kalorije 
        FROM aktivnosti 
        WHERE korisnik_id = @KorisnikId 
          AND datum::date = CURRENT_DATE
        ORDER BY datum DESC";

        var aktivnosti = await conn.QueryAsync<NovaAktivnostRequest>(sql, new { KorisnikId = korisnikId });
        return Ok(aktivnosti);
    }
    [HttpGet("biljeske")]
    public async Task<IActionResult> GetBiljeske([FromQuery] Guid korisnikId)
    {
        using var conn = GetConnection();
        var sql = "SELECT id, naslov, sadrzaj, datum FROM biljeske WHERE korisnik_id = @KorisnikId ORDER BY datum DESC";
        var biljeske = await conn.QueryAsync(sql, new { KorisnikId = korisnikId });
        return Ok(biljeske);
    }

    [HttpPost("biljeske")]
    public async Task<IActionResult> DodajBiljesku([FromBody] NovaBiljeskaRequest req)
    {
        using var conn = GetConnection();
        var sql = "INSERT INTO biljeske (korisnik_id, naslov, sadrzaj) VALUES (@KorisnikId, @Naslov, @Sadrzaj)";
        await conn.ExecuteAsync(sql, req);
        return Ok(new { message = "Bilješka spremljena!" });
    }

    public class NovaBiljeskaRequest
    {
        public Guid KorisnikId { get; set; }
        public string Naslov { get; set; }
        public string Sadrzaj { get; set; }
    }

    public class NovaAktivnostRequest
    {
        public Guid KorisnikId { get; set; }
        public string Naziv { get; set; }
        public int Trajanje { get; set; }
        public int Kalorije { get; set; }
    }
    private class DbJsonModel
    {
        public int ukupno_kcal { get; set; }
        public List<Namirnica> popis_namirnica { get; set; }
    }
    public class NoviParametriRequest
    {
        public Guid KorisnikId { get; set; }
        public decimal Tezina { get; set; }
        public decimal Visina { get; set; }
        public int TlakSys { get; set; }
        public int TlakDia { get; set; }
    }

    public class UnosPrehraneRequest
    {
        public Guid KorisnikId { get; set; }
        public int UkupnoKcal { get; set; }
        public List<Namirnica> Namirnice { get; set; }
    }

    public class Namirnica
    {
        public string Naziv { get; set; } = string.Empty;
        public int Kalorije { get; set; }
    }
    
}