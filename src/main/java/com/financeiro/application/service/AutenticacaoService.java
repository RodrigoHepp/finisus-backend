package com.financeiro.application.service;

import com.financeiro.application.ports.in.AutenticarUsuarioUseCase;
import com.financeiro.application.ports.in.CadastrarUsuarioUseCase;
import com.financeiro.application.ports.in.RenovarTokenUseCase;
import com.financeiro.application.ports.out.PasswordEncoderPort;
import com.financeiro.application.ports.out.RefreshTokenRepositoryPort;
import com.financeiro.application.ports.out.TokenPort;
import com.financeiro.application.ports.out.UsuarioRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.Usuario;
import com.financeiro.domain.vo.Email;

public class AutenticacaoService implements CadastrarUsuarioUseCase, AutenticarUsuarioUseCase, RenovarTokenUseCase {

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
	public CadastrarUsuarioUseCase.Result executar(CadastrarUsuarioUseCase.Command command) {
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
	public AutenticarUsuarioUseCase.Result executar(AutenticarUsuarioUseCase.Command command) {
		Email email = new Email(command.email());
		Usuario usuario = usuarioRepository.buscarPorEmail(email.valor())
				.orElseThrow(() -> new DomainException("error.auth.invalid"));
		if (!usuario.isAtivo()) {
			throw new DomainException("error.usuario.inativo");
		}
		if (!passwordEncoder.matches(command.senha(), usuario.getSenhaHash())) {
			throw new DomainException("error.auth.invalid");
		}
		return emitirTokens(usuario);
	}

	@Override
	public RenovarTokenUseCase.Result executar(RenovarTokenUseCase.Command command) {
		Long usuarioId = refreshTokenRepository.buscarUsuarioIdPorToken(command.refreshToken())
				.orElseThrow(() -> new DomainException("error.auth.refresh.invalid"));
		refreshTokenRepository.invalidar(command.refreshToken());
		Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
				.orElseThrow(() -> new DomainException("error.auth.invalid"));
		if (!usuario.isAtivo()) {
			refreshTokenRepository.invalidarTodosDoUsuario(usuarioId);
			throw new DomainException("error.usuario.inativo");
		}
		AutenticarUsuarioUseCase.Result tokens = emitirTokens(usuario);
		return new RenovarTokenUseCase.Result(tokens.accessToken(), tokens.refreshToken(),
				tokens.accessTokenExpiraEm());
	}

	private AutenticarUsuarioUseCase.Result emitirTokens(Usuario usuario) {
		String accessToken = tokenPort.gerarAccessToken(usuario.getId(), usuario.getEmail().valor(),
				usuario.getSessaoVersao());
		String refreshToken = tokenPort.gerarRefreshToken(usuario.getId());
		refreshTokenRepository.salvar(refreshToken, usuario.getId(), tokenPort.expiracaoRefreshToken());
		return new AutenticarUsuarioUseCase.Result(usuario.getId(), accessToken, refreshToken,
				tokenPort.expiracaoAccessToken());
	}
}
