package edu.stevens.spreadSheet.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.poi.ss.usermodel.Row;

public class TableRowModel {
    private final int numberOfCells;
    private final ObservableList<TableCellModel> cells;
    private Row POIRow = null;
    private int rowID = 0;

    public TableRowModel(int numberOfCells) {
        this.cells = FXCollections.observableArrayList();
        this.numberOfCells = numberOfCells;
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCellModel(""));
        }
    }

    public TableRowModel(Row POIRow, int rowID) {
        this.rowID = rowID;
        this.POIRow = POIRow;
        this.cells = FXCollections.observableArrayList();
        this.numberOfCells = POIRow.getLastCellNum();
        cells.add(new TableCellModel(String.valueOf(rowID)));
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCellModel(POIRow.getCell(i)));
        }
    }

    public int getRowID() {
        return this.rowID;
    }

    public void setRowID(int rowID) {
        this.rowID = rowID;
        getCell(0).setValueString(String.valueOf(rowID));
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public TableCellModel getCell(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        return cells.get(index);
    }

    public TableCellModel getCellOrCreateEmpty(int index) {
        if (index >= numberOfCells) {
            for (int i = 0; i <= index - numberOfCells; i++) {
                var cell = POIRow.createCell(i);
                cells.add(new TableCellModel(cell));
            }
        }
        return getCell(index);
    }

    public void addCell(TableCellModel cell) {
        cells.add(cell);
    }

    public void addCell(TableCellModel cell, int index) {
        cells.add(index, cell);
    }
}
