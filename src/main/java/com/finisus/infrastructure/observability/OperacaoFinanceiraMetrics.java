package com.finisus.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Component
public class OperacaoFinanceiraMetrics {
	private final MeterRegistry registry;
	private final ConcurrentHashMap<String, AtomicInteger> emAndamento = new ConcurrentHashMap<>();

	public OperacaoFinanceiraMetrics(MeterRegistry registry) {
		this.registry = registry;
	}

	public <T> T medir(String operacao, Supplier<T> acao) {
		AtomicInteger concorrencia = emAndamento.computeIfAbsent(operacao, this::registrarGauge);
		concorrencia.incrementAndGet();
		Timer.Sample inicio = Timer.start(registry);
		try {
			T resultado = acao.get();
			registrar(operacao, "sucesso", inicio);
			return resultado;
		} catch (RuntimeException exception) {
			registrar(operacao, "falha", inicio);
			throw exception;
		} finally {
			concorrencia.decrementAndGet();
		}
	}

	public void medir(String operacao, Runnable acao) {
		medir(operacao, () -> {
			acao.run();
			return null;
		});
	}

	private AtomicInteger registrarGauge(String operacao) {
		AtomicInteger concorrencia = new AtomicInteger();
		registry.gauge("financeiro.operacoes.concorrentes",
				java.util.List.of(io.micrometer.core.instrument.Tag.of("operacao", operacao)), concorrencia);
		return concorrencia;
	}

	private void registrar(String operacao, String resultado, Timer.Sample inicio) {
		registry.counter("financeiro.operacoes", "operacao", operacao, "resultado", resultado).increment();
		inicio.stop(Timer.builder("financeiro.operacoes.duracao").tag("operacao", operacao).tag("resultado", resultado)
				.register(registry));
	}
}
