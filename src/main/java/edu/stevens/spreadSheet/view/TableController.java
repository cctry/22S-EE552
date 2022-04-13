package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.POIWorkbook;
import edu.stevens.spreadSheet.model.TableCellModel;
import edu.stevens.spreadSheet.model.TableRowModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML
    private Button buttonInsertRow;
    @FXML
    private Button buttonInsertColumn;

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRowModel, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCellOrCreateEmpty(index).getValueStringProperty());
        column.setCellFactory(p -> new EditableTableCell<>());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRowModel, String> t) -> {
            int rowID = t.getTablePosition().getRow();
            int colID = t.getTablePosition().getColumn();
            assert colID != 0 : "The first column should not be edited.";
            var newValue = t.getNewValue();
            setCellContent(rowID, colID, newValue);
            /* Update the formula bar */
            formulaBar.setText(newValue);
        });
        column.setPrefWidth(75);
        column.setSortable(false);
        column.setReorderable(false); // TODO: Column drag and drop is disabled for now
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
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            tableRows.add(new TableRowModel(row, r));
            maxColumnNum = Math.max(row.getLastCellNum(), maxColumnNum);
        } // FIXME: Drawn number of rows is incorrect.
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

    private TablePosition getFocusedCellPos() {
        var pos = table.getFocusModel().focusedCellProperty().get();
        if (pos.getRow() < 0 || pos.getColumn() < 0) {
            return null;
        } else {
            return pos;
        }
    }

    public void insertRow() {
        var focusedPos = getFocusedCellPos();
        int insertPos = Objects.requireNonNull(focusedPos).getRow() + 1;
        var newRow = workbook.insertRow(insertPos);
        tableRows.add(insertPos, new TableRowModel(newRow, insertPos + 1));
        for (int r = insertPos + 1; r < tableRows.size(); r++) {
            tableRows.get(r).getCell(0).setValueString(String.valueOf(r + 1));
        }
    }

    public void insertColumn() {
        var focusedPos = getFocusedCellPos();
        int insertPos = Objects.requireNonNull(focusedPos).getColumn() + 1;
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


