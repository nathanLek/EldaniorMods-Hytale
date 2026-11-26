package com.eldanior.system.failles;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Une faille ouverte : le bloc dans le monde et, une fois quelqu'un entre, son donjon (monde d'instance). */
public final class Faille {

    public enum State { OUVERTE, EN_COURS, FERMEE, RATEE }

    /** Comment elle est apparue : naturelle (minuteur), faille scellee posee par un joueur, panneau d'administration. */
    public enum Source { NATURELLE, SCELLEE, ADMIN }

    final String id;
    final String world;
    final int x, y, z;
    final FailleRank rank;
    final String dungeonId;
    final int groupSize;
    final Source source;
    final long openedAt;
    long expireAt;
    volatile State state = State.OUVERTE;
    /** Premier entre (chef) ; s'il est dans un groupe, seuls ses membres peuvent le suivre. */
    UUID leader;
    final Set<UUID> entered = ConcurrentHashMap.newKeySet();
    final Map<UUID, String> names = new ConcurrentHashMap<>();

    // ---- donjon (instance) ----
    volatile CompletableFuture<World> ready;
    volatile String instanceName;
    volatile long readyAt, endAt;
    /** Jusqu'a quand les PNJ qui apparaissent viennent des pieces collees (retires, voir FailleNpcSystem). */
    volatile long pastingUntil;
    /** Instance fixe (modele construit, retouche a la main) : monstres des balises NPC_Spawn_Marker, rien de colle. */
    volatile boolean fixed;
    volatile Vector3d bossLastPos;
    /** Structure d'apparition : ancre (bloc de sol), etat des blocs avant (dx,dy,dz,id,rotation), heure d'ouverture. */
    int[] anchor;
    List<String> snapshot;
    volatile long openAt;
    volatile boolean revealed, restored, markersPlaced;
    volatile UUID bossDeadUuid;
    final Set<String> activeRooms = new HashSet<>();
    /** Monstres de la faille (uuid -> salle) ; monstres etoiles (uuid -> etoiles) ; mini-boss et boss. */
    final Map<UUID, String> mobs = new ConcurrentHashMap<>();
    final Map<UUID, Integer> starred = new ConcurrentHashMap<>();
    final Map<UUID, String> titles = new ConcurrentHashMap<>();
    volatile UUID miniboss, boss;
    volatile Ref<EntityStore> bossRef, minibossRef;
    volatile boolean barrierOpen, bossDead, reinforced, warned;
    volatile long bossAwakeAt, bossFightAt, closeInstanceAt;
    /** Mort du boss en attente : une nouvelle phase (nouvelle entite) a 3 s pour apparaitre, sinon c'est la victoire. */
    volatile long bossDeathAt;
    volatile String bossKiller = "?";
    volatile int bossLevel;
    volatile float bossScale = 1f;
    /** Boss retenu a l'ouverture : celui du modele (boss.json, poste pose en edition) ou celui du plan. */
    volatile String bossRole, bossName;
    /** Phase de boss adoptee, a habiller (niveau, taille, crane) au prochain passage. */
    volatile UUID dressBoss;
    volatile boolean keyGiven;
    volatile long overflowAt;
    final com.eldanior.system.boards.BossBarView bossBar = new com.eldanior.system.boards.BossBarView();
    final com.eldanior.system.boards.BossBarView miniBar = new com.eldanior.system.boards.BossBarView();

    // ---- dehors ----
    volatile Ref<EntityStore> hologram;
    /** Debordement : monstres sortis de la faille. */
    final Set<UUID> overflowMobs = ConcurrentHashMap.newKeySet();

    Faille(String id, String world, int x, int y, int z, FailleRank rank, String dungeonId, int groupSize, Source source, long openedAt, long expireAt) {
        this.id = id; this.world = world; this.x = x; this.y = y; this.z = z; this.rank = rank; this.dungeonId = dungeonId;
        this.groupSize = Math.max(1, Math.min(10, groupSize)); this.source = source; this.openedAt = openedAt; this.expireAt = expireAt;
    }

    FailleDungeon dungeon() { return FailleDungeon.get(dungeonId); }

    int entriesLeft() { return Math.max(0, groupSize - entered.size()); }

    String coords() { return "X " + x + ", Z " + z; }

    public String getId() { return id; }
    public State getState() { return state; }
    public FailleRank getRank() { return rank; }
    public String getWorld() { return world; }
    public Source getSource() { return source; }
    public String coordsLabel() { return coords(); }
    public String entriesLabel() { return "Entrées : " + entriesLeft() + "/" + groupSize; }
    public String enteredNames() { return names.isEmpty() ? "personne" : String.join(", ", names.values()); }

    public String statusLabel(long now) {
        if (!revealed) return "s'ouvre dans " + Math.max(0, (openAt - now + 999) / 1000) + " s";
        if (state == State.EN_COURS) return "donjon en cours (" + entered.size() + " joueur(s))";
        return "ouverte, se referme dans " + Math.max(0, (expireAt - now) / 60_000) + " min";
    }

    public String title() {
        FailleDungeon d = dungeon();
        return "Faille de rang " + rank.label + (d != null ? " · " + d.name : "");
    }
}
