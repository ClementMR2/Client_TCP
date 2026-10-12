package fr.btsciel.modeles;

import java.net.InetAddress;

public record Connexion(InetAddress adresseServeur, int portTCP, int portUDP) {
    public String addressAsString() {
        return adresseServeur.toString().replace("/", "");
    }
}
