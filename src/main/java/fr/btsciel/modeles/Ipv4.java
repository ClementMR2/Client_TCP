package fr.btsciel.modeles;

public record Ipv4(String interfaceType, String interfaceName, String ip) {
    @Override
    public String toString() {
        return interfaceName();
    }
}

