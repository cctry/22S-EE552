package edu.stevens.spreadSheet.model;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class POIWorkbookFactory {
    public static POIWorkbook emptyWorkbook(int nrow, int ncol) {
        var workbook = new HSSFWorkbook();
        var sheet = workbook.createSheet("new sheet");
        var workbookProxy = new POIWorkbook(workbook);
        workbookProxy.setCurrentSheet(sheet);
        for (int r = 0; r < nrow; r++) {
            var row = sheet.createRow(r);
            for (int c = 0; c < ncol; c++) {
                var cell = row.createCell(c);
                cell.setBlank();
            }
        }
        return workbookProxy;
    }
}
