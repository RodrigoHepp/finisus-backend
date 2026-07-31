package com.financeiro.domain.model;

import com.financeiro.domain.vo.ValorMonetario;
import com.financeiro.domain.RateioInvalidoException;
import com.financeiro.domain.vo.Email;

import java.math.BigDecimal;

public class RateioDespesa {

	private final Long id;
	private final Long despesaCompartilhadaId;
	private final TipoParticipante tipoParticipante;
	private final Long usuarioId;
	private final String nomeExterno;
	private final String emailExterno;
	private final ValorMonetario valorFixo;
	private final BigDecimal percentual;
	private StatusRateio status;

	private RateioDespesa(Long id, Long despesaCompartilhadaId, TipoParticipante tipoParticipante, Long usuarioId,
			String nomeExterno, String emailExterno, ValorMonetario valorFixo, BigDecimal percentual,
			StatusRateio status) {
		validar(despesaCompartilhadaId, tipoParticipante, usuarioId, nomeExterno, emailExterno, valorFixo, percentual);
		this.id = id;
		this.despesaCompartilhadaId = despesaCompartilhadaId;
		this.tipoParticipante = tipoParticipante;
		this.usuarioId = usuarioId;
		this.nomeExterno = nomeExterno;
		this.emailExterno = emailExterno == null ? null : new Email(emailExterno).valor();
		this.valorFixo = valorFixo;
		this.percentual = percentual;
		this.status = status;
	}

	public static RateioDespesa interno(Long despesaId, Long usuarioId, ValorMonetario valorFixo,
			BigDecimal percentual) {
		return new RateioDespesa(null, despesaId, TipoParticipante.INTERNO, usuarioId, null, null, valorFixo,
				percentual, StatusRateio.PENDENTE);
	}

	public static RateioDespesa externo(Long despesaId, String nome, String email, ValorMonetario valorFixo,
			BigDecimal percentual) {
		return new RateioDespesa(null, despesaId, TipoParticipante.EXTERNO, null, nome, email, valorFixo, percentual,
				StatusRateio.ACEITO);
	}

	public static RateioDespesa reconstituir(Long id, Long despesaCompartilhadaId, TipoParticipante tipoParticipante,
			Long usuarioId, String nomeExterno, String emailExterno, ValorMonetario valorFixo, BigDecimal percentual,
			StatusRateio status) {
		return new RateioDespesa(id, despesaCompartilhadaId, tipoParticipante, usuarioId, nomeExterno, emailExterno,
				valorFixo, percentual, status);
	}

	public void aceitar() {
		if (tipoParticipante != TipoParticipante.INTERNO || status != StatusRateio.PENDENTE)
			throw new com.financeiro.domain.DomainException("error.rateio.transicao.invalida");
		this.status = StatusRateio.ACEITO;
	}

	public void recusar() {
		if (tipoParticipante != TipoParticipante.INTERNO || status != StatusRateio.PENDENTE)
			throw new com.financeiro.domain.DomainException("error.rateio.transicao.invalida");
		this.status = StatusRateio.RECUSADO;
	}

	public void pagar() {
		if (status != StatusRateio.ACEITO)
			throw new com.financeiro.domain.DomainException("error.rateio.transicao.invalida");
		this.status = StatusRateio.PAGO;
	}

	public void cancelar() {
		if (status != StatusRateio.PAGO) {
			this.status = StatusRateio.CANCELADO;
		}
	}

	private static void validar(Long despesaId, TipoParticipante tipoParticipante, Long usuarioId, String nomeExterno,
			String emailExterno, ValorMonetario valorFixo, BigDecimal percentual) {
		boolean baseFixa = valorFixo != null && !valorFixo.isZero() && percentual == null;
		boolean basePercentual = valorFixo == null && percentual != null && percentual.signum() > 0
				&& percentual.compareTo(BigDecimal.valueOf(100)) <= 0;
		boolean participanteInterno = tipoParticipante == TipoParticipante.INTERNO && usuarioId != null && usuarioId > 0
				&& nomeExterno == null && emailExterno == null;
		boolean participanteExterno = tipoParticipante == TipoParticipante.EXTERNO && usuarioId == null
				&& nomeExterno != null && !nomeExterno.isBlank() && emailExterno != null && !emailExterno.isBlank();
		if (despesaId == null || despesaId <= 0 || !(baseFixa || basePercentual)
				|| !(participanteInterno || participanteExterno)) {
			throw new RateioInvalidoException();
		}
	}

	public Long getId() {
		return id;
	}

	public Long getDespesaCompartilhadaId() {
		return despesaCompartilhadaId;
	}

	public TipoParticipante getTipoParticipante() {
		return tipoParticipante;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public String getNomeExterno() {
		return nomeExterno;
	}

	public String getEmailExterno() {
		return emailExterno;
	}

	public ValorMonetario getValorFixo() {
		return valorFixo;
	}

	public BigDecimal getPercentual() {
		return percentual;
	}

	public StatusRateio getStatus() {
		return status;
	}
}
