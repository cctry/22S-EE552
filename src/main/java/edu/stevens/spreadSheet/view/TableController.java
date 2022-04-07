package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.TableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

class ColumnLabelGenerator {
    static String generate(int index) {
        assert index >= 0 && index < 26;
        return (char) ('A' + index) + "";
    }
}

public class TableController {

    private final int initNumberCol = 8;
    private final int initNumberRow = 50;
    private int numRows;
    private int numColumns;

    private ObservableList<TableRow> tableRows;

    @FXML
    private TableView<TableRow> table;


    TableColumn<TableRow, ?> getColumn(int index) {
        return table.getColumns().get(index);
    }

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRow, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCell(index).getValueStringProperty());
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRow, String> t) -> {
                    int row_id = t.getTablePosition().getRow();
                    int col_id = t.getTablePosition().getColumn();
                    var new_value = t.getNewValue();
                    t.getTableView().getItems().get(row_id)
                            .setCellContent(col_id, new_value);
                }
        ); // TODO: Require Enter to confirm
        table.getColumns().add(column);
    }

    @FXML
    public void initialize() {
        this.numRows = initNumberRow;
        this.numColumns = initNumberCol;
        this.tableRows = FXCollections.observableArrayList();
        for (int i = 0; i < initNumberRow; i++) {
            var row = new TableRow(initNumberCol);
            this.tableRows.add(row);
        }
        // add initial columns
        for (int i = 0; i < initNumberCol; i++) {
            var name = ColumnLabelGenerator.generate(i);
            addColumn(name, i);
        }
        // add all cells from tableRows
        this.table.setItems(this.tableRows);
        this.table.setEditable(true);


    }

    public void setCell(int row_id, int col_id, String content) {
        if (col_id >= numColumns || row_id >= numRows) {
            throw new IndexOutOfBoundsException("Set out-of-bound.");
        }
        var row = this.tableRows.get(row_id);
        row.setCellContent(col_id, content);
    }

    public ObservableList<TableRow> getTableRows() {
        return this.tableRows;
    }
}
