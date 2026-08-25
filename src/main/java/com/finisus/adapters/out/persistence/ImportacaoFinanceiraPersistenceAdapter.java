package com.finisus.adapters.out.persistence;

import com.finisus.adapters.out.persistence.entity.ImportacaoFinanceiraJpaEntity;
import com.finisus.adapters.out.persistence.entity.LancamentoImportadoJpaEntity;
import com.finisus.adapters.out.persistence.repository.ImportacaoFinanceiraJpaRepository;
import com.finisus.application.ports.out.ImportacaoFinanceiraRepositoryPort;
import com.finisus.domain.model.ImportacaoFinanceira;
import com.finisus.domain.model.LancamentoImportado;
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
			item.setValor(lancamento.getValor());
			item.setTipo(lancamento.getTipo());
			item.setPendenteConfirmacao(lancamento.isPendenteConfirmacao());
			item.setMotivoPendencia(lancamento.getMotivoPendencia());
			item.setImportar(lancamento.isImportar());
			item.setCategoriaId(lancamento.getCategoriaId());
			item.setItemId(lancamento.getItemId());
			item.setTransacaoId(lancamento.getTransacaoId());
			entity.getLancamentos().add(item);
		}
		return toDomain(repository.save(entity));
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
				entity.getNomeArquivo(), entity.getHashArquivo(), entity.getLeitor(), entity.getTipoDocumento(), entity.getIdentificadorOrigem(),
				entity.getPeriodoInicio(), entity.getPeriodoFim(), entity.getDataVencimento(), entity.getSaldoInicial(),
				entity.getSaldoFinal(), entity.getValorTotal(), entity.getStatus(), entity.getContaId(), entity.getFaturaId(),
				entity.getVersion() == null ? 0 : entity.getVersion(), entity.getLancamentos().stream().map(item ->
					LancamentoImportado.reconstituir(item.getId(), item.getOrdem(), item.getData(), item.getDescricao(),
							item.getConteudoOriginal(), item.getValor(), item.getTipo(), item.isPendenteConfirmacao(),
							item.getMotivoPendencia(), item.isImportar(), item.getCategoriaId(), item.getItemId(),
							item.getTransacaoId())).toList());
	}
}
