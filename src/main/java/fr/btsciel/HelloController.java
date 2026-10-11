package fr.btsciel;

import fr.btsciel.exceptions.DiagnosticException;
import fr.btsciel.modeles.Ipv4;
import fr.btsciel.multicast_diffusion.MulticastDiffusion;
import fr.btsciel.outils.ScanInterfaces;
import fr.btsciel.tcp.TCPClient;
import fr.btsciel.totp.Totp;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import static javafx.scene.paint.Color.*;

public class HelloController implements Initializable {
    public Button button;
    public Button connecter;
    public Button deconnecter;
    public TextField TextFieldIP;
    public TextField TextFieldPort;
    public TextField TextFieldRequette;
    public Circle voyant;
    public TextArea TextAreaReponses;
    public ChoiceBox<Ipv4> choiceBoxInterfaces;
    public static TCPClient tcp;
    static boolean enRun = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verifierTotp();

        voyant.setFill(RED);
        connecter.setDisable(false);
        deconnecter.setDisable(true);

        getInterfaces();

        connecter.setOnAction(_ -> this.connecter());
        deconnecter.setOnAction(_ -> this.deconnecter());
        button.setOnAction(_ -> this.envoyer());
        TextFieldRequette.setOnAction(_ -> this.envoyer());
        choiceBoxInterfaces.getSelectionModel().selectedItemProperty().addListener(
                (_, _, nouvelleValeur) -> {
                    if (nouvelleValeur == null) {
                        return;
                    };

                    new Thread(() -> {
                        getConfig(nouvelleValeur);
                    }).start();
                });
    }

    public boolean verifierTotp() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Vérification TOTP");
        dialog.setHeaderText("Saisissez votre code d'authentification.");
        dialog.setContentText("Code TOTP :");

        while (true) {
            Optional<String> saisie = dialog.showAndWait();
            if (saisie.isEmpty()) return false;
            if (Totp.testTotp(saisie.get())) return true;

            dialog.setHeaderText("Code invalide. Réessayez.");
            dialog.getEditor().clear();
        }
    }

    private void getInterfaces() {
        try {
            choiceBoxInterfaces.getItems().setAll(ScanInterfaces.getSystemIP());
        } catch (SocketException e) {
            DiagnosticException.afficheException(e);
        }
    }


    private void getConfig(Ipv4 monInterface) {
        try {
            MulticastDiffusion diffusion = new MulticastDiffusion(monInterface.interfaceName());
            Platform.runLater(() -> {
                if (!monInterface.equals(choiceBoxInterfaces.getValue()) || enRun) return;

                TextFieldIP.setText(diffusion.connexion.addressAsString());
                TextFieldPort.setText(String.valueOf(diffusion.connexion.portTCP()));
            });
        } catch (Exception ex) {
            String diagnostic = DiagnosticException.afficheException(ex);
            Platform.runLater(() -> TextAreaReponses.appendText(diagnostic + "\n"));
        }
    }

    private void envoyer() {
        if (!enRun) return;

        String s = TextFieldRequette.getText();
        if (s.isEmpty()) return;

        if (s.equalsIgnoreCase("exit")) {
            this.deconnecter();
            return;
        }

        tcp.requette(s);
    }

    private void deconnecter() {
        if (!enRun) return;

        tcp.deconnection();
        enRun = false;

        TextAreaReponses.clear();
        TextFieldRequette.clear();
        choiceBoxInterfaces.getSelectionModel().clearSelection();
    }

    private void connecter() {
        if (TextFieldIP.getText().isEmpty() || TextFieldPort.getText().isEmpty() || enRun) return;

        try {
            int port = Integer.parseInt(TextFieldPort.getText().strip());
            tcp = new TCPClient(InetAddress.getByName(TextFieldIP.getText().strip()), port, this);
            tcp.connection();
            enRun = tcp.isAlive();
        } catch (Exception e) {
            TextAreaReponses.appendText(DiagnosticException.afficheException(e) + "\n");
        }
    }

    public void connexionFermee() {
        enRun = false;
        connecter.setDisable(false);
        deconnecter.setDisable(true);
        voyant.setFill(RED);
        choiceBoxInterfaces.setDisable(false);
    }
}
