package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.PrevisaoMensalJpaEntity;
import com.financeiro.adapters.out.persistence.repository.PrevisaoMensalJpaRepository;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.PrevisaoMensalRepositoryPort;
import com.financeiro.domain.model.PrevisaoMensal;
import com.financeiro.domain.vo.AnoMes;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class PrevisaoMensalPersistenceAdapter implements PrevisaoMensalRepositoryPort {
    private final PrevisaoMensalJpaRepository repository;
    public PrevisaoMensalPersistenceAdapter(PrevisaoMensalJpaRepository repository) { this.repository = repository; }
    @Transactional public void substituir(Long usuarioId, AnoMes anoMes, List<PrevisaoMensal> previsoes) { repository.deleteByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado()); repository.saveAll(previsoes.stream().map(this::toEntity).toList()); }
    public List<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes) { return repository.findByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado()).stream().map(this::toDomain).toList(); }
    public Pagina<PrevisaoMensal> listar(Long usuarioId, AnoMes anoMes, Paginacao paginacao) { return PaginaJpaMapper.map(repository.findByUsuarioIdAndAnoMes(usuarioId, anoMes.formatado(), PaginaJpaMapper.pageable(paginacao, Sort.by("categoriaId").ascending().and(Sort.by("id").ascending()))), this::toDomain); }
    private PrevisaoMensalJpaEntity toEntity(PrevisaoMensal p) { PrevisaoMensalJpaEntity e = new PrevisaoMensalJpaEntity(); e.setUsuarioId(p.getUsuarioId()); e.setAnoMes(p.getAnoMes().formatado()); e.setCategoriaId(p.getCategoriaId()); e.setValorProjetadoEntrada(p.getValorProjetadoEntrada().valor()); e.setValorProjetadoSaida(p.getValorProjetadoSaida().valor()); e.setCalculadoEm(p.getCalculadoEm()); return e; }
    private PrevisaoMensal toDomain(PrevisaoMensalJpaEntity e) { return PrevisaoMensal.reconstituir(e.getId(), e.getUsuarioId(), AnoMes.parse(e.getAnoMes()), e.getCategoriaId(), ValorMonetario.of(e.getValorProjetadoEntrada()), ValorMonetario.of(e.getValorProjetadoSaida()), e.getCalculadoEm()); }
}
