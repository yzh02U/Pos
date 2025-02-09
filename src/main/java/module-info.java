module com.example.poshaisan {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires org.controlsfx.controls;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.attach.util;
    requires java.logging;
    requires mysql.connector.j;
    requires Util;
    requires com.fasterxml.jackson.databind;
    opens com.example.poshaisan to javafx.fxml;
    exports com.example.poshaisan;
}