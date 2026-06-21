package io.github.mido.kenning.document;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<SourceDocument>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDocument> get(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<SourceDocument> upload(@RequestParam("file") MultipartFile file) throws IOException {
        SourceDocument document = documentService.uploadDocument(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

}

