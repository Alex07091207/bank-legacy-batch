package com.banco.bank_legacy_batch.bff.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.banco.bank_legacy_batch.model.Interes;

@Repository
public class CuentaBffRepository {
    private final JdbcTemplate jdbcTemplate;

    public CuentaBffRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Interes> buscarCuenta(Long cuentaId) {
        String sql = """
                SELECT cuenta_id, nombre, saldo, edad, tipo, interes, saldo_final, estado 
                FROM intereses_procesados 
                WHERE cuenta_id = ?
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Interes cuenta = new Interes();
                    cuenta.setCuentaId(rs.getLong("cuenta_id"));
                    cuenta.setNombre(rs.getString("nombre"));
                    cuenta.setSaldo(rs.getBigDecimal("saldo"));
                    cuenta.setEdad(rs.getInt("edad"));
                    cuenta.setTipo(rs.getString("tipo"));
                    cuenta.setInteres(rs.getBigDecimal("interes"));
                    cuenta.setSaldoFinal(rs.getBigDecimal("saldo_final"));
                    cuenta.setEstado(rs.getString("estado"));
                    return cuenta;
                },
                cuentaId
        ).stream().findFirst();
    }

    public int actualizarSaldo(Long cuentaId, BigDecimal nuevoSaldo) {
        String sql = "UPDATE intereses_procesados SET saldo = ?, saldo_final = ? WHERE cuenta_id = ?";
        return jdbcTemplate.update(sql, nuevoSaldo, nuevoSaldo, cuentaId);
    }
}