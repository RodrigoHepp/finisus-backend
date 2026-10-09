package com.finisus.application.ports.out;

import java.util.List;
import java.util.Map;

public interface DadosPessoaisPort {
	Map<String, List<Map<String, Object>>> exportar(Long usuarioId);
}
