package com.eldanior.system.party;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class PartyHud extends CustomUIHud {

    private final UUID ownerUUID;

    public PartyHud(PlayerRef playerRef, UUID ownerUUID) {
        super(playerRef);
        this.ownerUUID = ownerUUID;
    }

    @Override
    protected void build(UICommandBuilder ui) {
        ui.append("Party/PartyHud.ui");
        renderMembers(ui);
    }

    @Override
    public void update(boolean firstUpdate, UICommandBuilder ui) {
        renderMembers(ui);
        super.update(firstUpdate, ui);
    }

    /** Methode statique pour le CombinedHud - delegue a l'instance existante */
    public static void renderPartyStatic(UICommandBuilder ui, UUID uuid) {
        Party party = PartyManager.getParty(uuid);
        if (party == null) {
            ui.set("#PartyPanel.Visible", false);
            return;
        }
        ui.set("#PartyPanel.Visible", true);
        ui.set("#PartyTitle.Text", "Groupe (" + party.getSize() + "/" + Party.MAX_MEMBERS + ")");

        List<Map.Entry<UUID, String>> memberList = new ArrayList<>(party.getMembers().entrySet());
        for (int i = 0; i < 5; i++) {
            int slot = i + 1;
            String card = "#Member" + slot + "Card";
            if (i < memberList.size()) {
                Map.Entry<UUID, String> entry = memberList.get(i);
                String name = entry.getValue();
                boolean isCaptain = party.isCaptain(entry.getKey());

                ui.set(card + ".Visible", true);
                ui.set("#Member" + slot + "Badge.Text", isCaptain ? "CAP" : "---");
                ui.set("#Member" + slot + "Badge.Style.TextColor", isCaptain ? "#FFD700" : "#667788");
                ui.set("#Member" + slot + "Name.Text", name);

                // Infos du membre via getMemberInfoStatic
                MemberInfo info = getMemberInfoStatic(entry.getKey());
                if (info.familyName != null) {
                    ui.set("#Member" + slot + "Name.Text", name + " " + info.familyName);
                }
                ui.set("#Member" + slot + "Level.Text", "Lv." + info.level);
                ui.set("#Member" + slot + "Class.Text", info.className);
                ui.set("#Member" + slot + "Bar.Value", info.hpRatio);
            } else {
                ui.set(card + ".Visible", false);
            }
        }
    }

    private void renderMembers(UICommandBuilder ui) {
        Party party = PartyManager.getParty(ownerUUID);
        if (party == null) return;

        ui.set("#PartyTitle.Text", "Groupe (" + party.getSize() + "/" + Party.MAX_MEMBERS + ")");

        List<Map.Entry<UUID, String>> memberList = new ArrayList<>(party.getMembers().entrySet());

        for (int i = 0; i < 5; i++) {
            int slot = i + 1;
            String card = "#Member" + slot + "Card";

            if (i < memberList.size()) {
                Map.Entry<UUID, String> entry = memberList.get(i);
                UUID memberUUID = entry.getKey();
                String name = entry.getValue();
                boolean isCaptain = party.isCaptain(memberUUID);

                ui.set(card + ".Visible", true);

                // Badge : CAP ou membre
                ui.set("#Member" + slot + "Badge.Text", isCaptain ? "CAP" : "---");
                ui.set("#Member" + slot + "Badge.Style.TextColor", isCaptain ? "#FFD700" : "#667788");

                // Nom + famille noble
                MemberInfo info = getMemberInfo(memberUUID);
                String displayName = info.familyName != null ? name + " " + info.familyName : name;
                ui.set("#Member" + slot + "Name.Text", displayName);

                ui.set("#Member" + slot + "Level.Text", "Lv." + info.level);
                ui.set("#Member" + slot + "Class.Text", info.className);

                // Barre de vie
                ui.set("#Member" + slot + "Bar.Value", info.hpRatio);
            } else {
                ui.set(card + ".Visible", false);
            }
        }
    }

    private MemberInfo getMemberInfo(UUID playerUUID) {
        MemberInfo info = new MemberInfo();
        try {
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            if (playerRef == null) {
                info.className = "Offline";
                return info;
            }

            var ref = playerRef.getReference();
            if (ref == null) return info;
            var store = ref.getStore();

            // HP
            EntityStatMap statMap = store.getComponent(ref,
                    EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                var healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
                if (healthStat != null) {
                    float current = healthStat.get();
                    float max = healthStat.getMax();
                    info.hpRatio = max > 0 ? current / max : 0f;
                    // hpRatio suffit pour la barre
                }
            }

            // Level, classe et famille depuis PlayerLevelData
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data != null) {
                info.level = data.getLevel();
                info.className = data.getPlayerClass();

                String familyId = data.getNobleFamilyId();
                if (familyId != null && !familyId.isEmpty()) {
                    NobleFamilyModel family = FamilyManager.get(familyId);
                    if (family != null) {
                        info.familyName = "Von " + family.getDisplayName();
                    }
                }
            }
        } catch (Exception e) {
            // silently fail
        }
        return info;
    }

    private static MemberInfo getMemberInfoStatic(UUID playerUUID) {
        MemberInfo info = new MemberInfo();
        try {
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            if (playerRef == null) {
                info.className = "Offline";
                return info;
            }
            var ref = playerRef.getReference();
            if (ref == null) return info;
            var store = ref.getStore();

            // HP
            EntityStatMap statMap = store.getComponent(ref,
                    EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                var healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
                if (healthStat != null) {
                    float current = healthStat.get();
                    float max = healthStat.getMax();
                    info.hpRatio = max > 0 ? current / max : 0f;
                }
            }

            // Level, classe et famille
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data != null) {
                info.level = data.getLevel();
                info.className = data.getPlayerClass();
                String familyId = data.getNobleFamilyId();
                if (familyId != null && !familyId.isEmpty()) {
                    NobleFamilyModel family = FamilyManager.get(familyId);
                    if (family != null) info.familyName = "Von " + family.getDisplayName();
                }
            }
        } catch (Exception ignored) {}
        return info;
    }

    private static class MemberInfo {
        int level = 1;
        String className = "?";
        String familyName = null;
        float hpRatio = 1.0f;
    }
}
