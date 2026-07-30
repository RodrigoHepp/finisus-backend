package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.*;
import com.financeiro.adapters.out.persistence.repository.*;
import com.financeiro.application.pagination.Pagina;
import com.financeiro.application.pagination.Paginacao;
import com.financeiro.application.ports.out.CompartilhamentoRepositoryPort;
import com.financeiro.domain.model.*;
import com.financeiro.domain.vo.ValorMonetario;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class CompartilhamentoPersistenceAdapter implements CompartilhamentoRepositoryPort {
    private final ConfiguracaoCompartilhamentoJpaRepository configuracoes; private final DespesaCompartilhadaJpaRepository despesas; private final RateioDespesaJpaRepository rateios;
    public CompartilhamentoPersistenceAdapter(ConfiguracaoCompartilhamentoJpaRepository configuracoes, DespesaCompartilhadaJpaRepository despesas, RateioDespesaJpaRepository rateios) { this.configuracoes = configuracoes; this.despesas = despesas; this.rateios = rateios; }
    public ConfiguracaoCompartilhamento salvarConfiguracao(ConfiguracaoCompartilhamento configuracao) { ConfiguracaoCompartilhamentoJpaEntity entity = new ConfiguracaoCompartilhamentoJpaEntity(); entity.setId(configuracao.getId()); entity.setUsuarioId(configuracao.getUsuarioId()); entity.setAceitaCompartilhamento(configuracao.isAceitaCompartilhamento()); entity = configuracoes.save(entity); return ConfiguracaoCompartilhamento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.isAceitaCompartilhamento()); }
    public Optional<ConfiguracaoCompartilhamento> buscarConfiguracao(Long usuarioId) { return configuracoes.findByUsuarioId(usuarioId).map(entity -> ConfiguracaoCompartilhamento.reconstituir(entity.getId(), entity.getUsuarioId(), entity.isAceitaCompartilhamento())); }
    public DespesaCompartilhada salvarDespesa(DespesaCompartilhada despesa) { DespesaCompartilhadaJpaEntity entity = new DespesaCompartilhadaJpaEntity(); entity.setId(despesa.getId()); entity.setTransacaoId(despesa.getTransacaoId()); entity.setCriadorId(despesa.getCriadorId()); entity.setTipoRateio(despesa.getTipoRateio()); entity = despesas.save(entity); return toDomain(entity); }
    public Optional<DespesaCompartilhada> buscarDespesaPorIdECriador(Long despesaId, Long criadorId) { return despesas.findByIdAndCriadorId(despesaId, criadorId).map(this::toDomain); }
    public List<DespesaCompartilhada> listarDespesasPorCriador(Long criadorId) { return despesas.findByCriadorId(criadorId).stream().map(this::toDomain).toList(); }
    public Pagina<DespesaCompartilhada> listarDespesasPorCriador(Long criadorId, Paginacao paginacao) { return PaginaJpaMapper.map(despesas.findByCriadorId(criadorId, PaginaJpaMapper.pageable(paginacao, Sort.by("id").descending())), this::toDomain); }
    public List<RateioDespesa> salvarRateios(Long despesaId, List<RateioDespesa> values) { DespesaCompartilhadaJpaEntity despesa = despesas.getReferenceById(despesaId); return values.stream().map(rateio -> { RateioDespesaJpaEntity entity = new RateioDespesaJpaEntity(); entity.setDespesaCompartilhada(despesa); entity.setTipoParticipante(rateio.getTipoParticipante()); entity.setUsuarioId(rateio.getUsuarioId()); entity.setNomeExterno(rateio.getNomeExterno()); entity.setEmailExterno(rateio.getEmailExterno()); entity.setValorFixo(rateio.getValorFixo() == null ? null : rateio.getValorFixo().valor()); entity.setPercentual(rateio.getPercentual()); entity.setStatus(rateio.getStatus()); return toDomain(rateios.save(entity)); }).toList(); }
    public Optional<RateioDespesa> buscarRateio(Long rateioId) { return rateios.findById(rateioId).map(this::toDomain); }
    public RateioDespesa salvarRateio(RateioDespesa rateio) { RateioDespesaJpaEntity entity = rateios.findById(rateio.getId()).orElseThrow(); entity.setStatus(rateio.getStatus()); return toDomain(rateios.save(entity)); }
    public List<RateioDespesa> listarRateios(Long despesaId) { return rateios.findByDespesaCompartilhadaId(despesaId).stream().map(this::toDomain).toList(); }
    public Pagina<RateioDespesa> listarRateios(Long despesaId, Paginacao paginacao) { return PaginaJpaMapper.map(rateios.findByDespesaCompartilhadaId(despesaId, PaginaJpaMapper.pageable(paginacao, Sort.by("id").ascending())), this::toDomain); }
    private DespesaCompartilhada toDomain(DespesaCompartilhadaJpaEntity entity) { return DespesaCompartilhada.reconstituir(entity.getId(), entity.getTransacaoId(), entity.getCriadorId(), entity.getTipoRateio()); }
    private RateioDespesa toDomain(RateioDespesaJpaEntity entity) { return RateioDespesa.reconstituir(entity.getId(), entity.getDespesaCompartilhada().getId(), entity.getTipoParticipante(), entity.getUsuarioId(), entity.getNomeExterno(), entity.getEmailExterno(), entity.getValorFixo() == null ? null : ValorMonetario.of(entity.getValorFixo()), entity.getPercentual(), entity.getStatus()); }
}
