package com.financeiro.application.ports.out;

import com.financeiro.application.service.ResultadoProcessamentoRecorrencias;

public interface RegistrarExecucaoRecorrenciaPort {
	void registrar(ResultadoProcessamentoRecorrencias resultado);
}
