package com.rex.common.util.elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuEsModel {
    // 商品ID
    private Long spuId;
    // sku_id
    private Long skuId;
    // 标题
    private String skuTitle;
    // 价格
    private BigDecimal skuPrice;
    // 图片
    private String skuImg;
    // 销售量
    private Long saleCount;
    // 是否还有库存
    private Boolean hasStock;
    // 热度评分
    private Long hotScore;
    // 品牌ID
    private Long brandId;
    // 品牌名
    private String brandName;
    // 品牌图片
    private String brandImg;
    // 分类ID
    private Long catalogId;
    // 分类名
    private String catalogName;
    // 属性
    private List<Attrs> attrs;

    // 内部类
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attrs {
        // 属性ID
        private Long attrId;
        // 属性名
        private String attrName;
        // 属性值
        private String attrValue;
    }
}