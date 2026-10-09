package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;

import com.finisus.application.ports.in.ImportacaoFinanceiraUseCase;
import jakarta.persistence.EntityManagerFactory;
import java.util.stream.IntStream;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class ImportacaoFinanceiraQueriesIntegrationTest {
	@Autowired
	private ImportacaoFinanceiraUseCase importacoes;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Test
	@Transactional
	void limitaConsultasDaRevisaoSemCarregarHistoricosLinhaPorLinha() {
		jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash, ativo) "
				+ "VALUES (910001, 'Importação', 'importacao-queries@teste.local', 'senha', TRUE)");
		jdbcTemplate.update("INSERT INTO conta (id, usuario_id, nome, tipo, banco_id, saldo) "
				+ "VALUES (910002, 910001, 'Conta', 'CORRENTE', 1, 0.00)");
		jdbcTemplate.update("INSERT INTO importacao_financeira (id, usuario_id, banco_id, nome_arquivo, hash_arquivo, "
				+ "leitor, tipo_documento, tipo_documento_pretendido, status, conta_id) VALUES "
				+ "(910003, 910001, 1, 'extrato.pdf', REPEAT('a', 64), 'teste', 'EXTRATO_CONTA', "
				+ "'EXTRATO_CONTA', 'PENDENTE_REVISAO', 910002)");
		IntStream.rangeClosed(1, 5).forEach(ordem -> jdbcTemplate.update("INSERT INTO lancamento_importado "
				+ "(id, importacao_id, ordem, data, descricao, conteudo_original, data_original, descricao_original, "
				+ "valor_original, tipo_original, valor, tipo, pendente_confirmacao, estado, importar) VALUES "
				+ "(?, 910003, ?, '2026-09-01', ?, ?, '2026-09-01', ?, ?, 'SAIDA', ?, 'SAIDA', FALSE, "
				+ "'PENDENTE', TRUE)", 910010 + ordem, ordem, "Linha " + ordem, "Linha " + ordem,
				"Linha " + ordem, ordem * 10, ordem * 10));
		var estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		estatisticas.setStatisticsEnabled(true);
		estatisticas.clear();

		try {
			var revisao = importacoes.buscar(910001L, 910003L);

			assertThat(revisao.importacao().getLancamentos()).hasSize(5);
			assertThat(estatisticas.getPrepareStatementCount()).as("consultas SQL da revisão de importação")
					.isLessThanOrEqualTo(8);
		} finally {
			estatisticas.setStatisticsEnabled(false);
		}
	}
}
