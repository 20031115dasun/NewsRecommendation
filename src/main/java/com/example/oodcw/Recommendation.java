package com.example.oodcw;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Recommendation {

    @FXML
    private ImageView articleImage;

    @FXML
    private Label articleTitle;

    @FXML
    private TextArea articleDescription;

    @FXML
    private Button homeButton;

    private List<Article> articles = new ArrayList<>();
    private int currentIndex = 0;
    private Article currentArticle;

    // ExecutorService for handling concurrency
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    private void initialize() {
        // Fetch articles initially based on user preferences
        String currentUser = Login.currentUser;
        executorService.submit(() -> fetchAndUpdateArticles(Database.getUserPreferences(currentUser)));
    }

    // Method to fetch and update articles
    private void fetchAndUpdateArticles(String preferences) {
        if (preferences != null && !preferences.isEmpty()) {
            try {
                articles = fetchArticlesBasedOnPreferences(preferences); // Fetch articles based on preferences
                if (!articles.isEmpty()) {
                    Platform.runLater(this::loadNextArticle); // Load the first article on the UI thread
                } else {
                    Platform.runLater(this::displayNoArticlesMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(this::displayNoArticlesMessage);
            }
        } else {
            Platform.runLater(this::displayNoArticlesMessage);
        }
    }

    // Method to update recommendations
    public void updateRecommendations(String preferences) {
        // Reset current state
        currentIndex = 0;
        articles.clear();
        currentArticle = null;

        // Fetch new articles based on updated preferences
        executorService.submit(() -> fetchAndUpdateArticles(preferences));
    }

    // Utility method to fetch articles based on preferences
    private List<Article> fetchArticlesBasedOnPreferences(String preferences) {
        List<Article> recommendedArticles = new ArrayList<>();
        if (preferences != null && !preferences.isEmpty()) {
            String[] categories = preferences.split(","); // Split the preferences

            // Use parallel threads to fetch articles for each category concurrently
            List<Future<List<Article>>> futures = new ArrayList<>();
            for (String category : categories) {
                String trimmedCategory = category.trim().toLowerCase(); // Ensure proper formatting
                futures.add(executorService.submit(() -> NewsAPIClient.fetchArticlesByCategory(trimmedCategory)));
            }

            // Collect all articles from futures
            for (Future<List<Article>> future : futures) {
                try {
                    recommendedArticles.addAll(future.get()); // Wait for each fetch to complete
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return recommendedArticles;
    }

    // Load the next article
    private void loadNextArticle() {
        if (currentIndex < articles.size()) {
            currentArticle = articles.get(currentIndex++);
            displayArticle(currentArticle);
        } else {
            displayNoMoreArticlesMessage();
        }
    }

    // Display the current article
    private void displayArticle(Article article) {
        articleTitle.setText(article.getTitle());
        articleDescription.setText(article.getDescription());
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            articleImage.setImage(new Image(article.getImageUrl()));
        } else {
            articleImage.setImage(null); // Placeholder for no image
        }
    }

    // Display message when no articles are available
    private void displayNoArticlesMessage() {
        articleTitle.setText("No recommended articles found.");
        articleDescription.setText("Please update your preferences to get recommendations.");
        articleImage.setImage(null);
    }

    // Display message when no more articles are available
    private void displayNoMoreArticlesMessage() {
        articleTitle.setText("No more recommended articles.");
        articleDescription.clear();
        articleImage.setImage(null);
    }

    @FXML
    private void handleLike(ActionEvent event) {
        if (currentArticle != null) {
            saveFeedback("like", false);
            loadNextArticle();
        }
    }

    @FXML
    private void handleSkip(ActionEvent event) {
        if (currentArticle != null) {
            saveFeedback("skip", true);
            loadNextArticle();
        }
    }

    // Save user feedback to the database
    private void saveFeedback(String feedback, boolean skipped) {
        String currentUser = Login.currentUser;
        if (currentArticle != null) {
            executorService.submit(() -> Database.saveUserFeedback(currentUser, currentArticle.getId(), feedback, skipped));
        }
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            AnchorPane homePage = loader.load();
            Scene homeScene = new Scene(homePage, 400, 600); // Match Dashboard size
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
