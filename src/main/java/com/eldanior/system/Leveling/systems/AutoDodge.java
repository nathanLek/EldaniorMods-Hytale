package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.VelocityThresholdStyle;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Esquive automatique des joueurs (refonte du 2026-11-25, docs/plan-equilibrage-stats.md).
 *
 * <p>Chance : {@link StatConfig#DODGE_CHANCE} (courbe de l'Agilite, 30 % vers 3 500 points, passifs Esquive compris,
 * plafond dur 30 %), puis ecart de niveau (dans les deux sens) et moitie de chance contre un coup dans le dos.
 *
 * <p>Jamais : chute, feu, poison, sorts elementaires (seulement coups physiques et projectiles d'une entite) ;
 * pendant sa propre attaque ou sa garde (l'animation d'attaque n'est plus coupee) ; etourdi ou immobilise ;
 * sans endurance ; moins de {@link #COOLDOWN_MS} ms apres la precedente ; contre un Spectre.
 *
 * <p>Reussite : le coup est annule, le joueur dashe a l'oppose de l'ennemi (ennemi a gauche -> a droite, devant -> en
 * arriere, derriere -> en avant, en diagonale pour ces deux-la), pas de cote court, animation d'esquive gauche/droite,
 * trainee, son de roulade, secousse de camera. Coute de l'endurance comme l'esquive vanilla.
 */
public final class AutoDodge {

    private AutoDodge() {}

    /** Causes que l'on peut esquiver (ids des DamageCause vanilla). */
    private static final Set<String> DODGEABLE = Set.of("Physical", "Bludgeoning", "Slashing", "Projectile");
    /** Effets qui empechent d'esquiver. */
    private static final String[] BLOCKING_EFFECTS = {"Stun", "Root", "Hit_Interrupt_Stunned", "Bomb_Explode_Stun"};
    /** Classe dont les coups ne s'esquivent pas. */
    private static final String UNDODGEABLE_CLASS = "spectre_assassin";

    public static final long COOLDOWN_MS = 5000L;     // 2 s au premier essai : esquives en rafale sous 25 squelettes
    private static final float STAMINA_COST = 2f;     // comme l'esquive vanilla
    private static final float DASH_SPEED = 9f;       // pas de cote court (13 = esquive vanilla, trop long : effet de teleportation)
    private static final String FX_LEFT = "Eldanior_Esquive_Gauche", FX_RIGHT = "Eldanior_Esquive_Droite";   // animation + trainee
    private static final String SFX = "SFX_Player_Roll";
    private static final String CAMERA = "Daggers_Dash_Side";
    private static final double DIAGONAL_SIDE = 0.7;  // part laterale du dash avant/arriere

    private static final Map<UUID, Long> lastDodge = new ConcurrentHashMap<>();

    /** Le coup vient d'une entite (attaque, projectile) et sa cause peut s'esquiver. Sert aussi aux aventuriers. */
    public static boolean isDodgeable(Damage damage) {
        if (!(damage.getSource() instanceof Damage.EntitySource src) || !src.getRef().isValid()) return false;
        String cause = damage.getCause() != null ? damage.getCause().getId() : null;
        return cause != null && DODGEABLE.contains(cause);
    }

    /** Tente l'esquive ; true si le coup est annule. log (journal de combat, peut etre null) : la decision expliquee. */
    public static boolean tryDodge(Ref<EntityStore> victim, Store<EntityStore> store, Damage damage, CommandBuffer<EntityStore> cb,
                                   StringBuilder log) {
        PlayerLevelData data = store.getComponent(victim, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return false;
        if (!isDodgeable(damage)) return why(log, "impossible (cause " + (damage.getCause() != null ? damage.getCause().getId() : "?") + ")");
        Ref<EntityStore> attacker = ((Damage.EntitySource) damage.getSource()).getRef();
        if (attacker.equals(victim)) return false;

        // --- attaquant : niveau, classe ---
        int attackerLevel = -1;
        var mobType = EldaniorSystem.get().getMobLevelDataType();
        var mob = mobType != null ? store.getComponent(attacker, mobType) : null;
        if (mob != null) attackerLevel = mob.getLevel();
        PlayerLevelData attackerData = store.getComponent(attacker, EldaniorSystem.get().getPlayerLevelDataType());
        if (attackerData != null) {
            if (UNDODGEABLE_CLASS.equals(attackerData.getPlayerClassId())) return why(log, "impossible (Spectre)");
            if (attackerLevel < 0) attackerLevel = attackerData.getLevel();
        }

        // --- etat de la victime ---
        UUIDComponent uuidC = store.getComponent(victim, UUIDComponent.getComponentType());
        UUID uuid = uuidC != null ? uuidC.getUuid() : null;
        long now = System.currentTimeMillis();
        if (uuid != null) {
            Long last = lastDodge.get(uuid);
            if (last != null && now - last < COOLDOWN_MS)
                return why(log, "en attente (" + String.format("%.1f", (COOLDOWN_MS - (now - last)) / 1000f) + " s)");
        }
        if (isBusy(victim, store)) return why(log, "impossible (en attaque ou en garde)");
        for (String e : BLOCKING_EFFECTS) if (EffectsManager.hasEffect(victim, e, store)) return why(log, "impossible (" + e + ")");
        EntityStatMap stats = store.getComponent(victim, EntityStatMap.getComponentType());
        int stamina = DefaultEntityStatTypes.getStamina();
        if (stats == null || stats.get(stamina) == null || stats.get(stamina).get() < STAMINA_COST) return why(log, "impossible (endurance)");

        // --- chance ---
        ClassModel cls = ClassManager.get(data.getPlayerClassId());
        float chance = StatConfig.DODGE_CHANCE.getFinalValue(data, cls);
        if (chance <= 0f) return why(log, "0 %");
        if (attackerLevel >= 0) chance *= levelFactor(attackerLevel - data.getLevel());

        Vector3d pos = position(victim, store), ap = position(attacker, store);
        Vector3d fwd = forward(victim, store);
        Vector3d right = new Vector3d(-fwd.z, 0, fwd.x);                    // meme convention que ArkiesDomini / AiglesDuDesert
        double f = 1, r = 0;
        if (pos != null && ap != null) {
            Vector3d to = new Vector3d(ap).sub(pos);
            to.y = 0;
            if (to.lengthSquared() > 1e-6) {
                to.normalize();
                f = to.dot(fwd);
                r = to.dot(right);
            }
        }
        if (f < -0.5) chance *= 0.5f;                                         // frappe dans le dos : moitie de chance
        chance = Math.min(chance, StatConfig.DODGE_MAX);

        String pct = String.format("%.1f %%", chance) + (f < -0.5 ? " (dans le dos)" : "");
        if (ThreadLocalRandom.current().nextFloat() * 100f >= chance) return why(log, pct + " : ratée");
        why(log, pct + " : RÉUSSIE");

        // --- esquive reussie ---
        damage.setCancelled(true);
        if (uuid != null) lastDodge.put(uuid, now);
        stats.addStatValue(stamina, -STAMINA_COST);
        dash(victim, fwd, right, f, r, store, cb);
        creditPassives(victim, data, cb);
        return true;
    }

    private static boolean why(StringBuilder log, String text) {
        if (log != null) log.append("esquive ").append(text);
        return false;
    }

    /** Ecart de niveau (attaquant - victime) : au-dela de 5 niveaux, -5 % par niveau (mini x0,25) ; en dessous, +2 % (maxi x1,3). */
    private static float levelFactor(int gap) {
        if (gap > 5) return Math.max(0.25f, 1f - (gap - 5) * 0.05f);
        if (gap < -5) return Math.min(1.3f, 1f + (-gap - 5) * 0.02f);
        return 1f;
    }

    /** En pleine attaque (clic gauche) ou en garde / charge (clic droit) : pas d'esquive, l'animation n'est pas coupee. */
    private static boolean isBusy(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            var manager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            if (manager == null) return false;
            for (var chain : manager.getChains().values()) {
                if (chain == null) continue;
                InteractionType t = chain.getType();
                if (t == InteractionType.Primary || t == InteractionType.Secondary) return true;
            }
        } catch (Exception e) {
            EldaniorLogger.error("AutoDodge: lecture des interactions", e);
        }
        return false;
    }

    /**
     * Dash a l'oppose de l'ennemi. f / r : position de l'ennemi dans le repere du regard (avant / droite).
     * Ennemi sur le cote : dash lateral pur. Devant ou derriere : dash arriere / avant, en diagonale d'un cote au hasard.
     * L'animation est toujours l'esquive gauche ou droite du jeu, du cote du deplacement lateral.
     */
    private static void dash(Ref<EntityStore> victim, Vector3d fwd, Vector3d right, double f, double r,
                             Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double main, side;
        if (Math.abs(r) > Math.abs(f)) {                                      // ennemi a gauche ou a droite
            main = 0;
            side = r > 0 ? -1 : 1;
        } else {                                                              // devant -> arriere, derriere -> avant
            main = f > 0 ? -1 : 1;
            side = ThreadLocalRandom.current().nextBoolean() ? DIAGONAL_SIDE : -DIAGONAL_SIDE;
        }
        Vector3d dir = new Vector3d(fwd).mul(main).add(new Vector3d(right).mul(side));
        dir.y = 0;
        if (dir.lengthSquared() < 1e-6) dir.set(right);
        dir.normalize().mul(DASH_SPEED);
        try {
            KnockbackComponent kb = new KnockbackComponent();
            kb.setVelocity(dir);
            kb.setVelocityType(ChangeVelocityType.Set);
            VelocityConfig vc = new VelocityConfig();                         // memes reglages que Dodge_Left/Right vanilla
            vc.setAirResistance(0.97f);
            vc.setAirResistanceMax(0.96f);
            vc.setGroundResistance(0.94f);
            vc.setGroundResistanceMax(0.82f);
            vc.setThreshold(5.0f);
            vc.setStyle(VelocityThresholdStyle.Exp);
            kb.setVelocityConfig(vc);
            cb.putComponent(victim, KnockbackComponent.getComponentType(), kb);
        } catch (Exception e) {
            EldaniorLogger.error("AutoDodge: dash", e);
        }
        // Retours visibles (2026-11-25, 2e version) : animation + trainee + particules a la 1re personne, son de roulade,
        // secousse laterale de la camera du joueur (sinon, a la 1re personne, le dash ressemble a une teleportation).
        // Plus d'invulnerabilite : le coup est deja annule, et l'attente de 5 s empeche les series.
        try {
            EffectsManager.applyEffect(victim, side > 0 ? FX_RIGHT : FX_LEFT, store);
            Vector3d pos = position(victim, store);
            int sfx = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex(SFX);
            if (pos != null && sfx >= 0)
                com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent3d(sfx,
                        com.hypixel.hytale.protocol.SoundCategory.SFX, pos, store);
            var camera = com.hypixel.hytale.server.core.asset.type.camera.CameraEffect.getAssetMap().getAsset(CAMERA);
            var player = store.getComponent(victim, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (camera != null && player != null) player.getPacketHandler().writeNoCache(camera.createCameraShakePacket(1.0f));
        } catch (Exception e) {
            EldaniorLogger.error("AutoDodge: effets", e);
        }
    }

    /** Une esquive fait progresser la maitrise des passifs d'Esquive actifs (x5 avec Diligence, comme les autres passifs). */
    private static void creditPassives(Ref<EntityStore> victim, PlayerLevelData data, CommandBuffer<EntityStore> cb) {
        int weight = data.hasActivePassive(PassiveSkill.ANGE_DILIGENCE) ? 5 : 1;
        boolean changed = false;
        for (PlayerLevelData.ActivePassive a : data.getActivePassiveEntries()) {
            if (a.skill().getLogic().getFlatStatBonus(StatConfig.DODGE_CHANCE, a.rank(), false) <= 0f) continue;
            for (int i = 0; i < weight; i++) data.addSkillProc(a.skill().name());
            changed = true;
        }
        if (changed) cb.putComponent(victim, EldaniorSystem.get().getPlayerLevelDataType(), data);
    }

    private static Vector3d position(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return null;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        return t != null ? new Vector3d(t.getPosition()) : null;
    }

    private static Vector3d forward(Ref<EntityStore> ref, Store<EntityStore> store) {
        HeadRotation head = store.getComponent(ref, HeadRotation.getComponentType());
        Vector3d d = head != null ? new Vector3d(head.getDirection()) : new Vector3d(0, 0, 1);
        d.y = 0;
        if (d.lengthSquared() < 1e-6) d.set(0, 0, 1);
        return d.normalize();
    }

    /** Oublie le joueur (deconnexion). */
    public static void forget(UUID uuid) { if (uuid != null) lastDodge.remove(uuid); }
}
