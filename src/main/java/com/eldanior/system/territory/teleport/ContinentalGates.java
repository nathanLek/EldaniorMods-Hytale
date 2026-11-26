package com.eldanior.system.territory.teleport;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.GuardManager;
import com.eldanior.system.guards.GuardType;
import com.eldanior.system.guards.NpcTrades;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portail Continental (2026-11-19, demande de l'utilisateur : « une porte de 7 x 9 sur une plateforme de 13 x 13 avec un PNJ lie
 * au teleporteur qui l'active lorsque le joueur paye, et il traverse la porte pour etre teleporte »).
 *  - Pose (admin) : la Pierre de fondation (ITEM) posee dans un Marquisat sans portail bati, en vrais blocs du jeu, une plateforme
 *    13 x 13 (bordure ornee, anneau d'aqua, damier de calcite, allee centrale d'aqua, seuil orne), quatre tours d'angle a
 *    brasero, deux lanternes, la porte (7 de large, 9 de haut : colonnes, linteau, clef d'aqua, arc ; couronne et lanternes
 *    au-dessus) et le Gardien du portail (PNJ, metier PASSEUR), devant a droite. L'axe suit le regard de l'admin.
 *  - Voyage : on paie le Gardien (ContinentalPage) ; l'ouverture se remplit d'un voile translucide (VEIL) et de l'effet du
 *    teleporteur du jeu agrandi (VEIL_CORE, scripts/portail/gen_portail.py) pendant {@link #PASS_MS} ; traverser = teleporte
 *    devant le portail de destination. Le voile disparait quand plus personne n'a de passage en cours.
 * Donnees : lois du Marquisat (tpAt « monde|x|y|z|gate|dir », tpNpc = poste du Gardien).
 */
public final class ContinentalGates {

    private ContinentalGates() {}

    public static final String ITEM = "Eldanior_Portail_Continental", VEIL = "Eldanior_Voile_Portail", VEIL_CORE = "Eldanior_Voile_Portail_Coeur";
    static final long PASS_MS = 30_000;
    static final int HALF = 6;                                                          // plateforme 13 x 13
    static final String SMOOTH = "Rock_Calcite_Brick_Smooth", BRICK = "Rock_Calcite_Brick", ORNATE = "Rock_Calcite_Brick_Ornate",
            DECO = "Rock_Calcite_Brick_Decorative", HALF_SLAB = "Rock_Calcite_Brick_Half", PILLAR_BASE = "Rock_Calcite_Brick_Pillar_Base",
            PILLAR = "Rock_Calcite_Brick_Pillar_Middle", AQUA = "Rock_Aqua_Brick_Smooth", AQUA_ORNATE = "Rock_Aqua_Brick_Ornate",
            GEM = "Rock_Gem_Sapphire", BRAZIER = "Furniture_Human_Ruins_Brazier", LANTERN = "Furniture_Human_Ruins_Lantern", EMPTY = "Empty";

    /** Passage paye : portail de depart (Marquisat), destination, fin de validite. */
    record Pass(String from, String to, long until) {}

    private static final Map<UUID, Pass> PASSES = new ConcurrentHashMap<>();

    // ==================== CONSTRUCTION ====================

    /** Bati le portail autour de la pierre de fondation ; dir = axe de passage. Thread du monde. Message d'erreur ou null. */
    static String build(World world, ParcelData m, int cx, int cy, int cz, int dir) {
        ContinentalTeleporters.Spot s = new ContinentalTeleporters.Spot(world.getName(), cx, cy, cz, true, dir);
        for (int a = -HALF; a <= HALF; a++)                                            // tout doit etre charge avant de batir
            for (int f = -HALF; f <= HALF; f++)
                if (WorldBlockUtil.getBlockType(world, cx + a * s.lx() + f * s.fx(), cy, cz + a * s.lz() + f * s.fz()) == null)
                    return "Une partie du terrain n'est pas chargée : approchez-vous du centre.";
        // ---- plateforme ----
        for (int a = -HALF; a <= HALF; a++) {
            for (int f = -HALF; f <= HALF; f++) {
                int ring = Math.max(Math.abs(a), Math.abs(f));
                String floor = ring == HALF ? ORNATE                                    // bordure ornee
                        : ring == HALF - 1 ? AQUA                                       // anneau d'aqua
                        : f == 0 && Math.abs(a) <= 2 ? AQUA_ORNATE                      // seuil sous la porte
                        : a == 0 ? AQUA                                                 // allee centrale
                        : Math.abs(a) == 1 ? SMOOTH
                        : ((a + f) & 1) == 0 ? SMOOTH : BRICK;                          // damier
                put(world, s, a, f, 0, floor);
                put(world, s, a, f, -1, BRICK);                                         // assise
                for (int h = 1; h <= 12; h++) put(world, s, a, f, h, EMPTY);            // degager herbes et arbres
            }
        }
        // ---- tours d'angle a brasero et lanternes ----
        for (int a : new int[]{-5, 5}) for (int f : new int[]{-5, 5}) {
            put(world, s, a, f, 1, PILLAR_BASE);
            put(world, s, a, f, 2, PILLAR);
            put(world, s, a, f, 3, DECO);
            put(world, s, a, f, 4, BRAZIER);
        }
        for (int a : new int[]{-5, 5}) put(world, s, a, 0, 1, LANTERN);
        // ---- porte : 7 de large, 9 de haut ----
        for (int a : new int[]{-3, 3}) {
            put(world, s, a, 0, 1, PILLAR_BASE);
            for (int h = 2; h <= 7; h++) put(world, s, a, 0, h, PILLAR);
            put(world, s, a, 0, 8, DECO);
            put(world, s, a, 0, 9, ORNATE);
            for (int f : new int[]{-1, 1}) put(world, s, a, f, 1, HALF_SLAB);           // empattement des colonnes
            put(world, s, a + Integer.signum(a), 0, 1, HALF_SLAB);
            put(world, s, a, 0, 10, LANTERN);                                           // lanterne sur chaque colonne
        }
        for (int a = -2; a <= 2; a++) put(world, s, a, 0, 9, a == 0 ? AQUA_ORNATE : ORNATE);   // linteau et clef
        put(world, s, -2, 0, 8, BRICK);                                                 // arc : ouverture 5 x 7, puis 3 au sommet
        put(world, s, 2, 0, 8, BRICK);
        for (int a = -1; a <= 1; a++) put(world, s, a, 0, 10, a == 0 ? GEM : HALF_SLAB); // couronne au saphir
        m.setLaw("tpAt", s.code());
        if (m.law("tpPrice").isEmpty()) m.setLaw("tpPrice", String.valueOf(ContinentalTeleporters.DEFAULT_PRICE));
        // ---- Gardien du portail : devant, a droite de la porte, tourne vers les voyageurs ----
        Vector3d npc = new Vector3d(cx + 0.5 + 3 * s.lx() - 3 * s.fx(), cy + 1, cz + 0.5 + 3 * s.lz() - 3 * s.fz());
        float yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(-s.fx(), -s.fz()));
        GuardManager.Post post = GuardManager.place(GuardType.COMMERCANT, world, npc, yaw, null, null, 0, null, null, NpcTrades.Metier.PASSEUR);
        if (post != null) m.setLaw("tpNpc", post.getId());
        ParcelManager.save();
        return null;
    }

    private static void put(World world, ContinentalTeleporters.Spot s, int a, int f, int h, String id) {
        boolean turned = id.equals(VEIL_CORE) || id.equals(VEIL) || id.equals(LANTERN) || id.equals(BRAZIER);   // le reste est symetrique
        WorldBlockUtil.setBlock(world, s.x() + a * s.lx() + f * s.fx(), s.y() + h, s.z() + a * s.lz() + f * s.fz(), id, turned ? s.dir() & 3 : 0);
    }

    /** Cases de l'ouverture : 5 de large sur 7, puis 3 au sommet (sous l'arc). */
    private static void opening(java.util.function.BiConsumer<Integer, Integer> cell) {
        for (int h = 1; h <= 7; h++) for (int a = -2; a <= 2; a++) cell.accept(a, h);
        for (int a = -1; a <= 1; a++) cell.accept(a, 8);
    }

    /** Retire le portail du reseau (les pierres restent) : Coeur eteint, Gardien renvoye. Thread du monde. */
    public static String unregister(ParcelData m, World world) {
        ContinentalTeleporters.Spot s = ContinentalTeleporters.spot(m);
        if (s == null) return "Ce Marquisat n'a pas de téléporteur.";
        opening((a, h) -> put(world, s, a, 0, h, EMPTY));
        String npc = m.law("tpNpc");
        if (!npc.isEmpty()) GuardManager.removeById(npc, world);
        m.setLaw("tpAt", "");
        m.setLaw("tpNpc", "");
        ParcelManager.save();
        return "Portail de " + m.getName() + " retiré du réseau (la structure reste, le Gardien est parti).";
    }

    // ==================== GARDIEN ET PASSAGE ====================

    /** Un joueur parle au Gardien : fenetre des destinations, ou refus. */
    public static void talk(String worldName, Vector3d at, Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr, String npcName) {
        Player player = store.getComponent(ref, Player.getComponentType());
        ParcelData m = ContinentalTeleporters.marquisatAt(worldName, at.x, at.y, at.z);
        ContinentalTeleporters.Spot s = m != null ? ContinentalTeleporters.spot(m) : null;
        if (player == null) return;
        if (s == null || !s.gate()) { pr.sendMessage(Message.raw(npcName + " : « Ce portail n'est relié à rien. Un admin doit le consacrer. »")); return; }
        if (!ContinentalTeleporters.active(m)) {
            pr.sendMessage(Message.raw(npcName + " : « Le Marquis a fermé le portail. Je ne peux rien pour vous. »"));
            return;
        }
        Pass p = PASSES.get(pr.getUuid());
        if (p != null && p.until() > System.currentTimeMillis() && p.from().equals(m.getId())) {
            pr.sendMessage(Message.raw(npcName + " : « Le passage est payé. Traversez la porte, vite ! »").color("#7fd4ff"));
            return;
        }
        player.getPageManager().openCustomPage(ref, store, new ContinentalPage(pr, m.getId()));
    }

    /** Passage paye : le portail s'allume pour ce voyageur. */
    static void grant(UUID u, ParcelData from, ParcelData to) {
        PASSES.put(u, new Pass(from.getId(), to.getId(), System.currentTimeMillis() + PASS_MS));
        light(from, true);
    }

    /** Joueur dans l'ouverture d'un portail pour lequel il a paye : teleporte. Appele chaque quart de seconde (GateSystem). */
    static Vector3d crossing(UUID u, String world, Vector3d pos) {
        Pass p = PASSES.get(u);
        if (p == null) return null;
        ParcelData from = ParcelManager.get(p.from()), to = ParcelManager.get(p.to());
        if (p.until() < System.currentTimeMillis() || from == null || to == null) {
            PASSES.remove(u);
            if (from != null) refreshLight(from);
            PlayerRef pr = Universe.get().getPlayer(u);
            if (pr != null) pr.sendMessage(Message.raw("Le portail s'est refermé : le passage payé est perdu.").color("#e8c060"));
            return null;
        }
        ContinentalTeleporters.Spot s = ContinentalTeleporters.spot(from), d = ContinentalTeleporters.spot(to);
        if (s == null || d == null || !s.gate() || !s.world().equals(world)) return null;
        double dx = pos.x - (s.x() + 0.5), dz = pos.z - (s.z() + 0.5), h = pos.y - s.y();
        double f = dx * s.fx() + dz * s.fz(), a = dx * s.lx() + dz * s.lz();
        if (Math.abs(f) > 0.8 || Math.abs(a) > 2.6 || h < 0.5 || h > 8.5) return null;
        PASSES.remove(u);
        refreshLight(from);
        return d.arrival();
    }

    static ParcelData destination(UUID u) {
        Pass p = PASSES.get(u);
        return p != null ? ParcelManager.get(p.to()) : null;
    }

    private static void refreshLight(ParcelData from) {
        long now = System.currentTimeMillis();
        boolean any = PASSES.values().stream().anyMatch(p -> p.from().equals(from.getId()) && p.until() > now);
        light(from, any);
    }

    /** Remplit l'ouverture du voile (et de l'effet au centre, en bas) ou la vide. Thread du monde du portail. */
    static void light(ParcelData m, boolean on) {
        ContinentalTeleporters.Spot s = ContinentalTeleporters.spot(m);
        World world = s != null ? Universe.get().getWorld(s.world()) : null;
        if (world == null) return;
        world.execute(() -> {
            try {
                opening((a, h) -> {
                    int x = s.x() + a * s.lx(), y = s.y() + h, z = s.z() + a * s.lz();
                    BlockType cur = WorldBlockUtil.getBlockType(world, x, y, z);
                    if (cur == null) return;                                           // non charge
                    String id = cur.getId() != null ? cur.getId() : "";
                    boolean veil = id.contains(VEIL);
                    if (on && (veil || cur == BlockType.EMPTY || EMPTY.equals(id)))
                        put(world, s, a, 0, h, a == 0 && h == 1 ? VEIL_CORE : VEIL);
                    else if (!on && veil) put(world, s, a, 0, h, EMPTY);
                });
            } catch (Exception e) {
                EldaniorLogger.error("[Portail] voile", e);
            }
        });
    }

    /** Axe de passage d'apres la position de l'admin et de la pierre : il regarde la porte de face. */
    static int dirFrom(double px, double pz, int bx, int bz) {
        double dx = bx + 0.5 - px, dz = bz + 0.5 - pz;
        if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? 1 : 3;
        return dz > 0 ? 0 : 2;
    }

    static boolean isMarquisat(ParcelData p) { return p != null && p.getType() == ParcelType.GRAND_TERRITORY; }
}
