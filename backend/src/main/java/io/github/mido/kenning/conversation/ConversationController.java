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
    public ResponseEntity<Conversation> get(@PathVariable UUID id) {
        return ResponseEntity.ok(conversationService.getConversation(id));
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
