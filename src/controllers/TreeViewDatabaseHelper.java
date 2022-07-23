package controllers;
import main_window.HelperClass;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreeViewDatabaseHelper {

    public static Connection conn = null;
    public static boolean ISCONN_CLOSED = false;
    protected ResultSet extractDBTables(File dbName) throws SQLException, ClassNotFoundException {
        Class.forName(HelperClass.getDB_DRIVER());

        conn = DriverManager.getConnection(HelperClass.getCONN_STRING(dbName));
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");

    }


    protected List<String> getColumnNames(File dbName, String tableName) throws SQLException, ClassNotFoundException {
        Class.forName(HelperClass.getDB_DRIVER());
        conn = DriverManager.getConnection(HelperClass.getCONN_STRING(dbName));

        List<String> columns = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " LIMIT 0";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        ResultSetMetaData mrs = rs.getMetaData();
        for(int i = 1; i <= mrs.getColumnCount(); i++) {
            columns.add(mrs.getColumnName(i));
        }
        return columns;
    }

}
