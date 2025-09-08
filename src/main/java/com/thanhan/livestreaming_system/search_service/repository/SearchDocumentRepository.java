package com.thanhan.livestreaming_system.search_service.repository;

import com.thanhan.livestreaming_system.search_service.index.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchDocumentRepository extends ElasticsearchRepository<SearchDocument, String> {

}
