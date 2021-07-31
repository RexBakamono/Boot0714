package com.rex.common.util.pay;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.*;

/**
 * 微信支付
 */
@Slf4j
public class WechatPayUtil {

    @Value("${wechat-pay.tradeType}")
    public static String trade_type;

    @Value("${wechat-pay.wechat-notify-url}")
    public static String notify_url;

    @Value("${wechat-pay.app-id}")
    public static String appId;

    @Value("${wechat-pay.mch-id}")
    public static String mchId;

    @Value("${wechat-pay.key}")
    public static String key;

    public static Map<String, Object> getWechatOrder(String out_trade_no, String body, String totalFee, HttpServletRequest request, Integer type) {
        Map<String, Object> resultObj = new HashMap<>();  //存储返回参数
        //默认失败
        resultObj.put("status", "fail");
        resultObj.put("message", "失败");
        resultObj.put("order_id", out_trade_no);
        resultObj.put("orderInfo", "");

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        // 组合请求参数
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("appid", appId);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body", body);
        paramMap.put("mch_id", mchId);
        if (1 == type) {
            paramMap.put("notify_url", notify_url);
        }

        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("spbill_create_ip", request.getRemoteAddr());
        paramMap.put("total_fee", totalFee);
        paramMap.put("trade_type", trade_type);
        paramMap.put("attach", "app");
        try {
            //MAP转换为XML字符串（自动添加签名）
            String reqBody = WXPayUtil.generateSignedXml(paramMap, key, WXPayConstants.SignType.MD5);
            String rslt = doPost(reqBody);
            rslt = rslt.replaceAll("\n", "").replace("\r", "").replace("\n\r", "").replaceAll("\t", "");
            Element root = DocumentHelper.parseText(rslt).getRootElement();
            log.info("===微信支付 请求参数：" + reqBody + ";返回结果" + rslt);
            if ("SUCCESS".equals(root.elementText("return_code"))) {
                if ("SUCCESS".equals(root.elementText("result_code"))) {

                    SortedMap<Object, Object> parameterMap = new TreeMap<Object, Object>();
                    parameterMap.put("appid", root.elementText("appid"));
                    parameterMap.put("noncestr", root.elementText("nonce_str"));
                    parameterMap.put("package", "Sign=WXPay");
                    parameterMap.put("partnerid", root.elementText("mch_id"));
                    parameterMap.put("prepayid", root.elementText("prepay_id"));
                    // 本来生成的时间戳是13位，但是ios必须是10位，所以截取了一下
                    parameterMap.put("timestamp", timestamp);
                    String sign = secondSign(root.elementText("nonce_str"), timestamp, root.elementText("prepay_id"));
                    parameterMap.put("sign", sign);
                    resultObj.put("status", "ok");
                    resultObj.put("orderNo", out_trade_no);
                    resultObj.put("message", "成功");
                    resultObj.put("weiOrderInfo", parameterMap);
                }
            } else {
                log.error("===微信下单异常");
                resultObj.put("status", "fail");
                resultObj.put("message", root.elementText("return_msg"));
                resultObj.put("orderNo", out_trade_no);
                resultObj.put("weiOrderInfo", "");
                return resultObj;
            }
        } catch (Exception e) {
            log.error("===微信下单异常", e);
            resultObj.put("status", "fail");
            resultObj.put("message", "失败");
            resultObj.put("orderNo", out_trade_no);
            resultObj.put("weiOrderInfo", "");
            return resultObj;
        }
        log.info("===微信支付返回：=" + resultObj);
        return resultObj;
    }


    public static String secondSign(String nonce_str, String time, String prepayid) {
        String sign = "";
        try {
            Map<String, String> map = new HashMap<>();
            map.put("appid", appId);
            map.put("noncestr", nonce_str);
            map.put("package", "Sign=WXPay");
            map.put("partnerid", mchId);
            map.put("prepayid", prepayid);
            map.put("timestamp", time);
            String xmlStr = WXPayUtil.generateSignedXml(map, key);

            Map<String, String> xmlMap = WXPayUtil.xmlToMap(xmlStr);
            sign = xmlMap.get("sign");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * 微信支付Post
     *
     * @param reqBody
     * @return
     */
    public static String doPost(String reqBody) {
        String resp = "";
        try {
            String UTF8 = "UTF-8";
            URL httpUrl = new URL("https://api.mch.weixin.qq.com/pay/unifiedorder");
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            httpURLConnection.setRequestProperty("Host", "api.mch.weixin.qq.com");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);//Post请求不能使用缓存

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setReadTimeout(10 * 1000);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(reqBody.getBytes(UTF8));

            //获取内容
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
            final StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            resp = stringBuffer.toString();
        } catch (Exception e) {
            log.error("http请求异常：" + e);
        }
        return resp;
    }

    /**
     * @param out_trade_no
     * @param total_fee
     * @Author: HONGLINCHEN
     * @Description:微信退款
     * @Date: 2017-9-11 14:35
     * @return:
     */
    public static String wxPayRefund(String out_trade_no, String total_fee, String refundId) {
        StringBuffer xml = new StringBuffer();
        String data = null;
        try {
            String nonceStr = genNonceStr();
            xml.append("</xml>");
            SortedMap<String, String> parameters = new TreeMap<String, String>();
            parameters.put("appid", appId);
            parameters.put("mch_id", mchId);
            parameters.put("nonce_str", nonceStr);
            parameters.put("out_trade_no", out_trade_no);
            //  parameters.put("transaction_id", transaction_id);
            parameters.put("out_refund_no", refundId);
            parameters.put("fee_type", "CNY");
            parameters.put("total_fee", total_fee);
            parameters.put("refund_fee", total_fee);
            parameters.put("op_user_id", mchId);
            parameters.put("sign", createSign(parameters, key));
            data = SortedMaptoXml(parameters);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        return data;
    }

    /**
     * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     */
    public static String createSign(SortedMap<String, String> packageParams, String AppKey) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + AppKey);
//        String sign = MD5.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
        String sign = sb.toString().toUpperCase();
        return sign;
    }

    /**
     * @param params
     * @Author: HONGLINCHEN
     * @Description:请求值转换为xml格式 SortedMap转xml
     * @Date: 2017-9-7 17:18
     */
    private static String SortedMaptoXml(SortedMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Set es = params.entrySet();
        Iterator it = es.iterator();
        sb.append("<xml>\n");
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            sb.append("<" + k + ">");
            sb.append(v);
            sb.append("</" + k + ">\n");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 证书使用
     * 微信退款
     */
    public static String wxPayBack(String data) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File("/data/certificate/apiclient_cert.p12"));
        String result = "";
        try {
            keyStore.load(instream, mchId.toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, mchId.toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
            HttpPost httppost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            StringEntity entitys = new StringEntity(data);
            httppost.setEntity((HttpEntity) entitys);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String text = "";
                    String t = "";
                    while ((text = bufferedReader.readLine()) != null) {
                        t += text;
                    }
                    byte[] temp = t.getBytes("gbk");//这里写原编码方式
                    String newStr = new String(temp, "utf-8");//这里写转换后的编码方式
                    result = newStr;
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }

    /**
     * 生成32位随机数字
     */
    public static String genNonceStr() {
        Random random = new Random();
//        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());

        return String.valueOf(random.nextInt(10000));
    }

}
