package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.POIWorkbook;
import edu.stevens.spreadSheet.model.TableCellModel;
import edu.stevens.spreadSheet.model.TableRowModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellReference;

import java.util.Objects;


public class TableController {

    private ObservableList<TableRowModel> tableRows;

    POIWorkbook workbook;

    @FXML
    private TableView<TableRowModel> table;
    @FXML
    private TableColumn<TableRowModel, String> rowIDColumn;
    @FXML
    private TextField formulaBar;
    final SimpleStringProperty formulaBarDisplay = new SimpleStringProperty();
    @FXML
    private Button buttonInsertRow;
    @FXML
    private Button buttonInsertColumn;

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRowModel, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCellOrCreateEmpty(index - 1).getValueStringProperty());
        column.setCellFactory(p -> new EditableTableCell<>());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRowModel, String> t) -> {
            int rowID = t.getTablePosition().getRow();
            int colID = t.getTablePosition().getColumn();
            assert colID != 0 : "The first column should not be edited.";
            var newValue = t.getNewValue();
            setCellContent(rowID, colID - 1, newValue);
            /* Update the formula bar */
            formulaBarDisplay.set(newValue);
        });
        column.setPrefWidth(75);
        column.setSortable(false);
        column.setReorderable(false); // Column drag and drop is disabled for now
        table.getColumns().add(column);
    }

    @FXML
    public void initialize() {
        // config table
        tableRows = FXCollections.observableArrayList();
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(tableRows);
        /*
         * Let the formula bar update cell only with user's input.
         * trick: https://stackoverflow.com/questions/28421122
         */
        formulaBarDisplay.addListener((observable, oldVal, newVal) -> formulaBar.setText(newVal));
    }

    void drawRowIDColumn(int numRow) {
        rowIDColumn.setCellValueFactory(p -> p.getValue().getRowIDProperty());
        rowIDColumn.setCellFactory(p -> {
            var cell = new TableCell<TableRowModel, String>() {
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
        for (int r = 1; r <= sheet.getLastRowNum() + 1; r++) {
            var row = sheet.getRow(r - 1);
            tableRows.add(new TableRowModel(row, r));
            maxColumnNum = Math.max(row.getLastCellNum(), maxColumnNum);
        }
        /* create columns */
        drawColumns(maxColumnNum);
    }

    private void drawColumns(int columnNum) {
        drawRowIDColumn(workbook.getCurrentSheet().getLastRowNum());
        for (int c = 1; c <= columnNum; c++) {
            var columnName = CellReference.convertNumToColString(c - 1);
            addColumn(columnName, c);
        }
    }

    public void setWorkbook(POIWorkbook workbook) {
        this.workbook = workbook;
        drawSheet();
        /* Delay the initialization here to make sure all cells are initialized. */
        /* Update cell content when editing formula bar */
        formulaBar.textProperty().addListener((observable, oldVal, newVal) -> {
            var pos = table.getFocusModel().getFocusedCell();
            if (!newVal.equals(formulaBarDisplay.get())) {
                setCellContent(pos.getRow(), pos.getColumn(), newVal);
            }
        });
        /* display content on formula bar when the cell is selected */
        table.getFocusModel().focusedCellProperty().addListener((observable, oldPos, newPos) -> {
            if ((newPos.getRow() != -1) && (newPos.getColumn() > 0)) {
                // TODO: conditional display formula
                var selectedValue = getCellContent(newPos.getRow(), newPos.getColumn() - 1);
                formulaBarDisplay.set(selectedValue);
            }
        });
    }

    void showAlert(String header, String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    void evaluateAll() {
        workbook.evaluateAll();
        // update all cells
        for (var row : tableRows) {
            for (int c = 0; c < row.getNumberOfCells(); c++) {
                row.getCell(c).updateFromCell();
            }
        }
    }

    public void setCellContent(int rowID, int colID, String content) {
        try {
            getCell(rowID, colID).setValue(content);
            evaluateAll();
        } catch (FormulaParseException e) {
            showAlert("Invalid formula", content + " is an invalid formula");
        }
    }

    public String getCellContent(int rowID, int colID) {
        return getCell(rowID, colID).getValueString();
    }

    private TableCellModel getCell(int rowID, int colID) {
        return tableRows.get(rowID).getCell(colID);
    }

    @SuppressWarnings({"rawtypes"})
    private TablePosition getFocusedCellPos() {
        /*
         * Using table.getFocusModel().focusedCellProperty().get(); will return -1 when column is inserted.
         * Current call will clear the focus after column insertion
         */
        var pos = table.getFocusModel().getFocusedCell();
        if (pos.getRow() < 0 || pos.getColumn() < 0) {
            return null;
        } else {
            return pos;
        }
    }

    public void insertRow() {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int insertPos = focusedPos.getRow() + 1;
        var newRow = workbook.insertRow(insertPos);
        tableRows.add(insertPos, new TableRowModel(newRow, insertPos + 1));
        for (int r = insertPos + 1; r < tableRows.size(); r++) {
            tableRows.get(r).getCell(0).setValueString(String.valueOf(r + 1));
        }
    }

    public void insertColumn() {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int insertPos = focusedPos.getColumn(); // insertPos is the one after the focused
        var addedCells = workbook.insertColumn(insertPos);
        for (int r = 0; r < tableRows.size(); r++) {
            tableRows.get(r).addCell(new TableCellModel(addedCells.get(r)), insertPos);
        }
        int numColumns = table.getColumns().size();
        table.getColumns().clear();
        /* Redraw all columns */
        table.getColumns().add(rowIDColumn);
        drawColumns(numColumns);
    }
}


