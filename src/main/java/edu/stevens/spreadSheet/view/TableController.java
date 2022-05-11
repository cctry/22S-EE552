package edu.stevens.spreadSheet.view;

import com.opencsv.exceptions.CsvException;
import edu.stevens.spreadSheet.model.POIWorkbook;
import edu.stevens.spreadSheet.model.POIWorkbookFactory;
import edu.stevens.spreadSheet.model.TableCellModel;
import edu.stevens.spreadSheet.model.TableRowModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import java.io.FileOutputStream;
import java.io.IOException;


public class TableController {

    private ObservableList<TableRowModel> tableRows;

    POIWorkbook workbook;

    @FXML
    private TableView<TableRowModel> table;
    @FXML
    private TableColumn<TableRowModel, String> rowIDColumn;
    @FXML
    private TextField formulaBar;
    final SimpleStringProperty formulaBarContent = new SimpleStringProperty();
    boolean formulaInputMode = false;
    @FXML
    private Button buttonInsertAbove;
    @FXML
    private Button buttonInsertBelow;
    @FXML
    private Button buttonInsertLeft;
    @FXML
    private Button buttonInsertRight;
    @FXML
    private Button leftAlign;
    @FXML
    private Button centerAlign;
    @FXML
    private Button rightAlign;
    @FXML
    private Button topBorder;
    @FXML
    private Button bottomBorder;
    @FXML
    private Button leftBorder;
    @FXML
    private Button rightBorder;
    private TableCellModel cellRegister;
    private Stage stage;

    public TableController() {

    }

    private void addColumn(String name, int index) {
        var column = new TableColumn<TableRowModel, String>(name);
        column.setCellValueFactory(p -> p.getValue().getCell(index).getValueStringProperty());
        column.setCellFactory(p -> new EditableTableCell<>());
        column.setOnEditCommit((TableColumn.CellEditEvent<TableRowModel, String> t) -> {
            int rowID = t.getTablePosition().getRow();
            int colID = t.getTablePosition().getColumn();
            assert colID != 0 : "The first column should not be edited.";
            var newValue = t.getNewValue();
            try {
                setCellContent(rowID, colID - 1, newValue);
            } catch (FormulaParseException e) {
                showAlert("Invalid formula", newValue + " is an invalid formula");
            }
            /* Update the formula bar */
            formulaBarContent.set(newValue);
        });
        column.setPrefWidth(75);
        column.setSortable(false);
        column.setReorderable(false); // Column drag and drop is disabled for now
        table.getColumns().add(index + 1, column);
    }

