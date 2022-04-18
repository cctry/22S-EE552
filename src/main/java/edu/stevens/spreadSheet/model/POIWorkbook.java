package edu.stevens.spreadSheet.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class POIWorkbook {
    final Workbook workbook;
    Sheet currentSheet;

    public POIWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public void setCurrentSheet(Sheet sheet) {
        currentSheet = sheet;
    }

    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public Row insertRow(int pos) {
        if (currentSheet.getLastRowNum() >= pos) {
            currentSheet.shiftRows(pos, currentSheet.getLastRowNum(), 1, true, true);
        }
        var row = currentSheet.createRow(pos);
        for (int c = 0; c < currentSheet.getRow(pos - 1).getLastCellNum(); c++) {
            row.createCell(c);
        }
        return row;
    }

    public List<Cell> insertColumn(int pos) {
        if (currentSheet.getRow(0).getLastCellNum() >= pos) {
            currentSheet.shiftColumns(pos, currentSheet.getRow(0).getLastCellNum(), 1);
        }
        List<Cell> addedCells = new ArrayList<>();
        for (var row : currentSheet) {
            var cell = row.createCell(pos);
            addedCells.add(cell);
        }
        return addedCells;
    }

}


