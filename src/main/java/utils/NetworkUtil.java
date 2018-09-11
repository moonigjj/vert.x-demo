/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author tangyue
 * @version $Id: NetworkUtil.java, v 0.1 2018-05-14 14:26 tangyue Exp $$
 */
@Log4j2
public final class NetworkUtil {

    private static HttpClient client;

    public enum ContentType {
        JSON, XML, FORM
    }

    public static void init(){

        Vertx vertx = CommonUtil.vertx();
        if (Objects.isNull(vertx)){
            throw new RuntimeException("请先初始化CommonUtil类！");
        }
        client = vertx.createHttpClient(new HttpClientOptions().setLogActivity(false));
    }

    public static void asyncPostStringWithData(String url, String body, ContentType type, Handler<String> callback) {
        asyncPostStringWithData(url, body, type, "UTF-8", callback);
    }

    public static void asyncPostStringWithData(String url, String body, ContentType type, String encode, Handler<String> callback) {
        checkInitialized();
        HttpClientRequest req = client.requestAbs(HttpMethod.POST, url, resp -> {
            resp.bodyHandler(buf -> {
                callback.handle(buf.toString());
            });
        });
        switch (type) {
            case XML:
                req.putHeader("content-type", "application/xml;charset=" + encode);
                break;
            case JSON:
                req.putHeader("content-type", "application/json;charset=" + encode);
                break;
            case FORM:
                req.putHeader("content-type", "application/x-www-form-urlencoded" + encode);
                break;
        }
//        req.putHeader("content-length", String.valueOf(body.length()));
//        req.write(body);
        req.end(body, encode);
    }

    public static void asyncPostJson(String url, Handler<JsonObject> callback) {
        asyncRequestJson(HttpMethod.POST, url, callback);
    }

    public static void asyncPostJson(String url, Future<JsonObject> callback) {
        asyncRequestJson(HttpMethod.POST, url, callback);
    }

    public static void asyncPostString(String url, Handler<String> callback) {
        asyncRequestString(HttpMethod.POST, url, callback);
    }

    public static void asyncGetJson(String url, Handler<JsonObject> callback) {
        asyncRequestJson(HttpMethod.GET, url, callback);
    }

    public static void asyncGetString(String url, Handler<String> callback) {
        asyncRequestString(HttpMethod.GET, url, callback);
    }

    private static void asyncRequestString(HttpMethod method, String url, Handler<String> callback){
        checkInitialized();
        client.requestAbs(method, url, resp -> {
            resp.bodyHandler(buf -> {
                callback.handle(buf.toString());
            });
        }).end();
    }

    private static void asyncRequestJson(HttpMethod method, String url, Handler<JsonObject> callback){
        checkInitialized();
        client.requestAbs(method, url, resp -> {
            resp.bodyHandler(buf -> {
                callback.handle(buf.toJsonObject());
            });
        }).end();
    }

    private static void asyncRequestJson(HttpMethod method, String url, Future<JsonObject> callback){
        checkInitialized();
        client.requestAbs(method, url, resp -> {
            resp.bodyHandler(buf -> {
                callback.complete(buf.toJsonObject());
            });
        }).end();
    }

    private static void checkInitialized() {
        if(client == null){
            throw new IllegalStateException("Please set Vertx before you call getSubRouter()!!!");
        }
    }

    /**
     * 下载网络文件到指定文件
     *
     * @param url  网络文件地址
     * @param file 指定文件对象
     */
    public static void downloadFile(String url, File file) {
        byte[] fileBytes = getFileFromNetByUrl(url);
        if (null != fileBytes && fileBytes.length > 0) {
            log.info("读取到：" + fileBytes.length + " 字节");
            writeImageToDisk(fileBytes, file);
        } else {
            throw new RuntimeException("没有从该链接获得内容");
        }
    }

    /**
     * 将文件写入到磁盘
     *
     * @param img  文件的字节数组
     * @param file 用于保存的文件
     */
    private static void writeImageToDisk(byte[] img, File file) {
        FileOutputStream fops = null;
        try {
            fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fops != null){
                try {
                    fops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据地址获得数据的字节流
     *
     * @param strUrl 网络连接地址
     * @return 读取到的字节数组
     */
    private static byte[] getFileFromNetByUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            return readInputStream(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从输入流中获取数据
     *
     * @param inStream 输入流
     * @return 读取到的字节数组
     */
    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
}
