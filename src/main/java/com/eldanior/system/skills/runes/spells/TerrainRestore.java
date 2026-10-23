package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modifications temporaires du terrain par les sorts divins (givre de la Porte de Niflheim, terre brulee du Courroux
 * de Muspelheim) : chaque bloc remplace est memorise puis restaure a la date prevue.
 *
 * Securites :
 *  - aucune modification dans une ville, une parcelle de joueur, une zone de recolte, une arene, un donjon ou une mine
 *    (royaumes et territoires, qui couvrent toute la carte, restent autorises) ;
 *  - les blocs a contenu ou fonctionnels (coffres, portes, panneaux...) et les parties de blocs multi-cases ne sont
 *    jamais remplaces ; l'orientation d'origine est restauree ;
 *  - a la restauration, un bloc n'est remis que s'il est encore celui pose par le sort (ou vide) ;
 *  - un bloc deja transforme par un autre sort (ex. terre brulee puis gelee) n'est jamais memorise comme "original" :
 *    le nouveau sort reprend le VRAI bloc d'origine et la charge de sa restauration (index des cases modifiees) ;
 *  - les restaurations en attente sont sauvegardees sur disque et reprises au redemarrage.
 * Toutes les operations sur les blocs doivent s'executer hors du tick ECS (world.execute).
 */
public final class TerrainRestore {

    private TerrainRestore() {}

    static final String EMPTY = "Empty";
    private static final int RESTORE_PER_TICK = 400;
    private static final Set<ParcelType> OPEN_PARCELS = Set.of(ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY);
    /** Blocs jamais remplaces (contenu, fonction, ou objet fin). */
    private static final String[] KEEP_KEYWORDS = {
            "Chest", "Bench", "Door", "Sign", "Bed", "Portal", "Container", "Barrel", "Crate", "Shelf", "Furnace",
            "Spawner", "Totem", "Trap", "Coffin", "Mailbox", "Torch", "Lantern", "Candle", "Light", "Banner",
            "Carpet", "Ladder", "Rail", "Rope", "Chain", "Lever", "Button", "Pressure", "Teleporter", "Waypoint"
    };

    /** Blocs d'origine a remettre a une date donnee. */
    static final class Restore {
        String world;
        long restoreAt;
        List<String[]> blocks = new ArrayList<>(); // {x, y, z, idOrigine, idPose, rotationOrigine}
        Restore() {}
        Restore(String world, long restoreAt) { this.world = world; this.restoreAt = restoreAt; }
    }

    private static final List<Restore> pending = new CopyOnWriteArrayList<>();
    /** Case modifiee -> lot et entree qui la restaureront (blocs et fluides indexes separement). */
    private record Owner(Restore restore, String[] entry) {}
    private static final java.util.Map<String, Owner> owners = new java.util.concurrent.ConcurrentHashMap<>();
    private static Path saveFile;
    private static final Gson GSON = new GsonBuilder().create();
    private static volatile boolean restoreQueued;

    /** Au demarrage : recharge les restaurations en attente (serveur arrete avant la date prevue). */
    public static void init(Path dataDir) {
        saveFile = dataDir.resolve("terrain_restore.json");
        try {
            for (String legacy : new String[]{"niflheim_restore.json", "zero_absolu_restore.json"}) { // anciens fichiers
                Path old = dataDir.resolve(legacy);
                if (!Files.exists(old)) continue;
                List<Restore> loaded = GSON.fromJson(Files.readString(old), new TypeToken<List<Restore>>() {}.getType());
                if (loaded != null) pending.addAll(loaded);
                Files.delete(old);
            }
            if (Files.exists(saveFile)) {
                List<Restore> loaded = GSON.fromJson(Files.readString(saveFile), new TypeToken<List<Restore>>() {}.getType());
                if (loaded != null) pending.addAll(loaded);
            }
            for (Restore r : pending) for (String[] b : r.blocks) owners.put(key(r.world, b), new Owner(r, b));
            EldaniorLogger.info("[SortsDivins] " + pending.size() + " zone(s) de terrain a restaurer");
            save();
        } catch (Exception e) {
            EldaniorLogger.error("TerrainRestore: lecture des restaurations", e);
        }
    }

