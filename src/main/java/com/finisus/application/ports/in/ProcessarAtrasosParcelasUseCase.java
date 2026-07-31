package com.finisus.application.ports.in;

import java.time.LocalDate;

public interface ProcessarAtrasosParcelasUseCase {
	int processarAtrasos(ProcessarAtrasosCommand command);

	record ProcessarAtrasosCommand(LocalDate dataReferencia) {
	}
}
