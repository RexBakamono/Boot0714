package com.rex.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rex.common.util.elastic.ElasticUtil;
import com.rex.common.util.elastic.SkuEsModel;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "es搜索")
@Slf4j
@RestController
@RequestMapping("/elastic")
public class ElasticController {

    @Resource
    private ElasticUtil util;

    @PostMapping("/add")
    public RestStatus add(@RequestBody String params) {
        try {
            JSONObject obj = JSON.parseObject(params);
            String index = obj.getString("index");
            String data = obj.getString("data");
//            SkuEsModel sku = new SkuEsModel();
//            sku.setSpuId(2L);
//            sku.setSkuId(201L);
//            sku.setSkuTitle("小熊饼干");
//            sku.setSkuPrice(new BigDecimal(19.99));
//            sku.setSkuImg("https://img.cloud/images/ddfdfdg.png");
//            sku.setSaleCount(200L);
//            sku.setHasStock(true);
//            sku.setHotScore(56656L);
//            sku.setBrandId(21L);
//            sku.setBrandName("小熊");
//            sku.setBrandImg("https://img.cloud/images/xiaoxiong.png");
//            sku.setCatalogId(32L);
//            sku.setCatalogName("饼干");
//            List<SkuEsModel.Attrs> list = new ArrayList<>();
//            SkuEsModel.Attrs attr1 = new SkuEsModel.Attrs();
//            attr1.setAttrId(212L);
//            attr1.setAttrName("包装");
//            attr1.setAttrValue("500g");
//            list.add(attr1);
//            SkuEsModel.Attrs attr2 = new SkuEsModel.Attrs();
//            attr2.setAttrId(213L);
//            attr2.setAttrName("包装");
//            attr2.setAttrValue("1000g");
//            list.add(attr2);
//            sku.setAttrs(list);
            IndexResponse response = util.addDocument(index, data);
            return response.status();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/search")
    public List search(@RequestParam String index, @RequestParam String field, @RequestParam String value, @RequestParam String color) {
        try {
            return util.search(index, field, value, 1, 20, color);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/nestedSearch")
    public List nestedSearch(@RequestParam String value, @RequestParam String attr, @RequestParam String field, @RequestParam String color) {
        try {
            log.info(value + attr + field);
            return util.boolSearch("product", field, value, attr, 1, 20, color);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam String index, @RequestParam String value) {
        try {
            GetResponse response = util.getDocument(index, value);
            return response.getSource();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @DeleteMapping("/delete")
    public RestStatus delete(@RequestParam String index, @RequestParam String id) {
        try {
            DeleteResponse response = util.deleteDocument(index, id);
            return response.status();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}