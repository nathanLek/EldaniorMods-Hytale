package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Rune de la Meditation martiale (technique du Clan Luo) : competence a bascule. Un clic assoit le joueur en tailleur :
 * emote Luo_Meditation (Server/Emote, emplacement Emote = corps entier, objet en main cache ; l'emplacement Action n'animait
 * que le haut du corps, vu en jeu le 2026-11-09), jouee 0,45 s apres le clic puis a 1,95 s, une fois l'animation de la rune
 * terminee ; effet Meditation_Martiale (deplacements bloques, volutes de Ki). Attaque et garde : chaines annulees a chaque
 * tick et coups annules (DivineDamageSystem -> isMeditating) ;
 * il est vulnerable. Un second clic le releve ; un coup recu brise la meditation. Pas de mana.
 * Gains selon le palier (1 Apprenti ... 8 Dieu Martial), revus le 2026-11-10 :
 *   vie, mana et endurance : 2, 3, 4, 5, 6,5, 8, 10, 12 % du maximum toutes les 10 s (au moins 2, 3, 4, 5, 6, 8, 10, 12 points) ;
 *   experience : 0,3, 0,4, 0,5, 0,6, 0,7, 0,8, 1, 1,2 % du niveau toutes les 15 s (au moins 1 a 5 points) ; ecriture differee de
 *   PlayerLevelData, relue au vidage.
 * Signes de Ki (effet Meditation_Martiale_T<palier>, couleur du Ki du palier) : aucun pour l'Apprenti, puis de plus en plus
 * fort ; Saint Martial et au-dela levitent a LEVITATE blocs en tailleur (vol force a vitesse nulle), dans une aura puissante
 * (Dieu Martial : or blanc) ; Maitre Supreme et au-dela soulevent des blocs du sol qui flottent autour d'eux. Yeux fermes.
 * Mediter coupe le Ki de l'Epee.
 * Fiche de conception : scripts/runes/specs/Rune_MeditationMartiale.json
 */
