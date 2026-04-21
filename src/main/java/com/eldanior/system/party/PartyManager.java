package com.eldanior.system.party;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {

    private static final Map<UUID, Party> playerParty = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public static void init() {
        System.out.println("[Eldanior] Systeme de Groupe initialise.");
    }

    // ==================== CREATION ====================

    public static Party createParty(UUID captainUUID, String captainName) {
        if (playerParty.containsKey(captainUUID)) return null;
        Party party = new Party(captainUUID, captainName);
        playerParty.put(captainUUID, party);

        return party;
    }

    // ==================== GETTERS ====================

    public static Party getParty(UUID playerUUID) {
        return playerParty.get(playerUUID);
    }

    public static boolean hasParty(UUID playerUUID) {
        return playerParty.containsKey(playerUUID);
    }

    // ==================== MEMBERSHIP ====================

    public static boolean joinParty(UUID playerUUID, String playerName, Party party) {
        if (party.isFull() || playerParty.containsKey(playerUUID)) return false;
        if (!party.addMember(playerUUID, playerName)) return false;
        playerParty.put(playerUUID, party);

        return true;
    }

    public static void leaveParty(UUID playerUUID) {
        Party party = playerParty.remove(playerUUID);
        if (party == null) return;

        removeHudByUUID(playerUUID);

        party.removeMember(playerUUID);

        if (party.getSize() == 0) return;

        if (party.getSize() == 1) {
            UUID lastMember = party.getMemberUUIDs().get(0);
            removeHudByUUID(lastMember);
            playerParty.remove(lastMember);
            party.removeMember(lastMember);
            return;
        }
    }

    public static void disbandParty(Party party) {
        for (UUID member : party.getMemberUUIDs()) {
            removeHudByUUID(member);
            playerParty.remove(member);
        }
        party.getMembers().clear();
    }

    // ==================== INVITATIONS ====================

    public static void sendInvite(UUID targetUUID, UUID captainUUID) {
        pendingInvites.put(targetUUID, captainUUID);
    }

    public static UUID getPendingInvite(UUID targetUUID) {
        return pendingInvites.get(targetUUID);
    }

    public static void clearInvite(UUID targetUUID) {
        pendingInvites.remove(targetUUID);
    }

    public static boolean hasPendingInvite(UUID targetUUID) {
        return pendingInvites.containsKey(targetUUID);
    }

    // ==================== DECONNEXION ====================

    public static void handleDisconnect(UUID playerUUID) {
        leaveParty(playerUUID);
        pendingInvites.remove(playerUUID);
    }

    // ==================== HUD ====================

    // HUD doit etre mis a jour depuis la commande qui a acces au Player

    public static void showHudForPlayer(Player player, UUID playerUUID) {
        try {
            var ref = player.getReference();
            if (ref == null) {
                System.out.println("[Party] HUD: player.getReference() est null");
                return;
            }
            var store = ref.getStore();
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) {
                System.out.println("[Party] HUD: PlayerRef component est null");
                return;
            }

            var hudManager = player.getHudManager();
            if (hudManager == null) {
                System.out.println("[Party] HUD: hudManager est null");
                return;
            }

            PartyHud hud = new PartyHud(playerRef, playerUUID);
            hudManager.setCustomHud(playerRef, hud);
            System.out.println("[Party] HUD affiche pour " + player.getDisplayName());
        } catch (Exception e) {
            System.out.println("[Party] Erreur HUD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeHudForPlayer(Player player, UUID playerUUID) {
        try {
            var ref = player.getReference();
            if (ref == null) return;
            var store = ref.getStore();
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;

            var hudManager = player.getHudManager();
            if (hudManager == null) return;

            hudManager.setCustomHud(playerRef, null);
        } catch (Exception ignored) {}
    }

    public static void removeHudByUUID(UUID playerUUID) {
        try {
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            if (playerRef == null) return;
            playerRef.getPacketHandler().writeNoCache(new CustomHud(true, null));
        } catch (Exception ignored) {}
    }
}