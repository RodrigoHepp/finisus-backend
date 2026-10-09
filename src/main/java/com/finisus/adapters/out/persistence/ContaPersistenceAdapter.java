package com.finisus.adapters.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.finisus.adapters.out.persistence.entity.ContaJpaEntity;
import com.finisus.adapters.out.persistence.repository.ContaJpaRepository;
import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;
import com.finisus.application.ports.out.ContaRepositoryPort;
import com.finisus.domain.model.Conta;
import com.finisus.domain.vo.ValorMonetario;
import jakarta.persistence.EntityManager;

@Component
public class ContaPersistenceAdapter implements ContaRepositoryPort {
	private final ContaJpaRepository repository;
	private final EntityManager entityManager;

	public ContaPersistenceAdapter(ContaJpaRepository repository, EntityManager entityManager) {
		this.repository = repository;
		this.entityManager = entityManager;
	}

	@Override
	public Conta salvar(Conta conta) {
		ContaJpaEntity e = new ContaJpaEntity();
		e.setId(conta.getId());
		e.setUsuarioId(conta.getUsuarioId());
		e.setNome(conta.getNome());
		e.setTipo(conta.getTipo());
		e.setBancoId(conta.getBancoId());
		e.setSaldo(conta.getSaldo().valor());
		e.setAtivo(conta.isAtivo());
		e.setVersion(conta.getVersion());
		return toDomain(repository.save(e));
	}

	@Override
	public Optional<Conta> buscarPorId(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId) {
		return repository.findByIdAndUsuarioId(id, usuarioId).map(this::toDomain);
	}

	@Override
	public List<Conta> buscarPorIdsEUsuarioParaAtualizacao(List<Long> ids, Long usuarioId) {
		return repository.findByIdsAndUsuarioIdForUpdate(ids, usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public List<Conta> listarPorUsuario(Long usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
	}

	@Override
	public Pagina<Conta> listarPorUsuario(Long usuarioId, Paginacao paginacao) {
		return PaginaJpaMapper.map(
				repository.findByUsuarioId(usuarioId,
						PaginaJpaMapper.pageable(paginacao, Sort.by("nome").ascending().and(Sort.by("id")))),
				this::toDomain);
	}

	@Override
	public boolean existePorIdEUsuario(Long id, Long usuarioId) {
		return repository.existsByIdAndUsuarioId(id, usuarioId);
	}

	@Override
	public ResumoMovimentosConta calcularMovimentosEficazes(Long contaId, Long usuarioId) {
		Object[] resultado = (Object[]) entityManager.createNativeQuery("""
				SELECT
				  COALESCE((SELECT SUM(CASE WHEN t.tipo = 'ENTRADA' THEN t.valor ELSE -t.valor END)
				            FROM transacao t
				            WHERE t.conta_id = :contaId AND t.usuario_id = :usuarioId
				              AND t.estornado_em IS NULL AND t.fatura_id IS NULL
				              AND t.compra_parcelada_id IS NULL), 0)
				  + COALESCE((SELECT SUM(a.valor_ajuste) FROM ajuste_saldo_conta a
				              WHERE a.conta_id = :contaId AND a.usuario_id = :usuarioId), 0),
				  (SELECT COUNT(*) FROM transacao t
				   WHERE t.conta_id = :contaId AND t.usuario_id = :usuarioId
				     AND t.estornado_em IS NULL AND t.fatura_id IS NULL AND t.compra_parcelada_id IS NULL),
				  (SELECT COUNT(*) FROM ajuste_saldo_conta a
				   WHERE a.conta_id = :contaId AND a.usuario_id = :usuarioId)
				""").setParameter("contaId", contaId).setParameter("usuarioId", usuarioId).getSingleResult();
		return new ResumoMovimentosConta((java.math.BigDecimal) resultado[0], ((Number) resultado[1]).longValue(),
				((Number) resultado[2]).longValue());
	}

	private Conta toDomain(ContaJpaEntity e) {
		return Conta.reconstituir(e.getId(), e.getUsuarioId(), e.getNome(), e.getTipo(), e.getBancoId(),
				ValorMonetario.of(e.getSaldo()), e.isAtivo(), e.getVersion() == null ? 0 : e.getVersion());
	}
}
