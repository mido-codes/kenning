package io.github.mido.kenning.document;

import io.github.mido.kenning.user.CurrentUserService;
import io.github.mido.kenning.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {
    private final CurrentUserService currentUserService;
    private final DocumentRepository documentRepository;
    private final DocumentProcessingService documentProcessingService;

    public DocumentService(CurrentUserService currentUserService, DocumentRepository documentRepository, DocumentProcessingService documentProcessingService) {
        this.currentUserService = currentUserService;
        this.documentRepository = documentRepository;
        this.documentProcessingService = documentProcessingService;
    }


    public SourceDocument getDocument(UUID id) {
        User currentUser = currentUserService.getCurrentUser();
        return documentRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + id));
    }

    public List<SourceDocument> getAllDocuments() {
        User currentUser = currentUserService.getCurrentUser();
        return documentRepository.findByUserId(currentUser.getId());
    }

    public void deleteDocument(UUID id) {
        User currentUser = currentUserService.getCurrentUser();
        SourceDocument document = documentRepository.findByIdAndUserId(id, currentUser.getId())
                        .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + id));

        documentRepository.delete(document);
    }

    public SourceDocument uploadDocument(MultipartFile file) throws IOException {
        User currentUser = currentUserService.getCurrentUser();
        SourceDocument sourceDocument = SourceDocument.builder()
                .filename(file.getOriginalFilename())
                .user(currentUser)
                .status(DocumentStatus.PROCESSING)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();

        SourceDocument savedDocument = documentRepository.save(sourceDocument);
        documentProcessingService.processDocument(savedDocument.getId(), currentUser.getId(), file.getBytes());
        return savedDocument;
    }
}
