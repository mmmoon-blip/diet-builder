package com.dietbutler.dto;

import lombok.Data;
import java.util.List;

@Data
public class LlmRequest {
    private String model;
    private List<Message> messages;
    private double temperature = 0.7;
    private List<Tool> tools;

    @Data
    public static class Message {
        private String role;
        private String content;
        private String name;
        private List<ToolCall> tool_calls;
        @com.fasterxml.jackson.annotation.JsonProperty("tool_call_id")
        private String toolCallId;

        public static Message of(String role, String content) {
            Message msg = new Message();
            msg.setRole(role);
            msg.setContent(content);
            return msg;
        }
    }

    @Data
    public static class Tool {
        private String type = "function";
        private Function function;

        @Data
        public static class Function {
            private String name;
            private String description;
            private String parameters;
        }
    }

    @Data
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;

        @Data
        public static class Function {
            private String name;
            private String arguments;
        }
    }
}