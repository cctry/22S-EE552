package edu.stevens.spreadSheet.model;

import javafx.beans.property.SimpleStringProperty;

public class TableCell {
    private final SimpleStringProperty valueString;

    public TableCell(String value) {
        this.valueString = new SimpleStringProperty(value);
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

