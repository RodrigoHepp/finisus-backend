package com.finisus.application.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.finisus.application.ports.in.TransferenciaContaUseCase;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.TransferenciaContaRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.StatusTransferencia;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransferenciaConta;
import com.finisus.domain.vo.ValorMonetario;

public class TransferenciaContaService implements TransferenciaContaUseCase {
	private final TransferenciaContaRepositoryPort transferencias;
	private final ContaRepositoryPort contas;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;

	public TransferenciaContaService(TransferenciaContaRepositoryPort transferencias, ContaRepositoryPort contas,
			TransacaoRepositoryPort transacoes, ObterDataAtualPort dataAtual) {
		this.transferencias = transferencias;
		this.contas = contas;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public TransferenciaConta transferir(Long usuarioId, String chaveIdempotencia, CriarCommand command) {
		validarEntrada(usuarioId, chaveIdempotencia, command);
		String chave = chaveIdempotencia.trim();
		String hash = hash(command);
		var existente = transferencias.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(existente.get(), hash);

		List<Conta> contasBloqueadas = contas.buscarPorIdsEUsuarioParaAtualizacao(
				List.of(command.contaOrigemId(), command.contaDestinoId()).stream().sorted().toList(), usuarioId);
		if (contasBloqueadas.size() != 2) throw naoEncontrado();
		existente = transferencias.buscarPorUsuarioEChave(usuarioId, chave);
		if (existente.isPresent()) return validarRepeticao(existente.get(), hash);

		Conta origem = conta(contasBloqueadas, command.contaOrigemId());
		Conta destino = conta(contasBloqueadas, command.contaDestinoId());
		ContaAtivaValidator.exigirAtiva(origem);
		ContaAtivaValidator.exigirAtiva(destino);
		ValorMonetario valor = ValorMonetario.of(command.valor());
		TransferenciaConta transferencia = transferencias.salvar(TransferenciaConta.nova(usuarioId,
				origem.getId(), destino.getId(), valor, command.data(), command.descricao(), chave, hash));

		Transacao saida = transacoes.salvar(Transacao.transferencia(usuarioId, TipoTransacao.SAIDA, valor,
				command.data(), command.descricao(), origem.getId(), transferencia.id()));
		Transacao entrada = transacoes.salvar(Transacao.transferencia(usuarioId, TipoTransacao.ENTRADA, valor,
				command.data(), command.descricao(), destino.getId(), transferencia.id()));
		origem.debitar(valor);
		destino.creditar(valor);
		contas.salvar(origem);
		contas.salvar(destino);
		registrarHistorico(saida, "TRANSFERENCIA_SAIDA", transferencia.id(), usuarioId);
		registrarHistorico(entrada, "TRANSFERENCIA_ENTRADA", transferencia.id(), usuarioId);
		return transferencia;
	}

	@Override
	public TransferenciaConta buscar(Long usuarioId, Long transferenciaId) {
		return transferencias.buscarPorIdEUsuario(transferenciaId, usuarioId).orElseThrow(this::naoEncontrado);
	}

	@Override
	@Transactional
	public TransferenciaConta estornar(Long usuarioId, Long transferenciaId) {
		TransferenciaConta transferencia = transferencias
				.buscarPorIdEUsuarioParaAtualizacao(transferenciaId, usuarioId).orElseThrow(this::naoEncontrado);
		if (transferencia.status() == StatusTransferencia.ESTORNADA) return transferencia;
		List<Conta> contasBloqueadas = contas.buscarPorIdsEUsuarioParaAtualizacao(
				List.of(transferencia.contaOrigemId(), transferencia.contaDestinoId()).stream().sorted().toList(), usuarioId);
		if (contasBloqueadas.size() != 2) throw naoEncontrado();
		List<Transacao> lancamentos = transacoes.listarPorTransferencia(transferencia.id());
		if (lancamentos.size() != 2 || lancamentos.stream().anyMatch(Transacao::isEstornada))
			throw new DomainException("error.transferencia.inconsistente");

		Conta origem = conta(contasBloqueadas, transferencia.contaOrigemId());
		Conta destino = conta(contasBloqueadas, transferencia.contaDestinoId());
		var instante = dataAtual.obterDataHora();
		for (Transacao lancamento : lancamentos) {
			transacoes.salvar(lancamento.estornada(instante));
			registrarHistorico(lancamento, "ESTORNO_TRANSFERENCIA", transferencia.id(), usuarioId);
		}
		origem.creditar(transferencia.valor());
		destino.debitar(transferencia.valor());
		contas.salvar(origem);
		contas.salvar(destino);
		return transferencias.salvar(transferencia.estornar(instante));
	}

	private void registrarHistorico(Transacao transacao, String campo, Long transferenciaId, Long usuarioId) {
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), campo, null,
				transferenciaId.toString(), usuarioId, dataAtual.obterDataHora()));
	}

	private TransferenciaConta validarRepeticao(TransferenciaConta existente, String hash) {
		if (!existente.hashRequisicao().equals(hash))
			throw new DomainException("error.transferencia.idempotencia.conflitante");
		return existente;
	}

	private Conta conta(List<Conta> contasBloqueadas, Long id) {
		return contasBloqueadas.stream().filter(conta -> conta.getId().equals(id)).findFirst()
				.orElseThrow(this::naoEncontrado);
	}

	private void validarEntrada(Long usuarioId, String chave, CriarCommand command) {
		if (usuarioId == null || command == null || chave == null || chave.isBlank() || chave.length() > 100
				|| command.contaOrigemId() == null || command.contaDestinoId() == null
				|| command.valor() == null || command.data() == null || command.descricao() == null
				|| command.descricao().isBlank()) throw new DomainException("error.transferencia.invalida");
	}

	private String hash(CriarCommand command) {
		String payload = command.contaOrigemId() + "|" + command.contaDestinoId() + "|"
				+ command.valor().stripTrailingZeros().toPlainString() + "|" + command.data() + "|"
				+ command.descricao().trim();
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(payload.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 indisponível", e);
		}
	}

	private DomainException naoEncontrado() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
