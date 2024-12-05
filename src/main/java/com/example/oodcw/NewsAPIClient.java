package com.example.oodcw;

import org.apache.hc.client5.http.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NewsAPIClient {
    private static final String API_KEY = "25845b361b4e4c82b398da76fa94e4e7";
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines";
    private static final Logger logger = LoggerFactory.getLogger(NewsAPIClient.class);

    // Fetch articles based on the category
    public static List<Article> fetchArticlesByCategory(String category) {
        List<Article> articles = new ArrayList<>();
        String url = BASE_URL + "?category=" + category + "&apiKey=" + API_KEY;

        try {
            String response = Request.get(url).execute().returnContent().asString();

            JSONObject jsonResponse = new JSONObject(response);

            if ("ok".equals(jsonResponse.optString("status"))) {
                JSONArray articlesArray = jsonResponse.getJSONArray("articles");

                // Iterate through the articles array
                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleObj = articlesArray.getJSONObject(i);

                    // Extract article details
                    String title = articleObj.optString("title", "No Title");
                    String description = articleObj.optString("description", "No Description");
                    String content = articleObj.optString("content", "No Content");
                    String imageUrl = articleObj.optString("urlToImage", "");
                    String source = articleObj.getJSONObject("source").optString("name", "Unknown Source");

                    // Create and add the article to the list
                    Article article = new Article(i, title, description, content, category, imageUrl, source);
                    articles.add(article);
                }
            } else {
                logger.error("API Error: {}", jsonResponse.optString("message"));
            }
        } catch (Exception e) {
            logger.error("Error fetching articles for category: {}", category, e);
        }

        return articles;
    }

    public static List<Article> fetchGeneralHeadlines() {
        return fetchArticlesByCategory("general");
    }
}
