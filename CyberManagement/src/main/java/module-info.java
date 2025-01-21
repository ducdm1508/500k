module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;
    requires de.jensd.fx.glyphs.fontawesome;


    opens com.cyber.server to javafx.fxml;
    exports com.cyber.server;
    exports com.cyber.server.controller;
    opens com.cyber.server.controller to javafx.fxml;
    opens com.cyber.server.model to javafx.base;
}