package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Maison royale (2026-11-13, demande de l'utilisateur) : « le Roi devient automatiquement Patriarche d'Eldanior lorsqu'il devient
 * Roi ». Appele apres chaque changement de rang (commande, menu admin, objet) et a la connexion : un Roi qui n'est pas Patriarche
 * d'Eldanior le devient (il quitte sa maison ; s'il en etait le Patriarche, elle redevient libre) ; un ancien Roi encore
 * Patriarche d'Eldanior redevient Noble de la maison.
 */
public final class RoyalHouse {

    private RoyalHouse() {}

    public static final String ID = "eldanior";

    /**
     * Met data en accord avec le rang (sans l'ecrire dans le store : l'appelant le fait). true si data a change.
     * ref / store : le joueur (pour son nom et son UUID).
     */
    public static boolean sync(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        if (data == null || ref == null || store == null) return false;
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        UUID me;
        try { me = pr != null ? UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { me = null; }
        if (me == null) return false;
        boolean king = NobilityRank.fromString(data.getNobilityRank()) == NobilityRank.ROI;
        String fam = data.getNobleFamilyId() != null ? FamilyManager.migrateId(data.getNobleFamilyId()) : "";
        boolean royalHead = ID.equals(fam) && data.isPatriarch();
        try {
            if (king && !royalHead) {
                crown(me, pr.getUsername(), data, fam);
                pr.sendMessage(Message.raw("Vous êtes désormais le Patriarche de la maison royale d'Eldanior."));
                return true;
            }
            if (!king && royalHead && !me.equals(NobilityManager.getCurrentKingUUID())) {   // ancien Roi : Noble de la maison
                data.setStatus(FamilyRole.NOBLE.code());
                if (me.equals(royalPatriarch)) royalPatriarch = null;
                pr.sendMessage(Message.raw("Vous n'êtes plus Roi : vous restez Noble de la maison d'Eldanior."));
                return true;
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Maison royale] " + pr.getUsername(), e);
        }
        return false;
    }

    /** Dernier Patriarche d'Eldanior installe par sync (pour retirer le titre a l'ancien Roi s'il est en ligne). */
    private static volatile UUID royalPatriarch;

    private static void crown(UUID me, String name, PlayerLevelData data, String oldFamily) {
        if (!oldFamily.isEmpty() && !ID.equals(oldFamily) && data.isPatriarch()) {    // sa maison redevient libre
            FamilyManager.releaseFamily(oldFamily);
            ParcelManager.clearFamilyParcelsOwnership(oldFamily);
        }
        demoteOnlineHead(me);
        data.setNobleFamilyId(ID);
        data.setStatus(FamilyRole.PATRIARCH.code());
        FamilyManager.claimFamily(ID);
        ParcelManager.transferFamilyParcelsOwnership(ID, me, name);
        FamilyRegistry.log(ID, name + " est couronné : Patriarche de la maison royale");
        royalPatriarch = me;
        if (!me.equals(NobilityManager.getCurrentKingUUID())) NobilityManager.setKing(me, name);
    }

    /**
     * Un Roi est elu (NobilityManager.setKing, 2026-11-13) : grand titre et message a tous les joueurs en ligne ; la quete
     * principale (« L'Epopee d'un Roi ») se ferme pour tout le monde (ChapterQuests.onKingElected).
     */
    public static void announce(UUID king, String name) {
        String who = name != null && !name.isBlank() ? name : "Un nouveau souverain";
        for (PlayerRef pr : Universe.get().getPlayers()) {
            try {
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "UN ROI EST ÉLU", who + " règne désormais sur Eldanior", true);
                pr.sendMessage(Message.raw("Les cloches sonnent dans tout le royaume : " + who + " est élu Roi d'Eldanior. "
                        + "L'Épopée d'un Roi s'achève pour tous."));
            } catch (Exception e) {
                EldaniorLogger.error("[Maison royale] annonce", e);
            }
        }
        EldaniorLogger.info("[Maison royale] " + who + " est elu Roi");
        com.eldanior.system.quest.chapitres.ChapterQuests.onKingElected();
    }

    /** L'ancien Patriarche d'Eldanior, s'il est en ligne, redevient Noble tout de suite (sinon a sa prochaine connexion). */
    private static void demoteOnlineHead(UUID newKing) {
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        for (PlayerRef other : Universe.get().getPlayers()) {
            try {
                if (other == null || newKing.equals(UUIDExtractor.getUUID(other))) continue;
                Ref<EntityStore> r = other.getReference();
                if (r == null || !r.isValid()) continue;
                Store<EntityStore> s = r.getStore();
                PlayerLevelData d = s.getComponent(r, type);
                if (d == null || !d.isPatriarch() || !ID.equals(FamilyManager.migrateId(d.getNobleFamilyId()))) continue;
                PlayerLevelData copy = (PlayerLevelData) d.clone();
                if (copy == null) continue;
                copy.setStatus(FamilyRole.NOBLE.code());
                s.putComponent(r, type, copy);
                other.sendMessage(Message.raw("Un nouveau Roi est couronné : vous restez Noble de la maison d'Eldanior."));
            } catch (Exception e) {
                EldaniorLogger.error("[Maison royale] ancien Patriarche", e);
            }
        }
    }
}
