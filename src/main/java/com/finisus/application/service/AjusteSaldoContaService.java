package com.finisus.application.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.ports.in.AjustarSaldoContaUseCase;
import com.finisus.application.ports.out.AjusteSaldoContaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.AjusteSaldoConta;
import com.finisus.domain.model.Conta;
import com.finisus.domain.vo.ValorMonetario;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

public class AjusteSaldoContaService implements AjustarSaldoContaUseCase {
	private final AjusteSaldoContaRepositoryPort ajustes;
	private final ContaRepositoryPort contas;
	private final ObterDataAtualPort dataAtual;

	public AjusteSaldoContaService(AjusteSaldoContaRepositoryPort ajustes, ContaRepositoryPort contas,
			ObterDataAtualPort dataAtual) {
		this.ajustes = ajustes;
		this.contas = contas;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public AjusteSaldoConta ajustar(Long usuarioId, Long contaId, String chaveIdempotencia, AjustarCommand command) {
		validar(usuarioId, contaId, chaveIdempotencia, command);
		String chave = chaveIdempotencia.trim();
		String hash = hash(contaId, command);
		var existente = ajustes.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(existente.get(), hash);

		List<Conta> bloqueadas = contas.buscarPorIdsEUsuarioParaAtualizacao(List.of(contaId), usuarioId);
		if (bloqueadas.size() != 1) throw new DomainException("error.recurso.nao.encontrado");
		existente = ajustes.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(existente.get(), hash);

		Conta conta = bloqueadas.getFirst();
		var movimentos = contas.calcularMovimentosEficazes(contaId, usuarioId);
		AjusteSaldoConta ajuste = ajustes.salvar(AjusteSaldoConta.novo(usuarioId, contaId,
				conta.getSaldo().valor(), movimentos.saldoCalculado(), command.saldoInformado(), command.motivo(),
				dataAtual.obter(), chave, hash));
		contas.salvar(Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), ValorMonetario.of(command.saldoInformado()), conta.isAtivo(), conta.getVersion()));
		return ajuste;
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<AjusteSaldoConta> listar(Long usuarioId, Long contaId, Paginacao paginacao) {
		if (!contas.existePorIdEUsuario(contaId, usuarioId))
			throw new DomainException("error.recurso.nao.encontrado");
		return ajustes.listarPorContaEUsuario(contaId, usuarioId, paginacao);
	}

	private AjusteSaldoConta validarRepeticao(AjusteSaldoConta existente, String hash) {
		if (!existente.hashRequisicao().equals(hash))
			throw new DomainException("error.ajuste.saldo.idempotencia.conflitante");
		return existente;
	}

	private void validar(Long usuarioId, Long contaId, String chave, AjustarCommand command) {
		if (usuarioId == null || contaId == null || command == null || command.saldoInformado() == null
				|| command.saldoInformado().signum() < 0 || command.motivo() == null || command.motivo().isBlank()
				|| chave == null || chave.isBlank() || chave.length() > 100)
			throw new DomainException("error.ajuste.saldo.invalido");
	}

	private String hash(Long contaId, AjustarCommand command) {
		String payload = contaId + "|" + command.saldoInformado().stripTrailingZeros().toPlainString() + "|"
				+ command.motivo().trim();
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(payload.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 indisponível", e);
		}
	}
}
