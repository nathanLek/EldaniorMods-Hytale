package com.eldanior.system.hologram;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.config.PersistenceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HologramManager {

    private static final Map<String, HologramData> holograms = new ConcurrentHashMap<>();
    private static final Map<String, java.util.List<com.hypixel.hytale.component.Ref<EntityStore>>> holoEntityRefs = new ConcurrentHashMap<>();
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static Path dataDir;

    public static void init(Path pluginDataDir) {
        dataDir = pluginDataDir.resolve("eldanior_data");
        load();
        System.out.println("[Eldanior] HologramManager initialise. " + holograms.size() + " hologramme(s) charge(s).");
    }

    // === CRUD ===

    public static HologramData create(String text, double x, double y, double z, String worldName) {
        return create(java.util.List.of(text), x, y, z, worldName);
    }

    public static HologramData create(java.util.List<String> lines, double x, double y, double z, String worldName) {
        String id = "holo_" + nextId.getAndIncrement();
        HologramData data = new HologramData(id, lines, x, y, z, worldName);
        holograms.put(id, data);
        save();
        return data;
    }

    public static boolean delete(String id) {
        HologramData removed = holograms.remove(id);
        if (removed != null) {
            despawnHologram(id, removed.getWorldName());
            save();
            return true;
        }
        return false;
    }

    private static void despawnHologram(String holoId, String worldName) {
        var refs = holoEntityRefs.remove(holoId);
        if (refs == null || refs.isEmpty()) return;

        try {
            World world = findWorld(worldName);
            if (world == null) return;

            world.execute(() -> {
                for (var entityRef : refs) {
                    try {
                        if (entityRef != null && entityRef.isValid()) {
                            world.getEntityStore().getStore().removeEntity(entityRef, com.hypixel.hytale.component.RemoveReason.REMOVE);
                        }
                    } catch (Exception e) {
                        System.err.println("[Hologram] Erreur despawn " + holoId + ": " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[Hologram] Erreur despawn " + holoId + ": " + e.getMessage());
        }
    }

    public static HologramData get(String id) {
        return holograms.get(id);
    }

    public static Collection<HologramData> getAll() {
        return holograms.values();
    }

    public static int count() {
        return holograms.size();
    }

    // === SPAWN ===

    public static void spawnAll() {
        for (HologramData data : holograms.values()) {
            spawnHologram(data);
        }
        System.out.println("[Eldanior] " + holograms.size() + " hologramme(s) spawne(s).");
    }

    public static void spawnHologram(HologramData data) {
        try {
            World world = findWorld(data.getWorldName());
            if (world == null) {
                System.err.println("[Hologram] World introuvable: " + data.getWorldName());
                return;
            }

            world.execute(() -> {
                try {
                    java.util.List<com.hypixel.hytale.component.Ref<EntityStore>> refs = new java.util.ArrayList<>();
                    java.util.List<String> lines = data.getLines();
                    double baseY = data.getY() + (lines.size() * 0.3);

                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (line.isEmpty()) line = " ";

                        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

                        ProjectileComponent projectileComponent = new ProjectileComponent("Projectile");
                        holder.putComponent(ProjectileComponent.getComponentType(), projectileComponent);

                        Vector3d pos = new Vector3d(data.getX(), baseY - (i * 0.3), data.getZ());
                        com.hypixel.hytale.math.vector.Rotation3f rot = new com.hypixel.hytale.math.vector.Rotation3f(0, 0, 0);
                        holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(pos, rot));

                        holder.ensureComponent(UUIDComponent.getComponentType());

                        if (projectileComponent.getProjectile() == null) {
                            projectileComponent.initialize();
                            if (projectileComponent.getProjectile() == null) continue;
                        }

                        holder.addComponent(NetworkId.getComponentType(),
                                new NetworkId(world.getEntityStore().getStore().getExternalData().takeNextNetworkId()));

                        holder.addComponent(Nameplate.getComponentType(), new Nameplate(line));

                        var entityRef = world.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);
                        if (entityRef != null) refs.add(entityRef);
                    }

                    if (!refs.isEmpty()) {
                        holoEntityRefs.put(data.getId(), refs);
                    }
                } catch (Exception e) {
                    System.err.println("[Hologram] Erreur spawn " + data.getId() + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Hologram] Erreur spawn " + data.getId() + ": " + e.getMessage());
        }
    }

    private static World findWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) worldName = "default";
        return Universe.get().getWorld(worldName);
    }

    // === PERSISTENCE ===

    public static void save() {
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Properties props = new Properties();
            props.setProperty("count", String.valueOf(holograms.size()));
            props.setProperty("nextId", String.valueOf(nextId.get()));

            int idx = 0;
            for (HologramData h : holograms.values()) {
                String prefix = "holo." + idx + ".";
                props.setProperty(prefix + "id", h.getId());
                props.setProperty(prefix + "lineCount", String.valueOf(h.getLineCount()));
                for (int l = 0; l < h.getLineCount(); l++) {
                    props.setProperty(prefix + "line." + l, h.getLines().get(l));
                }
                props.setProperty(prefix + "x", String.valueOf(h.getX()));
                props.setProperty(prefix + "y", String.valueOf(h.getY()));
                props.setProperty(prefix + "z", String.valueOf(h.getZ()));
                props.setProperty(prefix + "world", h.getWorldName());
                idx++;
            }

            PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("holograms.properties"), props, "Eldanior Holograms");
        } catch (Exception e) {
            System.err.println("[Hologram] Erreur save: " + e.getMessage());
        }
    }

    public static void load() {
        holograms.clear();
        Path file = dataDir.resolve("holograms.properties");
        if (!Files.exists(file)) return;

        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            }

            int count = Integer.parseInt(props.getProperty("count", "0"));
            int maxId = Integer.parseInt(props.getProperty("nextId", "1"));
            nextId.set(maxId);

            for (int i = 0; i < count; i++) {
                String prefix = "holo." + i + ".";
                String id = props.getProperty(prefix + "id");
                int lineCount = Integer.parseInt(props.getProperty(prefix + "lineCount", "1"));
                java.util.List<String> lines = new java.util.ArrayList<>();
                if (lineCount > 1) {
                    for (int l = 0; l < lineCount; l++) {
                        lines.add(props.getProperty(prefix + "line." + l, ""));
                    }
                } else {
                    // Compat ancien format
                    String text = props.getProperty(prefix + "text", props.getProperty(prefix + "line.0", ""));
                    lines.add(text);
                }
                double x = Double.parseDouble(props.getProperty(prefix + "x", "0"));
                double y = Double.parseDouble(props.getProperty(prefix + "y", "0"));
                double z = Double.parseDouble(props.getProperty(prefix + "z", "0"));
                String world = props.getProperty(prefix + "world", "default");

                if (id != null) {
                    holograms.put(id, new HologramData(id, lines, x, y, z, world));
                }
            }
        } catch (Exception e) {
            System.err.println("[Hologram] Erreur load: " + e.getMessage());
        }
    }
}
