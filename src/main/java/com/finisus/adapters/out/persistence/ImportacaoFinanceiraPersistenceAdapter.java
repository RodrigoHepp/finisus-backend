package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.ImportacaoFinanceiraJpaEntity;
import com.finisus.adapters.out.persistence.entity.LancamentoImportadoJpaEntity;
import com.finisus.adapters.out.persistence.entity.RevisaoLancamentoImportadoJpaEntity;
import com.finisus.adapters.out.persistence.repository.ImportacaoFinanceiraJpaRepository;
import com.finisus.application.ports.out.ImportacaoFinanceiraRepositoryPort;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.LancamentoImportado;
import com.finisus.domain.model.RevisaoLancamentoImportado;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ImportacaoFinanceiraPersistenceAdapter implements ImportacaoFinanceiraRepositoryPort {
	private final ImportacaoFinanceiraJpaRepository repository;

	public ImportacaoFinanceiraPersistenceAdapter(ImportacaoFinanceiraJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public ImportacaoFinanceira salvar(ImportacaoFinanceira importacao) {
		ImportacaoFinanceiraJpaEntity entity = new ImportacaoFinanceiraJpaEntity();
		entity.setId(importacao.getId());
		entity.setUsuarioId(importacao.getUsuarioId());
		entity.setBancoId(importacao.getBancoId());
		entity.setNomeArquivo(importacao.getNomeArquivo());
		entity.setHashArquivo(importacao.getHashArquivo());
		entity.setLeitor(importacao.getLeitor());
		entity.setTipoDocumento(importacao.getTipoDocumento());
		entity.setTipoDocumentoPretendido(importacao.getTipoDocumentoPretendido());
		entity.setIdentificadorOrigem(importacao.getIdentificadorOrigem());
		entity.setPeriodoInicio(importacao.getPeriodoInicio());
		entity.setPeriodoFim(importacao.getPeriodoFim());
		entity.setDataVencimento(importacao.getDataVencimento());
		entity.setSaldoInicial(importacao.getSaldoInicial());
		entity.setSaldoFinal(importacao.getSaldoFinal());
		entity.setValorTotal(importacao.getValorTotal());
		entity.setStatus(importacao.getStatus());
		entity.setContaId(importacao.getContaId());
		entity.setFaturaId(importacao.getFaturaId());
		entity.setVersion(importacao.getVersion());
		for (LancamentoImportado lancamento : importacao.getLancamentos()) {
			LancamentoImportadoJpaEntity item = new LancamentoImportadoJpaEntity();
			item.setId(lancamento.getId());
			item.setImportacao(entity);
			item.setOrdem(lancamento.getOrdem());
			item.setData(lancamento.getData());
			item.setDescricao(lancamento.getDescricao());
			item.setConteudoOriginal(lancamento.getConteudoOriginal());
			item.setDataOriginal(lancamento.getDataOriginal());
			item.setDescricaoOriginal(lancamento.getDescricaoOriginal());
			item.setValorOriginal(lancamento.getValorOriginal());
			item.setTipoOriginal(lancamento.getTipoOriginal());
			item.setValor(lancamento.getValor());
			item.setTipo(lancamento.getTipo());
			item.setPendenteConfirmacao(lancamento.isPendenteConfirmacao());
			item.setMotivoPendencia(lancamento.getMotivoPendencia());
			item.setEstado(lancamento.getEstado());
			item.setImportar(lancamento.isImportar());
			item.setCategoriaId(lancamento.getCategoriaId());
			item.setItemId(lancamento.getItemId());
			item.setTransacaoId(lancamento.getTransacaoId());
			item.setObrigacaoFinanceiraId(lancamento.getObrigacaoFinanceiraId());
			for (RevisaoLancamentoImportado revisao : lancamento.getRevisoes()) {
				RevisaoLancamentoImportadoJpaEntity historico = toEntity(revisao);
				historico.setLancamento(item);
				item.getRevisoes().add(historico);
			}
			entity.getLancamentos().add(item);
		}
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ImportacaoFinanceira> buscarPorUsuarioBancoEHash(Long usuarioId, Long bancoId,
			String hashArquivo) {
		return repository.findByUsuarioIdAndBancoIdAndHashArquivo(usuarioId, bancoId, hashArquivo)
				.map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ImportacaoFinanceira> buscarPorIdEUsuario(Long importacaoId, Long usuarioId) {
		return repository.findByIdAndUsuarioId(importacaoId, usuarioId).map(this::toDomain);
	}

	@Override
	public Optional<ImportacaoFinanceira> buscarPorIdEUsuarioParaAtualizacao(Long importacaoId, Long usuarioId) {
		return repository.findByIdAndUsuarioIdForUpdate(importacaoId, usuarioId).map(this::toDomain);
	}

	private ImportacaoFinanceira toDomain(ImportacaoFinanceiraJpaEntity entity) {
		return ImportacaoFinanceira.reconstituir(entity.getId(), entity.getUsuarioId(), entity.getBancoId(),
				entity.getNomeArquivo(), entity.getHashArquivo(), entity.getLeitor(), entity.getTipoDocumento(),
				entity.getTipoDocumentoPretendido(), entity.getIdentificadorOrigem(),
				entity.getPeriodoInicio(), entity.getPeriodoFim(), entity.getDataVencimento(), entity.getSaldoInicial(),
				entity.getSaldoFinal(), entity.getValorTotal(), entity.getStatus(), entity.getContaId(), entity.getFaturaId(),
				entity.getVersion() == null ? 0 : entity.getVersion(), entity.getLancamentos().stream().map(item ->
					LancamentoImportado.reconstituir(item.getId(), item.getOrdem(), item.getData(), item.getDescricao(),
							item.getConteudoOriginal(), item.getValor(), item.getTipo(), item.isPendenteConfirmacao(),
							item.getMotivoPendencia(), item.isImportar(), item.getCategoriaId(), item.getItemId(),
							item.getTransacaoId(), item.getObrigacaoFinanceiraId(), item.getDataOriginal(),
							item.getDescricaoOriginal(), item.getValorOriginal(), item.getTipoOriginal(),
							item.getRevisoes().stream().map(this::toDomain).toList(), item.getEstado())).toList());
	}

	private RevisaoLancamentoImportadoJpaEntity toEntity(RevisaoLancamentoImportado revisao) {
		var entity = new RevisaoLancamentoImportadoJpaEntity();
		entity.setId(revisao.id()); entity.setRevisadoPor(revisao.revisadoPor()); entity.setDecisao(revisao.decisao());
		entity.setMotivoIncerteza(revisao.motivoIncerteza()); entity.setJustificativa(revisao.justificativa());
		entity.setDataAnterior(revisao.anterior().data()); entity.setDescricaoAnterior(revisao.anterior().descricao());
		entity.setValorAnterior(revisao.anterior().valor()); entity.setTipoAnterior(revisao.anterior().tipo());
		entity.setImportarAnterior(revisao.anterior().importar());
		entity.setCategoriaIdAnterior(revisao.anterior().categoriaId()); entity.setItemIdAnterior(revisao.anterior().itemId());
		entity.setTransacaoIdAnterior(revisao.anterior().transacaoId());
		entity.setObrigacaoIdAnterior(revisao.anterior().obrigacaoFinanceiraId());
		entity.setDataNova(revisao.novo().data()); entity.setDescricaoNova(revisao.novo().descricao());
		entity.setValorNovo(revisao.novo().valor()); entity.setTipoNovo(revisao.novo().tipo());
		entity.setImportarNovo(revisao.novo().importar()); entity.setCategoriaIdNova(revisao.novo().categoriaId());
		entity.setItemIdNovo(revisao.novo().itemId()); entity.setTransacaoIdNova(revisao.novo().transacaoId());
		entity.setObrigacaoIdNova(revisao.novo().obrigacaoFinanceiraId()); entity.setRevisadoEm(revisao.revisadoEm());
		return entity;
	}

	private RevisaoLancamentoImportado toDomain(RevisaoLancamentoImportadoJpaEntity entity) {
		var anterior = new RevisaoLancamentoImportado.Estado(entity.getDataAnterior(), entity.getDescricaoAnterior(),
				entity.getValorAnterior(), entity.getTipoAnterior(), entity.isImportarAnterior(),
				entity.getCategoriaIdAnterior(), entity.getItemIdAnterior(), entity.getTransacaoIdAnterior(),
				entity.getObrigacaoIdAnterior());
		var novo = new RevisaoLancamentoImportado.Estado(entity.getDataNova(), entity.getDescricaoNova(),
				entity.getValorNovo(), entity.getTipoNovo(), entity.isImportarNovo(), entity.getCategoriaIdNova(),
				entity.getItemIdNovo(), entity.getTransacaoIdNova(), entity.getObrigacaoIdNova());
		return new RevisaoLancamentoImportado(entity.getId(), entity.getRevisadoPor(), entity.getDecisao(),
				entity.getMotivoIncerteza(), entity.getJustificativa(), anterior, novo, entity.getRevisadoEm());
	}
}
