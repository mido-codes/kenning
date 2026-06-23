package io.github.mido.kenning.conversation;

import io.github.mido.kenning.document.DocumentService;
import io.github.mido.kenning.document.SourceDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final DocumentService documentService  ;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ConversationService(ConversationRepository conversationRepository, DocumentService documentService, MessageService messageService, MessageRepository messageRepository, VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.conversationRepository = conversationRepository;
        this.documentService = documentService;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public Conversation createConversation(UUID documentId){
        SourceDocument sourceDocument = this.documentService.getDocument(documentId);
        Conversation newConversation = Conversation.builder()
                .title(sourceDocument.getFilename())
                .document(sourceDocument)
                .build();

        return this.conversationRepository.save(newConversation);
    }

    public void deleteConversation(UUID id) {
        if (!conversationRepository.existsById(id)) {
            throw new ConversationNotFoundException("Conversation not found: " + id);
        }
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        messageRepository.deleteAll(messages);
        conversationRepository.deleteById(id);
    }

    public Message askQuestion(UUID conversationId, String question) {
        Conversation conversation = this.getConversation(conversationId);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .filterExpression("documentId == '" + conversation.getDocument().getId()  + "'")
                .build();

        List<Document> similarChunks = vectorStore.similaritySearch(request);
        String context = convertChunks(similarChunks);

        String answer = prompt(question, context);

        messageService.createUserMessage(conversation, question);
        return messageService.createAssistantMessage(conversation, answer, context);
    }

    public Conversation getConversation(UUID id) {
       return this.conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + id));
    }

    public ConversationDetailResponse getConversationWithMessages(UUID id) {
        Conversation conversation = getConversation(id);
        List<Message> messageList = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);

        return new ConversationDetailResponse(conversation, messageList);
    }


    private String prompt(String question, String context) {
        return this.chatClient.prompt()
                .system("Answer ONLY using the provided context. If the answer isn't in the context, say you don't know.")
                .user(question + "\n\nContext:\n" + context)
                .call()
                .content();
    }

    private String convertChunks(List<Document> chunks) {
        return chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }
}

