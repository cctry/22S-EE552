package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.POIWorkbook;
import edu.stevens.spreadSheet.model.TableRow;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.poi.ss.util.CellReference;


public class TableController {

    private ObservableList<TableRow> tableRows;

    POIWorkbook workbook;

    @FXML
    private TableView<TableRow> table;
    @FXML
    TableColumn<TableRow, String> rowIDColumn;
    @FXML
    TextField formulaBar;

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRow, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCellOrCreateEmpty(index).getValueStringProperty());
        column.setCellFactory(p -> new EditableTableCell<>());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRow, String> t) -> {
            int rowID = t.getTablePosition().getRow();
            int colID = t.getTablePosition().getColumn();
            assert colID != 0 : "The first column should not be edited.";
            var newValue = t.getNewValue();
            setCellContent(rowID, colID, newValue);
        });
        column.setSortable(false);
        table.getColumns().add(column);
    }

    @FXML
    public void initialize() {
        // config table
        tableRows = FXCollections.observableArrayList();
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(tableRows);
    }

    void drawRowIDColumn(int numRow) {
        rowIDColumn.setCellValueFactory(p -> p.getValue().getCellOrCreateEmpty(0).getValueStringProperty());
        rowIDColumn.setCellFactory(p -> {
            var cell = new TableCell<TableRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.getStyleClass().clear();
            cell.getStyleClass().add("rowid-cell");
            return cell;
        });
    }

    void drawSheet() {
        var sheet = workbook.getCurrentSheet();
        int maxColumnNum = 0;
        /* create rows */
        for (int r = 0; r < sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            tableRows.add(new TableRow(row, r));
            maxColumnNum = Math.max(row.getLastCellNum(), maxColumnNum);
        } // FIXME: Drawn number of rows is incorrect.
        /* create columns */
        drawRowIDColumn(sheet.getLastRowNum());
        for (int c = 1; c <= maxColumnNum; c++) {
            var columnName = CellReference.convertNumToColString(c - 1);
            addColumn(columnName, c);
        }
    }

    public void setWorkbook(POIWorkbook workbook) {
        this.workbook = workbook;
        drawSheet();
        /* Delay the initialization here to make sure all cells are initialized. */
        /* Update cell content when editing formula bar */
        formulaBar.textProperty().addListener((obs, oldText, newText) -> {
            var pos = table.getFocusModel().getFocusedCell();
            if (newText.length() > 0) {
                setCellContent(pos.getRow(), pos.getColumn(), newText);
            }
        });
        /* display content on formula bar when the cell is selected */
        table.getFocusModel().focusedCellProperty().addListener((observable, oldPos, pos) -> {
            if ((pos.getRow() != -1) && (pos.getColumn() != -1)) {
                var selectedValue = getCellContent(pos.getRow(), pos.getColumn());
                formulaBar.setText(selectedValue);
            }
        });
    }

    public void setCellContent(int rowID, int colID, String content) {
        tableRows.get(rowID).getCell(colID).setValue(content);
    }

    public String getCellContent(int rowID, int colID) {
        return tableRows.get(rowID).getCell(colID).getValueString();
    }
}


