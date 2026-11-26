package com.eldanior.system.skills.skills.passives.Divin;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Outils communs des 7 Anges et 7 Peches (passifs divins, refaits le 2026-11-24). */
public final class PouvoirsDivins {

    private PouvoirsDivins() {}

    public static boolean isPlayer(Ref<EntityStore> ref, Store<EntityStore> store) {
        return ref != null && ref.isValid() && store.getComponent(ref, Player.getComponentType()) != null;
    }

    public static PlayerLevelData data(Ref<EntityStore> ref, Store<EntityStore> store) {
        return ref != null && ref.isValid() ? store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()) : null;
    }

    public static UUID uuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent c = ref != null && ref.isValid() ? store.getComponent(ref, UUIDComponent.getComponentType()) : null;
        return c != null ? c.getUuid() : null;
    }

    /** Vie / vie max (0..1), -1 si inconnue. */
    public static float healthRatio(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatValue hp = health(ref, store);
        return hp != null && hp.getMax() > 0 ? hp.get() / hp.getMax() : -1f;
    }

    private static EntityStatValue health(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap map = ref != null && ref.isValid() ? store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()) : null;
        return map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
    }

    /** Rend une part de la vie max. */
    public static void healPercent(Ref<EntityStore> ref, Store<EntityStore> store, float part) {
        EntityStatMap map = ref != null && ref.isValid() ? store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()) : null;
        EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
        if (hp == null || hp.get() <= 0 || hp.get() >= hp.getMax()) return;
        map.setStatValue(DefaultEntityStatTypes.getHealth(), Math.min(hp.getMax(), hp.get() + hp.getMax() * part));
    }

    /** Ce coup tue-t-il un joueur ? */
    public static boolean killsPlayer(Damage damage, Ref<EntityStore> victimRef, Store<EntityStore> store) {
        if (damage.isCancelled() || !isPlayer(victimRef, store)) return false;
        EntityStatValue hp = health(victimRef, store);
        return hp != null && hp.get() > 0 && hp.get() - damage.getAmount() <= 0;
    }

    /** Nombre de pieces d'armure portees (0 a 4). */
    public static int armorPieces(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        ItemContainer armor = p != null ? p.getInventory().getArmor() : null;
        if (armor == null) return 0;
        int n = 0;
        for (short i = 0; i < armor.getCapacity(); i++) {
            ItemStack it = armor.getItemStack(i);
            if (it != null && !it.isEmpty()) n++;
        }
        return Math.min(4, n);
    }

    /** Rien dans la main (objet actif de la barre d'action). */
    public static boolean emptyHand(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        if (p == null) return false;
        ItemStack held = p.getInventory().getActiveHotbarItem();
        return held == null || held.isEmpty();
    }

    /** Membres du groupe du joueur, dans le meme monde, a moins de radius blocs (sans lui). */
    public static List<Ref<EntityStore>> alliesNear(Ref<EntityStore> self, Store<EntityStore> store, double radius) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        UUID me = uuid(self, store);
        Party party = me != null ? PartyManager.getParty(me) : null;
        TransformComponent t = party != null ? store.getComponent(self, TransformComponent.getComponentType()) : null;
        if (t == null) return out;
        Vector3d pos = t.getPosition();
        for (UUID member : party.getMemberUUIDs()) {
            if (member.equals(me)) continue;
            PlayerRef pr = Universe.get().getPlayer(member);
            Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
            if (ref == null || !ref.isValid() || ref.getStore() != store) continue;
            TransformComponent mt = store.getComponent(ref, TransformComponent.getComponentType());
            if (mt != null && mt.getPosition().distanceSquared(pos) <= radius * radius) out.add(ref);
        }
        return out;
    }
}
