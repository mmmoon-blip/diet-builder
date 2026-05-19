package com.dietbutler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Simple chat with system prompt and user message
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();

            return response;
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return null;
        }
    }

    /**
     * Chat with previous conversation messages for context
     */
    public String chatWithHistory(String systemPrompt, String userMessage, java.util.List<ChatMessage> history) {
        try {
            var promptBuilder = chatClient.prompt()
                    .system(systemPrompt);

            // Add conversation history
            if (history != null && !history.isEmpty()) {
                for (ChatMessage msg : history) {
                    if ("user".equals(msg.getRole())) {
                        promptBuilder.user(msg.getContent());
                    } else if ("assistant".equals(msg.getRole())) {
                        promptBuilder.user(msg.getContent());
                    }
                }
            }

            promptBuilder.user(userMessage);

            String response = promptBuilder.call().content();
            return response;
        } catch (Exception e) {
            log.error("AI chat with history failed", e);
            return null;
        }
    }

    /**
     * Simple POJO for chat history
     */
    @lombok.Data
    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {}

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
