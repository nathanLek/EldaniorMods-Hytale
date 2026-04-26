package com.eldanior.system.territory;

public enum ParcelType {
    KINGDOM("Royaume"),
    TERRITORY("Territoire"),
    CITY("Ville"),
    PLOT("Parcelle"),
    FARM("Zone de Recolte"); // TODO: blocs cassables par tous + regeneration

    private final String label;

    ParcelType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
