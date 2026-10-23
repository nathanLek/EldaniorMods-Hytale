package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Avatar du Dragon Divin — competence legendaire de la classe Dragon (Rune_AvatarDragon).
 *
 * Le lanceur se transforme en dragon humanoide divin pendant {@link #DURATION_MS} : modele Eldanior_Dragon_Avatar
 * (3 blocs, sur le squelette et les animations du Loup-garou ; ailes et queue animees), affiche a {@link #MODEL_SCALE}
 * soit trois fois la taille du joueur :
 *  - +50 % de degats infliges, -30 % de degats subis (voir DivineDamageSystem), +20 % de vitesse, endurance max x1.5,
 *    insensible aux projections, immunise aux chutes ;
 *  - aura de feu divin : les ennemis a moins de {@link #AURA_RADIUS} blocs brulent chaque seconde.
 * La transformation libere une onde de choc ; a la fin, le lanceur reprend son apparence, reconstruite depuis son skin
 * (CosmeticsModule) pour retrouver tous ses cosmetiques.
 * Securite : un joueur qui porterait encore le modele de l'avatar sans transformation active (deconnexion, crash)
 * retrouve automatiquement son apparence.
 */
public final class DragonAvatar {

    private DragonAvatar() {}

    // ===== Reglages =====
    public static final String MODEL_ASSET = "Eldanior_Dragon_Avatar";
    private static final String PLAYER_MODEL_ASSET = "Player";
    private static final float MODEL_SCALE = 1.85f;          // modele de 3 blocs -> 5,55 blocs = trois fois le joueur (1,85)
    private static final long DURATION_MS = 30_000;
    private static final float DAMAGE_DEALT = 1.5f, DAMAGE_TAKEN = 0.7f;
    private static final double AURA_RADIUS = 3.5;
    private static final float AURA_DAMAGE = 40f;              // par seconde
    private static final float BURST_DAMAGE = 500f;
    private static final double BURST_RADIUS = 6.0, BURST_PUSH = 14.0, BURST_LIFT = 7.0;

    // ===== Assets =====
    private static final String FORM_EFFECT = "Dragon_Avatar_Forme";
    private static final String BURN_EFFECT = "Muspelheim_Embrasement";
    private static final String FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String FX_PILLAR = "Muspelheim_Fire_Pillar";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String SFX_ROAR = "SFX_Dragon_Void_Finale_Roar";

    static final class Form {
        final long startAt;
        Model original;
        boolean applied;
        long lastPulse;
        Form(long startAt) { this.startAt = startAt; }
    }

    private static final Map<UUID, Form> forms = new ConcurrentHashMap<>();
    /** Lanceurs transformes (lu par DivineDamageSystem). */
    private static final Map<Ref<EntityStore>, Long> transformedUntil = new ConcurrentHashMap<>();

    /** Transformation (ou prolongation si deja transforme). Appele depuis l'interaction (thread du monde). */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        Form existing = forms.get(uuid);
        long now = System.currentTimeMillis();
        if (existing != null) {
            Form renewed = new Form(now);
            renewed.original = existing.original;
            renewed.applied = existing.applied;
            forms.put(uuid, renewed);
        } else {
            forms.put(uuid, new Form(now));
        }
        transformedUntil.put(caster, now + DURATION_MS);
    }

    public static float damageDealtMultiplier(Ref<EntityStore> attacker) { return isTransformed(attacker) ? DAMAGE_DEALT : 1f; }
    public static float damageTakenMultiplier(Ref<EntityStore> target) { return isTransformed(target) ? DAMAGE_TAKEN : 1f; }

    private static boolean isTransformed(Ref<EntityStore> ref) {
        if (transformedUntil.isEmpty() || ref == null) return false;
        Long until = transformedUntil.get(ref);
        return until != null && System.currentTimeMillis() <= until;
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Form f = forms.get(uuid);
        if (f == null) { restoreIfStuck(caster, store, cb); return; }
        long now = System.currentTimeMillis();
        if (!f.applied) { f.applied = true; transform(caster, f, store, cb); }
        if (now - f.startAt >= DURATION_MS) {
            revert(caster, f, store, cb);
            forms.remove(uuid);
            transformedUntil.remove(caster);
            return;
        }
        if (now - f.lastPulse >= 1000) {
            f.lastPulse = now;
            SpellFx.effect(caster, FORM_EFFECT, cb);
            SpellFx.protectFromFall(caster, 1_500, store);
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, AURA_RADIUS, store)) {
                SpellFx.damage(caster, e, "Fire", AURA_DAMAGE, null, null, cb);
                SpellFx.effect(e, BURN_EFFECT, cb);
            }
        }
    }

    private static void transform(Ref<EntityStore> caster, Form f, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(MODEL_ASSET);
        ModelComponent current = store.getComponent(caster, ModelComponent.getComponentType());
        if (asset == null || current == null) {
            EldaniorLogger.error("DragonAvatar: modele " + MODEL_ASSET + " introuvable");
            return;
        }
        if (f.original == null && !MODEL_ASSET.equals(current.getModel().getModelAssetId())) f.original = current.getModel();
        cb.putComponent(caster, ModelComponent.getComponentType(), new ModelComponent(Model.createScaledModel(asset, MODEL_SCALE)));
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return;
        SpellFx.fx(FX_PILLAR, pos, 1.6f, store, cb);
        SpellFx.fx(FX_GOLD, new Vector3d(pos).add(0, 1.5, 0), 5.0f, store, cb);
        SpellFx.fx(FX_EXPLOSION, new Vector3d(pos).add(0, 1, 0), 4.0f, store, cb);
        SpellFx.sound(SFX_ROAR, pos, cb);
        SpellFx.shake(pos, 30, "Impact_Strong", store);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, BURST_RADIUS, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            if (out.lengthSquared() < 1e-4) out.set(SpellFx.forward(caster, store));
            out.normalize().mul(BURST_PUSH).add(0, BURST_LIFT, 0);
            SpellFx.damage(caster, e, "Fire", BURST_DAMAGE, out, "Impact_Strong", cb);
        }
    }

    private static void revert(Ref<EntityStore> caster, Form f, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        restoreAppearance(caster, f.original, store, cb);
        com.eldanior.system.config.Effects.EffectsManager.removeEffect(caster, FORM_EFFECT, cb);
        Vector3d pos = SpellFx.position(caster, store);
        if (pos != null) {
            SpellFx.fx(FX_GOLD, new Vector3d(pos).add(0, 1, 0), 4.0f, store, cb);
            SpellFx.sound(SFX_ROAR, pos, cb);
        }
    }

    /** Joueur reste avec le modele de l'avatar sans transformation active : on lui rend son apparence. */
    private static void restoreIfStuck(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ModelComponent current = store.getComponent(caster, ModelComponent.getComponentType());
        if (current == null || current.getModel() == null || !MODEL_ASSET.equals(current.getModel().getModelAssetId())) return;
        restoreAppearance(caster, null, store, cb);
    }

    /**
     * Rend au joueur son apparence : modele reconstruit depuis son skin (avec tous ses cosmetiques), puis le skin est
     * renvoye aux clients. A defaut de skin, modele d'origine memorise, sinon modele joueur par defaut.
     */
    private static void restoreAppearance(Ref<EntityStore> caster, Model original, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Model model = null;
        var skin = store.getComponent(caster, com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent.getComponentType());
        try {
            if (skin != null && skin.getPlayerSkin() != null) model = com.hypixel.hytale.server.core.cosmetics.CosmeticsModule.get().createModel(skin.getPlayerSkin());
        } catch (Exception e) {
            EldaniorLogger.error("DragonAvatar: reconstruction du skin", e);
        }
        if (model == null) model = original;
        if (model == null) {
            ModelAsset player = ModelAsset.getAssetMap().getAsset(PLAYER_MODEL_ASSET);
            if (player != null) model = Model.createUnitScaleModel(player);
        }
        if (model != null) cb.putComponent(caster, ModelComponent.getComponentType(), new ModelComponent(model));
        if (skin != null) skin.setNetworkOutdated();
    }

    public static void handleDisconnect(UUID uuid) { forms.remove(uuid); }
}
