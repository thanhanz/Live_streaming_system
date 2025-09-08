package com.thanhan.livestreaming_system.search_service.messaging;

import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import com.thanhan.livestreaming_system.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceComsumer {

    private final SearchService searchService;

    @RabbitListener(queues = "${rabbitmq.search.queue}")
    public void receiveMessage(SearchEvent event) {

        log.info("Received event: [{}]", event.action());
        switch (event.action()) {
            case "create" -> searchService.saveDocument(event);
            case "update" -> searchService.updateDocument(event);
            case "delete" -> searchService.deleteDocument(event.id(), event.type());
        }
    }
}
