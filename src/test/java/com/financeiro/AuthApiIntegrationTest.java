package com.financeiro;

import com.financeiro.application.ports.in.AutenticarUsuarioUseCase;
import com.financeiro.application.ports.in.CadastrarUsuarioUseCase;
import com.financeiro.application.ports.in.CategoriaUseCase;
import com.financeiro.application.ports.in.ContaUseCase;
import com.financeiro.application.ports.in.ItemUseCase;
import com.financeiro.application.ports.in.TransacaoUseCase;
import com.financeiro.domain.model.TipoConta;
import com.financeiro.domain.model.TipoTransacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class AuthApiIntegrationTest {
	@Autowired
	CadastrarUsuarioUseCase cadastrarUsuario;
	@Autowired
	AutenticarUsuarioUseCase autenticarUsuario;
	@Autowired
	ContaUseCase contas;
	@Autowired
	CategoriaUseCase categorias;
	@Autowired
	ItemUseCase itens;
	@Autowired
	TransacaoUseCase transacoes;

	@Test
	void cadastraUsuarioEAutentica() {
		cadastrarUsuario.executar(new CadastrarUsuarioUseCase.Command("Ana", "ana@example.com", "senha-segura"));
		var tokens = autenticarUsuario
				.executar(new AutenticarUsuarioUseCase.Command("ana@example.com", "senha-segura"));
		assertThat(tokens.accessToken()).isNotBlank();
	}

	@Test
	void registraTransacaoComOsCasosDeUsoSeparados() {
		Long usuarioId = cadastrarUsuario
				.executar(new CadastrarUsuarioUseCase.Command("Bia", "bia@example.com", "senha-segura")).id();
		var conta = contas.criar(usuarioId, new ContaUseCase.CriarCommand("Carteira", TipoConta.FISICO, null));
		var categoria = categorias.criar(usuarioId, new CategoriaUseCase.CriarCommand("Alimentação", null));
		var item = itens.criar(usuarioId, new ItemUseCase.CriarCommand("Projeto", categoria.getId()));
		var transacao = transacoes.registrar(usuarioId,
				new TransacaoUseCase.RegistrarCommand(TipoTransacao.ENTRADA, new BigDecimal("100.00"), LocalDate.now(),
						"Freelance", conta.getId(), categoria.getId(), null,
						List.of(new TransacaoUseCase.ItemCommand(item.getId(), new BigDecimal("100.00")))));
		assertThat(transacao.getId()).isNotNull();

		assertThat(transacao.getItens()).singleElement().satisfies(itemLancado -> {
			assertThat(itemLancado.getItemId()).isEqualTo(item.getId());
			assertThat(itemLancado.getDescricao()).isEqualTo("Projeto");
		});
		assertThat(contas.buscar(usuarioId, conta.getId()).getSaldo().valor()).isEqualByComparingTo("100.00");
	}
}
