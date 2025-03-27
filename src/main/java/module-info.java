module org.example.musikplayer_doit {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.media;

    opens org.example.musikplayer_doit.model to javafx.base;
    opens org.example.musikplayer_doit to javafx.fxml;
    exports org.example.musikplayer_doit;
    exports org.example.musikplayer_doit.controller;
    opens org.example.musikplayer_doit.controller to javafx.fxml;
}