package com.financeiro.application.service;

import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.in.DespesaCompartilhadaUseCase;
import com.financeiro.application.ports.out.ConfiguracaoCompartilhamentoRepositoryPort;
import com.financeiro.application.ports.out.DespesaCompartilhadaRepositoryPort;
import com.financeiro.application.ports.out.RateioDespesaRepositoryPort;
import com.financeiro.application.ports.out.TransacaoRepositoryPort;
import com.financeiro.application.ports.out.UsuarioRepositoryPort;
import com.financeiro.application.ports.out.ObterDataAtualPort;
import com.financeiro.domain.DomainException;
import com.financeiro.domain.model.ConfiguracaoCompartilhamento;
import com.financeiro.domain.model.DespesaCompartilhada;
import com.financeiro.domain.model.RateioDespesa;
import com.financeiro.domain.model.TipoRateio;
import com.financeiro.domain.model.TipoAlvoCompartilhamento;
import com.financeiro.domain.model.TipoTransacao;
import com.financeiro.domain.model.Transacao;
import com.financeiro.domain.vo.Email;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Orquestra a criação atômica de uma despesa e de seus rateios. */
public class DespesaCompartilhadaService implements DespesaCompartilhadaUseCase {
	private final DespesaCompartilhadaRepositoryPort despesas;
	private final RateioDespesaRepositoryPort rateios;
	private final ConfiguracaoCompartilhamentoRepositoryPort configuracoes;
	private final TransacaoRepositoryPort transacoes;
	private final UsuarioRepositoryPort usuarios;
	private final ObterDataAtualPort dataAtual;

	public DespesaCompartilhadaService(DespesaCompartilhadaRepositoryPort despesas, RateioDespesaRepositoryPort rateios,
			ConfiguracaoCompartilhamentoRepositoryPort configuracoes, TransacaoRepositoryPort transacoes,
			UsuarioRepositoryPort usuarios, ObterDataAtualPort dataAtual) {
		this.despesas = despesas;
		this.rateios = rateios;
		this.configuracoes = configuracoes;
		this.transacoes = transacoes;
		this.usuarios = usuarios;
		this.dataAtual = dataAtual;
	}

