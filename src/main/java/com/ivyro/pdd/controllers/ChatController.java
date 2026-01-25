package com.ivyro.pdd.controllers;

import com.ivyro.pdd.models.request.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder,
                          PgVectorStore pgVectorStore){
        this.chatClient = builder
                    .defaultAdvisors(QuestionAnswerAdvisor.builder(pgVectorStore)
                    .searchRequest(SearchRequest.builder().topK(2).build()).build())
                    .build();
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest chatRequest){
        return ResponseEntity.ok(chatClient.prompt()
                .user(chatRequest.prompt())
                .call()
                .content());
    }
}
