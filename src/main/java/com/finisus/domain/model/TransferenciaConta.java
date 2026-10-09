package com.finisus.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.finisus.domain.DomainException;
import com.finisus.domain.vo.ValorMonetario;

public record TransferenciaConta(Long id, Long usuarioId, Long contaOrigemId, Long contaDestinoId,
		ValorMonetario valor, LocalDate data, String descricao, String chaveIdempotencia, String hashRequisicao,
		StatusTransferencia status, LocalDateTime estornadaEm, long version) {

	public static TransferenciaConta nova(Long usuarioId, Long origemId, Long destinoId, ValorMonetario valor,
			LocalDate data, String descricao, String chave, String hash) {
		if (origemId == null || destinoId == null || origemId.equals(destinoId) || valor == null
				|| valor.valor().signum() <= 0 || data == null || descricao == null || descricao.isBlank()
				|| chave == null || chave.isBlank())
			throw new DomainException("error.transferencia.invalida");
		return new TransferenciaConta(null, usuarioId, origemId, destinoId, valor, data, descricao.trim(), chave, hash,
				StatusTransferencia.ATIVA, null, 0);
	}

	public TransferenciaConta estornar(LocalDateTime instante) {
		return status == StatusTransferencia.ESTORNADA ? this
				: new TransferenciaConta(id, usuarioId, contaOrigemId, contaDestinoId, valor, data, descricao,
						chaveIdempotencia, hashRequisicao, StatusTransferencia.ESTORNADA, instante, version);
	}
}
