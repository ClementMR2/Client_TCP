package fr.btsciel.totp;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class Totp {
    private static final String cleTotp = "IIW6JG7FYVRBF5S47QZCDYFHPVBLAYAW";
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static boolean testTotp(String code) {
        if (code == null) return false;
        return code.matches("[0-9]{6}") && gAuth.authorize(cleTotp, Integer.parseInt(code));
    }
}

