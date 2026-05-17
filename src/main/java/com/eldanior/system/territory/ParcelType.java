package com.eldanior.system.territory;

public enum ParcelType {
    KINGDOM("Royaume"),
    GRAND_TERRITORY("Grand Territoire"),
    TERRITORY("Territoire"),
    CITY("Ville"),
    PLOT("Parcelle"),
    HOUSING("Logement"),
    ROOM("Chambre"),
    FARM("Zone de Recolte"),
    FOREST("Foret"),
    ARENA("Arene"),
    DUNGEON("Donjon"),
    MINE("Mine");

    private final String label;

    ParcelType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
