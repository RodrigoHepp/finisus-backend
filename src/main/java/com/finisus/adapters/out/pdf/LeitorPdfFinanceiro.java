package com.finisus.adapters.out.pdf;

import com.finisus.application.ports.out.LeitorDocumentoFinanceiroPort;

interface LeitorPdfFinanceiro {
	boolean suporta(LeitorDocumentoFinanceiroPort.ArquivoPdf arquivo, String texto);
	LeitorDocumentoFinanceiroPort.DocumentoLido ler(String texto);
}