    void initializeFormulaBar() {
        /*
         * Let the formula bar update cell only with user's input.
         * trick: https://stackoverflow.com/questions/28421122
         */
        /*
            Make real-time update disable when input formula.
            Hit enter or lose focus will quit input formula.
         */
        formulaBarContent.addListener((observable, oldVal, newVal) -> formulaBar.setText(newVal));
        formulaBar.setOnKeyPressed(key -> {
            if (key.getCode().equals(KeyCode.ENTER)) {
                formulaInputMode = false;
                var pos = getFocusedCellPos();
                if (pos != null) {
                    try {
                        setCellContent(pos.getRow(), pos.getColumn() - 1, formulaBar.getText());
                    } catch (FormulaParseException e) {
                        showAlert("Invalid formula", formulaBar.getText() + " is an invalid formula");
                    }
                }
            }
        });
        formulaBar.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                formulaInputMode = false;
                formulaBarContent.set("");
            }
        });
        /* Update cell content when editing formula bar */
        formulaBar.textProperty().addListener((observable, oldVal, newVal) -> {
            var pos = getFocusedCellPos();
            if (pos == null) {
                return;
            }
            if (pos.getRow() >= 0 && pos.getColumn() >= 1) {
                if (!newVal.equals(formulaBarContent.get())) {
                    formulaInputMode = newVal.startsWith("=");
                    try {
                        setCellContent(pos.getRow(), pos.getColumn() - 1, newVal);
                    } catch (FormulaParseException e) {
                        if (!formulaInputMode) {
                            throw e;
                        }
                    }
                }
            }
        });
    }

    @FXML
    public void initialize() {
        // config table
        tableRows = FXCollections.observableArrayList();
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(tableRows);
        initializeFormulaBar();
    }

    void drawRowIDColumn() {
        rowIDColumn.setCellValueFactory(p -> p.getValue().getRowIDProperty());
        rowIDColumn.setCellFactory(p -> {
            var cell = new TableCell<TableRowModel, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.getStyleClass().clear();
            cell.getStyleClass().add("rowID-cell");
            return cell;
        });
    }

    void drawSheet() {
        tableRows.clear();
        var sheet = workbook.getCurrentSheet();
        /* create rows */
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            tableRows.add(new TableRowModel(row, r + 1));
        }
        /* create columns */
        refreshColumns(workbook.maxColumnNum());
    }

    private void drawColumns(int columnNum) {
        drawRowIDColumn();
        for (int c = 0; c < columnNum; c++) {
            var columnName = CellReference.convertNumToColString(c);
            addColumn(columnName, c);
        }
    }

    public void setWorkbook(POIWorkbook workbook) {
        this.workbook = workbook;
        drawSheet();
        /* Delay the initialization here to make sure all cells are initialized. */
        /* display content on formula bar when the cell is selected */
        table.getFocusModel().focusedCellProperty().addListener((observable, oldPos, newPos) -> {
            if ((newPos.getRow() != -1) && (newPos.getColumn() > 0)) {
                String displayContent;
                var cell = getCell(newPos.getRow(), newPos.getColumn() - 1);
                if (cell.isFormulaCell()) {
                    displayContent = "=" + cell.getCellFormula();
                } else {
                    displayContent = cell.getValueString();
                }
                formulaBarContent.set(displayContent);
            }
        });
    }

    void showAlert(String header, String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    void evaluateAll() {
        workbook.evaluateAll();
        // update all cells
        for (var row : tableRows) {
            for (int c = 0; c < row.getNumberOfCells(); c++) {
                row.getCell(c).updateFromCell();
            }
        }
    }

    public void setCellContent(int rowID, int colID, String content) throws FormulaParseException {
        getCell(rowID, colID).setValue(content);
        evaluateAll();
    }

    private TableCellModel getCell(int rowID, int colID) {
        return tableRows.get(rowID).getCell(colID);
    }

    @SuppressWarnings({"rawtypes"})
    private TablePosition getFocusedCellPos() {
        /*
         * Using table.getFocusModel().focusedCellProperty().get(); will return -1 when column is inserted.
         * Current call will clear the focus after column insertion
         */
        var pos = table.getFocusModel().getFocusedCell();
        if (pos.getRow() < 0 || pos.getColumn() < 0) {
            return null;
        } else {
            return pos;
        }
    }

    public void insertRow(MouseEvent event) {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int insertPos = focusedPos.getRow();
        if (event.getSource() == buttonInsertBelow) {
            insertPos += 1;
        }
        var newRow = workbook.insertRow(insertPos);
        tableRows.add(insertPos, new TableRowModel(newRow, insertPos + 1));
        refreshRowID();
    }

    public void insertColumn(MouseEvent event) {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int insertPos = focusedPos.getColumn(); // insertPos is the one after the focused
        if (event.getSource() == buttonInsertLeft) {
            insertPos -= 1;
        }
        var addedCells = workbook.insertColumn(insertPos);
        for (int r = 0; r < tableRows.size(); r++) {
            tableRows.get(r).addCell(new TableCellModel(addedCells.get(r)), insertPos);
        }
        refreshColumns(getColumnNum() + 1);
    }

    /*
    * Return the number of data columns so the rowID column is not counted.
    */
    private int getColumnNum() {
        return table.getColumns().size() - 1;
    }

    private void refreshColumns(int columnNum) {
        /* Redraw all columns */
        /* TODO: Resize the columns when deleted. */
        table.getColumns().clear();
        table.getColumns().add(rowIDColumn);
        drawColumns(columnNum);
    }

    public void removeColumn() {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int deletedPos = focusedPos.getColumn() - 1;
        workbook.deleteColumn(deletedPos);
        for (var tableRow : tableRows) {
            tableRow.removeCell(deletedPos);
        }
        refreshColumns(getColumnNum() - 1);
    }

    public void removeRow() {
        var focusedPos = getFocusedCellPos();
        if (focusedPos == null) {
            return;
        }
        int deletedPos = focusedPos.getRow();
        workbook.deleteRow(deletedPos);
        tableRows.remove(deletedPos);
        refreshRowID();
    }

    private void refreshRowID() {
        for (int r = 0; r < tableRows.size(); r++) {
            tableRows.get(r).setRowID(r + 1);
        }
    }

    public void copyCell() {
        this.cellRegister = getFocusedCell();
    }

    public void clearCell() {
        var cell = getFocusedCell();
        cell.setValue("");
        evaluateAll();
    }

    private TableCellModel getFocusedCell() {
        var pos = table.getFocusModel().getFocusedCell();
        return getCell(pos.getRow(), pos.getColumn() - 1);
    }

    public void pasteCell() {
        var cell = getFocusedCell();
        if (cellRegister.isFormulaCell()) {
            cell.setValue("=" + cellRegister.getCellFormula());
        } else {
            cell.setValue(cellRegister.getValueString());
        }
        evaluateAll();
    }

    public void setCellAlignment(MouseEvent event){
        System.out.println(event.getSource());
        var cell = getFocusedCell();
        if(cell.getCellStyle() == null){
            cell.setPOICellStyle(workbook.initializeCS());
        }
        if(event.getSource() == leftAlign){
            cell.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
            System.out.println(cell.getCellStyle().getAlignment().toString());
        }
        else if(event.getSource() == centerAlign){
            cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
            System.out.println(cell.getCellStyle().getAlignment().toString());
        }
        else if(event.getSource() == rightAlign){
            cell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
            System.out.println(cell.getCellStyle().getAlignment().toString());
        }
    }

    public void setBorder(MouseEvent event){
        var cell = getFocusedCell();
        if(cell.getCellStyle() == null){
            cell.setPOICellStyle(workbook.initializeCS());
        }
        if(event.getSource() == topBorder){
            if (cell.getCellStyle().getBorderTop() == BorderStyle.NONE){
                cell.getCellStyle().setBorderTop(BorderStyle.MEDIUM);
            }
            else cell.getCellStyle().setBorderTop(BorderStyle.NONE);
        }
        else if(event.getSource() == bottomBorder){
            if (cell.getCellStyle().getBorderBottom() == BorderStyle.NONE){
                cell.getCellStyle().setBorderBottom(BorderStyle.MEDIUM);
            }
            else cell.getCellStyle().setBorderBottom(BorderStyle.NONE);
        }
        else if(event.getSource() == leftBorder){
            if (cell.getCellStyle().getBorderLeft() == BorderStyle.NONE){
                cell.getCellStyle().setBorderLeft(BorderStyle.MEDIUM);
            }
            else cell.getCellStyle().setBorderLeft(BorderStyle.NONE);
        }
        else if(event.getSource() == rightBorder){
            if (cell.getCellStyle().getBorderRight() == BorderStyle.NONE){
                cell.getCellStyle().setBorderRight(BorderStyle.MEDIUM);
            }
            else cell.getCellStyle().setBorderRight(BorderStyle.NONE);
        }
    }

    public void menuAboutAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("This software");
        alert.setContentText("42");
        alert.show();
    }

    public void menuQuitAction() {
        workbook.close();
    }

    public void menuSaveAction() {
        try {
            if (workbook.file == null) {
                menuSaveAsAction();
            } else {
                FileOutputStream fileOS = new FileOutputStream(workbook.file);
                workbook.write(fileOS);
                fileOS.close();
            }
        } catch (IOException e) {
            showAlert("File save failed", "Unable to open " + workbook.file);
        }
    }

    public void menuSaveAsAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Save Spreadsheet");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel 97-2003 workbook", "*.xls"), new FileChooser.ExtensionFilter("CSV (Comma delimited)", "*.csv"));
        var file = fileChooser.showSaveDialog(stage);
        var fileName = file.getAbsolutePath();
        workbook.setFile(fileName);
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            showAlert("File save failed", "Unable to open " + fileName);
        }
    }

    public void menuOpenAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Open Spreadsheet");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel workbook", "*.xlsx"), new FileChooser.ExtensionFilter("Excel 97-2003 workbook", "*.xls"), new FileChooser.ExtensionFilter("CSV (Comma delimited)", "*.csv"));
        var file = fileChooser.showOpenDialog(stage);
        if (file == null) { // cancel open
            return;
        }
        var fileName = file.getName();
        workbook.setFile(file.getAbsolutePath());
        try {
            POIWorkbook workbook;
            if (file.getName().endsWith("csv")) {
                workbook = POIWorkbookFactory.fromCSVFile(file);
            } else {
                workbook = POIWorkbookFactory.fromExcelFile(file);
            }
            setWorkbook(workbook);
        } catch (IOException e) {
            showAlert("File open failed", "Unable to open " + fileName);
        } catch (CsvException e) {
            showAlert("File open failed", fileName + " is not a valid CSV file");
        }
    }

    public void menuNewAction() {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}


