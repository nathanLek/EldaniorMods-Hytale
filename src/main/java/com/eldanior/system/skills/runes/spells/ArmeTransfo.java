package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.joml.Vector3d;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transformations des mousquets legendaires (Rune de Transformation d'Arme : garde Eldanior_ArmeLegendaireTenue, sort
 * TransformationArme, recharge de la rune) et baionnette fixee le temps d'un estoc.
 *
 *  - transform (Rune de Transformation d'Arme, sort TransformationArme) : le mousquet tenu devient son arme (Mousquet.Arme.transform) pendant 5 minutes. La fin est
 *    gardee a la fois cote serveur (ENDS, par joueur) et dans l'objet (cle Eldanior_TransfoFin, relue apres un redemarrage).
 *    L'arme d'origine se deduit de l'arme transformee (SARS -> Black Bess, canon -> Blue Tiger, Colts -> Green Colt) :
 *    une arme transformee sans fin valide redevient toujours son mousquet.
 *  - ArmeTransfoVeille : chaque seconde, rend le mousquet a la place de toute arme transformee expiree (barre, reserve,
 *    sac a dos, utilitaires), previent a 1 min et a 10 s ; relancee a la connexion (watch). La detente le verifie aussi
 *    (expireHeld) : une arme transformee expiree ne tire plus, elle redevient mousquet.
 *  - MousquetBaionnette : debut du coup de baionnette ; le mousquet tenu devient sa variante « _Baionnette » (meme modele,
 *    baionnette fixee) pendant 0,65 s, puis redevient le mousquet.
 * Aucun effet visuel sur le personnage (demande du 2026-11-15) : seulement un son.
 */
public final class ArmeTransfo {

    private ArmeTransfo() {}

    static final long DURATION_MS = 5 * 60_000, BAYONET_MS = 650;
    static final String KEY_END = "Eldanior_TransfoFin", KEY_ORIGIN = "Eldanior_TransfoOrigine";
    private static final String SFX_TRANSFORM = "SFX_Weapon_Goblin_Cannon_Charge", SFX_BAYONET = "SFX_Blunderbuss_Load";

    /** Fin de la transformation en cours, par joueur. */
    private static final Map<UUID, Long> ENDS = new ConcurrentHashMap<>();
    /** Joueurs dont la veille tourne deja, et ceux dont la baionnette est fixee. */
    private static final Set<UUID> WATCHING = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> BAYONET = ConcurrentHashMap.newKeySet();

    /** Mousquet d'origine d'une arme transformee, sinon null. */
    static String originOf(String transformedId) {
        for (Mousquet.Arme a : Mousquet.Arme.values()) if (a.transform != null && a.transform.equals(transformedId)) return a.id;
        return null;
    }

    /** A la connexion : rend les mousquets des transformations finies pendant l'absence. */
    public static void watch(Ref<EntityStore> player) {
        if (player != null && player.isValid()) RuneSpellRuntime.queue(player, "ArmeTransfoVeille");
    }

    // =====================================================================================================================
    // Capacite des legendaires
    // =====================================================================================================================

    /** Le joueur tient-il un mousquet legendaire (variante a baionnette comprise) ? Garde de la rune. */
    static boolean canTransform(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        ItemStack held = p != null && p.getInventory() != null ? p.getInventory().getActiveHotbarItem() : null;
        Mousquet.Arme a = held == null || held.isEmpty() ? null : Mousquet.arme(held.getItemId());
        return a != null && a.transform != null;
    }

    /** Rune de Transformation d'Arme (TransformationArme) : le mousquet legendaire tenu devient son arme 5 minutes. */
    static void transform(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        {
            InventoryComponent hotbar = store.getComponent(caster, InventoryComponent.Hotbar.getComponentType());
            Player player = store.getComponent(caster, Player.getComponentType());
            UUID uuid = SpellFx.uuid(caster, store);
            if (hotbar == null || player == null || player.getInventory() == null || uuid == null) return;
            short slot = player.getInventory().getActiveHotbarSlot();
            if (slot < 0) return;
            ItemContainer container = hotbar.getInventory();
            ItemStack stack = container.getItemStack(slot);
            Mousquet.Arme a = stack == null || stack.isEmpty() ? null : Mousquet.arme(stack.getItemId());
            if (a == null || a.transform == null) return;
            long end = System.currentTimeMillis() + DURATION_MS;
            ItemStack weapon = new ItemStack(a.transform, 1, stack.getDurability(), stack.getMaxDurability(), stack.getMetadata())
                    .withMetadata(KEY_END, Codec.LONG, end)
                    .withMetadata(KEY_ORIGIN, Codec.STRING, a.id);
            container.setItemStackForSlot(slot, weapon);
            hotbar.markDirty();
            ENDS.put(uuid, end);
            Mousquet.prime(caster, a.transform, cb);
            Vector3d p = SpellFx.position(caster, store);
            if (p != null) SpellFx.sound(SFX_TRANSFORM, p, cb);
            Mousquet.tell(store.getComponent(caster, PlayerRef.getComponentType()), a.name + " se transforme : " + label(a.transform) + " pour 5 minutes.", true);
            watch(caster);
        }
    }

    static String label(String id) {
        if (Mousquet.SARS.equals(id)) return "SARS militaire";
        if (Mousquet.CANON.equals(id)) return "canon de char à main";
        if (ArtefactWeapons.AIGLES.equals(id)) return "Colts de cavalerie";
        return id;
    }

    // =====================================================================================================================
    // Fin des transformations
    // =====================================================================================================================

    public static final class Veille implements RuneSpell {
        @Override public String id() { return "ArmeTransfoVeille"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            UUID uuid = SpellFx.uuid(caster, store);
            if (uuid == null || !WATCHING.add(uuid)) return null;
            return new Watch(uuid);
        }
    }

    private static final class Watch implements RuneSpell.Instance {
        final UUID uuid;
        long next;
        boolean warned60, warned10;

        Watch(UUID uuid) { this.uuid = uuid; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (now < next) return false;
            next = now + 1_000;
            long left = sweep(caster, now, store);
            Long end = ENDS.get(uuid);
            if (left < 0 && (end == null || now >= end)) { ENDS.remove(uuid); return true; }   // plus rien de transforme
            if (left < 0) left = end - now;
            PlayerRef player = store.getComponent(caster, PlayerRef.getComponentType());
            if (!warned60 && left <= 60_000) { warned60 = true; Mousquet.tell(player, "Transformation : encore 1 minute.", false); }
            if (!warned10 && left <= 10_000) { warned10 = true; Mousquet.tell(player, "Transformation : encore 10 secondes.", false); }
            return false;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { WATCHING.remove(uuid); }

        @Override public void abort() { WATCHING.remove(uuid); }
    }

    /** Detente : si l'arme tenue est une transformation expiree, elle redevient son mousquet (vrai = rien ne tire). */
    static boolean expireHeld(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        ItemStack held = p != null && p.getInventory() != null ? p.getInventory().getActiveHotbarItem() : null;
        if (held == null || held.isEmpty() || originOf(held.getItemId()) == null) return false;
        long now = System.currentTimeMillis();
        if (now < endOf(held, SpellFx.uuid(ref, store))) return false;
        sweep(ref, now, store);
        return true;
    }

    /** Fin d'une arme transformee : registre du joueur, sinon cle de l'objet, sinon 0 (deja expiree). */
    private static long endOf(ItemStack s, UUID uuid) {
        Long end = uuid != null ? ENDS.get(uuid) : null;
        if (end != null) return end;
        try {
            Long meta = s.getFromMetadataOrNull(KEY_END, Codec.LONG);
            if (meta != null) return meta;
        } catch (Exception ignored) { }
        return 0L;
    }

    /**
     * Rend le mousquet a la place des armes transformees expirees (et des baionnettes oubliees). Renvoie le temps restant
     * de la transformation la plus proche de sa fin, ou -1 s'il n'en reste aucune.
     */
    static long sweep(Ref<EntityStore> ref, long now, ComponentAccessor<EntityStore> store) {
        Player player = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        Inventory inv = player != null ? player.getInventory() : null;
        if (inv == null) return -1;
        UUID uuid = SpellFx.uuid(ref, store);
        boolean bayonetBusy = uuid != null && BAYONET.contains(uuid);
        long soonest = -1;
        boolean changed = false;
        for (ItemContainer c : new ItemContainer[]{inv.getHotbar(), inv.getStorage(), inv.getBackpack(), inv.getUtility()}) {
            if (c == null) continue;
            for (short i = 0; i < c.getCapacity(); i++) {
                ItemStack s = c.getItemStack(i);
                if (s == null || s.isEmpty()) continue;
                String id = s.getItemId();
                if (id.endsWith(Mousquet.BAYONET_SUFFIX) && Mousquet.isMusket(id)) {
                    if (bayonetBusy) { soonest = soonest < 0 ? BAYONET_MS : Math.min(soonest, BAYONET_MS); continue; }
                    c.setItemStackForSlot(i, swapped(s, id.substring(0, id.length() - Mousquet.BAYONET_SUFFIX.length()), false));
                    changed = true;
                    continue;
                }
                String origin = originOf(id);
                if (origin == null) continue;
                long end = endOf(s, uuid);
                if (now < end) { soonest = soonest < 0 ? end - now : Math.min(soonest, end - now); continue; }
                c.setItemStackForSlot(i, swapped(s, origin, true));
                changed = true;
                if (uuid != null) ENDS.remove(uuid);
                Mousquet.Arme a = Mousquet.arme(origin);
                Mousquet.tell(store.getComponent(ref, PlayerRef.getComponentType()),
                        "Fin de la transformation : " + (a != null ? a.name : origin) + " redevient un mousquet (la rune se recharge).", true);
            }
        }
        if (changed) {
            InventoryComponent hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            if (hotbar != null) hotbar.markDirty();
        }
        return soonest;
    }

    /** Meme objet sous un autre identifiant (durabilite et metadonnees gardees ; cles de transformation retirees). */
    static ItemStack swapped(ItemStack from, String id, boolean dropKeys) {
        BsonDocument meta = from.getMetadata();
        if (dropKeys && meta != null) {
            meta = meta.clone();
            meta.remove(KEY_END);
            meta.remove(KEY_ORIGIN);
            if (meta.isEmpty()) meta = null;
        }
        return new ItemStack(id, Math.max(1, from.getQuantity()), from.getDurability(), from.getMaxDurability(), meta);
    }

    // =====================================================================================================================
    // Baionnette fixee le temps de l'estoc
    // =====================================================================================================================

    public static final class Baionnette implements RuneSpell {
        @Override public String id() { return "MousquetBaionnette"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            UUID uuid = SpellFx.uuid(caster, store);
            InventoryComponent hotbar = store.getComponent(caster, InventoryComponent.Hotbar.getComponentType());
            Player player = store.getComponent(caster, Player.getComponentType());
            if (uuid == null || hotbar == null || player == null || player.getInventory() == null || BAYONET.contains(uuid)) return null;
            short slot = player.getInventory().getActiveHotbarSlot();
            ItemContainer container = hotbar.getInventory();
            ItemStack stack = slot < 0 ? null : container.getItemStack(slot);
            if (stack == null || stack.isEmpty() || stack.getItemId().endsWith(Mousquet.BAYONET_SUFFIX) || !Mousquet.isMusket(stack.getItemId())) return null;
            container.setItemStackForSlot(slot, swapped(stack, stack.getItemId() + Mousquet.BAYONET_SUFFIX, false));
            hotbar.markDirty();
            BAYONET.add(uuid);
            Vector3d p = SpellFx.position(caster, store);
            if (p != null) SpellFx.sound(SFX_BAYONET, p, cb);
            return new Fixed(uuid);
        }
    }

    private static final class Fixed implements RuneSpell.Instance {
        final UUID uuid;
        final long until = System.currentTimeMillis() + BAYONET_MS;

        Fixed(UUID uuid) { this.uuid = uuid; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (now < until) return false;
            BAYONET.remove(uuid);
            sweep(caster, now, store);                                          // la baionnette se retire
            return true;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { BAYONET.remove(uuid); }

        @Override public void abort() { BAYONET.remove(uuid); }
    }
}
