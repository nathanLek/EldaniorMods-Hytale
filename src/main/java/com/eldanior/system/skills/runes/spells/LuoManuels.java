package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.EldaniorLogger;
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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Manuels secrets du Clan Luo (Eldanior_Manuel_Luo_<Technique>, scripts/familles/gen_voie_martiale_objets.py) : les techniques
 * de la voie martiale (runes du type Luo) s'apprennent en les lisant. Maintenir le clic droit ouvre le livre (animation
 * Lecture : pages qui tournent) ; au bout de 2,5 s le sort LuoManuelApprendre donne la rune dans le sac de runes et le manuel
 * se consume. Condition : palier de la voie martiale suffisant (admins exemptes ; la voie n'exige pas d'etre du Clan Luo) ;
 * technique deja connue = le manuel reste.
 * Remise : au serment (VoieMartialePage) et a chaque promotion, RuneManager.grantClassRunes donne le manuel au lieu de la rune.
 */
public final class LuoManuels {

    private LuoManuels() {}

    public static final String PREFIX = "Eldanior_Manuel_Luo_";
    private static final String FX_LEARN = "Eldanior_Pas_Celeste", FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String SFX_LEARN = "SFX_Deployable_Totem_Heal_Spawn";

    /** Manuel qui enseigne cette rune (Mouvement de famille, techniques de la voie martiale et des clans), sinon null. */
    public static String bookFor(String runeId) {
        var mv = com.eldanior.system.titles.nobility.family.FamilyMovements.ofRune(runeId);
        if (mv != null) return mv.item();
        return runeId != null && runeId.startsWith("Rune_")
                && (VoieMartiale.runeTier(runeId) > 0 || com.eldanior.system.luo.LuoClans.technique(runeId) != null)
                ? PREFIX + runeId.substring(5) : null;
    }

    /** Rune enseignee par ce manuel, sinon null (un Mouvement sans technique n'enseigne rien encore). */
    public static String runeOf(String itemId) {
        var mv = com.eldanior.system.titles.nobility.family.FamilyMovements.ofItem(itemId);
        if (mv != null) return mv.rune();
        return itemId != null && itemId.startsWith(PREFIX) ? "Rune_" + itemId.substring(PREFIX.length()) : null;
    }

