package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.runes.RuneManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Manuels de magie (2026-11-24, demande de l'utilisateur) : les runes de sort s'apprennent en lisant un manuel, un modele de livre
 * par type de magie (Eldanior_Manuel_Magie_<Type>_<Rune>, scripts/runes/gen_manuels_magie.py). Les types passent un par un des
 * parchemins aux manuels ; les anciens parchemins deja trouves s'apprennent toujours (ConsumableItemSkillInteraction).
 * Maintenir le clic droit ouvre le livre (animation Lecture : pages qui tournent) ; au bout de 2,5 s le sort ManuelMagieApprendre
 * donne la rune dans le sac de runes et le manuel se consume. Memes conditions que le parchemin : classe apte (RuneManager.canUseRune),
 * rune pas deja connue, sac de runes pas plein. Rune deja connue ou classe inapte : le manuel reste.
 */
public final class MagicManuals {

    private MagicManuals() {}

    public static final String PREFIX = "Eldanior_Manuel_Magie_";
    private static final String FX_LEARN = "Eldanior_Pas_Celeste", FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String SFX_LEARN = "SFX_Deployable_Totem_Heal_Spawn";

    /** Rune enseignee par ce manuel (« Eldanior_Manuel_Magie_Feu_Meteore » -> « Rune_Meteore »), sinon null. */
    public static String runeOf(String itemId) {
        if (itemId == null || !itemId.startsWith(PREFIX)) return null;
        String rest = itemId.substring(PREFIX.length());
        int cut = rest.indexOf('_');                                                     // type de magie (sans tiret bas)
        return cut > 0 && cut < rest.length() - 1 ? "Rune_" + rest.substring(cut + 1) : null;
    }

    /** Types de magie qui ont des manuels (scripts/runes/gen_manuels_magie.py, TYPES). */
    private static final String[] TYPES = {"Feu", "Glace", "Foudre", "Air", "Terre", "Lumiere", "Tenebres", "Temps", "Espace",
            "Spatial", "Dragonic", "Combat", "Speciales"};
    private static final java.util.Map<String, String> BOOKS = new java.util.concurrent.ConcurrentHashMap<>();

    /** Manuel de magie qui enseigne cette rune (« Rune_Meteore » -> « Eldanior_Manuel_Magie_Feu_Meteore »), sinon null. */
    public static String bookFor(String runeId) {
        if (runeId == null || !runeId.startsWith("Rune_")) return null;
        String known = BOOKS.get(runeId);
        if (known != null) return known.isEmpty() ? null : known;
        String key = runeId.substring(5), found = "";
        var items = com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap();
        for (String type : TYPES) {
            String id = PREFIX + type + "_" + key;
            if (items.getAsset(id) != null) { found = id; break; }
        }
        BOOKS.put(runeId, found);
        return found.isEmpty() ? null : found;
    }

    /** Manuel de n'importe quelle sorte (magie, voie martiale, Mouvement de famille) qui enseigne cette rune, sinon null. */
    public static String anyBookFor(String runeId) {
        String book = LuoManuels.bookFor(runeId);
        return book != null ? book : bookFor(runeId);
    }

    /** Type de magie de ce manuel (« Feu »), sinon null. */
    public static String typeOf(String itemId) {
        if (itemId == null || !itemId.startsWith(PREFIX)) return null;
        String rest = itemId.substring(PREFIX.length());
        int cut = rest.indexOf('_');
        return cut > 0 ? rest.substring(0, cut) : null;
    }

    /** Fin de la lecture (2,5 s de clic droit maintenu) : la rune est apprise, le manuel se consume. */
    public static final class Apprendre implements RuneSpell {
        @Override public String id() { return "ManuelMagieApprendre"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Player player = store.getComponent(caster, Player.getComponentType());
            if (player == null || player.getInventory() == null) return null;
            short slot = player.getInventory().getActiveHotbarSlot();
            InventoryComponent hotbarSection = store.getComponent(caster, InventoryComponent.Hotbar.getComponentType());
            if (slot < 0 || hotbarSection == null) return null;
            ItemContainer hotbar = hotbarSection.getInventory();
            ItemStack held = hotbar.getItemStack(slot);
            if (held == null || held.isEmpty()) return null;
            String rune = runeOf(held.getItemId());
            if (rune == null) return null;
            PlayerLevelData data = MartialKi.data(caster, store);
            if (data == null) return null;
            if (!RuneManager.canUseRune(rune, data)) {
                MartialKi.notify(caster, store, "<color:orange>Votre classe n'est pas apte à déchiffrer ce manuel.</color>", NotificationStyle.Warning);
                return null;
            }
            if (RuneManager.ownsRune(caster, store, rune)) {
                MartialKi.notify(caster, store, "<color:gray>Vous connaissez déjà cette rune.</color>", NotificationStyle.Default);
                return null;
            }
            if (!RuneManager.giveRune(caster, store, rune)) {
                MartialKi.notify(caster, store, "<color:red>Votre sac de runes est plein.</color> <color:gray>Libérez une place dans</color> "
                        + "<color:gold>/es system</color><color:gray>, onglet Compétences.</color>", NotificationStyle.Warning);
                return null;
            }
            if (held.getQuantity() > 1) hotbar.setItemStackForSlot(slot, held.withQuantity(held.getQuantity() - 1));
            else hotbar.removeItemStackFromSlot(slot);
            hotbarSection.markDirty();
            Vector3d pos = SpellFx.position(caster, store);
            if (pos != null) {
                SpellFx.fx(FX_LEARN, new Vector3d(pos).add(0, 0.1, 0), 1.6f, store, cb);
                SpellFx.fx(FX_GOLD, new Vector3d(pos).add(0, 1.4, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_LEARN, pos, cb);
            }
            // 2026-11-26 : /es runes n'existe plus, les runes s'equipent dans le menu systeme (onglet Competences, RunesTab)
            MartialKi.notify(caster, store, "<color:gold>Nouvelle compétence apprise !</color> <color:gray>Ouvrez</color> "
                    + "<color:gold>/es system</color><color:gray>, onglet</color> <color:gold>Compétences</color>"
                    + "<color:gray>, pour l'équiper.</color>", NotificationStyle.Success);
            return null;
        }
    }
}
