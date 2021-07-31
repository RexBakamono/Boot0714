package com.rex.common.util.pay;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付
 */
@Slf4j
public class AlipayUtil {

    @Value("${alipay.appid}")
    public static String APP_ID;

    @Value("${alipay.notify-url}")
    public static String ALIPAY_NOTIFY_URL;

    @Value("${alipay.public-key}")
    public static String ALIPAY_PUBLIC_KEY;

    @Value("${alipay.private-key}")
    public static String APP_PRIVATE_KEY;


    //支付宝沙箱环境请求应答格式
    public static String format = "json";
    //支付宝沙箱环境请求编码
    public static String charset = "UTF-8";
    //支付宝沙箱环境请求编码
    public static String signType = "RSA2";

    public static String product_code = "QUICK_MSECURITY_PAY";


    //orderId订单号,orderName订单名称,total_amount金额
    public static Map<String, Object> alipay(String orderId, String orderName, String totalAmount) {

        HashMap<String, Object> resultMap = new HashMap<>();
        AlipayClient client = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, format, charset, ALIPAY_PUBLIC_KEY, signType);
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        // 封装请求支付信息
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

        model.setOutTradeNo(orderId);
        model.setSubject(orderName);
        model.setBody(orderName);
        model.setTotalAmount(totalAmount);
        //该笔订单允许的最晚付款时间
        model.setTimeoutExpress("30m");
        model.setProductCode(product_code);
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(ALIPAY_NOTIFY_URL);
        try {
            AlipayTradeAppPayResponse res = client.sdkExecute(alipay_request);
            if (res.isSuccess()) {
                resultMap.put("orderNo", orderId);
                resultMap.put("aliOrderInfo", res.getBody());
                resultMap.put("status", "ok");
                resultMap.put("message", "成功");
                log.info("支付宝成功返回returnMap为[{}]", resultMap);
                return resultMap;
            } else {
                resultMap.put("orderNo", orderId);
                resultMap.put("orderInfo", "");
                resultMap.put("status", "fail");
                resultMap.put("message", "失败");
                log.info("支付宝returnMap为[{}]", resultMap);
                return resultMap;
            }

        } catch (AlipayApiException e) {
            log.error("=请求支付宝接口出错", e);
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 交易查询
     *
     * @param out_trade_no 订单号
     */
    public static String queryAliPay(String out_trade_no) {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, format, charset, ALIPAY_PUBLIC_KEY, signType);
        AlipayTradeQueryRequest alipayQueryRequest = new AlipayTradeQueryRequest();
        String biz_content = "{\"out_trade_no\":\"" + out_trade_no + "\"}";
        alipayQueryRequest.setBizContent(biz_content);
        AlipayTradeQueryResponse alipayQueryResponse = null;
        try {
            alipayQueryResponse = alipayClient.execute(alipayQueryRequest);

            if (null != alipayQueryResponse && alipayQueryResponse.isSuccess()) {
                if (alipayQueryResponse.getCode().equals("10000")) {
                    if ("TRADE_SUCCESS".equalsIgnoreCase(alipayQueryResponse
                            .getTradeStatus())) {

                        //表示支付成功
                    } else if ("TRADE_CLOSED".equalsIgnoreCase(alipayQueryResponse
                            .getTradeStatus())) {
                        // 表示未付款关闭，或已付款的订单全额退款后关闭
                    } else if ("TRADE_FINISHED".equalsIgnoreCase(alipayQueryResponse
                            .getTradeStatus())) {
                        // 此状态，订单不可退款或撤销
                    }
                } else {
                    // 如果请求未成功，请重试

                }
            }
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return alipayQueryResponse.getBody();
    }

    /**
     * 退款接口
     *
     * @param order
     * @return
     */
    public static String refundOrder(Map order) {
        String retStr = "";
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
                APP_ID, APP_PRIVATE_KEY, "json", charset,
                ALIPAY_PUBLIC_KEY, "RSA2");

        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.refund
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建API对应的request类
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(order.get("orderId").toString());
        //model.setTradeNo(order.getRefundId());

        model.setRefundAmount(String.valueOf(order.get("fee")));//全部退款-如果是部分退款需要退款单号，可详细阅读文档


        request.setBizModel(model);
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeRefundResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
            retStr = response.getBody();//就是orderString 可以直接给项目业务做处理。
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return retStr;
    }

    public static void main(String args[]) {
        System.out.println("AlipayTradeQueryResponse=======" + queryAliPay("2021011109464323638182"));
    }
}

