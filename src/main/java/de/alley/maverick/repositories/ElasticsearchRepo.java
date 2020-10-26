package de.alley.maverick.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.alley.maverick.protocol.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ElasticsearchRepo {

    private static final String INDEX_NAME = "files";
    private static final String TAGS_FIELD_NAME = "tags";
    private static final int START_SEARCH_RECORDS = 0;
    private static final int MAX_SEARCH_RECORDS = 10000;

    private final RestHighLevelClient esClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public ElasticsearchRepo(RestHighLevelClient esClient, ObjectMapper objectMapper) {
        this.esClient = esClient;
        this.objectMapper = objectMapper;
    }

    private IndexRequest prepareIndexRequest(FileInfo fileInfo, boolean wait) {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        Map<String, Object> mappedFileInfo = objectMapper.convertValue(fileInfo, Map.class);
        request.id(fileInfo.getId());
        request.timeout(TimeValue.timeValueSeconds(1));
        request.opType(DocWriteRequest.OpType.CREATE);  //put if absent
        if (wait) {
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        }
        request.source(mappedFileInfo);
        return request;
    }

    public void createElasticSearchRecordsIfNotExists(List<FileInfo> fileInfos) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            fileInfos.forEach(fileInfo -> {
                IndexRequest request = prepareIndexRequest(fileInfo, false);
                bulkRequest.add(request);
            });
            esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            log.warn(String.format("Cannot bulk the files %s in elasticsearch", fileInfos));
            throw new IllegalStateException("Critical error in elasticsearch", ex);
        }
    }

    public void createElasticsearchRecordIfNotExists(FileInfo fileInfo) {
        try {
            IndexRequest request = prepareIndexRequest(fileInfo, true);
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException ex) {
            log.warn(String.format("Cannot save the file %s in elasticsearch", fileInfo));
            throw new IllegalStateException("Critical error in elasticsearch", ex);
        }
    }

    public Optional<FileInfo> findElasticsearchRecordById(String fileId) {
        try {
            GetSourceRequest getSourceRequest = new GetSourceRequest(INDEX_NAME, fileId);
            GetSourceResponse response = esClient.getSource(getSourceRequest, RequestOptions.DEFAULT);
            return Optional.of(objectMapper.convertValue(response.getSource(), FileInfo.class));
        } catch (ElasticsearchStatusException ex) {
            log.warn(String.format("Cannot find the file %s in elasticsearch, %s", fileId, ex.getMessage()));
            return Optional.empty();
        } catch (IOException ex) {
            log.warn(String.format("Something wrong with the file %s in elasticsearch", fileId));
            throw new IllegalStateException("Critical error in elasticsearch", ex);
        }
    }

    public Optional<FileInfo> updateElasticsearchRecord(FileInfo existingFileInfo, List<String> parameterTags, Mode mode) {
        try {
            List<String> newTags;
            switch (mode) {
                case MERGE: {
                    Set<String> uniqueTags = new LinkedHashSet<>();
                    uniqueTags.addAll(existingFileInfo.getTags());
                    uniqueTags.addAll(parameterTags);
                    newTags = new ArrayList<>(uniqueTags);
                    break;
                }
                case RESET: {
                    newTags = parameterTags;
                    break;
                }
                case DELETE: {
                    newTags = existingFileInfo.getTags().stream()
                            .filter(t -> !parameterTags.contains(t)).collect(Collectors.toList());
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown mode for updating elasticsearch.");
            }
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put(TAGS_FIELD_NAME, newTags);
            UpdateRequest request = new UpdateRequest(INDEX_NAME, existingFileInfo.getId()).doc(requestMap);
            request.retryOnConflict(3);
            request.timeout(TimeValue.timeValueSeconds(1));
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            UpdateResponse response = esClient.update(request, RequestOptions.DEFAULT);
            log.debug(String.format("response after elasticsearch update %s", response.toString()));
            return findElasticsearchRecordById(existingFileInfo.getId());
        } catch (ElasticsearchStatusException | IOException ex) {
            log.warn(String.format("Cannot update the file %s in elasticsearch", existingFileInfo.getId()));
            throw new IllegalStateException("Critical error in elasticsearch", ex);
        }
    }

    public List<FileInfo> findByPartName(String fileName) {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder scb = new SearchSourceBuilder();
            scb.from(START_SEARCH_RECORDS);
            scb.size(MAX_SEARCH_RECORDS);
            scb.timeout(new TimeValue(5, TimeUnit.SECONDS));
            WildcardQueryBuilder mcb = QueryBuilders.wildcardQuery("name", "*" + fileName + "*");
            scb.query(mcb);
            request.source(scb);
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            SearchHit[] searchHits = hits.getHits();
            return Arrays.stream(searchHits)
                    .filter(Objects::nonNull)
                    .map(e -> objectMapper.convertValue(e.getSourceAsMap(), FileInfo.class))
                    .sorted(Comparator.comparing(FileInfo::getName))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            log.warn(String.format("Cannot find the files %s in elasticsearch", fileName));
            throw new IllegalStateException("Critical error in elasticsearch", ex);
        }
    }
}
