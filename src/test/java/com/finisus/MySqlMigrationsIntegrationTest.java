package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class MySqlMigrationsIntegrationTest {

	@Container
	static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
			.withDatabaseName("finisus_test")
			.withUsername("finisus")
			.withPassword("finisus");

	@Test
	void criaBaselineLimpaEReaplicaSemAlteracoes() throws Exception {
		Flyway flyway = Flyway.configure()
				.dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
				.locations("classpath:db/migration")
				.load();

		var primeiraExecucao = flyway.migrate();

		assertThat(primeiraExecucao.success).isTrue();
		assertThat(primeiraExecucao.migrationsExecuted).isPositive();
		assertThat(flyway.info().current()).isNotNull();
		assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
		assertThat(flyway.migrate().migrationsExecuted).isZero();

		try (var conexao = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(),
				MYSQL.getPassword())) {
			assertThat(contarTabelas(conexao, "divisao_compartilhada", "participante_divisao_compartilhada",
					"transacao_divisao_compartilhada", "responsabilidade_transacao_divisao", "alocacao_pagamento_divisao",
					"reembolso_divisao", "importacao_financeira", "lancamento_importado",
					"revisao_lancamento_importado"))
					.isEqualTo(9);
			assertThat(contarTabelas(conexao, "despesa_compartilhada", "rateio_despesa",
					"migracao_rateio_externo"))
					.isZero();
			assertThat(contarColunas(conexao, "importacao_financeira", "documento_canonico")).isZero();
			assertThat(contarColunasDeIndicesUnicos(conexao, "importacao_financeira", "usuario_id", "banco_id",
					"hash_arquivo"))
					.isEqualTo(3);
		}
	}

	private int contarTabelas(Connection conexao, String... tabelas) throws SQLException {
		String marcadores = String.join(",", Collections.nCopies(tabelas.length, "?"));
		try (var consulta = conexao.prepareStatement("""
				SELECT COUNT(*)
				FROM information_schema.tables
				WHERE table_schema = DATABASE() AND table_name IN (%s)
				""".formatted(marcadores))) {
			for (int indice = 0; indice < tabelas.length; indice++) {
				consulta.setString(indice + 1, tabelas[indice]);
			}
			try (var linhas = consulta.executeQuery()) {
				linhas.next();
				return linhas.getInt(1);
			}
		}
	}

	private int contarColunas(Connection conexao, String tabela, String coluna) throws SQLException {
		try (var consulta = conexao.prepareStatement("""
				SELECT COUNT(*)
				FROM information_schema.columns
				WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
				""")) {
			consulta.setString(1, tabela);
			consulta.setString(2, coluna);
			try (var linhas = consulta.executeQuery()) {
				linhas.next();
				return linhas.getInt(1);
			}
		}
	}

	private int contarColunasDeIndicesUnicos(Connection conexao, String tabela, String... colunas)
			throws SQLException {
		String marcadores = String.join(",", Collections.nCopies(colunas.length, "?"));
		try (var consulta = conexao.prepareStatement("""
				SELECT COUNT(DISTINCT column_name)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE() AND table_name = ? AND non_unique = 0
				  AND column_name IN (%s)
				""".formatted(marcadores))) {
			consulta.setString(1, tabela);
			for (int indice = 0; indice < colunas.length; indice++) {
				consulta.setString(indice + 2, colunas[indice]);
			}
			try (var linhas = consulta.executeQuery()) {
				linhas.next();
				return linhas.getInt(1);
			}
		}
	}
}
