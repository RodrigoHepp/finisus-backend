package com.finisus.application.ports.out;

import com.finisus.domain.model.ReembolsoDivisao;
import com.finisus.domain.vo.ValorMonetario;
import java.time.LocalDate;
import java.util.List;

public interface ReembolsoDivisaoRepositoryPort {
    ReembolsoDivisao salvar(Long divisaoId, Long transacaoId, Long pagadorId, Long recebedorId,
            ValorMonetario valor);
    boolean existeAtivoPorTransacao(Long transacaoId);
    boolean cancelar(Long divisaoId, Long reembolsoId, Long canceladoPor);
    List<ReembolsoDivisao> listar(Long divisaoId);
    List<ReembolsoDivisao> listarAtivos(Long divisaoId, LocalDate inicio, LocalDate fim);
}
