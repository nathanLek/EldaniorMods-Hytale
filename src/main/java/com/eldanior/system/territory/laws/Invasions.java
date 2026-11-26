package com.eldanior.system.territory.laws;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.NpcAttitudes;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Invasions de monstres (2026-11-18). Regles de l'utilisateur : **lancees par un admin seulement** (/es invasion), et **jamais un
 * monstre ne nait dans une ville** : les vagues apparaissent hors de toute ville, autour des joueurs presents (chunks charges,
 * voir le piege des apparitions hors chunk), a 24-40 blocs, puis marchent sur les joueurs du domaine.
 *  - une vague toutes les {@link #WAVE_MS} (ou des que la precedente est battue), monstres d'un niveau choisi ;
 *  - victoire : toutes les vagues battues ; prime optionnelle partagee entre les defenseurs au prorata des monstres abattus ;
 *  - defaite : temps ecoule avec des monstres debout : le domaine est pille ({@link #PILLAGE} de son tresor), monstres retires.
 *
 * 2026-11-19 (demande de l'utilisateur, « comme les failles des gobelins ») : une FAILLE s'ouvre au sol a 24-40 blocs d'un
 * defenseur (jamais en ville) : bloc Eldanior_Faille_Invasion (effet et son de la faille gobeline du jeu,
 * scripts/territoire/gen_evenements.py), marquee sur la carte (ParcelMapSystem) ; chaque vague en sort monstre par monstre
 * ({@link #EMERGE_MS}). La faille se referme a la fin de l'invasion. Lancement depuis le panneau d'administration (Tableau ›
 * Invasion) ; la commande reste pour les habitues.
 */
public final class Invasions {

    private Invasions() {}

    static final long WAVE_MS = 75_000, CLEAR_PAUSE_MS = 10_000, EXTRA_MS = 3 * 60_000;
    static final double PILLAGE = 0.10;
    static final double MIN_DIST = 24, MAX_DIST = 40, CITY_REACH = 64;
    /** Un monstre sort de la faille toutes les EMERGE_MS. */
    static final long EMERGE_MS = 1_500;
    static final String RIFT = "Eldanior_Faille_Invasion", RIFT_SOUND = "SFX_Goblin_World_Breach_Open";
    /** Failles posees (monde|x|y|z), pour les refermer meme apres un arret du serveur en pleine invasion. */
    private static final Set<String> RIFTS = ConcurrentHashMap.newKeySet();
    private static java.nio.file.Path riftFile;
    private static final Random RNG = new Random();

    /** Monstres d'invasion, du plus faible au plus fort (memes especes que les contrats du tableau de prime). */
    static final String[][] ROSTER = {
            {"Goblin_Scrapper", "Trork_Warrior"},                                        // < 60
            {"Trork_Warrior", "Trork_Mauler", "Outlander_Berserker"},                    // < 150
            {"Outlander_Brute", "Skeleton_Burnt_Knight", "Scarak_Fighter", "Werewolf"},  // < 300
            {"Skeleton_Frost_Knight", "Trork_Chieftain", "Yeti", "Golem_Firesteel"}};    // 300+

    static final class Invasion {
        final String domainId, world;
        final int waves, level, perWave;
        final long prize, endAt;
        int wave;
        long nextWaveAt;
        final Set<UUID> mobs = ConcurrentHashMap.newKeySet();
        final Map<UUID, Integer> kills = new ConcurrentHashMap<>();
        final Map<UUID, String> names = new ConcurrentHashMap<>();
        volatile ScheduledFuture<?> task;
        volatile boolean over;
        /** Faille au sol (null : pas encore ouverte), monstres encore a sortir, heure du prochain. */
        volatile int[] rift;
        int pending;
        long nextEmergeAt;

        Invasion(String domainId, String world, int waves, int level, int perWave, long prize) {
            this.domainId = domainId; this.world = world; this.waves = waves; this.level = level; this.perWave = perWave; this.prize = prize;
            this.endAt = System.currentTimeMillis() + waves * WAVE_MS + EXTRA_MS;
        }
    }

    private static final Map<String, Invasion> RUNNING = new ConcurrentHashMap<>();

    public static boolean running(String domainId) { return RUNNING.containsKey(domainId); }

    /** Au demarrage : failles restees au sol d'une invasion interrompue, refermees des que leur chunk est charge. */
    public static void init(java.nio.file.Path pluginDataDir) {
        riftFile = pluginDataDir.resolve("eldanior_data").resolve("failles_invasion.txt");
        try {
            if (java.nio.file.Files.exists(riftFile))
                for (String l : java.nio.file.Files.readAllLines(riftFile)) if (!l.isBlank()) RIFTS.add(l.trim());
        } catch (Exception e) { EldaniorLogger.error("[Invasion] failles", e); }
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { closeOrphanRifts(); } catch (Exception e) { EldaniorLogger.error("[Invasion] failles orphelines", e); }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private static void saveRifts() {
        if (riftFile == null) return;
        try {
            java.nio.file.Files.createDirectories(riftFile.getParent());
            java.nio.file.Files.write(riftFile, new ArrayList<>(RIFTS));
        } catch (Exception e) { EldaniorLogger.error("[Invasion] failles", e); }
    }

    private static String riftKey(String world, int[] r) { return world + "|" + r[0] + "|" + r[1] + "|" + r[2]; }

    private static void closeOrphanRifts() {
        java.util.Set<String> live = new java.util.HashSet<>();
        for (Invasion inv : RUNNING.values()) if (inv.rift != null) live.add(riftKey(inv.world, inv.rift));
        for (String key : new ArrayList<>(RIFTS)) {
            if (live.contains(key)) continue;
            String[] k = key.split("\\|");
            World world = k.length == 4 ? Universe.get().getWorld(k[0]) : null;
            if (world == null) continue;
            int x = Integer.parseInt(k[1]), y = Integer.parseInt(k[2]), z = Integer.parseInt(k[3]);
            world.execute(() -> {
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null) return;                                                  // chunk non charge : plus tard
                if (RIFT.equals(t.getId())) WorldBlockUtil.setBlock(world, x, y, z, "Empty");
                RIFTS.remove(key);
                saveRifts();
            });
        }
    }

    /** Failles ouvertes, pour la carte (ParcelMapSystem) : {monde, nom du domaine, x, y, z}. */
    public static List<Object[]> rifts() {
        List<Object[]> out = new ArrayList<>();
        for (Invasion inv : RUNNING.values()) {
            if (inv.rift == null) continue;
            ParcelData d = ParcelManager.get(inv.domainId);
            out.add(new Object[]{inv.world, d != null ? d.getName() : inv.domainId, inv.rift[0], inv.rift[1], inv.rift[2]});
        }
        return out;
    }

    public static List<String> list() {
        List<String> out = new ArrayList<>();
        for (Invasion inv : RUNNING.values()) {
            ParcelData d = ParcelManager.get(inv.domainId);
            out.add((d != null ? d.getName() : inv.domainId) + " : vague " + inv.wave + "/" + inv.waves + ", " + inv.mobs.size() + " monstre(s), niveau " + inv.level
                    + (inv.rift != null ? ", faille en X " + inv.rift[0] + " Z " + inv.rift[2] : ""));
        }
        return out;
    }

    /** Domaine ou se tient l'admin : la ville, sinon le territoire, etc. (le plus petit domaine). */
    public static ParcelData domainAt(String world, Vector3d pos) {
        for (ParcelData p : DomainLaws.chain(world, pos.x, pos.y, pos.z)) if (DomainLaws.isDomain(p) && p.getType() != ParcelType.KINGDOM) return p;
        return null;
    }

    /** Lance une invasion ; renvoie le message pour l'admin. */
    public static String start(ParcelData d, World world, int waves, int level, long prize) {
        if (d == null) return "Placez-vous dans une ville, un territoire ou un Marquisat.";
        if (RUNNING.containsKey(d.getId())) return "Une invasion est déjà en cours à " + d.getName() + ".";
        waves = Math.max(1, Math.min(10, waves));
        level = Math.max(1, Math.min(999, level));
        int per = d.getType() == ParcelType.CITY ? 5 : 8;
        Invasion inv = new Invasion(d.getId(), world.getName(), waves, level, per, Math.max(0, prize));
        inv.nextWaveAt = System.currentTimeMillis() + 15_000;                           // le temps de sonner l'alarme
        RUNNING.put(d.getId(), inv);
        inv.task = EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> world.execute(() -> {
            try { step(inv, world); } catch (Exception e) { EldaniorLogger.error("[Invasion] " + d.getName(), e); }
        }), 2, 1, TimeUnit.SECONDS);
        broadcast(d, "INVASION ! Des monstres marchent sur " + d.getName() + " : " + waves + " vague(s), dans 15 secondes. Aux armes !", "#e0574a");
        EldaniorLogger.info("[Invasion] lancee sur " + d.getName() + " (" + waves + " vagues, niveau " + level + ", prime " + prize + ")");
        return "Invasion lancée sur " + d.getName() + " : " + waves + " vague(s) de " + per + " monstres, niveau " + level
                + (prize > 0 ? ", prime de " + CoinWallet.format(prize) + " aux défenseurs" : "") + ".";
    }

    public static String stop(ParcelData d) {
        Invasion inv = d != null ? RUNNING.get(d.getId()) : null;
        if (inv == null) return "Aucune invasion en cours ici.";
        World world = Universe.get().getWorld(inv.world);
        if (world != null) world.execute(() -> finish(inv, world, null));
        return "Invasion de " + d.getName() + " arrêtée, monstres retirés.";
    }

    /** Monstre abattu par un joueur (DeathXPSystem) : compte pour la prime. */
    public static void killed(UUID mob, UUID killer, String killerName) {
        if (mob == null || killer == null) return;
        for (Invasion inv : RUNNING.values()) {
            if (!inv.mobs.contains(mob)) continue;
            inv.kills.merge(killer, 1, Integer::sum);
            inv.names.put(killer, killerName);
            return;
        }
    }

    // ==================== DEROULEMENT ====================

    private static void step(Invasion inv, World world) {
        if (inv.over) return;
        ParcelData d = ParcelManager.get(inv.domainId);
        if (d == null) { finish(inv, world, null); return; }
        Store<EntityStore> store = world.getEntityStore().getStore();
        long now = System.currentTimeMillis();
        List<Ref<EntityStore>> alive = new ArrayList<>();
        for (UUID u : new ArrayList<>(inv.mobs)) {
            Ref<EntityStore> r = world.getEntityStore().getRefFromUUID(u);
            if (r == null || !r.isValid() || store.getComponent(r, DeathComponent.getComponentType()) != null) { inv.mobs.remove(u); continue; }
            alive.add(r);
        }
        if (inv.wave >= inv.waves && alive.isEmpty() && inv.pending == 0 && now > inv.nextWaveAt - WAVE_MS + 5_000) { finish(inv, world, true); return; }
        if (now >= inv.endAt) { finish(inv, world, false); return; }
        if (inv.pending > 0 && now >= inv.nextEmergeAt) emerge(inv, d, world, store);   // un monstre sort de la faille
        if (inv.wave < inv.waves && inv.pending == 0
                && (now >= inv.nextWaveAt || alive.isEmpty() && inv.wave > 0 && now >= inv.nextWaveAt - WAVE_MS + CLEAR_PAUSE_MS)) {
            int n = spawnWave(inv, d, world, store);
            if (n == 0) { inv.nextWaveAt = now + CLEAR_PAUSE_MS; return; }               // personne pour defendre : on attend (le temps court)
            inv.wave++;
            inv.nextWaveAt = now + WAVE_MS;
            broadcast(d, "Vague " + inv.wave + "/" + inv.waves + " sur " + d.getName() + " : " + n + " monstre(s) !", "#e8a050");
        }
        // les monstres marchent sur le joueur le plus proche dans le domaine (ou pres de la ville)
        for (Ref<EntityStore> mob : alive) {
            Vector3d at = position(mob, store);
            Ref<EntityStore> target = nearestDefender(d, world, at);
            if (target != null) NpcAttitudes.hostile(mob, target, 6, store);
        }
    }

    /** Nouvelle vague : la faille s'ouvre si besoin, puis ses monstres en sortent un par un (emerge). 0 : personne a defendre. */
    private static int spawnWave(Invasion inv, ParcelData d, World world, Store<EntityStore> store) {
        if (inv.rift == null && !openRift(inv, d, world)) return 0;
        inv.pending += inv.perWave;
        inv.nextEmergeAt = System.currentTimeMillis() + 800;
        return inv.perWave;
    }

    /** Ouvre la faille a 24-40 blocs d'un defenseur, hors des villes ; annonce et son de la faille. */
    private static boolean openRift(Invasion inv, ParcelData d, World world) {
        List<Vector3d> anchors = defenders(d, world);
        if (anchors.isEmpty()) return false;                                            // personne : on n'ouvre rien dans le vide
        for (int i = 0; i < 6; i++) {
            Vector3d p = spot(d, world, anchors.get(RNG.nextInt(anchors.size())));
            if (p == null) continue;
            int x = (int) Math.floor(p.x), y = (int) Math.floor(p.y), z = (int) Math.floor(p.z);
            WorldBlockUtil.setBlock(world, x, y, z, RIFT);
            inv.rift = new int[]{x, y, z};
            RIFTS.add(riftKey(world.getName(), inv.rift));
            saveRifts();
            int sound = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex(RIFT_SOUND);
            for (PlayerRef pr : Universe.get().getPlayers()) {
                Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
                if (sound != 0 && at != null && at.distanceSquared(p) <= 90 * 90)
                    com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent2dToPlayer(pr, sound, com.hypixel.hytale.protocol.SoundCategory.SFX);
            }
            broadcast(d, "Une faille s'ouvre près de " + d.getName() + " (X " + x + ", Z " + z + ") : les monstres en sortent ! Elle est marquée sur la carte.", "#e0574a");
            return true;
        }
        return false;
    }

    /** Un monstre sort de la faille et marche sur le defenseur le plus proche. */
    private static void emerge(Invasion inv, ParcelData d, World world, Store<EntityStore> store) {
        int[] r = inv.rift;
        if (r == null) { inv.pending = 0; return; }
        if (WorldBlockUtil.getBlockType(world, r[0], r[1], r[2]) == null) return;         // chunk de la faille pas charge : on attend
        inv.pending--;
        inv.nextEmergeAt = System.currentTimeMillis() + EMERGE_MS;
        String[] tier = ROSTER[inv.level < 60 ? 0 : inv.level < 150 ? 1 : inv.level < 300 ? 2 : 3];
        String role = tier[RNG.nextInt(tier.length)];
        double a = RNG.nextDouble() * Math.PI * 2;
        Vector3d p = new Vector3d(r[0] + 0.5 + Math.cos(a) * 1.5, r[1] + 0.1, r[2] + 0.5 + Math.sin(a) * 1.5);
        try {
            var pair = NPCPlugin.get().spawnNPC(store, role, null, p, new Rotation3f(0, RNG.nextFloat() * 360f, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) return;
            int lvl = Math.max(1, inv.level + RNG.nextInt(11) - 5);
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(lvl, role, true));
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u != null) inv.mobs.add(u.getUuid());
            Ref<EntityStore> target = nearestDefender(d, world, p);
            if (target != null) NpcAttitudes.hostile(ref, target, 6, store);
        } catch (Exception e) {
            EldaniorLogger.error("[Invasion] apparition " + role, e);
        }
    }

    /** Point d'apparition : a 24-40 blocs d'un defenseur, au sol, JAMAIS dans une ville, dans le domaine (ou pres de la ville). */
    private static Vector3d spot(ParcelData d, World world, Vector3d around) {
        for (int tries = 0; tries < 24; tries++) {
            double a = RNG.nextDouble() * Math.PI * 2, r = MIN_DIST + RNG.nextDouble() * (MAX_DIST - MIN_DIST);
            int x = (int) Math.floor(around.x + Math.cos(a) * r), z = (int) Math.floor(around.z + Math.sin(a) * r);
            Integer y = surface(world, x, z, (int) Math.floor(around.y));
            if (y == null) continue;
            if (inCity(world.getName(), x + 0.5, y + 1.0, z + 0.5)) continue;           // regle : jamais dans une ville
            if (d.getType() == ParcelType.CITY ? distanceToBox(d, x, z) > CITY_REACH : !d.contains(x + 0.5, y + 1.0, z + 0.5)) continue;
            return new Vector3d(x + 0.5, y + 1.0, z + 0.5);
        }
        return null;
    }

    private static boolean inCity(String world, double x, double y, double z) {
        for (ParcelData p : DomainLaws.chain(world, x, y, z)) if (p.getType() == ParcelType.CITY) return true;
        for (ParcelData c : ParcelManager.getByType(ParcelType.CITY))                    // marge de 4 blocs autour des murs
            if (world.equals(c.getWorld()) && distanceToBox(c, (int) x, (int) z) <= 4 && y >= c.getMinY() - 8 && y <= c.getMaxY() + 8) return true;
        return false;
    }

    private static double distanceToBox(ParcelData c, int x, int z) {
        double dx = Math.max(Math.max(c.getMinX() - x, 0), x - c.getMaxX()), dz = Math.max(Math.max(c.getMinZ() - z, 0), z - c.getMaxZ());
        return Math.hypot(dx, dz);
    }

    private static Integer surface(World world, int x, int z, int hintY) {
        if (WorldBlockUtil.getBlockType(world, x, Math.max(1, hintY), z) == null) return null;   // chunk non charge
        for (int y = Math.min(318, hintY + 30); y > Math.max(1, hintY - 40); y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null) return null;
            if (type == BlockType.EMPTY || "Empty".equals(type.getId())) continue;
            if (WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) return null;     // pas dans l'eau
            String id = type.getId();
            if (id.contains("Leaves") || id.startsWith("Plant_") || id.startsWith("Wood_")) return null;   // pas dans les arbres
            BlockType above = WorldBlockUtil.getBlockType(world, x, y + 1, z), above2 = WorldBlockUtil.getBlockType(world, x, y + 2, z);
            boolean free = (above == null || above == BlockType.EMPTY || "Empty".equals(above.getId()))
                    && (above2 == null || above2 == BlockType.EMPTY || "Empty".equals(above2.getId()));
            return free ? y : null;
        }
        return null;
    }

    /** Positions des joueurs presents dans le domaine (ou a moins de CITY_REACH blocs d'une ville). */
    private static List<Vector3d> defenders(ParcelData d, World world) {
        List<Vector3d> out = new ArrayList<>();
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid() || r.getStore().getExternalData().getWorld() != world) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at == null) continue;
            if (d.contains(at.x, at.y, at.z) || d.getType() == ParcelType.CITY && distanceToBox(d, (int) at.x, (int) at.z) <= CITY_REACH)
                out.add(new Vector3d(at));
        }
        return out;
    }

    private static Ref<EntityStore> nearestDefender(ParcelData d, World world, Vector3d from) {
        if (from == null) return null;
        Ref<EntityStore> best = null;
        double bd = Double.MAX_VALUE;
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid() || r.getStore().getExternalData().getWorld() != world) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at == null) continue;
            boolean in = d.contains(at.x, at.y, at.z) || d.getType() == ParcelType.CITY && distanceToBox(d, (int) at.x, (int) at.z) <= CITY_REACH;
            double dist = at.distanceSquared(from);
            if (in && dist < bd && dist < 120 * 120) { bd = dist; best = r; }
        }
        return best;
    }

    private static Vector3d position(Ref<EntityStore> ref, Store<EntityStore> store) {
        var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        return t != null ? new Vector3d(t.getPosition()) : null;
    }

    /** Fin : victoire (true), defaite (false) ou arret par l'admin (null). Monstres restants retires. */
    private static void finish(Invasion inv, World world, Boolean won) {
        if (inv.over) return;
        inv.over = true;
        RUNNING.remove(inv.domainId);
        if (inv.task != null) inv.task.cancel(false);
        Store<EntityStore> store = world.getEntityStore().getStore();
        for (UUID u : inv.mobs) {
            Ref<EntityStore> r = world.getEntityStore().getRefFromUUID(u);
            if (r != null && r.isValid()) store.removeEntity(r, RemoveReason.REMOVE);
        }
        inv.mobs.clear();
        inv.pending = 0;
        if (inv.rift != null) {                                                        // la faille se referme
            int[] r = inv.rift;
            BlockType t = WorldBlockUtil.getBlockType(world, r[0], r[1], r[2]);
            if (t != null && RIFT.equals(t.getId())) WorldBlockUtil.setBlock(world, r[0], r[1], r[2], "Empty");
            if (t != null) { RIFTS.remove(riftKey(inv.world, r)); saveRifts(); }       // sinon : refermee plus tard (closeOrphanRifts)
            inv.rift = null;
        }
        ParcelData d = ParcelManager.get(inv.domainId);
        if (d == null || won == null) return;
        if (won) {
            int total = inv.kills.values().stream().mapToInt(Integer::intValue).sum();
            StringBuilder heroes = new StringBuilder();
            Map<UUID, Integer> sorted = new LinkedHashMap<>();
            inv.kills.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(e -> sorted.put(e.getKey(), e.getValue()));
            for (Map.Entry<UUID, Integer> e : sorted.entrySet()) {
                if (heroes.length() > 0) heroes.append(", ");
                heroes.append(inv.names.getOrDefault(e.getKey(), "?")).append(" (").append(e.getValue()).append(")");
                if (inv.prize > 0 && total > 0) pay(e.getKey(), inv.prize * e.getValue() / total, d);
            }
            broadcast(d, d.getName() + " a repoussé l'invasion !" + (heroes.length() > 0 ? " Défenseurs : " + heroes + "." : ""), "#00e7b6");
        } else {
            long loot = Math.round(d.getTreasury() * PILLAGE);
            d.setTreasury(Math.max(0, d.getTreasury() - loot));
            ParcelManager.save();
            String house = DomainLaws.house(d);
            if (!house.isEmpty()) com.eldanior.system.titles.nobility.family.FamilyRegistry.log(house, "Invasion à " + d.getName()
                    + " : le domaine est pillé (" + CoinWallet.format(loot) + ")");
            broadcast(d, d.getName() + " n'a pas tenu : les monstres pillent le domaine et emportent " + CoinWallet.format(loot) + " du trésor.", "#e0574a");
        }
        EldaniorLogger.info("[Invasion] fin a " + d.getName() + " : " + (won ? "victoire" : "defaite"));
    }

    /** Prime d'un defenseur, versee sur son compte en banque (en ligne : composant du joueur). */
    private static void pay(UUID u, long amount, ParcelData d) {
        if (amount <= 0) return;
        PlayerRef pr = Universe.get().getPlayer(u);
        Ref<EntityStore> r = pr != null ? pr.getReference() : null;
        if (r == null || !r.isValid()) return;
        World w = r.getStore().getExternalData().getWorld();
        w.execute(() -> {
            if (!r.isValid()) return;
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            var data = r.getStore().getComponent(r, type);
            if (data == null) return;
            var copy = (com.eldanior.system.config.Player.PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.addMoney(amount);
            r.getStore().putComponent(r, type, copy);
            pr.sendMessage(Message.raw("Prime de défense de " + d.getName() + " : " + CoinWallet.format(amount) + " versés sur votre compte.").color("#00e7b6"));
        });
    }

    /** Annonce a tout le serveur (titre pour ceux qui sont sur place). */
    private static void broadcast(ParcelData d, String text, String color) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            pr.sendMessage(Message.raw("[Invasion] " + text).color(color));
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null && (d.contains(at.x, at.y, at.z) || d.getType() == ParcelType.CITY && distanceToBox(d, (int) at.x, (int) at.z) <= CITY_REACH))
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "Invasion", text.length() > 60 ? d.getName() : text, true);
        }
    }
}
