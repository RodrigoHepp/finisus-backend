package com.financeiro.adapters.out.persistence;

import com.financeiro.adapters.out.persistence.entity.RefreshTokenJpaEntity;
import com.financeiro.adapters.out.persistence.repository.RefreshTokenJpaRepository;
import com.financeiro.application.ports.out.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {
    private final RefreshTokenJpaRepository repository;
    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repository) { this.repository = repository; }
    public void salvar(String token, Long usuarioId, Instant expiraEm) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setToken(hash(token)); entity.setUsuarioId(usuarioId); entity.setExpiraEm(expiraEm); entity.setInvalidado(false); entity.setCriadoEm(LocalDateTime.now());
        repository.save(entity);
    }
    public Optional<Long> buscarUsuarioIdPorToken(String token) { return repository.findByTokenAndInvalidadoFalseAndExpiraEmAfter(hash(token), Instant.now()).map(RefreshTokenJpaEntity::getUsuarioId); }
    @Transactional public void invalidar(String token) { repository.invalidarPorToken(hash(token)); }
    @Transactional public void invalidarTodosDoUsuario(Long usuarioId) { repository.invalidarTodosDoUsuario(usuarioId); }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }
}