    /** Ouvre un lot de blocs a restaurer a la date donnee. */
    static Restore open(World world, long restoreAt) {
        Restore r = new Restore(world.getName(), restoreAt);
        pending.add(r);
        return r;
    }

    static void save() {
        if (saveFile == null) return;
        try {
            Files.writeString(saveFile, GSON.toJson(new ArrayList<>(pending)));
        } catch (Exception e) {
            EldaniorLogger.error("TerrainRestore: sauvegarde des restaurations", e);
        }
    }

    /**
     * Pose un bloc temporaire et memorise l'original (et son orientation). Si la case est deja modifiee par un sort,
     * son vrai bloc d'origine est repris et l'ancienne entree abandonnee : c'est ce lot qui la restaurera.
     * A appeler hors tick ECS.
     */
    static boolean place(World world, Restore r, int x, int y, int z, String original, String placed, int rotation) {
        if (BlockType.fromString(placed) == null) return false;
        String originalRotation = EMPTY.equals(original) ? "0" : String.valueOf(Math.max(0, WorldBlockUtil.getRotationIndex(world, x, y, z)));
        String[] entry = {String.valueOf(x), String.valueOf(y), String.valueOf(z), original, placed, originalRotation};
        Owner previous = owners.get(key(r.world, entry));
        if (previous != null) {                       // deja transformee : on herite de son vrai bloc d'origine
            entry[3] = previous.entry()[3];
            entry[5] = previous.entry().length > 5 ? previous.entry()[5] : "0";
            previous.restore().blocks.remove(previous.entry());
        }
        r.blocks.add(entry);
        owners.put(key(r.world, entry), new Owner(r, entry));
        WorldBlockUtil.setBlock(world, x, y, z, placed, rotation);
        return true;
    }

    /** Cle d'index d'une entree : monde + position + nature (bloc ou fluide). */
    private static String key(String world, String[] b) {
        boolean fluid = b.length > 4 && b[4].startsWith("fluid:");
        return world + ":" + b[0] + ":" + b[1] + ":" + b[2] + (fluid ? ":f" : ":b");
    }

    /** Retire l'entree de l'index si elle en est toujours la proprietaire. */
    private static void release(Restore r, String[] b) {
        String k = key(r.world, b);
        Owner o = owners.get(k);
        if (o != null && o.entry() == b) owners.remove(k);
    }

    private static final String FLUID_PREFIX = "fluid:";

    /** Pose un fluide immobile (ex. "Lava_Source") dans une case vide, retire a la restauration. A appeler hors tick ECS. */
    static boolean placeFluid(World world, Restore r, int x, int y, int z, String fluidId) {
        if (!WorldBlockUtil.setFluid(world, x, y, z, fluidId)) return false;
        String[] entry = {String.valueOf(x), String.valueOf(y), String.valueOf(z), EMPTY, FLUID_PREFIX + fluidId, "0"};
        Owner previous = owners.get(key(r.world, entry));
        if (previous != null) previous.restore().blocks.remove(previous.entry());
        r.blocks.add(entry);
        owners.put(key(r.world, entry), new Owner(r, entry));
        return true;
    }

    /**
     * Restaure immediatement les blocs poses par un sort dans un rayon horizontal (ex. la glace de Niflheim fondue par
     * une meteorite de Muspelheim). Seuls les blocs dont l'identifiant pose contient un des mots-cles sont concernes.
     * A appeler hors tick ECS.
     */
    static int restoreArea(World world, double cx, double cz, double radius, String[] placedKeywords) {
        int restored = 0;
        for (Restore r : pending) {
            if (!world.getName().equals(r.world)) continue;
            for (int i = r.blocks.size() - 1; i >= 0; i--) {
                String[] b = r.blocks.get(i);
                if (b.length < 5 || !contains(b[4], placedKeywords)) continue;
                int x = Integer.parseInt(b[0]), z = Integer.parseInt(b[2]);
                if (Math.hypot(x + 0.5 - cx, z + 0.5 - cz) > radius) continue;
                if (restoreEntry(world, b)) { r.blocks.remove(i); release(r, b); restored++; }
            }
        }
        if (restored > 0) save();
        return restored;
    }

