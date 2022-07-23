package controllers;

import main_window.HelperClass;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CommandHistoryDatabaseHelper {

    public static Connection conn = null;
    public static boolean ISCONN_CLOSED = false;
    public static Statement stmt =  null;
    public static void initDB(){
        try {
            getConnection(HelperClass.commandHistoryDatabase);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void getConnection(String databaseName) throws SQLException, ClassNotFoundException {
//        if(conn == null && !ISCONN_CLOSED){
//            Class.forName(HelperClass.getDB_DRIVER());
//            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(databaseName));
//            conn.setAutoCommit(true);
//        }if(ISCONN_CLOSED){
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(databaseName));
            conn.setAutoCommit(true);
//        }
        createTable(HelperClass.commandHistoryTable);

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
                    " (id INT PRIMARY KEY, message TEXT NOT NULL, " +
                    "query TEXT NOT NULL, status INT NOT NULL," +
                    " date TEXT NOT NULL);";
            stmt.execute(sql);

        }

    }
    public static void saveCommand(String tableName, String message, String query, int status, String date) throws SQLException {
        initDB();
        String sql = "INSERT INTO " + tableName +  " (message, query, status, date) VALUES (?, ?, ?, ?)";
        try(PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, message);
            pstmt.setString(2, query);
            pstmt.setInt(3, status);
            pstmt.setString(4, date);
            pstmt.executeUpdate();
        }
    }

    public static ResultSet getCommand() throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql = "SELECT * FROM " + HelperClass.commandHistoryTable;
        return stmt.executeQuery(sql);
    }

    public static void clearCommandHistory() throws SQLException {
        initDB();
        stmt = conn.createStatement();
        String sql = "DELETE FROM " + HelperClass.commandHistoryTable;
        stmt.executeUpdate(sql);
    }
    public static void closeDB() throws SQLException {
        if(!conn.isClosed())
            conn.close();
    }

}
