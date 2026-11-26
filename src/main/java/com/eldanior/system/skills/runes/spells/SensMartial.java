package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.skills.system.InvisibilityManager;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Rune du Sens martial (Luo, Rare, Maitre Martial, voie martiale ; refaite le 2026-11-10 a la demande de l'utilisateur) :
 * les ennemis traques sont enveloppes d'une silhouette rouge collee a eux (particules Sens_Silhouette_*, gen_fx_martiaux.py,
 * reemises toutes les DRAW_MS) et d'un anneau a leurs pieds, que seul le lanceur voit (SpellFx.fxFor).
 * A travers les murs (retour du 2026-11-10 soir) : le moteur ne dessine rien derriere un mur ; pour un ennemi cache, un double
 * de son modele, rouge (effet Sens_Fantome), est place DEVANT le mur sur la ligne des yeux du lanceur a l'ennemi, reduit
 * dans le rapport des distances : a l'ecran il occupe exactement la place de l'ennemi (perspective). Seul le lanceur le voit
 * (PrivateVisibility, classe WallGhosts partagee avec la rune Arkies Domini ; plus d'etiquette nom/distance depuis le
 * 2026-11-16). Valable en vue a la premiere personne (en vue a la troisieme, la camera n'est pas aux yeux).
 *
 * Selon la classe (Maitre ... Dieu Martial) : rayon 30 / 50 / 75 / 110 / 150 blocs ; cibles suivies 1 / 2 / 4 / 8 / 24 ;
 * duree 12 / 15 / 20 / 25 / 30 s (un second clic l'arrete avant ; mana preleve chaque seconde 2 / 2 / 3 / 3 / 4).
 * Maitre et Grand Maitre : sixieme sens, il traque la ou les cibles choisies a l'activation (celle qu'il vise d'abord, puis
 * les plus proches), ou qu'elles aillent dans le rayon. Saint et au-dela : tous les ennemis du rayon (releves chaque seconde),
 * et les presences cachees (InvisibilityManager) sont revelees. Toujours : les coups recus dans le dos sont reduits de moitie.
 * Fiche de conception : scripts/runes/specs/Rune_SensMartial.json
 */
public final class SensMartial implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    static final int[] MANA = {2, 2, 2, 2, 2, 3, 3, 4};
    static final double[] RADIUS = {30, 30, 30, 30, 50, 75, 110, 150};
    static final int[] TARGETS = {1, 1, 1, 1, 2, 4, 8, 24};
    static final long[] DURATION_MS = {12_000, 12_000, 12_000, 12_000, 15_000, 20_000, 25_000, 30_000};
    static final int REVEAL_TIER = 6;
    static final float BACK_REDUCTION = 0.5f;
    static final long SECOND_MS = 1_000, DRAW_MS = 150;
    /** Parties de la silhouette : hauteur depuis les pieds, systeme de particules. */
    private static final double[] PART_Y = {1.65, 1.1, 0.45};
    private static final String[] PART_FX = {"Sens_Silhouette_Tete", "Sens_Silhouette_Corps", "Sens_Silhouette_Jambes"};

    /** Doubles rouges devant les murs : WallGhosts (sans etiquette depuis le 2026-11-16). Onde du sixieme sens. */
    private static final long SONAR_MS = 3_000;

    // ===== Assets =====
    private static final String FX_START = "Eldanior_Pas_Celeste", FX_REVEAL = "Gilgamesh_Sword_Burst";
    private static final String SFX_START = "SFX_Ability_Wind_Strike_Imbue";

    @Override public String id() { return "SensMartial"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Active running = RuneSpellRuntime.find(uuid, Active.class);
        if (running != null) {
            running.stop = true;
            MartialKi.notify(caster, store, "<color:gray>Vos sens reviennent à la normale.</color>", NotificationStyle.Default);
            return null;
        }
        int tier = Math.max(4, MartialKi.tier(caster, store));
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < MANA[tier - 1]) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana.</color>", NotificationStyle.Warning);
            return null;
        }
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_START, new Vector3d(pos).add(0, 0.1, 0), 2.0f, store, cb);
        SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, 0.05, 0), 1.5f + 0.2f * tier, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        Active a = new Active(caster, tier, System.currentTimeMillis());
        a.pick(caster, pos, store, SpellFx.targetEntity(caster, store, RADIUS[tier - 1]));
        MartialKi.notify(caster, store, "<color:aqua>Sens martial</color> <color:gray>— " + VoieMartiale.rank(tier) + ", "
                + (int) RADIUS[tier - 1] + " blocs, " + DURATION_MS[tier - 1] / 1000 + " s : "
                + (tier >= REVEAL_TIER ? a.tracked.size() + " présence(s)" : a.tracked.isEmpty() ? "aucune cible"
                : a.tracked.size() + " cible(s) traquée(s)") + "</color>", NotificationStyle.Success);
        return a;
    }

    static final class Active implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        volatile boolean stop;
        final int tier;
        long lastSecond, lastDraw;
        int draws;
        List<Ref<EntityStore>> tracked = new ArrayList<>();
        final WallGhosts walls = new WallGhosts();
        long lastSonar;

        Active(Ref<EntityStore> caster, int tier, long now) { this.caster = caster; this.tier = tier; this.startAt = now; this.lastSecond = now; }

        /** Cibles : celle qu'il vise d'abord (palier 4-5), puis les plus proches, dans la limite du palier. */
        void pick(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store, Ref<EntityStore> aimed) {
            List<Ref<EntityStore>> all = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, me, RADIUS[tier - 1], store)) {
                UUID id = SpellFx.uuid(e, store);
                if (id != null && InvisibilityManager.isInvisible(id)) {
                    if (tier < REVEAL_TIER) continue;                             // cache : seul le Saint le sent
                    InvisibilityManager.removeInvisibility(id, e, store);
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null) SpellFx.fxFor(FX_REVEAL, new Vector3d(p).add(0, 1.2, 0), 1.0f, caster, store);
                    MartialKi.notify(e, store, "<color:red>Votre présence a été percée à jour.</color>", NotificationStyle.Warning);
                }
                all.add(e);
            }
            all.sort(Comparator.comparingDouble(e -> {
                Vector3d p = SpellFx.position(e, store);
                return p == null ? Double.MAX_VALUE : p.distanceSquared(me);
            }));
            List<Ref<EntityStore>> out = new ArrayList<>();
            if (aimed != null && all.remove(aimed)) out.add(aimed);
            for (Ref<EntityStore> e : all) { if (out.size() >= TARGETS[tier - 1]) break; out.add(e); }
            tracked = out;
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (stop || attacker == null || !attacker.isValid() || caster == null || !caster.isValid()) return amount;
            try {
                Store<EntityStore> store = caster.getStore();
                Vector3d me = SpellFx.position(caster, store), them = SpellFx.position(attacker, store);
                if (me == null || them == null) return amount;
                Vector3d to = new Vector3d(them).sub(me);
                to.y = 0;
                if (to.lengthSquared() < 1e-4) return amount;
                return SpellFx.forward(caster, store).dot(to.normalize()) < -0.3 ? amount * (1f - BACK_REDUCTION) : amount;
            } catch (Exception e) {
                return amount;
            }
        }

        /**
         * Silhouette rouge collee a chaque cible (tete, buste, jambes a sa position reelle) et anneau rouge a ses pieds, vus du
         * seul lanceur. Retour du 2026-11-10 : la projection devant les yeux faisait apparaitre des points rouges autour du
         * lanceur, peu lisibles ; le moteur ne sait pas dessiner a travers les murs, la marque reste donc sur l'entite.
         */
        private void draw(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store) {
            double r2 = RADIUS[tier - 1] * RADIUS[tier - 1];
            boolean ring = ++draws % 4 == 0;
            for (Ref<EntityStore> e : tracked) {
                Vector3d p = e != null && e.isValid() ? SpellFx.position(e, store) : null;
                if (p == null || p.distanceSquared(me) > r2) continue;
                for (int i = 0; i < PART_Y.length; i++)
                    SpellFx.fxFor(PART_FX[i], new Vector3d(p).add(0, PART_Y[i], 0), 1.0f, caster, store);
                if (ring) SpellFx.fxFor("Ki_Anneau_T1", new Vector3d(p).add(0, 0.05, 0), 0.6f, caster, store);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            walls.cleanup(cb);
        }

        @Override
        public void abort() {
            walls.abort();
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (stop) return true;
            if (now - startAt >= DURATION_MS[tier - 1]) {
                MartialKi.notify(caster, store, "<color:gray>Vos sens reviennent à la normale.</color>", NotificationStyle.Default);
                return true;
            }
            Vector3d me = SpellFx.position(caster, store);
            if (me == null) return true;
            if (now - lastDraw >= DRAW_MS) { lastDraw = now; draw(caster, me, store); }
            walls.update(caster, me, tracked, RADIUS[tier - 1], Integer.MAX_VALUE, store, cb);
            if (now - lastSonar >= SONAR_MS) {                                  // onde du sixieme sens, vue du seul lanceur
                lastSonar = now;
                SpellFx.fxFor("Ki_Anneau_T" + tier, new Vector3d(me).add(0, 0.08, 0), (float) Math.min(14.0, 2.0 + RADIUS[tier - 1] / 12.0), caster, store);
            }
            if (now - lastSecond < SECOND_MS) return false;
            lastSecond = now;
            int cost = MANA[tier - 1];
            if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
                MartialKi.notify(caster, store, "<color:gray>Vos sens s'émoussent : mana épuisé.</color>", NotificationStyle.Warning);
                return true;
            }
            MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -cost, store);
            if (tier >= REVEAL_TIER) pick(caster, me, store, null);              // Saint et au-dela : tout le rayon, releve
            else tracked.removeIf(e -> e == null || !e.isValid());               // sixieme sens : les memes cibles
            return false;
        }
    }
}