    /** Remet un bloc (ou retire un fluide) s'il est toujours celui pose par le sort. False si la section n'est pas chargee. */
    private static boolean restoreEntry(World world, String[] b) {
        int x = Integer.parseInt(b[0]), y = Integer.parseInt(b[1]), z = Integer.parseInt(b[2]);
        if (b.length > 4 && b[4].startsWith(FLUID_PREFIX)) {
            int current = WorldBlockUtil.getFluidIndex(world, x, y, z);
            if (current < 0) return false;
            int ours = com.hypixel.hytale.server.core.asset.type.fluid.Fluid.getAssetMap().getIndex(b[4].substring(FLUID_PREFIX.length()));
            if (current == ours) WorldBlockUtil.setFluid(world, x, y, z, null);
            return true;
        }
        BlockType current = WorldBlockUtil.getBlockType(world, x, y, z);
        if (current == null) return false;
        String id = current.getId();
        // Ne jamais ecraser ce qu'un joueur a pose entre-temps : seul le bloc pose par le sort (ou le vide) est remis
        boolean ours = b.length > 4 ? b[4].equals(id) : "Soil_Snow".equals(id) || "Rock_Ice".equals(id);
        if (current == BlockType.EMPTY || EMPTY.equals(id) || ours) {
            int rotation = b.length > 5 ? Integer.parseInt(b[5]) : 0;
            WorldBlockUtil.setBlock(world, x, y, z, b[3], rotation);
        }
        return true;
    }

    /** True quand tout le lot a ete restaure (ou qu'il n'est plus suivi). */
    static boolean isDone(Restore r) {
        return r == null || !pending.contains(r);
    }

    static boolean isEmpty(World world, int x, int y, int z) {
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        return type != null && (type == BlockType.EMPTY || EMPTY.equals(type.getId()));
    }

    /** Une ville, une parcelle de joueur, une arene... est protegee ; royaumes et territoires (toute la carte) non. */
    static boolean isProtectedArea(String world, int x, int y, int z) {
        ParcelData parcel = ParcelManager.getParcelAt(world, x, y, z);
        return parcel != null && !OPEN_PARCELS.contains(parcel.getType());
    }

    /** Bloc a laisser tel quel : contenu / fonction / objet fin, ou partie secondaire d'un bloc multi-cases. */
    static boolean isKept(World world, String id, int x, int y, int z) {
        return contains(id, KEEP_KEYWORDS) || WorldBlockUtil.getFiller(world, x, y, z) != 0;
    }

    static boolean contains(String id, String[] keywords) {
        for (String k : keywords) if (id.contains(k)) return true;
        return false;
    }

    /** Planifie la restauration progressive des lots dont la date est passee (hors tick ECS, via world.execute). */
    static void tick(World world) {
        if (world == null || pending.isEmpty() || restoreQueued) return;
        long now = System.currentTimeMillis();
        boolean due = false;
        for (Restore r : pending) if (now >= r.restoreAt && world.getName().equals(r.world)) { due = true; break; }
        if (!due) return;
        restoreQueued = true;
        world.execute(() -> {
            try { restoreStep(world); }
            catch (Exception e) { EldaniorLogger.error("TerrainRestore: restauration", e); }
            finally { restoreQueued = false; }
        });
    }

    /** Restaure tout de suite un lot entier (ex. le trone de Nyx qui disparait). A appeler hors tick ECS. */
    static void restoreNow(World world, Restore r) {
        if (r == null) return;
        for (int i = r.blocks.size() - 1; i >= 0; i--) {
            String[] b = r.blocks.get(i);
            if (restoreEntry(world, b)) { r.blocks.remove(i); release(r, b); }
        }
        if (r.blocks.isEmpty()) pending.remove(r);
        save();
    }

    private static void restoreStep(World world) {
        long now = System.currentTimeMillis();
        for (Restore r : pending) {
            if (now < r.restoreAt || !world.getName().equals(r.world)) continue;
            int budget = RESTORE_PER_TICK;
            while (!r.blocks.isEmpty() && budget-- > 0) {
                String[] b = r.blocks.get(r.blocks.size() - 1);
                if (!restoreEntry(world, b)) break; // section non chargee : on reessaiera plus tard
                r.blocks.remove(r.blocks.size() - 1);
                release(r, b);
            }
            if (r.blocks.isEmpty()) {
                pending.remove(r);
                save();
            }
            return; // un lot a la fois
        }
    }
}
