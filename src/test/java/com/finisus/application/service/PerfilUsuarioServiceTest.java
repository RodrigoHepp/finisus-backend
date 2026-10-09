package com.finisus.application.service;

import com.finisus.application.ports.out.RefreshTokenRepositoryPort;
import com.finisus.application.ports.out.DadosPessoaisPort;
import com.finisus.application.ports.out.SolicitacaoPrivacidadeRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.model.SolicitacaoPrivacidade;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PerfilUsuarioServiceTest {
	@Test
	void exportaDadosSomenteDepoisDeValidarOTitular() {
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		RefreshTokenRepositoryPort refreshTokens = mock(RefreshTokenRepositoryPort.class);
		DadosPessoaisPort dadosPessoais = mock(DadosPessoaisPort.class);
		SolicitacaoPrivacidadeRepositoryPort solicitacoes = mock(SolicitacaoPrivacidadeRepositoryPort.class);
		Usuario atual = Usuario.reconstituir(42L, "Maria", new Email("maria@example.com"), "hash", true,
				LocalDateTime.of(2026, 1, 10, 9, 30), 7);
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(atual));
		when(dadosPessoais.exportar(42L)).thenReturn(Map.of("perfil", List.of(Map.of("id", 42L))));
		Clock clock = Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

		var exportacao = new PerfilUsuarioService(usuarios, refreshTokens, dadosPessoais, solicitacoes, clock)
				.exportarDados(42L);

		assertThat(exportacao.geradaEm()).isEqualTo(clock.instant());
		assertThat(exportacao.secoes()).containsKey("perfil");
		verify(dadosPessoais).exportar(42L);
	}

	@Test
	void naoDuplicaSolicitacaoDeAnonimizacaoAberta() {
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		RefreshTokenRepositoryPort refreshTokens = mock(RefreshTokenRepositoryPort.class);
		DadosPessoaisPort dadosPessoais = mock(DadosPessoaisPort.class);
		SolicitacaoPrivacidadeRepositoryPort solicitacoes = mock(SolicitacaoPrivacidadeRepositoryPort.class);
		Usuario atual = Usuario.reconstituir(42L, "Maria", new Email("maria@example.com"), "hash", true,
				LocalDateTime.of(2026, 1, 10, 9, 30), 7);
		var aberta = new SolicitacaoPrivacidade(9L, 42L, SolicitacaoPrivacidade.Tipo.ANONIMIZACAO,
				SolicitacaoPrivacidade.Status.SOLICITADA, "motivo original",
				LocalDateTime.of(2026, 9, 30, 9, 0), null, null, 0);
		when(usuarios.buscarPorIdParaAtualizacao(42L)).thenReturn(Optional.of(atual));
		when(solicitacoes.buscarAberta(42L, SolicitacaoPrivacidade.Tipo.ANONIMIZACAO))
				.thenReturn(Optional.of(aberta));

		var resultado = new PerfilUsuarioService(usuarios, refreshTokens, dadosPessoais, solicitacoes,
				Clock.systemUTC()).solicitarAnonimizacao(42L, "novo motivo");

		assertThat(resultado).isSameAs(aberta);
		verify(solicitacoes).buscarAberta(42L, SolicitacaoPrivacidade.Tipo.ANONIMIZACAO);
		verifyNoMoreInteractions(solicitacoes);
	}

	@Test
	void desativaContaPreservandoIdentidadeERevogaSessoes() {
		UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
		RefreshTokenRepositoryPort refreshTokens = mock(RefreshTokenRepositoryPort.class);
		DadosPessoaisPort dadosPessoais = mock(DadosPessoaisPort.class);
		SolicitacaoPrivacidadeRepositoryPort solicitacoes = mock(SolicitacaoPrivacidadeRepositoryPort.class);
		LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 10, 9, 30);
		Usuario atual = Usuario.reconstituir(42L, "Maria", new Email("maria@example.com"), "hash", true,
				criadoEm, 7);
		when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(atual));

		new PerfilUsuarioService(usuarios, refreshTokens, dadosPessoais, solicitacoes, Clock.systemUTC())
				.desativar(42L);

		var usuarioSalvo = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarios).salvar(usuarioSalvo.capture());
		Usuario desativado = usuarioSalvo.getValue();
		assertThat(desativado.getId()).isEqualTo(42L);
		assertThat(desativado.getNome()).isEqualTo("Maria");
		assertThat(desativado.getEmail().valor()).isEqualTo("maria@example.com");
		assertThat(desativado.getSenhaHash()).isEqualTo("hash");
		assertThat(desativado.getCriadoEm()).isEqualTo(criadoEm);
		assertThat(desativado.isAtivo()).isFalse();
		assertThat(desativado.getSessaoVersao()).isEqualTo(8);
		verify(refreshTokens).invalidarTodosDoUsuario(42L);
	}
}
