module com.example.demo {
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires javafx.controls;

    opens com.cyber.server to javafx.fxml;
    opens com.cyber.server.model to javafx.base;
    exports com.cyber.server;
    exports com.cyber.server.controller;
    opens com.cyber.server.controller to javafx.fxml;
}
