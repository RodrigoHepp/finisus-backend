package com.finisus;

import static org.assertj.core.api.Assertions.assertThat;

import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.in.CategoriaUseCase;
import com.finisus.application.ports.in.ContaUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import com.finisus.domain.vo.AnoMes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class TransacaoPesquisaIntegrationTest {
	@Autowired
	private UsuarioRepositoryPort usuarios;

	@Autowired
	private ContaUseCase contas;

	@Autowired
	private CategoriaUseCase categorias;

	@Autowired
	private TransacaoUseCase transacoes;

	@Test
	void filtraPorMesTipoECategoriaSemPerderPaginacao() {
		Long usuarioId = usuarios.salvar(Usuario.novo("Pesquisa", new Email("pesquisa@teste.local"),
				"hash-sintetico", LocalDateTime.now())).getId();
		var conta = contas.criar(usuarioId, new ContaUseCase.CriarCommand("Carteira", TipoConta.FISICO, null));
		var assinaturas = categorias.criar(usuarioId, new CategoriaUseCase.CriarCommand("Assinaturas", null));
		var alimentacao = categorias.criar(usuarioId, new CategoriaUseCase.CriarCommand("Alimentação", null));
		registrar(usuarioId, conta.getId(), alimentacao.getId(), TipoTransacao.ENTRADA, "Saldo inicial", "2026-07-31",
				new BigDecimal("100.00"));
		registrar(usuarioId, conta.getId(), assinaturas.getId(), TipoTransacao.SAIDA, "Netflix", "2026-08-25");
		registrar(usuarioId, conta.getId(), assinaturas.getId(), TipoTransacao.SAIDA, "Streaming setembro", "2026-09-01");
		registrar(usuarioId, conta.getId(), alimentacao.getId(), TipoTransacao.SAIDA, "Mercado", "2026-08-20");
		registrar(usuarioId, conta.getId(), alimentacao.getId(), TipoTransacao.ENTRADA, "Reembolso", "2026-08-18");

		var filtro = new TransacaoUseCase.FiltroListagem(AnoMes.parse("2026-08"), TipoTransacao.SAIDA,
				assinaturas.getId());
		var resultado = transacoes.listar(usuarioId, new Paginacao(0, 100), filtro);
		var semFiltros = transacoes.listar(usuarioId, new Paginacao(0, 1),
				new TransacaoUseCase.FiltroListagem(null, null, null));

		assertThat(resultado.conteudo()).extracting(transacao -> transacao.getDescricao()).containsExactly("Netflix");
		assertThat(resultado.pagina()).isZero();
		assertThat(resultado.tamanho()).isEqualTo(100);
		assertThat(resultado.totalElementos()).isEqualTo(1);
		assertThat(resultado.totalPaginas()).isEqualTo(1);
		assertThat(semFiltros.conteudo()).hasSize(1);
		assertThat(semFiltros.totalElementos()).isEqualTo(5);
		assertThat(semFiltros.totalPaginas()).isEqualTo(5);
	}

	private void registrar(Long usuarioId, Long contaId, Long categoriaId, TipoTransacao tipo, String descricao,
			String data) {
		registrar(usuarioId, contaId, categoriaId, tipo, descricao, data, new BigDecimal("25.00"));
	}

	private void registrar(Long usuarioId, Long contaId, Long categoriaId, TipoTransacao tipo, String descricao,
			String data, BigDecimal valor) {
		transacoes.registrar(usuarioId,
				new TransacaoUseCase.RegistrarCommand(tipo, valor, LocalDate.parse(data), descricao,
						contaId, categoriaId, null, List.of()));
	}
}
