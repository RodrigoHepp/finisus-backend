package com.finisus.adapters.out.csv;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;

interface LeitorCsvFinanceiro {
	boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, TabelaCsv tabela);
	LeitorDocumentoFinanceiroPort.DocumentoLido ler(TabelaCsv tabela);
}
