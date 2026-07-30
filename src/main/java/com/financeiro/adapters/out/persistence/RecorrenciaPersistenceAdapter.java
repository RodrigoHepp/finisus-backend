package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.RecorrenciaGeracaoJpaEntity;
import com.financeiro.adapters.out.persistence.entity.RecorrenciaJpaEntity;
import com.financeiro.adapters.out.persistence.repository.RecorrenciaGeracaoJpaRepository;
import com.financeiro.adapters.out.persistence.repository.RecorrenciaJpaRepository;
import com.financeiro.application.ports.out.RecorrenciaRepositoryPort;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.domain.model.Recorrencia;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Component
public class RecorrenciaPersistenceAdapter implements RecorrenciaRepositoryPort {
    private final RecorrenciaJpaRepository repository;
    private final RecorrenciaGeracaoJpaRepository geracaoRepository;
    public RecorrenciaPersistenceAdapter(RecorrenciaJpaRepository repository, RecorrenciaGeracaoJpaRepository geracaoRepository) { this.repository = repository; this.geracaoRepository = geracaoRepository; }
    public Recorrencia salvar(Recorrencia recorrencia) { RecorrenciaJpaEntity e = new RecorrenciaJpaEntity(); e.setId(recorrencia.getId()); e.setUsuarioId(recorrencia.getUsuarioId()); e.setNome(recorrencia.getNome()); e.setTipo(recorrencia.getTipo()); e.setValorEsperado(recorrencia.getValorEsperado().valor()); e.setDiaDoMes(recorrencia.getDiaDoMes()); e.setCategoriaId(recorrencia.getCategoriaId()); e.setContaId(recorrencia.getContaId()); e.setMeioPagamentoId(recorrencia.getMeioPagamentoId()); e.setAtivo(recorrencia.isAtivo()); return toDomain(repository.save(e)); }
    public Optional<Recorrencia> buscarPorId(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<Recorrencia> buscarPorIdEUsuario(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain); }
    public List<Recorrencia> listarPorUsuario(Long usuarioId) { return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList(); }
    public Pagina<Recorrencia> listarPorUsuario(Long usuarioId, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioId(usuarioId, PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))), this::toDomain); }
    public List<Recorrencia> listarAtivasPorUsuario(Long usuarioId) { return repository.findByUsuarioIdAndAtivoTrue(usuarioId).stream().map(this::toDomain).toList(); }
    public void registrarGeracao(Long recorrenciaId, AnoMes anoMes, Long transacaoId) { RecorrenciaGeracaoJpaEntity e = new RecorrenciaGeracaoJpaEntity(); e.setRecorrenciaId(recorrenciaId); e.setAnoMes(anoMes.formatado()); e.setTransacaoId(transacaoId); geracaoRepository.save(e); }
    public boolean existsGeracaoPorRecorrenciaEAnoMes(Long recorrenciaId, AnoMes anoMes) { return geracaoRepository.existsByRecorrenciaIdAndAnoMes(recorrenciaId, anoMes.formatado()); }
    private Recorrencia toDomain(RecorrenciaJpaEntity e) { return Recorrencia.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getTipo(), ValorMonetario.of(e.getValorEsperado()), e.getDiaDoMes(), e.getCategoriaId(), e.getContaId(), e.getMeioPagamentoId(), e.isAtivo()); }
}
