package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.observateur.ObservateurManager;
import com.eldanior.system.classes.observateur.SansVisagePage;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune Sans Visage (Tenebres, Legendaire), deuxieme competence de l'Observateur (2026-11-17). Un clic ouvre la fenetre
 * Sans Visage (classes/observateur/SansVisagePage) : prendre l'apparence d'un joueur du monde, d'un PNJ, d'un animal ou d'un
 * monstre, tirer une apparence de joueur au hasard, afficher une fausse classe, prendre un nom d'emprunt.
 *
 * Apparence : ModelComponent remplace (modele d'entite) ; pour une apparence de joueur, PlayerSkinComponent aussi (comme
 * CloneFx) et le modele est reconstruit depuis le skin (CosmeticsModule). Le skin d'origine est garde pour le masque qui tombe.
 * Ces composants ne sont pas sauvegardes : a la reconnexion le joueur retrouve son vrai visage (le nom d'emprunt, lui, est
 * garde par ObservateurManager). Le masque tombe aussi a la mort (DeathXPSystem) et a la Sentence Silencieuse.
 * Chaque nouveau visage coute COST mana (la fenetre est gratuite ; fiche : overrides).
 * Fiche de conception : scripts/runes/specs/Rune_SansVisage.json
 */
public final class SansVisage implements RuneSpell {

    // ===== Reglages =====
    public static final int COST = 300;

    // ===== Assets (verifies : rune_tool assets) =====
    private static final String FX_SMOKE = "Smoke_Black", SFX = "SFX_Crawler_Void_Despawn";

    /** Apparence prise : genre (joueur, pnj, animal, monstre, aleatoire), nom montre, modele (portrait), skin d'origine. */
    public record Mask(String kind, String label, String modelId, PlayerSkin original) {}

    private static final Map<UUID, Mask> MASKS = new ConcurrentHashMap<>();
    /** Fausse classe affichee aux autres (groupe, profils) : id de classe. */
    private static final Map<UUID, String> FAKE_CLASS = new ConcurrentHashMap<>();

    @Override public String id() { return "SansVisage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Player player = store.getComponent(caster, Player.getComponentType());
        PlayerRef pr = store.getComponent(caster, PlayerRef.getComponentType());
        if (player == null || pr == null) return null;
        cb.run(s -> {
            try {
                if (caster.isValid()) player.getPageManager().openCustomPage(caster, s, new SansVisagePage(pr));
            } catch (Exception e) {
                EldaniorLogger.error("[Sans Visage] ouverture de la fenetre", e);
            }
        });
        return null;
    }

    // ===================== ETAT =====================

    public static Mask mask(UUID uuid) { return uuid == null ? null : MASKS.get(uuid); }

    /** true si l'Observateur est masque (apparence, nom d'emprunt ou fausse classe) : bonus de la Sentence Silencieuse. */
    public static boolean isDisguised(UUID uuid) {
        return uuid != null && (MASKS.containsKey(uuid) || FAKE_CLASS.containsKey(uuid) || ObservateurManager.hasAlias(uuid));
    }

    /** Classe montree aux autres : la fausse si elle est posee, sinon la vraie. */
    public static ClassModel shownClass(UUID uuid, PlayerLevelData data) {
        String fake = uuid == null ? null : FAKE_CLASS.get(uuid);
        ClassModel m = fake != null ? ClassManager.get(fake) : null;
        return m != null ? m : data == null ? null : ClassManager.get(data.getPlayerClassId());
    }

    public static String fakeClassId(UUID uuid) { return uuid == null ? null : FAKE_CLASS.get(uuid); }

    public static void setFakeClass(UUID uuid, String classId) {
        if (uuid == null) return;
        if (classId == null || classId.isEmpty()) FAKE_CLASS.remove(uuid); else FAKE_CLASS.put(uuid, classId);
    }

    /**
     * Nameplate impose par le masque : null = rien a imposer (nom d'emprunt ou nom normal), "" = aucun nom (creature sans nom
     * d'emprunt : un ours qui porte un nom de joueur se trahirait).
     */
    public static String nameplate(UUID uuid) {
        Mask m = mask(uuid);
        if (m == null || "joueur".equals(m.kind()) || "aleatoire".equals(m.kind())) return null;
        return ObservateurManager.hasAlias(uuid) ? null : "";
    }

    // ===================== CHANGER DE VISAGE =====================

    /** Mana suffisant puis preleve ; message sinon. A appeler sur le thread du monde. */
    public static boolean pay(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (MartialKi.stat(ref, DefaultEntityStatTypes.getMana(), store) < COST) {
            MartialKi.notify(ref, store, "<color:red>Pas assez de mana (" + COST + ").</color>", NotificationStyle.Warning);
            return false;
        }
        MartialKi.addStat(ref, DefaultEntityStatTypes.getMana(), -COST, store);
        return true;
    }

