package com.finisus.application.service;

import com.finisus.domain.DomainException;
import com.finisus.domain.model.Categoria;

final class CategoriaAtivaValidator {
	private CategoriaAtivaValidator() {
	}

	static Categoria exigirAtiva(Categoria categoria) {
		if (!categoria.isAtivo())
			throw new DomainException("error.categoria.inativa");
		return categoria;
	}
}
