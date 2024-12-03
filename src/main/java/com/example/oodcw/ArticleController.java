package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ArticleController {

    @FXML
    private ListView<String> articleList;

    @FXML
    private TextArea articleDetails;

    @FXML
    private Button homeButton;

    @FXML
    private ImageView articleImage;

    private List<Article> articles = new ArrayList<>();

    // ExecutorService for concurrency
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    private void initialize() {
        // Fetch articles based on user preferences concurrently
        String preferences = Login.userPreference;
        if (preferences == null || preferences.isEmpty()) {
            displayNoArticlesMessage("No preferences set. Please update your profile to get article recommendations.");
            return;
        }

        executorService.submit(() -> {
            List<Article> fetchedArticles = fetchArticlesBasedOnPreferences(preferences);
            if (!fetchedArticles.isEmpty()) {
                articles = fetchedArticles;
                javafx.application.Platform.runLater(this::populateArticleList);
            } else {
                javafx.application.Platform.runLater(() -> displayNoArticlesMessage("No articles available based on your preferences."));
            }
        });
    }

    private List<Article> fetchArticlesBasedOnPreferences(String preferences) {
        List<Article> recommendedArticles = new ArrayList<>();
        String[] categories = preferences.split(",");

        List<Future<List<Article>>> futures = new ArrayList<>();
        for (String category : categories) {
            String trimmedCategory = category.trim().toLowerCase();
            futures.add(executorService.submit(() -> NewsAPIClient.fetchArticlesByCategory(trimmedCategory)));
        }

        for (Future<List<Article>> future : futures) {
            try {
                recommendedArticles.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return recommendedArticles;
    }

    private void populateArticleList() {
        articleList.getItems().clear();
        for (Article article : articles) {
            articleList.getItems().add(article.getTitle());
        }

        articleList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayArticleDetails(newValue);
            }
        });
    }

    private void displayArticleDetails(String selectedTitle) {
        for (Article article : articles) {
            if (article.getTitle().equals(selectedTitle)) {
                StringBuilder details = new StringBuilder();
                details.append("Title: ").append(article.getTitle()).append("\n\n")
                        .append("Content: ").append(article.getContent()).append("\n\n")
                        .append("Source: ").append(article.getSource());

                articleDetails.setText(details.toString());

                if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                    articleImage.setImage(new Image(article.getImageUrl()));
                } else {
                    articleImage.setImage(null);
                }
                return;
            }
        }
        articleDetails.setText("No details available for the selected article.");
    }

    private void displayNoArticlesMessage(String message) {
        articleList.getItems().clear();
        articleList.getItems().add(message);
        articleDetails.clear();
        articleImage.setImage(null);
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            AnchorPane homePage = loader.load();
            Scene homeScene = new Scene(homePage, 400, 600); // Dashboard size
            Stage currentStage = (Stage) homeButton.getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Shutdown the executor service when the class is destroyed
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
