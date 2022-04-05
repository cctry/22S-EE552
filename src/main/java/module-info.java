module spreadSheet {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    
    opens edu.stevens.spreadSheet to javafx.fxml;
    exports edu.stevens.spreadSheet;
}