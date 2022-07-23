package controllers;

import main_window.HelperClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AddDatabaseHelper {
    public static Connection conn = null;
    public static boolean ISCONN_CLOSED = false;
    public static void initDB(String absolutePath, String dbName){
        try {
            getConnection(absolutePath, dbName);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getConnection(String path, String dbName) throws SQLException, ClassNotFoundException {
            String connString = "jdbc:sqlite:" + path + "/" + dbName;
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(connString);
            conn.setAutoCommit(true);
    }
}
