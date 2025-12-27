package com.manus.poemchallenge.api;

import com.manus.poemchallenge.data.PoemData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * 古诗 API 客户端，用于异步获取古诗词数据。
 */
public class PoemAPIClient {

    private final CloseableHttpClient httpClient;
    private final String apiUrl;
    private final Gson gson;

    public PoemAPIClient(String apiUrl) {
        this.apiUrl = apiUrl;
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    /**
     * 异步获取一句古诗词。
     * @return 包含 PoemData 的 CompletableFuture
     */
    public CompletableFuture<PoemData> fetchRandomPoem() {
        // 使用 CompletableFuture.supplyAsync 在 ForkJoinPool 中执行阻塞的 HTTP 请求
        return CompletableFuture.supplyAsync(() -> {
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    return parsePoemData(responseBody);
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Poem API request failed with status code: " + statusCode + ". Response body: " + responseBody);
                    return null;
                }
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error fetching poem from API: " + e.getMessage(), e);
                return null;
            }
        });
    }

    /**
     * 解析 API 返回的 JSON 字符串为 PoemData 对象。
     */
    private PoemData parsePoemData(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            // 检查状态
            if (!root.has("status") || !"success".equals(root.get("status").getAsString())) {
                Bukkit.getLogger().log(Level.WARNING, "Poem API returned failure status: " + json);
                return null;
            }

            JsonObject data = root.getAsJsonObject("data");
            String content = data.get("content").getAsString();
            JsonObject origin = data.getAsJsonObject("origin");
            String title = origin.get("title").getAsString();
            String author = origin.get("author").getAsString();
            String dynasty = origin.get("dynasty").getAsString();

            return new PoemData(content, title, author, dynasty);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error parsing poem JSON: " + e.getMessage() + "\nJSON: " + json, e);
            return null;
        }
    }
    
    /**
     * 关闭 HttpClient
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error closing HttpClient: " + e.getMessage(), e);
        }
    }
}
