package com.eldanior.system.territory;

public enum ParcelType {
    KINGDOM("Royaume"),
    TERRITORY("Territoire"),
    CITY("Ville"),
    PLOT("Parcelle"),
    HOUSING("Logement"),
    ROOM("Chambre"),
    FARM("Zone de Recolte");

    private final String label;

    ParcelType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
