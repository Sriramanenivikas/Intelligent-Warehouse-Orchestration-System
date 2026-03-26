package com.iwos.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteService {

    private final OpenSearchClient client;

    public List<String> suggest(String prefix, int limit) {
        try {
            SearchRequest req = new SearchRequest.Builder()
                    .index("products")
                    .query(q -> q.matchPhrasePrefix(m -> m.field("name").query(prefix)))
                    .size(limit)
                    .source(s -> s.filter(f -> f.includes("name")))
                    .build();
            SearchResponse<Map> response = client.search(req, Map.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(m -> (String) m.get("name"))
                    .distinct().toList();
        } catch (IOException e) {
            log.error("Autocomplete failed for prefix: {}", prefix, e);
            return List.of();
        }
    }
}
