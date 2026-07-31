package com.finisus.infrastructure.scheduling;

import com.finisus.application.ports.in.RecorrenciaUseCase;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.RegistrarExecucaoRecorrenciaPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.service.ResultadoProcessamentoRecorrencias;
import com.finisus.domain.vo.AnoMes;
import com.finisus.infrastructure.observability.OperacaoFinanceiraMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class RecorrenciaScheduler {
	private static final Logger log = LoggerFactory.getLogger(RecorrenciaScheduler.class);
	private final UsuarioRepositoryPort usuarios;
	private final RecorrenciaUseCase recorrencias;
	private final OperacaoFinanceiraMetrics metrics;
	private final ObterDataAtualPort dataAtual;
	private final RegistrarExecucaoRecorrenciaPort execucoes;

	public RecorrenciaScheduler(UsuarioRepositoryPort usuarios, RecorrenciaUseCase recorrencias,
			OperacaoFinanceiraMetrics metrics, ObterDataAtualPort dataAtual,
			RegistrarExecucaoRecorrenciaPort execucoes) {
		this.usuarios = usuarios;
		this.recorrencias = recorrencias;
		this.metrics = metrics;
		this.dataAtual = dataAtual;
		this.execucoes = execucoes;
	}

	@Scheduled(cron = "${app.recorrencias.cron:0 5 0 1 * *}", zone = "${app.recorrencias.timezone:America/Sao_Paulo}")
	public void gerarMesAtual() {
		MDC.put("correlationId", "recorrencia-" + UUID.randomUUID());
		long inicio = System.nanoTime();
		try {
			metrics.medir("job_recorrencia", () -> {
				String anoMes = AnoMes.from(dataAtual.obter()).formatado();
				int sucessos = 0;
				int falhas = 0;
				var usuariosAtivos = usuarios.listarAtivos();
				for (var usuario : usuariosAtivos) {
					try {
						recorrencias.gerarMes(usuario.getId(), anoMes);
						sucessos++;
					} catch (RuntimeException exception) {
						falhas++;
						log.error("Falha ao gerar recorrências para o usuário {} no período {}", usuario.getId(),
								anoMes, exception);
					}
				}
				execucoes.registrar(new ResultadoProcessamentoRecorrencias(usuariosAtivos.size(), sucessos, falhas,
						Duration.ofNanos(System.nanoTime() - inicio)));
			});
		} finally {
			MDC.remove("correlationId");
		}
	}
}