    /** true si le joueur porte deja ce manuel (sac, barre, reserve). */
    public static boolean hasBook(Ref<EntityStore> ref, Store<EntityStore> store, String bookId) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getInventory() == null) return false;
        ItemContainer all = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short s = 0; s < all.getCapacity(); s++) {
            ItemStack st = all.getItemStack(s);
            if (st != null && !st.isEmpty() && bookId.equals(st.getItemId())) return true;
        }
        return false;
    }

    /** Nom affiche d'une famille (« Luo »). */
    static String label(String familyId) {
        var f = com.eldanior.system.titles.nobility.family.FamilyManager.get(familyId);
        return f != null ? f.getDisplayName() : familyId;
    }

    /** Donne un manuel (barre puis reserve). false si l'inventaire est plein. */
    public static boolean giveBook(Ref<EntityStore> ref, Store<EntityStore> store, String bookId) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getInventory() == null) return false;
        ItemContainer inv = player.getInventory().getCombinedHotbarFirst();
        ItemStack stack = new ItemStack(bookId, 1);
        if (!inv.canAddItemStack(stack)) return false;
        inv.addItemStack(stack);
        return true;
    }

    /**
     * Boutique du Sensei (2026-11-12, « montrer seulement les manuels que le joueur peut apprendre ») : true si ce manuel
     * s'apprend a ce rang martial (tier 1 a 8) et n'est pas deja connu. Les objets qui ne sont pas des manuels restent visibles.
     */
    public static boolean learnable(String itemId, int tier, PlayerLevelData data, Ref<EntityStore> ref, Store<EntityStore> store) {
        var mv = com.eldanior.system.titles.nobility.family.FamilyMovements.ofItem(itemId);
        if (mv != null) return tier >= mv.tier() && !(mv.ready() && RuneManager.ownsRune(ref, store, mv.rune()));
        String rune = runeOf(itemId);
        if (rune == null) return true;
        if (RuneManager.ownsRune(ref, store, rune)) return false;
        var technique = com.eldanior.system.luo.LuoClans.technique(rune);
        if (technique != null) return data != null && data.getLevel() >= technique.level();
        return tier >= VoieMartiale.runeTier(rune);
    }

    /**
     * Entree dans une famille (appele toutes les 5 s par locks.FamilyKeySystem) : remet une fois le manuel du 1er Mouvement de la
     * famille. Deja connu ou deja dans le sac : seulement note. Retourne le titre du Mouvement remis, sinon null.
     */
    public static String welcome(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data, java.util.UUID uuid) {
        if (data == null || uuid == null || data.getNobleFamilyId() == null || data.getNobleFamilyId().isEmpty()) return null;
        String fam = data.getNobleFamilyId();
        var first = com.eldanior.system.titles.nobility.family.FamilyMovements.first(fam);
        if (first == null || com.eldanior.system.titles.nobility.family.FamilyRegistry.welcomed(fam, uuid)) return null;
        if (com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset(first.item()) == null) return null;
        if ((first.ready() && RuneManager.ownsRune(ref, store, first.rune())) || hasBook(ref, store, first.item())) {
            com.eldanior.system.titles.nobility.family.FamilyRegistry.markWelcomed(fam, uuid);
            return null;
        }
        if (!giveBook(ref, store, first.item())) return null;                         // inventaire plein : on reessaiera
        com.eldanior.system.titles.nobility.family.FamilyRegistry.markWelcomed(fam, uuid);
        return first.title();
    }

    /** Fin de la lecture (2,5 s de clic droit maintenu) : la technique est apprise, le manuel se consume. */
    public static final class Apprendre implements RuneSpell {
        @Override public String id() { return "LuoManuelApprendre"; }

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
            PlayerLevelData data = MartialKi.data(caster, store);
            PlayerRef pr = store.getComponent(caster, PlayerRef.getComponentType());
            boolean admin = (pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION))
                    || com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(data);   // Observateur : tous les manuels
            var mv = com.eldanior.system.titles.nobility.family.FamilyMovements.ofItem(held.getItemId());
            if (mv != null) {                                                            // Mouvement de famille (2026-11-12)
                String mine = data != null ? com.eldanior.system.titles.nobility.family.FamilyManager.migrateId(data.getNobleFamilyId()) : null;
                if (!admin && !mv.familyId().equalsIgnoreCase(mine)) {
                    MartialKi.notify(caster, store, "<color:red>Seuls les membres de la famille " + label(mv.familyId())
                            + " peuvent lire ce manuel.</color>", NotificationStyle.Warning);
                    return null;
                }
                int tier = Math.max(1, VoieMartiale.tier(data));
                if (!admin && tier < mv.tier()) {
                    MartialKi.notify(caster, store, "<color:red>Le " + mv.title() + " demande d'être " + VoieMartiale.className(mv.tier())
                            + ".</color>", NotificationStyle.Warning);
                    return null;
                }
                if (!mv.ready()) {
                    MartialKi.notify(caster, store, "<color:gold>" + mv.title() + "</color> <color:gray>— la technique n'est pas encore "
                            + "transmise. Le manuel reste avec vous.</color>", NotificationStyle.Default);
                    return null;
                }
            }
            String rune = runeOf(held.getItemId());
            if (rune == null) return null;
            var technique = mv != null ? null : com.eldanior.system.luo.LuoClans.technique(rune);   // technique d'un clan
            if (technique != null && !admin) {
                if (!com.eldanior.system.luo.LuoClans.knowsClan(technique, data)) {
                    MartialKi.notify(caster, store, "<color:red>Seuls les membres du " + technique.label()
                            + " peuvent lire ce manuel.</color>", NotificationStyle.Warning);
                    return null;
                }
                if (data == null || data.getLevel() < technique.level()) {
                    MartialKi.notify(caster, store, "<color:red>Il faut être niveau " + technique.level()
                            + " pour comprendre cette technique.</color>", NotificationStyle.Warning);
                    return null;
                }
            }
            int need = VoieMartiale.runeTier(rune), tier = VoieMartiale.tier(data);
            if (mv == null && technique == null && !admin && tier < need) {
                MartialKi.notify(caster, store, "<color:red>Vous n'êtes pas encore digne de cette technique : il faut être "
                        + VoieMartiale.className(need) + ".</color>", NotificationStyle.Warning);
                return null;
            }
            if (RuneManager.ownsRune(caster, store, rune)) {
                MartialKi.notify(caster, store, "<color:gray>Vous connaissez déjà cette technique.</color>", NotificationStyle.Default);
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
            String where = " <color:gray>Ouvrez</color> <color:gold>/es system</color><color:gray>, onglet</color> "
                    + "<color:gold>Compétences</color><color:gray>, pour l'équiper.</color>";              // 2026-11-26
            MartialKi.notify(caster, store, (mv != null
                    ? "<color:gold>" + mv.title() + " appris !</color>"
                    : "<color:gold>Technique du Clan Luo apprise !</color>") + where,
                    NotificationStyle.Success);
            return null;
        }
    }
}
