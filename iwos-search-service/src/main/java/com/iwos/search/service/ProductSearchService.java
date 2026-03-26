package com.iwos.search.service;

import com.iwos.search.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final OpenSearchClient openSearchClient;
    private static final String INDEX = "products";

    public List<ProductDocument> search(String query, String categoryId, String brandId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         String sortBy, int page, int size) {
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // Multi-match across name, description, keywords
            if (query != null && !query.isBlank()) {
                boolBuilder.must(Query.of(q -> q.multiMatch(m -> m
                        .fields("name^3", "description", "searchKeywords^2", "brandName", "categoryName")
                        .query(query)
                        .fuzziness("AUTO")
                )));
            }

            // Filters
            boolBuilder.filter(Query.of(q -> q.term(t -> t.field("active").value(true))));
            if (categoryId != null) boolBuilder.filter(Query.of(q -> q.term(t -> t.field("categoryId").value(categoryId))));
            if (brandId != null) boolBuilder.filter(Query.of(q -> q.term(t -> t.field("brandId").value(brandId))));
            if (minPrice != null) boolBuilder.filter(Query.of(q -> q.range(r -> r.number(n -> n.field("sellingPrice").gte(minPrice.doubleValue())))));
            if (maxPrice != null) boolBuilder.filter(Query.of(q -> q.range(r -> r.number(n -> n.field("sellingPrice").lte(maxPrice.doubleValue())))));

            SearchRequest.Builder reqBuilder = new SearchRequest.Builder()
                    .index(INDEX)
                    .query(Query.of(q -> q.bool(boolBuilder.build())))
                    .from(page * size)
                    .size(size);

            // Sorting
            if ("price_asc".equals(sortBy)) reqBuilder.sort(s -> s.field(f -> f.field("sellingPrice").order(SortOrder.Asc)));
            else if ("price_desc".equals(sortBy)) reqBuilder.sort(s -> s.field(f -> f.field("sellingPrice").order(SortOrder.Desc)));
            else if ("rating".equals(sortBy)) reqBuilder.sort(s -> s.field(f -> f.field("avgRating").order(SortOrder.Desc)));
            else if ("newest".equals(sortBy)) reqBuilder.sort(s -> s.field(f -> f.field("_id").order(SortOrder.Desc)));

            SearchResponse<ProductDocument> response = openSearchClient.search(reqBuilder.build(), ProductDocument.class);
            return response.hits().hits().stream().map(Hit::source).toList();
        } catch (IOException e) {
            log.error("Search failed for query: {}", query, e);
            throw new RuntimeException("Search service unavailable", e);
        }
    }
}
