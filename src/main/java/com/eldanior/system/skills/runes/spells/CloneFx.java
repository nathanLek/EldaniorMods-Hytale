package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import org.joml.Vector3d;

/**
 * Copies du lanceur (clones, doppelganger) : de vrais PNJ (role Server/NPC/Roles/Eldanior/Summons/*) habilles du modele, du
 * skin et de l'arme du lanceur. Ils ont un inventaire, des points de vie et jouent les vraies animations d'attaque de l'arme.
 * Le PNJ est cree hors du tick (world.execute) : sa reference n'existe qu'au tick suivant.
 */
public final class CloneFx {

    private CloneFx() {}

    private static final String HP_MODIFIER = "Eldanior_Clone_Vie";

    /** Apparence du lanceur, capturee au lancement (modele, skin, nom affiche au-dessus de lui). */
    record Look(Model model, PlayerSkin skin, String nameplate) {
        static Look of(Ref<EntityStore> caster, Store<EntityStore> store) {
            ModelComponent mc = store.getComponent(caster, ModelComponent.getComponentType());
            PlayerSkinComponent skin = store.getComponent(caster, PlayerSkinComponent.getComponentType());
            Nameplate np = store.getComponent(caster, Nameplate.getComponentType());
            return new Look(mc != null ? mc.getModel() : null, skin != null ? skin.getPlayerSkin() : null, np != null ? np.getText() : null);
        }
    }

    /** Une copie PNJ : ref est null tant que le PNJ n'est pas cree (ou si la creation a echoue : failed). */
    static final class Npc {
        volatile Ref<EntityStore> ref;
        volatile boolean failed;
        final long born;
        Npc(long born) { this.born = born; }
        boolean alive() { Ref<EntityStore> r = ref; return r != null && r.isValid(); }
    }

    /**
     * Cree une copie PNJ du lanceur a pos (au prochain passage du monde) : modele, skin, arme en main, vie maximale
     * (maxHealth <= 0 : celle du role), effet de teinte. Elle est inscrite comme invocation du lanceur (pas de tir ami).
     */
    static Npc spawn(World world, String roleId, Ref<EntityStore> owner, Vector3d pos, Rotation3f rot, Look look,
                     ItemStack weapon, float maxHealth, String effectId, boolean invulnerable, long now) {
        Npc npc = new Npc(now);
        Vector3d at = new Vector3d(pos);
        world.execute(() -> {
            try {
                Store<EntityStore> store = world.getEntityStore().getStore();
                var pair = NPCPlugin.get().spawnNPC(store, roleId, null, at, rot);
                Ref<EntityStore> ref = pair != null ? pair.first() : null;
                if (ref == null || !ref.isValid()) { npc.failed = true; EldaniorLogger.warn("[CloneFx] PNJ non cree : " + roleId); return; }
                dress(ref, look, weapon, maxHealth, effectId, store);
                RuneSpellRuntime.registerSummon(owner, ref, invulnerable);
                npc.ref = ref;
            } catch (Exception e) {
                npc.failed = true;
                EldaniorLogger.error("[CloneFx] creation du PNJ " + roleId, e);
            }
        });
        return npc;
    }

