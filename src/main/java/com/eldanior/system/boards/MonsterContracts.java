package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Contrats de monstres du tableau de prime : chaque jour, {@link #PER_DAY} zones de contrat (posees par un admin : /es contrat)
 * recoivent un monstre d'elite nomme ("Grok le Devoreur", espece tiree dans SPECIES), au niveau de la zone x {@link #ELITE}
 * (MobLevelData), deux fois plus grand ({@link #SCALE}, une seule fois a l'apparition : changer le modele en plein combat le
 * fait sauter), avec une aura (effet Eldanior_Contrat_Aura reapplique). Son point de laisse le suit chaque seconde : sans cela
 * le moteur le ramene d'un coup a son point d'apparition quand le combat l'en eloigne (laisse dure). Il apparait
 * quand un joueur arrive a moins de {@link #SPAWN_RANGE} blocs de sa zone. Disparu sans etre reclame (mort sans tueur, retire
 * par un autre systeme) : il ne revient qu'apres {@link #RESPAWN_DELAY_MS}, et jamais sous les yeux d'un joueur (personne a
 * moins de {@link #CLEAR_RANGE} blocs du point). Le premier joueur qui l'abat touche la recompense (DeathXPSystem -> claim),
 * annonce a tous. Contrats et zones : eldanior_data/monster_contracts.properties.
 */
public final class MonsterContracts {

    private MonsterContracts() {}

    static final int PER_DAY = 3;
    static final double ELITE = 1.2, SPAWN_RANGE = 64.0, CLEAR_RANGE = 16.0;
    static final float SCALE = 2.0f;
    static final long REWARD_PER_LEVEL = 30, RESPAWN_DELAY_MS = 60_000;
    private static final String AURA = "Eldanior_Contrat_Aura";

    /** Espece : role vanilla (verifie dans Assets.zip) et nom affiche. */
    public record Species(String role, String label, float scale) {
        Species(String role, String label) { this(role, label, SCALE); }
    }

    /**
     * Especes reservees aux monstres etoiles (2026-11-16, demande de l'utilisateur) : dragons de lave et de givre, a leur taille
     * normale (deja immenses). Roles combattants du mod (scripts/monstres/gen_dragons_etoiles.py) : ceux du jeu (Dragon_Fire,
     * Dragon_Frost) sont des figurants invulnerables. Jamais tires pour les contrats ordinaires.
     */
    private static final List<Species> STARRED_ONLY = List.of(
            new Species("Eldanior_Dragon_Etoile_Feu", "Dragon de lave", 1.0f), new Species("Eldanior_Dragon_Etoile_Givre", "Dragon de givre", 1.0f));

    /** Anciens roles de dragon (figurants invulnerables du jeu) -> roles combattants du mod (zones et contrats deja enregistres). */
    static String migrateRole(String role) {
        if ("Dragon_Fire".equals(role)) return "Eldanior_Dragon_Etoile_Feu";
        if ("Dragon_Frost".equals(role)) return "Eldanior_Dragon_Etoile_Givre";
        return role;
    }

    /** Niveau maximum d'une zone (2026-11-16 : au-dela de 999, les monstres sont de rang SS puis EX). */
    public static final int MAX_LEVEL = 9999;

    /** «  ·  Rang SS » pour un monstre au-dela du niveau 999, « » sinon (tableaux, fenetres). */
    public static String rankSuffix(int level) {
        var r = com.eldanior.system.quest.QuestDifficulty.forMonsterLevel(level);
        return r == null ? "" : "  ·  Rang " + r.getDisplayName();
    }

    private static final List<Species> SPECIES = List.of(
            new Species("Trork_Warrior", "Trork"), new Species("Trork_Mauler", "Trork"), new Species("Trork_Chieftain", "Chef trork"),
            new Species("Goblin_Scrapper", "Gobelin"), new Species("Outlander_Berserker", "Outlander"),
            new Species("Outlander_Brute", "Outlander"), new Species("Skeleton_Burnt_Knight", "Squelette"),
            new Species("Skeleton_Frost_Knight", "Squelette de givre"), new Species("Scarak_Fighter", "Scarak"),
            new Species("Werewolf", "Loup-garou"), new Species("Yeti", "Yéti"), new Species("Bear_Grizzly", "Grizzly"),
            new Species("Golem_Firesteel", "Golem"), new Species("Toad_Rhino", "Crapaud-rhino"));
    private static final String[] NAMES = {"Grok", "Morgash", "Skarn", "Vhul", "Brakka", "Zogg", "Ulgrim", "Kraal", "Thrak",
            "Mordax", "Sliss", "Vorag", "Drogul", "Karnak", "Gorza", "Ruk"};
    private static final String[] EPITHETS = {"le Dévoreur", "l'Éventreur", "le Maudit", "Brise-Crânes", "le Sanguinaire",
            "Croc-de-Fer", "le Rôdeur", "l'Insatiable", "le Fléau", "Mange-Os", "Œil-Rouge", "le Colosse"};

    /**
     * Zone de contrat : point d'apparition et niveau de base. stars > 0 (2026-11-16) : zone de monstre etoile, creee depuis la
     * fenetre d'un poste de PNJ (categorie Hors-la-loi) ; elle a son contrat CHAQUE jour, en plus des PER_DAY tires au hasard,
     * avec l'espece choisie (role, null : au hasard). Plus il y a d'etoiles, plus le monstre est fort et mieux il paie.
     */
    public record Zone(String id, String name, String world, Vector3d pos, int level, int stars, String role) {
        public Zone(String id, String name, String world, Vector3d pos, int level) { this(id, name, world, pos, level, 0, null); }
    }

    /** Multiplicateur de niveau par nombre d'etoiles (0 : contrat ordinaire, ELITE). */
    public static double eliteFor(int stars) {
        return switch (stars) { case 2 -> 1.5; case 3 -> 2.0; default -> ELITE; };
    }

    /** « ★★ » */
    /**
     * « 2 étoiles » (2026-11-16 : le jeu n'affiche pas le symbole etoile dans les noms ni dans les fenetres, d'ou le texte).
     */
    public static String stars(int n) {
        n = Math.min(3, n);
        return n <= 0 ? "" : n == 1 ? "1 étoile" : n + " étoiles";
    }

    /** Especes possibles d'un monstre etoile (fenetre du poste) : celles des contrats, puis les dragons. */
    public static List<Species> species() {
        List<Species> all = new ArrayList<>(SPECIES);
        all.addAll(STARRED_ONLY);
        return all;
    }

    /**
     * Especes de plus pour les boss de donjon et de zone (2026-11-19, « augmenter le nombre de boss ») : roles du jeu verifies
     * dans Assets.zip (Server/NPC/Roles). Les tres grands gardent une taille moderee.
     */
    private static final List<Species> BOSS_ONLY = List.of(
            new Species("Wraith", "Spectre"), new Species("Shadow_Knight", "Chevalier des ombres"), new Species("Ghoul", "Goule"),
            new Species("Zombie_Aberrant_Big", "Aberration", 1.6f), new Species("Skeleton_Elite", "Squelette d'élite"),
            new Species("Skeleton_Archmage", "Archimage squelette"), new Species("Risen_Knight", "Chevalier ressuscité"),
            new Species("Hound_Bleached", "Molosse blanchi"), new Species("Golem_Crystal_Earth", "Golem de cristal de terre", 1.5f),
            new Species("Golem_Crystal_Flame", "Golem de cristal de feu", 1.5f), new Species("Golem_Crystal_Frost", "Golem de cristal de givre", 1.5f),
            new Species("Golem_Crystal_Thunder", "Golem de cristal de foudre", 1.5f), new Species("Golem_Guardian_Void", "Gardien du Vide", 1.3f),
            new Species("Spirit_Ember", "Esprit de braise"), new Species("Spirit_Frost", "Esprit de givre"),
            new Species("Scarak_Broodmother", "Mère-couveuse scarak", 1.3f), new Species("Scarak_Defender", "Défenseur scarak"),
            new Species("Trork_Shaman", "Chaman trork"), new Species("Trork_Brawler", "Cogneur trork"),
            new Species("Outlander_Marauder", "Maraudeur outlander"), new Species("Outlander_Sorcerer", "Sorcier outlander"),
            new Species("Outlander_Priest", "Prêtre outlander"), new Species("Rex_Cave", "Rex des cavernes", 1.2f),
            new Species("Raptor_Cave", "Raptor des cavernes"), new Species("Tiger_Sabertooth", "Tigre à dents de sabre"),
            new Species("Bear_Polar", "Ours polaire"), new Species("Bear_Voidtaken", "Ours du Vide", 1.5f), new Species("Emberwulf", "Emberwulf"),
            new Species("Fen_Stalker", "Rôdeur des marais"), new Species("Crocodile", "Crocodile"),
            new Species("Toad_Rhino_Magma", "Crapaud-rhino de magma"), new Species("Spider_Cave", "Araignée des cavernes"));

    /** Especes d'un boss de donjon ou de zone : celles des monstres etoiles, puis BOSS_ONLY. */
    public static List<Species> bossSpecies() {
        List<Species> all = species();
        all.addAll(BOSS_ONLY);
        return all;
    }

    /** Taille du monstre de ce role (SCALE, ou 1 pour un dragon). */
    static float scaleOf(String role) {
        for (Species s : bossSpecies()) if (s.role().equals(role)) return s.scale();
        return SCALE;
    }

    /** Contrat du jour. mob : UUID du monstre apparu (null : pas encore) ; claimedBy : joueur qui l'a abattu. */
    public static final class Contract {
        final String zoneId, role, species, name;
        final int level;
        int stars;                                                          // monstre etoile (2026-11-16) : 1 a 3, 0 sinon
        final long reward;
        volatile UUID mob;
        volatile String claimedBy;
        volatile long claimedAt;
        transient volatile boolean spawning;
        transient volatile Ref<EntityStore> ref;                             // monstre en vie (thread du monde)
        transient volatile long goneAt;                                     // disparu sans etre reclame (0 : non)
        transient volatile boolean enlarged;                                // modele deja agrandi
        /** Barre de boss (monstre etoile). */
        transient final BossBarView bar = new BossBarView();

        Contract(String zoneId, String role, String species, String name, int level, long reward) {
            this.zoneId = zoneId; this.role = role; this.species = species; this.name = name; this.level = level; this.reward = reward;
        }

        public String getName() { return name; }
        public int getStars() { return stars; }
        /** Nom affiche aux tableaux : « ★★ Grok le Devoreur » pour un monstre etoile. */
        public String title() { return stars > 0 ? name + " (" + stars(stars) + ")" : name; }
        /** « d'elite » ou « etoile » (monstre etoile, 2026-11-16). */
        public String grade() { return stars > 0 ? "étoilé" : "d'élite"; }
        /** Cle de son avis au tableau de prime (quest.chapitres.Primes). */
        public String key() { return "contrat:" + zoneId; }
        public String getSpecies() { return species; }
        public int getLevel() { return level; }
        public long getReward() { return reward; }
        public String getClaimedBy() { return claimedBy; }
        public long getClaimedAt() { return claimedAt; }
        public Zone zone() { return zones.get(zoneId); }
    }

    private static final Map<String, Zone> zones = new ConcurrentHashMap<>();
    private static final List<Contract> today = new CopyOnWriteArrayList<>();
    private static volatile LocalDate day;
    private static int nextZone = 1;
    private static Path file;
    private static final Random RNG = new Random();

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("monster_contracts.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (!LocalDate.now().equals(day)) roll();
                watch();
            } catch (Exception e) {
                EldaniorLogger.error("[Boards] contrats de monstres", e);
            }
        }, 10, 5, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { followLeash(); } catch (Exception e) { EldaniorLogger.error("[Boards] laisse des contrats", e); }
        }, 10, 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Boards] " + zones.size() + " zone(s) de contrat, " + today.size() + " contrat(s) du jour.");
    }

    public static Collection<Zone> getZones() { return zones.values(); }

    public static List<Contract> getContracts() { return today; }

    // ===== Zones (commande admin) =====

    public static Zone addZone(String name, String world, Vector3d pos, int level) {
        Zone z = new Zone("zone_" + nextZone++, name, world, new Vector3d(pos), Math.clamp(level, 1, MAX_LEVEL));
        zones.put(z.id(), z);
        save();
        return z;
    }

    /**
     * Monstre etoile (2026-11-16, fenetre du poste, categorie Hors-la-loi) : zone a la position du poste et son contrat du jour,
     * tout de suite au tableau de prime. Une zone etoilee deja a moins de 10 blocs est remplacee. role null : espece au hasard.
     */
    public static synchronized Contract addStarred(String name, String world, Vector3d pos, int level, int stars, String role) {
        removeNearest(world, pos);
        Zone z = new Zone("zone_" + nextZone++, name, world, new Vector3d(pos), Math.clamp(level, 1, MAX_LEVEL), Math.clamp(stars, 1, 3), role);
        zones.put(z.id(), z);
        if (day == null) day = LocalDate.now();
        Contract c = draw(z);
        today.add(c);
        save();
        return c;
    }

    /** Contrat d'une zone : espece (imposee pour une zone etoilee, sinon au hasard), nom, niveau et recompense selon les etoiles. */
    private static Contract draw(Zone z) {
        Species s = null;
        List<Species> pool = z.stars() > 0 ? species() : SPECIES;                // dragons : monstres etoiles seulement
        if (z.role() != null) for (Species sp : pool) if (sp.role().equals(z.role())) s = sp;
        if (s == null) s = pool.get(RNG.nextInt(pool.size()));
        int level = (int) Math.round(z.level() * eliteFor(z.stars()));
        String name = NAMES[RNG.nextInt(NAMES.length)] + " " + EPITHETS[RNG.nextInt(EPITHETS.length)];
        long reward = Math.round(level * REWARD_PER_LEVEL / 10.0) * 10 * Math.max(1, z.stars());
        Contract c = new Contract(z.id(), s.role(), s.label(), name, level, reward);
        c.stars = z.stars();
        return c;
    }

    /**
     * Zones de monstre etoile a moins de 10 blocs (bloc de poste casse ou « Retirer », 2026-11-16) : retirees avec leur contrat
     * du jour (monstre retire, barre de boss cachee). Les zones ordinaires (/es contrat zone) ne sont pas touchees.
     */
    public static synchronized List<Zone> removeStarredNear(String world, Vector3d pos) {
        List<Zone> out = new ArrayList<>();
        for (Zone z : new ArrayList<>(zones.values())) {
            if (z.stars() <= 0 || !z.world().equals(world) || z.pos().distanceSquared(pos) > 100) continue;
            zones.remove(z.id());
            for (Contract c : today) if (c.zoneId.equals(z.id())) { c.bar.hide(); despawn(c, z.world()); today.remove(c); }
            out.add(z);
            EldaniorLogger.info("[Boards] monstre etoile retire : zone " + z.id() + " (" + z.name() + ")");
        }
        if (!out.isEmpty()) save();
        return out;
    }

    /** Retire la zone la plus proche (10 blocs) ; ses contrats du jour aussi. */
    public static Zone removeNearest(String world, Vector3d pos) {
        Zone best = null;
        double bestD = 100;
        for (Zone z : zones.values())
            if (z.world().equals(world) && z.pos().distanceSquared(pos) <= bestD) { bestD = z.pos().distanceSquared(pos); best = z; }
        if (best == null) return null;
        zones.remove(best.id());
        String id = best.id();
        for (Contract c : today) if (c.zoneId.equals(id)) { c.bar.hide(); despawn(c, best.world()); today.remove(c); }
        save();
        return best;
    }

    // ===== Contrats du jour =====

    /** Nouveaux contrats : PER_DAY zones au hasard, une espece et un nom au hasard pour chacune. */
    public static synchronized void roll() {
        for (Contract c : today) if (c.claimedBy == null) despawn(c);
        today.clear();
        day = LocalDate.now();
        List<Zone> pool = new ArrayList<>();
        for (Zone z : zones.values()) {
            if (z.stars() > 0) today.add(draw(z));                           // monstre etoile : son contrat chaque jour
            else pool.add(z);
        }
        Collections.shuffle(pool, RNG);
        for (Zone z : pool.subList(0, Math.min(PER_DAY, pool.size()))) today.add(draw(z));
        save();
        EldaniorLogger.info("[Boards] " + today.size() + " contrat(s) de monstre pour le " + day);
    }

    /** Monstre de contrat en vie (systemes de plaques : son nom est gere ici). */
    public static boolean isContractMob(Ref<EntityStore> ref, com.hypixel.hytale.component.ComponentAccessor<EntityStore> store) {
        if (today.isEmpty() || ref == null || !ref.isValid()) return false;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        return u != null && isContractMob(u.getUuid());
    }

    /** Monstre de contrat en cours, ou boss de donjon (DungeonBosses) : leur nom est gere par leur systeme. */
    public static boolean isContractMob(UUID uuid) {
        if (uuid == null) return false;
        for (Contract c : today) if (uuid.equals(c.mob) && c.claimedBy == null) return true;
        return DungeonBosses.isBoss(uuid) || WorldBosses.isWorldBoss(uuid);
    }

    /**
     * Toutes les 5 s, pour chaque contrat en cours pres duquel se trouve un joueur : le monstre en vie garde son aura, son nom
     * et sa taille ; disparu, il reapparait apres RESPAWN_DELAY_MS, si personne n'est pres du point d'apparition.
     */
    private static void watch() {
        long now = System.currentTimeMillis();
        orphanCheck();
        for (Contract c : today) {
            Zone z = c.zone();
            if (c.claimedBy != null || z == null || c.spawning) continue;
            World world = Universe.get().getWorld(z.world());
            if (world == null || !playerNear(z, SPAWN_RANGE)) continue;
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> ref = c.ref;
                    if ((ref == null || !ref.isValid()) && c.mob != null) ref = world.getEntityStore().getRefFromUUID(c.mob);   // apres un redemarrage
                    if (ref != null && ref.isValid()) { c.ref = ref; c.goneAt = 0; keep(c, ref, store); return; }
                    if (c.mob != null && c.goneAt == 0) {                                                  // vient de disparaitre
                        c.goneAt = now;
                        c.enlarged = false;
                        EldaniorLogger.info("[Boards] contrat : " + c.name + " a disparu sans etre reclame, retour dans " + RESPAWN_DELAY_MS / 1000 + " s");
                        return;
                    }
                    if (c.mob != null && now - c.goneAt < RESPAWN_DELAY_MS) return;
                    if (playerNear(z, CLEAR_RANGE)) return;                                                // pas sous les yeux d'un joueur
                    spawn(c, z, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Boards] contrat " + c.name, e);
                }
            });
        }
    }

    /**
     * Filet de securite (2026-11-16) : une zone etoilee dont le bloc de poste a disparu (casse sans que l'evenement soit vu,
     * outils de construction...) est retiree avec son contrat et son monstre. Seulement si le bloc est charge (sinon inconnu).
     */
    private static void orphanCheck() {
        for (Zone z : zones.values()) {
            if (z.stars() <= 0 || !playerNear(z, SPAWN_RANGE)) continue;
            World world = Universe.get().getWorld(z.world());
            if (world == null) continue;
            world.execute(() -> {
                Boolean post = com.eldanior.system.guards.NpcPostBlocks.postBlockAt(world, z.pos());
                if (post == null || post) return;
                EldaniorLogger.info("[Boards] zone etoilee " + z.id() + " sans bloc de poste : retiree");
                removeStarredNear(z.world(), z.pos());
                com.eldanior.system.guards.NpcPostBlocks.forget(world, z.pos());
            });
        }
    }

    /** Monstre en vie : aura (l'effet dure 8 s), nom ; taille x2 une seule fois (a l'apparition, ou au rechargement). */
    private static void keep(Contract c, Ref<EntityStore> ref, Store<EntityStore> store) {
        com.eldanior.system.config.Effects.EffectsManager.applyEffect(ref, AURA, store);
        name(c, ref, store);
        if (!c.enlarged) { dress(ref, store, scaleOf(c.role), c.stars > 0 ? starsEmblem(c.stars) : null); c.enlarged = true; }
    }

    /**
     * Nom, reecrit a chaque appel (chaque seconde) meme inchange : ce renvoi garde la barre de vie du jeu affichee au-dessus du
     * nom (ecrit seulement s'il changeait, elle avait disparu, 2026-11-16).
     */
    private static void name(Contract c, Ref<EntityStore> ref, Store<EntityStore> store) {
        store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(nameplate(c)));
    }

    /** Chaque seconde : le point de laisse de chaque monstre en vie suit sa position (jamais ramene a son point d'apparition). */
    private static void followLeash() {
        for (Contract c : today) {
            Ref<EntityStore> ref = c.ref;
            Zone z = c.zone();
            if (c.claimedBy != null || ref == null || !ref.isValid() || z == null) { c.bar.hide(); continue; }
            World world = Universe.get().getWorld(z.world());
            if (world == null) continue;
            world.execute(() -> {
                if (!ref.isValid()) { c.bar.hide(); return; }
                Store<EntityStore> store = ref.getStore();
                NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                if (npc != null && t != null) npc.setLeashPoint(new Vector3d(t.getPosition()));
                name(c, ref, store);                                                  // garde la barre de vie du jeu affichee
                if (c.stars > 0)                                                       // barre de boss des monstres etoiles
                    c.bar.update(ref, store, world, c.title() + " · " + c.species + " Lv." + c.level + rankSuffix(c.level).replace("  ·  ", " · "), c.stars >= 3);
            });
        }
    }

    /**
     * Emblemes flottant au-dessus de la tete (scripts/monstres/gen_emblemes_etoiles.py) : etoiles d'or a 5 branches des monstres
     * etoiles, tete de mort des boss de donjon. Deux versions par embleme (_x1, _x2) : l'attachement prend l'echelle du monstre,
     * la version choisie la compense (meme taille et meme hauteur, ~2,5 blocs au-dessus du pivot de la tete).
     */
    public record Emblem(String model, String texture, int[] scales) {
        Emblem(String model, String texture) { this(model, texture, new int[]{1, 2}); }

        /** Version la plus proche de l'echelle du modele. */
        int variant(float scale) {
            int best = scales[0];
            for (int s : scales) if (Math.abs(s - scale) < Math.abs(best - scale)) best = s;
            return best;
        }
    }

    // Couronnes d'or des Boss Mondiaux : WorldBosses.Event.emblem (1 a 3 couronnes, une version par boss, 2026-11-20).

    public static Emblem starsEmblem(int stars) {
        return new Emblem("NPC/Eldanior/Emblemes/Etoiles_" + Math.clamp(stars, 1, 3) + "_x%d.blockymodel", "NPC/Eldanior/Emblemes/Etoiles_Texture.png");
    }

    public static final Emblem SKULL = new Emblem("NPC/Eldanior/Emblemes/Crane_x%d.blockymodel", "NPC/Eldanior/Emblemes/Crane_Texture.png");

    /**
     * Une seule fois a l'apparition (changer le modele en plein combat le fait sauter) : modele a l'echelle voulue (SCALE ; 1 pour
     * un dragon), memes attachements tires au hasard, et l'embleme (null : aucun) accroche a l'os Head.
     */
    public static void dress(Ref<EntityStore> ref, Store<EntityStore> store, float scale, Emblem emblem) {
        ModelComponent mc = store.getComponent(ref, ModelComponent.getComponentType());
        if (mc == null || mc.getModel() == null) return;
        Model cur = mc.getModel();
        boolean grow = scale > 1.0f && cur.getScale() < scale * 0.95f;
        if (!grow && emblem == null) return;
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(cur.getModelAssetId());
        if (asset == null) return;
        Model m = grow ? Model.createScaledModel(asset, scale, cur.getRandomAttachmentIds()) : cur;
        if (emblem != null) {
            int variant = emblem.variant(m.getScale());                                     // version la plus proche de l'echelle
            java.util.List<com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment> atts = new java.util.ArrayList<>();
            if (m.getAttachments() != null)
                for (var a : m.getAttachments()) if (a.getModel() == null || !a.getModel().startsWith("NPC/Eldanior/Emblemes/")) atts.add(a);
            atts.add(new com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment(
                    String.format(emblem.model(), variant), emblem.texture(), null, null, 1));
            m = new Model(m.getModelAssetId(), m.getScale(), m.getRandomAttachmentIds(),
                    atts.toArray(new com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment[0]), m.getBoundingBox(),
                    m.getModel(), m.getTexture(), m.getGradientSet(), m.getGradientId(), m.getEyeHeight(), m.getCrouchOffset(),
                    m.getSittingOffset(), m.getSleepingOffset(), m.getAnimationSetMap(), m.getCamera(), m.getLight(), m.getParticles(),
                    m.getTrails(), m.getPhysicsValues(), m.getDetailBoxes(), m.getPhobia(), m.getPhobiaModelAssetId());
        }
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(m));
    }

    /** Un joueur dans le monde de la zone a moins de range blocs du point (positions relevees par PlayerPositionTracker). */
    private static boolean playerNear(Zone z, double range) {
        return playerNear(z.world(), z.pos(), range);
    }

    public static boolean playerNear(String world, Vector3d pos, double range) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d at = w != null && w.getName().equals(world)
                          ? com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (at != null && at.distanceSquared(pos) <= range * range) return true;
        }
        return false;
    }

    private static void spawn(Contract c, Zone z, Store<EntityStore> store) {
        c.spawning = true;
        try {
            var pair = NPCPlugin.get().spawnNPC(store, c.role, null, new Vector3d(z.pos()), new Rotation3f(0, RNG.nextFloat() * 360f, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Boards] monstre de contrat non cree : " + c.role); return; }
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(c.level, c.role, true));
            keep(c, ref, store);
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            c.mob = u != null ? u.getUuid() : null;
            c.ref = ref;
            c.goneAt = 0;
            save();
            EldaniorLogger.info("[Boards] contrat : " + c.name + " (" + c.role + " niv. " + c.level + ") apparait a " + z.name());
        } finally {
            c.spawning = false;
        }
    }

    private static void despawn(Contract c) {
        Zone z = c.zone();
        if (z != null) despawn(c, z.world());
    }

    /**
     * Retire le monstre du contrat. Le monde est donne a part : quand on retire une zone, elle est deja hors de la table
     * (c.zone() null) ; avant le 2026-11-16 le monstre restait alors en place (dragon etoile toujours la apres la casse du poste).
     */
    private static void despawn(Contract c, String worldName) {
        World world = Universe.get().getWorld(worldName);
        if (world == null || (c.mob == null && c.ref == null)) return;
        UUID mob = c.mob;
        Ref<EntityStore> known = c.ref;
        world.execute(() -> {
            Ref<EntityStore> ref = known != null && known.isValid() ? known : mob != null ? world.getEntityStore().getRefFromUUID(mob) : null;
            if (ref != null && ref.isValid()) {
                world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
                EldaniorLogger.info("[Boards] monstre du contrat " + c.name + " retire");
            } else EldaniorLogger.warn("[Boards] monstre du contrat " + c.name + " introuvable (" + mob + ")");
        });
    }

    static String nameplate(Contract c) {
        String rank = rankSuffix(c.level).replace("  ·  ", " · ");
        if (c.stars > 0) return c.name + " · " + c.species + " étoilé, " + stars(c.stars) + " · Lv." + c.level + rank;   // monstre etoile
        return c.name + " · " + c.species + " d'élite Lv." + c.level + rank;
    }

    /** Monstre abattu par un joueur (DeathXPSystem) : la recompense si c'est un contrat en cours (0 sinon), annonce. */
    /** Cle de l'avis du contrat dont ce monstre est la cible (Primes), null si ce n'en est pas un. */
    public static String keyOf(UUID mob) {
        if (mob == null) return null;
        for (Contract c : today) if (mob.equals(c.mob) && c.claimedBy == null) return c.key();
        return null;
    }

    public static long claim(UUID victim, String killer) {
        for (Contract c : today) {
            if (victim == null || !victim.equals(c.mob) || c.claimedBy != null) continue;
            c.claimedBy = killer;
            c.bar.hide();
            c.claimedAt = System.currentTimeMillis();
            save();
            Zone z = c.zone();
            BountyBoard.announce("[Contrat] " + killer + " a abattu " + c.name + " (" + c.species + " niv. " + c.level + ")"
                    + (z != null ? " à " + z.name() : "") + " et touche " + BoardScreen.gold(c.reward) + " or.");
            return c.reward;
        }
        return 0;
    }

    // ===== Persistance =====

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            props.setProperty("nextZone", String.valueOf(nextZone));
            if (day != null) props.setProperty("day", day.toString());
            for (Zone z : zones.values()) {
                String k = "zone." + z.id() + ".";
                props.setProperty(k + "name", z.name());
                props.setProperty(k + "world", z.world());
                props.setProperty(k + "pos", z.pos().x + "," + z.pos().y + "," + z.pos().z);
                props.setProperty(k + "level", String.valueOf(z.level()));
                if (z.stars() > 0) props.setProperty(k + "stars", String.valueOf(z.stars()));
                if (z.role() != null) props.setProperty(k + "role", z.role());
            }
            for (int i = 0; i < today.size(); i++) {
                Contract c = today.get(i);
                String k = "contract." + i + ".";
                props.setProperty(k + "zone", c.zoneId);
                props.setProperty(k + "role", c.role);
                props.setProperty(k + "species", c.species);
                props.setProperty(k + "name", c.name);
                props.setProperty(k + "level", String.valueOf(c.level));
                props.setProperty(k + "reward", String.valueOf(c.reward));
                if (c.stars > 0) props.setProperty(k + "stars", String.valueOf(c.stars));
                if (c.mob != null) props.setProperty(k + "mob", c.mob.toString());
                if (c.claimedBy != null) {
                    props.setProperty(k + "claimedBy", c.claimedBy);
                    props.setProperty(k + "claimedAt", String.valueOf(c.claimedAt));
                }
            }
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - contrats de monstres");
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] sauvegarde des contrats", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            nextZone = Integer.parseInt(props.getProperty("nextZone", "1"));
            String d = props.getProperty("day");
            day = d != null ? LocalDate.parse(d) : null;
            for (String key : props.stringPropertyNames()) {
                if (!key.startsWith("zone.") || !key.endsWith(".name")) continue;
                String id = key.substring(5, key.length() - 5), k = "zone." + id + ".";
                String[] c = props.getProperty(k + "pos", "0,0,0").split(",");
                zones.put(id, new Zone(id, props.getProperty(key), props.getProperty(k + "world", "default"),
                        new Vector3d(Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2])),
                        Integer.parseInt(props.getProperty(k + "level", "50")),
                        Integer.parseInt(props.getProperty(k + "stars", "0")), migrateRole(props.getProperty(k + "role"))));
            }
            for (int i = 0; props.getProperty("contract." + i + ".zone") != null; i++) {
                String k = "contract." + i + ".";
                Contract c = new Contract(props.getProperty(k + "zone"), migrateRole(props.getProperty(k + "role")), props.getProperty(k + "species"),
                        props.getProperty(k + "name"), Integer.parseInt(props.getProperty(k + "level")), Long.parseLong(props.getProperty(k + "reward")));
                String mob = props.getProperty(k + "mob");
                if (mob != null) c.mob = UUID.fromString(mob);
                c.claimedBy = props.getProperty(k + "claimedBy");
                c.claimedAt = Long.parseLong(props.getProperty(k + "claimedAt", "0"));
                c.stars = Integer.parseInt(props.getProperty(k + "stars", "0"));
                today.add(c);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement des contrats", e);
        }
    }
}
