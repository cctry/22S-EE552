package edu.stevens.spreadSheet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.poi.ss.usermodel.Row;

public class TableRowModel {
    private final ObservableList<TableCellModel> cells = FXCollections.observableArrayList();
    private final SimpleStringProperty rowID = new SimpleStringProperty();

    public TableRowModel(Row POIRow, int rowID) {
        this.rowID.set(String.valueOf(rowID));
        for (int i = 0; i < POIRow.getLastCellNum(); i++) {
            cells.add(new TableCellModel(POIRow.getCell(i)));
        }
    }

    public int getNumberOfCells() {
        return cells.size();
    }

    public TableCellModel getCell(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        return cells.get(index);
    }

    public SimpleStringProperty getRowIDProperty() {
        return rowID;
    }

    public void addCell(TableCellModel cell, int index) {
        cells.add(index, cell);
    }

    public void setRowID(int i) {
        rowID.set(String.valueOf(i));
    }

    public void removeCell(int i) {
        cells.remove(i);
    }
}
