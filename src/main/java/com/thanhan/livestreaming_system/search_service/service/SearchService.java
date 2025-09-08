package com.thanhan.livestreaming_system.search_service.service;

import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import com.thanhan.livestreaming_system.search_service.index.SearchDocument;

import java.util.List;

public interface SearchService {

    List<SearchDocument> searchDocuments(String key);
    void deleteDocument(String id, String type);
    void saveDocument(SearchEvent event);
    void updateDocument(SearchEvent event);

}
