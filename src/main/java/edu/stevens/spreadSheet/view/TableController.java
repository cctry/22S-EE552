package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.TableRow;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

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


    private TableColumn<TableRow, ?> getColumn(int index) {
        return table.getColumns().get(index);
    }

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRow, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCell(index).getValueStringProperty());
        column.setCellFactory(p -> new EditableStringTableCell<>());
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
        // config table
        this.table.getSelectionModel().setCellSelectionEnabled(true);
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


class EditableStringTableCell<T> extends TableCell<T, String> {
    protected TextField textField;
    protected final ChangeListener<? super Boolean> changeListener = (obs, ov, nv) -> {
        if (!nv) {
            commitEdit(textField.getText());
        }
    };

    public EditableStringTableCell() {

    }

    @Override
    public void startEdit() {
        if (editableProperty().get()) {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.requestFocus();
            }
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                    textField.selectAll();
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    protected void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener(changeListener);
        textField.setOnAction(evt -> commitEdit(textField.getText()));

        textField.setOnKeyPressed((ke) -> {
            if (ke.getCode().equals(KeyCode.ESCAPE)) {
                textField.focusedProperty().removeListener(changeListener);
                cancelEdit();
            }
            if (ke.getCode().equals(KeyCode.TAB)) {
                commitEdit(textField.getText());
            }
        });
    }


    protected String getString() {
        return getItem() == null ? "" : getItem();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void commitEdit(String item) {
        textField.focusedProperty().removeListener(changeListener);
        if (isEditing()) {
            super.commitEdit(item);
        } else {
            final TableView table = getTableView();
            if (table != null) {
                TablePosition position = new TablePosition(getTableView(),
                        getTableRow().getIndex(), getTableColumn());
                TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(table, position,
                        TableColumn.editCommitEvent(), item);
                Event.fireEvent(getTableColumn(), editEvent);
            }
            updateItem(item, false);
            if (table != null) {
                table.edit(-1, null);
            }

        }
    }

}