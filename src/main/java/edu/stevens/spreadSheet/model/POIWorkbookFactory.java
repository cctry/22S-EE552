package edu.stevens.spreadSheet.model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class POIWorkbookFactory {
    public static final int defaultColumnNum = 10;
    public static final int defaultRowNum = 20;

    public static POIWorkbook emptyWorkbook(int nrow, int ncol) {
        var workbook = new HSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");
        var workbookProxy = new POIWorkbook(workbook);
        workbookProxy.setCurrentSheet(sheet);
        for (int r = 0; r < nrow; r++) {
            var row = sheet.createRow(r);for (int c = 0; c < ncol; c++) {
                row.createCell(c);
            }
        }
        return workbookProxy;
    }

    public static POIWorkbook fromExcelFile(File excelFile) throws IOException {
        var workbook = WorkbookFactory.create(excelFile);
        var workbookProxy = new POIWorkbook(workbook);
        for (var sheet : workbook) {
            // pad rows before
            for (int r = 0; r < Math.max(defaultRowNum, sheet.getLastRowNum() + 1); r++) {
                if (sheet.getRow(r) == null) {
                    sheet.createRow(r);
                }
            }
            workbookProxy.setCurrentSheet(sheet);
            int maxColumnNum = Math.max(defaultColumnNum, workbookProxy.maxColumnNum());
            for (var row : sheet) {
                for (int c = 0; c < maxColumnNum; c++) {
                    if (row.getCell(c) == null) {
                        row.createCell(c);
                    }
                }
            }
        }
        workbookProxy.setCurrentSheet(workbook.getSheetAt(0));
        return workbookProxy;
    }

    public static POIWorkbook fromCSVFile(File CSVFile) throws IOException, CsvException {
        var reader = new FileReader(CSVFile);
        var csvReader = new CSVReader(reader);
        var records = csvReader.readAll();
        var maxColumnNum = records.parallelStream().map(record -> record.length).max(Integer::compare).orElse(null);
        if (maxColumnNum == null) {
            throw new CsvException("Unable to fine the widest row.");
        }
        int numberOfRow = Math.max(records.size(), defaultRowNum);
        int numberOfColumn = Math.max(maxColumnNum, defaultColumnNum);
        var workbookProxy = emptyWorkbook(numberOfRow, numberOfColumn);
        var sheet = workbookProxy.getCurrentSheet();
        for (int r = 0; r < records.size(); r++) {
            var record = records.get(r);
            for (int c = 0; c < record.length; c++) {
                var cell = sheet.getRow(r).getCell(c);
                var entry = record[c];
                // parse the entry and store
                if (entry.length() == 0) {
                    cell.setBlank();
                } else if (NumberUtils.isParsable(entry)) {
                    cell.setCellValue(Double.parseDouble(entry));
                } else if ("false".equalsIgnoreCase(entry) || "true".equalsIgnoreCase(entry)) {
                    cell.setCellValue(Boolean.parseBoolean(entry));
                } else {
                    cell.setCellValue(entry);
                }
            }
        }
        return workbookProxy;
    }
}
