package io.github.mido.kenning.config;

import io.github.mido.kenning.document.DocumentNotFoundException;
import io.github.mido.kenning.conversation.ConversationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<String> handleDocumentNotFound(DocumentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<String> handleConversationNotFound(ConversationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}