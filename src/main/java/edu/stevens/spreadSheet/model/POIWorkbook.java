package edu.stevens.spreadSheet.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

public class POIWorkbook {
    final Workbook workbook;
    final FormulaEvaluator formulaEvaluator;
    Sheet currentSheet;

    public POIWorkbook(Workbook workbook) {
        this.workbook = workbook;
        this.formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
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
        int currentCellNum = currentSheet.getRow(0).getLastCellNum();
        assert pos <= currentCellNum : "Inserted column must be neighbors of existing columns";
        if (currentCellNum > pos) {
            currentSheet.shiftColumns(pos, currentCellNum, 1);
        }
        List<Cell> addedCells = new ArrayList<>();
        for (var row : currentSheet) {
            var cell = row.createCell(pos);
            addedCells.add(cell);
        }
        return addedCells;
    }

    public void evaluateAll() {
        formulaEvaluator.clearAllCachedResultValues();
        formulaEvaluator.evaluateAll();
    }

    public FormulaEvaluator getEvaluator() {
        return formulaEvaluator;
    }
}


