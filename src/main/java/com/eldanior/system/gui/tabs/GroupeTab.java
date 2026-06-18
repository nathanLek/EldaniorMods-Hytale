package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class GroupeTab {

    public static final int MAX_MEMBER_SLOTS = 5;
    public static final int MAX_INVITE_SLOTS = 10;

    // Cache des noms invitables pour le mapping index -> nom
    private static final List<String> cachedInviteNames = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID playerUUID = getPlayerUUID(ref, store);
        if (playerUUID == null) return;

        boolean hasParty = PartyManager.hasParty(playerUUID);
        Party party = hasParty ? PartyManager.getParty(playerUUID) : null;
        boolean isCaptain = party != null && party.isCaptain(playerUUID);

        // Toggle panels
        ui.set("#GroupeNoParty.Visible", !hasParty);
        ui.set("#GroupeHasParty.Visible", hasParty);

        if (!hasParty) return;

        // Header
        ui.set("#GroupeTitle.Text", "GROUPE (" + party.getSize() + "/" + Party.MAX_MEMBERS + ")");

        // Captain actions + invite section
        ui.set("#GroupeCaptainActions.Visible", isCaptain);
        ui.set("#GroupeInviteSection.Visible", isCaptain && !party.isFull());

        // Populate online players for invite (captain only)
        if (isCaptain && !party.isFull()) {
            populateInviteList(ui, party, playerUUID);
        } else {
            for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
                ui.set("#InvitePlayer" + i + ".Visible", false);
            }
        }

        // Leave button always visible
        ui.set("#GroupeBtnLeave.Visible", true);

        // Members
        List<Map.Entry<UUID, String>> members = new ArrayList<>(party.getMembers().entrySet());
        for (int i = 0; i < MAX_MEMBER_SLOTS; i++) {
            if (i < members.size()) {
                Map.Entry<UUID, String> entry = members.get(i);
                UUID memberUUID = entry.getKey();
                String name = entry.getValue();
                boolean memberIsCaptain = party.isCaptain(memberUUID);

                ui.set("#GrpMember" + i + ".Visible", true);
                ui.set("#GrpName" + i + ".Text", name);
                ui.set("#GrpBadge" + i + ".Text", memberIsCaptain ? "CAP" : "---");
                ui.set("#GrpLevel" + i + ".Text", "Lv." + getMemberLevel(memberUUID));

                // HP ratio
                float hpRatio = getMemberHPRatio(memberUUID);
                ui.set("#GrpBar" + i + ".Value", hpRatio);

                // Kick + Promote buttons: visible only for captain, and not on himself
                boolean showActions = isCaptain && !memberUUID.equals(playerUUID);
                ui.set("#GrpBtnKick" + i + ".Visible", showActions);
                ui.set("#GrpBtnPromote" + i + ".Visible", showActions);
            } else {
                ui.set("#GrpMember" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleCreate(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (uuid == null || player == null) return false;
        if (PartyManager.hasParty(uuid)) return false;

        Party party = PartyManager.createParty(uuid, player.getPlayerRef().getUsername());
        if (party == null) return false;

        PartyManager.showHudForPlayer(player, uuid);
        return true;
    }

    public static boolean handleLeave(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null || !PartyManager.hasParty(uuid)) return false;

        PartyManager.leaveParty(uuid);
        return true;
    }

    public static boolean handleDisband(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;

        Party party = PartyManager.getParty(uuid);
        if (party == null || !party.isCaptain(uuid)) return false;

        // Notify members
        for (UUID memberUUID : party.getMemberUUIDs()) {
            PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
            if (memberRef != null) {
                memberRef.sendMessage(Message.raw("§cLe groupe a ete dissout."));
            }
        }

        PartyManager.disbandParty(party);
        return true;
    }

    public static boolean handleKick(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;

        Party party = PartyManager.getParty(uuid);
        if (party == null || !party.isCaptain(uuid)) return false;

        List<Map.Entry<UUID, String>> members = new ArrayList<>(party.getMembers().entrySet());
        if (idx < 0 || idx >= members.size()) return false;

        UUID targetUUID = members.get(idx).getKey();
        if (targetUUID.equals(uuid)) return false; // can't kick self

        String targetName = members.get(idx).getValue();
        PartyManager.leaveParty(targetUUID);

        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("§cVous avez ete exclu du groupe."));
        }

        // Notify remaining
        Party updatedParty = PartyManager.getParty(uuid);
        if (updatedParty != null) {
            for (UUID m : updatedParty.getMemberUUIDs()) {
                PlayerRef mRef = Universe.get().getPlayer(m);
                if (mRef != null) {
                    mRef.sendMessage(Message.raw("§e" + targetName + " a ete exclu du groupe."));
                }
            }
        }
        return true;
    }

    public static boolean handlePromote(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;

        Party party = PartyManager.getParty(uuid);
        if (party == null || !party.isCaptain(uuid)) return false;

        List<Map.Entry<UUID, String>> members = new ArrayList<>(party.getMembers().entrySet());
        if (idx < 0 || idx >= members.size()) return false;

        UUID targetUUID = members.get(idx).getKey();
        if (targetUUID.equals(uuid)) return false;

        // Transfer captain
        party.transferCaptain(targetUUID);

        // Notify
        PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("§6Vous etes maintenant le Capitaine du groupe !"));
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPlayerRef().sendMessage(Message.raw("§eVous avez donne le role de Capitaine a " + members.get(idx).getValue()));
        }
        return true;
    }

    public static boolean handleInviteByIndex(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        if (idx < 0 || idx >= cachedInviteNames.size()) return false;

        String targetName = cachedInviteNames.get(idx);
        return handleInviteByName(targetName, ref, store);
    }

    private static boolean handleInviteByName(String targetName, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null || targetName == null || targetName.isEmpty()) return false;

        Party party = PartyManager.getParty(uuid);
        if (party == null || !party.isCaptain(uuid)) return false;
        if (party.isFull()) return false;

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) return false;

        UUID targetUUID = extractUUID(targetRef);
        if (targetUUID == null || PartyManager.hasParty(targetUUID)) return false;

        PartyManager.sendInvite(targetUUID, uuid);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPlayerRef().sendMessage(Message.raw("§aInvitation envoyee a " + targetName));
        }
        targetRef.sendMessage(Message.raw("§eVous avez ete invite a rejoindre un groupe !"));
        targetRef.sendMessage(Message.raw("§7Tapez §f/es party accept _ §7pour accepter."));
        return true;
    }

    private static void populateInviteList(UICommandBuilder ui, Party party, UUID myUUID) {
        cachedInviteNames.clear();

        List<PlayerRef> allPlayers = new ArrayList<>(Universe.get().getPlayers());
        Set<UUID> memberUUIDs = new HashSet<>(party.getMemberUUIDs());

        for (PlayerRef pRef : allPlayers) {
            if (cachedInviteNames.size() >= MAX_INVITE_SLOTS) break;

            UUID pUUID = extractUUID(pRef);
            if (pUUID == null || memberUUIDs.contains(pUUID)) continue;
            if (PartyManager.hasParty(pUUID)) continue; // deja dans un groupe

            cachedInviteNames.add(pRef.getUsername());
        }

        for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
            if (i < cachedInviteNames.size()) {
                ui.set("#InvitePlayer" + i + ".Visible", true);
                ui.set("#InviteName" + i + ".Text", cachedInviteNames.get(i));
            } else {
                ui.set("#InvitePlayer" + i + ".Visible", false);
            }
        }
    }

    // === UTILS ===

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }

    private static int getMemberLevel(UUID memberUUID) {
        try {
            PlayerRef ref = Universe.get().getPlayer(memberUUID);
            if (ref == null) return 0;
            var eRef = ref.getReference();
            if (eRef == null) return 0;
            var store = eRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(eRef, type);
            return data != null ? data.getLevel() : 0;
        } catch (Exception e) { return 0; }
    }

    private static float getMemberHPRatio(UUID memberUUID) {
        try {
            PlayerRef ref = Universe.get().getPlayer(memberUUID);
            if (ref == null) return 0f;
            var eRef = ref.getReference();
            if (eRef == null) return 0f;
            var store = eRef.getStore();
            EntityStatMap statMap = store.getComponent(eRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return 0f;
            var hp = statMap.get(DefaultEntityStatTypes.getHealth());
            if (hp == null) return 0f;
            return hp.getMax() > 0 ? hp.get() / hp.getMax() : 0f;
        } catch (Exception e) { return 0f; }
    }
}
