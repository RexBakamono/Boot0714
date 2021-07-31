package com.rex.mapper;

import com.rex.bean.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component(value = "goodsMapper")
public interface GoodsMapper {

    int add(Goods goods);

}
