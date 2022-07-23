package models;

public class ActivateDBHistory {
    private int id;
    private String connectionString;
    private String databaseName;
    private String connectionTime;
    private String selected;

    public ActivateDBHistory(int id, String connectionString, String databaseName, String connectionTime, String selected) {
        this.id = id;
        this.connectionString = connectionString;
        this.databaseName = databaseName;
        this.connectionTime = connectionTime;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getConnectionTime() {
        return connectionTime;
    }

    public void setConnectionTime(String connectionTime) {
        this.connectionTime = connectionTime;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
