module fr.btsciel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.net.http;
    requires com.google.gson;
    requires googleauth;

    opens fr.btsciel to javafx.fxml;
    opens fr.btsciel.modeles to com.google.gson;
    exports fr.btsciel;
}
