package com.thanhan.livestreaming_system.search_service.controller;


import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.search_service.index.SearchDocument;
import com.thanhan.livestreaming_system.search_service.service.impl.SearchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchServiceImpl searchService;

    @GetMapping
    public ApiResponse<List<SearchDocument>> search(@RequestParam String query) {
        return ApiResponse.<List<SearchDocument>>builder()
                .data(searchService.searchDocuments(query))
                .status(201)
                .message("Success get data from elastic search")
                .build();
    }
}
