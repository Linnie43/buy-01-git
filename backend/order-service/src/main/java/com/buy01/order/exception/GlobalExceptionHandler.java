package com.buy01.order.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("RESPONSE_STATUS_ERROR", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("PAYLOAD_TOO_LARGE_ERROR", "File size exceeds the maximum limit");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);  //413
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("INTERNAL_SERVER_ERROR", "Internal server error"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("ILLEGAL_ARGUMENT_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("METHOD_NOT_ALLOWED_ERROR", "Method not allowed or endpoint does not exist");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("NO_HANDLER_FOUND_ERROR", "Endpoint does not exist");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );
        return ResponseEntity.badRequest().body(
                Map.of("METHOD_ARGUMENT_NOT_VALID_ERROR", errors.toString())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = "Bad Request";
        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            message = String.format("Invalid value '%s' for field '%s",
                    invalidFormat.getValue(),
                    invalidFormat.getPath().get(0).getFieldName());
        }
        return ResponseEntity.badRequest().body(Map.of("HTTP_MESSAGE_NOT_READABLE_ERROR", message));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(FileUploadException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("FILE_UPLOAD_ERROR", ex.getMessage());
        // 400 Bad Request is appropriate for invalid file type/size
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("NOT_FOUND_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("FORBIDDEN_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("BAD_REQUEST_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleOutOfStock(OutOfStockException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("OUT_OF_STOCK_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
