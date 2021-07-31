package com.rex.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rex.bean.Goods;
import com.rex.common.code.ApiMessage;
import com.rex.service.GoodsService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 添加商品
     *
     * @param params
     * @return
     */
    @PutMapping("/add")
    public ApiMessage add(@RequestBody String params) {
        Goods goods = JSON.toJavaObject(JSONObject.parseObject(params), Goods.class);
        return new ApiMessage<>(goodsService.add(goods));
    }


}