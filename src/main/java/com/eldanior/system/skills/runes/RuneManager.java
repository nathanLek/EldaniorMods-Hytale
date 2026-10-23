package com.eldanior.system.skills.runes;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Regles des runes (items portant un bloc "Ability", equipables via la fenetre du Bench_Abilities).
 *
 * - Restriction de classe (meme format que SkillModel.requiredClass : "warrior, dragon", "all"...).
 *   Une rune absente de {@link #RUNE_CLASSES} n'est pas restreinte (runes Support, runes debug...).
 * - Parchemins de rune : appris via ConsumableItemSkillInteraction, la rune va dans le sac de runes.
 * - Purge des runes non autorisees au changement de classe, wipe complet au reset niveau 1.
 */
public final class RuneManager {

    private RuneManager() {}

    public static final String AURA_RUNE_ID = "Rune_Aura";
    public static final int AURA_RUNE_MIN_DIGNITY = 5;

    /** Rune -> classes autorisees. A ajuster ici pour equilibrer. */
    private static final Map<String, String> RUNE_CLASSES = Map.ofEntries(
            Map.entry("Rune_Enrage",          "warrior, dragon"),
            Map.entry("Rune_GroundSlam",      "warrior, dragon"),
            Map.entry("Rune_AuraBlade",       "warrior, dragon"),
            Map.entry("Rune_Fireball",        "mage, dragon"),
            Map.entry("Rune_ChargedShot",     "archer, dragon"),
            Map.entry("Rune_ChainHook",       "archer, dragon"),
            Map.entry("Rune_PoisonImbue",     "assassin, dragon"),
            Map.entry("Rune_WindStrike",      "assassin, dragon"),
            Map.entry(AURA_RUNE_ID,           "all"),
            Map.entry("Rune_CourrouxMuspelheim", "mage, dragon"),
            Map.entry("Rune_SanctuaireTaranis", "mage, dragon"),
            Map.entry("Rune_AvenementBahamut", "dragon"),
            Map.entry("Rune_AvatarDragon", "dragon"),
            Map.entry("Rune_SablierChronos", "mage, marchand, dragon"),
            Map.entry("Rune_AbimeChaos", "mage, assassin, dragon"),
            Map.entry("Rune_PontBifrost", "mage, assassin, archer, dragon"),
            Map.entry("Rune_VoileNyx", "assassin, mage, dragon"),
            Map.entry("Rune_SanctuaireApollon", "mage, warrior, dragon"),
            Map.entry("Rune_CourrouxFujin", "archer, assassin, dragon"),
            Map.entry("Rune_ReveilGaia", "warrior, dragon"),
            Map.entry("Rune_EveilYggdrasil", "mage, marchand, dragon"),
            Map.entry("Rune_GilgameshBabylon",      "warrior, dragon"),
            Map.entry("Rune_PorteNiflheim",       "mage, dragon"),
            Map.entry("Rune_Bourrasque", "mage, archer, assassin, dragon"),
            Map.entry("Rune_PuitsGravitationnel", "mage, assassin, dragon"),
            Map.entry("Rune_AutoriteGravitationnelle", "mage, assassin, dragon"),
            Map.entry("Rune_Telekinesie", "mage, assassin, dragon"),
            Map.entry("Rune_Clonage", "dragon, warrior, assassin, archer"),
            Map.entry("Rune_Doppelganger", "dragon, warrior, assassin, archer")
    );

    /** Parchemin -> rune enseignee. */
    private static final Map<String, String> PAGE_TO_RUNE = Map.ofEntries(
            Map.entry("rune_page_enrage",           "Rune_Enrage"),
            Map.entry("rune_page_ground_slam",      "Rune_GroundSlam"),
            Map.entry("rune_page_aura_blade",       "Rune_AuraBlade"),
            Map.entry("rune_page_fireball",         "Rune_Fireball"),
            Map.entry("rune_page_charged_shot",     "Rune_ChargedShot"),
            Map.entry("rune_page_chain_hook",       "Rune_ChainHook"),
            Map.entry("rune_page_poison_imbue",     "Rune_PoisonImbue"),
            Map.entry("rune_page_wind_strike",      "Rune_WindStrike"),
            Map.entry("rune_page_courroux_muspelheim", "Rune_CourrouxMuspelheim"),
            Map.entry("rune_page_sanctuaire_taranis", "Rune_SanctuaireTaranis"),
            Map.entry("rune_page_avenement_bahamut", "Rune_AvenementBahamut"),
            Map.entry("rune_page_avatar_dragon", "Rune_AvatarDragon"),
            Map.entry("rune_page_sablier_chronos", "Rune_SablierChronos"),
            Map.entry("rune_page_abime_chaos", "Rune_AbimeChaos"),
            Map.entry("rune_page_pont_bifrost", "Rune_PontBifrost"),
            Map.entry("rune_page_voile_nyx", "Rune_VoileNyx"),
            Map.entry("rune_page_sanctuaire_apollon", "Rune_SanctuaireApollon"),
            Map.entry("rune_page_courroux_fujin", "Rune_CourrouxFujin"),
            Map.entry("rune_page_reveil_gaia", "Rune_ReveilGaia"),
            Map.entry("rune_page_eveil_yggdrasil", "Rune_EveilYggdrasil"),
            Map.entry("rune_page_gilgamesh_babylon",      "Rune_GilgameshBabylon"),
            Map.entry("rune_page_porte_niflheim",       "Rune_PorteNiflheim"),
            Map.entry("rune_page_bourrasque", "Rune_Bourrasque"),
            Map.entry("rune_page_puits_gravitationnel", "Rune_PuitsGravitationnel"),
            Map.entry("rune_page_autorite_gravitationnelle", "Rune_AutoriteGravitationnelle"),
            Map.entry("rune_page_telekinesie", "Rune_Telekinesie"),
            Map.entry("rune_page_clonage", "Rune_Clonage"),
            Map.entry("rune_page_doppelganger", "Rune_Doppelganger")
    );

    /** Joueurs dont on sait deja qu'ils possedent la Rune d'Aura (evite de rescanner l'inventaire). */
    private static final Set<UUID> auraRuneChecked = ConcurrentHashMap.newKeySet();

    // ===================== REGLES =====================

    public static Optional<String> getRuneFromPage(String itemId) {
        return Optional.ofNullable(PAGE_TO_RUNE.get(itemId));
    }

    public static String getRequiredClass(String runeId) {
        return RUNE_CLASSES.getOrDefault(runeId, "all");
    }

    public static boolean isRune(ItemStack stack) {
        return !ItemStack.isEmpty(stack) && stack.getItem() != null && stack.getItem().getAbility() != null;
    }

    /** Meme regle que les parchemins de sorts : la classe du joueur OU sa classe parente doit figurer dans la liste. */
    public static boolean isClassAllowed(String requiredClass, PlayerLevelData data) {
        if (requiredClass == null || requiredClass.equalsIgnoreCase("all")) return true;
        String reqLower = requiredClass.toLowerCase();
        String playerClass = data.getPlayerClassId().toLowerCase();
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        String parentClass = classModel != null ? classModel.getType().name().toLowerCase() : "";
        return reqLower.contains(playerClass) || (!parentClass.isEmpty() && reqLower.contains(parentClass));
    }

    public static boolean canUseRune(String runeId, PlayerLevelData data) {
        return isClassAllowed(getRequiredClass(runeId), data);
    }

    // ===================== INVENTAIRE =====================

    private static List<ComponentType<EntityStore, ? extends InventoryComponent>> runeSections() {
        return List.of(
                InventoryComponent.RuneBag.getComponentType(),
                InventoryComponent.AbilitySlots.getComponentType(),
                InventoryComponent.Hotbar.getComponentType(),
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Backpack.getComponentType(),
                InventoryComponent.Utility.getComponentType());
    }

    public static boolean ownsRune(Ref<EntityStore> ref, Store<EntityStore> store, String runeId) {
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section != null && section.getInventory()
                    .countItemStacks(stack -> runeId.equals(stack.getItemId())) > 0) {
                return true;
            }
        }
        return false;
    }

    /** Ajoute une rune dans le sac de runes. Retourne false si le sac est plein / absent. */
    public static boolean giveRune(Ref<EntityStore> ref, Store<EntityStore> store, String runeId) {
        InventoryComponent.RuneBag runeBag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        if (runeBag == null) return false;
        if (!runeBag.getInventory().addItemStack(new ItemStack(runeId, 1)).succeeded()) return false;
        runeBag.markDirty();
        return true;
    }

    /** Retire de toutes les sections les runes qui matchent le filtre. Retourne le nombre retire. */
    private static int removeRunes(Ref<EntityStore> ref, Store<EntityStore> store, Predicate<ItemStack> filter) {
        int removed = 0;
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            ItemContainer container = section.getInventory();
            List<Short> slots = new ArrayList<>();
            container.forEach((slot, stack) -> {
                if (isRune(stack) && filter.test(stack)) slots.add(slot);
            });
            for (short slot : slots) container.removeItemStackFromSlot(slot);
            if (!slots.isEmpty()) {
                section.markDirty();
                removed += slots.size();
            }
        }
        return removed;
    }

    /** Anciens identifiants des runes divines (avant renommage) -> nouveaux. */
    private static final Map<String, String> LEGACY_IDS = Map.of(
            "Rune_ChatimentCeleste", "Rune_SanctuaireTaranis",
            "Rune_EpeeCeleste", "Rune_GilgameshBabylon",
            "Rune_ZeroAbsolu", "Rune_PorteNiflheim",
            "rune_page_chatiment_celeste", "rune_page_sanctuaire_taranis",
            "rune_page_epee_celeste", "rune_page_gilgamesh_babylon",
            "rune_page_zero_absolu", "rune_page_porte_niflheim",
            "Rune_JugementCeleste", "Rune_CourrouxMuspelheim",
            "rune_page_jugement_celeste", "rune_page_courroux_muspelheim");

    /** Remplace dans l'inventaire les runes / parchemins portant un ancien identifiant. Retourne le nombre converti. */
    public static int migrateLegacyRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        int migrated = 0;
        for (var type : runeSections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            ItemContainer container = section.getInventory();
            Map<Short, ItemStack> replace = new java.util.HashMap<>();
            container.forEach((slot, stack) -> {
                String newId = stack == null ? null : LEGACY_IDS.get(stack.getItemId());
                if (newId != null) replace.put(slot, new ItemStack(newId, Math.max(1, stack.getQuantity())));
            });
            replace.forEach(container::setItemStackForSlot);
            if (!replace.isEmpty()) {
                section.markDirty();
                migrated += replace.size();
            }
        }
        return migrated;
    }

    /** Changement de classe : supprime les runes que la nouvelle classe ne peut pas utiliser. */
    public static int purgeDisallowedRunes(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        return removeRunes(ref, store, stack -> !canUseRune(stack.getItemId(), data));
    }

    /** Reset niveau 1 : supprime toutes les runes (la Rune d'Aura sera redonnee avec la dignite). */
    public static int wipeAllRunes(Ref<EntityStore> ref, Store<EntityStore> store) {
        int removed = removeRunes(ref, store, stack -> true);
        UUID uuid = getUuid(ref, store);
        if (uuid != null) auraRuneChecked.remove(uuid);
        return removed;
    }

    /**
     * Restriction a l'equipement : une rune non autorisee posee dans les slots de competence
     * est renvoyee dans le sac de runes. Retourne le nombre de runes deplacees.
     */
    public static int enforceAbilitySlots(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        InventoryComponent.AbilitySlots abilitySlots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        InventoryComponent.RuneBag runeBag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        if (abilitySlots == null || runeBag == null) return 0;

        ItemContainer slotsContainer = abilitySlots.getInventory();
        List<Short> forbidden = new ArrayList<>();
        slotsContainer.forEach((slot, stack) -> {
            if (isRune(stack) && !canUseRune(stack.getItemId(), data)) forbidden.add(slot);
        });

        int moved = 0;
        for (short slot : forbidden) {
            ItemStack stack = slotsContainer.getItemStack(slot);
            if (ItemStack.isEmpty(stack) || !runeBag.getInventory().canAddItemStack(stack)) continue;
            slotsContainer.removeItemStackFromSlot(slot);
            runeBag.getInventory().addItemStack(stack);
            moved++;
        }
        if (moved > 0) {
            abilitySlots.markDirty();
            runeBag.markDirty();
        }
        return moved;
    }

    // ===================== RUNE D'AURA =====================

    /** Donne la Rune d'Aura (sac de runes) si le joueur ne la possede nulle part. Retourne true si donnee. */
    public static boolean grantAuraRuneIfMissing(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getUuid(ref, store);
        if (uuid == null || auraRuneChecked.contains(uuid)) return false;

        if (ownsRune(ref, store, AURA_RUNE_ID)) {
            auraRuneChecked.add(uuid);
            return false;
        }
        if (giveRune(ref, store, AURA_RUNE_ID)) {
            auraRuneChecked.add(uuid);
            return true;
        }
        return false;
    }

    public static void handleDisconnect(UUID uuid) {
        auraRuneChecked.remove(uuid);
    }

    private static UUID getUuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef netRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (netRef == null) return null;
        try { return UUIDExtractor.getUUID(netRef); }
        catch (Exception e) { return null; }
    }
}
