package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.ports.in.ReconciliarSaldoContaUseCase;
import com.finisus.application.ports.in.AjustarSaldoContaUseCase;
import com.finisus.domain.DomainException;
import com.finisus.application.pagination.Paginacao;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class ReconciliacaoSaldoContaIntegrationTest {
	@Autowired
	private ReconciliarSaldoContaUseCase reconciliacao;

	@Autowired
	private AjustarSaldoContaUseCase ajusteSaldo;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@Transactional
	void calculaSomenteMovimentosEficazesEExplicitaDivergencia() {
		jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash, ativo) "
				+ "VALUES (910001, 'Reconciliação', 'reconciliacao@teste.local', 'senha', TRUE)");
		jdbcTemplate.update("INSERT INTO conta (id, usuario_id, nome, tipo, saldo) "
				+ "VALUES (910002, 910001, 'Principal', 'FISICO', 930.00)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id) VALUES "
				+ "(910003, 910001, 'ENTRADA', 1000.00, '2026-09-01', 'Receita', 910002), "
				+ "(910004, 910001, 'SAIDA', 80.00, '2026-09-02', 'Despesa', 910002)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id, estornado_em) "
				+ "VALUES (910005, 910001, 'SAIDA', 10.00, '2026-09-03', 'Estornada', 910002, CURRENT_TIMESTAMP)");

		var resultado = reconciliacao.reconciliar(910001L, 910002L);

		assertThat(resultado.saldoCalculado()).isEqualByComparingTo("920.00");
		assertThat(resultado.saldoMaterializado()).isEqualByComparingTo("930.00");
		assertThat(resultado.divergencia()).isEqualByComparingTo("10.00");
		assertThat(resultado.quantidadeMovimentos()).isEqualTo(2);
		assertThat(resultado.conciliado()).isFalse();
	}

	@Test
	@Transactional
	void naoExpoeReconciliacaoDeContaAlheia() {
		jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash, ativo) "
				+ "VALUES (910011, 'Titular', 'titular-reconciliacao@teste.local', 'senha', TRUE)");
		jdbcTemplate.update("INSERT INTO conta (id, usuario_id, nome, tipo, saldo) "
				+ "VALUES (910012, 910011, 'Privada', 'FISICO', 0.00)");

		assertThatThrownBy(() -> reconciliacao.reconciliar(999999L, 910012L))
				.isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.recurso.nao.encontrado");
	}

	@Test
	@Transactional
	void ajustaDivergenciaComAuditoriaEIdempotenciaSemCriarReceitaOuDespesa() {
		jdbcTemplate.update("INSERT INTO usuario (id, nome, email, senha_hash, ativo) "
				+ "VALUES (910021, 'Ajuste', 'ajuste-saldo@teste.local', 'senha', TRUE)");
		jdbcTemplate.update("INSERT INTO conta (id, usuario_id, nome, tipo, saldo) "
				+ "VALUES (910022, 910021, 'Principal', 'FISICO', 930.00)");
		jdbcTemplate.update("INSERT INTO transacao (id, usuario_id, tipo, valor, data, descricao, conta_id) VALUES "
				+ "(910023, 910021, 'ENTRADA', 1000.00, '2026-09-01', 'Receita', 910022), "
				+ "(910024, 910021, 'SAIDA', 80.00, '2026-09-02', 'Despesa', 910022)");
		var command = new AjustarSaldoContaUseCase.AjustarCommand(new java.math.BigDecimal("930.00"),
				"Saldo conferido no extrato bancário");

		var criado = ajusteSaldo.ajustar(910021L, 910022L, "ajuste-1", command);
		var repetido = ajusteSaldo.ajustar(910021L, 910022L, "ajuste-1", command);
		var resultado = reconciliacao.reconciliar(910021L, 910022L);
		var historico = ajusteSaldo.listar(910021L, 910022L, new Paginacao(0, 20));

		assertThat(criado.valorAjuste()).isEqualByComparingTo("10.00");
		assertThat(repetido.id()).isEqualTo(criado.id());
		assertThat(resultado.saldoMaterializado()).isEqualByComparingTo("930.00");
		assertThat(resultado.saldoCalculado()).isEqualByComparingTo("930.00");
		assertThat(resultado.divergencia()).isEqualByComparingTo("0.00");
		assertThat(resultado.quantidadeAjustes()).isEqualTo(1);
		assertThat(resultado.conciliado()).isTrue();
		assertThat(historico.totalElementos()).isEqualTo(1);
		assertThat(historico.conteudo()).singleElement()
				.satisfies(a -> assertThat(a.motivo()).isEqualTo("Saldo conferido no extrato bancário"));
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transacao WHERE conta_id = 910022", Long.class))
				.isEqualTo(2L);

		assertThatThrownBy(() -> ajusteSaldo.ajustar(910021L, 910022L, "ajuste-1",
				new AjustarSaldoContaUseCase.AjustarCommand(new java.math.BigDecimal("940.00"), "Outro saldo")))
				.isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.ajuste.saldo.idempotencia.conflitante");
	}
}
