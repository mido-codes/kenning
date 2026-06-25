package io.github.mido.kenning.conversation;

import io.github.mido.kenning.user.CurrentUserService;
import io.github.mido.kenning.user.User;
import tools.jackson.databind.ObjectMapper;
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
    private static final double SIMILARITY_THRESHOLD = 0.3; // TODO tuning with manual test

    private final ConversationRepository conversationRepository;
    private final DocumentService documentService  ;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;

    public ConversationService(ConversationRepository conversationRepository, DocumentService documentService, MessageService messageService, MessageRepository messageRepository, VectorStore vectorStore, ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, CurrentUserService currentUserService) {
        this.conversationRepository = conversationRepository;
        this.documentService = documentService;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.currentUserService = currentUserService;
    }

    public Conversation createConversation(UUID documentId){
        SourceDocument sourceDocument = this.documentService.getDocument(documentId);
        Conversation newConversation = Conversation.builder()
                .title(sourceDocument.getFilename())
                .user(currentUserService.getCurrentUser())
                .document(sourceDocument)
                .build();

        return this.conversationRepository.save(newConversation);
    }

    public void deleteConversation(UUID id) {
        User currentUser = this.currentUserService.getCurrentUser();
        Conversation conversation = conversationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + id));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        messageRepository.deleteAll(messages);
        conversationRepository.delete(conversation);
    }

    public Message askQuestion(UUID conversationId, String question) {
        Conversation conversation = this.getConversation(conversationId);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression("documentId == '" + conversation.getDocument().getId() + "' && userId == '" + conversation.getUser().getId() + "'")
                .build();

        List<Document> similarChunks = vectorStore.similaritySearch(request);

        messageService.createUserMessage(conversation, question);

        if (similarChunks.isEmpty()) {
            String fallback = "I couldn't find relevant information in this document to answer that question.";
            return messageService.createAssistantMessage(conversation, fallback, "[]");
        }

        String context = convertChunks(similarChunks);
        String answer = prompt(question, context);
        String sourcesJson = toSourcesJson(similarChunks);

        return messageService.createAssistantMessage(conversation, answer, sourcesJson);
    }

    public List<Conversation> getAllConversations() {
        User currentUser = currentUserService.getCurrentUser();
        return conversationRepository.findByUserId(currentUser.getId());
    }


    public Conversation getConversation(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        return this.conversationRepository.findByIdAndUserId(id, currentUser.getId())
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

    private String toSourcesJson(List<Document> chunks) {
        List<SourceReference> sources = chunks.stream()
                .map(chunk -> new SourceReference(chunk.getText(), chunk.getScore()))
                .toList();

        try {
            return objectMapper.writeValueAsString(sources);
        } catch (Exception e) {
            return "[]";
        }
    }
}

