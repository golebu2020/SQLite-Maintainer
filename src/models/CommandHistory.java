package models;

import javafx.scene.image.ImageView;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommandHistory {
    private String message;
    private String query;
    private ImageView status;
    private String date;

    public CommandHistory(String message, String query, ImageView status, String date) {
        this.message = message;
        this.query = query;
        this.status = status;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ImageView getStatus() {
        return status;
    }

    public void setStatus(ImageView status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
