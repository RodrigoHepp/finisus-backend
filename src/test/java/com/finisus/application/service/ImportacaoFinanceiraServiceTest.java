package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finisus.application.ports.in.BancoUseCase;
import com.finisus.application.ports.in.FaturaUseCase;
import com.finisus.application.ports.in.ImportacaoFinanceiraUseCase;
import com.finisus.application.ports.in.ObrigacaoFinanceiraUseCase;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ImportacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.Fatura;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.TipoConta;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.ObrigacaoFinanceira;
import com.finisus.domain.model.StatusImportacaoFinanceira;
import com.finisus.domain.model.StatusObrigacaoFinanceira;
import com.finisus.domain.model.TipoDocumentoFinanceiro;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.ValorMonetario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ImportacaoFinanceiraServiceTest {
	@Test
	void confirmarCreditoDeFaturaCriaEntradaVinculadaEmVezDeRejeitar() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		FaturaUseCase faturas = mock(FaturaUseCase.class);
		Fatura fatura = Fatura.reconstituir(40L, 50L, com.finisus.domain.vo.AnoMes.parse("2026-09"),
				LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 30),
				com.finisus.domain.model.StatusFatura.ABERTA, 30L, 0);
		LancamentoImportado credito = LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 5),
				"Estorno compra", "Estorno compra + 25,00", new BigDecimal("25.00"), TipoTransacao.ENTRADA,
				false, null, true, null, null, null);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "fatura.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.FATURA_CARTAO, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, 40L, 0, List.of(credito));
		Transacao transacaoCredito = Transacao.creditoFatura(1L, ValorMonetario.of(new BigDecimal("25.00")),
				LocalDate.of(2026, 9, 5), "Estorno compra", 30L, null, 40L, List.of());
		when(importacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(importacao));
		when(importacoes.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
		when(faturas.buscar(1L, 40L)).thenReturn(fatura);
		when(faturas.lancarCredito(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
				.thenReturn(transacaoCredito);
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), mock(TransacaoRepositoryPort.class),
				mock(RegistrarTransacaoUseCase.class), faturas, mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));

		ImportacaoFinanceira confirmada = service.confirmar(1L, 10L);

		assertThat(transacaoCredito.getTipo()).isEqualTo(TipoTransacao.ENTRADA);
		assertThat(confirmada.getLancamentos().getFirst().getEstado())
				.isEqualTo(com.finisus.domain.model.EstadoLancamentoImportado.CRIADA);
		verify(faturas).lancarCredito(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any());
		verify(faturas, never()).lancarGasto(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
	}

	@Test
	void confirmarNovamenteRetornaResultadoAnteriorSemNovoEfeito() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		RegistrarTransacaoUseCase registrar = mock(RegistrarTransacaoUseCase.class);
		FaturaUseCase faturas = mock(FaturaUseCase.class);
		ObrigacaoFinanceiraUseCase obrigacoes = mock(ObrigacaoFinanceiraUseCase.class);
		ImportacaoFinanceira confirmada = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.CONFIRMADA, 30L, null, 1,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 1), "Linha", "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, true, null, null, 99L)));
		when(importacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(confirmada));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), mock(TransacaoRepositoryPort.class), registrar, faturas,
				obrigacoes, mock(ObrigacaoFinanceiraRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));

		ImportacaoFinanceira resultado = service.confirmar(1L, 10L);

		assertThat(resultado).isSameAs(confirmada);
		verify(importacoes, never()).salvar(org.mockito.ArgumentMatchers.any());
		verify(registrar, never()).registrar(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
		verify(faturas, never()).lancarGasto(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
		verify(obrigacoes, never()).criar(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
	}

	@Test
	void iniciarPreservaIntencaoEDestinoERetornaDivergenciaDoLeitor() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		BancoUseCase bancos = mock(BancoUseCase.class);
		LeitorDocumentoFinanceiroPort leitor = mock(LeitorDocumentoFinanceiroPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		byte[] conteudo = "%PDF-fatura".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		var banco = com.finisus.domain.model.Banco.reconstituir(2L, "Banco", "001", true, 1L);
		Conta conta = Conta.reconstituir(30L, 1L, "Conta", TipoConta.CORRENTE, 2L,
				ValorMonetario.zero(), 0);
		when(bancos.buscar(1L, 2L)).thenReturn(banco);
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(conta));
		when(importacoes.buscarPorUsuarioBancoEHash(org.mockito.ArgumentMatchers.eq(1L),
				org.mockito.ArgumentMatchers.eq(2L), org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());
		when(leitor.ler(org.mockito.ArgumentMatchers.any())).thenReturn(new LeitorDocumentoFinanceiroPort.DocumentoLido(
				"leitor", TipoDocumentoFinanceiro.FATURA_CARTAO, null, null, null, null, null, null, null, List.of()));
		when(usuarios.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(mock(com.finisus.domain.model.Usuario.class)));
		when(importacoes.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(transacoes.listarPorUsuario(1L)).thenReturn(List.of());
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(bancos, importacoes, leitor, transacoes,
				mock(RegistrarTransacaoUseCase.class), mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), contas, mock(CategoriaRepositoryPort.class),
				mock(ItemRepositoryPort.class), usuarios);

		var revisao = service.iniciar(1L, new ImportacaoFinanceiraUseCase.IniciarCommand(2L,
				TipoDocumentoFinanceiro.EXTRATO_CONTA, 30L, null, "documento.pdf", "application/pdf", conteudo));

		assertThat(revisao.importacao().getTipoDocumentoPretendido())
				.isEqualTo(TipoDocumentoFinanceiro.EXTRATO_CONTA);
		assertThat(revisao.importacao().getContaId()).isEqualTo(30L);
		assertThat(revisao.divergencias()).singleElement().satisfies(divergencia -> {
			assertThat(divergencia.codigo()).isEqualTo("TIPO_DOCUMENTO_DIVERGENTE");
			assertThat(divergencia.esperado()).isEqualTo("EXTRATO_CONTA");
			assertThat(divergencia.detectado()).isEqualTo("FATURA_CARTAO");
		});
	}

	@Test
	void recusaExtratoComContaDeOutroBancoAntesDaLeitura() {
		BancoUseCase bancos = mock(BancoUseCase.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		LeitorDocumentoFinanceiroPort leitor = mock(LeitorDocumentoFinanceiroPort.class);
		var banco = com.finisus.domain.model.Banco.reconstituir(2L, "Banco", "001", true, 1L);
		Conta conta = Conta.reconstituir(30L, 1L, "Outra", TipoConta.CORRENTE, 3L,
				ValorMonetario.zero(), 0);
		when(bancos.buscar(1L, 2L)).thenReturn(banco);
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(conta));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(bancos,
				mock(ImportacaoFinanceiraRepositoryPort.class), leitor, mock(TransacaoRepositoryPort.class),
				mock(RegistrarTransacaoUseCase.class), mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), contas, mock(CategoriaRepositoryPort.class),
				mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));

		assertThatThrownBy(() -> service.iniciar(1L, new ImportacaoFinanceiraUseCase.IniciarCommand(2L,
				TipoDocumentoFinanceiro.EXTRATO_CONTA, 30L, null, "documento.pdf", "application/pdf",
				"%PDF-extrato".getBytes(java.nio.charset.StandardCharsets.US_ASCII))))
				.isInstanceOf(DomainException.class).hasMessage("error.importacao.destino.incompativel");
		verify(leitor, never()).ler(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void iniciarRevalidaDocumentoDepoisDeBloquearUsuario() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		BancoUseCase bancos = mock(BancoUseCase.class);
		LeitorDocumentoFinanceiroPort leitor = mock(LeitorDocumentoFinanceiroPort.class);
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		byte[] conteudo = "%PDF-concorrente".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		String hash = "15b5ce9428324ff4e23378bfd39f8c33942a67b658fd71c242fe02a246ca665c";
		var banco = com.finisus.domain.model.Banco.reconstituir(2L, "Banco", "001", true, 1L);
		ImportacaoFinanceira existente = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "original.pdf", hash,
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0, List.of());
		when(bancos.buscar(1L, 2L)).thenReturn(banco);
		when(importacoes.buscarPorUsuarioBancoEHash(1L, 2L, hash))
				.thenReturn(Optional.empty(), Optional.of(existente));
		when(leitor.ler(org.mockito.ArgumentMatchers.any())).thenReturn(new LeitorDocumentoFinanceiroPort.DocumentoLido(
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null, List.of()));
		when(usuarios.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(mock(com.finisus.domain.model.Usuario.class)));
		when(transacoes.listarPorUsuario(1L)).thenReturn(List.of());
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(bancos, importacoes, leitor, transacoes,
				mock(RegistrarTransacaoUseCase.class), mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), usuarios);

		var revisao = service.iniciar(1L, new ImportacaoFinanceiraUseCase.IniciarCommand(2L, "concorrente.pdf",
				"application/pdf", conteudo));

		assertThat(revisao.importacao()).isSameAs(existente);
		verify(usuarios).buscarPorIdParaAtualizacao(1L);
		verify(importacoes, never()).salvar(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void buscarCobrancaApresentaObrigacaoComoPossivelDuplicidade() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		ObrigacaoFinanceiraRepositoryPort obrigacoesRepository = mock(ObrigacaoFinanceiraRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "boleto.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.COBRANCA, "Empresa", null, null, LocalDate.of(2026, 9, 10),
				null, null, new BigDecimal("150.00"), StatusImportacaoFinanceira.PENDENTE_REVISAO, 30L, null, 0,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 10), "Mensalidade",
						"Mensalidade", new BigDecimal("150.00"), TipoTransacao.SAIDA, false, null, true,
						null, null, null)));
		ObrigacaoFinanceira existente = ObrigacaoFinanceira.reconstituir(40L, 1L, "  MENSALIDADE ", "Empresa",
				ValorMonetario.of(new BigDecimal("150.00")), LocalDate.of(2026, 9, 10), 30L, null,
				StatusObrigacaoFinanceira.EM_ABERTO, null, null, null, 0);
		when(importacoes.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(importacao));
		when(obrigacoesRepository.buscarCandidatosImportacao(1L, 30L, LocalDate.of(2026, 9, 7),
				LocalDate.of(2026, 9, 13), new BigDecimal("150.00"))).thenReturn(List.of(existente));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), transacoes, mock(RegistrarTransacaoUseCase.class),
				mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class), obrigacoesRepository,
				mock(ContaRepositoryPort.class), mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class),
				mock(UsuarioRepositoryPort.class));

		var revisao = service.buscar(1L, 10L);

		assertThat(revisao.possiveisDuplicidades()).singleElement().satisfies(duplicidade -> {
			assertThat(duplicidade.transacaoId()).isNull();
			assertThat(duplicidade.obrigacaoFinanceiraId()).isEqualTo(40L);
			assertThat(duplicidade.nivel()).isEqualTo(ImportacaoFinanceiraUseCase.NivelDuplicidade.EXATA);
			assertThat(duplicidade.evidencias()).contains("MESMO_VALOR", "DATA_EXATA", "DESCRICAO_EXATA");
		});
		verify(transacoes, never()).listarPorUsuario(org.mockito.ArgumentMatchers.anyLong());
	}

	@Test
	void associarTransacaoExistenteConfirmaSemCriarNovoEfeitoFinanceiro() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		RegistrarTransacaoUseCase registrar = mock(RegistrarTransacaoUseCase.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 1), "Linha", "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, true, null, null, null)));
		Transacao existente = Transacao.nova(1L, TipoTransacao.SAIDA, ValorMonetario.of(new BigDecimal("10.00")),
				LocalDate.of(2026, 9, 1), "Linha", 30L, null, null, List.of());
		when(importacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(importacao));
		when(importacoes.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(mock(com.finisus.domain.model.Conta.class)));
		when(transacoes.buscarPorIdEUsuario(99L, 1L)).thenReturn(Optional.of(existente));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), transacoes, registrar, mock(FaturaUseCase.class),
				mock(ObrigacaoFinanceiraUseCase.class), mock(ObrigacaoFinanceiraRepositoryPort.class), contas,
				mock(CategoriaRepositoryPort.class),
				mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));
		var command = new ImportacaoFinanceiraUseCase.RevisarCommand(30L, null,
				List.of(new ImportacaoFinanceiraUseCase.LancamentoCommand(20L, LocalDate.of(2026, 9, 1), "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, true, null, null, 99L, null,
						"Correspondência conferida")));

		service.revisar(1L, 10L, command);
		assertThat(importacao.getLancamentos().getFirst().getRevisoes()).singleElement().satisfies(revisao -> {
			assertThat(revisao.revisadoPor()).isEqualTo(1L);
			assertThat(revisao.decisao().name()).isEqualTo("ASSOCIAR_TRANSACAO");
			assertThat(revisao.justificativa()).isEqualTo("Correspondência conferida");
		});
		ImportacaoFinanceira confirmada = service.confirmar(1L, 10L);

		assertThat(confirmada.getStatus()).isEqualTo(StatusImportacaoFinanceira.CONFIRMADA);
		assertThat(confirmada.getLancamentos().getFirst().getTransacaoId()).isEqualTo(99L);
		verify(registrar, never()).registrar(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.any());
	}

	@Test
	void sugerePontaDaTransferenciaEExplicitaAgregadoDeOrigem() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, 30L, null, 0,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 1), "Transferência",
						"Transferência", new BigDecimal("250.00"), TipoTransacao.SAIDA, false, null, true,
						null, null, null)));
		Transacao ponta = Transacao.reconstituirComTransferencia(99L, 1L, TipoTransacao.SAIDA,
				ValorMonetario.of(new BigDecimal("250.00")), LocalDate.of(2026, 9, 2), "Transferência", 30L,
				null, null, null, null, null, null, 50L, null, 0, List.of());
		when(importacoes.buscarPorIdEUsuario(10L, 1L)).thenReturn(Optional.of(importacao));
		when(transacoes.buscarCandidatosImportacaoPorConta(1L, 30L, TipoTransacao.SAIDA,
				new BigDecimal("250.00"), LocalDate.of(2026, 8, 29), LocalDate.of(2026, 9, 4)))
				.thenReturn(List.of(ponta));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), transacoes, mock(RegistrarTransacaoUseCase.class),
				mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));

		var revisao = service.buscar(1L, 10L);

		assertThat(revisao.possiveisDuplicidades()).singleElement().satisfies(duplicidade -> {
			assertThat(duplicidade.transacaoId()).isEqualTo(99L);
			assertThat(duplicidade.transferenciaId()).isEqualTo(50L);
			assertThat(duplicidade.nivel()).isEqualTo(ImportacaoFinanceiraUseCase.NivelDuplicidade.PROVAVEL);
			assertThat(duplicidade.evidencias()).contains("MESMO_DESTINO", "MESMO_TIPO", "MESMO_VALOR",
					"DATA_PROXIMA", "DESCRICAO_EXATA");
		});
		verify(transacoes, never()).listarPorUsuario(org.mockito.ArgumentMatchers.anyLong());
	}

	@Test
	void recusaAssociacaoQuandoTipoDataOuValorNaoCorrespondemAoMovimento() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		ContaRepositoryPort contas = mock(ContaRepositoryPort.class);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 1), "Linha", "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, true, null, null, null)));
		Transacao existente = Transacao.nova(1L, TipoTransacao.ENTRADA, ValorMonetario.of(new BigDecimal("10.00")),
				LocalDate.of(2026, 9, 1), "Linha", 30L, null, null, List.of());
		when(importacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(importacao));
		when(contas.buscarPorIdEUsuario(30L, 1L)).thenReturn(Optional.of(mock(com.finisus.domain.model.Conta.class)));
		when(transacoes.buscarPorIdEUsuario(99L, 1L)).thenReturn(Optional.of(existente));
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), transacoes, mock(RegistrarTransacaoUseCase.class),
				mock(FaturaUseCase.class), mock(ObrigacaoFinanceiraUseCase.class),
				mock(ObrigacaoFinanceiraRepositoryPort.class), contas, mock(CategoriaRepositoryPort.class),
				mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));
		var command = new ImportacaoFinanceiraUseCase.RevisarCommand(30L, null,
				List.of(new ImportacaoFinanceiraUseCase.LancamentoCommand(20L, LocalDate.of(2026, 9, 1), "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, true, null, null, 99L, null,
						"Correspondência conferida")));

		assertThatThrownBy(() -> service.revisar(1L, 10L, command)).isInstanceOf(DomainException.class)
				.extracting("messageKey").isEqualTo("error.importacao.revisao.invalida");
		verify(importacoes, never()).salvar(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void iniciarReaproveitaImportacaoDoMesmoArquivoParaUsuarioEBanco() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		BancoUseCase bancos = mock(BancoUseCase.class);
		LeitorDocumentoFinanceiroPort leitor = mock(LeitorDocumentoFinanceiroPort.class);
		TransacaoRepositoryPort transacoes = mock(TransacaoRepositoryPort.class);
		byte[] conteudo = "%PDF-conteudo".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		var banco = com.finisus.domain.model.Banco.reconstituir(2L, "Banco", "001", true, 1L);
		ImportacaoFinanceira existente = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf",
				"0c8a3b82be09a10e8004608dd2c85bc71d6111646e82297fd1ea514b52869cbb", "leitor",
				TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0, List.of());
		when(bancos.buscar(1L, 2L)).thenReturn(banco);
		when(importacoes.buscarPorUsuarioBancoEHash(1L, 2L,
				"0c8a3b82be09a10e8004608dd2c85bc71d6111646e82297fd1ea514b52869cbb"))
				.thenReturn(Optional.of(existente));
		when(transacoes.listarPorUsuario(1L)).thenReturn(List.of());
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(bancos, importacoes, leitor, transacoes,
				mock(RegistrarTransacaoUseCase.class), mock(FaturaUseCase.class),
				mock(ObrigacaoFinanceiraUseCase.class), mock(ObrigacaoFinanceiraRepositoryPort.class),
				mock(ContaRepositoryPort.class),
				mock(CategoriaRepositoryPort.class), mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));

		var revisao = service.iniciar(1L, new ImportacaoFinanceiraUseCase.IniciarCommand(2L, "reenvio.pdf",
				"application/pdf", conteudo));

		assertThat(revisao.importacao()).isSameAs(existente);
		verify(leitor, never()).ler(org.mockito.ArgumentMatchers.any());
		verify(importacoes, never()).salvar(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void revisarRecusaCategoriaDeOutroUsuarioMesmoEmLinhaIgnorada() {
		ImportacaoFinanceiraRepositoryPort importacoes = mock(ImportacaoFinanceiraRepositoryPort.class);
		CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
		ImportacaoFinanceira importacao = ImportacaoFinanceira.reconstituir(10L, 1L, 2L, "extrato.pdf", "hash",
				"leitor", TipoDocumentoFinanceiro.EXTRATO_CONTA, null, null, null, null, null, null, null,
				StatusImportacaoFinanceira.PENDENTE_REVISAO, null, null, 0,
				List.of(LancamentoImportado.reconstituir(20L, 1, LocalDate.of(2026, 9, 1), "Linha", "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, null, true, null, null, null)));
		when(importacoes.buscarPorIdEUsuarioParaAtualizacao(10L, 1L)).thenReturn(Optional.of(importacao));
		when(categorias.buscarPorIdEUsuario(99L, 1L)).thenReturn(Optional.empty());
		ImportacaoFinanceiraService service = new ImportacaoFinanceiraService(mock(BancoUseCase.class), importacoes,
				mock(LeitorDocumentoFinanceiroPort.class), mock(TransacaoRepositoryPort.class),
				mock(RegistrarTransacaoUseCase.class), mock(FaturaUseCase.class),
				mock(ObrigacaoFinanceiraUseCase.class), mock(ObrigacaoFinanceiraRepositoryPort.class),
				mock(ContaRepositoryPort.class), categorias,
				mock(ItemRepositoryPort.class), mock(UsuarioRepositoryPort.class));
		var command = new ImportacaoFinanceiraUseCase.RevisarCommand(null, null,
				List.of(new ImportacaoFinanceiraUseCase.LancamentoCommand(20L, LocalDate.of(2026, 9, 1), "Linha",
						new BigDecimal("10.00"), TipoTransacao.SAIDA, false, 99L, null, null, null,
						"Linha descartada")));

		assertThatThrownBy(() -> service.revisar(1L, 10L, command)).isInstanceOf(DomainException.class);
		verify(importacoes, never()).salvar(org.mockito.ArgumentMatchers.any());
	}
}
