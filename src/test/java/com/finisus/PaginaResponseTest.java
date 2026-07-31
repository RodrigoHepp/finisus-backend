package com.finisus;

import com.finisus.adapters.in.web.PaginaResponse;
import com.finisus.application.pagination.Pagina;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PaginaResponseTest {
	@Test
	void recortaConteudoEMantemMetadados() {
		var pagina = PaginaResponse.from(new Pagina<>(List.of(3, 4), 1, 2, 5, 3));
		assertThat(pagina.conteudo()).containsExactly(3, 4);
		assertThat(pagina.totalElementos()).isEqualTo(5);
		assertThat(pagina.totalPaginas()).isEqualTo(3);
	}
}
