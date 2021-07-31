package com.rex.common.util.elastic;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ElasticUtil {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     *
     * @param index
     * @return
     * @throws IOException
     */
    public CreateIndexResponse createIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        return restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 是否存在索引
     *
     * @param index
     * @return
     * @throws IOException
     */
    public boolean getIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引
     *
     * @param index
     * @return
     * @throws IOException
     */
    public boolean deleteIndex(String index) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    /**
     * 插入数据
     *
     * @param index 索引名称
     * @param data  json数据
     * @return
     * @throws IOException
     */
    public IndexResponse addDocument(String index, String data) throws IOException {
        IndexRequest request = new IndexRequest(index);
        request.id(UUID.randomUUID().toString());
        request.type("doc");
        request.source(data, XContentType.JSON);
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 更新数据
     *
     * @param index
     * @param data
     * @return
     * @throws IOException
     */
    public UpdateResponse updateDocument(String index, String data) throws IOException {
        UpdateRequest request = new UpdateRequest();
        request.index(index);
        request.type("doc");
        request.doc(data, XContentType.JSON);
        return restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 是否存在数据
     *
     * @param index
     * @param id
     * @return
     * @throws IOException
     */
    public boolean isExsit(String index, String id) throws IOException {
        GetRequest request = new GetRequest(index);
        request.type("doc");
        request.id(id);
        return restHighLevelClient.exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 获取数据
     *
     * @param index
     * @param id
     * @return
     * @throws IOException
     */
    public GetResponse getDocument(String index, String id) throws IOException {
        GetRequest request = new GetRequest(index);
        request.type("doc");
        request.id(id);
        return restHighLevelClient.get(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除数据
     *
     * @param index
     * @param id
     * @return
     * @throws IOException
     */
    public DeleteResponse deleteDocument(String index, String id) throws IOException {
        DeleteRequest request = new DeleteRequest();
        request.index(index);
        request.type("doc");
        request.id(id);
        return restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量插入数据
     *
     * @param index
     * @param list
     * @return
     * @throws IOException
     */
    public BulkResponse insertBulkDocument(String index, List list) throws IOException {
        BulkRequest request = new BulkRequest();
        for (Object o : list) {
            request.add(new IndexRequest().index(index)
                    .type("doc")
                    .source(JSON.toJSONString(o), XContentType.JSON)
                    .id(UUID.randomUUID().toString()));
        }
        return restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 搜索
     *
     * @param index      索引名称
     * @param sField     搜索字段名称
     * @param value      搜索字段值
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List search(String index, String sField, String value, int pageNumber, int pageSize, String color) throws IOException {
        int startNo = (pageNumber - 1) * pageSize;
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 名称查询
        if (StringUtils.isNotEmpty(sField)) {
            MatchQueryBuilder termQueryBuilder = QueryBuilders.matchQuery(sField, value);
//            termQueryBuilder.analyzer("ik_max_word");
            builder.query(termQueryBuilder);
            builder.from(startNo);
            builder.size(pageSize);
            // 排序
//        builder.sort(sField, SortOrder.DESC);
            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(sField);
            highlightBuilder.requireFieldMatch(false);
            highlightBuilder.preTags("<span style='color:" + color + ";'>");
            highlightBuilder.postTags("</span>");
            builder.highlighter(highlightBuilder);
        }
        request.source(builder);
        // 获取返回数据
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, HighlightField> map = hit.getHighlightFields();
            HighlightField name = map.get(sField);
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (name != null) {
                Text[] fragments = name.fragments();
                StringBuilder newName = new StringBuilder();
                for (Text text : fragments) {
                    newName.append(text);
                }
                // 减少返回数据
                sourceAsMap.put(sField, newName.toString());
            }
            String id = hit.getId();
            sourceAsMap.put("_id", id);
            list.add(sourceAsMap);
        }
        return list;
    }

    /**
     * 多条件查询
     * must=and  should=or must_not/mustNot=not
     *
     * @param index      索引
     * @param field      查询字段
     * @param value      字段值
     * @param attrValue  attrs.attrValue值
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List boolSearch(String index, String field, String value, String attrValue, int pageNumber, int pageSize, String color) throws IOException {
        int startNo = (pageNumber - 1) * pageSize;
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 多条件查询
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder term = QueryBuilders.matchQuery(field, value);
        NestedQueryBuilder nested = QueryBuilders.nestedQuery("attrs", QueryBuilders.matchQuery("attrs.attrValue", attrValue), ScoreMode.None);
        boolBuilder.must(term).must(nested);
        builder.query(boolBuilder);
        builder.from(startNo);
        builder.size(pageSize);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(field);
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:" + color + ";'>");
        highlightBuilder.postTags("</span>");
        builder.highlighter(highlightBuilder);
        request.source(builder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, HighlightField> map = hit.getHighlightFields();
            HighlightField name = map.get(field);
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (name != null) {
                Text[] fragments = name.fragments();
                StringBuilder newName = new StringBuilder();
                for (Text text : fragments) {
                    newName.append(text);
                }
                sourceAsMap.put(field, newName.toString());
            }
            String id = hit.getId();
            sourceAsMap.put("_id", id);
            list.add(sourceAsMap);
        }
        return list;
    }
}