package io.github.mido.kenning.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ai.reader.tika.TikaDocumentReader;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingService {
    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    public DocumentProcessingService(VectorStore vectorStore, DocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    @Async
    public void processDocument(UUID documentId, byte[] fileContent) {
        try {
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.read();

            TokenTextSplitter splitter = TokenTextSplitter.builder().build();
            List<Document> chunks = splitter.apply(documents);

            for (Document chunk : chunks) {
                chunk.getMetadata().put("documentId", documentId.toString());
            }

            vectorStore.add(chunks);

            SourceDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalStateException("Document " + documentId + " missing after save"));
            document.setStatus(DocumentStatus.READY);
            documentRepository.save(document);

        } catch (Exception e) {
            documentRepository.findById(documentId).ifPresent(document -> {
                document.setStatus(DocumentStatus.FAILED);
                document.setErrorMessage(e.getMessage());
                documentRepository.save(document);
            });
        }
    }
}
