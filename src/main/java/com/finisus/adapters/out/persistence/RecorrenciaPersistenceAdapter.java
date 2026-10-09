package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.RecorrenciaGeracaoJpaEntity;
import com.finisus.adapters.out.persistence.entity.RecorrenciaJpaEntity;
import com.finisus.adapters.out.persistence.repository.RecorrenciaGeracaoJpaRepository;
import com.finisus.adapters.out.persistence.repository.RecorrenciaJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.RecorrenciaRepositoryPort;
import com.finisus.domain.model.Recorrencia;
import com.finisus.domain.model.OcorrenciaRecorrencia;
import com.finisus.domain.vo.AnoMes;
import com.finisus.domain.vo.ValorMonetario;

@Component
public class RecorrenciaPersistenceAdapter implements RecorrenciaRepositoryPort {
	private final RecorrenciaJpaRepository repository;
	private final RecorrenciaGeracaoJpaRepository geracaoRepository;

	public RecorrenciaPersistenceAdapter(RecorrenciaJpaRepository repository,
			RecorrenciaGeracaoJpaRepository geracaoRepository) {
		this.repository = repository;
		this.geracaoRepository = geracaoRepository;
	}

	@Override
	public Recorrencia salvar(Recorrencia recorrencia) {
		RecorrenciaJpaEntity e = new RecorrenciaJpaEntity();
		e.setId(recorrencia.getId());
		e.setUsuarioId(recorrencia.getUsuarioId());
		e.setNome(recorrencia.getNome());
		e.setTipo(recorrencia.getTipo());
		e.setValorEsperado(recorrencia.getValorEsperado().valor());
		e.setDiaDoMes(recorrencia.getDiaDoMes());
		e.setCategoriaId(recorrencia.getCategoriaId());
		e.setContaId(recorrencia.getContaId());
		e.setMeioPagamentoId(recorrencia.getMeioPagamentoId());
		e.setAtivo(recorrencia.isAtivo());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<Recorrencia> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Recorrencia> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Recorrencia> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Recorrencia> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioId(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	@Override
	public List<Recorrencia> listarAtivasPorUsuario(Long usuarioId) {
		return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public OcorrenciaRecorrencia salvarOcorrencia(OcorrenciaRecorrencia ocorrencia) {
		RecorrenciaGeracaoJpaEntity e = new RecorrenciaGeracaoJpaEntity();
		e.setId(ocorrencia.id());
		e.setRecorrenciaId(ocorrencia.recorrenciaId());
		e.setUsuarioId(ocorrencia.usuarioId());
		e.setAnoMes(ocorrencia.anoMes());
		e.setVencimento(ocorrencia.vencimento());
		e.setTipo(ocorrencia.tipo());
		e.setValor(ocorrencia.valor().valor());
		e.setDescricao(ocorrencia.descricao());
		e.setContaId(ocorrencia.contaId());
		e.setCategoriaId(ocorrencia.categoriaId());
		e.setMeioPagamentoId(ocorrencia.meioPagamentoId());
		e.setStatus(ocorrencia.status());
		e.setTransacaoId(ocorrencia.transacaoId());
		return toDomain(geracaoRepository.save(e));
	}

	@Override
	public Optional<OcorrenciaRecorrencia> buscarOcorrenciaParaAtualizacao(Long id, Long usuarioId) {
		return geracaoRepository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<OcorrenciaRecorrencia> listarOcorrencias(Long usuarioId, AnoMes anoMes) {
		return geracaoRepository.findByUsuarioIdAndAnoMesOrderByVencimentoAscIdAsc(usuarioId, anoMes.formatado())
				.stream().map(this::toDomain).toList();
	}

	@Override
	public boolean existsGeracaoPorRecorrenciaEAnoMes(Long recorrenciaId, AnoMes anoMes) {
		return geracaoRepository.existsByRecorrenciaIdAndAnoMes(recorrenciaId, anoMes.formatado());
	}

	@Override
	public Set<ChaveGeracao> listarGeracoesExistentes(List<Long> recorrenciasIds, AnoMes inicio, AnoMes fim) {
		if (recorrenciasIds.isEmpty()) return Set.of();
		return geracaoRepository.findChavesByRecorrenciaIdInAndAnoMesBetween(recorrenciasIds, inicio.formatado(), fim.formatado())
				.stream().map(linha -> new ChaveGeracao((Long) linha[0], AnoMes.parse((String) linha[1])))
				.collect(Collectors.toSet());
	}

	private Recorrencia toDomain(RecorrenciaJpaEntity e) {
		return Recorrencia.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getTipo(),
				ValorMonetario.of(e.getValorEsperado()), e.getDiaDoMes(), e.getCategoriaId(), e.getContaId(),
				e.getMeioPagamentoId(), e.isAtivo());
	}

	private OcorrenciaRecorrencia toDomain(RecorrenciaGeracaoJpaEntity e) {
		return new OcorrenciaRecorrencia(e.getId(), e.getRecorrenciaId(), e.getUsuarioId(), e.getAnoMes(),
				e.getVencimento(), e.getTipo(), ValorMonetario.of(e.getValor()), e.getDescricao(), e.getContaId(),
				e.getCategoriaId(), e.getMeioPagamentoId(), e.getStatus(), e.getTransacaoId());
	}
}
