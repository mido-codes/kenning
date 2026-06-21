package io.github.mido.kenning.document;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentProcessingService documentProcessingService;

    public DocumentService(DocumentRepository documentRepository, DocumentProcessingService documentProcessingService) {
        this.documentRepository = documentRepository;
        this.documentProcessingService = documentProcessingService;
    }


    public SourceDocument getDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + id));
    }

    public List<SourceDocument> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(UUID id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException("Document not found: " + id);
        }
        documentRepository.deleteById(id);
    }

    public SourceDocument uploadDocument(MultipartFile file) throws IOException {
        SourceDocument sourceDocument = SourceDocument.builder()
                .filename(file.getOriginalFilename())
                .status(DocumentStatus.PROCESSING)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();

        SourceDocument savedDocument = documentRepository.save(sourceDocument);
        documentProcessingService.processDocument(savedDocument.getId(), file.getBytes());
        return savedDocument;
    }
}
