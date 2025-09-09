package com.thanhan.livestreaming_system.search_service.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import com.thanhan.livestreaming_system.search_service.index.SearchDocument;
import com.thanhan.livestreaming_system.search_service.repository.SearchDocumentRepository;
import com.thanhan.livestreaming_system.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public List<SearchDocument> searchDocuments(String key) {
        NativeQuery query = new NativeQueryBuilder()
                .withQuery(q ->

                    q.multiMatch(m -> m.query(key).fields("title^3", "description", "channelName")
                            .fuzziness("AUTO") //fuzzy search
                            .type(TextQueryType.BestFields)
                            .prefixLength(1)) //Khớp với ký tự đầu.
                )
                .withMaxResults(20)
                .build();

        SearchHits<SearchDocument> hits = elasticsearchTemplate.search(query, SearchDocument.class);

        log.info("[ElasticSearch] get hits: " + hits.getTotalHits());

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public void saveDocument(SearchEvent event) {
        SearchDocument document = mapToDocument(event);
        searchDocumentRepository.save(document);

        log.info("[ElasticSearch] create new document: " + document.getId());
    }

    @Override
    public void updateDocument(SearchEvent event) {
        SearchDocument document = mapToDocument(event);
        //Synchronization Database vs ES (Video, Livestream "channelName") when update type = "channel"

        searchDocumentRepository.save(document);
    }


    @Override
    public void deleteDocument(String id, String type) {
        String doc_id = type + "_" + id;
        SearchDocument doc = searchDocumentRepository.findById(doc_id).orElseThrow(() -> new RuntimeException("Document not found in elastic search"));

        //Delete
        log.info("[ElasticSearch] delete document: " + doc.getId());
        searchDocumentRepository.delete(doc);
    }

    private SearchDocument mapToDocument(SearchEvent event) {
        SearchDocument doc = new SearchDocument();

        doc.setId(event.type() + "_" + event.id());
        doc.setType(event.type());

        Map<String, Object> payload = event.payload();
        doc.setEntityId(event.id());
        doc.setTitle((String) payload.get("title"));
        doc.setDescription((String) payload.get("description"));
        doc.setThumbnailUrl((String) payload.get("thumbnailUrl"));
        doc.setIsOnlyMember((Boolean) payload.getOrDefault("isOnlyMember", false));
        doc.setViewCount(payload.get("viewCount") != null ? ((Number) payload.get("viewCount")).longValue() : 0L);
        doc.setCreatedAt((String) payload.get("createdAt"));

        doc.setChannelId((String) payload.get("channelId"));
        doc.setChannelName((String) payload.get("channelName"));
        doc.setChannelAvatar((String) payload.get("channelAvatar"));
        doc.setChannelFollowers(payload.get("channelFollowers") != null ? ((Number) payload.get("channelFollowers")).longValue() : 0L);
        doc.setAvatarUrl((String) payload.get("avatarUrl"));

        return doc;
    }

}
