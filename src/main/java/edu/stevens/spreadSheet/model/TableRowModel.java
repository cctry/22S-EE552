package edu.stevens.spreadSheet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.poi.ss.usermodel.Row;

public class TableRowModel {
    private final ObservableList<TableCellModel> cells;
    private Row POIRow = null;
    private final SimpleStringProperty rowID = new SimpleStringProperty("0");

    public TableRowModel(int numberOfCells) {
        this.cells = FXCollections.observableArrayList();
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCellModel(""));
        }
    }

    public TableRowModel(Row POIRow, int rowID) {
        this.rowID.set(String.valueOf(rowID));
        this.POIRow = POIRow;
        this.cells = FXCollections.observableArrayList();
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

    public TableCellModel getCellOrCreateEmpty(int index) {
        for (int i = 0; i <= index - getNumberOfCells(); i++) {
            var cell = POIRow.createCell(i);
            cells.add(new TableCellModel(cell));
        }
        return getCell(index);
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
