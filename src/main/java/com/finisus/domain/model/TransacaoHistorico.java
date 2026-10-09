package com.finisus.domain.model;

import java.time.LocalDateTime;

public class TransacaoHistorico {

	private final Long id;
	private final Long transacaoId;
	private final String campoAlterado;
	private final String valorAnterior;
	private final String valorNovo;
	private final Long alteradoPor;
	private final LocalDateTime alteradoEm;
	private final String motivo;
	private final String correlacaoId;
	private final String snapshotAnterior;
	private final String snapshotNovo;

	private TransacaoHistorico(Long id, Long transacaoId, String campoAlterado, String valorAnterior, String valorNovo,
			Long alteradoPor, LocalDateTime alteradoEm, String motivo, String correlacaoId, String snapshotAnterior,
			String snapshotNovo) {
		this.id = id;
		this.transacaoId = transacaoId;
		this.campoAlterado = campoAlterado;
		this.valorAnterior = valorAnterior;
		this.valorNovo = valorNovo;
		this.alteradoPor = alteradoPor;
		this.alteradoEm = alteradoEm;
		this.motivo = motivo;
		this.correlacaoId = correlacaoId;
		this.snapshotAnterior = snapshotAnterior;
		this.snapshotNovo = snapshotNovo;
	}

	public static TransacaoHistorico registrar(Long transacaoId, String campoAlterado, String valorAnterior,
			String valorNovo, Long alteradoPor, LocalDateTime alteradoEm) {
		return new TransacaoHistorico(null, transacaoId, campoAlterado, valorAnterior, valorNovo, alteradoPor,
				alteradoEm, null, null, null, null);
	}

	public static TransacaoHistorico registrarCorrecao(Long transacaoId, String valorAnterior, String valorNovo,
			Long alteradoPor, LocalDateTime alteradoEm, String motivo, String correlacaoId, String snapshotAnterior,
			String snapshotNovo) {
		if (motivo == null || motivo.isBlank() || correlacaoId == null || correlacaoId.isBlank())
			throw new IllegalArgumentException("Motivo e correlação são obrigatórios para correções");
		return new TransacaoHistorico(null, transacaoId, "CORRECAO", valorAnterior, valorNovo, alteradoPor,
				alteradoEm, motivo.trim(), correlacaoId, snapshotAnterior, snapshotNovo);
	}

	public static TransacaoHistorico registrarDetalhamento(Long transacaoId, Long alteradoPor,
			LocalDateTime alteradoEm, String motivo, String correlacaoId, String snapshotAnterior, String snapshotNovo) {
		if (motivo == null || motivo.isBlank() || correlacaoId == null || correlacaoId.isBlank())
			throw new IllegalArgumentException("Motivo e correlação são obrigatórios para detalhamentos");
		return new TransacaoHistorico(null, transacaoId, "DETALHAMENTO_ITENS", null, null, alteradoPor,
				alteradoEm, motivo.trim(), correlacaoId, snapshotAnterior, snapshotNovo);
	}

	public static TransacaoHistorico reconstituir(Long id, Long transacaoId, String campoAlterado, String valorAnterior,
			String valorNovo, Long alteradoPor, LocalDateTime alteradoEm) {
		return new TransacaoHistorico(id, transacaoId, campoAlterado, valorAnterior, valorNovo, alteradoPor,
				alteradoEm, null, null, null, null);
	}

	public static TransacaoHistorico reconstituir(Long id, Long transacaoId, String campoAlterado, String valorAnterior,
			String valorNovo, Long alteradoPor, LocalDateTime alteradoEm, String motivo, String correlacaoId,
			String snapshotAnterior, String snapshotNovo) {
		return new TransacaoHistorico(id, transacaoId, campoAlterado, valorAnterior, valorNovo, alteradoPor,
				alteradoEm, motivo, correlacaoId, snapshotAnterior, snapshotNovo);
	}

	public Long getId() {
		return id;
	}

	public Long getTransacaoId() {
		return transacaoId;
	}

	public String getCampoAlterado() {
		return campoAlterado;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public String getValorNovo() {
		return valorNovo;
	}

	public Long getAlteradoPor() {
		return alteradoPor;
	}

	public LocalDateTime getAlteradoEm() {
		return alteradoEm;
	}

	public String getMotivo() { return motivo; }

	public String getCorrelacaoId() { return correlacaoId; }

	public String getSnapshotAnterior() { return snapshotAnterior; }

	public String getSnapshotNovo() { return snapshotNovo; }
}
