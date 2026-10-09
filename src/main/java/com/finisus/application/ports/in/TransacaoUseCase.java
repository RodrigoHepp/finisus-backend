package com.finisus.application.ports.in;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.vo.AnoMes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoUseCase {
	Transacao registrar(Long usuarioId, RegistrarCommand command);

	Transacao buscar(Long usuarioId, Long transacaoId);

	List<Transacao> listar(Long usuarioId);

	Pagina<Transacao> listar(Long usuarioId, Paginacao paginacao);

	Pagina<Transacao> listar(Long usuarioId, Paginacao paginacao, FiltroListagem filtro);

	Transacao corrigir(Long usuarioId, Long transacaoId, CorrigirCommand command);

	Transacao detalhar(Long usuarioId, Long transacaoId, DetalharCommand command);

	Transacao estornar(Long usuarioId, Long transacaoId);

	record RegistrarCommand(TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao, Long contaId,
			Long categoriaId, Long meioPagamentoId, List<ItemCommand> itens) {
	}

	record ItemCommand(Long itemId, String descricao, BigDecimal quantidade, BigDecimal valor, Long categoriaId) {
		public ItemCommand(Long itemId, BigDecimal valor) {
			this(itemId, null, null, valor, null);
		}
	}

	record CorrigirCommand(RegistrarCommand transacao, String motivo) {
	}

	record DetalharCommand(List<ItemCommand> itens, String motivo) {
	}

	record FiltroListagem(AnoMes mes, TipoTransacao tipo, Long categoriaId) {
	}
}
