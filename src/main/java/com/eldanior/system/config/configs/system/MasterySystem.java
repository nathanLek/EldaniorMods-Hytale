package com.eldanior.system.config.configs.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.ClassType;
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
import java.util.Set;
import java.util.WeakHashMap;

@SuppressWarnings("removal")
public class MasterySystem extends EntityTickingSystem<EntityStore> {

    private final Map<Ref<EntityStore>, Integer> lastValidSlot = new WeakHashMap<>();

    /** Artefacts reserves a certains types de classe (quelle que soit la maitrise d'arme). */
    private static final Set<ClassType> WARRIOR_ARTIFACT = Set.of(ClassType.WARRIOR, ClassType.DRAGON);
    private static final Map<String, Set<ClassType>> ARTIFACT_CLASSES = Map.ofEntries(
            Map.entry("Artefact_Gungnir", Set.of(ClassType.WARRIOR, ClassType.DRAGON, ClassType.MERCHANT)),
            Map.entry("Artefact_Masamune_Celeste", WARRIOR_ARTIFACT),
            // Epees a une main (Mythique, Legendaires)
            Map.entry("Artefact_Sword_Excalibur", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Sword_Gram", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Sword_Solarys", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Sword_Katanas_Jumeaux", Set.of(ClassType.WARRIOR, ClassType.ASSASSIN, ClassType.DRAGON)),  // doubles katanas
            // Armes divines (une par famille, scripts/artefacts/gen_divines.py)
            Map.entry("Artefact_Staff_Ouas_Ra", Set.of(ClassType.MAGE, ClassType.DRAGON)),
            Map.entry("Artefact_Wand_Circe", Set.of(ClassType.MAGE, ClassType.DRAGON)),
            Map.entry("Artefact_Shield_Egide", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Longsword_Durandal", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Axe_Skadi", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Battleaxe_Labrys", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Battleaxe_Faux_Thanatos", Set.of(ClassType.WARRIOR, ClassType.ASSASSIN, ClassType.DRAGON)),
            Map.entry("Artefact_Mace_Mjollnir", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Mace_Sharur", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Shortbow_Artemis", Set.of(ClassType.ARCHER, ClassType.DRAGON)),
            Map.entry("Artefact_Shortbow_Apollon", Set.of(ClassType.ARCHER, ClassType.DRAGON)),
            Map.entry("Artefact_Staff_Ruyi_Jingu_Bang", Set.of(ClassType.WARRIOR, ClassType.ASSASSIN, ClassType.DRAGON)),
            Map.entry("Artefact_Club_Heracles", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Club_Dagda", WARRIOR_ARTIFACT),
            Map.entry("Artefact_Daggers_Carnwennan", Set.of(ClassType.ASSASSIN, ClassType.DRAGON)),
            Map.entry("Artefact_Spear_Guandao", WARRIOR_ARTIFACT),
            // Mumei et Kagero, Lames de l'Observateur (2026-11-17, gen_lames_observateur.py) : sa classe seule
            Map.entry(com.eldanior.system.classes.observateur.ObservateurLames.ITEM, Set.of(ClassType.OBSERVATEUR)));

    /** Artefacts de famille : seuls les membres de la famille noble peuvent les tenir (quelle que soit leur classe). */
    private static final Map<String, String> ARTIFACT_FAMILIES = Map.of(
            "Artefact_Sword_Ryukiri", "runkandel",                                // Tranche-Dragon des Runkandel
            "Artefact_Sword_Tenryumaru", "luo",                                   // Lame du Premier Serment, relique du Clan Luo
            "Artefact_Sword_Tenryumaru_Degainee", "luo");                         // la meme, lame au clair

    /** Famille a qui l'objet est reserve : artefacts de famille, et tous les katanas du Clan Luo (LuoKatanas). */
    private static String familyOf(String itemId) {
        String family = ARTIFACT_FAMILIES.get(itemId);
        if (family == null && com.eldanior.system.skills.runes.spells.LuoKatanas.isClanItem(itemId)) family = "luo";
        return family;
    }

    /** Reliques de famille : en plus du sang, il faut etre le Patriarche. */
    private static final Set<String> PATRIARCH_RELICS = Set.of("Artefact_Sword_Tenryumaru", "Artefact_Sword_Tenryumaru_Degainee",
            "Artefact_Sword_Kinryu", "Artefact_Sword_Kinryu_Degainee");                  // tresor du Patriarche Luo (FamilyKeySystem)

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

            // Observateur (2026-11-16) : toutes les armes du mod (armes a feu, artefacts de classe et de famille compris, et catalyseurs de sorts)
            if (com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(playerData)) {
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }


            // Armes a feu (mousquets, leurs transformations, fusils et pistolets) : SEULEMENT le Mousquetaire (2026-11-15)
            if (com.eldanior.system.skills.runes.spells.Mousquet.isFirearm(heldItem.getItemId())
                    || ((required == WeaponMastery.GUN || required == WeaponMastery.RIFLE)          // fusils et pistolets vanilla
                        && heldItem.getItemId().startsWith("Weapon_") && !ARTIFACT_CLASSES.containsKey(heldItem.getItemId()))) {
                ClassModel shooterClass = ClassManager.get(playerData.getPlayerClassId());
                if (shooterClass == null || shooterClass.getType() != ClassType.MOUSQUETAIRE) {
                    int fallbackSlotInt = lastValidSlot.getOrDefault(playerRef, -1);
                    short fallbackSlot = (fallbackSlotInt == -1 || fallbackSlotInt == currentSlot) ? findSafeSlot(inv) : (short) fallbackSlotInt;
                    inv.setActiveHotbarSlot(playerRef, (byte) fallbackSlot, store);
                    NotificationHelper.sendNotification(netRef,
                            "<color:red>Seul un Mousquetaire sait se servir d'une arme à feu !</color>", NotificationStyle.Warning);
                    return;
                }
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }

            // Admin : tient tous les artefacts (famille et classe), pour les tester
            boolean artifact = familyOf(heldItem.getItemId()) != null || ARTIFACT_CLASSES.containsKey(heldItem.getItemId());
            if (artifact && netRef.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION)) {
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }

            String artifactFamily = familyOf(heldItem.getItemId());
            if (artifactFamily != null) {                                         // artefact de famille : seul le sang compte
                boolean refused = !artifactFamily.equalsIgnoreCase(playerData.getNobleFamilyId())
                        || (PATRIARCH_RELICS.contains(heldItem.getItemId()) && !playerData.isPatriarch());
                if (refused) {
                    int fallbackSlotInt = lastValidSlot.getOrDefault(playerRef, -1);
                    short fallbackSlot = (fallbackSlotInt == -1 || fallbackSlotInt == currentSlot) ? findSafeSlot(inv) : (short) fallbackSlotInt;
                    inv.setActiveHotbarSlot(playerRef, (byte) fallbackSlot, store);
                    NotificationHelper.sendNotification(netRef,
                            PATRIARCH_RELICS.contains(heldItem.getItemId())
                                    ? "<color:red>Cette relique ne répond qu'au Patriarche de sa famille !</color>"
                                    : "<color:red>Cette lame ne répond qu'au sang de sa famille !</color>",
                            NotificationStyle.Warning);
                    return;
                }
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }

            ClassModel classModel = ClassManager.get(playerData.getPlayerClassId());
            Set<ClassType> artifactClasses = ARTIFACT_CLASSES.get(heldItem.getItemId());
            if (artifactClasses != null) {                                        // artefact : seule sa classe compte
                if (classModel == null || !artifactClasses.contains(classModel.getType())) {
                    int fallbackSlotInt = lastValidSlot.getOrDefault(playerRef, -1);
                    short fallbackSlot = (fallbackSlotInt == -1 || fallbackSlotInt == currentSlot) ? findSafeSlot(inv) : (short) fallbackSlotInt;
                    inv.setActiveHotbarSlot(playerRef, (byte) fallbackSlot, store);
                    NotificationHelper.sendNotification(netRef,
                            "<color:red>Cet artefact refuse votre classe !</color>",
                            NotificationStyle.Warning);
                    return;
                }
                lastValidSlot.put(playerRef, (int) currentSlot);                  // pas de maitrise d'arme exigee (Gungnir contient « gun »)
                return;
            }

            if (required == WeaponMastery.ANY) {
                lastValidSlot.put(playerRef, (int) currentSlot);
                return;
            }

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


    public WeaponMastery getWeaponMastery(ItemStack item) {
        if (item == null || item.isEmpty()) return WeaponMastery.ANY;
        // armes a feu (mousquets et leurs transformations : Colts, SARS, canon) : maitrise GUN, reservee au Mousquetaire
        String rawId = item.getItemId();
        if (com.eldanior.system.skills.runes.spells.Mousquet.isFirearm(rawId)) return WeaponMastery.GUN;

        String itemId = item.getItemId().toLowerCase();

        if (itemId.contains("sword")) return WeaponMastery.SWORD;
        if (itemId.contains("bow")) return WeaponMastery.BOW;
        if (itemId.contains("dagger")) return WeaponMastery.DAGGER;
        if (itemId.contains("staff") || itemId.contains("wand")) return WeaponMastery.STAFF;
        if (itemId.contains("axe")) return WeaponMastery.AXE;
        if (itemId.contains("shield")) return WeaponMastery.SHIELD;
        if (itemId.contains("spear")) return WeaponMastery.SPEAR;
        if (itemId.contains("spellbook")) return WeaponMastery.SPELLBOOK;
        if (itemId.contains("flail")) return WeaponMastery.FLAIL;           // avant club : le fleau vanilla est Weapon_Club_Steel_Flail_Rusty
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
            if (item == null || item.isEmpty() || (getWeaponMastery(item) == WeaponMastery.ANY && !ARTIFACT_CLASSES.containsKey(item.getItemId()))) {
                return i;
            }
        }
        return 0;
    }
}