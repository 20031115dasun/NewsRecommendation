package com.example.oodcw;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:database.db";
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    //connecting to the SQLite database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Connection to SQLite database failed.", e);
        }
        return conn;
    }

    // Initialize all  database tables
    public static void initializeDatabase() {
        createUsersTable();
        createArticlesTable();
        createUserFeedbackTable();
    }

    // Create the users table
    private static void createUsersTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                preferences TEXT DEFAULT ''
            );
        """;
        executeUpdate(sql, "users table");
    }

    // Create the articles table
    private static void createArticlesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS articles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                content TEXT NOT NULL,
                category TEXT NOT NULL,
                published_date TEXT NOT NULL,
                image_url TEXT,
                source TEXT
            );
        """;
        executeUpdate(sql, "articles table");
    }

    // Create the user feedback table
    private static void createUserFeedbackTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                article_id INTEGER NOT NULL,
                feedback TEXT NOT NULL,
                skipped INTEGER DEFAULT 0,
                FOREIGN KEY (username) REFERENCES users(username),
                FOREIGN KEY (article_id) REFERENCES articles(id),
                UNIQUE(username, article_id)
            );
        """;
        executeUpdate(sql, "user_feedback table");
    }

    private static void executeUpdate(String sql, String tableName) {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create " + tableName + " with SQL: " + sql, e);
        }
    }

    // Insert article into the database
    public static void insertArticle(String title, String description, String content, String category, String publishedDate, String imageUrl, String source) {
        String sql = """
            INSERT INTO articles (title, description, content, category, published_date, image_url, source)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(title) DO NOTHING; -- Prevent duplicate titles
        """;

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, content);
            pstmt.setString(4, category);
            pstmt.setString(5, publishedDate);
            pstmt.setString(6, imageUrl);
            pstmt.setString(7, source);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Inserted article: " + title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to insert article.", e);
        }
    }

    // Fetch recommended articles based on user preferences
    public static List<Article> getRecommendedArticles(String username) {
        List<Article> recommendedArticles = new ArrayList<>();
        String query = """
            SELECT a.id, a.title, a.description, a.content, a.category, a.published_date, a.image_url, a.source
            FROM articles a
            JOIN users u ON ',' || u.preferences || ',' LIKE '%,' || a.category || ',%'
            WHERE u.username = ?
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                recommendedArticles.add(new Article(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("content"),
                        rs.getString("category"),
                        rs.getString("image_url"),
                        rs.getString("source")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to fetch recommended articles.", e);
        }
        return recommendedArticles;
    }

    // Save user feedback on articles
    public static void saveUserFeedback(String username, int articleId, String feedback, boolean skipped) {
        String sql = """
            INSERT INTO user_feedback (username, article_id, feedback, skipped)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(username, article_id)
            DO UPDATE SET feedback = excluded.feedback, skipped = excluded.skipped;
        """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, articleId);
            pstmt.setString(3, feedback);
            pstmt.setInt(4, skipped ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to save user feedback.", e);
        }
    }

    // Fetch user preferences
    public static String getUserPreferences(String username) {
        String preferences = null;
        String query = "SELECT preferences FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                preferences = rs.getString("preferences");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to fetch user preferences.", e);
        }
        return preferences;
    }

    // Fetch user interaction history
    public static List<String> getUserInteractionHistory(String username) {
        List<String> history = new ArrayList<>();
        String query = """
            SELECT a.title, f.feedback, f.skipped
            FROM user_feedback f
            JOIN articles a ON f.article_id = a.id
            WHERE f.username = ?
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String feedback = rs.getString("feedback");
                boolean skipped = rs.getInt("skipped") == 1;

                String entry = String.format("Title: %s | Feedback: %s | Skipped: %s",
                        title, feedback, skipped ? "Yes" : "No");
                history.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to fetch user interaction history.", e);
        }
        return history;
    }

    // Fetch all users from the database
    public static List<List<String>> getAllUsers() {
        List<List<String>> users = new ArrayList<>();
        String query = "SELECT id, name, username FROM users";  // Query to fetch id, name, and username from users table

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Add each user as a list of attributes to the users list
                List<String> user = new ArrayList<>();
                user.add(String.valueOf(rs.getInt("id"))); // ID
                user.add(rs.getString("name"));  // Name
                user.add(rs.getString("username")); // Username
                users.add(user);  // Add user data to the list
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to fetch all users.", e);
        }
        return users;
    }

    // Delete a user from the database
    public static boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if a user was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Failed to delete user.", e);
        }
        return false;
    }



}
