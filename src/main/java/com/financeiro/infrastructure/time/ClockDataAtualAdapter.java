package com.financeiro.infrastructure.time;

import com.financeiro.application.ports.out.ObterDataAtualPort;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ClockDataAtualAdapter implements ObterDataAtualPort {
	private final Clock clock;

	public ClockDataAtualAdapter(Clock clock) {
		this.clock = clock;
	}

	@Override
	public LocalDate obter() {
		return LocalDate.now(clock);
	}

	@Override
	public LocalDateTime obterDataHora() {
		return LocalDateTime.now(clock);
	}
}
