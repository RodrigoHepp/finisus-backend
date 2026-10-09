package com.finisus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.finisus.application.ports.in.AutenticarUsuarioUseCase;
import com.finisus.application.ports.in.RenovarTokenUseCase;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.PasswordEncoderPort;
import com.finisus.application.ports.out.RefreshTokenRepositoryPort;
import com.finisus.application.ports.out.TokenPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.domain.AcessoNegadoException;
import com.finisus.domain.CredenciaisInvalidasException;
import com.finisus.domain.model.PermissaoUsuario;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;

class AutenticacaoServiceTest {
	private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
	private final PasswordEncoderPort senhas = mock(PasswordEncoderPort.class);
	private final TokenPort tokens = mock(TokenPort.class);
	private final RefreshTokenRepositoryPort refreshTokens = mock(RefreshTokenRepositoryPort.class);
	private final ObterDataAtualPort dataAtual = mock(ObterDataAtualPort.class);
	private final AtomicReference<Usuario> persistido = new AtomicReference<>();
	private final AutenticacaoService service = new AutenticacaoService(usuarios, senhas, tokens, refreshTokens,
			dataAtual);

	@BeforeEach
	void configurar() {
		when(usuarios.buscarPorEmailParaAtualizacao("ana@example.com"))
				.thenAnswer(invocacao -> Optional.ofNullable(persistido.get()));
		when(usuarios.salvar(any(Usuario.class))).thenAnswer(invocacao -> {
			Usuario salvo = invocacao.getArgument(0);
			persistido.set(salvo);
			return salvo;
		});
		when(senhas.matches("senha-correta", "hash")).thenReturn(true);
		when(tokens.gerarAccessToken(any(), any(), any(Long.class), any())).thenReturn("access-token");
		when(tokens.gerarRefreshToken(any())).thenReturn("refresh-token");
		when(tokens.expiracaoAccessToken()).thenReturn(Instant.parse("2026-10-08T12:00:00Z"));
		when(tokens.expiracaoRefreshToken()).thenReturn(Instant.parse("2026-10-09T12:00:00Z"));
	}

	@Test
	void quintaFalhaBloqueiaERecusaSenhaCorretaAteDesbloqueioManual() {
		persistido.set(usuario(1L, 0, false, 0, Set.of()));
		for (int tentativa = 1; tentativa <= 4; tentativa++) {
			assertThatThrownBy(() -> autenticar("senha-incorreta"))
					.isInstanceOf(CredenciaisInvalidasException.class);
		}
		assertThat(persistido.get().getTentativasLoginInvalidas()).isEqualTo(4);
		assertThatThrownBy(() -> autenticar("senha-incorreta")).isInstanceOf(CredenciaisInvalidasException.class);
		assertThat(persistido.get().getTentativasLoginInvalidas()).isEqualTo(5);
		assertThat(persistido.get().isBloqueado()).isTrue();
		verify(refreshTokens).invalidarTodosDoUsuario(1L);
		assertThatThrownBy(() -> autenticar("senha-correta")).isInstanceOf(CredenciaisInvalidasException.class);
		verify(tokens, never()).gerarAccessToken(any(), any(), any(Long.class), any());
	}

	@Test
	void refreshDeUsuarioBloqueadoRevogaTokensERetornaErroGenerico() {
		Usuario bloqueado = usuario(1L, 5, true, 1, Set.of());
		when(refreshTokens.buscarUsuarioIdPorToken("refresh-bloqueado")).thenReturn(Optional.of(1L));
		when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(bloqueado));

		assertThatThrownBy(() -> service.executar(new RenovarTokenUseCase.Command("refresh-bloqueado")))
				.isInstanceOf(CredenciaisInvalidasException.class);
		verify(refreshTokens).invalidar("refresh-bloqueado");
		verify(refreshTokens).invalidarTodosDoUsuario(1L);
	}

	@Test
	void refreshInexistenteRetornaMesmoErroGenerico() {
		when(refreshTokens.buscarUsuarioIdPorToken("refresh-inexistente")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.executar(new RenovarTokenUseCase.Command("refresh-inexistente")))
				.isInstanceOf(CredenciaisInvalidasException.class);
	}

	@Test
	void loginCorretoAntesDaQuintaFalhaZeraContador() {
		persistido.set(usuario(1L, 4, false, 0, Set.of()));
		var resultado = autenticar("senha-correta");
		assertThat(resultado.accessToken()).isEqualTo("access-token");
		assertThat(persistido.get().getTentativasLoginInvalidas()).isZero();
		assertThat(persistido.get().isBloqueado()).isFalse();
	}

	@Test
	void desbloqueioExigePermissaoZeraBloqueioERevogaSessoes() {
		Usuario administrador = usuario(10L, 0, false, 0, Set.of(PermissaoUsuario.USUARIO_DESBLOQUEAR));
		Usuario bloqueado = usuario(20L, 5, true, 3, Set.of());
		persistido.set(bloqueado);
		when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(administrador));
		when(usuarios.buscarPorIdParaAtualizacao(20L)).thenReturn(Optional.of(bloqueado));
		service.desbloquear(10L, 20L);
		assertThat(persistido.get().isBloqueado()).isFalse();
		assertThat(persistido.get().getTentativasLoginInvalidas()).isZero();
		assertThat(persistido.get().getSessaoVersao()).isEqualTo(4);
		verify(refreshTokens).invalidarTodosDoUsuario(20L);
	}

	@Test
	void desbloqueioSemPermissaoEhNegadoSemAlterarAlvo() {
		when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(usuario(10L, 0, false, 0, Set.of())));
		assertThatThrownBy(() -> service.desbloquear(10L, 20L)).isInstanceOf(AcessoNegadoException.class);
		verify(usuarios, never()).buscarPorIdParaAtualizacao(20L);
		verify(refreshTokens, never()).invalidarTodosDoUsuario(20L);
	}

	private AutenticarUsuarioUseCase.Result autenticar(String senha) {
		return service.executar(new AutenticarUsuarioUseCase.Command("ana@example.com", senha));
	}

	private Usuario usuario(Long id, int tentativas, boolean bloqueado, long sessaoVersao,
			Set<PermissaoUsuario> permissoes) {
		return Usuario.reconstituir(id, "Ana", new Email("ana@example.com"), "hash", true,
				LocalDateTime.of(2026, 10, 8, 9, 0), sessaoVersao, tentativas, bloqueado, permissoes);
	}
}
