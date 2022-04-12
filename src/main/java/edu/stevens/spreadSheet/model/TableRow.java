package edu.stevens.spreadSheet.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.Row;

public class TableRow {
    private final int numberOfCells;
    private final ObservableList<TableCell> cells;
    private Row POIRow = null;

    public TableRow(int numberOfCells) {
        this.cells = FXCollections.observableArrayList();
        this.numberOfCells = numberOfCells;
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCell(""));
        }
    }

    public TableRow(Row POIRow) {
        this.POIRow = POIRow;
        this.cells = FXCollections.observableArrayList();
        this.numberOfCells = POIRow.getLastCellNum();
        for (int i = 0; i < numberOfCells; i++) {
            cells.add(new TableCell(POIRow.getCell(i)));
        }
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public TableCell getCell(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        return cells.get(index);
    }

    public ObservableList<TableCell> getAllCells() {
        return cells;
    }

    public TableCell getCellOrCreateEmpty(int index) {
        if (index >= numberOfCells) {
            for (int i = 0; i <= index - numberOfCells; i++) {
                var cell = POIRow.createCell(i);
                cells.add(new TableCell(cell));
            }
        }
        return getCell(index);
    }
}
