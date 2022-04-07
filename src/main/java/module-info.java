module spreadSheet {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    
    opens edu.stevens.spreadSheet to javafx.fxml;
    opens edu.stevens.spreadSheet.view to javafx.fxml;
    exports edu.stevens.spreadSheet;
    exports edu.stevens.spreadSheet.view;
}