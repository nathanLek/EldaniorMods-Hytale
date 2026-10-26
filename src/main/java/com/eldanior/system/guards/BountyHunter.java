package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.duel.DuelManager;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.territory.ArenaManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Chasseur de prime (GuardType.CHASSEUR_PRIME) : erre dans 200 blocs autour de son poste comme le chevalier errant
 * (PostMovement) et traque les joueurs PK. Trois types (guerrier, archer, assassin) : roles Eldanior_Npc_Chasseur_Prime[_Archer
 * |_Assassin], equipement des aventuriers selon son rang (AdventurerType).
 *   - Un PK a moins de {@link #SIGHT} blocs (hors duel et arene) : attitude hostile forcee et appel au combat. Les autres joueurs
 *     sont ignores ; les monstres seulement s'ils l'attaquent (groupe d'attitude Eldanior_Hunter sans hostiles : le gabarit
 *     riposte aux coups).
 *   - Il tue un PK : la prime est effacee, {@link #TREASURY_SHARE} va au tresor de la ville (sinon du territoire / royaume) ou
 *     a lieu le kill (a defaut, celle de son poste), et tous les joueurs sont prevenus.
 */
public final class BountyHunter {

    private BountyHunter() {}

    static final double SIGHT = 30.0, TREASURY_SHARE = 0.5;
    /** Duree de l'attitude hostile forcee, renouvelee a chaque passage (2 s) tant que le PK est en vue. */
    private static final double HOSTILE_SECONDS = 15.0;

    /** Role selon son type (guerrier par defaut). */
    static String role(GuardManager.Post p) {
        String base = GuardType.CHASSEUR_PRIME.getRoleId();
        return switch (AdventurerType.of(p.skinSeed)) {
            case ARCHER -> base + "_Archer";
            case ASSASSIN -> base + "_Assassin";
            default -> base;
        };
    }

    /** Types possibles : guerrier, archer, assassin (pas de mage). */
    static boolean isHunterType(AdventurerType t) {
        return t != AdventurerType.MAGE;
    }

    // ===== Chasse =====

    /** Toutes les 2 s (thread du monde) : le PK ou le criminel le plus proche a moins de SIGHT blocs devient sa cible. */
    static void hunt(Ref<EntityStore> hunter, Vector3d at, Store<EntityStore> store) {
        Ref<EntityStore> best = null;
        double bestD = SIGHT * SIGHT;
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, SIGHT, store))) {
            if (!isWantedPK(e, store) && !isCriminal(e, store)) continue;
            TransformComponent t = store.getComponent(e, TransformComponent.getComponentType());
            if (t == null) continue;
            double d = t.getPosition().distanceSquared(at);
            if (d < bestD) { bestD = d; best = e; }
        }
        if (best == null) return;
        NpcAttitudes.hostile(hunter, best, HOSTILE_SECONDS, store);
        GuardManager.call(hunter, best, store);
    }

    /** Joueur PK, hors duel et arene. */
    private static boolean isWantedPK(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid() || store.getComponent(ref, Player.getComponentType()) == null) return false;
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || !data.isPK()) return false;
        UUID u = PostMovement.uuid(ref, store);
        return u != null && !DuelManager.isInDuel(u) && !ArenaManager.isInArena(u);
    }

    /** Criminel PNJ (Criminals). */
    private static boolean isCriminal(Ref<EntityStore> ref, Store<EntityStore> store) {
        GuardManager.Post p = ref != null && ref.isValid() ? GuardManager.postOf(ref, store) : null;
        return p != null && p.type == GuardType.CRIMINEL;
    }

    // ===== Kill d'un PK ou d'un criminel =====

    /**
     * Entite tuee (DeathXPSystem, thread du monde). False si le tueur n'est pas un chasseur de prime ; sinon true (aucune autre
     * recompense) et, si la victime est un PK ou un criminel : prime effacee (criminel : remise a sa base), part au tresor de la
     * ville, annonce.
     */
    public static boolean onKill(UUID killer, UUID victim, Ref<EntityStore> victimRef, Store<EntityStore> store,
                                 CommandBuffer<EntityStore> cb) {
        GuardManager.Post p = GuardManager.hunterPost(killer);
        if (p == null) return false;
        String target;
        long bounty;
        if (store.getComponent(victimRef, Player.getComponentType()) == null) {
            GuardManager.Post c = GuardManager.postOf(victimRef, store);
            if (c == null || c.type != GuardType.CRIMINEL) return true;                          // monstre : rien a faire
            target = "le criminel " + Criminals.name(c);
            bounty = Criminals.bounty(c);
            c.bounty = 0;
            GuardManager.saveLater();
        } else {
            ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(victimRef, lvlType);
            if (data == null || !data.isPK()) return true;
            bounty = data.getBounty();
            // Apres la penalite de mort (meme tampon) : on efface la prime sur la version deja penalisee
            cb.run(s -> {
                PlayerLevelData current = s.getComponent(victimRef, lvlType);
                PlayerLevelData copy = current != null ? (PlayerLevelData) current.clone() : null;
                if (copy == null) return;
                copy.collectBounty();
                s.putComponent(victimRef, lvlType, copy);
            });
            QuestManager.unregisterBounty(victim);
            PlayerRef pr = Universe.get().getPlayer(victim);
            target = pr != null ? pr.getUsername() : "un criminel";
        }

        long share = Math.round(bounty * TREASURY_SHARE);
        World world = store.getExternalData().getWorld();
        TransformComponent t = store.getComponent(victimRef, TransformComponent.getComponentType());
        ParcelData town = world != null && t != null ? treasuryAt(world.getName(), new Vector3d(t.getPosition())) : null;
        if (town == null) town = treasuryAt(p.world, p.position());
        if (town != null && share > 0) {
            town.addTreasury(share);
            ParcelManager.save();
        }

        String hunter = GuardManager.firstName(p) + " [" + getAdventureRank(p.level) + "]";
        String text = "[Prime] Le chasseur de prime " + hunter + " a eu la peau de " + target + " !"
                + (bounty > 0 ? " Sa prime de " + bounty + " or est effacee" : "")
                + (town != null && share > 0 ? ", " + share + " or vont au tresor de " + town.getName() + "." : bounty > 0 ? "." : "");
        for (PlayerRef other : Universe.get().getPlayers()) other.sendMessage(Message.raw(text).color(Color.ORANGE));
        EldaniorLogger.info("[Guards] " + p.id + " a tue " + target + " (prime " + bounty + ", tresor "
                + (town != null ? town.getName() + " +" + share : "aucun") + ")");
        return true;
    }

    /** Parcelle qui a un tresor (ville, territoire, royaume) a cette position : la plus petite, ou l'une de ses parentes. */
    public static ParcelData treasuryAt(String world, Vector3d pos) {
        ParcelData parcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);
        for (int depth = 0; parcel != null && depth < 10; depth++) {
            switch (parcel.getType()) {
                case CITY, TERRITORY, GRAND_TERRITORY, KINGDOM -> { return parcel; }
                default -> { }
            }
            parcel = parcel.getParentId() != null ? ParcelManager.get(parcel.getParentId()) : null;
        }
        return null;
    }
}
