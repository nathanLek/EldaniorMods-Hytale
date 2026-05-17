package com.eldanior.system.hologram;

import java.util.List;

public class HologramData {

    private final String id;
    private List<String> lines;
    private double x, y, z;
    private String worldName;

    public HologramData(String id, List<String> lines, double x, double y, double z, String worldName) {
        this.id = id;
        this.lines = lines;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    // Compat single line
    public HologramData(String id, String text, double x, double y, double z, String worldName) {
        this(id, List.of(text), x, y, z, worldName);
    }

    public String getId() { return id; }
    public List<String> getLines() { return lines; }
    public String getText() { return String.join(" | ", lines); }
    public int getLineCount() { return lines.size(); }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getWorldName() { return worldName; }

    public String getLocationString() {
        return String.format("%.0f, %.0f, %.0f", x, y, z);
    }
}
