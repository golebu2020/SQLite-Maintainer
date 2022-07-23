package controllers;

import main_window.HelperClass;

import java.sql.*;

public class OpenRecentDatabaseHelper {

    public static Connection conn = null;
    public static boolean ISCONN_CLOSED = false;

    public static void initDB(){
        try {
            getConnection();
            System.out.println("Database already crated");
            createTable(HelperClass.openRecentTable);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getConnection() throws SQLException, ClassNotFoundException {
            Class.forName(HelperClass.getDB_DRIVER());
            conn = DriverManager.getConnection(HelperClass.getCONN_STRING(HelperClass.menuInfoDatabase));
            conn.setAutoCommit(true);

    }

    public static void createTable(String tableName) throws SQLException, ClassNotFoundException {
        //check if table already exists
        getConnection();
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery(HelperClass.sqlMaster);
        boolean statusChecker= false;
        while(resultSet.next()){
           if(resultSet.getString("name").equals(tableName)){
               statusChecker = true;
               System.out.println("Table already created");
           }
        }

        if(!statusChecker){
            String sql = "CREATE TABLE " + tableName + " "+
                    "(id INT PRIMARY KEY," +
                    " name TEXT NOT NULL);";
            stmt.execute(sql);
            System.out.println("Successfully created the table : " + tableName);
        }

    }

    public static void saveDirName(String tableName, String dirName) throws SQLException, ClassNotFoundException {
        getConnection();
        String sql = "INSERT INTO openRecentTable (name) VALUES ('" + dirName + "');";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        System.out.println("Successfully inserted");

    }

    public static ResultSet retrieveDirNames() throws SQLException, ClassNotFoundException {
        getConnection();
        Statement stmt = conn.createStatement();

        return stmt.executeQuery("SELECT * FROM " +
                HelperClass.openRecentTable +
                " ORDER BY id DESC;"
        );
    }



}
