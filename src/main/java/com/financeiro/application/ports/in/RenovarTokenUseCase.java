package com.financeiro.application.ports.in;

import java.time.Instant;

public interface RenovarTokenUseCase {

    Result executar(Command command);

    record Command(String refreshToken) {}

    record Result(String accessToken, String refreshToken, Instant accessTokenExpiraEm) {}
}
