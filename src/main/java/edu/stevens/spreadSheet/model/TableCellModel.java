package edu.stevens.spreadSheet.model;

import javafx.beans.property.SimpleStringProperty;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.commons.lang3.math.NumberUtils;

public class TableCellModel {
    private final SimpleStringProperty valueString;
    private Cell POICell = null;

    public TableCellModel(String value) {
        this.valueString = new SimpleStringProperty(value);
    }

    public TableCellModel(Cell POICell) {
        this.POICell = POICell;
        assert !DateUtil.isCellDateFormatted(POICell) : "Date value is not supported.";
        this.valueString = new SimpleStringProperty(getStringFromCell());
    }

    String getStringFromCell() {
        return switch (POICell.getCellType()) {
            case STRING -> POICell.getRichStringCellValue().getString();
            case NUMERIC -> String.valueOf(POICell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(POICell.getBooleanCellValue());
            case FORMULA ->
                    // TODO:
                    //  Now the formula is displayed in raw.
                    //  The formula should be evaluated by default unless the cell is selected.
                    String.valueOf(POICell.getCellFormula());
            default -> "";
        };
    }


    public void setValue(String value) {
        setValueString(value);
        if (value.length() == 0) {
            POICell.setBlank();
        }
        if (NumberUtils.isParsable(value)) {
            POICell.setCellValue(Double.parseDouble(value));
        } else if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            POICell.setCellValue(Boolean.parseBoolean(value));
        } else if (value.startsWith("=")) {
            POICell.setCellFormula(value);
        } else {
            POICell.setCellValue(value);
        }
    }

    public boolean isBlank() {
        return getValueString().length() == 0;
    }


    public String getValueString() {
        return valueString.get();
    }

    public void setValueString(String value) {
        this.valueString.set(value);
    }

    public SimpleStringProperty getValueStringProperty() {
        return valueString;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "valueString=" + valueString +
                '}';
    }
}

