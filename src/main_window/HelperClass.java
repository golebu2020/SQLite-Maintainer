package main_window;

import java.io.File;
import java.util.regex.Pattern;

public class HelperClass {

       public static final String openRecentTable = "openRecentTable";
       public static final String menuInfoDatabase = "menuInfo.db";
       public static final String sqlMaster = "SELECT name FROM sqlite_master WHERE type='table';";

       //CommandHistoryDatabaseHelper
       public static final String commandHistoryDatabase = "commandHistory.db";
       public static final String commandHistoryTable = "commandHistoryTable";

       public static final String connectedDBHistory = "connectedDBHistory.db";
       public static final String connectedDBHistoryTable = "connectedDBHistoryTable";


       public static final String INSERT = "Insert"+ " operation successful!";
       public static final String DELETE = "Delete"+ " operation successful!";
       public static final String UPDATE = "Update"+ " operation successful!";
       public static final String ALTER= "Alter"+ " operation successful!";
       public static final String CREATE = "Create"+ " operation successful!";
       public static final String DROP = "Drop"+ " operation successful!";
       public static final String SELECT= "Select"+ " operation successful!";
       public static final String[] KEYWORDS = new String[] {
               "SELECT", "select","FROM", "from",
               "WHILE", "while", "INSERT", "insert",
               "UPDATE", "update", "DELETE", "delete",
               "ALTER", "alter", "DROP", "drop",
               "CREATE", "create", "table", "TABLE",
               "COLUMN", "column", "rows", "ROWS", "INTEGER", "integer", "INT", "int",
               "primary", "PRIMARY", "KEY", "key", "TEXT", "text", "NOT", "not", "OR",
               "and", "AND", "INTO", "into", "VALUES", "values"
       };
       private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", HelperClass.KEYWORDS) + ")\\b";
       private static final String PAREN_PATTERN = "\\(|\\)";
       private static final String BRACE_PATTERN = "\\{|\\}";
       private static final String BRACKET_PATTERN = "\\[|\\]";
       private static final String SEMICOLON_PATTERN = "\\;";
       private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
       private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

       public static final Pattern PATTERN = Pattern.compile(
               "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                       + "|(?<PAREN>" + PAREN_PATTERN + ")"
                       + "|(?<BRACE>" + BRACE_PATTERN + ")"
                       + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                       + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                       + "|(?<STRING>" + STRING_PATTERN + ")"
                       + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
       );

       public static String getDB_DRIVER() {
           String DB_DRIVER = "org.sqlite.JDBC";
           return DB_DRIVER;
       }
       public static String getCONN_STRING(File dbName){
           return "jdbc:sqlite:" + "\\" + String.valueOf(dbName);
       }
       public static String getCONN_STRING(String dbName){
           return "jdbc:sqlite:" + dbName;
       }

}
