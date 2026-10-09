package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.RegistrarTransacaoUseCase;
import com.finisus.application.ports.in.TransacaoUseCase;
import com.finisus.application.ports.in.EstornarTransacaoVinculadaUseCase;
import com.finisus.application.ports.out.CategoriaRepositoryPort;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.application.ports.out.ItemRepositoryPort;
import com.finisus.application.ports.out.MeioPagamentoRepositoryPort;
import com.finisus.application.ports.out.ObterDataAtualPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.VinculoTransacaoDivisaoRepositoryPort;
import com.finisus.application.ports.out.ObrigacaoFinanceiraRepositoryPort;
import com.finisus.application.ports.out.ParcelaFinanciamentoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.Conta;
import com.finisus.domain.model.Item;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.TransacaoHistorico;
import com.finisus.domain.model.TransacaoItem;
import com.finisus.domain.vo.ValorMonetario;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class TransacaoService implements TransacaoUseCase, RegistrarTransacaoUseCase, EstornarTransacaoVinculadaUseCase {
	private final ContaRepositoryPort contas;
	private final CategoriaRepositoryPort categorias;
	private final MeioPagamentoRepositoryPort meios;
	private final ItemRepositoryPort itensCatalogo;
	private final TransacaoRepositoryPort transacoes;
	private final ObterDataAtualPort dataAtual;
	private final VinculoTransacaoDivisaoRepositoryPort vinculosDivisao;
	private final ObrigacaoFinanceiraRepositoryPort obrigacoes;
	private final ParcelaFinanciamentoRepositoryPort parcelas;

	public TransacaoService(ContaRepositoryPort contas, CategoriaRepositoryPort categorias,
			MeioPagamentoRepositoryPort meios, ItemRepositoryPort itensCatalogo, TransacaoRepositoryPort transacoes,
			ObterDataAtualPort dataAtual, VinculoTransacaoDivisaoRepositoryPort vinculosDivisao,
			ObrigacaoFinanceiraRepositoryPort obrigacoes, ParcelaFinanciamentoRepositoryPort parcelas) {
		this.contas = contas;
		this.categorias = categorias;
		this.meios = meios;
		this.itensCatalogo = itensCatalogo;
		this.transacoes = transacoes;
		this.dataAtual = dataAtual;
		this.vinculosDivisao = vinculosDivisao;
		this.obrigacoes = obrigacoes;
		this.parcelas = parcelas;
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
	public Pagina<Transacao> listar(Long usuarioId, Paginacao paginacao, FiltroListagem filtro) {
		return transacoes.listarPorUsuario(usuarioId, paginacao, filtro);
	}

	@Override
	@Transactional(readOnly = true)
	public Transacao buscar(Long usuarioId, Long transacaoId) {
		return transacoes.buscarPorIdEUsuario(transacaoId, usuarioId).orElseThrow(this::notFound);
	}

	@Override
	@Transactional
	public Transacao corrigir(Long usuarioId, Long transacaoId, CorrigirCommand correcao) {
		if (correcao == null || correcao.transacao() == null || correcao.motivo() == null
				|| correcao.motivo().isBlank())
			throw new DomainException("error.transacao.correcao.motivo.obrigatorio");
		RegistrarCommand command = correcao.transacao();
		Transacao anterior = buscar(usuarioId, transacaoId);
		validarSemLiquidacaoVinculada(usuarioId, anterior);
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
		transacoes.salvarHistorico(TransacaoHistorico.registrarCorrecao(transacaoId,
				anterior.getValor().valor().toPlainString(), corrigida.getValor().valor().toPlainString(), usuarioId,
				dataAtual.obterDataHora(), correcao.motivo(), UUID.randomUUID().toString(), snapshot(anterior),
				snapshot(corrigida)));
		return corrigida;
	}

	@Override
	@Transactional
	public Transacao detalhar(Long usuarioId, Long transacaoId, DetalharCommand command) {
		if (command == null || command.itens() == null || command.itens().isEmpty() || command.motivo() == null
				|| command.motivo().isBlank()) {
			throw new DomainException("error.transacao.detalhamento.dados.obrigatorios");
		}
		Transacao anterior = transacoes.buscarPorIdEUsuarioParaAtualizacao(transacaoId, usuarioId)
				.orElseThrow(this::notFound);
		validarSemLiquidacaoVinculada(usuarioId, anterior);
		validarItensSemCompartilhamentoAtivo(anterior);
		Transacao detalhada = transacoes.salvar(anterior.detalhada(itens(usuarioId, command.itens())));
		transacoes.salvarHistorico(TransacaoHistorico.registrarDetalhamento(transacaoId, usuarioId,
				dataAtual.obterDataHora(), command.motivo(), UUID.randomUUID().toString(), snapshot(anterior),
				snapshot(detalhada)));
		return detalhada;
	}

	private String snapshot(Transacao transacao) {
		String itens = transacao.getItens().stream()
				.map(item -> "{\"itemId\":" + item.getItemId() + ",\"descricao\":" + json(item.getDescricao())
						+ ",\"quantidade\":" + decimal(item.getQuantidade()) + ",\"valor\":"
						+ json(item.getValor().valor().toPlainString()) + ",\"categoriaId\":"
						+ numero(item.getCategoriaId()) + "}")
				.collect(java.util.stream.Collectors.joining(","));
		return "{\"tipo\":" + json(transacao.getTipo().name()) + ",\"valor\":"
				+ json(transacao.getValor().valor().toPlainString()) + ",\"data\":" + json(transacao.getData().toString())
				+ ",\"descricao\":" + json(transacao.getDescricao()) + ",\"contaId\":" + numero(transacao.getContaId())
				+ ",\"categoriaId\":" + numero(transacao.getCategoriaId()) + ",\"meioPagamentoId\":"
				+ numero(transacao.getMeioPagamentoId()) + ",\"itens\":[" + itens + "]}";
	}

	private String json(String valor) {
		if (valor == null)
			return "null";
		return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r")
				.replace("\n", "\\n") + "\"";
	}

	private String numero(Long valor) {
		return valor == null ? "null" : valor.toString();
	}

	private String decimal(java.math.BigDecimal valor) {
		return valor == null ? "null" : json(valor.toPlainString());
	}

	@Override
	@Transactional
	public Transacao estornar(Long usuarioId, Long transacaoId) {
		Transacao transacao = buscar(usuarioId, transacaoId);
		validarSemLiquidacaoVinculada(usuarioId, transacao);
		return estornar(usuarioId, transacao);
	}

	@Override
	@Transactional
	public Transacao estornarVinculada(Long usuarioId, Long transacaoId) {
		return estornar(usuarioId, buscar(usuarioId, transacaoId));
	}

	private Transacao estornar(Long usuarioId, Transacao transacao) {
		validarSemCompartilhamentoAtivo(transacao);
		Transacao estornada = transacoes.salvar(transacao.estornada(dataAtual.obterDataHora()));
		if (transacao.impactaSaldoDaConta()) {
			Conta conta = contas.buscarPorIdEUsuario(transacao.getContaId(), usuarioId).orElseThrow(this::notFound);
			ValorMonetario saldo = transacao.getTipo() == TipoTransacao.ENTRADA
					? conta.getSaldo().subtrair(transacao.getValor())
					: conta.getSaldo().somar(transacao.getValor());
			contas.salvar(comSaldo(conta, saldo));
		}
		transacoes.salvarHistorico(TransacaoHistorico.registrar(transacao.getId(), "ESTORNO",
				transacao.getValor().valor().toPlainString(), null, usuarioId, dataAtual.obterDataHora()));
		return estornada;
	}

	private void validarSemLiquidacaoVinculada(Long usuarioId, Transacao transacao) {
		transacao.validarEstornoGenerico();
		if (obrigacoes.existePorTransacaoEUsuario(transacao.getId(), usuarioId)
				|| parcelas.existePorTransacao(transacao.getId())) {
			throw new DomainException("error.transacao.origem.imutavel");
		}
	}

	private void validarSemCompartilhamentoAtivo(Transacao transacao) {
		boolean possuiAtivo = vinculosDivisao.existePorTransacaoId(transacao.getId());
		transacao.validarAlteracaoSemCompartilhamentoAtivo(possuiAtivo);
	}

	private void validarItensSemCompartilhamentoAtivo(Transacao transacao) {
		boolean possuiAtivo = vinculosDivisao.existePorTransacaoId(transacao.getId());
		transacao.validarAlteracaoSemCompartilhamentoAtivo(possuiAtivo);
	}

	private void validarReferencias(Long usuarioId, RegistrarCommand command) {
		if (command.categoriaId() != null)
			CategoriaAtivaValidator.exigirAtiva(
					categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId).orElseThrow(this::notFound));
		if (command.meioPagamentoId() != null)
			meios.buscarPorIdEUsuario(command.meioPagamentoId(), usuarioId).orElseThrow(this::notFound);
	}

	private List<TransacaoItem> itens(Long usuarioId, List<ItemCommand> commands) {
		return commands == null ? List.of() : commands.stream().map(command -> {
			Item item = command.itemId() == null ? null
					: itensCatalogo.buscarPorIdEUsuario(command.itemId(), usuarioId).orElseThrow(this::notFound);
			if (item != null && !item.isAtivo())
				throw new DomainException("error.item.inativo");
			String descricao = command.descricao() == null || command.descricao().isBlank()
					? item == null ? null : item.getNome()
					: command.descricao();
			Long categoriaId = command.categoriaId() != null ? command.categoriaId()
					: item == null ? null : item.getCategoriaPadraoId();
			if (command.categoriaId() != null)
				CategoriaAtivaValidator.exigirAtiva(
						categorias.buscarPorIdEUsuario(command.categoriaId(), usuarioId).orElseThrow(this::notFound));
			else if (categoriaId != null)
				CategoriaAtivaValidator.exigirAtiva(
						categorias.buscarPorIdEUsuario(categoriaId, usuarioId).orElseThrow(this::notFound));
			return TransacaoItem.novo(item == null ? null : item.getId(), descricao, command.quantidade(),
					ValorMonetario.of(command.valor()), categoriaId);
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
