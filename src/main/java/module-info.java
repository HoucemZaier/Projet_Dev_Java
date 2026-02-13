module PI_JAVA {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.sql;

    // Remplacez les deux anciennes lignes par celle-ci :
    requires de.jensd.fx.glyphs.fontawesome;

    exports Esprit.tn;
    opens Esprit.tn to javafx.fxml;
    opens Controlleurs to javafx.fxml;
}