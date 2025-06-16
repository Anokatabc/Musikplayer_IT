module org.example.musikplayer_doit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires jaudiotagger;
    requires java.desktop;
    requires java.logging;


    opens org.example.musikplayer_doit.model to javafx.base;
    opens org.example.musikplayer_doit to javafx.fxml;
    exports org.example.musikplayer_doit;
    exports org.example.musikplayer_doit.controller;
    exports org.example.musikplayer_doit.model;
    opens org.example.musikplayer_doit.controller to javafx.fxml;
    exports org.example.musikplayer_doit.services;
    opens org.example.musikplayer_doit.services to javafx.fxml;
}