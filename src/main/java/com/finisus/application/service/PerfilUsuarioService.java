package com.finisus.application.service;

import com.finisus.application.ports.in.GerenciarPerfilUseCase;
import com.finisus.application.ports.out.RefreshTokenRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.out.DadosPessoaisPort;
import com.finisus.application.ports.out.SolicitacaoPrivacidadeRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Usuario;
import com.finisus.domain.vo.Email;
import com.finisus.domain.model.SolicitacaoPrivacidade;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

public class PerfilUsuarioService implements GerenciarPerfilUseCase {
	private final UsuarioRepositoryPort usuarios;
	private final RefreshTokenRepositoryPort refreshTokens;
	private final DadosPessoaisPort dadosPessoais;
	private final SolicitacaoPrivacidadeRepositoryPort solicitacoes;
	private final Clock clock;

	public PerfilUsuarioService(UsuarioRepositoryPort usuarios, RefreshTokenRepositoryPort refreshTokens,
			DadosPessoaisPort dadosPessoais, SolicitacaoPrivacidadeRepositoryPort solicitacoes, Clock clock) {
		this.usuarios = usuarios;
		this.refreshTokens = refreshTokens;
		this.dadosPessoais = dadosPessoais;
		this.solicitacoes = solicitacoes;
		this.clock = clock;
	}

	public Usuario consultar(Long usuarioId) {
		return buscar(usuarioId);
	}

	public Usuario atualizar(Long usuarioId, AtualizarCommand command) {
		Usuario atual = buscar(usuarioId);
		Email email = new Email(command.email());
		if (!atual.getEmail().valor().equals(email.valor()) && usuarios.existePorEmail(email.valor()))
			throw new DomainException("error.usuario.email.existe");
		return usuarios.salvar(Usuario.reconstituir(atual.getId(), command.nome(), email, atual.getSenhaHash(),
				atual.isAtivo(), atual.getCriadoEm(), atual.getSessaoVersao(), atual.getTentativasLoginInvalidas(),
				atual.isBloqueado(), atual.getPermissoes()));
	}

	public void desativar(Long usuarioId) {
		Usuario atual = buscar(usuarioId);
		usuarios.salvar(Usuario.reconstituir(atual.getId(), atual.getNome(), atual.getEmail(), atual.getSenhaHash(),
				false, atual.getCriadoEm(), atual.getSessaoVersao() + 1, atual.getTentativasLoginInvalidas(),
				atual.isBloqueado(), atual.getPermissoes()));
		refreshTokens.invalidarTodosDoUsuario(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public ExportacaoDados exportarDados(Long usuarioId) {
		buscar(usuarioId);
		return new ExportacaoDados(1, clock.instant(), dadosPessoais.exportar(usuarioId));
	}

	@Override
	@Transactional
	public SolicitacaoPrivacidade solicitarAnonimizacao(Long usuarioId, String motivo) {
		usuarios.buscarPorIdParaAtualizacao(usuarioId).orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
		if (motivo == null || motivo.isBlank() || motivo.length() > 500)
			throw new DomainException("error.privacidade.solicitacao.invalida");
		var existente = solicitacoes.buscarAberta(usuarioId, SolicitacaoPrivacidade.Tipo.ANONIMIZACAO);
		if (existente.isPresent()) return existente.get();
		return solicitacoes.salvar(new SolicitacaoPrivacidade(null, usuarioId,
				SolicitacaoPrivacidade.Tipo.ANONIMIZACAO, SolicitacaoPrivacidade.Status.SOLICITADA, motivo.trim(),
				LocalDateTime.now(clock), null, null, 0));
	}

	@Override
	@Transactional(readOnly = true)
	public java.util.List<SolicitacaoPrivacidade> listarSolicitacoesPrivacidade(Long usuarioId) {
		buscar(usuarioId);
		return solicitacoes.listarPorUsuario(usuarioId);
	}

	private Usuario buscar(Long id) {
		return usuarios.buscarPorId(id).orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
