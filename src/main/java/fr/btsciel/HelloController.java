package fr.btsciel;

import com.astier.bts.totp.Totp;
import fr.btsciel.exceptions.DiagnosticException;
import fr.btsciel.modeles.Ipv4;
import fr.btsciel.multicast_diffusion.MulticastDiffusion;
import fr.btsciel.outils.ScanInterfaces;
import fr.btsciel.tcp.TCPClient;
import com.astier.bts.totp.Totp;
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
    static MulticastDiffusion multicastDiffusion;
    static String interfaceName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        voyant.setFill(RED);
        connecter.setDisable(false);
        deconnecter.setDisable(true);

        getInterfaces();
        verifierTotp();

        connecter.setOnAction(_ -> this.connecter());
        deconnecter.setOnAction(_ -> this.deconnecter());
        button.setOnAction(_ -> this.envoyer());
        TextFieldRequette.setOnAction(_ -> this.envoyer());
        choiceBoxInterfaces.getSelectionModel().selectedItemProperty().addListener(
                (_, _, nouvelleValeur) -> {
                    Ipv4 monInterface = nouvelleValeur;
                    if (monInterface != null) {
                        System.out.println("\t[ Interfaces ]");
                        System.out.printf("""
                            
                            Type : %s
                            Nom : %s
                            IP : %s
                            
                            %n""", monInterface.interfaceType(), monInterface.interfaceName(), monInterface.ip());
                    }

                    interfaceName = monInterface.interfaceName();
                    new Thread(this::getConfig).start();
                });
    }

    public boolean verifierTotp() {
        Totp totp = new Totp();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Vérification TOTP");
        dialog.setHeaderText("Saisissez votre code d'authentification.");
        dialog.setContentText("Code TOTP :");

        while (true) {
            Optional<String> saisie = dialog.showAndWait();
            if (saisie.isEmpty()) return false;
            try {
                if (totp.testCodeTOTP(Integer.parseInt(saisie.get()))) return true;
            } catch (Exception e) {
                System.err.println(DiagnosticException.afficheException(e));
            }

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


    private void getConfig() {
        try {
            multicastDiffusion = new MulticastDiffusion(interfaceName);
            Thread.sleep(500);
            TextFieldIP.setText(multicastDiffusion.connexion.addressAsString());
            TextFieldPort.setText(String.valueOf(multicastDiffusion.connexion.portTCP()));
        } catch (Exception ex) {
            System.err.println(DiagnosticException.afficheException(ex));
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
