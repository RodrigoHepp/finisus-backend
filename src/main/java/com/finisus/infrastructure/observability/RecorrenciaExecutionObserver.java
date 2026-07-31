package com.finisus.infrastructure.observability;

import com.finisus.application.ports.out.RegistrarExecucaoRecorrenciaPort;
import com.finisus.application.service.ResultadoProcessamentoRecorrencias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RecorrenciaExecutionObserver implements RegistrarExecucaoRecorrenciaPort {
	private static final Logger log = LoggerFactory.getLogger(RecorrenciaExecutionObserver.class);

	@Override
	public void registrar(ResultadoProcessamentoRecorrencias resultado) {
		log.info("Processamento de recorrências concluído: totalUsuarios={}, sucessos={}, falhas={}, duracaoMs={}",
				resultado.totalUsuarios(), resultado.sucessos(), resultado.falhas(), resultado.duracao().toMillis());
	}
}
