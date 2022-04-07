package edu.stevens.spreadSheet.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TableRow {
    private final int numberOfCells;
    private final ObservableList<TableCell> cells;

    public TableRow(int numberOfCells) {
        this.cells = FXCollections.observableArrayList();
        this.numberOfCells = numberOfCells;
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCell(""));
        }
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public TableCell getCell(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        if (index >= numberOfCells) {
            return new TableCell("");
        }
        return cells.get(index);
    }

    public void setCellContent(int index, String value) {
        if (index < 0 || index >= numberOfCells) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        cells.get(index).setValueString(value);
    }

    public ObservableList<TableCell> getAllCells() {
        return cells;
    }
}
