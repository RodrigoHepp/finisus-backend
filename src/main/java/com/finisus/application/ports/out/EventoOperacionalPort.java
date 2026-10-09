package com.finisus.application.ports.out;

public interface EventoOperacionalPort {
	EventoOperacionalPort NENHUM = evento -> { };

	void registrar(String evento);
}