public final class MeditationMartiale implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    static final int[] POINTS = {2, 3, 4, 5, 6, 8, 10, 12};
    static final float[] PCT = {0.02f, 0.03f, 0.04f, 0.05f, 0.065f, 0.08f, 0.10f, 0.12f};
    static final int[] XP = {1, 1, 2, 2, 3, 3, 4, 5};
    static final float[] XP_PCT = {0.003f, 0.004f, 0.005f, 0.006f, 0.007f, 0.008f, 0.010f, 0.012f};
    /** Saint Martial (palier 6) et au-dela : levitation, assis, a LEVITATE blocs du sol. */
    static final int LEVITATE_TIER = 6;
    static final double LEVITATE = 3.0;
    static final long RISE_MS = 1_500, HOLD_MS = 400;
    /** Maitre Supreme et au-dela : des blocs du sol se soulevent et flottent autour du meditant (MartialTerrain.Floating). */
    static final int STONES_TIER = 7;
    static final long GAIN_MS = 10_000, XP_MS = 15_000, REFRESH_MS = 1_000;
    /** La posture est jouee apres l'animation de fin d'incantation de la rune (sinon elle l'efface : vu en jeu le 2026-11-09). */
    static final long POSE_DELAY_MS = 450, POSE_AGAIN_MS = 1_500;

    // ===== Assets =====
    static final String EFFECT = "Meditation_Martiale", EMOTE = "Luo_Meditation", EMOTE_AWAKE = "Luo_Meditation_Eveil";
    /** Maitre Martial (palier 4) et au-dela : meditation eveillee (mains jointes, respiration ample), orbes de Ki en orbite,
     *  anneau de souffle au sol ; Saint et au-dela : colonne de Ki (gen_fx_martiaux.py), retour du 2026-11-10. */
    static final int AWAKE_TIER = 4;
    static final long ORBIT_MS = 200, BREATH_MS = 2_400, COLUMN_MS = 1_200;
    static final double ORBIT_RADIUS = 1.3;
    private static final String FX_GAIN = "Apollo_Heal_Pulse", FX_RING = "Eldanior_Pas_Celeste";
    private static final String SFX_START = "SFX_Deployable_Totem_Heal_Spawn", SFX_GAIN = "SFX_Deployable_Totem_Heal_Despawn";

    @Override public String id() { return "MeditationMartiale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Calm running = RuneSpellRuntime.find(uuid, Calm.class);
        if (running != null) {                                                 // second clic : on se releve
            running.stop = true;
            return null;
        }
        KiEpee.Active ki = RuneSpellRuntime.find(uuid, KiEpee.Active.class);
        if (ki != null) ki.stop = true;
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        int tier = MartialKi.tier(caster, store);
        SpellFx.effect(caster, effect(tier), cb);
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 1.4f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        MartialKi.notify(caster, store, "<color:aqua>Méditation</color> <color:gray>— " + VoieMartiale.rank(tier)
                + " : +" + Math.round(PCT[tier - 1] * 1000) / 10f + " % de vie, mana, endurance / 10 s, +"
                + Math.round(XP_PCT[tier - 1] * 1000) / 10f + " % d'XP / 15 s" + (tier >= LEVITATE_TIER ? ", lévitation" : "") + "</color>",
                NotificationStyle.Success);
        long now = System.currentTimeMillis();
        return new Calm(caster, tier, now, pos);
    }

    /** Posture : meditation eveillee (mains jointes) a partir du Maitre Martial. */
    static String emote(int tier) {
        return tier >= AWAKE_TIER ? EMOTE_AWAKE : EMOTE;
    }

    /** Effet de posture et d'aura du palier. */
    static String effect(int tier) {
        return EFFECT + "_T" + Math.max(1, Math.min(8, tier));
    }

    static final class Calm implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d ground;
        volatile boolean stop, hurt;
        int tier, shownTier;
        long lastRefresh, lastGain, lastXp, startAt, lastHold;
        int poses;
        boolean levitated, suspended;
        long seatedAt;
        com.hypixel.hytale.protocol.FlyMode prevFly;
        float prevH, prevV;
        final MartialTerrain.Floating stones = new MartialTerrain.Floating();
        boolean stonesUp;

        Calm(Ref<EntityStore> caster, int tier, long now, Vector3d ground) {
            this.caster = caster; this.tier = tier; this.shownTier = tier; this.ground = new Vector3d(ground);
            this.lastRefresh = now; this.lastGain = now; this.lastXp = now; this.startAt = now;
        }

        /**
         * Saint et au-dela : s'eleve en RISE_MS jusqu'a LEVITATE blocs, puis reste suspendu par un vol force a vitesse nulle
         * (comme la levitation du Courroux de Muspelheim) : plus de gravite, donc plus de teleportations ni de tremblement, et
         * la posture en tailleur reste jouee en l'air (retour du 2026-11-10 : le siege invisible imposait la pose de la chaise).
         */
        private void levitate(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (tier < LEVITATE_TIER) return;
            SpellFx.protectFromFall(caster, 3_000, store);
            double rise = Math.min(1.0, (now - startAt - POSE_DELAY_MS) / (double) RISE_MS);
            if (rise <= 0 || suspended) return;
            levitated = true;
            SpellFx.push(caster, new Vector3d(0, 0, 0), false, cb);
            SpellFx.teleport(caster, new Vector3d(ground).add(0, LEVITATE * rise * rise * (3 - 2 * rise), 0), store, cb);
            if (rise >= 1) {
                suspended = suspend(caster, true, store, cb);
                poses = 2;                                                      // la posture est rejouee une fois en l'air
                seatedAt = now;
            }
        }

        /** Vol force a vitesse nulle (suspendu) ou retour des reglages d'avant. */
        private boolean suspend(Ref<EntityStore> caster, boolean on, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            try {
                var manager = store.getComponent(caster, com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager.getComponentType());
                PlayerRef net = store.getComponent(caster, PlayerRef.getComponentType());
                if (manager == null || net == null || manager.getSettings() == null) return false;
                var st = manager.getSettings();
                if (on) {
                    prevFly = st.fly; prevH = st.horizontalFlySpeed; prevV = st.verticalFlySpeed;
                    st.fly = com.hypixel.hytale.protocol.FlyMode.Forced;
                    st.horizontalFlySpeed = 0f;
                    st.verticalFlySpeed = 0f;
                } else {
                    if (prevFly == null) return false;
                    st.fly = prevFly; st.horizontalFlySpeed = prevH; st.verticalFlySpeed = prevV;
                    prevFly = null;
                }
                manager.update(net.getPacketHandler());
                cb.putComponent(caster, com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager.getComponentType(), manager);
                net.getPacketHandler().writeNoCache(new com.hypixel.hytale.protocol.packets.player.SetMovementStates(
                        new com.hypixel.hytale.protocol.SavedMovementStates(on)));
                return on;
            } catch (Exception e) {
                EldaniorLogger.error("MeditationMartiale: suspension", e);
                return false;
            }
        }

        long lastOrbit, lastBreath, lastColumn, lastMandala, lastPulse;
        /** Couronne d'epees 3D de Ki (Saint : 6 ; Dieu Martial : 6 + 6 en sens inverse), retour du 2026-11-10 (nuit). */
        final java.util.List<Ref<EntityStore>> halo = new java.util.ArrayList<>();
        final java.util.Set<Ref<EntityStore>> dressed = new java.util.HashSet<>();

        /** Effets de Ki du Maitre Martial et au-dela : orbes en orbite, souffle au sol, colonne (Saint et au-dela). */
        private void aura(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (tier < AWAKE_TIER || now - startAt < POSE_DELAY_MS) return;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return;
            if (now - lastOrbit >= ORBIT_MS) {
                lastOrbit = now;
                int orbs = tier - 2;                                            // 2 orbes (Maitre) ... 6 (Dieu Martial)
                double turn = (now - startAt) / 1000.0 * (0.8 + 0.1 * tier);
                for (int i = 0; i < orbs; i++) {
                    double a = turn + i * 2 * Math.PI / orbs;
                    double h = 0.9 + 0.35 * Math.sin(turn * 1.7 + i);
                    SpellFx.fx("Ki_Orbe_T" + tier, new Vector3d(pos).add(Math.cos(a) * ORBIT_RADIUS, h, Math.sin(a) * ORBIT_RADIUS),
                            0.6f + 0.05f * tier, store, cb);
                }
            }
            if (now - lastBreath >= BREATH_MS) {                               // souffle : anneau au sol a chaque expiration
                lastBreath = now;
                SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, levitated ? -LEVITATE + 0.05 : 0.05, 0), 0.8f + 0.12f * tier, store, cb);
            }
            if (tier >= LEVITATE_TIER && now - lastColumn >= COLUMN_MS) {
                lastColumn = now;
                SpellFx.fx("Ki_Colonne_T" + tier, new Vector3d(pos).add(0, 0.2, 0), 0.9f + 0.1f * (tier - 5), store, cb);
            }
            if (tier >= LEVITATE_TIER) sacred(caster, pos, now, store, cb);
        }

        /**
         * Saint Martial et Dieu Martial (retour du 2026-11-10 nuit : « rends ca plus impressionnant ») : couronne d'epees 3D de
         * Ki qui tournent lentement autour du meditant, pointe dans le sens de leur course (6 ; Dieu Martial : une seconde
         * couronne de 6, plus large et inclinee, en sens inverse) ; mandala d'ondes de Ki au sol ; Dieu Martial : le voile
         * d'or du domaine et une secousse a chaque grande respiration.
         */
        private void sacred(Ref<EntityStore> caster, Vector3d pos, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            int want = tier >= 8 ? 12 : 6;
            while (halo.size() < want) {
                Ref<EntityStore> r = SpellFx.spawnModel(caster, "Projectile_Config_Ki_Epee_Proj_T" + tier, new Vector3d(pos).add(0, 1, 0),
                        new Vector3d(0, 0, 1), cb);
                if (r == null) break;
                SpellFx.notSaved(r, store, cb);
                halo.add(r);
            }
            double t = (now - startAt) / 1000.0;
            Vector3d center = new Vector3d(pos).add(0, 1.0, 0);
            for (int i = 0; i < halo.size(); i++) {
                Ref<EntityStore> r = halo.get(i);
                if (r == null || !r.isValid()) continue;
                if (dressed.add(r)) SpellFx.effect(r, "Ki_Epee_Corps_T" + tier, cb);
                boolean outer = i >= 6;
                int k = i % 6;
                double speed = outer ? -0.45 : 0.6, radius = outer ? 3.4 : 2.3, tilt = outer ? 0.35 : 0.0;
                double a = t * speed + k * Math.PI / 3 + (outer ? Math.PI / 6 : 0);
                double bob = Math.sin(t * 1.3 + k) * 0.15;
                Vector3d at = new Vector3d(center).add(Math.cos(a) * radius, bob + Math.sin(a) * radius * tilt, Math.sin(a) * radius);
                Vector3d tangent = new Vector3d(-Math.sin(a), Math.cos(a) * tilt, Math.cos(a)).mul(Math.signum(speed)).normalize();
                SpellFx.hold(r, at, SpellFx.facing(tangent, true), store);
            }
            if (now - lastMandala >= 1_600) {                                   // mandala : trois ondes concentriques au sol
                lastMandala = now;
                double gy = levitated ? -LEVITATE + 0.06 : 0.06;
                for (int i = 0; i < 3; i++)
                    SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, gy + i * 0.02, 0), 1.2f + 0.9f * i + (tier >= 8 ? 0.8f : 0f), store, cb);
            }
            if (tier >= 8 && now - lastPulse >= 5_000) {                        // Dieu Martial : le monde respire avec lui
                lastPulse = now;
                Vector3d g = new Vector3d(pos).add(0, levitated ? -LEVITATE : 0, 0);
                SpellFx.fx("Domaine_Voile", g, 0.7f, store, cb);
                SpellFx.fx("Ki_Orbe_T8", center, 2.5f, store, cb);
                SpellFx.shake(g, 24, "Impact_Light", store);
                SpellFx.sound("SFX_Longsword_Special_Impact", g, cb);
            }
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (amount > 0) hurt = true;                                         // un coup brise la meditation
            return amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (hurt) {
                MartialKi.notify(caster, store, "<color:red>Votre méditation est brisée.</color>", NotificationStyle.Warning);
                return true;
            }
            if (stop) {
                MartialKi.notify(caster, store, "<color:gray>Vous vous relevez.</color>", NotificationStyle.Default);
                return true;
            }
            if (poses < 2 && now - startAt >= POSE_DELAY_MS + poses * POSE_AGAIN_MS) {   // s'asseoir (puis une fois encore)
                poses++;
                AnimationUtils.playAnimation(caster, AnimationSlot.Emote, null, emote(tier), true, store);
            }
            if (suspended && poses < 3 && now - seatedAt >= 250) {             // posture en tailleur une fois suspendu en l'air
                poses = 3;
                AnimationUtils.playAnimation(caster, AnimationSlot.Emote, null, emote(tier), true, store);
            }
            aura(caster, now, store, cb);
            if (tier >= STONES_TIER) {                                          // Maitre Supreme et au-dela : la terre se souleve
                if (!stonesUp && now - startAt >= POSE_DELAY_MS) {
                    stonesUp = true;
                    stones.spawn(SpellFx.world(store), ground, tier >= 8 ? 12 : 7, store, cb);
                    SpellFx.shake(ground, 12, "Impact_Light", store);
                }
                stones.tick(ground, (now - startAt) / 1000.0, Math.min(1.0, (now - startAt) / 2500.0), store);
            }
            SpellFx.cancelChains(caster, store, com.hypixel.hytale.protocol.InteractionType.Primary);     // pas d'attaque assis
            SpellFx.cancelChains(caster, store, com.hypixel.hytale.protocol.InteractionType.Secondary);
            levitate(caster, now, store, cb);
            if (now - lastRefresh >= REFRESH_MS) {
                lastRefresh = now;
                tier = MartialKi.tier(caster, store);
                if (tier != shownTier) { EffectsManager.removeEffect(caster, effect(shownTier), cb); shownTier = tier; }
                SpellFx.effect(caster, effect(tier), cb);
            }
            if (now - lastGain >= GAIN_MS) {
                lastGain = now;
                for (int stat : new int[]{DefaultEntityStatTypes.getHealth(), DefaultEntityStatTypes.getMana(), DefaultEntityStatTypes.getStamina()})
                    MartialKi.addStat(caster, stat, Math.max(POINTS[tier - 1], max(caster, stat, store) * PCT[tier - 1]), store);
                Vector3d pos = SpellFx.position(caster, store);
                if (pos != null) {
                    SpellFx.fx(FX_GAIN, pos, 0.8f, store, cb);
                    SpellFx.sound(SFX_GAIN, pos, cb);
                }
            }
            if (now - lastXp >= XP_MS) {
                lastXp = now;
                giveXp(caster, XP[tier - 1], XP_PCT[tier - 1], cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            stones.clear(cb);
            for (Ref<EntityStore> r : halo) SpellFx.remove(r, cb);
            halo.clear();
            if (caster == null || !caster.isValid()) return;
            try {
                for (int t = 1; t <= 8; t++) EffectsManager.removeEffect(caster, effect(t), cb);
                AnimationUtils.stopAnimation(caster, AnimationSlot.Emote, cb);
                if (suspended) suspend(caster, false, caster.getStore(), cb);
            } catch (Exception e) {
                EldaniorLogger.error("MeditationMartiale: fin", e);
            }
        }

        @Override
        public void abort() {
            for (Ref<EntityStore> r : halo) RuneSpellRuntime.removeLater(r);
            halo.clear();
        }
    }

    /** true si ce joueur medite (ses coups sont annules : DivineDamageSystem). */
    public static boolean isMeditating(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return false;
        Calm c = RuneSpellRuntime.find(SpellFx.uuid(ref, store), Calm.class);
        return c != null && !c.stop && !c.hurt;
    }

    /** Maximum d'une statistique (0 si inconnue). */
    private static float max(Ref<EntityStore> ref, int stat, Store<EntityStore> store) {
        try {
            var map = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            var v = map != null ? map.get(stat) : null;
            return v != null ? v.getMax() : 0f;
        } catch (Exception e) {
            return 0f;
        }
    }

    /** XP de meditation : PlayerLevelData relu et ecrit au vidage du tampon (ne peut pas ecraser un gain du meme tick). */
    private static void giveXp(Ref<EntityStore> caster, int minimum, float pct, CommandBuffer<EntityStore> cb) {
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        cb.run(s -> {
            PlayerLevelData data = caster.isValid() ? s.getComponent(caster, type) : null;
            if (data == null) return;
            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            int before = copy.getLevel();
            copy.addExperience(Math.max(minimum, Math.round(copy.getRequiredExperience() * pct)));
            s.putComponent(caster, type, copy);
            PlayerRef player = s.getComponent(caster, PlayerRef.getComponentType());
            if (copy.getLevel() > before) {
                StatCalculator.updatePlayerStats(caster, s, copy);
                if (player != null) NotificationHelper.showLevelUpTitle(player, copy.getLevel());
            }
        });
    }
}
