package io.github.mido.kenning.conversation;

import java.util.List;

public record ConversationDetailResponse(
        Conversation conversation,
        List<Message> messages) {}
