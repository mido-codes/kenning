package io.github.mido.kenning.conversation;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message createUserMessage(Conversation conversation, String question) {
        Message userMessage = Message.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(question)
                .build();
        return messageRepository.save(userMessage);
    }

    public Message createAssistantMessage(Conversation conversation, String answer, String context) {
        Message assistantMessage = Message.builder()
                .conversation(conversation)
                .role(MessageRole.ASSISTANT)
                .content(answer)
                .sources(context) // TODO use JSON-Array instead of raw values
                .build();
        return messageRepository.save(assistantMessage);
    }
}
