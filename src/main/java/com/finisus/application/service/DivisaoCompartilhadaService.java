package com.finisus.application.service;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.in.DivisaoCompartilhadaUseCase;
import com.finisus.application.ports.out.DivisaoCompartilhadaRepositoryPort;
import com.finisus.application.ports.out.UsuarioRepositoryPort;
import com.finisus.application.ports.out.VinculoTransacaoDivisaoRepositoryPort;
import com.finisus.application.ports.out.TransacaoRepositoryPort;
import com.finisus.application.ports.out.AlocacaoPagamentoDivisaoRepositoryPort;
import com.finisus.application.ports.out.ReembolsoDivisaoRepositoryPort;
import com.finisus.domain.DomainException;
import com.finisus.domain.model.DivisaoCompartilhada;
import com.finisus.domain.model.ParticipanteDivisao;
import com.finisus.domain.model.ResumoDivisao;
import com.finisus.domain.model.ResumoParticipanteDivisao;
import com.finisus.domain.model.TipoTransacao;
import com.finisus.domain.model.Transacao;
import com.finisus.domain.model.StatusSnapshotDivisao;
import com.finisus.domain.model.HistoricoParticipanteDivisao;
import com.finisus.domain.model.AlocacaoPagamentoDivisao;
import com.finisus.domain.model.ResumoPagamentoDivisao;
import com.finisus.domain.model.StatusPagamentoDivisao;
import com.finisus.domain.model.ReembolsoDivisao;
import com.finisus.domain.vo.ValorMonetario;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class DivisaoCompartilhadaService {
    private final DivisaoCompartilhadaRepositoryPort divisoes;
    private final VinculoTransacaoDivisaoRepositoryPort vinculos;
    private final TransacaoRepositoryPort transacoes;
    private final UsuarioRepositoryPort usuarios;
    private final AlocacaoPagamentoDivisaoRepositoryPort alocacoes;
    private final ReembolsoDivisaoRepositoryPort reembolsos;

    public DivisaoCompartilhadaService(DivisaoCompartilhadaRepositoryPort divisoes,
            VinculoTransacaoDivisaoRepositoryPort vinculos, TransacaoRepositoryPort transacoes,
            UsuarioRepositoryPort usuarios,
            AlocacaoPagamentoDivisaoRepositoryPort alocacoes, ReembolsoDivisaoRepositoryPort reembolsos) {
        this.divisoes = divisoes;
        this.vinculos = vinculos;
        this.transacoes = transacoes;
        this.usuarios = usuarios;
        this.alocacoes = alocacoes;
        this.reembolsos = reembolsos;
    }

    @Transactional
    public DivisaoCompartilhada criar(Long usuarioId, DivisaoCompartilhadaUseCase.CriarCommand command) {
        List<ParticipanteDivisao> participantes = participantes(command.participantes());
        validarUsuarios(participantes);
        return divisoes.salvar(DivisaoCompartilhada.nova(usuarioId, command.nome(), participantes));
    }

    @Transactional(readOnly = true)
    public Pagina<DivisaoCompartilhada> listar(Long usuarioId, Paginacao paginacao) {
        return divisoes.listarPorParticipante(usuarioId, paginacao);
    }

    @Transactional(readOnly = true)
    public DivisaoCompartilhada buscar(Long usuarioId, Long divisaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirParticipante(divisao, usuarioId);
        return divisao;
    }

    @Transactional
    public DivisaoCompartilhada atualizarParticipantes(Long usuarioId, Long divisaoId,
            List<DivisaoCompartilhadaUseCase.ParticipanteCommand> commands) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        List<ParticipanteDivisao> participantes = participantes(commands);
        validarUsuarios(participantes);
        return divisoes.salvar(divisao.comParticipantes(participantes));
    }

    @Transactional
    public DivisaoCompartilhada inativar(Long usuarioId, Long divisaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        divisao.inativar();
        return divisoes.salvar(divisao);
    }

    @Transactional(readOnly = true)
    public List<HistoricoParticipanteDivisao> listarHistoricoParticipantes(Long usuarioId, Long divisaoId) {
        buscar(usuarioId, divisaoId);
        return divisoes.listarHistoricoParticipantes(divisaoId);
    }

    @Transactional
    public void associar(Long usuarioId, Long divisaoId, Long transacaoId) {
        associar(usuarioId, divisaoId, transacaoId, null, null);
    }

    @Transactional
    public void associar(Long usuarioId, Long divisaoId, Long transacaoId, BigDecimal baseCompartilhada,
            List<com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand> commands) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirParticipanteAtivo(divisao, usuarioId);
        Transacao transacao = exigir(transacoes.buscarPorId(transacaoId));
        if (transacao.getTipo() != TipoTransacao.SAIDA || transacao.isEstornada() || !transacao.impactaSaldoDaConta()
                || !divisao.possuiParticipante(transacao.getUsuarioId())
                || (!usuarioId.equals(divisao.getCriadorId()) && !usuarioId.equals(transacao.getUsuarioId()))) {
            throw new DomainException("error.divisao.transacao.invalida");
        }
        if (vinculos.existePorTransacaoId(transacaoId)) {
            throw new DomainException("error.divisao.transacao.ja.associada");
        }
        BigDecimal base = baseCompartilhada == null ? transacao.getValor().valor() : baseCompartilhada;
        if (base.signum() <= 0 || base.compareTo(transacao.getValor().valor()) > 0 || base.scale() > 2) {
            throw new DomainException("error.divisao.base.compartilhada.invalida");
        }
        List<VinculoTransacaoDivisaoRepositoryPort.Responsabilidade> responsabilidades;
        if (commands == null || commands.isEmpty()) {
            List<BigDecimal> percentuais = calcularPercentuaisEfetivos(divisao.getParticipantes());
            List<BigDecimal> valoresDevidos = calcularDevidos(divisao.getParticipantes(), base);
            responsabilidades = java.util.stream.IntStream.range(0, divisao.getParticipantes().size())
                    .mapToObj(indice -> {
                        ParticipanteDivisao participante = divisao.getParticipantes().get(indice);
                        return new VinculoTransacaoDivisaoRepositoryPort.Responsabilidade(participante.usuarioId(),
                                percentuais.get(indice), ValorMonetario.of(valoresDevidos.get(indice)));
                    }).toList();
        } else {
            responsabilidades = validarResponsabilidades(commands, base, divisao, true);
        }
        if (base.compareTo(transacao.getValor().valor()) == 0) {
            vinculos.associar(divisaoId, transacaoId, responsabilidades);
        } else {
            vinculos.associar(divisaoId, transacaoId, responsabilidades, ValorMonetario.of(base));
        }
        alocacoes.criarInicial(divisaoId, transacaoId, transacao.getUsuarioId(), ValorMonetario.of(base));
    }

    @Transactional
    public void desassociar(Long usuarioId, Long divisaoId, Long transacaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        if (!vinculos.cancelar(divisaoId, transacaoId, usuarioId)) {
            throw new DomainException("error.recurso.nao.encontrado");
        }
    }

    @Transactional(readOnly = true)
    public List<com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.VinculoPendente> listarPendentes(
            Long usuarioId, Long divisaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        return vinculos.listarPendentes(divisaoId).stream()
                .map(v -> new com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.VinculoPendente(
                        v.transacaoId(), v.pagadorId(), v.data(), v.descricao(), v.valor().valor()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlocacaoPagamentoDivisao> listarAlocacoes(Long usuarioId, Long divisaoId, Long transacaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirParticipante(divisao, usuarioId);
        return alocacoes.listar(divisaoId, transacaoId);
    }

    @Transactional
    public List<AlocacaoPagamentoDivisao> substituirAlocacoes(Long usuarioId, Long divisaoId, Long transacaoId,
            List<com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.AlocacaoCommand> commands) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        BigDecimal base = exigir(alocacoes.buscarBaseCompartilhadaAtivaParaAtualizacao(divisaoId, transacaoId)).valor();
        if (commands == null || commands.isEmpty()
                || commands.stream().anyMatch(command -> command.transacaoId() == null || command.valor() == null
                        || command.valor().signum() <= 0 || command.valor().scale() > 2)
                || commands.stream().map(command -> command.transacaoId()).distinct().count() != commands.size()) {
            throw new DomainException("error.divisao.alocacoes.invalidas");
        }

        BigDecimal total = commands.stream().map(command -> command.valor()).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(base) > 0) {
            throw new DomainException("error.divisao.alocacoes.excedem.base");
        }

        Map<Long, Transacao> pagamentos = new LinkedHashMap<>();
        commands.stream().map(command -> command.transacaoId()).sorted().forEach(id -> {
            Transacao pagamento = exigir(transacoes.buscarPorIdParaAtualizacao(id));
            if (pagamento.getTipo() != TipoTransacao.SAIDA || pagamento.isEstornada()
                    || !pagamento.impactaSaldoDaConta() || !divisao.possuiParticipante(pagamento.getUsuarioId())) {
                throw new DomainException("error.divisao.alocacao.transacao.invalida");
            }
            pagamentos.put(id, pagamento);
        });

        List<AlocacaoPagamentoDivisaoRepositoryPort.NovaAlocacao> novas = commands.stream().map(command -> {
            Transacao pagamento = pagamentos.get(command.transacaoId());
            BigDecimal jaAlocado = alocacoes.totalAtivoPorTransacaoExcluindoVinculo(command.transacaoId(), divisaoId,
                    transacaoId);
            if (jaAlocado.add(command.valor()).compareTo(pagamento.getValor().valor()) > 0) {
                throw new DomainException("error.divisao.alocacao.excede.transacao");
            }
            return new AlocacaoPagamentoDivisaoRepositoryPort.NovaAlocacao(command.transacaoId(),
                    pagamento.getUsuarioId(), ValorMonetario.of(command.valor()));
        }).toList();
        return alocacoes.substituir(divisaoId, transacaoId, usuarioId, novas);
    }

    @Transactional
    public void cancelarAlocacao(Long usuarioId, Long divisaoId, Long transacaoId, Long alocacaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        if (!alocacoes.cancelar(divisaoId, transacaoId, alocacaoId, usuarioId)) {
            throw new DomainException("error.recurso.nao.encontrado");
        }
    }

    @Transactional
    public ReembolsoDivisao registrarReembolso(Long usuarioId, Long divisaoId, Long transacaoId,
            Long recebedorId, BigDecimal valor) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorIdParaAtualizacao(divisaoId));
        exigirParticipanteAtivo(divisao, usuarioId);
        if (recebedorId == null || valor == null || valor.signum() <= 0 || valor.scale() > 2
                || usuarioId.equals(recebedorId) || !divisao.possuiParticipante(recebedorId)) {
            throw new DomainException("error.divisao.reembolso.invalido");
        }
        Transacao transacao = exigir(transacoes.buscarPorIdParaAtualizacao(transacaoId));
        if (!usuarioId.equals(transacao.getUsuarioId()) || transacao.getTipo() != TipoTransacao.SAIDA
                || transacao.isEstornada() || !transacao.impactaSaldoDaConta()
                || reembolsos.existeAtivoPorTransacao(transacaoId)) {
            throw new DomainException("error.divisao.reembolso.transacao.invalida");
        }
        BigDecimal jaAlocado = alocacoes.totalAtivoPorTransacaoExcluindoVinculo(transacaoId, -1L, -1L);
        if (jaAlocado.add(valor).compareTo(transacao.getValor().valor()) > 0) {
            throw new DomainException("error.divisao.alocacao.excede.transacao");
        }
        ResumoDivisao posicao = consultarResumo(usuarioId, divisaoId, LocalDate.of(1000, 1, 1),
                transacao.getData());
        BigDecimal saldoPagador = posicao.participantes().stream()
                .filter(p -> p.usuarioId().equals(usuarioId)).map(ResumoParticipanteDivisao::saldo)
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal saldoRecebedor = posicao.participantes().stream()
                .filter(p -> p.usuarioId().equals(recebedorId)).map(ResumoParticipanteDivisao::saldo)
                .findFirst().orElse(BigDecimal.ZERO);
        BigDecimal limite = saldoPagador.negate().min(saldoRecebedor);
        if (saldoPagador.signum() >= 0 || saldoRecebedor.signum() <= 0 || valor.compareTo(limite) > 0) {
            throw new DomainException("error.divisao.reembolso.excede.saldo");
        }
        return reembolsos.salvar(divisaoId, transacaoId, usuarioId, recebedorId, ValorMonetario.of(valor));
    }

    @Transactional
    public void cancelarReembolso(Long usuarioId, Long divisaoId, Long reembolsoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorIdParaAtualizacao(divisaoId));
        exigirCriador(divisao, usuarioId);
        if (!reembolsos.cancelar(divisaoId, reembolsoId, usuarioId)) {
            throw new DomainException("error.recurso.nao.encontrado");
        }
    }

    @Transactional(readOnly = true)
    public List<ReembolsoDivisao> listarReembolsos(Long usuarioId, Long divisaoId) {
        buscar(usuarioId, divisaoId);
        return reembolsos.listar(divisaoId);
    }

    @Transactional(readOnly = true)
    public ResumoPagamentoDivisao consultarPagamento(Long usuarioId, Long divisaoId, Long transacaoId) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirParticipante(divisao, usuarioId);
        ValorMonetario base = exigir(alocacoes.buscarBaseCompartilhadaAtiva(divisaoId, transacaoId));
        List<AlocacaoPagamentoDivisao> ativas = alocacoes.listarAtivas(divisaoId, transacaoId);
        BigDecimal pago = ativas.stream().map(alocacao -> alocacao.valor().valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendente = base.valor().subtract(pago);
        StatusPagamentoDivisao status = pago.signum() == 0 ? StatusPagamentoDivisao.PENDENTE
                : pendente.signum() == 0 ? StatusPagamentoDivisao.QUITADO : StatusPagamentoDivisao.PARCIAL;
        return new ResumoPagamentoDivisao(divisaoId, transacaoId, base, ValorMonetario.of(pago),
                ValorMonetario.of(pendente), status, ativas);
    }

    @Transactional
    public void revisar(Long usuarioId, Long divisaoId, Long transacaoId,
            List<com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand> commands) {
        DivisaoCompartilhada divisao = exigir(divisoes.buscarPorId(divisaoId));
        exigirCriador(divisao, usuarioId);
        StatusSnapshotDivisao status = exigir(vinculos.buscarStatusParaAtualizacao(divisaoId, transacaoId));
        if (status != StatusSnapshotDivisao.PENDENTE_REVISAO) {
            throw new DomainException("error.divisao.snapshot.ja.revisado");
        }
        Transacao transacao = exigir(transacoes.buscarPorId(transacaoId));
        if (transacao.getTipo() != TipoTransacao.SAIDA || transacao.isEstornada() || !transacao.impactaSaldoDaConta()) {
            throw new DomainException("error.divisao.transacao.invalida");
        }
        List<VinculoTransacaoDivisaoRepositoryPort.Responsabilidade> responsabilidades = validarResponsabilidades(
                commands, transacao.getValor().valor(), divisao, false);
        vinculos.substituirResponsabilidades(divisaoId, transacaoId, responsabilidades);
    }

    @Transactional(readOnly = true)
    public ResumoDivisao consultarResumo(Long usuarioId, Long divisaoId, LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new DomainException("error.divisao.periodo.invalido");
        }
        DivisaoCompartilhada divisao = buscar(usuarioId, divisaoId);
        List<VinculoTransacaoDivisaoRepositoryPort.LancamentoDivisao> lancamentos = vinculos
                .listarLancamentosAtivos(divisaoId, inicio, fim);
        List<VinculoTransacaoDivisaoRepositoryPort.LancamentoDivisao> confirmados = lancamentos.stream()
                .filter(lancamento -> !lancamento.pendenteRevisao()).toList();
        BigDecimal total = confirmados.stream().map(lancamento -> lancamento.valor().valor()).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        List<AlocacaoPagamentoDivisao> alocacoesAtivas = alocacoes.listarAtivas(divisaoId, inicio, fim);
        Map<Long, BigDecimal> pagos = alocacoesAtivas.stream().collect(Collectors.groupingBy(
                AlocacaoPagamentoDivisao::pagadorId,
                Collectors.reducing(BigDecimal.ZERO, alocacao -> alocacao.valor().valor(), BigDecimal::add)));
        List<ReembolsoDivisao> reembolsosAtivos = reembolsos.listarAtivos(divisaoId, inicio, fim);
        reembolsosAtivos.forEach(reembolso -> {
            pagos.merge(reembolso.pagadorId(), reembolso.valor().valor(), BigDecimal::add);
            pagos.merge(reembolso.recebedorId(), reembolso.valor().valor().negate(), BigDecimal::add);
        });
        Map<Long, BigDecimal> devidos = confirmados.stream().flatMap(lancamento -> lancamento.responsabilidades().stream())
                .collect(Collectors.groupingBy(VinculoTransacaoDivisaoRepositoryPort.Responsabilidade::usuarioId,
                        Collectors.reducing(BigDecimal.ZERO, responsabilidade ->
                                responsabilidade.valorDevido().valor(), BigDecimal::add)));
        Map<Long, BigDecimal> percentuaisAtuais = divisao.getParticipantes().stream().collect(Collectors.toMap(
                ParticipanteDivisao::usuarioId, ParticipanteDivisao::percentual, (a, b) -> a, LinkedHashMap::new));
        Set<Long> usuariosResumo = new LinkedHashSet<>(percentuaisAtuais.keySet());
        confirmados.forEach(lancamento -> {
            lancamento.responsabilidades().forEach(r -> usuariosResumo.add(r.usuarioId()));
        });
        alocacoesAtivas.forEach(alocacao -> usuariosResumo.add(alocacao.pagadorId()));
        reembolsosAtivos.forEach(reembolso -> { usuariosResumo.add(reembolso.pagadorId()); usuariosResumo.add(reembolso.recebedorId()); });
        List<ResumoParticipanteDivisao> participantes = usuariosResumo.stream().map(id -> {
            BigDecimal pago = pagos.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal devido = devidos.getOrDefault(id, BigDecimal.ZERO);
            return new ResumoParticipanteDivisao(id, percentuaisAtuais.get(id), ValorMonetario.of(pago),
                    ValorMonetario.of(devido), pago.subtract(devido));
        }).toList();
        long pendentes = lancamentos.stream().filter(VinculoTransacaoDivisaoRepositoryPort.LancamentoDivisao::pendenteRevisao)
                .count();
        return new ResumoDivisao(divisao.getId(), divisao.getNome(), ValorMonetario.of(total), participantes, pendentes);
    }

    private List<BigDecimal> calcularDevidos(List<ParticipanteDivisao> participantes, BigDecimal total) {
        List<BigDecimal> pesos = participantes.stream()
                .map(participante -> participante.percentual() == null ? BigDecimal.ONE : participante.percentual())
                .toList();
        return distribuirPorMaioresRestos(participantes, pesos, total);
    }

    private List<BigDecimal> calcularPercentuaisEfetivos(List<ParticipanteDivisao> participantes) {
        if (participantes.stream().allMatch(participante -> participante.percentual() != null)) {
            return participantes.stream().map(ParticipanteDivisao::percentual).toList();
        }
        return distribuirPorMaioresRestos(participantes,
                participantes.stream().map(participante -> BigDecimal.ONE).toList(), new BigDecimal("100.00"));
    }

    private List<BigDecimal> distribuirPorMaioresRestos(List<ParticipanteDivisao> participantes,
            List<BigDecimal> pesos, BigDecimal total) {
        record Parcela(int indice, Long usuarioId, BigDecimal valor, BigDecimal resto) {}

        BigDecimal totalPesos = pesos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Parcela> parcelas = java.util.stream.IntStream.range(0, participantes.size()).mapToObj(indice -> {
            ParticipanteDivisao participante = participantes.get(indice);
            BigDecimal valorExato = total.multiply(pesos.get(indice)).divide(totalPesos, 12, RoundingMode.HALF_UP);
            BigDecimal valorBase = valorExato.setScale(2, RoundingMode.DOWN);
            return new Parcela(indice, participante.usuarioId(), valorBase, valorExato.subtract(valorBase));
        }).toList();

        BigDecimal totalBase = parcelas.stream().map(Parcela::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        int centavosRestantes = total.subtract(totalBase).movePointRight(2).intValueExact();
        Set<Integer> indicesComCentavo = parcelas.stream()
                .sorted(java.util.Comparator.comparing(Parcela::resto).reversed()
                        .thenComparing(Parcela::usuarioId))
                .limit(centavosRestantes)
                .map(Parcela::indice)
                .collect(Collectors.toSet());

        return parcelas.stream()
                .map(parcela -> indicesComCentavo.contains(parcela.indice())
                        ? parcela.valor().add(new BigDecimal("0.01"))
                        : parcela.valor())
                .toList();
    }

    private List<VinculoTransacaoDivisaoRepositoryPort.Responsabilidade> validarResponsabilidades(
            List<com.finisus.application.ports.in.AssociarTransacaoDivisaoUseCase.ResponsabilidadeCommand> commands,
            BigDecimal total, DivisaoCompartilhada divisao, boolean exigirParticipacaoAtual) {
        if (commands == null || commands.isEmpty()
                || commands.stream().map(c -> c.usuarioId()).distinct().count() != commands.size()) {
            throw new DomainException("error.divisao.responsabilidades.invalidas");
        }
        for (var command : commands) {
            if (command.usuarioId() == null || usuarios.buscarPorId(command.usuarioId()).isEmpty()
                    || exigirParticipacaoAtual && !divisao.possuiParticipante(command.usuarioId())) {
                throw new DomainException("error.divisao.responsabilidades.invalidas");
            }
        }
        boolean somentePercentual = commands.stream().allMatch(c -> c.percentual() != null && c.valorDevido() == null);
        boolean somenteValor = commands.stream().allMatch(c -> c.percentual() == null && c.valorDevido() != null);
        boolean completos = commands.stream().allMatch(c -> c.percentual() != null && c.valorDevido() != null);
        if (!somentePercentual && !somenteValor && !completos) {
            throw new DomainException("error.divisao.responsabilidades.invalidas");
        }

        List<ParticipanteDivisao> participantes = commands.stream()
                .map(c -> new ParticipanteDivisao(c.usuarioId(), somenteValor ? null : c.percentual())).toList();
        List<BigDecimal> percentuais = somenteValor
                ? distribuirPorMaioresRestos(participantes, commands.stream().map(c -> c.valorDevido()).toList(),
                        new BigDecimal("100.00"))
                : commands.stream().map(c -> c.percentual()).toList();
        List<BigDecimal> valores = somentePercentual
                ? distribuirPorMaioresRestos(participantes, percentuais, total)
                : commands.stream().map(c -> c.valorDevido()).toList();
        BigDecimal totalPercentual = percentuais.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDevido = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (percentuais.stream().anyMatch(p -> p.signum() <= 0 || p.compareTo(BigDecimal.valueOf(100)) > 0)
                || valores.stream().anyMatch(v -> v.signum() <= 0)
                || totalPercentual.compareTo(BigDecimal.valueOf(100)) != 0 || totalDevido.compareTo(total) != 0) {
            throw new DomainException("error.divisao.responsabilidades.invalidas");
        }
        return java.util.stream.IntStream.range(0, commands.size())
                .mapToObj(i -> new VinculoTransacaoDivisaoRepositoryPort.Responsabilidade(commands.get(i).usuarioId(),
                        percentuais.get(i), ValorMonetario.of(valores.get(i))))
                .toList();
    }

    private List<ParticipanteDivisao> participantes(List<DivisaoCompartilhadaUseCase.ParticipanteCommand> commands) {
        if (commands == null) {
            throw new DomainException("error.divisao.participantes.invalidos");
        }
        return commands.stream().map(command -> new ParticipanteDivisao(command.usuarioId(), command.percentual())).toList();
    }

    private void validarUsuarios(List<ParticipanteDivisao> participantes) {
        for (ParticipanteDivisao participante : participantes) {
            if (usuarios.buscarPorId(participante.usuarioId()).filter(usuario -> usuario.isAtivo()).isEmpty()) {
                throw new DomainException("error.recurso.nao.encontrado");
            }
        }
    }

    private void exigirParticipanteAtivo(DivisaoCompartilhada divisao, Long usuarioId) {
        exigirParticipante(divisao, usuarioId);
        if (!divisao.isAtiva()) {
            throw new DomainException("error.divisao.inativa");
        }
    }

    private void exigirParticipante(DivisaoCompartilhada divisao, Long usuarioId) {
        if (!divisao.possuiParticipante(usuarioId)) {
            throw new DomainException("error.recurso.sem.permissao");
        }
    }

    private void exigirCriador(DivisaoCompartilhada divisao, Long usuarioId) {
        if (!divisao.getCriadorId().equals(usuarioId)) {
            throw new DomainException("error.recurso.sem.permissao");
        }
    }

    private <T> T exigir(java.util.Optional<T> value) {
        return value.orElseThrow(() -> new DomainException("error.recurso.nao.encontrado"));
    }
}
