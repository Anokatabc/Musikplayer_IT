module org.example.musikplayer_doit {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires jaudiotagger;
    requires java.desktop;
    requires java.sql;


    opens org.example.musikplayer_doit to javafx.fxml;
    exports org.example.musikplayer_doit;
    exports org.example.musikplayer_doit.controller;
    exports org.example.musikplayer_doit.model;
    opens org.example.musikplayer_doit.controller to javafx.fxml;
    opens org.example.musikplayer_doit.model to javafx.base, javafx.fxml;

}