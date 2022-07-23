package main_window;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import models.ActivateDBHistory;

import java.io.IOException;

public class ListItemAdapter extends ListCell<ActivateDBHistory> {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label lblListItemDBName;
    @FXML
    private Label lblListItemTime;
    @FXML
    private Pane paneRed;
    @FXML
    private Pane paneGreen;
    @FXML
    private Label lblListItemConnString;
    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(ActivateDBHistory activateDbHistory, boolean b) {
        super.updateItem(activateDbHistory, b);
        if(b || activateDbHistory == null) {

            setText(null);
            setGraphic(null);

        }else{

                if (mLLoader == null) {
                    mLLoader = new FXMLLoader(getClass().getResource("../views/history_list_cell.fxml"));
                    mLLoader.setController(this);

                    try {
                        mLLoader.load();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                lblListItemDBName.setText(activateDbHistory.getDatabaseName());
                lblListItemTime.setText(activateDbHistory.getConnectionTime());
                lblListItemConnString.setText(activateDbHistory.getConnectionString());
                if(activateDbHistory.getSelected().equals("0")) {
                    paneRed.setVisible(true);
                    paneGreen.setVisible(false);
                }else{
                    paneRed.setVisible(false);
                    paneGreen.setVisible(true);
                }
                setText(null);
                setGraphic(anchorPane);
        }

    }
}
