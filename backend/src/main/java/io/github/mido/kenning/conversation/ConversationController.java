package io.github.mido.kenning.conversation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(conversationService.getConversationWithMessages(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping()
    public ResponseEntity<Conversation> create(@RequestParam UUID documentId) {
        return ResponseEntity.ok( conversationService.createConversation(documentId));
    }

    @PostMapping("/{id}/message")
    public ResponseEntity<Message> message(@PathVariable UUID id, @RequestParam String question) {
        return ResponseEntity.ok(this.conversationService.askQuestion(id, question));
    }

}
