package controllers;

import javafx.scene.control.Tab;
import main_window.HelperClass;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WriteQueryDatabaseHelper {
    public static Connection conn = null;
    public static List<String> TABLE_NAMES = new ArrayList<>();
    public static List<List<String>> DATA_ROWS = new ArrayList<>();
    public static List<String> COLUMN_NAMES = new ArrayList<>();
    public static String QUERY_TABLE_NAME = null;
    private static String DATABASE_NAME = null;
    public static boolean opType = false;
    public static String QUERY_TYPE = "Unknown Query Type";

    public static void initDB(String databaseName){
        try {
            getConnection(databaseName);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void getConnection(String databaseName) throws SQLException, ClassNotFoundException {
        DATABASE_NAME = databaseName;
        Class.forName(HelperClass.getDB_DRIVER());
        conn = DriverManager.getConnection(HelperClass.getCONN_STRING(databaseName));
    }

    public static void executeCode(String queryString) throws SQLException, ClassNotFoundException {
        if(conn == null || conn.isClosed()){
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(DATABASE_NAME));
        }
        conn.setAutoCommit(true);
        DATA_ROWS.clear();
        COLUMN_NAMES.clear();
        String matchedTableName = null;
        for (String tableName : TABLE_NAMES) {
            if (queryString.contains(tableName)) {
                QUERY_TABLE_NAME = tableName;
                matchedTableName = tableName;
                break;
            }
        }
        List<String> columnNames = getColumnNames(matchedTableName, queryString);
        COLUMN_NAMES = columnNames;
        Statement stmt = conn.createStatement();
        if(queryString.toLowerCase().contains("insert") ||
                queryString.toLowerCase().contains("update") ||
                queryString.toLowerCase().contains("delete") ||
                queryString.toLowerCase().contains("alter") ||
                queryString.toLowerCase().contains("create") ||
                queryString.toLowerCase().contains("drop")){
           // extracted(queryString);
            opType = true;
            stmt.executeUpdate(queryString);
            //System.out.println(WriteQueryDatabaseHelper.QUERY_TABLE_NAME);
           // executeCode("SELECT * FROM " + WriteQueryDatabaseHelper.QUERY_TABLE_NAME);

        }else {
            opType = false;
            ResultSet resultSet = stmt.executeQuery(queryString);
            while (resultSet.next()) {
                List<String> info = new ArrayList<>();
                for (String columnName : columnNames) {
                    info.add(resultSet.getString(columnName));
                }
                DATA_ROWS.add(info);
            }
            QUERY_TYPE = "Select"+ " operation successful!";
        }
    }



    private static List<String> getColumnNames(String tableName, String sql) throws SQLException, ClassNotFoundException {
        if(conn == null || conn.isClosed()){
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(DATABASE_NAME));
        }

        List<String> columns = new ArrayList<>();
        Statement statement = conn.createStatement();
        if(!(sql.toLowerCase().contains("insert") ||
                sql.toLowerCase().contains("update") || sql.toLowerCase().contains("delete") ||
                sql.toLowerCase().contains("create") || sql.toLowerCase().contains("drop")  )) {
            ResultSet rs = statement.executeQuery(sql);
            ResultSetMetaData mrs = rs.getMetaData();
            for (int i = 1; i <= mrs.getColumnCount(); i++) {
                columns.add(mrs.getColumnName(i));
            }
        }
        return columns;
    }
}
