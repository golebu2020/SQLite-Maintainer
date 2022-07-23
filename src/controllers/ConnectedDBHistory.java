package controllers;

import main_window.HelperClass;

import java.sql.*;

public class ConnectedDBHistory {
    public static Connection conn = null;
    public static boolean ISCONN_CLOSED = false;
    public static Statement stmt =  null;
    final private static String dbName = HelperClass.connectedDBHistory;
    public static void initDB(){
        try {
            getConnection(dbName);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getConnection(String databaseName) throws SQLException, ClassNotFoundException {
        if(conn == null){
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(databaseName));
            conn.setAutoCommit(true);

            createTable(HelperClass.connectedDBHistoryTable);
        }
    }

    public static void createTable(String tableName) throws SQLException, ClassNotFoundException {

        stmt = conn.createStatement();
        boolean tableCreated = false;
        ResultSet resultSet = stmt.executeQuery(HelperClass.sqlMaster);
        while(resultSet.next()){
            if(resultSet.getString("name").equals(tableName)){
                tableCreated = true;
            }
        }
        if(!tableCreated){
            String sql = "CREATE TABLE " + tableName +
                    " (id INTEGER PRIMARY KEY, " +
                    "connString TEXT NOT NULL, " +
                    "dbName TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "selected TEXT NOT NULL);";
            stmt.execute(sql);

        }

    }
    public static void saveCommand(String tableName, String connString, String dbName, String time, String selected) throws SQLException {
        initDB();
        String sql = "INSERT INTO " + tableName +  " (connString, dbName, time, selected) VALUES (?, ?, ?, ?)";
        try(PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, connString);
            pstmt.setString(2, dbName);
            pstmt.setString(3, time);
            pstmt.setString(4, selected);
            pstmt.executeUpdate();
        }
    }

    public static ResultSet getCommand() throws SQLException {
        initDB();
        updateSelected();
        stmt = conn.createStatement();
        String sql = "SELECT * FROM " + HelperClass.connectedDBHistoryTable;
        return stmt.executeQuery(sql);
    }

    public static ResultSet getCommand2() throws SQLException {
        initDB();
        //updateSelected();
        stmt = conn.createStatement();
        String sql = "SELECT * FROM " + HelperClass.connectedDBHistoryTable;
        return stmt.executeQuery(sql);
    }

    public static void updateSelected() throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql1 = "UPDATE " + HelperClass.connectedDBHistoryTable + " SET selected = " + "0";
        stmt.execute(sql1);

    }

    public static void updateSelectedExact(int id) throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql = "UPDATE " + HelperClass.connectedDBHistoryTable + " SET selected = " + "1" + " WHERE id = " + id;
        stmt.executeUpdate(sql);
    }

    public static void clearCommandHistory() throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql = "DELETE FROM " + HelperClass.connectedDBHistoryTable;
        stmt.executeUpdate(sql);
    }

    public static void clearCommandHistory(int id) throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql = "DELETE FROM " + HelperClass.connectedDBHistoryTable + " WHERE id = " + id;
        stmt.executeUpdate(sql);
    }

    public static void closeDB() throws SQLException {
        if(!conn.isClosed())
            conn.close();
    }
}
