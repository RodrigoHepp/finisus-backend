package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.DespesaCompartilhadaRepositoryPort;
import com.finisus.domain.model.TipoAlvoCompartilhamento;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Item;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransacaoItem;
import com.finisus.domain.vo.ValorMonetario;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class TransacaoService implements TransacaoUseCase, RegistrarTransacaoUseCase {
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final MeioPagamentoRepositoryPort meios;
	private final ItemRepositoryPort itensCatalogo;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;
	private final DespesaCompartilhadaRepositoryPort despesasCompartilhadas;

	public TransacaoService(ContaRepositoryPort contas, CategoriaRepositoryPort categorias,
			MeioPagamentoRepositoryPort meios, ItemRepositoryPort itensCatalogo, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, DespesaCompartilhadaRepositoryPort despesasCompartilhadas) {
		this.contas = contas;
		this.categorias = categorias;
		this.meios = meios;
		this.itensCatalogo = itensCatalogo;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
		this.despesasCompartilhadas = despesasCompartilhadas;
	}

	@Override
	@Transactional
	public Transacao registrar(Long usuarioId, RegistrarCommand command) {
		Conta conta = ContaAtivaValidator
				.exigirAtiva(contas.buscarPorIdEUsuario(command.contaId(), usuarioId).orElseThrow(this::notFound));
		validarReferencias(usuarioId, command);
		Transacao transacao = transacoes.salvar(Transacao.nova(usuarioId, command.tipo(),
				ValorMonetario.of(command.valor()), command.data(), command.descricao(), command.contaId(),
				command.categoriaId(), command.meioPagamentoId(), itens(usuarioId, command.itens())));
		ValorMonetario saldo = command.tipo() == TipoTransacao.ENTRADA ? conta.getSaldo().somar(transacao.getValor())
				: conta.getSaldo().subtrair(transacao.getValor());
		contas.salvar(comSaldo(conta, saldo));
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "CRIACAO", null,
				transacao.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return transacao;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Transacao> listar(Long usuarioId) {
		return transacoes.listarPorUsuario(usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public Pagina<Transacao> listar(Long usuarioId, Paginacao paginacao) {
		return transacoes.listarPorUsuario(usuarioId, paginacao);
	}

	@Override
	@Transactional(readOnly = true)
	public Transacao buscar(Long usuarioId, Long transacaoId) {
		return transacoes.buscarPorIdEUsuario(transacaoId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	@Transactional
	public Transacao corrigir(Long usuarioId, Long transacaoId, RegistrarCommand command) {
		Transacao anterior = buscar(usuarioId, transacaoId);
		validarSemCompartilhamentoAtivo(anterior);
		Conta contaAnterior = contas.buscarPorIdEUsuario(anterior.getContaId(), usuarioId).orElseThrow(this::notFound);
		Conta contaNova = ContaAtivaValidator
				.exigirAtiva(contas.buscarPorIdEUsuario(command.contaId(), usuarioId).orElseThrow(this::notFound));
		validarReferencias(usuarioId, command);
		Transacao corrigida = transacoes.salvar(anterior.corrigida(command.tipo(), ValorMonetario.of(command.valor()),
				command.data(), command.descricao(), command.contaId(), command.categoriaId(),
				command.meioPagamentoId(), itens(usuarioId, command.itens())));
		ValorMonetario revertido = anterior.getTipo() == TipoTransacao.ENTRADA
				? contaAnterior.getSaldo().subtrair(anterior.getValor())
				: contaAnterior.getSaldo().somar(anterior.getValor());
		if (contaAnterior.getId().equals(contaNova.getId())) {
			contas.salvar(comSaldo(contaAnterior, aplicar(revertido, corrigida)));
		} else {
			contas.salvar(comSaldo(contaAnterior, revertido));
			contas.salvar(comSaldo(contaNova, aplicar(contaNova.getSaldo(), corrigida)));
		}
		transacoes.salvarHistorico(
				TransacaoHistorico.registrar(transacaoId, "CORRECAO", anterior.getValor().valor().toPlainString(),
						corrigida.getValor().valor().toPlainString(), usuarioId, dataAtual.obterDataHora()));
		return corrigida;
	}

	@Override
	@Transactional
	public Transacao estornar(Long usuarioId, Long transacaoId) {
		Transacao transacao = buscar(usuarioId, transacaoId);
		validarSemCompartilhamentoAtivo(transacao);
		Transacao estornada = transacoes.salvar(transacao.estornada(dataAtual.obterDataHora()));
		if (transacao.impactaSaldoDaConta()) {
			Conta conta = contas.buscarPorIdEUsuario(transacao.getContaId(), usuarioId).orElseThrow(this::notFound);
			ValorMonetario saldo = transacao.getTipo() == TipoTransacao.ENTRADA
					? conta.getSaldo().subtrair(transacao.getValor())
					: conta.getSaldo().somar(transacao.getValor());
			contas.salvar(comSaldo(conta, saldo));
		}
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacaoId, "ESTORNO",
				transacao.getValor().valor().toPlainString(), null, usuarioId, dataAtual.obterDataHora()));
		return estornada;
	}

	private void validarSemCompartilhamentoAtivo(Transacao transacao) {
		boolean possuiAtivo = despesasCompartilhadas.existePorTransacaoETipoAlvo(transacao.getId(),
				TipoAlvoCompartilhamento.TRANSACAO)
				|| transacao.getItens().stream()
						.anyMatch(item -> despesasCompartilhadas.existePorTransacaoItemId(item.getId()));
		transacao.validarAlteracaoSemCompartilhamentoAtivo(possuiAtivo);
	}

	private void validarReferencias(Long usuarioId, RegistrarCommand command) {
		if (command.categoriaId() != null)
			categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId).orElseThrow(this::notFound);
		if (command.meioPagamentoId() != null)
			meios.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId).orElseThrow(this::notFound);
	}

	private List<TransacaoItem> itens(Long usuarioId, List<ItemCommand> commands) {
		return commands == null ? List.of() : commands.stream().map(command -> {
			Item item = itensCatalogo.buscarPorIdEUsuario(command.itemId(), usuarioId).orElseThrow(this::notFound);
			if (!item.isAtivo())
				throw new DomainException("error.item.inativo");
			return TransacaoItem.novo(item.getId(), item.getNome(), ValorMonetario.of(command.valor()),
					item.getCategoriaPadraoId());
		}).toList();
	}

	private ValorMonetario aplicar(ValorMonetario saldo, Transacao transacao) {
		return transacao.getTipo() == TipoTransacao.ENTRADA ? saldo.somar(transacao.getValor())
				: saldo.subtrair(transacao.getValor());
	}

	private Conta comSaldo(Conta conta, ValorMonetario saldo) {
		return Conta.reconstituir(conta.getId(), conta.getUsuarioId(), conta.getNome(), conta.getTipo(),
				conta.getBancoId(), saldo, conta.isAtivo(), conta.getVersion());
	}

	private DomainException notFound() {
		return new DomainException("error.recurso.nao.encontrado");
	}
}