    /** Prend le modele d'entite modelId (PNJ, animal, monstre). Retourne false si le modele est introuvable. */
    public static boolean wearModel(Ref<EntityStore> ref, Store<EntityStore> store, String kind, String modelId, String label) {
        ModelAsset asset = modelId == null ? null : ModelAsset.getAssetMap().getAsset(modelId);
        if (asset == null) return false;
        UUID uuid = SpellFx.uuid(ref, store);
        if (uuid == null) return false;
        Mask before = MASKS.get(uuid);
        PlayerSkin original = before != null ? before.original() : currentSkin(ref, store);
        Model model = Model.createScaledModel(asset, 1f, asset.generateRandomAttachmentIds());
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        if (before != null && before.original() != null && "joueur aleatoire".contains(before.kind()))   // revenir a son skin
            store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(before.original()));
        MASKS.put(uuid, new Mask(kind, label, modelId, original));
        puff(ref, store);
        return true;
    }

    /** Prend une apparence de joueur (skin copie ou aleatoire). */
    public static boolean wearSkin(Ref<EntityStore> ref, Store<EntityStore> store, String kind, PlayerSkin skin, String label) {
        if (skin == null) return false;
        UUID uuid = SpellFx.uuid(ref, store);
        if (uuid == null) return false;
        Model model;
        try {
            model = CosmeticsModule.get().createModel(skin);
        } catch (Exception e) {
            EldaniorLogger.error("[Sans Visage] modele du skin", e);
            return false;
        }
        if (model == null) return false;
        Mask before = MASKS.get(uuid);
        PlayerSkin original = before != null ? before.original() : currentSkin(ref, store);
        store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        MASKS.put(uuid, new Mask(kind, label, "Player", original));
        puff(ref, store);
        return true;
    }

    /** Apparence de joueur tiree au hasard parmi les cosmetiques du jeu. */
    public static PlayerSkin randomSkin() {
        try {
            return CosmeticsModule.get().generateRandomSkin(new java.util.Random(ThreadLocalRandom.current().nextLong()));
        } catch (Exception e) {
            EldaniorLogger.error("[Sans Visage] skin aleatoire", e);
            return null;
        }
    }

    /** Skin actuel d'un joueur (copie : le composant peut etre remplace ensuite). */
    public static PlayerSkin currentSkin(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        PlayerSkinComponent sk = ref != null && ref.isValid() ? store.getComponent(ref, PlayerSkinComponent.getComponentType()) : null;
        return sk != null ? sk.getPlayerSkin() : null;
    }

    private static void puff(Ref<EntityStore> ref, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(ref, store);
        if (p == null) return;
        try {
            SpellFx.fx(FX_SMOKE, new Vector3d(p).add(0, 0.9, 0), 1.6f, store, store);
            SpellFx.sound(SFX, p, store);
        } catch (Exception ignored) { }
    }

    // ===================== LE MASQUE TOMBE =====================

    /**
     * Rend son vrai visage au joueur (skin d'origine, modele reconstruit), retire la fausse classe et le nom d'emprunt.
     * cb non null : ecritures differees (pendant un tick de systeme) ; sinon directes (fenetre, thread du monde).
     */
    public static void reveal(Ref<EntityStore> ref, UUID uuid, Store<EntityStore> store, CommandBuffer<EntityStore> cb, String why) {
        if (uuid == null) return;
        Mask m = MASKS.remove(uuid);
        boolean had = m != null | FAKE_CLASS.remove(uuid) != null;
        if (ObservateurManager.hasAlias(uuid)) { ObservateurManager.setAlias(uuid, null); had = true; }
        if (m != null && ref != null && ref.isValid()) restoreLook(ref, m.original(), store, cb);
        if (had && why != null && ref != null && ref.isValid()) MartialKi.notify(ref, store, "<color:gray>" + why + "</color>", NotificationStyle.Default);
    }

    /** Remet le skin d'origine et le modele qui en est construit (repli : apparence actuelle reconstruite, comme DragonAvatar). */
    private static void restoreLook(Ref<EntityStore> ref, PlayerSkin original, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        try {
            if (original != null) {
                Model model = CosmeticsModule.get().createModel(original);
                if (cb != null) {
                    cb.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(original));
                    if (model != null) cb.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
                } else {
                    store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(original));
                    if (model != null) store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
                }
                return;
            }
            if (cb != null) { DragonAvatar.restoreAppearance(ref, null, store, cb); return; }
            ModelAsset player = ModelAsset.getAssetMap().getAsset("Player");                 // aucun skin connu : modele de base
            if (player != null) store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(Model.createUnitScaleModel(player)));
        } catch (Exception e) {
            EldaniorLogger.error("[Sans Visage] retour au vrai visage", e);
        }
    }

    /** Mort du joueur (DeathXPSystem) : le masque tombe. */
    public static void onDeath(UUID uuid, Ref<EntityStore> ref, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (uuid != null && (MASKS.containsKey(uuid) || FAKE_CLASS.containsKey(uuid)))
            reveal(ref, uuid, store, cb, "Dans la mort, votre masque est tombé.");
    }

    /** Deconnexion : apparence et fausse classe oubliees (rien n'est sauvegarde) ; le nom d'emprunt reste. */
    public static void handleDisconnect(UUID uuid) {
        if (uuid == null) return;
        MASKS.remove(uuid);
        FAKE_CLASS.remove(uuid);
    }
}
