package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Capacites et effets visuels des familiers (package familiers), sur les outils des sorts de rune (SpellFx).
 * Talents du Fenrir : Hurlement glacial (adolescent et adulte) et Croc du Ragnarok (adulte).
 * Competence d'element (elementSkill) : une par element (familiers.Element, meme identifiant), de la rarete de l'element.
 * Les degats sont attribues au familier (causes elementaires : FamilierManager ne les remplace pas par la morsure) ; les PV
 * virtuels des monstres s'appliquent (MobDamageReductionSystem), comme pour les aventuriers.
 */
public final class FamilierSpells {

    private FamilierSpells() {}

    private static final String FX_SNOW = "Effect_Snow_Impact", FX_ICE = "Ability_Ice_Impact", FX_BURST = "Niflheim_Freeze_Burst";
    private static final String FX_BREAK = "Block_Break_Snow";
    private static final String SFX_HOWL = "SFX_Wolf_Alerted", SFX_SLAM = "SFX_Golem_Frost_Slam_Impact", SFX_ICE = "SFX_Ice_Break";
    private static final String SLOW = "Elda_Givre", FREEZE = "Elda_Gel";

    /** Monstres (hostiles aux joueurs) dans la sphere. */
    public static List<Ref<EntityStore>> monstersAround(Vector3d at, double radius, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(com.hypixel.hytale.server.core.util.TargetUtil.getAllEntitiesInSphere(at, radius, store)))
            if (CloneFx.isMonster(e, store)) out.add(e);
        return out;
    }

    public static boolean isMonster(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) { return CloneFx.isMonster(ref, store); }

    /**
     * Hurlement glacial : le familier hurle, les monstres a moins de radius blocs subissent des degats de glace et sont
     * ralentis (Elda_Givre). Renvoie le nombre de monstres touches.
     */
    public static int howl(Ref<EntityStore> pet, float damage, double radius, float scale, Store<EntityStore> store) {
        Vector3d at = SpellFx.position(pet, store);
        if (at == null) return 0;
        gesture(pet, "Howl", store);
        SpellFx.sound(SFX_HOWL, at, store);
        SpellFx.sound(SFX_SLAM, at, store);
        SpellFx.fx(FX_SNOW, new Vector3d(at).add(0, 0.6, 0), 2.2f * scale, store, store);
        SpellFx.fx(FX_ICE, new Vector3d(at).add(0, 0.3, 0), 1.4f * scale, store, store);
        int n = 0;
        for (Ref<EntityStore> e : monstersAround(at, radius, store)) {
            hit(pet, e, damage, store);
            try { EffectsManager.applyCustomEffect(e, SLOW, 4f, OverlapBehavior.OVERWRITE, store); }
            catch (Exception ex) { EldaniorLogger.error("[Familier] givre", ex); }
            Vector3d p = SpellFx.position(e, store);
            if (p != null) SpellFx.fx(FX_SNOW, new Vector3d(p).add(0, 1, 0), 1.0f, store, store);
            n++;
        }
        return n;
    }

    /** Croc du Ragnarok : morsure geante sur la cible, degats de glace et gel (Elda_Gel + immobilise) pendant freezeMs. */
    public static boolean fang(Ref<EntityStore> pet, Ref<EntityStore> target, float damage, long freezeMs, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(target, store);
        if (p == null || !CloneFx.isMonster(target, store)) return false;
        CloneFx.faceTowards(pet, p, store);
        hit(pet, target, damage, store);
        SpellFx.stun(target, FREEZE, freezeMs, store, store);
        SpellFx.fx(FX_ICE, new Vector3d(p).add(0, 1, 0), 2.0f, store, store);
        SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 0.5, 0), 1.2f, store, store);
        SpellFx.sound(SFX_ICE, p, store);
        SpellFx.sound(SFX_SLAM, p, store);
        SpellFx.shake(p, 10, "Impact_Light", store);
        return true;
    }


    // =================================================================================================================
    // Competences d'element
    // =================================================================================================================

    /** Forme de la competence : zone autour du familier, cible (+ eclaboussure), bond derriere la cible, attraction, cone. */
    private enum Shape { AREA, TARGET, BLINK, PULL, CONE }

    /**
     * mult : x degats du familier ; radius : zone (ou eclaboussure) ; effect/effectSec : effet pose ; fx1 sur chaque cible,
     * fx2 au centre ; heal : part des degats rendue (Tenebres : au familier, Lumiere : au maitre, en % de sa vie max).
     */
    private record Skill(Shape shape, float mult, double radius, String effect, float effectSec, boolean root, String fx1, String fx2,
                         String sfx, String cause, long cooldownMs, float heal) {}

    private static final java.util.Map<String, Skill> SKILLS = java.util.Map.ofEntries(
            java.util.Map.entry("EAU", new Skill(Shape.AREA, 1.2f, 5, "Elda_Trempe", 4f, false, "Muspelheim_Steam_Burst", "Effect_Snow_Impact",
                    "SFX_Ice_Item_Impact", "Water", 16_000, 0f)),
            java.util.Map.entry("FEU", new Skill(Shape.TARGET, 1.8f, 2.5, "Elda_Brulure", 4f, false, "Impact_Fire", "Explosion_Small",
                    "SFX_Staff_Flame_Fireball_Impact", "Fire", 14_000, 0f)),
            java.util.Map.entry("TERRE", new Skill(Shape.AREA, 1.3f, 5, "Elda_Racines", 1.5f, true, "Block_Break_Stone", "Gaia_Dust_Burst",
                    "SFX_Golem_Earth_Stomp_Impact", "Earth", 16_000, 0f)),
            java.util.Map.entry("AIR", new Skill(Shape.AREA, 1.1f, 5, "Elda_Bourrasque", 2f, false, "Fujin_Gust", "Fujin_Gust",
                    "SFX_Ability_Wind_Strike_Slash", "Wind", 14_000, 0f)),
            java.util.Map.entry("ECLAIR", new Skill(Shape.TARGET, 2.0f, 4, "Taranis_Foudroye", 0.8f, true, "Taranis_Bolt", "Taranis_Electric_Burst",
                    "SFX_Emit_Goblin_Electric_Fence_Zap", "Lightning", 16_000, 0f)),
            java.util.Map.entry("LUMIERE", new Skill(Shape.AREA, 1.4f, 6, "Elda_Aveuglement", 3f, false, "Impact_Sword_Charged", "Apollo_Heal_Pulse",
                    "SFX_Divine_Respawn", "Elemental", 20_000, 0.08f)),
            java.util.Map.entry("TENEBRES", new Skill(Shape.TARGET, 2.0f, 2.5, "Elda_Corruption", 5f, false, "Nyx_Drain_Burst", "Smoke_Black",
                    "SFX_Bear_Voidtaken_Growl", "Elemental", 16_000, 0.3f)),
            java.util.Map.entry("NATURE", new Skill(Shape.AREA, 1.2f, 5, "Yggdrasil_Ronces", 3f, true, "Block_Break_Leaves", "Gaia_Dust_Burst",
                    "SFX_Bramble_Break", "Poison", 16_000, 0f)),
            java.util.Map.entry("SPATIAL", new Skill(Shape.BLINK, 2.6f, 3, "Gravite_Ecrasement", 1.5f, true, "Gravite_Fissure", "Eldanior_Pas_Celeste",
                    "SFX_Portal_Neutral", "Elemental", 18_000, 0f)),
            java.util.Map.entry("TEMPS", new Skill(Shape.AREA, 1.5f, 7, "Chronos_Fige", 2f, true, "Chronos_Shatter", "Chronos_Burst",
                    "SFX_Crystal_Break", "Elemental", 22_000, 0f)),
            java.util.Map.entry("ESPACE", new Skill(Shape.PULL, 1.6f, 8, "Chaos_Attraction", 1f, false, "Chaos_Burst", "Chaos_Shockwave",
                    "SFX_Portal_Void", "Elemental", 20_000, 0f)),
            java.util.Map.entry("DRAGONIC", new Skill(Shape.CONE, 2.2f, 7, "Elda_Brulure", 5f, false, "Impact_Fire", "Explosion_Medium",
                    "SFX_Staff_Flame_Fireball_Launch", "Fire", 20_000, 0f)),
            // ----- competences de bete (2026-11-13 : toutes especes, tirees a l'evolution adulte) -----
            java.util.Map.entry("MORSURE", new Skill(Shape.TARGET, 2.4f, 0, "Elda_Faiblesse", 4f, false, "Impact_Sword_Charged", "Impact_Sword_Charged",
                    "SFX_Wolf_Alerted", "Slashing", 12_000, 0f)),
            java.util.Map.entry("BOND", new Skill(Shape.BLINK, 2.0f, 2, null, 0f, false, "Impact_Sword_Charged", "Fujin_Gust",
                    "SFX_Ability_Wind_Strike_Slash", "Slashing", 14_000, 0f)),
            java.util.Map.entry("RUGISSEMENT", new Skill(Shape.AREA, 0.9f, 6, "Elda_Terreur", 3f, false, "Impact_Sword_Charged", "Gaia_Dust_Burst",
                    "SFX_Bear_Voidtaken_Growl", "Bludgeoning", 18_000, 0f)),
            java.util.Map.entry("COUP_DE_PATTE", new Skill(Shape.CONE, 1.8f, 4.5, "Elda_Faiblesse", 3f, false, "Impact_Sword_Charged", "Gaia_Dust_Burst",
                    "SFX_Golem_Earth_Stomp_Impact", "Bludgeoning", 14_000, 0f)),
            java.util.Map.entry("HURLEMENT_MEUTE", new Skill(Shape.AREA, 1.1f, 6, "Elda_Faiblesse", 4f, false, "Fujin_Gust", "Fujin_Gust",
                    "SFX_Wolf_Alerted", "Slashing", 16_000, 0f)),
            java.util.Map.entry("PIQUE", new Skill(Shape.BLINK, 2.2f, 2, "Elda_Faiblesse", 3f, false, "Impact_Sword_Charged", "Fujin_Gust",
                    "SFX_Ability_Wind_Strike_Slash", "Slashing", 13_000, 0f)),
            java.util.Map.entry("CRI", new Skill(Shape.AREA, 0.8f, 6, "Elda_Terreur", 3f, false, "Fujin_Gust", "Fujin_Gust",
                    "SFX_Ability_Wind_Strike_Slash", "Wind", 17_000, 0f)),
            java.util.Map.entry("RUADE", new Skill(Shape.CONE, 1.7f, 4, null, 0f, false, "Impact_Sword_Charged", "Gaia_Dust_Burst",
                    "SFX_Golem_Earth_Stomp_Impact", "Bludgeoning", 13_000, 0f)));

    /** Recharge d'une competence de familier (0 : inconnue). */
    public static long cooldown(String element) {
        if ("GLACE".equals(element)) return 18_000;
        if ("CROC".equals(element)) return 25_000;
        Skill s = element != null ? SKILLS.get(element) : null;
        return s != null ? s.cooldownMs() : 0;
    }

    /**
     * Lance la competence du familier (familiers.Competence : de bete, du Fenrir ou d'element) sur sa cible (monstre). owner :
     * soigne par la Lumiere. Renvoie le nombre de monstres touches (0 : rien lance, la recharge ne part pas).
     */
    public static int skill(String element, Ref<EntityStore> pet, Ref<EntityStore> target, Ref<EntityStore> owner, float petDamage,
                            Store<EntityStore> store) {
        if ("GLACE".equals(element)) return howl(pet, petDamage * 0.8f, 7, 1.3f, store);   // Fenrir : Hurlement glacial
        if ("CROC".equals(element)) {                                                       // Fenrir : Croc du Ragnarok
            Vector3d a = SpellFx.position(pet, store), b = SpellFx.position(target, store);
            if (a == null || b == null || a.distance(b) > 4.5) return 0;
            return fang(pet, target, petDamage * 2.5f, 2_000, store) ? 1 : 0;
        }
        Skill s = element != null ? SKILLS.get(element) : null;
        Vector3d at = SpellFx.position(pet, store), tp = SpellFx.position(target, store);
        if (s == null || at == null || tp == null || !CloneFx.isMonster(target, store)) return 0;
        float dmg = petDamage * s.mult();
        List<Ref<EntityStore>> hit = new ArrayList<>();
        Vector3d center = at;
        switch (s.shape()) {
            case AREA -> hit.addAll(monstersAround(at, s.radius(), store));
            case TARGET -> {
                hit.add(target);
                for (Ref<EntityStore> e : monstersAround(tp, s.radius(), store)) if (!e.equals(target)) hit.add(e);
                center = tp;
            }
            case BLINK -> {                                                         // bond dans le dos de la cible
                Vector3d dir = new Vector3d(tp).sub(at);
                dir.y = 0;
                if (dir.lengthSquared() > 1e-4) dir.normalize();
                Vector3d land = SpellFx.ground(SpellFx.world(store), new Vector3d(tp).add(dir.x * 1.6, 0.2, dir.z * 1.6));
                SpellFx.fx(s.fx2(), new Vector3d(at).add(0, 0.8, 0), 1.2f, store, store);
                if (land != null) teleport(pet, land, SpellFx.facing(new Vector3d(dir).negate(), false), store);
                hit.add(target);
                for (Ref<EntityStore> e : monstersAround(tp, s.radius(), store)) if (!e.equals(target)) hit.add(e);
                center = tp;
            }
            case PULL -> {
                for (Ref<EntityStore> e : monstersAround(at, s.radius(), store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    Vector3d pull = new Vector3d(at).sub(p);
                    double d = pull.length();
                    if (d > 1.5) push(e, pull.normalize().mul(Math.min(14, d * 2.2)).add(0, 3, 0), store);
                    hit.add(e);
                }
            }
            case CONE -> {
                Vector3d fwd = new Vector3d(tp).sub(at);
                fwd.y = 0;
                if (fwd.lengthSquared() > 1e-4) fwd.normalize();
                CloneFx.faceTowards(pet, tp, store);
                for (Ref<EntityStore> e : monstersAround(at, s.radius(), store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    Vector3d to = new Vector3d(p).sub(at);
                    to.y = 0;
                    double d = to.length();
                    if (d < 1.2 || to.normalize().dot(fwd) > 0.6) hit.add(e);
                }
                for (int k = 1; k <= 3; k++)
                    SpellFx.fx(s.fx2(), new Vector3d(at).add(fwd.x * 2.2 * k, 1.0, fwd.z * 2.2 * k), 0.8f + 0.2f * k, store, store);
            }
        }
        if (hit.isEmpty()) return 0;
        if (s.shape() != Shape.CONE) SpellFx.fx(s.fx2(), new Vector3d(center).add(0, 0.6, 0), s.shape() == Shape.AREA || s.shape() == Shape.PULL ? 1.8f : 1.3f, store, store);
        SpellFx.sound(s.sfx(), center, store);
        float dealt = 0;
        for (Ref<EntityStore> e : hit) {
            Vector3d p = SpellFx.position(e, store);
            hit(pet, e, dmg, s.cause(), store);
            dealt += dmg;
            if (s.effect() != null) {
                try { EffectsManager.applyCustomEffect(e, s.effect(), s.effectSec(), OverlapBehavior.OVERWRITE, store); }
                catch (Exception ex) { EldaniorLogger.error("[Familier] effet " + s.effect(), ex); }
            }
            if (s.root()) SpellFx.lockInPlace(e, (long) (s.effectSec() * 1000), store);
            if (p != null) SpellFx.fx(s.fx1(), new Vector3d(p).add(0, 1, 0), 1.0f, store, store);
        }
        if (s.heal() > 0) {
            if ("TENEBRES".equals(element)) SpellFx.heal(pet, dealt * s.heal(), store);
            else if (owner != null && owner.isValid()) SpellFx.restore(owner, com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), s.heal(), store);
        }
        return hit.size();
    }

    /** Porte de Niflheim (rune divine) ouverte sur le familier au nom de son maitre : Fenrir au niveau maximal. */
    public static void niflheimGate(Ref<EntityStore> owner, Vector3d at, Store<EntityStore> store) {
        NiflheimGate.castAt(owner, at, store);
    }

    /** Eclosion, croissance : eclats de givre et etincelles. */
    public static void bloom(Vector3d at, float scale, Store<EntityStore> store) {
        SpellFx.fx(FX_SNOW, new Vector3d(at).add(0, 0.8, 0), 2.0f * scale, store, store);
        SpellFx.fx(FX_ICE, new Vector3d(at).add(0, 0.4, 0), 1.2f * scale, store, store);
        SpellFx.sound(SFX_ICE, at, store);
    }

    /** Mort du familier : il se dissout en neige ; son oeuf vole en eclats. */
    public static void shatter(Vector3d at, ComponentAccessor<EntityStore> accessor, Store<EntityStore> store) {
        SpellFx.fx(FX_SNOW, new Vector3d(at).add(0, 0.8, 0), 2.5f, store, accessor);
        SpellFx.fx(FX_BREAK, new Vector3d(at).add(0, 0.5, 0), 2.0f, store, accessor);
        SpellFx.fx(FX_ICE, new Vector3d(at).add(0, 0.5, 0), 1.6f, store, accessor);
        SpellFx.sound(SFX_ICE, at, accessor);
    }

    /** Geste d'une animation du modele (Howl, Greet...) ; sans effet si le modele ne l'a pas. */
    public static void gesture(Ref<EntityStore> ref, String animation, ComponentAccessor<EntityStore> store) {
        try {
            AnimationUtils.playAnimation(ref, AnimationSlot.Action, animation, true, store);
        } catch (Exception e) {
            EldaniorLogger.warn("[Familier] geste " + animation + " : " + e.getMessage());
        }
    }

    /** Teleportation hors du tick d'un systeme (sur le Store, sans CommandBuffer). */
    private static void teleport(Ref<EntityStore> ref, Vector3d pos, com.hypixel.hytale.math.vector.Rotation3f rot, Store<EntityStore> store) {
        try {
            store.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(),
                    new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(new Vector3d(pos), rot));
        } catch (Exception e) {
            EldaniorLogger.error("[Familier] bond", e);
        }
    }

    /** Poussee hors du tick d'un systeme (comme SpellFx.push). */
    private static void push(Ref<EntityStore> target, Vector3d velocity, Store<EntityStore> store) {
        try {
            var knockback = new com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent();
            knockback.setVelocity(velocity);
            knockback.setVelocityType(com.hypixel.hytale.protocol.ChangeVelocityType.Set);
            var cfg = new com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig();
            cfg.setGroundResistanceMax(0f);
            cfg.setThreshold(0.25f);
            knockback.setVelocityConfig(cfg);
            store.putComponent(target, com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent.getComponentType(), knockback);
        } catch (Exception e) {
            EldaniorLogger.error("[Familier] attraction", e);
        }
    }

    private static void hit(Ref<EntityStore> pet, Ref<EntityStore> target, float amount, Store<EntityStore> store) {
        hit(pet, target, amount, "Ice", store);
    }

    private static void hit(Ref<EntityStore> pet, Ref<EntityStore> target, float amount, String cause, Store<EntityStore> store) {
        try {
            Damage d = new Damage(new Damage.EntitySource(pet), DamageCause.getAssetMap().getIndex(cause), amount);
            DamageSystems.executeDamage(target, store, d);
        } catch (Exception e) {
            EldaniorLogger.error("[Familier] degats", e);
        }
    }
}
