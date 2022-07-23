package controllers;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main_window.HelperClass;
import main_window.ListItemAdapter;
import models.CommandHistory;
import models.ActivateDBHistory;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.sql.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController extends TreeViewDatabaseHelper implements Initializable{

    private static  String QUERY_TYPE = null;
    @FXML private Pane paneRefresh;
    @FXML private Pane paneDelete;
    @FXML private ImageView imageViewRefresh;
    @FXML private ImageView imageViewDelete;
    @FXML public Label lblTableName;
    @FXML public Tab tab1;
    @FXML public Tab tab2;
    @FXML public TextArea errorTextArea;
    @FXML public TableColumn<CommandHistory, String> colMessage;
    @FXML public TableColumn<CommandHistory, String> colQuery;
    @FXML public TableColumn<CommandHistory, Integer> colImage;
    @FXML public TableColumn<CommandHistory, Date> colDate;
    @FXML private ListView<ActivateDBHistory> historyListView;
    @FXML private  TabPane resultTabPane;
    @FXML private Label lblDBLocation;
    @FXML private Label lblDBName;
    @FXML private TableView<String[]> dataTable;
    @FXML private Menu openRecentMenuItem;
    @FXML private MenuItem menuItemOpen;
    @FXML private MenuItem menuItemSave;
    @FXML private MenuItem menuItemSaveAs;
    @FXML private VBox mainWindowParent;
    @FXML private CodeArea textAreaCode;
    @FXML private TreeView<String> databaseTreeView;
    @FXML private MenuItem menuItemRun;
//    @FXML private TextArea scrollCodeNumber;
    @FXML private Label lblCommandHistory;
    @FXML private Label lblCommandHistoryState;
    @FXML private VBox vBoxDBLoad;
    @FXML private TableView<CommandHistory> tableCommandHistory;
    @FXML private Label lblLeftStatus;
    @FXML private Label lblRightStatus;
    @FXML private Label lblLoadingDB;
    @FXML private AnchorPane anchorPaneAdd;

    private int commandState = 0;
     private ObservableList<String> observableList;
    private String databasePath = null;
    private final ObservableList<ActivateDBHistory> activateDbHistoryObservableList;
    private ObservableList<String> commandHistoryObservableList;
    private final String clipBoard = "";
    private ObservableList<CommandHistory> cmdHistory;
    private boolean toggleColor = false;
    private final List<String> textNumber = new ArrayList<>();
    private final int textNumberCount = 0;
    private String databaseName = null;
    private Alert alert;
    private int databaseToBeDeleted = 0;
    private TreeItem<String> root;
    private boolean DBInitialized = false;
    private boolean isSelectedFromList =false;


    public static ExecutorService executor;
    Subscription cleanupWhenDone;

    public MainController() throws SQLException {
        commandHistoryObservableList = FXCollections.observableArrayList();
        activateDbHistoryObservableList = FXCollections.observableArrayList();
        cmdHistory = FXCollections.observableArrayList();
        ConnectedDBHistory.initDB();
        ResultSet command = ConnectedDBHistory.getCommand();
        while(command.next()){
            activateDbHistoryObservableList.add(new ActivateDBHistory(command.getInt("id"), command.getString("connString"),
                    command.getString("dbName"), command.getString("time"), String.valueOf(command.getString("selected"))));
        }
    }

    public void executorCode(){
        executor = Executors.newSingleThreadExecutor();
        textAreaCode.setParagraphGraphicFactory(LineNumberFactory.get(textAreaCode));
        cleanupWhenDone = textAreaCode.multiPlainChanges()
                .successionEnds(Duration.ofMillis(50))
                .retainLatestUntilLater(executor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(textAreaCode.multiPlainChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);


    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = textAreaCode.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        textAreaCode.setStyleSpans(0, highlighting);
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = HelperClass.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }



    public void openFolder(ActionEvent actionEvent) throws SQLException, ClassNotFoundException {

        Stage primaryStage = (Stage)mainWindowParent.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select your project folder");
        File dir = directoryChooser.showDialog(primaryStage);
        if(dir != null){
            databasePath = dir.toString();
            lblDBLocation.setText(databasePath);
            extractedFilesAndBuild(dir, dir, !isMenuDBContainsName(dir.toString()));
            isSelectedFromList = false;
            anchorPaneAdd.setVisible(true);
            paneRefresh.setVisible(true);
        }
    }

    private boolean isMenuDBContainsName(String dirName) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = OpenRecentDatabaseHelper.retrieveDirNames();
        while(resultSet.next()){
            if(resultSet.getString("name").equals(dirName)){
                return true;
            }
        }
        return false;
    }

    private void extractedFilesAndBuild(File dir, File files, boolean saveState) throws SQLException, ClassNotFoundException {
        lblLeftStatus.setText("A database folder has been opened");
        List<File> allFiles = Arrays.asList(Objects.requireNonNull(files.listFiles()));
        List<File> collectedDB = allFiles.stream()
                .filter(File::isFile)
                .filter(file -> file.toString().endsWith(".db"))
                .collect(Collectors.toList());
        if(collectedDB.size()>0 && saveState){
            OpenRecentDatabaseHelper.saveDirName(HelperClass.openRecentTable, dir.toString());
        }
        buildTreeView(collectedDB);

    }


    public void closeApp(ActionEvent actionEvent) {
        cleanupWhenDone.unsubscribe();
        Stage window = (Stage) mainWindowParent.getScene().getWindow();
        //executor.shutdown();
        window.close();
    }

    public void createNewProjectFolder(ActionEvent actionEvent) throws SQLException, ClassNotFoundException {
        Stage primaryStage = (Stage)mainWindowParent.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Create a new Project Folder");
        File dir = directoryChooser.showDialog(primaryStage);
        if(dir != null) {
            anchorPaneAdd.setVisible(true);
            paneRefresh.setVisible(true);
            //System.out.println(dir.toString());
            OpenRecentDatabaseHelper.saveDirName(HelperClass.openRecentTable, dir.toString());
            buildOpenRecentMenu();
            databasePath = dir.toString();
            lblDBLocation.setText(databasePath);
            isSelectedFromList = false;

            List<File> collectedDB = getFiles(dir.toString());
            new LoadingProgress(collectedDB, null, true).start();
        }

    }

    class LoadingProgress extends Thread{
        private List<File> collectedDB;
        private File collectedDBSingleFile;
        private boolean initialBuild;
        public LoadingProgress(){

        }
        public LoadingProgress(List<File> collectedDB, File collectedDBSingleFile, boolean initialBuild) {
            this.collectedDB = collectedDB;
            lblLoadingDB.setVisible(true);
            this.initialBuild = initialBuild;
            this.collectedDBSingleFile = collectedDBSingleFile;
        }
        @Override
        public void run() {

            Platform.runLater(() -> {
                if(databaseTreeView.getRoot() != null) databaseTreeView.getRoot().getChildren().clear();
                if(initialBuild){
                    for(File dbName : collectedDB) {
                        try {
                            InitialTreeBuilding(root, dbName);
                        } catch (SQLException | ClassNotFoundException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }else{
                    try {
                        InitialTreeBuilding(root, collectedDBSingleFile);

                    } catch (SQLException | ClassNotFoundException throwables) {
                        throwables.printStackTrace();
                    }
                }
                try {
                    finalTreeBuilding(root);
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
            });
        }
    }

    private void buildTreeView(List<File> collectedDB) throws SQLException, ClassNotFoundException {

        Tooltip tooltip = new Tooltip();
        tooltip.setText("Select a database to begin");
        databaseTreeView.setTooltip(tooltip);

        new LoadingProgress(collectedDB, null, true).start();
        //Creating the children items
    }

    private void InitialTreeBuilding(TreeItem<String> root, File dbName) throws SQLException, ClassNotFoundException {
        //if(databaseTreeView.getRoot() != null) databaseTreeView.getRoot().getChildren().clear();
        ImageView node = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/database.png"))));
        node.setFitHeight(14);
        node.setFitWidth(14);
        TreeItem<String> dbTreeItem = new TreeItem<>(dbName.getName(), node);
        //Attach a tooltip to a tree item;

        //dbTreeItem.setExpanded(true);
        ResultSet resultSet = extractDBTables(dbName);

        List<TreeItem<String>> tableTrees = new ArrayList<>();
        //title table;
        TreeItem<String> titleTable = new TreeItem<>("Tables");

        titleTable.setExpanded(true);
        while (resultSet.next()) {
            String tableName = resultSet.getString("name");
            WriteQueryDatabaseHelper.TABLE_NAMES.add(tableName);

            ImageView node2 = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/table.png"))));
            node2.setFitHeight(14);
            node2.setFitWidth(14);

            TreeItem<String> tableNameTree = new TreeItem<>(tableName, node2);

            tableNameTree.setExpanded(false);

            buildColumnTree(dbName, tableName, tableNameTree);

            tableTrees.add(tableNameTree);
        }
        for(TreeItem<String> tableTree : tableTrees){
            tableTree.setExpanded(false);
            titleTable.getChildren().add(tableTree);
        }
        dbTreeItem.getChildren().add(titleTable);
        root.getChildren().add(dbTreeItem);
    }

    private void finalTreeBuilding(TreeItem<String> root) throws SQLException, ClassNotFoundException {
        databaseTreeView.setShowRoot(true);
        databaseTreeView.setRoot(root);

        buildOpenRecentMenu();
        databaseTreeView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringTreeItem, t1) -> {
            if(t1 != null) databaseName = t1.getValue();
        });
        lblLoadingDB.setVisible(false);
    }

    private void buildColumnTree(File dbName, String tableName, TreeItem<String> tableNameTree)
            throws SQLException, ClassNotFoundException {
        List<String> tableColumns = getColumnNames(dbName, tableName);
        TreeItem<String> columnTitleTree = new TreeItem<>("Columns");
        columnTitleTree.setExpanded(false);
        for(String tableColumn : tableColumns){
            ImageView node = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/vertical.png"))));
            node.setFitHeight(14);
            node.setFitWidth(14);
            columnTitleTree.getChildren().add(new TreeItem<>(tableColumn,node));
        }
        tableNameTree.getChildren().add(columnTitleTree);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        //Initializing the Root Node here
        executorCode();
        root = new TreeItem<>("Databases");
        root.setExpanded(true);
        historyListView.scrollTo(Integer.MAX_VALUE);
        lblLeftStatus.setText("No database folder has been selected");
        CommandHistoryDatabaseHelper.initDB();

        try {
            getCommandHistory();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        dataTable.setPlaceholder(new Label("No data table loaded"));
        setUpCommandHistoryMenu();
        setUpLineNumberList();
        setUpListView();
        runContextMenu();
        textAreaListener();

        databaseTreeView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringTreeItem, t1) -> {
            try{
                if(t1 != null){
                    String parent  = t1.getParent().getValue();
                    if(parent.equals("Tables") && WriteQueryDatabaseHelper.conn != null) {
                        textAreaCode.clear();
                        textAreaCode.replaceText(0,0,"SELECT * FROM " + t1.getValue());
                        textAreaCode.selectAll();
                        try {
                            runProgram();
                        } catch (SQLException | InterruptedException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                    if(t1.getValue().endsWith(".db")){
                        lblDBName.setText(t1.getValue());
                        DBInitialized = true;
                        WriteQueryDatabaseHelper.initDB(databasePath + "\\" + t1.getValue());
                        Stage stage = (Stage)mainWindowParent.getScene().getWindow();
                        stage.setTitle("SQLiteMaintainer-" + databasePath + "\\" + t1.getValue());
                        lblLeftStatus.setText(t1.getValue() + " has been selected");

                        activateDbHistoryObservableList.clear();
                        ResultSet command;
                        ConnectedDBHistory.updateSelected();
                        ConnectedDBHistory.saveCommand(HelperClass.connectedDBHistoryTable, databasePath + "\\" + t1.getValue(),
                                t1.getValue(), String.valueOf(LocalDateTime.now()), "1");

                        command = ConnectedDBHistory.getCommand2();

                        while(command.next()){
                            activateDbHistoryObservableList.add(new ActivateDBHistory(command.getInt("id"), command.getString("connString"),
                                    command.getString("dbName"), command.getString("time"), command.getString("selected")));
                        }

                        historyListView.scrollTo(Integer.MAX_VALUE);
                    }
                }
            }
            catch(NullPointerException | SQLException ignored){}
        });
        setUpMenuIcons();
        try {
            buildOpenRecentMenu();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    private void buildOpenRecentMenu() throws SQLException, ClassNotFoundException {
        ResultSet resultSet = OpenRecentDatabaseHelper.retrieveDirNames();
        if(openRecentMenuItem.getItems().size() != 0) openRecentMenuItem.getItems().clear();
        while(resultSet.next()){
            MenuItem menuItem = new MenuItem(resultSet.getString("name"));
            openRecentMenuItem.getItems().add(menuItem);
            menuItem.setOnAction(actionEvent -> {
                anchorPaneAdd.setVisible(true);
                paneRefresh.setVisible(true);
                List<File> collectedDB = getFiles(menuItem);
                new LoadingProgress(collectedDB, null, true).start();
                databasePath = menuItem.getText().toString();
                lblDBLocation.setText(databasePath);
            });
        }

    }
    private List<File> getFiles(String dir) {
        List<File> allFiles = Arrays.asList(Objects.requireNonNull(new File(dir).listFiles()));
        return allFiles.stream()
                .filter(File::isFile)
                .filter(file -> file.toString().endsWith(".db"))
                .collect(Collectors.toList());
    }
    private List<File> getFiles(MenuItem menuItem) {
        List<File> allFiles = Arrays.asList(Objects.requireNonNull(new File(menuItem.getText()).listFiles()));
        return allFiles.stream()
                .filter(File::isFile)
                .filter(file -> file.toString().endsWith(".db"))
                .collect(Collectors.toList());
    }

    private void setUpMenuIcons(){

        ContextMenu menu = new ContextMenu();
        ImageView imgView = new ImageView(new Image(Objects.requireNonNull(getClass()
                .getResourceAsStream("../resources/delete.png"))));
        imgView.setFitWidth(20);
        imgView.setFitHeight(20);
        MenuItem listViewMenu = new MenuItem("Clear connected database history",imgView);

        menu.getItems().add(listViewMenu);
        historyListView.setContextMenu(menu);

        listViewMenu.setOnAction(actionEvent -> {
            try {
                ConnectedDBHistory.clearCommandHistory();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            activateDbHistoryObservableList.clear();
        });

        historyListView.getSelectionModel().selectedItemProperty().addListener((observableValue, activateDBHistory, t1) -> {
            try {
                Stage stage = (Stage)mainWindowParent.getScene().getWindow();
                if(t1 != null){
                    anchorPaneAdd.setVisible(false);
                    paneRefresh.setVisible(false);
                    if(databaseTreeView.getRoot() != null){
                        databaseTreeView.getRoot().getChildren().clear();
                    }
                    WriteQueryDatabaseHelper.initDB(t1.getConnectionString());
                    databasePath = t1.getConnectionString().replace(t1.getDatabaseName(), "");
                    new LoadingProgress(null, new File(t1.getConnectionString()), false).start();
                    isSelectedFromList = true;

                    stage.setTitle("SQLiteMaintainer--" + t1.getConnectionString());
                    lblLeftStatus.setText(t1.getConnectionString());
                    databaseToBeDeleted = t1.getId();
                }
                ConnectedDBHistory.updateSelected();
                if(t1 != null){
                    ConnectedDBHistory.updateSelectedExact(t1.getId());
                }
                for(ActivateDBHistory item: activateDbHistoryObservableList){
                    item.setSelected("0");
                }
                if(t1 != null){
                    try {
                        activateDbHistoryObservableList.get(t1.getId() - 1).setSelected("1");
                    }
                    catch(IndexOutOfBoundsException ignored){}
                }
            } catch (SQLException ignored) {
            }
        });

        //Open
        Image openIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/folder_open.png")));
        ImageView openView = new ImageView(openIcon);
        openView.setFitWidth(20);
        openView.setFitHeight(20);

        menuItemOpen.setGraphic(openView);
        menuItemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

        Image runIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/runcode.png")));
        ImageView runView = new ImageView(runIcon);
        runView.setFitWidth(20);
        runView.setFitHeight(20);

        menuItemRun.setGraphic(runView);
    }
    private void runContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem runMenu = new MenuItem("Run");
        Image runIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/runcode.png")));
        ImageView runView = new ImageView(runIcon);
        runView.setFitWidth(20);
        runView.setFitHeight(20);

        runMenu.setGraphic(runView);
        runMenu.setOnAction(actionEvent -> {
            try {
                runProgram();
            } catch (SQLException | InterruptedException throwables) {
                throwables.printStackTrace();
            }
        });
        MenuItem clearMenu = new MenuItem("Clear");
        clearMenu.setOnAction(actionEvent -> {
            textAreaCode.clear();
        });

        MenuItem pasteMenu = new MenuItem("Paste");
        pasteMenu.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteMenu.setOnAction(actionEvent -> textAreaCode.replaceText(0,0,clipBoard));

        contextMenu.getItems().addAll(runMenu, pasteMenu,clearMenu);
        textAreaCode.setContextMenu(contextMenu);
    }
    private void setUpCommandHistoryMenu() {

        ContextMenu contextMenu = new ContextMenu();


        MenuItem clearMenu = new MenuItem("Clear History");
        clearMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        Image clearIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/delete.png")));
        ImageView clearView = new ImageView(clearIcon);
        clearView.setFitWidth(20);
        clearView.setFitHeight(20);
        clearMenu.setGraphic(clearView);
        clearMenu.setOnAction(actionEvent -> {
            try {
                CommandHistoryDatabaseHelper.clearCommandHistory();
                //lblCommandHistoryState.setVisible(true);
                 tableCommandHistory.getItems().clear();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        contextMenu.getItems().addAll(clearMenu);
        tableCommandHistory.setContextMenu(contextMenu);
    }

    public void runQuery(ActionEvent actionEvent) throws SQLException, InterruptedException {
        runProgram();
    }

    private void setUpListView(){
        databaseTreeView.setPadding(new Insets(20,20,20,20));

        historyListView.setItems(activateDbHistoryObservableList);
        historyListView.setCellFactory(e -> new ListItemAdapter());
    }

    private void setUpLineNumberList(){

    }

    private void textAreaListener(){

    }

    private void monitorTextChange(){

    }

    private String TEMP_TABLE_NAME = "";
    private static void extracted(String queryString) {
        if(queryString.toLowerCase().contains("insert")) QUERY_TYPE = HelperClass.INSERT;
        else if(queryString.toLowerCase().contains("delete")) QUERY_TYPE = HelperClass.DELETE;
        else if(queryString.toLowerCase().contains("update")) QUERY_TYPE = HelperClass.UPDATE;
        else if(queryString.toLowerCase().contains("alter")) QUERY_TYPE = HelperClass.ALTER;
        else if(queryString.toLowerCase().contains("create")) QUERY_TYPE = HelperClass.CREATE;
        else if(queryString.toLowerCase().contains("drop")) QUERY_TYPE = HelperClass.DROP;
        else QUERY_TYPE = HelperClass.SELECT;
    }
    private void runProgram() throws SQLException, InterruptedException {
        try {
            WriteQueryDatabaseHelper.executeCode(textAreaCode.getSelectedText());
            extracted(textAreaCode.getSelectedText());
            String[] columnNames = WriteQueryDatabaseHelper.COLUMN_NAMES.toArray(new String[0]);
            if(WriteQueryDatabaseHelper.opType){


            }else{
                TEMP_TABLE_NAME = WriteQueryDatabaseHelper.QUERY_TABLE_NAME;
                dataTable.getItems().clear();
                dataTable.getColumns().clear();

                for (int i = 0; i < columnNames.length; i++) {
                    TableColumn<String[], String> column = new TableColumn<>(columnNames[i]);
                    int finalI = i;
                    column.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[finalI]));
                    dataTable.getColumns().add(column);
                }
                List<List<String>> dataRows = WriteQueryDatabaseHelper.DATA_ROWS;
                for (List<String> dataRow : dataRows) {
                    String[] row = dataRow.toArray(new String[0]);
                    dataTable.getItems().add(row);
                }
                resultTabPane.getSelectionModel().select(0);

                ObservableList<Tab> tabs = resultTabPane.getTabs();
                tabs.get(0).setText("Result");
            }
            if(textAreaCode.getSelectedText().toLowerCase().contains("drop")) refreshTreeView();
            commandState = 1;
        }
        catch(SQLException | ClassNotFoundException e){

            errorTextArea.lookup(".content").setStyle("-fx-background-color: #332b33;");
            errorTextArea.setStyle("-fx-text-fill: red;");
            resultTabPane.getSelectionModel().select(1);
            String message = e.toString();
            errorTextArea.setText(message);
            WriteQueryDatabaseHelper.QUERY_TYPE = message;
            commandState = 0;
            QUERY_TYPE = "Operation unsuccessful!";
        }
        updateCommandsList();
    }

    public void executeProgram(MouseEvent mouseEvent) throws SQLException, InterruptedException {
        runProgram();
    }

    public void toggleBackgroundColor(MouseEvent mouseEvent) {
        Scene scene = mainWindowParent.getScene();
        Parent root = scene.getRoot();
        if(!toggleColor){
            root.getStylesheets().clear();
            root.getStylesheets().add(Objects.requireNonNull
                    (getClass().getResource("../resources/blacktheme.css")).toExternalForm());
//            scrollCodeNumber.getStylesheets().clear();
//            scrollCodeNumber.getStylesheets()
//                    .add(String.valueOf(getClass().getResource("../resources/scroll_numbering_white.css")));
            toggleColor = true;
        }
        else{
            root.getStylesheets().clear();
            root.getStylesheets().add(Objects.requireNonNull
                    (getClass().getResource("../resources/application.css")).toExternalForm());
//            scrollCodeNumber.getStylesheets().clear();
//            scrollCodeNumber.getStylesheets()
//                    .add(String.valueOf(getClass().getResource("../resources/scroll_numbering_dark.css")));
            toggleColor = false;
        }
    }

//    public void checkKey3(KeyEvent keyEvent) {
////        if(keyEvent.getCode() == KeyCode.ENTER){
////            scrollCodeNumber.setScrollTop((int) Double.MAX_VALUE);
////        }
//    }

    public void updateCommandsList() throws SQLException, InterruptedException {
        LocalDateTime d = LocalDateTime.now();
        String mydate = d.getHour() + ":" + d.getMinute() + ":" + d.getSecond();
        CommandHistoryDatabaseHelper.saveCommand(HelperClass.commandHistoryTable,
                QUERY_TYPE, textAreaCode.getSelectedText(), commandState,mydate);
        lblRightStatus.setText(QUERY_TYPE);
        getCommandHistory();

    }

    public void getCommandHistory() throws SQLException {
        if(tableCommandHistory.getItems().size() > 0) tableCommandHistory.getItems().clear();
        ResultSet commandResultSet = CommandHistoryDatabaseHelper.getCommand();
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colQuery.setCellValueFactory(new PropertyValueFactory<>("query"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        int count = 0;
        while(commandResultSet.next()){
            ImageView statusImage = new ImageView();
            if(commandResultSet.getInt("status")== 1){
                statusImage.setImage(new Image(Objects.requireNonNull
                        (getClass().getResourceAsStream("../resources/tick.png"))));
            }else{
                statusImage.setImage(new Image(Objects.requireNonNull
                        (getClass().getResourceAsStream("../resources/cross.png"))));
            }

            tableCommandHistory.getItems().add(new CommandHistory(commandResultSet.getString("message"),
                                                commandResultSet.getString("query"),
                                                statusImage, commandResultSet.getString("date")));
            count++;

        }
        tableCommandHistory.setPlaceholder(new Label("No command history available"));
        tableCommandHistory.scrollTo((int) Double.MAX_VALUE);
        tableCommandHistory.getSelectionModel().select(count-1);
    }

    public void addDatabase(MouseEvent mouseEvent) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../views/dialog.fxml"));
        Parent parent = loader.load();
        DialogController dialogController = loader.getController();

        Scene scene = new Scene(parent);
        Stage stage = new Stage();

        dialogController.btnAdd.setOnAction(actionEvent -> {
            AddDatabaseHelper.initDB(databasePath, dialogController.textAreaDBName.getText());

            lblRightStatus.setText(dialogController.textAreaDBName.getText() + " database has been added successfully!");
            List<File> allFiles = Arrays.asList(Objects.requireNonNull(new File(databasePath).listFiles()));

            List<File> collectDB = allFiles.stream()
                    .filter(File::isFile)
                    .filter(file -> file.toString().endsWith(".db"))
                    .collect(Collectors.toList());
            new LoadingProgress(collectDB, null, true).start();
            isSelectedFromList = true;
            //refreshTreeView();
            stage.close();
        });

        dialogController.btnCancel.setOnAction(actionEvent -> {
            stage.close();
        });

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void refreshTreeView(MouseEvent mouseEvent) {
    }
    public void refreshTreeView(){
        //if(databaseTreeView.getRoot() != null) databaseTreeView.getRoot().getChildren().clear();
        File file = new File(databasePath);
        if(!observableList.contains(databasePath)){
            try {
                extractedFilesAndBuild(file, file, true);

            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
        }else{

            try {
                extractedFilesAndBuild(file, file, false);
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
        }
    }
    public void deleteTreeItem(MouseEvent mouseEvent) throws SQLException, ClassNotFoundException, InterruptedException {
        File file = new File(lblLeftStatus.getText());
        if(file.exists()) {
            boolean delete = file.delete();
            ConnectedDBHistory.clearCommandHistory(databaseToBeDeleted);
            activateDbHistoryObservableList.clear();
            ResultSet command;
            command = ConnectedDBHistory.getCommand2();
            while(command.next()){
                activateDbHistoryObservableList.add(new ActivateDBHistory(command.getInt("id"), command.getString("connString"),
                        command.getString("dbName"), command.getString("time"), command.getString("selected")));
            }
           refreshTreeView();
        }
    }

    public void refreshDB(MouseEvent event) throws SQLException, ClassNotFoundException {
        extractedFilesAndBuild(new File(databasePath), new File(databasePath), false);
    }
}
