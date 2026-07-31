package com.financeiro.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {
	@Bean
	Clock operationalClock() {
		return Clock.systemDefaultZone();
	}
}
