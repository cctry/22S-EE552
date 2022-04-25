package edu.stevens.spreadSheet.model;

import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import static org.apache.poi.ss.usermodel.CellType.FORMULA;

public class TableCellModel {
    private final SimpleStringProperty valueString;
    private Cell POICell = null;

    public TableCellModel(String value) {
        this.valueString = new SimpleStringProperty(value);
    }

    public TableCellModel(Cell POICell) {
        this.POICell = POICell;
        assert !DateUtil.isCellDateFormatted(POICell) : "Date value is not supported.";
        this.valueString = new SimpleStringProperty(getStringFromCell(POICell.getCellType()));
    }

    String getStringFromCell(CellType cellType) {
        return switch (cellType) {
            case STRING -> POICell.getRichStringCellValue().getString();
            case NUMERIC -> String.valueOf(POICell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(POICell.getBooleanCellValue()).toUpperCase();
            case FORMULA -> getStringFromCell(POICell.getCachedFormulaResultType());
            default -> "";
        };
    }


    public void setValue(String value) {
        if (value.length() == 0) {
            POICell.setBlank();
        } else if (NumberUtils.isParsable(value)) {
            POICell.setCellValue(Double.parseDouble(value));
        } else if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            POICell.setCellValue(Boolean.parseBoolean(value));
        } else if (value.startsWith("=")) {
            POICell.setCellFormula(value.substring(1));
        } else {
            POICell.setCellValue(value);
        }
        setValueString(getStringFromCell(POICell.getCellType()));
    }

    public boolean isFormulaCell() {
        return POICell.getCellType() == FORMULA;
    }

    public void updateFromCell() {
        setValueString(getStringFromCell(POICell.getCellType()));
    }

    public String getValueString() {
        updateFromCell();
        return valueString.get();
    }

    private void setValueString(String value) {
        valueString.set(value);
    }

    public SimpleStringProperty getValueStringProperty() {
        return valueString;
    }

    public String toString() {
        if (POICell == null) {
            return "Invalid cell";
        }
        String valueString;
        switch (POICell.getCellType()) {
            case STRING -> valueString = "STRING " + POICell.getRichStringCellValue().getString();
            case NUMERIC -> valueString = "NUMERIC " + POICell.getNumericCellValue();
            case BOOLEAN -> valueString = "BOOLEAN " + POICell.getBooleanCellValue();
            case FORMULA ->
                    valueString = "FORMULA = " + POICell.getCellFormula() + " = " + getStringFromCell(POICell.getCachedFormulaResultType());
            default -> valueString = "BLANK";
        }
        return valueString;
    }

    public String getCellFormula() {
        assert isFormulaCell() : "This is not a formula cell.";
        return POICell.getCellFormula();
    }
}

