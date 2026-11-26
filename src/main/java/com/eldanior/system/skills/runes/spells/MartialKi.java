package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;

/**
 * Outils communs des runes de la voie martiale du Clan Luo (Ki de l'Epee, Meditation martiale et runes de palier) :
 * palier du lanceur, couleur de ses lames de Ki, arme a lame, statistiques en points fixes, messages, et memoire des coups
 * infliges par le Ki lui-meme (pour qu'ils ne relancent pas les techniques du Ki).
 */
final class MartialKi {

    private MartialKi() {}

    /** Modele de lame d'air par palier (Tenryumaru_Lame_<cle>) : blanc, blanc, bleu, cyan, violet, or, blanc nacre, blanc. */
    private static final String[] BLADE = {"Shirasagi_H", "Shirasagi_H", "Aonami_H", "Aonami_H", "Tsukikage_H", "H", "Shirasagi_H", "Shirasagi_H"};

    /** Palier de la voie martiale du lanceur (1 a 8) ; 1 hors de la voie (rune obtenue autrement, admins). */
    static int tier(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Integer npc = ref != null ? NPC_TIER.get(ref) : null;                  // PNJ de poste (rang martial, MartialNpcs)
        if (npc != null) return Math.clamp(npc, 1, 8);
        PlayerLevelData data = data(ref, store);
        return Math.max(1, VoieMartiale.tier(data));
    }

    /** Palier martial des PNJ de poste qui combattent (renseigne par MartialNpcs). */
    static final Map<Ref<EntityStore>, Integer> NPC_TIER = new java.util.concurrent.ConcurrentHashMap<>();

    static PlayerLevelData data(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid()) return null;
        try { return store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()); }
        catch (Exception e) { return null; }
    }

    static String bladeKey(int tier) { return BLADE[Math.clamp(tier, 1, 8) - 1]; }

    /** Lame de la couleur du palier inclinee comme le coup : orient = H, G, D ou V (Tenryumaru_Lame_<couleur>_<orient>). */
    static String bladeKey(int tier, String orient) {
        String key = bladeKey(tier);
        return key.substring(0, key.length() - 1) + orient;
    }

    /** Arme a lame en main (epee, katana, dague) ; au palier 8 (Dieu Martial), toute arme et les mains nues. */
    static boolean holdsBlade(Ref<EntityStore> ref, Store<EntityStore> store, int tier) {
        if (tier >= 8) return true;
        String id = ArtefactWeapons.held(ref, store);
        if (id == null) return false;
        if (LuoKatanas.of(id) != null) return true;
        String low = id.toLowerCase();
        return low.contains("sword") || low.contains("katana") || low.contains("dagger") || low.contains("blade") || low.contains("epee");
    }

    /** Valeur actuelle d'une statistique (vie, mana, endurance...), -1 si illisible. */
    static float stat(Ref<EntityStore> ref, int statIndex, ComponentAccessor<EntityStore> store) {
        try {
            EntityStatMap map = store.getComponent(ref, EntityStatMap.getComponentType());
            var v = map == null ? null : map.get(statIndex);
            return v == null ? -1 : v.get();
        } catch (Exception e) { return -1; }
    }

    /** Ajoute (ou retire) un nombre fixe de points a une statistique (borne par le moteur). */
    static void addStat(Ref<EntityStore> ref, int statIndex, float amount, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid() || amount == 0) return;
        try {
            EntityStatMap map = store.getComponent(ref, EntityStatMap.getComponentType());
            if (map != null && map.get(statIndex) != null) map.addStatValue(statIndex, amount);
        } catch (Exception e) {
            EldaniorLogger.error("MartialKi: statistique", e);
        }
    }

    static void notify(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, String text, NotificationStyle style) {
        try {
            PlayerRef p = ref != null && ref.isValid() ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            if (p != null) NotificationHelper.sendNotification(p, text, style);
        } catch (Exception ignored) { }
    }

    /**
     * Degats de Ki infliges par une technique : la cible est notee (own) pour que ce coup ne relance pas les techniques du Ki
     * (onOwnerHit), et marque comme echo pour les autres sorts qui copient les coups du lanceur.
     */
    static void kiDamage(Ref<EntityStore> caster, Ref<EntityStore> target, float amount, String cause, org.joml.Vector3d knockback,
                         Map<Ref<EntityStore>, Long> own, CommandBuffer<EntityStore> cb) {
        if (target == null || !target.isValid() || amount <= 0) return;
        if (own != null) own.put(target, System.currentTimeMillis() + 400);
        RuneSpellRuntime.markEcho(target, amount);
        SpellFx.damage(caster, target, cause, amount, knockback, null, cb);
    }
}
