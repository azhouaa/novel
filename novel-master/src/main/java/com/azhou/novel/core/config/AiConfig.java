package com.azhou.novel.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * AI related configuration.
 */
@Configuration
@Slf4j
public class AiConfig {

    /**
     * Customize RestClient timeout used by Spring AI.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(60000);
        return RestClient.builder().requestFactory(factory);
    }

    /**
     * Build ChatClient.Builder explicitly so we can control model selection
     * when multiple model starters are enabled.
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(List<ChatModel> chatModels) {
        if (chatModels == null || chatModels.isEmpty()) {
            throw new IllegalStateException("No ChatModel bean found, please check Spring AI config.");
        }

        ChatModel preferredModel = chatModels.stream()
            .sorted((left, right) -> Integer.compare(modelPriority(left), modelPriority(right)))
            .findFirst()
            .orElse(chatModels.get(0));

        log.info("Using ChatModel: {}", preferredModel.getClass().getName());
        return ChatClient.builder(preferredModel);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    private int modelPriority(ChatModel model) {
        String modelClassName = model.getClass().getName().toLowerCase();
        if (modelClassName.contains("deepseek")) {
            return 0;
        }
        if (modelClassName.contains("openai")) {
            return 1;
        }
        return 10;
    }
}