package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Rune du Renforcement corporel (Luo, Uncommon, Disciple Martial, voie martiale, 2026-11-10) : competence a bascule comme le
 * Ki de l'Epee. Un clic durcit le corps par le Ki, un second clic le relache ; elle s'eteint quand le mana manque (cout preleve
 * chaque seconde). Effets selon le palier (2 Disciple ... 8 Dieu Martial) :
 *   degats subis -10, -12, -15, -18, -22, -25, -30 % ; mana/s 2, 2, 3, 4, 5, 6, 6 ;
 *   Grand Maitre et plus : bouclier de Ki qui absorbe entierement un coup toutes les 8 s (jusqu'a 80, 110, 140, 200 degats).
 * Cumulable avec le Ki de l'Epee. Aura ambree (effet Renforcement_Corps, gen_voie_martiale.py).
 * Fiche de conception : scripts/runes/specs/Rune_RenforcementCorps.json
 */
public final class RenforcementCorps implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 (paliers 1 et 2 identiques : rune obtenue hors palier) =====
    static final int[] MANA = {2, 2, 2, 3, 4, 5, 6, 6};
    static final float[] REDUCTION = {0.10f, 0.10f, 0.12f, 0.15f, 0.18f, 0.22f, 0.25f, 0.30f};
    /** Bouclier de Ki (palier 5 et plus) : degats absorbes d'un coup, toutes les SHIELD_MS. */
    static final float[] SHIELD = {0, 0, 0, 0, 80, 110, 140, 200};
    static final long SECOND_MS = 1_000, SHIELD_MS = 8_000;

    // ===== Assets =====
    static final String EFFECT = "Renforcement_Corps_T";
    /** Montee en puissance (retour du 2026-11-10) : posture Luo_Renforcement (gen_luo_anims.py), jouee apres l'animation de la
     *  rune ; onde de Ki au sol et colonne a la liberation ; veines de Ki (orbes) qui parcourent le corps tant qu'il est actif. */
    static final String EMOTE = "Luo_Renforcement";
    static final long POSE_DELAY_MS = 300, BURST_MS = 950, VEIN_MS = 450, PULSE_MS = 2_200;
    private static final String FX_START = "Eldanior_Pas_Celeste", FX_SHIELD = "Impact_Sword_Charged";
    private static final String SFX_START = "SFX_Ability_Wind_Strike_Imbue", SFX_SHIELD = "SFX_Sword_T2_Impact";

    @Override public String id() { return "RenforcementCorps"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Active running = RuneSpellRuntime.find(uuid, Active.class);
        if (running != null) {
            running.stop = true;
            MartialKi.notify(caster, store, "<color:gray>Votre corps se relâche.</color>", NotificationStyle.Default);
            return null;
        }
        int tier = MartialKi.tier(caster, store);
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < MANA[tier - 1]) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana.</color>", NotificationStyle.Warning);
            return null;
        }
        Vector3d pos = SpellFx.position(caster, store);
        if (pos != null) {
            SpellFx.fx(FX_START, new Vector3d(pos).add(0, 0.1, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_START, pos, cb);
        }
        SpellFx.effect(caster, EFFECT + tier, cb);
        MartialKi.notify(caster, store, "<color:gold>Renforcement corporel</color> <color:gray>— " + VoieMartiale.rank(tier) + " : −"
                + Math.round(REDUCTION[tier - 1] * 100) + " % de dégâts" + (SHIELD[tier - 1] > 0 ? ", bouclier de Ki" : "")
                + " (" + MANA[tier - 1] + " mana/s)</color>", NotificationStyle.Success);
        return new Active(caster, tier, System.currentTimeMillis());
    }

    /** Liberation de la force : onde de Ki au sol, poussiere, colonne de Ki, tremblement leger autour. */
    static void burst(Ref<EntityStore> caster, int tier, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return;
        SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, 0.05, 0), 1.2f + 0.15f * tier, store, cb);
        SpellFx.fx("Gaia_Dust_Burst", pos, 0.6f + 0.08f * tier, store, cb);
        SpellFx.fx("Ki_Colonne_T" + tier, new Vector3d(pos).add(0, 0.1, 0), 0.8f + 0.08f * tier, store, cb);
        if (tier >= 5) SpellFx.fx("Impact_Sword_Charged", new Vector3d(pos).add(0, 1.2, 0), 1.0f + 0.1f * tier, store, cb);
        SpellFx.sound("SFX_Longsword_Special_Impact", pos, cb);
        SpellFx.shake(pos, 6 + tier, tier >= 7 ? "Impact_Strong" : "Impact_Light", store);
        if (tier >= 7) {                                                        // Maitre Supreme et au-dela : le sol se fend en etoile
            int cracks = tier >= 8 ? 6 : 4;
            for (int i = 0; i < cracks; i++) {
                double a = i * 2 * Math.PI / cracks;
                MartialTerrain.fissure(SpellFx.world(store), pos, new Vector3d(Math.cos(a), 0, Math.sin(a)), 4 + tier * 0.6, store, cb);
            }
        }
    }

    static final class Active implements Instance {
        final Ref<EntityStore> caster;
        volatile boolean stop;
        volatile int tier;
        volatile long shieldReadyAt;
        volatile boolean shieldPop;
        long lastSecond, lastVein, lastPulse;
        final long startAt;
        boolean posed, burst;

        Active(Ref<EntityStore> caster, int tier, long now) { this.caster = caster; this.tier = tier; this.lastSecond = now; this.startAt = now; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (stop || amount <= 0) return amount;
            long now = System.currentTimeMillis();
            float shield = SHIELD[tier - 1];
            if (shield > 0 && now >= shieldReadyAt) {                          // le bouclier de Ki prend le coup
                shieldReadyAt = now + SHIELD_MS;
                shieldPop = true;
                amount = Math.max(0f, amount - shield);
            }
            return amount * (1f - REDUCTION[tier - 1]);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (stop) return true;
            if (!posed && now - startAt >= POSE_DELAY_MS) {                     // il se ramasse...
                posed = true;
                com.hypixel.hytale.server.core.entity.AnimationUtils.playAnimation(caster,
                        com.hypixel.hytale.protocol.AnimationSlot.Emote, null, EMOTE, true, store);
            }
            if (!burst && now - startAt >= BURST_MS) {                          // ... et libere sa force
                burst = true;
                burst(caster, tier, store, cb);
            }
            if (burst && now - lastPulse >= PULSE_MS) {                          // la force bat comme un coeur : onde, colonne
                lastPulse = now;
                Vector3d pos = SpellFx.position(caster, store);
                if (pos != null) {
                    SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, 0.05, 0), 0.7f + 0.08f * tier, store, cb);
                    if (tier >= 4) SpellFx.fx("Ki_Colonne_T" + tier, new Vector3d(pos).add(0, 0.1, 0), 0.5f + 0.06f * tier, store, cb);
                }
            }
            if (now - lastVein >= VEIN_MS) {                                     // veines de Ki qui parcourent le corps
                lastVein = now;
                Vector3d pos = SpellFx.position(caster, store);
                if (pos != null) {
                    var r = java.util.concurrent.ThreadLocalRandom.current();
                    int veins = 1 + tier / 3;
                    for (int i = 0; i < veins; i++)
                        SpellFx.fx("Ki_Orbe_T" + tier, new Vector3d(pos).add(r.nextDouble(-0.35, 0.35), r.nextDouble(0.3, 1.7),
                                r.nextDouble(-0.35, 0.35)), 0.35f + 0.03f * tier, store, cb);
                }
            }
            if (shieldPop) {
                shieldPop = false;
                Vector3d pos = SpellFx.position(caster, store);
                if (pos != null) {
                    SpellFx.fx(FX_SHIELD, new Vector3d(pos).add(0, 1.1, 0), 1.3f, store, cb);
                    SpellFx.sound(SFX_SHIELD, pos, cb);
                }
            }
            if (now - lastSecond < SECOND_MS) return false;
            lastSecond = now;
            tier = MartialKi.tier(caster, store);
            int cost = MANA[tier - 1];
            if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
                MartialKi.notify(caster, store, "<color:gray>Votre corps se relâche : mana épuisé.</color>", NotificationStyle.Warning);
                return true;
            }
            MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -cost, store);
            SpellFx.effect(caster, EFFECT + tier, cb);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (caster != null && caster.isValid()) for (int t = 1; t <= 8; t++) EffectsManager.removeEffect(caster, EFFECT + t, cb);
        }
    }
}
