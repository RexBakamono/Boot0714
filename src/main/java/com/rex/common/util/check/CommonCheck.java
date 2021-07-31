package com.rex.common.util.check;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.*;

/**
 * 网易易盾检测
 * 文本/图片 可疑需要人工审核的直接不通过
 * 视频 只能离线调用接口获取审核结果
 */
@Slf4j
public class CommonCheck {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 业务ID，易盾根据产品业务特点分配
     */
    private final static String BUSINESSID = "your_business_id";
    /**
     * 易盾反垃圾云服务文本在线检测接口地址
     */
    private final static String TEXT_URL = "http://as.dun.163.com/v3/text/check";
    /**
     * 易盾反垃圾云服务图片在线检测接口地址
     */
    private final static String IMAGE_URL = "http://as.dun.163.com/v4/image/check";
    /**
     * 易盾反垃圾云服务视频信息提交接口地址
     */
    private final static String VIDEO_URL = "http://as.dun.163.com/v3/video/submit";

    /**
     * 易盾反垃圾云服务视频信息离线获取接口地址
     */
    private final static String GET_VIDEO_URL = "http://as.dun.163.com/v3/video/callback/results";

    /**
     * 设置公共参数
     *
     * @param version 版本
     */
    private static Map<String, String> getCommon(String version) {
        Map<String, String> params = new HashMap<>();
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", version);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // 默认MD5
        params.put("signatureMethod", "MD5");
        // 设置签名
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);
        return params;
    }

    /**
     * 文本检测(实时)
     *
     * @param id      id
     * @param content 文本内容,非null
     * @return -1-审核异常 0-通过信息 1-可疑信息，转人工复核 2-不通过信息
     */
    public static int checkText(String id, String content) {
        log.error("文本审核：id-" + id + ",content:" + content);
        // 文本过滤表情
        EmojiParser.removeAllEmojis(content);
        // 设置公共参数
        Map<String, String> params = getCommon("v3.1");
        // 设置私有参数
        params.put("dataId", id);
        params.put("content", content);
        // 发送HTTP请求
        HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);
        String response = HttpClient4Utils.sendPost(httpClient, TEXT_URL, params, Consts.UTF_8);
        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject resultObject = jObject.getAsJsonObject("result");
            return resultObject.get("action").getAsInt();
        } else {
            log.error("文本审核异常：" + msg);
        }
        return -1;
    }

    /**
     * 图片检测(实时)
     *
     * @param jsonArray 图片集合
     * @return -1-审核异常 0-通过信息 1-可疑信息，转人工复核 2-不通过信息 610-图片下载失败，620-图片格式错误，630-其它
     */
    public static int checkImage(JsonArray jsonArray) {
        log.error("图片审核：jsonArray-" + jsonArray.toString());
        Map<String, String> params = getCommon("v4");
        params.put("images", jsonArray.toString());
        HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);
        String response = HttpClient4Utils.sendPost(httpClient, IMAGE_URL, params, Consts.UTF_8);
        JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
        int code = obj.get("code").getAsInt();
        String msg = obj.get("msg").getAsString();
        if (code == 200) {
            int result = 0;
            JsonArray array = obj.getAsJsonArray("antispam");
            for (JsonElement jsonElement : array) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                int status = jObject.get("status").getAsInt();
                if (status != 0) {
                    result = status;
                    break;
                }
            }
            return result;
        } else {
            log.error("图片检测异常：" + msg);
        }
        return -1;
    }

    /**
     * 视频检测
     *
     * @param id  id 视频id
     * @param url 视频链接
     * @return null-检测失败 taskId 定时离线获取是否通过的id
     */
    public static String checkVideo(String id, String url) {
        log.error("视频审核：id-" + id + ",url" + url);
        Map<String, String> params = getCommon("v3");
        params.put("url", url);
        params.put("dataId", id);
        HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);
        String response = HttpClient4Utils.sendPost(httpClient, VIDEO_URL, params, Consts.UTF_8);
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = jObject.getAsJsonObject("result");
            // status 0:成功，1:失败
            int status = result.get("status").getAsInt();
            if (status == 0) {
                return result.get("taskId").getAsString();
            }
        } else {
            log.error("视频审核异常：" + msg);
        }
        return null;
    }

    /**
     * 获取视频检测数据
     *
     * @return id-任务id level-等级（0-正常，1-不确定，2-确定）
     */
    public static List<Map<String, Object>> getVideoCheck() {
        Map<String, String> params = getCommon("v3.1");
        HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);
        String response = HttpClient4Utils.sendPost(httpClient, GET_VIDEO_URL, params, Consts.UTF_8);
        JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
        int code = obj.get("code").getAsInt();
        String msg = obj.get("msg").getAsString();
        if (code == 200) {
            JsonArray resultArray = obj.getAsJsonArray("result");
            if (resultArray.size() == 0) {
                log.info("暂无视频回调数据");
            } else {
                List<Map<String, Object>> list = new ArrayList<>();
                for (JsonElement jsonElement : resultArray) {
                    Map<String, Object> map = new HashMap<>();
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int status = jObject.get("status").getAsInt();
                    if (status != 0) {
                        log.error("视频异常，status=" + status);
                        continue;
                    }
                    map.put("id", jObject.get("taskId").getAsString());
                    map.put("level", jObject.get("level").getAsInt());
                    list.add(map);
                }
                return list;
            }
        } else {
            log.error("主动获取离线视频检测接口失败：" + msg);
        }
        return null;
    }
}