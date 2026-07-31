package com.finisus.adapters.out.persistence;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.finisus.application.pagination.Pagina;
import com.finisus.application.pagination.Paginacao;

final class PaginaJpaMapper {
	private PaginaJpaMapper() {
	}

	static Pageable pageable(Paginacao paginacao, Sort sort) {
		return PageRequest.of(paginacao.pagina(), paginacao.tamanho(), sort);
	}

	static <E, D> Pagina<D> map(Page<E> page, Function<E, D> mapper) {
		return new Pagina<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}
}
