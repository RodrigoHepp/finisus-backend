package com.finisus.infrastructure.config;

import java.net.URI;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.finisus.domain.ConflitoAtualizacaoException;
import com.finisus.domain.DomainException;
import com.finisus.domain.AcessoNegadoException;
import com.finisus.domain.CredenciaisInvalidasException;
import com.finisus.domain.RecursoNaoEncontradoException;
import com.finisus.domain.UsuarioBloqueadoException;

import jakarta.persistence.LockTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {
	private final MessageSource messages;

	public ApiExceptionHandler(MessageSource messages) {
		this.messages = messages;
	}

	@ExceptionHandler(DomainException.class)
	ProblemDetail handleDomain(DomainException exception, HttpServletRequest request) {
		String detail = messages.getMessage(exception.getMessageKey(), exception.getArgs(), exception.getMessageKey(),
				LocaleContextHolder.getLocale());

		return problem(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessageKey(), detail, request);
	}

	@ExceptionHandler(AcessoNegadoException.class)
	ProblemDetail handleForbidden(AcessoNegadoException exception, HttpServletRequest request) {
		String detail = messages.getMessage(exception.getMessageKey(), exception.getArgs(), exception.getMessageKey(),
				LocaleContextHolder.getLocale());
		return problem(HttpStatus.FORBIDDEN, exception.getMessageKey(), detail, request);
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	ProblemDetail handleInvalidCredentials(CredenciaisInvalidasException exception, HttpServletRequest request) {
		return localized(HttpStatus.UNAUTHORIZED, exception, request);
	}

	@ExceptionHandler(UsuarioBloqueadoException.class)
	ProblemDetail handleLocked(UsuarioBloqueadoException exception, HttpServletRequest request) {
		return localized(HttpStatus.LOCKED, exception, request);
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	ProblemDetail handleNotFound(RecursoNaoEncontradoException exception, HttpServletRequest request) {
		return localized(HttpStatus.NOT_FOUND, exception, request);
	}

	private ProblemDetail localized(HttpStatus status, DomainException exception, HttpServletRequest request) {
		String detail = messages.getMessage(exception.getMessageKey(), exception.getArgs(), exception.getMessageKey(),
				LocaleContextHolder.getLocale());
		return problem(status, exception.getMessageKey(), detail, request);
	}

	@ExceptionHandler(ConflitoAtualizacaoException.class)
	ProblemDetail handleConflict(ConflitoAtualizacaoException exception, HttpServletRequest request) {
		String detail = messages.getMessage(exception.getMessageKey(), exception.getArgs(), exception.getMessageKey(),
				LocaleContextHolder.getLocale());
		return problem(HttpStatus.CONFLICT, exception.getMessageKey(), detail, request);
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
		MethodArgumentTypeMismatchException.class })
	ProblemDetail handleValidation(Exception exception, HttpServletRequest request) {
		String detail = switch (exception) {
		case MethodArgumentNotValidException invalid -> invalid.getBindingResult().getFieldErrors().stream().findFirst()
		.map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("Requisição inválida");
		case ConstraintViolationException invalid -> invalid.getConstraintViolations().stream().findFirst()
		.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
		.orElse("Parâmetro inválido");
		case MethodArgumentTypeMismatchException invalid -> invalid.getName() + ": valor inválido";
		default -> "Requisição inválida";
		};
		return problem(HttpStatus.BAD_REQUEST, "error.validacao", detail, request);
	}

	@ExceptionHandler({ OptimisticLockingFailureException.class, CannotAcquireLockException.class,
		LockTimeoutException.class })
	ProblemDetail handleConcurrency(Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "error.conflito.atualizacao",
				"O recurso foi alterado concorrentemente. Tente novamente.", request);
	}

	private ProblemDetail problem(HttpStatus status, String code, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("urn:finisus:problem:" + code));
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("code", code);
		return problem;
	}
}
