package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de famille du Clan Luo : Execution Celeste (idee de l'utilisateur, developpee le 2026-11-11). Il faut tenir un katana
 * du clan. Trois gestes selon la classe de la voie martiale (Apprenti hors de la voie) :
 *
 *  Apprenti, Disciple (iai) : posture iai accroupie, la main sur la poignee (IaiCharging), puis degaine eclair de gauche a
 *    droite arrete net devant soi (IaiCourt) : coupe 3,5 / 4,5 blocs devant, une / deux epees de Ki holographiques sur la cible.
 *  Expert, Maitre, Grand Maitre (ruee) : la cible visee (8 / 10 / 12 blocs, ligne droite sans mur) est marquee ; pas leger qui
 *    la traverse (intouchable pendant la ruee) et s'arrete 3 blocs derriere elle, tourne vers son dos ; des images remanentes
 *    dorees restent sur le trajet et la tranchent chacune ; puis 3 / 4 / 6 epees Ki_Epee_Holo l'encerclent, figees, et
 *    convergent quand le lanceur rengaine (Rengainer) : coupe finale.
 *  Saint, Maitre Supreme, Dieu Martial (zone) : le katana est presente au fourreau devant soi, la main droite le tire de
 *    quelques centimetres (Koiguchi) ; les ennemis de la zone (8 / 11 / 15 blocs ; 5, 8, tous) sont tranches l'un apres
 *    l'autre par des images remanentes reliees par des eclairs ; des epees de Ki les encerclent ; la lame claque dans le
 *    fourreau (KoiguchiFin) : toutes les epees tombent ensemble, foudre sur chaque cible. Dieu Martial : son ame de Ki se
 *    dresse derriere lui pendant la technique.
 *
 * Execution : un ennemi sous le seuil de sa classe (10 a 25 % de vie) apres la coupe finale est decapite. Joueurs : degats
 * seulement. Gros monstres (2 000 PV et plus) : jamais executes, degats x2 a la place. Degats x la puissance du katana.
 * Fiche de conception : scripts/runes/specs/Rune_ExecutionCeleste.json
 */
public final class ExecutionCeleste implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    static final String[] NAMES = {"Exécution Céleste — Iai", "Exécution Céleste — Iai du Disciple", "Exécution Céleste — Pas de l'Expert",
            "Exécution Céleste — Pas du Maître", "Exécution Céleste — Pas du Grand Maître", "Exécution Céleste — Koiguchi du Saint",
            "Exécution Céleste — Koiguchi Suprême", "Exécution Céleste — Jugement du Dieu Martial"};
    static final float[] DAMAGE = {40, 55, 80, 100, 125, 160, 200, 260};
    static final double[] REACH = {3.5, 4.5, 8, 10, 12, 8, 11, 15};
    static final int[] SWORDS = {1, 2, 3, 4, 6, 3, 3, 4};
    static final int[] IMAGES = {0, 0, 3, 4, 5, 1, 2, 2};
    static final int[] ZONE_TARGETS = {0, 0, 0, 0, 0, 5, 8, 64};
    static final float[] EXECUTE = {0.10f, 0.12f, 0.15f, 0.15f, 0.18f, 0.20f, 0.22f, 0.25f};
    static final float BIG_HP = 2_000f;
    static final double BEHIND = 3.0;

    // ===== Assets =====
    static final String SET_DRAWN = "Tenryumaru_Degainee", SET_SHEATHED = "Tenryumaru_Fourreau";
    static final String GHOST_CARRIER = "Projectile_Config_Chronos_Ghost", GHOST_EFFECT = "Execution_Remanence", SOUL_EFFECT = "Execution_Ame";
    static final String FX_SLASH = "Impact_Sword_Charged", FX_FLASH = "Gilgamesh_Sword_Burst", FX_BOLT = "Taranis_Bolt",
            FX_SPARK = "Taranis_Electric_Burst";

    @Override public String id() { return "ExecutionCeleste"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        LuoKatanas.Katana k = Percee.held(caster, store);
        if (k == null) {
            MartialKi.notify(caster, store, "<color:gray>L'Exécution Céleste se porte un katana du clan en main.</color>", NotificationStyle.Warning);
            return null;
        }
        int t = BokkenSignature.tier(caster, store);
        float power = Percee.power(k);
        MartialKi.notify(caster, store, "<color:gold>" + NAMES[t - 1] + "</color> <color:gray>— " + VoieMartiale.className(t) + "</color>",
                NotificationStyle.Default);
        TenryumaruSystem.drawn(caster);                                          // pas de rengainage automatique pendant le geste
        SpellFx.protectFromFall(caster, 4_000, store);
        return new Cut(caster, k, t, power, System.currentTimeMillis(), store, cb);
    }

    // ===================================================================================================================

    /** Image remanente : porteur de projectile habille du lanceur (comme les Echos du Passe), fige dans une pose de coupe. */
    static final class Ghost {
        final Ref<EntityStore> ref;
        final Vector3d pos;
        final Rotation3f rot;
        final long until;
        final String effect;
        boolean dressed;

        Ghost(Ref<EntityStore> ref, Vector3d pos, Rotation3f rot, long until, String effect) {
            this.ref = ref; this.pos = pos; this.rot = rot; this.until = until; this.effect = effect;
        }
    }

    /** Epees de Ki holographiques autour d'une cible : figees (radius) puis convergentes. */
    record Ring(Ref<EntityStore> target, int count, double phase) {}

    static final class Cut implements Instance {
        final Ref<EntityStore> caster;
        final LuoKatanas.Katana katana;
        final int tier;
        final float power;
        final long startAt;
        final com.hypixel.hytale.server.core.asset.type.model.config.Model model;
        final com.hypixel.hytale.protocol.PlayerSkin skin;
        final List<Ghost> ghosts = new ArrayList<>();
        final List<Ring> rings = new ArrayList<>();
        final List<Ref<EntityStore>> victims = new ArrayList<>();
        int phase, step;
        long nextAt, intangibleUntil, ringsFrom, ringsConverge = -1, checkAt = -1;
        Vector3d dir, behind;
        Ref<EntityStore> target;
        Vector3d lastBolt;
        boolean sheatheAtEnd;

        Cut(Ref<EntityStore> caster, LuoKatanas.Katana katana, int tier, float power, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            this.caster = caster; this.katana = katana; this.tier = tier; this.power = power; this.startAt = now;
            ModelComponent mc = s.getComponent(caster, ModelComponent.getComponentType());
            PlayerSkinComponent sk = s.getComponent(caster, PlayerSkinComponent.getComponentType());
            this.model = mc != null ? mc.getModel() : null;
            this.skin = sk != null ? sk.getPlayerSkin() : null;
            Vector3d feet = SpellFx.position(caster, s);
            if (tier <= 2) {                                                     // posture iai accroupie
                play(s, "IaiCharging");
                SpellFx.sound("SFX_Sword_T2_Lunge_Charge_Local", feet, b);
                nextAt = now + (tier == 1 ? 650 : 520);
            } else if (tier <= 5) {                                              // posture iai, la cible est marquee
                play(s, "IaiCharging");
                dir = SpellFx.lookDirection(caster, s).normalize();
                target = SpellFx.targetEntity(caster, s, REACH[tier - 1]);
                Vector3d tp = target != null ? SpellFx.position(target, s) : null;
                if (tp != null) SpellFx.fx("Ki_Orbe_T" + tier, new Vector3d(tp).add(0, 2.3, 0), 0.8f, s, b);
                SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(feet).add(0, 0.05, 0), 1.2f, s, b);
                SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", feet, b);
                nextAt = now + 380;
            } else {                                                             // koiguchi : le katana presente au fourreau
                drawBlade(s);
                play(s, "Koiguchi");
                SpellFx.fx("Ki_Colonne_T" + tier, new Vector3d(feet).add(0, 0.05, 0), 1.2f, s, b);
                SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(feet).add(0, 0.05, 0), 1.6f + (tier - 6) * 0.6f, s, b);
                SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", feet, b);
                if (tier >= 8) soul(feet, s, b);
                nextAt = now + 360;
            }
        }

        float dmg() { return DAMAGE[tier - 1] * power; }

        private void play(Store<EntityStore> s, String anim) {
            AnimationUtils.playAnimation(caster, AnimationSlot.Action, sheathed(s) ? SET_SHEATHED : SET_DRAWN, anim, true, s);
        }

        private boolean sheathed(Store<EntityStore> s) {
            Player p = s.getComponent(caster, Player.getComponentType());
            var it = p == null || p.getInventory() == null ? null : p.getInventory().getActiveHotbarItem();
            return it != null && LuoKatanas.isSheathed(it.getItemId());
        }

        /** La lame passe au clair (objet degaine) : les animations de coupe et de koiguchi tiennent la lame en main droite. */
        private void drawBlade(Store<EntityStore> s) {
            if (sheathed(s) && Tenryumaru.swapActive(caster, katana.id(), katana.drawn(), s)) {
                TenryumaruSystem.drawn(caster);
                sheatheAtEnd = true;
            }
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return System.currentTimeMillis() < intangibleUntil ? 0f : amount;    // intouchable pendant la ruee
        }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            tickGhosts(now, s, b);
            tickRings(now, s, b);
            if (checkAt > 0 && now >= checkAt) { checkAt = -1; executions(s, b); }
            if (now < nextAt) return false;
            Vector3d feet = SpellFx.position(c, s);
            if (feet == null) return true;
            if (tier <= 2) return iai(now, feet, s, b);
            if (tier <= 5) return dash(now, feet, s, b);
            return zone(now, feet, s, b);
        }

        // ----- Apprenti, Disciple -----
        private boolean iai(long now, Vector3d feet, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            if (phase == 0) {
                drawBlade(s);
                play(s, "IaiCourt");
                Vector3d f = flat(SpellFx.lookDirection(caster, s));
                SpellFx.push(caster, new Vector3d(f).mul(3), false, b);
                SpellFx.sound("SFX_Ability_Wind_Strike_Slash", feet, b);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, feet, REACH[tier - 1] + 0.5, s)) {
                    Vector3d p = SpellFx.position(e, s);
                    if (p == null) continue;
                    Vector3d to = new Vector3d(p.x - feet.x, 0, p.z - feet.z);
                    if (to.lengthSquared() > 1e-4 && to.normalize().angle(f) > Math.toRadians(40)) continue;
                    strike(e, dmg(), new Vector3d(f).mul(3).add(0, 1, 0), s, b);
                    SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), (float) Math.toDegrees(Math.atan2(f.x, f.z)), 0f, 1.1f, s, b);
                    rings.add(new Ring(e, SWORDS[tier - 1], ThreadLocalRandom.current().nextDouble(Math.PI * 2)));
                    victims.add(e);
                }
                ringsFrom = now;
                ringsConverge = now + 280;
                checkAt = now + 420;
                phase = 1;
                nextAt = now + 700;
                return false;
            }
            return ghosts.isEmpty() && checkAt < 0;
        }

        // ----- Expert, Maitre, Grand Maitre -----
        private boolean dash(long now, Vector3d feet, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            switch (phase) {
                case 0 -> {                                                          // depart de la ruee
                    drawBlade(s);
                    play(s, "IaiCharged");
                    Vector3d eye = new Vector3d(feet).add(0, 1.5, 0);
                    Vector3d tp = target != null && target.isValid() ? SpellFx.position(target, s) : null;
                    Vector3d goal;
                    if (tp != null) {
                        dir = flat(new Vector3d(tp).sub(feet));
                        goal = new Vector3d(tp).add(new Vector3d(dir).mul(BEHIND));
                    } else {
                        dir = flat(dir != null ? dir : SpellFx.lookDirection(caster, s));
                        goal = new Vector3d(feet).add(new Vector3d(dir).mul(REACH[tier - 1]));
                    }
                    double len = new Vector3d(goal).sub(feet).length();
                    double free = Percee.reach(SpellFx.world(s), eye, new Vector3d(dir), len, 0);   // un mur arrete la ruee
                    behind = new Vector3d(feet).add(new Vector3d(dir).mul(Math.max(1.0, Math.min(len, free - 0.6))));
                    behind.y = goal.y;
                    intangibleUntil = now + 450;
                    SpellFx.effect(caster, "Pas_Leger_Sillage_" + tier, b);
                    SpellFx.push(caster, new Vector3d(dir).mul(Math.max(10, len / 0.22)).add(0, 0.4, 0), false, b);
                    SpellFx.sound("SFX_Light_Melee_T2_Lunge", feet, b);
                    // images remanentes sur le trajet, chacune tranche la cible
                    int n = IMAGES[tier - 1];
                    Rotation3f rot = SpellFx.facing(dir, false);
                    for (int i = 0; i < n; i++) {
                        Vector3d at = new Vector3d(feet).lerp(behind, (i + 0.5) / n);
                        ghost(at, rot, now + 900, GHOST_EFFECT, s, b);
                    }
                    // ennemis sur la ligne : une coupe plus faible
                    for (Ref<EntityStore> e : Percee.line(caster, eye, dir, len, 1.2, s)) {
                        if (e.equals(target)) continue;
                        strike(e, dmg() * 0.35f, new Vector3d(dir).mul(2).add(0, 1.5, 0), s, b);
                        Vector3d p = SpellFx.position(e, s);
                        if (p != null) SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 0.9f, s, b);
                    }
                    phase = 1;
                    nextAt = now + 230;
                    return false;
                }
                case 1 -> {                                                          // arrivee derriere la cible, tourne vers elle
                    SpellFx.push(caster, new Vector3d(), false, b);
                    SpellFx.teleport(caster, behind, SpellFx.facing(new Vector3d(dir).mul(-1), false), s, b);
                    if (target != null && target.isValid()) {
                        Vector3d tp = SpellFx.position(target, s);
                        if (tp != null) {
                            for (int i = 0; i < IMAGES[tier - 1]; i++)               // chaque image a tranche au passage
                                SpellFx.fx(FX_SLASH, new Vector3d(tp).add(ThreadLocalRandom.current().nextDouble(-0.4, 0.4), 1.0 + i * 0.15, 0), 1.0f, s, b);
                            strike(target, dmg() * 0.4f, new Vector3d(dir).mul(-1).add(0, 0.5, 0), s, b);
                            rings.add(new Ring(target, SWORDS[tier - 1], ThreadLocalRandom.current().nextDouble(Math.PI * 2)));
                            victims.add(target);
                            SpellFx.stun(target, "Taranis_Foudroye", 700, s, b);    // « le temps s'arrete »
                        }
                    }
                    ringsFrom = now;
                    phase = 2;
                    nextAt = now + 420;
                    return false;
                }
                case 2 -> {                                                          // rengainage : les epees tombent
                    TenryumaruSystem.sheatheNow(caster, s);
                    sheatheAtEnd = false;
                    ringsConverge = now;
                    phase = 3;
                    nextAt = now + 180;
                    return false;
                }
                case 3 -> {
                    finale(s, b, false);
                    phase = 4;
                    nextAt = now + 300;
                    return false;
                }
                default -> { return ghosts.isEmpty() && checkAt < 0; }
            }
        }

        // ----- Saint, Maitre Supreme, Dieu Martial -----
        private boolean zone(long now, Vector3d feet, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            switch (phase) {
                case 0 -> {                                                          // la lame sort de quelques centimetres
                    SpellFx.fx(FX_FLASH, new Vector3d(feet).add(0, 1.3, 0), 1.0f, s, b);
                    SpellFx.sound("SFX_Ability_Wind_Strike_Slash", feet, b);
                    List<Ref<EntityStore>> all = new ArrayList<>(SpellFx.enemiesAround(caster, feet, REACH[tier - 1], s));
                    all.sort(Comparator.comparingDouble(e -> { Vector3d p = SpellFx.position(e, s); return p == null ? 1e9 : p.distanceSquared(feet); }));
                    for (int i = 0; i < all.size() && i < ZONE_TARGETS[tier - 1]; i++) victims.add(all.get(i));
                    lastBolt = new Vector3d(feet).add(0, 1.2, 0);
                    phase = 1;
                    step = 0;
                    nextAt = now + 60;
                    return false;
                }
                case 1 -> {                                                          // une cible apres l'autre
                    if (step < victims.size()) {
                        Ref<EntityStore> e = victims.get(step++);
                        Vector3d p = e.isValid() ? SpellFx.position(e, s) : null;
                        if (p != null) {
                            var r = ThreadLocalRandom.current();
                            for (int i = 0; i < IMAGES[tier - 1]; i++) {
                                double a = r.nextDouble(Math.PI * 2);
                                Vector3d at = new Vector3d(p).add(Math.cos(a) * 1.8, 0, Math.sin(a) * 1.8);
                                ghost(at, SpellFx.facing(new Vector3d(p).sub(at), false), now + 1_100, GHOST_EFFECT, s, b);
                                Vector3d chest = new Vector3d(at).add(0, 1.2, 0);
                                SpellFx.arc(FX_SPARK, lastBolt, chest, 0.7f, s, b);  // eclair d'une image a l'autre
                                lastBolt = chest;
                            }
                            SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), (float) r.nextDouble(0, 360), 0f, 1.2f, s, b);
                            strike(e, dmg() * 0.3f, new Vector3d(0, 0.6, 0), s, b);
                            SpellFx.stun(e, "Taranis_Foudroye", 900, s, b);
                            rings.add(new Ring(e, SWORDS[tier - 1], r.nextDouble(Math.PI * 2)));
                        }
                        nextAt = now + (tier >= 8 ? 45 : 70);
                        return false;
                    }
                    ringsFrom = now;
                    phase = 2;
                    nextAt = now + 380;                                              // les epees figees, le monde retient son souffle
                    return false;
                }
                case 2 -> {                                                          // la lame claque dans le fourreau
                    play(s, "KoiguchiFin");
                    ringsConverge = now;
                    phase = 3;
                    nextAt = now + 160;
                    return false;
                }
                case 3 -> {
                    finale(s, b, true);
                    SpellFx.sound2dNear("SFX_Global_Weather_Thunder", feet, 40, s);
                    SpellFx.shake(feet, 25, "Impact_Strong", s);
                    phase = 4;
                    nextAt = now + 450;
                    return false;
                }
                case 4 -> {                                                          // fin du geste : katana rengaine
                    if (sheatheAtEnd || !sheathed(s)) {
                        Tenryumaru.swapActive(caster, katana.drawn(), katana.id(), s);
                        sheatheAtEnd = false;
                    }
                    phase = 5;
                    return false;
                }
                default -> { return ghosts.isEmpty() && checkAt < 0; }
            }
        }

        /** Coupe finale : les epees de Ki tombent, coup final sur chaque victime, verification de l'execution juste apres. */
        private void finale(Store<EntityStore> s, CommandBuffer<EntityStore> b, boolean bolts) {
            for (Ref<EntityStore> e : victims) {
                if (!e.isValid()) continue;
                Vector3d p = SpellFx.position(e, s);
                if (p == null) continue;
                SpellFx.fx(FX_FLASH, new Vector3d(p).add(0, 1.1, 0), 1.2f, s, b);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 45f, 0f, 1.6f, s, b);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), -45f, 0f, 1.6f, s, b);
                if (bolts) SpellFx.fx(FX_BOLT, p, 1.0f, s, b);
                strike(e, dmg() * (tier >= 6 ? 0.7f : 0.6f), new Vector3d(0, 1.5, 0), s, b);
            }
            SpellFx.sound("SFX_Longsword_Special_Impact", SpellFx.position(caster, s), b);
            checkAt = System.currentTimeMillis() + 120;
        }

        /** Coup d'execution (degats x2 sur les gros monstres, jamais d'execution sur eux). */
        private void strike(Ref<EntityStore> e, float amount, Vector3d kb, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            boolean big = s.getComponent(e, Player.getComponentType()) == null && CloneFx.maxHealth(e, s) >= BIG_HP;
            SpellFx.damage(caster, e, "Physical", big ? amount * 2 : amount, kb, "Impact_Strong", b);
        }

        /** Decapitation : un ennemi (ni joueur, ni gros monstre) sous le seuil de la classe apres la coupe finale. */
        private void executions(Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            int health = DefaultEntityStatTypes.getHealth();
            int count = 0;
            for (Ref<EntityStore> e : victims) {
                if (!e.isValid() || RuneSpellRuntime.isDying(e) || s.getComponent(e, Player.getComponentType()) != null) continue;
                float max = CloneFx.maxHealth(e, s), hp = MartialKi.stat(e, health, s);
                if (max <= 0 || max >= BIG_HP || hp <= 0 || hp > max * EXECUTE[tier - 1]) continue;
                Vector3d p = SpellFx.position(e, s);
                if (p != null) {
                    SpellFx.fx(FX_FLASH, new Vector3d(p).add(0, 1.7, 0), 1.6f, s, b);
                    SpellFx.fx("Ki_Colonne_T" + tier, p, 0.8f, s, b);
                }
                SpellFx.damage(caster, e, "Physical", max * 1_000f, new Vector3d(0, 2, 0), "Impact_Strong", b);
                count++;
            }
            if (count > 0) MartialKi.notify(caster, s, "<color:gold>Exécution !</color>" + (count > 1 ? " <color:gray>×" + count + "</color>" : ""),
                    NotificationStyle.Success);
        }

        private void ghost(Vector3d at, Rotation3f rot, long until, String effect, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            Ref<EntityStore> ref = SpellFx.spawnModel(caster, GHOST_CARRIER, at, new Vector3d(0, 0, 1), b);
            if (ref == null) return;
            SpellFx.notSaved(ref, s, b);
            ghosts.add(new Ghost(ref, new Vector3d(at), rot, until, effect));
        }

        /** Dieu Martial : son ame de Ki se dresse derriere lui pendant la technique. */
        private void soul(Vector3d feet, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            Vector3d back = flat(SpellFx.lookDirection(caster, s)).mul(-1.4);
            Vector3d at = new Vector3d(feet).add(back).add(0, 0.4, 0);
            ghost(at, SpellFx.facing(new Vector3d(back).mul(-1), false), System.currentTimeMillis() + 2_600, SOUL_EFFECT, s, b);
            SpellFx.effect(caster, "Pas_Leger_Sillage_8", b);
        }

        private void tickGhosts(long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            for (var it = ghosts.iterator(); it.hasNext(); ) {
                Ghost g = it.next();
                if (now >= g.until) { SpellFx.remove(g.ref, b); it.remove(); continue; }
                if (!g.ref.isValid()) continue;                                      // valide au tick suivant
                if (!g.dressed) {
                    g.dressed = true;
                    if (model != null) b.putComponent(g.ref, ModelComponent.getComponentType(), new ModelComponent(model));
                    if (skin != null) b.putComponent(g.ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
                    b.putComponent(g.ref, MovementStatesComponent.getComponentType(), new MovementStatesComponent());
                    SpellFx.effect(g.ref, g.effect, b);
                    AnimationUtils.playAnimation(g.ref, AnimationSlot.Action, SET_DRAWN,
                            SOUL_EFFECT.equals(g.effect) ? "Koiguchi" : "IaiCharged", true, s);
                }
                SpellFx.hold(g.ref, g.pos, g.rot, s);
            }
        }

        /** Epees de Ki : figees en cercle autour de la cible, puis elles convergent (re-emises a chaque passage, vie 0,1 s). */
        private void tickRings(long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            if (rings.isEmpty() || now < ringsFrom) return;
            double k = 1.0;
            if (ringsConverge > 0 && now >= ringsConverge) {
                k = 1.0 - Math.min(1.0, (now - ringsConverge) / 160.0);
                if (k <= 0) { rings.clear(); return; }
            }
            String fx = "Ki_Epee_Holo_T" + Math.clamp(tier, 1, 8);
            for (Ring r : rings) {
                Vector3d p = r.target().isValid() ? SpellFx.position(r.target(), s) : null;
                if (p == null) continue;
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                double radius = 0.4 + 1.6 * k, lift = 0.9 * k;
                for (int i = 0; i < r.count(); i++) {
                    double a = r.phase() + i * Math.PI * 2 / r.count();
                    Vector3d at = new Vector3d(chest).add(Math.cos(a) * radius, lift, Math.sin(a) * radius);
                    Vector3d to = new Vector3d(chest).sub(at);
                    float yaw = (float) Math.toDegrees(Math.atan2(to.x, to.z));
                    float pitch = (float) Math.toDegrees(Math.atan2(-to.y, Math.sqrt(to.x * to.x + to.z * to.z)));
                    SpellFx.fx(fx, at, yaw, pitch, 1.0f, s, b);
                }
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ghost g : ghosts) SpellFx.remove(g.ref, cb);
            ghosts.clear();
            rings.clear();
        }

        @Override
        public void abort() {
            for (Ghost g : ghosts) RuneSpellRuntime.removeLater(g.ref);
            ghosts.clear();
        }
    }

    private static Vector3d flat(Vector3d v) {
        Vector3d f = new Vector3d(v.x, 0, v.z);
        return f.lengthSquared() < 1e-6 ? new Vector3d(0, 0, 1) : f.normalize();
    }
}