    private static void dress(Ref<EntityStore> ref, Look look, ItemStack weapon, float maxHealth, String effectId, Store<EntityStore> store) {
        if (look.model() != null) store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(look.model()));
        if (look.skin() != null) store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(look.skin()));
        if (look.nameplate() != null) store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(look.nameplate()));
        setWeapon(ref, weapon, store);
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null) {
            int health = DefaultEntityStatTypes.getHealth();
            EntityStatValue hp = stats.get(health);
            if (maxHealth > 0 && hp != null)
                stats.putModifier(health, HP_MODIFIER, new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, maxHealth - hp.getMax()));
            stats.maximizeStatValue(health);
        }
        if (effectId != null) SpellFx.effect(ref, effectId, store);
    }

    /** Met l'objet (copie d'un seul exemplaire, ou main nue si null) dans la main du PNJ. */
    static void setWeapon(Ref<EntityStore> ref, ItemStack item, ComponentAccessor<EntityStore> accessor) {
        NPCEntity npc = accessor.getComponent(ref, NPCEntity.getComponentType());
        Inventory inv = npc != null ? npc.getInventory() : null;
        if (inv == null) return;
        inv.getHotbar().setItemStackForSlot((short) 0, item == null || item.isEmpty() ? ItemStack.EMPTY : new ItemStack(item.getItemId(), 1));
        inv.setActiveHotbarSlot(ref, (byte) 0, accessor);
    }

    /** Identifiant de l'objet en main du PNJ (null : main nue). */
    static String weaponId(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor) {
        NPCEntity npc = accessor.getComponent(ref, NPCEntity.getComponentType());
        ItemStack it = npc != null && npc.getInventory() != null ? npc.getInventory().getItemInHand() : null;
        return it == null || it.isEmpty() ? null : it.getItemId();
    }

    /** Objet en main du lanceur (null : main nue). */
    static ItemStack heldItem(Ref<EntityStore> caster, ComponentAccessor<EntityStore> accessor) {
        Player p = accessor.getComponent(caster, Player.getComponentType());
        ItemStack it = p != null && p.getInventory() != null ? p.getInventory().getItemInHand() : null;
        return it == null || it.isEmpty() ? null : it;
    }

    /** Arme de corps a corps du lanceur : celle en main, sinon la premiere de la barre d'action ; null : main nue. */
    static ItemStack meleeWeapon(Ref<EntityStore> caster, ComponentAccessor<EntityStore> accessor) {
        ItemStack held = heldItem(caster, accessor);
        if (isWeapon(held)) return held;
        Player p = accessor.getComponent(caster, Player.getComponentType());
        ItemContainer bar = p != null && p.getInventory() != null ? p.getInventory().getHotbar() : null;
        if (bar == null) return null;
        for (short i = 0; i < bar.getCapacity(); i++) {
            ItemStack it = bar.getItemStack(i);
            if (isWeapon(it)) return it;
        }
        return null;
    }

    private static boolean isWeapon(ItemStack it) {
        return it != null && !it.isEmpty() && it.getItemId() != null && it.getItemId().startsWith("Weapon_")
                && !it.getItemId().startsWith("Weapon_Spellbook") && !it.getItemId().startsWith("Weapon_Staff");
    }

    /** Vie maximale d'une entite (0 si inconnue). */
    static float maxHealth(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor) {
        EntityStatMap stats = accessor.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue hp = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return hp != null ? hp.getMax() : 0f;
    }

    /** Remplit une stat (ex. energie de signature : le clone peut lancer la competence de son arme). */
    static void fillStat(Ref<EntityStore> ref, int stat, ComponentAccessor<EntityStore> accessor) {
        EntityStatMap stats = accessor.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null && stats.get(stat) != null) stats.maximizeStatValue(stat);
    }

    /** Pose une entite marquee dans un emplacement du role du PNJ (LockedTarget = cible de combat). */
    static void mark(Ref<EntityStore> npc, String slot, Ref<EntityStore> target, ComponentAccessor<EntityStore> accessor) {
        MarkedEntitySupport marks = MarkedEntitySupport.get(npc, accessor);
        if (marks != null) marks.setMarkedEntity(slot, target);
    }

    /** Force l'etat du role du PNJ (ex. "Combat" a l'envoi d'un clone ; sous-etat null : celui par defaut). */
    static void setState(Ref<EntityStore> npc, String state, String subState, ComponentAccessor<EntityStore> accessor) {
        try {
            StateSupport s = npc != null && npc.isValid() ? StateSupport.get(npc, accessor) : null;
            if (s != null) s.setState(npc, state, subState, accessor);
        } catch (Exception e) {
            EldaniorLogger.error("[CloneFx] etat " + state, e);
        }
    }

    /** Le PNJ npc considere other comme hostile pendant seconds secondes. */
    static void hostile(Ref<EntityStore> npc, Ref<EntityStore> other, double seconds, ComponentAccessor<EntityStore> accessor) {
        com.eldanior.system.util.NpcAttitudes.hostile(npc, other, seconds, accessor);
    }

    /** Monstre : PNJ hostile par defaut aux joueurs (les animaux paisibles ne sont pas des cibles). */
    static boolean isMonster(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor) {
        if (ref == null || !ref.isValid() || accessor.getComponent(ref, NPCEntity.getComponentType()) == null) return false;
        WorldSupport ws = WorldSupport.get(ref, accessor);
        return ws != null && ws.getDefaultPlayerAttitude() == Attitude.HOSTILE;
    }

    /**
     * Point de laisse du PNJ : son role s'y rend lui-meme (Sensor Leash + Seek), avec les animations de marche et de course
     * du moteur. C'est ainsi que les copies suivent le lanceur sans glisser.
     */
    static void leash(Ref<EntityStore> ref, Vector3d pos, ComponentAccessor<EntityStore> accessor) {
        NPCEntity npc = ref != null && ref.isValid() ? accessor.getComponent(ref, NPCEntity.getComponentType()) : null;
        if (npc != null && pos != null) npc.setLeashPoint(new Vector3d(pos));
    }

    /** Oriente une copie (corps et tete) sans la deplacer : son deplacement et ses animations restent ceux du moteur. */
    static void face(Ref<EntityStore> ref, Rotation3f body, Rotation3f head, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t != null && body != null) t.setRotation(body);
        HeadRotation h = head != null ? store.getComponent(ref, HeadRotation.getComponentType()) : null;
        if (h != null) h.setRotation(head);
    }

    /** Oriente un PNJ (corps et tete) vers un point, depuis ses yeux. */
    public static void faceTowards(Ref<EntityStore> ref, Vector3d point, Store<EntityStore> store) {
        Vector3d at = SpellFx.position(ref, store);
        if (at == null || point == null) return;
        Vector3d dir = new Vector3d(point).sub(at.x, at.y + 1.6, at.z);
        if (dir.lengthSquared() < 1e-4) return;
        face(ref, SpellFx.facing(dir, false), SpellFx.facing(dir, true), store);
    }

    /** Pose une copie : position, corps, tete, etat de mouvement (anime par le client). */
    static void pose(Ref<EntityStore> ref, Vector3d pos, Rotation3f body, Rotation3f head, MovementStates states, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        SpellFx.hold(ref, pos, body, store);
        if (head != null) {
            HeadRotation h = store.getComponent(ref, HeadRotation.getComponentType());
            if (h != null) h.setRotation(head);
        }
        if (states != null) {
            MovementStatesComponent ms = store.getComponent(ref, MovementStatesComponent.getComponentType());
            if (ms != null && !states.equals(ms.getMovementStates())) ms.setMovementStates(new MovementStates(states));
        }
    }

    /** Etat de mouvement actuel du lanceur (copie). */
    static MovementStates statesOf(Ref<EntityStore> caster, Store<EntityStore> store) {
        MovementStatesComponent ms = store.getComponent(caster, MovementStatesComponent.getComponentType());
        return ms != null && ms.getMovementStates() != null ? new MovementStates(ms.getMovementStates()) : null;
    }
}
