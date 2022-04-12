package edu.stevens.spreadSheet.model;

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

}


