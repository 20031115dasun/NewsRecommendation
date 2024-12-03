package com.example.oodcw;

public class Article {
    private final int id;
    private final String title;
    private final String description;
    private final String content;
    private final String category;
    private final String imageUrl;
    private final String source;

    // Constructor
    public Article(int id, String title, String description, String content, String category, String imageUrl, String source) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        this.source = source;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getSource() { return source; }

    // Override toString to display Article information
    @Override
    public String toString() {
        return "Title: " + title + "\nDescription: " + description + "\nSource: " + source;
    }
}