	@Override
	@Transactional
	public Resultado criar(Long usuarioId, CriarCommand command) {
		Transacao transacao = exigir(transacoes.buscarPorIdEUsuarioParaAtualizacao(command.transacaoId(), usuarioId));
		if (transacao.getTipo() != TipoTransacao.SAIDA) {
			throw new DomainException("error.compartilhamento.apenas.saida");
		}
		if (command.transacaoItemId() != null) {
			var item = transacao.getItens().stream().filter(atual -> command.transacaoItemId().equals(atual.getId()))
					.findFirst().orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
			if (despesas.existePorTransacaoETipoAlvo(transacao.getId(), TipoAlvoCompartilhamento.TRANSACAO)
					|| despesas.existePorTransacaoItemId(item.getId())) {
				throw new DomainException("error.compartilhamento.alvo.ja.compartilhado");
			}
			DespesaCompartilhada despesa = despesas.salvar(
					DespesaCompartilhada.novoItem(transacao.getId(), item.getId(), usuarioId, command.tipoRateio()));
			List<RateioDespesa> novosRateios = command.participantes().stream()
					.map(participante -> criarParticipante(despesa.getId(), command.tipoRateio(), participante))
					.toList();
			validarTotal(command.tipoRateio(), novosRateios, item.getValor().valor());
			return new Resultado(despesa, rateios.salvarTodos(despesa.getId(), novosRateios));
		}
		if (despesas.existePorTransacaoETipoAlvo(transacao.getId(), TipoAlvoCompartilhamento.TRANSACAO)
				|| despesas.existePorTransacaoETipoAlvo(transacao.getId(), TipoAlvoCompartilhamento.ITEM_TRANSACAO)) {
			throw new DomainException("error.compartilhamento.alvo.ja.compartilhado");
		}
		DespesaCompartilhada despesa = despesas
				.salvar(DespesaCompartilhada.nova(transacao.getId(), usuarioId, command.tipoRateio()));
		List<RateioDespesa> novosRateios = command.participantes().stream()
				.map(participante -> criarParticipante(despesa.getId(), command.tipoRateio(), participante)).toList();
		validarTotal(command.tipoRateio(), novosRateios, transacao.getValor().valor());
		List<RateioDespesa> rateiosSalvos = rateios.salvarTodos(despesa.getId(), novosRateios);
		transacoes.salvar(transacao.associadaADespesaCompartilhada(despesa.getId()));
		return new Resultado(despesa, rateiosSalvos);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DespesaCompartilhada> listar(Long usuarioId) {
		return despesas.listarPorCriadorId(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<DespesaCompartilhada> listar(Long usuarioId, Paginacao paginacao) {
		return despesas.listarPorCriadorId(usuarioId, paginacao);
	}

	@Override
	@Transactional(readOnly = true)
	public DespesaCompartilhada buscar(Long usuarioId, Long despesaId) {
		return exigir(despesas.buscarPorIdECriadorId(despesaId, usuarioId));
	}

	@Override
	@Transactional
	public Resultado cancelar(Long usuarioId, Long despesaId) {
		DespesaCompartilhada despesa = buscar(usuarioId, despesaId);
		despesa.cancelar(dataAtual.obterDataHora());
		DespesaCompartilhada cancelada = despesas.salvar(despesa);
		List<RateioDespesa> cancelados = rateios.listarPorDespesaId(despesaId).stream().peek(RateioDespesa::cancelar)
				.map(rateios::salvar).toList();
		return new Resultado(cancelada, cancelados);
	}

	private RateioDespesa criarParticipante(Long despesaId, TipoRateio tipoRateio, ParticipanteCommand participante) {
		if (participante.usuarioId() != null) {
			usuarios.buscarPorId(participante.usuarioId())
					.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
			ConfiguracaoCompartilhamento configuracao = configuracoes.buscarPorUsuarioId(participante.usuarioId())
					.orElseThrow(() -> new DomainException("error.compartilhamento.optin.obrigatorio"));
			if (!configuracao.isAceitaCompartilhamento()) {
				throw new DomainException("error.compartilhamento.optin.obrigatorio");
			}
			return RateioDespesa.interno(despesaId, participante.usuarioId(),
					valor(tipoRateio, participante.valorFixo()), percentual(tipoRateio, participante.percentual()));
		}
		new Email(participante.emailExterno());
		if (participante.nomeExterno() == null || participante.nomeExterno().isBlank()) {
			throw new DomainException("error.compartilhamento.externo.invalido");
		}
		return RateioDespesa.externo(despesaId, participante.nomeExterno(), participante.emailExterno(),
				valor(tipoRateio, participante.valorFixo()), percentual(tipoRateio, participante.percentual()));
	}

	private ValorMonetario valor(TipoRateio tipo, BigDecimal valor) {
		return tipo == TipoRateio.VALOR_FIXO ? ValorMonetario.of(valor) : null;
	}

	private BigDecimal percentual(TipoRateio tipo, BigDecimal percentual) {
		if (tipo == TipoRateio.PERCENTUAL && (percentual == null || percentual.signum() <= 0)) {
			throw new DomainException("error.compartilhamento.percentual.invalido");
		}
		return tipo == TipoRateio.PERCENTUAL ? percentual : null;
	}

	private void validarTotal(TipoRateio tipo, List<RateioDespesa> novosRateios, BigDecimal total) {
		BigDecimal soma = tipo == TipoRateio.VALOR_FIXO
				? novosRateios.stream().map(rateio -> rateio.getValorFixo().valor()).reduce(BigDecimal.ZERO,
						BigDecimal::add)
				: novosRateios.stream().map(RateioDespesa::getPercentual).reduce(BigDecimal.ZERO, BigDecimal::add);
		if ((tipo == TipoRateio.VALOR_FIXO && soma.compareTo(total) > 0)
				|| (tipo == TipoRateio.PERCENTUAL && soma.compareTo(BigDecimal.valueOf(100)) > 0)) {
			throw new DomainException("error.compartilhamento.total.invalido");
		}
	}

	private <T> T exigir(java.util.Optional<T> value) {
		return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
	}
}
