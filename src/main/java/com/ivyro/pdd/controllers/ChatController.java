package com.ivyro.pdd.controllers;

import com.ivyro.pdd.models.request.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Простой шаблон для финального ответа
    private final String finalPromptStr = """
            Ты — эксперт ГИБДД. Ответь на вопрос пользователя, используя ТОЛЬКО предоставленный ниже контекст.
            
            КОНТЕКСТ:
            {context}
            
            ВОПРОС ПОЛЬЗОВАТЕЛЯ:
            {query}
            
            Если в контексте нет ответа, так и скажи. Не выдумывай.
            Учитывай, что "автобус" = "маршрутное транспортное средство".
            """;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest chatRequest) {
        String userQuery = chatRequest.prompt();

        String improvedSearchQuery = chatClient.prompt()
                .user(u -> u.text("Сформулируй поисковый запрос для базы данных ПДД на основе вопроса пользователя. " +
                        "Используй официальные термины (например, 'маршрутное транспортное средство'). " +
                        "НЕ добавляй номера знаков (цифры), если они не указаны в явном виде в вопросе пользователя. " +
                        "Ищи по описанию действия знака (например, 'действие знака поворот запрещен').\n\n" +
                        "Вопрос: " + userQuery))
                .call()
                .content();

        System.out.println(">>> User Query: " + userQuery);
        System.out.println(">>> Search Query: " + improvedSearchQuery); // Увидишь в логах разницу!

        // 2. Идем в базу с УЛУЧШЕННЫМ запросом
        assert improvedSearchQuery != null;
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(improvedSearchQuery).topK(3)
                        .similarityThreshold(0.5).build()// Можно поднять порог, так как запрос теперь точнее
        );

        // Логируем, что нашли (для отладки)
        System.out.println(">>> Найденные документы:");
        docs.forEach(d -> System.out.println("--- " + d.getFormattedContent().substring(0, Math.min(50, d.getFormattedContent().length()))));

        if (docs.isEmpty()) {
            return ResponseEntity.ok("Извините, я не нашел информации в ПДД по вашему вопросу.");
        }

        // 3. Собираем контекст в строку
        String contextData = docs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        // 4. Формируем финальный ответ
        PromptTemplate template = new PromptTemplate(finalPromptStr);
        template.add("context", contextData);
        template.add("query", userQuery);

        String response = chatClient.prompt()
                .user(template.createMessage().getText())
                .call()
                .content();

        return ResponseEntity.ok(response);
    }
}