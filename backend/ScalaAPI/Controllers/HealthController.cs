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
}

    public class NoviParametriRequest
    {
        public Guid KorisnikId { get; set; }
        public decimal Tezina { get; set; }
        public decimal Visina { get; set; }
        public int TlakSys { get; set; }
        public int TlakDia { get; set; }
    }