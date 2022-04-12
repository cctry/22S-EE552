package edu.stevens.spreadSheet.view;

import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class EditableTableCell<T> extends TableCell<T, String> {


    protected TextField textField;
    protected final ChangeListener<? super Boolean> changeListener = (obs, ov, nv) -> {
        if (!nv) {
            commitEdit(textField.getText());
        }
    };

    public EditableTableCell() {
        getStyleClass().clear();
        getStyleClass().add("editable-cell");
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
        textField.getStyleClass().add("editable-cell");
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
