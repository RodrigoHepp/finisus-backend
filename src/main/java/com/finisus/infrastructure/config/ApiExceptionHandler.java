package com.finisus.infrastructure.config;

import com.finisus.domain.DomainException;
import com.finisus.domain.ConflitoAtualizacaoException;
import jakarta.persistence.LockTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

import java.net.URI;

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
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, detail, request);
	}

	@ExceptionHandler(ConflitoAtualizacaoException.class)
	ProblemDetail handleConflict(ConflitoAtualizacaoException exception, HttpServletRequest request) {
		String detail = messages.getMessage(exception.getMessageKey(), exception.getArgs(), exception.getMessageKey(),
				LocaleContextHolder.getLocale());
		return problem(HttpStatus.CONFLICT, detail, request);
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
		return problem(HttpStatus.BAD_REQUEST, detail, request);
	}

	@ExceptionHandler({ OptimisticLockingFailureException.class, CannotAcquireLockException.class,
			LockTimeoutException.class })
	ProblemDetail handleConcurrency(Exception exception, HttpServletRequest request) {
		return problem(HttpStatus.CONFLICT, "O recurso foi alterado concorrentemente. Tente novamente.", request);
	}

	private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}
