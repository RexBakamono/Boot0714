package com.rex.service.impl;

import com.alibaba.fastjson.JSON;
import com.rex.bean.Goods;
import com.rex.common.util.elastic.ElasticUtil;
import com.rex.mapper.GoodsMapper;
import com.rex.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;


@Service("goodsService")
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private ElasticUtil util;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(Goods goods) {
        goodsMapper.add(goods);
        log.info(goods.toString());
        try {
            util.addDocument("goods", JSON.toJSONString(goods));
            return true;
        }catch (Exception e) {
            log.error(e.getMessage());
            // 手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return false;
    }
}
