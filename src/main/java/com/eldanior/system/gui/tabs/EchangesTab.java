package com.eldanior.system.gui.tabs;

import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.trade.TradeManager;
import com.eldanior.system.trade.TradeSession;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import java.util.*;

public class EchangesTab {

    public static final int MAX_TRADE_SLOTS = 8;
    private static final double MAX_DISTANCE = 15.0;
    private static final java.util.concurrent.ConcurrentHashMap<UUID, List<String>> playerCachedNames = new java.util.concurrent.ConcurrentHashMap<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, boolean isAdmin) {
        // Recuperer l'UUID du joueur
        UUID myUUID = null;
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                                myUUID = UUIDExtractor.getUUID(pRef);
            }
        } catch (Exception e) { EldaniorLogger.error("EchangesTab", e); }

        // Recuperer la position du joueur
        Vector3d myPos = myUUID != null ? PlayerPositionTracker.PLAYER_POSITIONS.get(myUUID) : null;

        // Lister les joueurs a proximite
        UUID stateUUID = myUUID != null ? myUUID : new UUID(0, 0);
        List<String> cachedPlayerNames = playerCachedNames.computeIfAbsent(stateUUID, k -> new ArrayList<>());
        cachedPlayerNames.clear();
        List<UUID> nearbyPlayers = new ArrayList<>();

        for (Map.Entry<UUID, Vector3d> entry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            UUID otherUUID = entry.getKey();
            // Admin peut se voir lui-meme pour tester seul
            if (otherUUID.equals(myUUID) && !isAdmin) continue;

            if (myPos != null && !otherUUID.equals(myUUID)) {
                Vector3d otherPos = entry.getValue();
                double dx = myPos.x - otherPos.x;
                double dy = myPos.y - otherPos.y;
                double dz = myPos.z - otherPos.z;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > MAX_DISTANCE) continue;
            }

            nearbyPlayers.add(otherUUID);
        }

        // Afficher les joueurs
        for (int i = 0; i < MAX_TRADE_SLOTS; i++) {
            if (i < nearbyPlayers.size()) {
                UUID otherUUID = nearbyPlayers.get(i);
                PlayerRef otherRef = Universe.get().getPlayer(otherUUID);
                String name = otherRef != null ? otherRef.getUsername() : "???";
                cachedPlayerNames.add(name);

                ui.set("#EchSlot" + i + ".Visible", true);
                ui.set("#EchName" + i + ".Text", name);

                boolean inTrade = TradeManager.isInTrade(otherUUID);
                ui.set("#EchBtn" + i + ".Visible", !inTrade);
                ui.set("#EchForceBtn" + i + ".Visible", isAdmin && !inTrade);
                ui.set("#EchStatus" + i + ".Text", inTrade ? "En echange..." : "");
            } else {
                cachedPlayerNames.add("");
                ui.set("#EchSlot" + i + ".Visible", false);
            }
        }

        ui.set("#EchInfoLabel.Text", nearbyPlayers.isEmpty()
                ? "Aucun joueur a proximite (15 blocs)."
                : nearbyPlayers.size() + " joueur(s) a proximite.");
    }

    public static boolean handleInvite(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID callerUUID = null;
        try {
            com.hypixel.hytale.server.core.universe.PlayerRef pRef = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRef != null) callerUUID = UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { /* skip */ }
        UUID stateUUID = callerUUID != null ? callerUUID : new UUID(0, 0);
        List<String> cachedPlayerNames = playerCachedNames.getOrDefault(stateUUID, new ArrayList<>());
        if (index < 0 || index >= cachedPlayerNames.size()) return false;
        String targetName = cachedPlayerNames.get(index);
        if (targetName == null || targetName.isEmpty()) return false;

        try {
            // UUID du marchand
            PlayerRef myPRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (myPRef == null) return false;
                        UUID myUUID = UUIDExtractor.getUUID(myPRef);

            // UUID de la cible
            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) return false;
            UUID targetUUID = UUIDExtractor.getUUID(targetRef);

            if (TradeManager.isInTrade(myUUID) || TradeManager.isInTrade(targetUUID)) return false;

            boolean sent = TradeManager.sendInvite(myUUID, targetUUID);
            if (sent) {
                com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (player != null) {
                    player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Demande d'echange envoyee a " + targetName));
                }
                targetRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("" + myPRef.getUsername() + " veut echanger avec vous !"));
                targetRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Tapez /es trade accept ou /es trade decline"));
                return true;
            }
        } catch (Exception e) {
            System.err.println("[Echanges] Erreur invite: " + e.getMessage());
        }
        return false;
    }

    public static boolean handleForceOpen(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID callerUUID = null;
        try {
            com.hypixel.hytale.server.core.universe.PlayerRef pRef = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRef != null) callerUUID = UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { /* skip */ }
        UUID stateUUID = callerUUID != null ? callerUUID : new UUID(0, 0);
        List<String> cachedPlayerNames = playerCachedNames.getOrDefault(stateUUID, new ArrayList<>());
        if (index < 0 || index >= cachedPlayerNames.size()) return false;
        String targetName = cachedPlayerNames.get(index);
        if (targetName == null || targetName.isEmpty()) return false;

        try {
            PlayerRef myPRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (myPRef == null) return false;
                        UUID myUUID = UUIDExtractor.getUUID(myPRef);

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) return false;
            UUID targetUUID = UUIDExtractor.getUUID(targetRef);

            if (TradeManager.isInTrade(myUUID) || TradeManager.isInTrade(targetUUID)) return false;

            // Demarrer directement sans invitation
            TradeSession session = TradeManager.startTrade(myUUID, targetUUID);

            // Ouvrir la fenetre pour l'admin
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new com.eldanior.system.trade.TradeScreen(myPRef, myUUID, session));
                player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Echange force avec " + targetName + " !"));
            }

            // Ouvrir la fenetre pour la cible
            var targetEntRef = targetRef.getReference();
            if (targetEntRef != null) {
                var targetStore = targetEntRef.getStore();
                com.hypixel.hytale.server.core.entity.entities.Player targetPlayer = targetStore.getComponent(targetEntRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (targetPlayer != null) {
                    targetPlayer.getPageManager().openCustomPage(targetEntRef, targetStore,
                            new com.eldanior.system.trade.TradeScreen(targetRef, targetUUID, session));
                }
            }
            targetRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Un admin a ouvert un echange avec vous !"));
            return true;
        } catch (Exception e) {
            System.err.println("[Echanges] Erreur force open: " + e.getMessage());
        }
        return false;
    }

    /** Nettoyage memoire quand un joueur se deconnecte */
    public static void cleanupPlayer(java.util.UUID uuid) {
        playerCachedNames.remove(uuid);
    }
}
