package com.eldanior.system.config.configs.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.WeaponMastery;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("removal")
public class MasterySystem extends EntityTickingSystem<EntityStore> {

    private final Map<Ref<EntityStore>, Integer> lastValidSlot = new WeakHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        PlayerRef netRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

        if (player == null || netRef == null) return;

        Inventory inv = player.getInventory();
        if (inv == null) return;

        short currentSlot = inv.getActiveHotbarSlot();
        ItemStack heldItem = inv.getActiveHotbarItem();

        if (heldItem != null && !heldItem.isEmpty()) {
            WeaponMastery required = getWeaponMastery(heldItem);

            PlayerLevelData playerData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (playerData == null) return;

            // Verifier les items catalyst de competence active en premier
            if (isCatalystWithoutSkill(heldItem, playerData)) {
                int fallbackSlotInt = lastValidSlot.getOrDefault(playerRef, -1);
                short fallbackSlot = (fallbackSlotInt == -1 || fallbackSlotInt == currentSlot) ? findSafeSlot(inv) : (short) fallbackSlotInt;

                inv.setActiveHotbarSlot(playerRef, (byte) fallbackSlot, store);

                NotificationHelper.sendNotification(netRef,
                        "<color:red>Vous n'avez pas appris cette competence !</color>",
                        NotificationStyle.Warning);
                return;
            }

            if (required == WeaponMastery.ANY) {
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }

            ClassModel classModel = ClassManager.get(playerData.getPlayerClassId());
            if (classModel == null) return;

            if (!classModel.canEquip(required)) {
                int fallbackSlotInt = lastValidSlot.getOrDefault(playerRef, -1);
                short fallbackSlot = (fallbackSlotInt == -1 || fallbackSlotInt == currentSlot) ? findSafeSlot(inv) : (short) fallbackSlotInt;

                inv.setActiveHotbarSlot(playerRef, (byte) fallbackSlot, store);

                NotificationHelper.sendNotification(netRef,
                        "<color:red>Attention, votre classe ne vous permet pas d'équiper cette arme !</color>",
                        NotificationStyle.Warning);
            } else {
                lastValidSlot.put(playerRef, (int) currentSlot);
            }
        } else {
            lastValidSlot.put(playerRef, (int) currentSlot);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), PlayerRef.getComponentType());
    }

    private boolean isCatalystWithoutSkill(ItemStack item, PlayerLevelData data) {
        if (item == null || item.isEmpty() || data == null) return false;
        String itemId = item.getItemId();
        for (var skill : com.eldanior.system.skills.SkillManager.getAllSkills()) {
            if (itemId.equals(skill.catalystId())) {
                // Vérifié dans les skills appris
                if (data.getUnlockedSkills().contains(skill.skillId())) return false;
                // Vérifié dans les skills actifs de la classe
                var classModel = com.eldanior.system.classes.ClassManager.get(data.getPlayerClassId());
                if (classModel != null && classModel.getActiveSkillIds().contains(skill.skillId())) return false;
                return true;
            }
        }
        return false;
    }

    public WeaponMastery getWeaponMastery(ItemStack item) {
        if (item == null || item.isEmpty()) return WeaponMastery.ANY;

        String itemId = item.getItemId().toLowerCase();

        if (itemId.contains("sword")) return WeaponMastery.SWORD;
        if (itemId.contains("bow")) return WeaponMastery.BOW;
        if (itemId.contains("dagger")) return WeaponMastery.DAGGER;
        if (itemId.contains("staff") || itemId.contains("wand")) return WeaponMastery.STAFF;
        if (itemId.contains("axe")) return WeaponMastery.AXE;
        if (itemId.contains("shield")) return WeaponMastery.SHIELD;
        if (itemId.contains("spear")) return WeaponMastery.SPEAR;
        if (itemId.contains("spellbook")) return WeaponMastery.SPELLBOOK;
        if (itemId.contains("mace")) return WeaponMastery.MACE;
        if (itemId.contains("gun")) return WeaponMastery.GUN;
        if (itemId.contains("club")) return WeaponMastery.CLUB;
        if (itemId.contains("rifle") || itemId.contains("riffle")) return WeaponMastery.RIFLE;

        return WeaponMastery.ANY;
    }

    private short findSafeSlot(Inventory inv) {
        ItemContainer hotbar = inv.getHotbar();
        if (hotbar == null) return 0;

        for (short i = 0; i < 9; i++) {
            ItemStack item = hotbar.getItemStack(i);
            if (item == null || item.isEmpty() || getWeaponMastery(item) == WeaponMastery.ANY) {
                return i;
            }
        }
        return 0;
    }
}