package com.finisus.application.service;

import com.finisus.application.ports.in.AutenticarUsuarioUseCase;
import com.finisus.application.ports.in.CadastrarUsuarioUseCase;
import com.finisus.application.ports.in.GerenciarAcessoUsuarioUseCase;
import com.finisus.application.ports.in.RenovarTokenUseCase;
import com.finisus.application.ports.out.PasswordEncoderPort;
import com.finisus.application.ports.out.RefreshTokenRepositoryPort;
import com.finisus.application.ports.out.TokenPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.AcessoNegadoException;
import com.finisus.domain.CredenciaisInvalidasException;
import com.finisus.domain.RecursoNaoEncontradoException;
import com.finisus.domain.model.PermissaoUsuario;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import org.springframework.transaction.annotation.Transactional;

public class AutenticacaoService implements CadastrarUsuarioUseCase, AutenticarUsuarioUseCase, RenovarTokenUseCase,
		GerenciarAcessoUsuarioUseCase {

	private final UsuarioRepositoryPort usuarioRepository;
	private final PasswordEncoderPort passwordEncoder;
	private final TokenPort tokenPort;
	private final RefreshTokenRepositoryPort refreshTokenRepository;
	private final ObterDataAtualPort dataAtual;

	public AutenticacaoService(UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder,
			TokenPort tokenPort, RefreshTokenRepositoryPort refreshTokenRepository, ObterDataAtualPort dataAtual) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenPort = tokenPort;
		this.refreshTokenRepository = refreshTokenRepository;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public CadastrarUsuarioUseCase.Result executar(Long usuarioSolicitanteId, CadastrarUsuarioUseCase.Command command) {
		exigirPermissao(usuarioSolicitanteId, PermissaoUsuario.USUARIO_CADASTRAR);
		Email email = new Email(command.email());
		if (usuarioRepository.existePorEmail(email.valor())) {
			throw new DomainException("error.usuario.email.existe");
		}
		String senhaHash = passwordEncoder.encode(command.senha());
		Usuario usuario = Usuario.novo(command.nome(), email, senhaHash, dataAtual.obterDataHora());
		Usuario salvo = usuarioRepository.salvar(usuario);
		return new CadastrarUsuarioUseCase.Result(salvo.getId(), salvo.getNome(), salvo.getEmail().valor());
	}

	@Override
	@Transactional(noRollbackFor = CredenciaisInvalidasException.class)
	public AutenticarUsuarioUseCase.Result executar(AutenticarUsuarioUseCase.Command command) {
		Email email = new Email(command.email());
		Usuario usuario = usuarioRepository.buscarPorEmailParaAtualizacao(email.valor())
				.orElseThrow(CredenciaisInvalidasException::new);
		if (!usuario.isAtivo()) {
			throw new CredenciaisInvalidasException();
		}
		if (usuario.isBloqueado()) {
			throw new CredenciaisInvalidasException();
		}
		if (!passwordEncoder.matches(command.senha(), usuario.getSenhaHash())) {
			Usuario atualizado = usuario.registrarFalhaLogin();
			usuarioRepository.salvar(atualizado);
			if (atualizado.isBloqueado()) {
				refreshTokenRepository.invalidarTodosDoUsuario(atualizado.getId());
				throw new CredenciaisInvalidasException();
			}
			throw new CredenciaisInvalidasException();
		}
		Usuario atualizado = usuario.registrarLoginBemSucedido();
		if (atualizado != usuario) {
			atualizado = usuarioRepository.salvar(atualizado);
		}
		return emitirTokens(atualizado);
	}

	@Override
	@Transactional(noRollbackFor = CredenciaisInvalidasException.class)
	public RenovarTokenUseCase.Result executar(RenovarTokenUseCase.Command command) {
		Long usuarioId = refreshTokenRepository.buscarUsuarioIdPorToken(command.refreshToken())
				.orElseThrow(CredenciaisInvalidasException::new);
		refreshTokenRepository.invalidar(command.refreshToken());
		Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
				.orElseThrow(CredenciaisInvalidasException::new);
		if (!usuario.isAtivo()) {
			refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
			throw new CredenciaisInvalidasException();
		}
		if (usuario.isBloqueado()) {
			refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
			throw new CredenciaisInvalidasException();
		}
		AutenticarUsuarioUseCase.Result tokens = emitirTokens(usuario);
		return new RenovarTokenUseCase.Result(tokens.accessToken(), tokens.refreshToken(),
				tokens.accessTokenExpiraEm());
	}

	private AutenticarUsuarioUseCase.Result emitirTokens(Usuario usuario) {
		String accessToken = tokenPort.gerarAccessToken(usuario.getId(), usuario.getEmail().valor(),
				usuario.getSessaoVersao(), usuario.getPermissoes());
		String refreshToken = tokenPort.gerarRefreshToken(usuario.getId());
		refreshTokenRepository.salvar(refreshToken, usuario.getId(), tokenPort.expiracaoRefreshToken());
		return new AutenticarUsuarioUseCase.Result(usuario.getId(), accessToken, refreshToken,
				tokenPort.expiracaoAccessToken());
	}

	@Override
	@Transactional
	public void desbloquear(Long usuarioSolicitanteId, Long usuarioId) {
		exigirPermissao(usuarioSolicitanteId, PermissaoUsuario.USUARIO_DESBLOQUEAR);
		Usuario usuario = usuarioRepository.buscarPorIdParaAtualizacao(usuarioId)
				.orElseThrow(RecursoNaoEncontradoException::new);
		usuarioRepository.salvar(usuario.desbloquear());
		refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
	}

	private void exigirPermissao(Long usuarioId, PermissaoUsuario permissao) {
		Usuario usuario = usuarioRepository.buscarPorId(usuarioId).orElseThrow(AcessoNegadoException::new);
		if (!usuario.isAtivo() || usuario.isBloqueado() || !usuario.possuiPermissao(permissao)) {
			throw new AcessoNegadoException();
		}
	}
}
