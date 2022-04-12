package edu.stevens.spreadSheet.view;

import edu.stevens.spreadSheet.model.POIWorkbook;
import edu.stevens.spreadSheet.model.TableRow;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.apache.poi.ss.util.CellReference;


public class TableController {

    private ObservableList<TableRow> tableRows;

    POIWorkbook workbook;

    @FXML
    private TableView<TableRow> table;

    private TableColumn<TableRow, ?> getColumns(int index) {
        return table.getColumns().get(index);
    }

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRow, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCellOrCreateEmpty(index).getValueStringProperty());
        column.setCellFactory(p -> new EditableStringTableCell<>());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRow, String> t) -> {
                    int rowID = t.getTablePosition().getRow();
                    int colID = t.getTablePosition().getColumn() - 1; // The first column is not about data
                    var newValue = t.getNewValue();
                    setCell(rowID, colID, newValue);
                }
        );
        table.getColumns().add(column);
    }

    @FXML
    public void initialize() {
        // config table
        tableRows = FXCollections.observableArrayList();
        this.table.getSelectionModel().setCellSelectionEnabled(true);
        this.table.setItems(this.tableRows);
    }

    void drawTable() {
        var sheet = workbook.getCurrentSheet();
        int maxColumnNum = 0;
        /* create rows */
        for (int r = 0; r < sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            tableRows.add(new TableRow(row));
            maxColumnNum = Math.max(row.getLastCellNum(), maxColumnNum);
        }
        /* create columns */
        for (int c = 0; c < maxColumnNum; c++) {
            var columnName = CellReference.convertNumToColString(c);
            addColumn(columnName, c);
        }
    }


    public void setWorkbook(POIWorkbook workbook) {
        this.workbook = workbook;
        drawTable();
    }

    public void setCell(int rowID, int colID, String content) {
        tableRows.get(rowID).getCell(colID).setValue(content);
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