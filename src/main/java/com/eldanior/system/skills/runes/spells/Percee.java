package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.eldanior.system.util.WorldBlockUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Percee (estoc des katanas Luo, validee le 2026-11-11) : garde levee (clic droit maintenu) puis clic gauche — la garde du sabre
 * a un embranchement « Primary » qui lit la variable Guard_Bash, que les katanas remplacent par Luo_Percee (gen_luo_katanas.py).
 * Bokken (2026-11-12) : aussi au maintien de l'attaque, apres la posture (PerceePosture). Huit versions, une par palier de la voie
 * martiale (Apprenti hors de la voie) :
 *
 *   1 Apprenti       Tsuki                estoc droit avec un pas, 3,5 blocs, le premier ennemi
 *   2 Disciple       Tsuki du Ruisseau    deux estocs enchaines : le premier ennemi, puis deux ennemis alignes, le dernier repousse
 *   3 Expert         Estoc Fendant        ruee rasante de 6 blocs a travers la ligne, images remanentes ; la coupe eclate apres
 *                                         le passage (60 % au passage, 40 % a l'entaille)
 *   4 Maitre         Aiguille de Ki       aiguille de Ki effilee sur 12 blocs (arretee par les blocs pleins), fil en spirale
 *   5 Grand Maitre   Mille Aiguilles      7 estocs en 0,6 s, une aiguille courte chacun, puis l'aiguille sur 14 blocs et six
 *                                         aiguilles en eventail
 *   6 Saint          Percee du Prunier    trait d'or sur 20 blocs (traverse feuilles, herbe et verre) ; les fleurs de prunier
 *                                         eclosent sur les ennemis transperces
 *   7 Maitre Supreme Percee Celeste       canon de Ki (l'ancien palier 8) : 1,2 s de charge, faisceau de 2,5 blocs de rayon sur
 *                                         48 blocs, transperce, creuse le tunnel (blocs remis apres 60 s), debris, explosion
 *   8 Dieu Martial   Cinq Lances Celestes cinq estocs eclair dans le vide : chacun fait naitre une lance de Ki qui flotte derriere
 *                                         le lanceur, au-dessus de ses epaules ; apres un temps, elles se chargent et partent une
 *                                         a une : chacune tire le canon de Ki du Maitre Supreme (Shot) vers la cible
 *
 * Decisions du 2026-11-12 (retours de l'utilisateur) : la Lance de l'Ame (ancien palier 7) est retiree, le canon passe au
 * Maitre Supreme, le Dieu Martial recoit les Cinq Lances (« comme Gilgamesh, sans portail ») ; les effets sont enrichis a
 * partir de l'Expert ; chaque Percee coute de l'endurance ET du mana en proportion de sa puissance (STAMINA_COST, MANA_COST,
 * fractions du maximum ; refusee s'il en manque). Suite : chaque lance du Dieu Martial tire le canon complet, les lances partent
 * plus lentement apres le mouvement, et tous les effets du coup (souffle, eclat, bouche du canon) partent de la pointe de la lame
 * bras tendu (TIP_*), plus de la main.
 *
 * Degats x la puissance du katana (Bokken et katana de fer : x1 ; Gotetsu : x3). Le Bokken ne tue pas (LuoPassives.trainingCap).
 * Animations : percee_anims.py (instants de frappe ci-dessous, 60 images/s). Assets a la couleur du Ki du palier :
 * gen_percee.py (suffixe _T<palier>).
 */
public final class Percee implements RuneSpell {

    static final String[] NAMES = {"Tsuki", "Tsuki du Ruisseau", "Estoc Fendant", "Aiguille de Ki", "Mille Aiguilles",
            "Percée du Prunier", "Percée Céleste", "Cinq Lances Célestes"};
    /** Degats (palier 8 : par lance). */
    static final float[] DAMAGE = {22, 30, 38, 48, 52, 70, 240, 120};
    static final double[] RANGE = {3.5, 4.5, 6, 12, 14, 20, 48, 45};
    static final long[] COOLDOWN_MS = {1_200, 1_200, 1_500, 2_500, 3_000, 3_500, 14_000, 20_000};
    /** Cout en fraction de l'endurance et du mana maximum, croissant avec la puissance (2026-11-12). */
    static final float[] STAMINA_COST = {0.08f, 0.10f, 0.14f, 0.17f, 0.20f, 0.24f, 0.40f, 0.50f};
    static final float[] MANA_COST = {0.03f, 0.05f, 0.08f, 0.12f, 0.15f, 0.20f, 0.35f, 0.45f};
    /** Percee Celeste : charge, vitesse du front du faisceau (blocs/s), rayon du tunnel, duree de vie du faisceau apres le bout. */
    static final long CANNON_CHARGE_MS = 1_200, CANNON_LINGER_MS = 900, TUNNEL_RESTORE_MS = 60_000;
    static final double CANNON_SPEED = 140, CANNON_RADIUS = 2.5;
    private static final String SET = "Tenryumaru_Degainee";
    /** Instants de frappe (images des animations de percee_anims.py, 60 images/s). */
    static final long STRIKE_MS = 133, DOUBLE_A_MS = 100, DOUBLE_B_MS = 267, RUEE_MS = 83, RAFALE_FIRST_MS = 50, RAFALE_STEP_MS = 83,
            RAFALE_FINAL_MS = 667, RUEE_CUT_MS = 260, BLOOM_MS = 450;
    /** Assets aux couleurs du Ki : <prefixe><palier> (gen_percee.py). */
    static final String AIR = "Percee_Air_T", TRAIT = "Percee_Trait_T", AURA = "Percee_Aura_T", SPIRALE = "Percee_Spirale_T",
            FX_TIP = "Percee_Pointe_T", FX_READY = "Percee_Pret_T", FX_HIT = "Percee_Impact_T", FX_CUT = "Percee_Entaille_T",
            FX_TRACE = "Percee_Trace_T", FX_SOUL = "Percee_Silhouette_T", FX_CONVERGE = "Percee_Convergence_T",
            FX_MUZZLE = "Percee_Bouche_T", FX_BOOM = "Percee_Explosion_T", FX_PLUM = "Percee_Prunier", FX_RING = "Ki_Anneau_T",
            FX_COLUMN = "Ki_Colonne_T", FX_DUST = "Gaia_Dust_Burst", RAY = "Percee_Rayon_T", RAY_SPIRAL = "Percee_Spirale_Point_T";
    /** Cadence de dessin du rayon du canon (particules reemises). */
    static final long DRAW_MS = 90;
    /**
     * Pointe de la lame aux images de frappe, mesuree sur les animations (percee_anims.py + modele du katana) : {droite, haut,
     * avant} en blocs depuis les pieds, repere horizontal du regard. Les effets « coup de canon » partent de la, bras tendu.
     */
    static final double[] TIP_TSUKI = {0.06, 1.19, 2.23}, TIP_DOUBLE_A = {0.06, 1.21, 2.07}, TIP_DOUBLE_B = {0.05, 1.14, 2.33},
            TIP_RUEE = {0.07, 0.85, 2.35}, TIP_RAFALE = {0.0, 1.28, 2.15}, TIP_FINAL = {0.05, 1.14, 2.33},
            TIP_GATOTSU = {0.23, 1.26, 1.31}, TIP_CELESTE = {0.05, 1.19, 2.34}, TIP_POSTURE = {0.14, 1.1, 1.27};
    static final double[][] TIP_CINQ = {{0.05, 1.58, 2.08}, {-0.22, 1.84, 1.97}, {0.32, 1.82, 2.01}, {-0.35, 2.05, 1.85}, {0.47, 2.07, 1.9}};
    private static final Map<UUID, Long> ready = new ConcurrentHashMap<>();

    /** Point du repere du joueur : o = {droite, haut, avant} en blocs depuis les pieds, avant = regard a l'horizontale. */
    static Vector3d at(Vector3d feet, Vector3d flat, double[] o) {
        Vector3d right = new Vector3d(-flat.z, 0, flat.x);
        return new Vector3d(feet).add(right.mul(o[0])).add(0, o[1], 0).add(new Vector3d(flat).mul(o[2]));
    }

    @Override public String id() { return "LuoPercee"; }

    /** Puissance du katana tenu : ses degats d'iai rapportes au katana de fer, au moins 1. */
    static float power(LuoKatanas.Katana k) {
        return k == null ? 1f : Math.max(1f, k.iaiDamage() / 26f);
    }

    static LuoKatanas.Katana held(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player p = store.getComponent(ref, Player.getComponentType());
        ItemStack it = p == null || p.getInventory() == null ? null : p.getInventory().getActiveHotbarItem();
        return it == null ? null : LuoKatanas.of(it.getItemId());
    }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        PerceePosture.release(caster, store);
        int t = BokkenSignature.tier(caster, store);
        boolean npc = MartialKi.NPC_TIER.containsKey(caster);
        UUID id = SpellFx.uuid(caster, store);
        long now = System.currentTimeMillis();
        if (id != null && !npc) {
            Long r = ready.get(id);
            if (r != null && now < r) {
                MartialKi.notify(caster, store, "<color:gray>" + NAMES[t - 1] + " : encore " + Math.max(1, (r - now + 999) / 1000) + " s.</color>",
                        NotificationStyle.Warning);
                return null;
            }
        }
        if (!npc && !pay(caster, store, t)) return null;
        if (id != null) ready.put(id, now + COOLDOWN_MS[t - 1]);
        TenryumaruSystem.drawn(caster);
        float power = power(held(caster, store));
        if (!npc) MartialKi.notify(caster, store, "<color:gold>" + NAMES[t - 1] + "</color> <color:gray>— " + VoieMartiale.className(t) + "</color>",
                NotificationStyle.Default);
        if (t >= 8) return new Lances(caster, power, now);
        if (t == 7) return new Cannon(caster, 7, power, now, pos, store, cb);
        return new Thrust(caster, t, power, now);
    }

    /** Endurance et mana : STAMINA_COST / MANA_COST du maximum ; rien n'est pris s'il manque l'un ou l'autre. */
    static boolean pay(Ref<EntityStore> caster, Store<EntityStore> store, int t) {
        int st = DefaultEntityStatTypes.getStamina(), mn = DefaultEntityStatTypes.getMana();
        float sMax = CloneFx.maxStat(caster, st, store), sCur = MartialKi.stat(caster, st, store);
        float mMax = CloneFx.maxStat(caster, mn, store), mCur = MartialKi.stat(caster, mn, store);
        float sNeed = sMax * STAMINA_COST[t - 1], mNeed = mMax * MANA_COST[t - 1];
        boolean sOk = sMax <= 0 || sCur < 0 || sCur + 0.01f >= sNeed, mOk = mMax <= 0 || mCur < 0 || mCur + 0.01f >= mNeed;
        if (!sOk || !mOk) {
            String need = (!sOk ? Math.round(STAMINA_COST[t - 1] * 100) + " % d'endurance" : "")
                    + (!sOk && !mOk ? " et " : "") + (!mOk ? Math.round(MANA_COST[t - 1] * 100) + " % de mana" : "");
            MartialKi.notify(caster, store, "<color:gray>" + NAMES[t - 1] + " : il faut " + need + ".</color>", NotificationStyle.Warning);
            return false;
        }
        if (sMax > 0 && sCur >= 0) MartialKi.addStat(caster, st, -sNeed, store);
        if (mMax > 0 && mCur >= 0) MartialKi.addStat(caster, mn, -mNeed, store);
        return true;
    }

    // ===================================================================================================================
    // Outils communs : deroule minute et faisceaux qui s'effacent
    // ===================================================================================================================

    interface Act { void run(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b); }

    record Step(long at, Act act) {}

    /** Faisceau qui s'efface : largeurs w0 (depart) et w1 (arrivee), tenues hold ms puis reduites a rien en life ms. */
    record Fade(Ref<EntityStore> ref, String id, Vector3d from, Vector3d to, float w0, float w1, long born, long hold, long life) {}

    static final class Fades {
        final List<Fade> list = new ArrayList<>();

        void add(Store<EntityStore> s, CommandBuffer<EntityStore> b, String id, Vector3d from, Vector3d to, float w0, float w1, long hold, long life) {
            Ref<EntityStore> r = SpellFx.spawnBeam(id, from, to, w0, w1, hold + life + 300, s, b);
            if (r != null) list.add(new Fade(r, id, from, to, w0, w1, System.currentTimeMillis(), hold, life));
        }

        void tick(long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            for (var it = list.iterator(); it.hasNext(); ) {
                Fade f = it.next();
                long age = now - f.born();
                if (age >= f.hold() + f.life()) { SpellFx.remove(f.ref(), b); it.remove(); continue; }
                double k = age <= f.hold() ? 1 : 1 - (age - f.hold()) / (double) f.life();
                SpellFx.beam(f.ref(), f.id(), f.from(), f.to(), (float) (f.w0() * k) + 0.02f, (float) (f.w1() * k) + 0.01f, s);
            }
        }

        boolean isEmpty() { return list.isEmpty(); }

        void clear(CommandBuffer<EntityStore> cb) {
            for (Fade f : list) SpellFx.remove(f.ref(), cb);
            list.clear();
        }
    }

    // ===================================================================================================================
    // Paliers 1 a 6 : un deroule minute par palier
    // ===================================================================================================================

    static final class Thrust implements Instance {
        final Ref<EntityStore> caster;
        final int tier;
        final float power;
        final long startAt;
        final List<Step> steps = new ArrayList<>(), added = new ArrayList<>();
        final Fades fades = new Fades();
        Vector3d feet, eye, look, flat, right;
        double[] tipOff = TIP_TSUKI;

        Thrust(Ref<EntityStore> caster, int tier, float power, long now) {
            this.caster = caster; this.tier = tier; this.power = power; this.startAt = now;
            String anim = switch (tier) {
                case 2 -> "TsukiDouble";
                case 3 -> "TsukiRuee";
                case 5 -> "TsukiRafale";
                default -> "Tsuki";
            };
            steps.add(new Step(0, (c, s, b) -> {
                aim(c, s);
                AnimationUtils.playAnimation(c, AnimationSlot.Action, SET, anim, true, s);
                SpellFx.sound("SFX_Sword_T2_Swing_Down", eye, b);
            }));
            switch (tier) {
                case 1 -> steps.add(new Step(STRIKE_MS, (c, s, b) -> {
                    strike(c, s, b, 4, 0.3f);
                    air(s, b, look, 3.0, 0.3f);
                    tipFlash(s, b, 0.75f);
                    trace(s, b, tip(), look, 0.8, 3.4, 0.9);
                    melee(c, s, b, RANGE[0], 0.95, 1, 1f, 3, false);
                }));
                case 2 -> {
                    steps.add(new Step(DOUBLE_A_MS, (c, s, b) -> {                       // le courant touche une premiere fois
                        tipOff = TIP_DOUBLE_A;
                        strike(c, s, b, 2, 0.2f);
                        air(s, b, look, 2.6, 0.22f);
                        tipFlash(s, b, 0.6f);
                        melee(c, s, b, 3.0, 0.9, 1, 0.5f, 1, false);
                    }));
                    steps.add(new Step(DOUBLE_B_MS, (c, s, b) -> {                       // puis emporte tout sur son passage
                        tipOff = TIP_DOUBLE_B;
                        strike(c, s, b, 4, 0.35f);
                        air(s, b, look, 4.2, 0.34f);
                        tipFlash(s, b, 0.9f);
                        trace(s, b, tip(), look, 0.6, 4.6, 0.5);
                        melee(c, s, b, RANGE[1], 0.95, 2, 1f, 3, true);
                    }));
                }
                case 3 -> steps.add(new Step(RUEE_MS, this::ruee));
                case 4 -> steps.add(new Step(STRIKE_MS, (c, s, b) -> {
                    strike(c, s, b, 4, 0.4f);
                    SpellFx.fx(FX_READY + tier, tip(), 0.8f, s, b);                       // la pointe se charge de Ki
                    SpellFx.fx(FX_RING + tier, tip(), 0.8f, s, b);                       // onde de pression a la main
                    air(s, b, look, 2.4, 0.28f);
                    tipFlash(s, b, 0.9f);
                    needle(c, s, b, eye(), look, RANGE[3], 0, 0.6f, 0.12f, 1f, 160, 360, false);
                }));
                case 5 -> {
                    for (int i = 0; i < 7; i++) {
                        final int k = i;
                        steps.add(new Step(RAFALE_FIRST_MS + i * RAFALE_STEP_MS, (c, s, b) -> rafaleStab(c, s, b, k)));
                    }
                    steps.add(new Step(RAFALE_FINAL_MS, this::rafaleFinal));
                }
                default -> steps.add(new Step(STRIKE_MS, (c, s, b) -> {                  // 6 : Percee du Prunier
                    strike(c, s, b, 4, 0.45f);
                    SpellFx.shake(feet, 10, "Impact_Light", s);
                    SpellFx.fx(FX_PLUM, new Vector3d(feet).add(0, 0.6, 0), 1.2f, s, b);   // les fleurs se levent aux pieds
                    air(s, b, look, 3.0, 0.34f);
                    tipFlash(s, b, 1.1f);
                    SpellFx.fx(FX_RING + tier, tip(), 1.0f, s, b);
                    needle(c, s, b, eye(), look, RANGE[5], 1, 1.0f, 0.32f, 1f, 260, 420, true);
                }));
            }
        }

        float dmg() { return DAMAGE[tier - 1] * power; }

        void later(long delayMs, Act act) { added.add(new Step(System.currentTimeMillis() - startAt + delayMs, act)); }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            fades.tick(now, s, b);
            long age = now - startAt;
            for (var it = steps.iterator(); it.hasNext(); ) {
                Step st = it.next();
                if (age < st.at()) continue;
                it.remove();
                if (SpellFx.position(c, s) == null) return true;
                st.act().run(c, s, b);
            }
            steps.addAll(added);
            added.clear();
            return steps.isEmpty() && fades.isEmpty();
        }

        // ----- reperes -----
        private void aim(Ref<EntityStore> c, Store<EntityStore> s) {
            Vector3d f = SpellFx.position(c, s);
            if (f == null) return;
            feet = f;
            eye = new Vector3d(f).add(0, 1.45, 0);
            look = SpellFx.lookDirection(c, s).normalize();
            flat = flat(look);
            right = new Vector3d(-flat.z, 0, flat.x);                                  // droite du joueur
        }

        private Vector3d eye() { return new Vector3d(eye); }

        /** Pointe de la lame a l'image de frappe en cours (tipOff, mesuree sur l'animation). */
        private Vector3d tip() { return at(feet, flat, tipOff); }

        // ----- gestes -----
        /** Le bras se tend : le coup part, le pied avant frappe le sol, le corps avance. */
        private void strike(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b, double step, float dust) {
            aim(c, s);
            stabSound(b);
            SpellFx.push(c, new Vector3d(flat).mul(step), false, b);
            SpellFx.fx(FX_DUST, new Vector3d(feet).add(new Vector3d(flat).mul(0.9)).add(0, 0.05, 0), dust, s, b);
        }

        private void stabSound(CommandBuffer<EntityStore> b) {
            SpellFx.sound("SFX_Light_Melee_T2_Lunge", eye, b);
        }

        /** Souffle d'air de l'estoc, teinte du Ki : de la main vers l'avant, s'amincit en 0,26 s. */
        private void air(Store<EntityStore> s, CommandBuffer<EntityStore> b, Vector3d dir, double length, float width) {
            Vector3d from = tip(), to = new Vector3d(from).add(new Vector3d(dir).mul(length));
            fades.add(s, b, AIR + tier, from, to, width, width * 0.3f, 40, 220);
        }

        private void tipFlash(Store<EntityStore> s, CommandBuffer<EntityStore> b, float scale) {
            SpellFx.fx(FX_TIP + tier, tip(), scale, s, b);
        }

        /** Poussieres de Ki laissees le long de la ligne, de d0 a d1, toutes les step. */
        private void trace(Store<EntityStore> s, CommandBuffer<EntityStore> b, Vector3d from, Vector3d dir, double d0, double d1, double step) {
            for (double d = d0; d <= d1; d += step)
                SpellFx.fx(FX_TRACE + tier, new Vector3d(from).add(new Vector3d(dir).mul(d)), 0.8f + 0.05f * tier, s, b);
        }

        /**
         * Estoc lui-meme (paliers 1 et 2) : max ennemis sur la ligne, factor des degats, kb : recul ; pushLast : le dernier
         * touche est projete (Tsuki du Ruisseau).
         */
        private void melee(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b, double range, double radius, int max,
                           float factor, double kb, boolean pushLast) {
            List<Ref<EntityStore>> hits = line(c, eye, look, range, radius, s);
            int n = Math.min(max, hits.size());
            for (int i = 0; i < n; i++) {
                Ref<EntityStore> e = hits.get(i);
                boolean last = pushLast && i == n - 1;
                impact(e, s, b, 0.8f + tier * 0.12f);
                Vector3d push = new Vector3d(flat).mul(last ? 9 : kb).add(0, last ? 2 : 0.5, 0);
                SpellFx.damage(c, e, "Physical", dmg() * factor, push, "Impact_Strong", b);
            }
        }

        private void impact(Ref<EntityStore> e, Store<EntityStore> s, CommandBuffer<EntityStore> b, float scale) {
            Vector3d p = SpellFx.position(e, s);
            if (p == null) return;
            SpellFx.fx(FX_HIT + tier, new Vector3d(p).add(0, 1.1, 0), scale, s, b);
            if (tier >= 4) SpellFx.fx(FX_CUT + tier, new Vector3d(p).add(0, 1.1, 0), scale * 0.8f, s, b);
        }

        /** Expert : ruee rasante, images remanentes ; la coupe eclate apres le passage. */
        private void ruee(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            tipOff = TIP_RUEE;
            aim(c, s);
            stabSound(b);
            Vector3d start = new Vector3d(feet), dir = new Vector3d(flat);
            SpellFx.push(c, new Vector3d(flat).mul(22).add(0, 0.6, 0), false, b);
            SpellFx.effect(c, "Pas_Leger_Sillage_3", b);
            SpellFx.protectFromFall(c, 2_000, s);
            SpellFx.shake(feet, 8, "Impact_Light", s);
            SpellFx.fx(FX_SOUL + tier, start, 1.0f, s, b);                               // image remanente au depart
            SpellFx.fx(FX_DUST, feet, 0.8f, s, b);
            SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 1.0f, s, b);
            tipFlash(s, b, 0.9f);
            air(s, b, look, RANGE[2] + 2, 0.45f);
            trace(s, b, tip(), look, 1.0, RANGE[2] + 2, 0.7);
            for (double d = 1.5; d <= RANGE[2]; d += 1.5)                                // la terre se souleve sous la ruee
                SpellFx.fx(FX_DUST, new Vector3d(start).add(new Vector3d(dir).mul(d)).add(0, 0.05, 0), 0.4f, s, b);
            List<Ref<EntityStore>> hits = line(c, eye, look, RANGE[2] + 1, 1.4, s);
            for (Ref<EntityStore> e : hits) {                                             // 60 % au passage
                Vector3d p = SpellFx.position(e, s);
                if (p != null) SpellFx.fx(FX_HIT + tier, new Vector3d(p).add(0, 1.1, 0), 0.8f, s, b);
                SpellFx.damage(c, e, "Physical", dmg() * 0.6f, new Vector3d(flat).mul(1).add(0, 0.3, 0), "Impact_Light", b);
            }
            for (int i = 1; i <= 2; i++) {                                               // images remanentes le long de la ruee
                final double d = i * 2.2;
                later(i * 110L, (c2, s2, b2) -> SpellFx.fx(FX_SOUL + tier, new Vector3d(start).add(new Vector3d(dir).mul(d)), 0.9f, s2, b2));
            }
            later(RUEE_CUT_MS, (c2, s2, b2) -> {                                          // la coupe eclate : 40 %
                for (Ref<EntityStore> e : hits) {
                    Vector3d p = SpellFx.position(e, s2);
                    if (p == null) continue;
                    SpellFx.fx(FX_CUT + tier, new Vector3d(p).add(0, 1.1, 0), 1.1f, s2, b2);
                    SpellFx.damage(c2, e, "Physical", dmg() * 0.4f, new Vector3d(dir).mul(4).add(0, 1.5, 0), "Impact_Strong", b2);
                }
                if (!hits.isEmpty()) SpellFx.sound("SFX_Ability_Wind_Strike_Slash", new Vector3d(start).add(new Vector3d(dir).mul(3)), b2);
            });
            later(340, (c2, s2, b2) -> {                                                  // arrivee : la lame claque
                aim(c2, s2);
                tipFlash(s2, b2, 1.1f);
                SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 1.3f, s2, b2);
                SpellFx.shake(feet, 8, "Impact_Light", s2);
                SpellFx.sound("SFX_Light_Melee_T2_Lunge", eye, b2);
            });
        }

        /** Mille Aiguilles : un estoc de la rafale, une aiguille courte dans un cone autour du regard. */
        private void rafaleStab(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b, int k) {
            tipOff = TIP_RAFALE;
            aim(c, s);
            SpellFx.sound(k % 2 == 0 ? "SFX_Light_Melee_T2_Lunge" : "SFX_Sword_T2_Swing_Down", eye, b);
            if (k % 2 == 1) SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 0.7f, s, b);
            var r = ThreadLocalRandom.current();
            Vector3d dir = new Vector3d(look).add(r.nextDouble(-0.07, 0.07), r.nextDouble(-0.05, 0.08), r.nextDouble(-0.07, 0.07)).normalize();
            Vector3d from = tip(), to = new Vector3d(from).add(new Vector3d(dir).mul(4.5));
            fades.add(s, b, TRAIT + tier, from, to, 0.24f, 0.05f, 40, 110);
            SpellFx.fx(FX_TIP + tier, to, 0.45f, s, b);
            for (Ref<EntityStore> e : line(c, eye, look, 4.5, 1.1, s)) {
                Vector3d p = SpellFx.position(e, s);
                if (p != null) SpellFx.fx(FX_HIT + tier, new Vector3d(p).add(0, 1.1, 0), 0.6f, s, b);
                SpellFx.damage(c, e, "Physical", dmg() * 0.25f, new Vector3d(look).mul(0.6), "Impact_Light", b);
            }
        }

        /** Mille Aiguilles, estoc final : l'aiguille sur 14 blocs et six aiguilles en eventail (30 %, un ennemi une fois). */
        private void rafaleFinal(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            tipOff = TIP_FINAL;
            strike(c, s, b, 4, 0.45f);
            SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 1.4f, s, b);
            SpellFx.shake(feet, 10, "Impact_Light", s);
            air(s, b, look, 3.0, 0.32f);
            tipFlash(s, b, 1.0f);
            needle(c, s, b, eye(), look, RANGE[4], 0, 0.75f, 0.14f, 1f, 180, 380, false);
            Set<Ref<EntityStore>> fanHit = new HashSet<>(line(c, eye, look, RANGE[4], 1.0, s));
            Vector3d up = new Vector3d(right).cross(look).normalize();
            World world = SpellFx.world(s);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3;
                Vector3d dir = new Vector3d(look).add(new Vector3d(right).mul(Math.cos(a) * 0.16)).add(new Vector3d(up).mul(Math.sin(a) * 0.16)).normalize();
                double len = reach(world, eye, dir, 8, 0);
                Vector3d z = new Vector3d(eye).add(new Vector3d(dir).mul(len));
                fades.add(s, b, TRAIT + tier, tip(), z, 0.3f, 0.06f, 120, 260);
                SpellFx.fx(FX_TIP + tier, z, 0.55f, s, b);
                for (Ref<EntityStore> e : line(c, eye, dir, len, 0.9, s)) {
                    if (!fanHit.add(e)) continue;
                    impact(e, s, b, 0.8f);
                    SpellFx.damage(c, e, "Physical", dmg() * 0.3f, new Vector3d(dir).mul(2).add(0, 0.5, 0), "Impact_Light", b);
                }
            }
        }

        /**
         * Trait de Ki effile (paliers 4 a 6) : de la main jusqu'au premier obstacle (mode : 0 tout bloc plein, 1 bloc plein et
         * opaque), traverse les ennemis. plum : fleurs de prunier le long du trait, qui eclosent sur les ennemis transperces.
         */
        private void needle(Ref<EntityStore> c, Store<EntityStore> s, CommandBuffer<EntityStore> b, Vector3d from, Vector3d dir, double max,
                            int mode, float w0, float w1, float scale, long hold, long life, boolean plum) {
            World world = SpellFx.world(s);
            double len = reach(world, from, dir, max, mode);
            Vector3d a = tip(), z = new Vector3d(from).add(new Vector3d(dir).mul(len));
            fades.add(s, b, TRAIT + tier, a, z, w0 * scale, w1 * scale, hold, life);
            fades.add(s, b, SPIRALE + tier, a, z, w0 * 0.45f * scale, w1 * 0.6f * scale, hold / 2, life);   // fil lumineux qui court sur le trait
            if (tier >= 6) fades.add(s, b, AURA + tier, a, z, w0 * 2.4f * scale, w1 * 2.6f * scale, hold, life + 120);
            SpellFx.sound(tier >= 6 ? "SFX_Longsword_Special_Impact" : "SFX_Ability_Wind_Strike_Slash", a, b);
            trace(s, b, a, dir, 1.0, Math.max(1.0, len - 2.2), tier >= 6 ? 1.2 : 1.6);
            if (plum) for (double d = 3; d < len; d += 3) SpellFx.fx(FX_PLUM, new Vector3d(from).add(new Vector3d(dir).mul(d)), 0.9f, s, b);
            boolean wall = len < max - 0.5;
            SpellFx.fx(FX_TIP + tier, z, wall ? 1.0f : 0.7f, s, b);
            if (wall) {                                                                    // la pointe heurte un mur
                SpellFx.fx(FX_BOOM + tier, z, 0.25f + 0.04f * tier, s, b);
                SpellFx.fx(FX_DUST, z, 0.6f, s, b);
            }
            if (plum) SpellFx.fx(FX_PLUM, z, 1.8f, s, b);
            List<Ref<EntityStore>> hits = line(c, from, dir, len, 1.0, s);
            for (Ref<EntityStore> e : hits) {
                impact(e, s, b, 1.1f);
                SpellFx.damage(c, e, "Physical", dmg() * scale, new Vector3d(dir).mul(3).add(0, 1, 0), "Impact_Strong", b);
            }
            if (plum && !hits.isEmpty()) later(BLOOM_MS, (c2, s2, b2) -> {                 // les fleurs eclosent sur les ennemis
                for (Ref<EntityStore> e : hits) {
                    Vector3d p = SpellFx.position(e, s2);
                    if (p == null) continue;
                    SpellFx.fx(FX_PLUM, new Vector3d(p).add(0, 1.2, 0), 1.4f, s2, b2);
                    SpellFx.fx(FX_CUT + tier, new Vector3d(p).add(0, 1.1, 0), 1.0f, s2, b2);
                }
                SpellFx.sound("SFX_Ability_Wind_Strike_Slash", z, b2);
            });
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { fades.clear(cb); }
    }

    // ===================================================================================================================
    // Tir du canon de Ki (Percee Celeste du Maitre Supreme, et chacune des Cinq Lances du Dieu Martial)
    // ===================================================================================================================

    record Debris(Ref<EntityStore> ref, Vector3d pos, Vector3d vel, long until) {}

    /**
     * Un tir de canon : depart a la bouche (pointe de la lame, ou pointe d'une lance), le front avance a CANNON_SPEED ; rayon et
     * deux spirales dessines en particules lumineuses (draw) ; il transperce (degats, paralysie 1 s), creuse le tunnel (blocs remis apres
     * 60 s), projette des debris, explose au bout, puis s'amincit et laisse retomber des braises de Ki.
     */
    static final class Shot {
        final Ref<EntityStore> caster;
        final int tier, segments;
        final float damage;
        final Vector3d muzzle, look, u, v;
        final double length;
        final long fireAt;
        int debrisBudget, phase = 1;                                                    // 1 : le front avance, 2 : s'efface, 3 : fini
        long endAt = -1, nextEmber;
        double front = 2.0, nextRing = 4;
        long nextDraw;
        final Set<Long> dug = new HashSet<>();
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        final List<Debris> debris = new ArrayList<>();

        Shot(Ref<EntityStore> caster, int tier, float damage, Vector3d muzzle, Vector3d dir, double range, int segments, int debrisBudget,
             float scale, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            this.caster = caster; this.tier = tier; this.damage = damage; this.segments = segments; this.debrisBudget = debrisBudget;
            this.muzzle = new Vector3d(muzzle);
            this.look = new Vector3d(dir).normalize();
            Vector3d up = Math.abs(look.y) > 0.9 ? new Vector3d(1, 0, 0) : new Vector3d(0, 1, 0);
            this.u = new Vector3d(look).cross(up).normalize();
            this.v = new Vector3d(look).cross(u).normalize();
            this.length = range;                                                       // rien n'arrete le canon
            this.fireAt = now;
            SpellFx.shake(muzzle, 45 * scale, "Impact_Strong", s);
            SpellFx.sound("SFX_Longsword_Special_Impact", muzzle, b);
            SpellFx.sound("SFX_Ability_Wind_Strike_Slash", muzzle, b);
            SpellFx.fx(FX_MUZZLE + tier, muzzle, scale, s, b);
            SpellFx.fx("Explosion_Medium", muzzle, 0.8f * scale, s, b);
            SpellFx.fx(FX_RING + tier, muzzle, 3.0f * scale, s, b);
        }

        Vector3d point(double d) { return new Vector3d(muzzle).add(new Vector3d(look).mul(d)); }

        boolean done() { return phase == 3; }

        /** true quand le tir est entierement fini (faisceaux retires, debris retombes). */
        boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            tickDebris(now, s, b);
            if (phase == 1) fire(c, now, s, b);
            else if (phase == 2) linger(now, s, b);
            if (phase == 3 && debris.isEmpty()) return true;
            return false;
        }

        private void fire(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            double before = front;
            front = Math.min(length, 2.0 + (now - fireAt) / 1000.0 * CANNON_SPEED);
            Vector3d tip = point(front);
            draw(now, 1f, s, b);
            World world = SpellFx.world(s);
            if (world != null && front > before) {                                     // tunnel
                List<int[]> cells = MartialTerrain.tube(muzzle, look, Math.max(0, before - 1), front, CANNON_RADIUS, dug);
                if (!cells.isEmpty()) {
                    String[] ids = MartialTerrain.borableIds(world, cells);
                    var r = ThreadLocalRandom.current();
                    int dust = 0;
                    for (int i = 0; i < cells.size(); i++) {
                        if (ids[i] == null) continue;
                        int[] cell = cells.get(i);
                        Vector3d at = new Vector3d(cell[0] + 0.5, cell[1] + 0.5, cell[2] + 0.5);
                        if (dust < 4 && r.nextFloat() < 0.08f) { SpellFx.fx(FX_DUST, at, 0.9f, s, b); dust++; }
                        if (debrisBudget > 0 && r.nextFloat() < 0.06f) {               // blocs arraches projetes
                            Ref<EntityStore> d = SpellFx.spawnBlock(ids[i], at, s, b);
                            if (d != null) {
                                SpellFx.notSaved(d, s, b);
                                Vector3d out = new Vector3d(at).sub(muzzle);
                                double along = out.dot(look);
                                out.sub(new Vector3d(look).mul(along));
                                if (out.lengthSquared() < 1e-4) out.set(u);
                                out.normalize().mul(r.nextDouble(6, 11)).add(new Vector3d(look).mul(r.nextDouble(4, 10))).add(0, r.nextDouble(5, 9), 0);
                                debris.add(new Debris(d, new Vector3d(at), out, now + 1_800));
                                debrisBudget--;
                            }
                        }
                    }
                    MartialTerrain.bore(world, cells, TUNNEL_RESTORE_MS);
                }
            }
            while (nextRing <= front) {                                                // ondes et poussieres de Ki le long du trajet
                Vector3d at = point(nextRing);
                SpellFx.fx(FX_TIP + tier, at, 1.6f, s, b);
                SpellFx.fx(FX_TRACE + tier, at, 2.0f, s, b);
                if (((int) nextRing) % 8 == 0) SpellFx.fx("Taranis_Electric_Burst", at, 1.0f, s, b);
                nextRing += 4;
            }
            for (Ref<EntityStore> e : line(c, muzzle, look, front, CANNON_RADIUS, s)) {  // transpercer
                if (!hit.add(e)) continue;
                Vector3d p = SpellFx.position(e, s);
                if (p != null) {
                    SpellFx.fx(FX_HIT + tier, new Vector3d(p).add(0, 1.1, 0), 2.0f, s, b);
                    SpellFx.fx(FX_CUT + tier, new Vector3d(p).add(0, 1.1, 0), 1.6f, s, b);
                    SpellFx.fx("Taranis_Electric_Burst", new Vector3d(p).add(0, 1.1, 0), 0.8f, s, b);
                }
                SpellFx.damage(c, e, "Physical", damage, new Vector3d(look).mul(9).add(0, 3, 0), "Impact_Strong", b);
                SpellFx.stun(e, "Taranis_Foudroye", 1_000, s, b);
            }
            if (front >= length) {                                                     // explosion au bout
                SpellFx.fx(FX_BOOM + tier, tip, 1.2f, s, b);
                SpellFx.fx("Explosion_Big", tip, 0.9f, s, b);
                SpellFx.fx(FX_RING + tier, tip, 3.5f, s, b);
                SpellFx.sound("SFX_Longsword_Special_Impact", tip, b);
                SpellFx.shake(tip, 30, "Impact_Strong", s);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(c, tip, 5.0, s))
                    if (hit.add(e)) SpellFx.damage(c, e, "Physical", damage * 0.5f, new Vector3d(look).mul(6).add(0, 6, 0), "Impact_Strong", b);
                phase = 2;
                endAt = now + CANNON_LINGER_MS;
            }
        }

        /** Le faisceau s'amincit puis disparait ; des braises de Ki retombent le long du trajet. */
        private void linger(long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            double k = Math.max(0, (endAt - now) / (double) CANNON_LINGER_MS);
            if (k > 0.12) draw(now, (float) k, s, b);
            if (now >= nextEmber && k > 0.2) {
                nextEmber = now + 150;
                var r = ThreadLocalRandom.current();
                SpellFx.fx(FX_TRACE + tier, point(r.nextDouble(2, Math.max(3, front))).add(r.nextDouble(-1, 1), r.nextDouble(-1, 1), r.nextDouble(-1, 1)),
                        2.2f, s, b);
            }
            if (now < endAt) return;
            phase = 3;
        }

        /**
         * Rayon dessine en particules (Percee_Rayon_T, Percee_Spirale_Point_T), reemises toutes les DRAW_MS : les faisceaux du
         * moteur sont eclaires par le monde et devenaient noirs dans le tunnel creuse (retour du 2026-11-12). k : 1 au tir, puis
         * decroit pendant que le rayon s'efface (maillons plus petits et plus espaces, spirales coupees sous 0,45).
         */
        private void draw(long now, float k, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            if (now < nextDraw) return;
            nextDraw = now + DRAW_MS;
            double step = k > 0.6 ? 1.6 : 2.4;
            for (double d = 0.4; d <= front; d += step) {
                float taper = (float) (1.0 - 0.3 * d / Math.max(1.0, length));
                SpellFx.fx(RAY + tier, point(d), k * taper, s, b);
            }
            if (front < 9) SpellFx.fx(FX_TIP + tier, muzzle, 1.2f * k, s, b);           // onde de la bouche tant que le front part
            if (k < 0.45) return;
            double seg = Math.max(0.6, front / segments), phaseT = (now - fireAt) / 1000.0 * 14.0, radius = CANNON_RADIUS * 0.9;
            for (int h = 0; h < 2; h++)
                for (int i = 0; i <= segments; i++)
                    SpellFx.fx(RAY_SPIRAL + tier, helix(i * seg, phaseT + h * Math.PI, radius), k, s, b);
        }

        private Vector3d helix(double d, double phase, double radius) {
            double a = d * 0.55 + phase;
            return new Vector3d(muzzle).add(new Vector3d(look).mul(d)).add(new Vector3d(u).mul(Math.cos(a) * radius))
                    .add(new Vector3d(v).mul(Math.sin(a) * radius));
        }

        private void tickDebris(long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            for (var it = debris.iterator(); it.hasNext(); ) {
                Debris d = it.next();
                if (now >= d.until() || d.ref() == null || !d.ref().isValid()) { SpellFx.remove(d.ref(), b); it.remove(); continue; }
                double dt = 0.05;
                d.vel().y -= 22 * dt;
                d.pos().add(new Vector3d(d.vel()).mul(dt));
                SpellFx.hold(d.ref(), d.pos(), null, s);
            }
        }

        void cleanup(CommandBuffer<EntityStore> cb) {
            for (Debris d : debris) SpellFx.remove(d.ref(), cb);
            debris.clear();
        }
    }

    // ===================================================================================================================
    // Palier 7 : Percee Celeste, le canon de Ki
    // ===================================================================================================================

    /** 1,2 s de charge en garde gatotsu (Ki aspire vers la pointe de la lame), puis le tir part de la pointe, bras tendu. */
    static final class Cannon implements Instance {
        final int tier;
        final Ref<EntityStore> caster;
        final float power;
        final long startAt;
        int pulses;
        Shot shot;

        Cannon(Ref<EntityStore> caster, int tier, float power, long now, Vector3d feet, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            this.caster = caster; this.tier = tier; this.power = power; this.startAt = now;
            AnimationUtils.playAnimation(caster, AnimationSlot.Action, SET, "TsukiCharge", true, s);
            SpellFx.protectFromFall(caster, 5_000, s);
            SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", feet, b);
            SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 1.6f, s, b);
            SpellFx.fx(FX_COLUMN + tier, new Vector3d(feet).add(0, 0.05, 0), 1.2f, s, b);
        }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            if (shot != null) return shot.tick(c, now, s, b);
            Vector3d feet = SpellFx.position(c, s);
            if (feet == null) return true;
            Vector3d look = SpellFx.lookDirection(c, s).normalize(), flat = flat(look);
            Vector3d tip = at(feet, flat, TIP_GATOTSU);                                 // pointe de la lame, garde gatotsu
            double p = Math.min(1.0, (now - startAt) / (double) CANNON_CHARGE_MS);
            if (now - startAt >= pulses * 150L) {
                pulses++;
                var r = ThreadLocalRandom.current();
                SpellFx.fx(FX_CONVERGE + tier, tip, 0.8f + (float) p * 0.7f, s, b);
                if (pulses % 2 == 0) SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 1.4f + (float) p * 2.4f, s, b);
                SpellFx.fx(FX_DUST, new Vector3d(feet).add(r.nextDouble(-3, 3), 0.1, r.nextDouble(-3, 3)), 0.6f, s, b);
                if (pulses == 4) {
                    SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", tip, b);
                    SpellFx.shake(feet, 20, "Impact_Light", s);
                }
                if (p >= 0.6) SpellFx.fx(FX_TIP + tier, tip, 0.6f + (float) p * 0.8f, s, b);
                if (p >= 0.75) SpellFx.fx("Taranis_Electric_Burst", tip, 0.5f + (float) p * 0.4f, s, b);
            }
            if (p < 1.0) return false;
            AnimationUtils.playAnimation(c, AnimationSlot.Action, SET, "TsukiCeleste", true, s);
            SpellFx.push(c, new Vector3d(look).mul(-6).add(0, 0.5, 0), false, b);       // recul
            SpellFx.fx(FX_COLUMN + tier, new Vector3d(feet).add(0, 0.05, 0), 2.0f, s, b);
            shot = new Shot(c, tier, DAMAGE[tier - 1] * power, at(feet, flat, TIP_CELESTE), look, RANGE[tier - 1], 12, 28, 1f, now, s, b);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (shot != null) shot.cleanup(cb);
        }
    }

    // ===================================================================================================================
    // Palier 8 : Cinq Lances Celestes
    // ===================================================================================================================

    /**
     * Cinq estocs eclair dans le vide (animation TsukiCinq) : chacun fait naitre, depuis la pointe de la lame, une lance de Ki
     * (faisceaux effiles : coeur, gaine) qui flotte derriere le lanceur, en arc au-dessus de ses epaules, et vise sans cesse le
     * point vise. Le bras designe alors la cible ; apres un temps (FIRE_FIRST_MS), les lances se chargent puis partent une a une
     * (FIRE_STEP_MS) : chacune tire un canon de Ki complet, celui du Maitre Supreme (Shot), depuis sa pointe vers la cible.
     */
    static final class Lances implements Instance {
        static final int T = 8;
        static final long[] STAB_MS = {67, 167, 267, 367, 467};
        static final long FIRE_FIRST_MS = 1_150, FIRE_STEP_MS = 420, PRECHARGE_MS = 300, MAX_MS = 8_000;
        static final double HOVER_LENGTH = 2.8, BACK = 0.9;
        /** Place de chaque lance : decalage lateral (droite > 0) et hauteur au-dessus des pieds, dans l'ordre de naissance. */
        static final double[][] SLOT = {{0, 3.4}, {-1.3, 3.05}, {1.3, 3.05}, {-2.4, 2.55}, {2.4, 2.55}};

        final Ref<EntityStore> caster;
        final float power;
        final long startAt;
        final Lance[] lances = new Lance[5];
        final List<Shot> shots = new ArrayList<>();
        final Fades fades = new Fades();
        int born, fired, charged;
        boolean started;

        static final class Lance {
            final int slot;
            Ref<EntityStore> core, aura;
            Vector3d tail, tip;
            long nextSpark;

            Lance(int slot) { this.slot = slot; }
        }

        Lances(Ref<EntityStore> caster, float power, long now) {
            this.caster = caster; this.power = power; this.startAt = now;
        }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            fades.tick(now, s, b);
            shots.removeIf(sh -> sh.tick(c, now, s, b));
            Vector3d feet = SpellFx.position(c, s);
            if (feet == null) return true;
            long age = now - startAt;
            Vector3d look = SpellFx.lookDirection(c, s).normalize(), flat = flat(look), right = new Vector3d(-flat.z, 0, flat.x);
            Vector3d eye = new Vector3d(feet).add(0, 1.45, 0);
            if (!started) {                                                              // premier tick : le mouvement commence
                started = true;
                AnimationUtils.playAnimation(c, AnimationSlot.Action, SET, "TsukiCinq", true, s);
                SpellFx.fx(FX_RING + T, new Vector3d(feet).add(0, 0.05, 0), 1.6f, s, b);
                SpellFx.fx(FX_COLUMN + T, new Vector3d(feet).add(0, 0.05, 0), 1.0f, s, b);
                SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", eye, b);
                SpellFx.protectFromFall(c, 4_000, s);
            }
            Vector3d target = aimPoint(c, eye, look, s);
            // naissance : un estoc dans le vide ; le Ki part de la pointe de la lame et forme la lance derriere le lanceur
            while (born < 5 && age >= STAB_MS[born]) {
                Lance l = new Lance(born);
                lances[born] = l;
                Vector3d bladeTip = at(feet, flat, TIP_CINQ[born]);
                place(l, feet, flat, right, target, now);
                l.core = SpellFx.spawnBeam(TRAIT + T, l.tail, l.tip, 0.55f, 0.05f, MAX_MS + 2_000, s, b);
                l.aura = SpellFx.spawnBeam(AURA + T, l.tail, l.tip, 1.5f, 0.25f, MAX_MS + 2_000, s, b);
                SpellFx.sound(born % 2 == 0 ? "SFX_Light_Melee_T2_Lunge" : "SFX_Sword_T2_Swing_Down", eye, b);
                SpellFx.fx(FX_TIP + T, bladeTip, 0.7f, s, b);
                Vector3d mid = new Vector3d(l.tail).add(l.tip).mul(0.5);
                fades.add(s, b, SPIRALE + T, bladeTip, mid, 0.25f, 0.12f, 60, 240);       // le Ki de l'estoc file vers sa lance
                SpellFx.fx(FX_READY + T, mid, 1.1f, s, b);
                born++;
            }
            // lances en attente : elles suivent le lanceur et visent la cible
            for (int i = fired; i < born; i++) {
                Lance l = lances[i];
                place(l, feet, flat, right, target, now);
                SpellFx.beam(l.core, TRAIT + T, l.tail, l.tip, 0.55f, 0.05f, s);
                SpellFx.beam(l.aura, AURA + T, l.tail, l.tip, 1.5f, 0.25f, s);
                if (now >= l.nextSpark) {
                    l.nextSpark = now + 220;
                    SpellFx.fx(FX_TRACE + T, new Vector3d(l.tail).add(l.tip).mul(0.5), 0.9f, s, b);
                }
            }
            // la lance suivante se charge (le Ki converge sur sa pointe), puis part : un canon de Ki complet
            while (charged < born && age >= FIRE_FIRST_MS + charged * FIRE_STEP_MS - PRECHARGE_MS) {
                Lance l = lances[charged++];
                SpellFx.fx(FX_CONVERGE + T, l.tip, 0.9f, s, b);
                SpellFx.fx(FX_READY + T, l.tip, 1.0f, s, b);
                SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", l.tip, b);
            }
            while (fired < born && age >= FIRE_FIRST_MS + fired * FIRE_STEP_MS) {
                Lance l = lances[fired++];
                Vector3d dir = new Vector3d(target).sub(l.tip);
                if (dir.lengthSquared() < 1e-4) dir.set(look);
                SpellFx.remove(l.core, b);
                SpellFx.remove(l.aura, b);
                l.core = l.aura = null;
                shots.add(new Shot(c, T, DAMAGE[7] * power, l.tip, dir, RANGE[7], 8, 12, 0.7f, now, s, b));
            }
            return (fired >= 5 && shots.isEmpty() && fades.isEmpty()) || age > MAX_MS;
        }

        /** Place une lance en attente : derriere le lanceur, au-dessus des epaules, legere oscillation, pointe vers la cible. */
        private void place(Lance l, Vector3d feet, Vector3d flat, Vector3d right, Vector3d target, long now) {
            double[] sl = SLOT[l.slot];
            double bob = Math.sin((now + l.slot * 310) / 210.0) * 0.07;
            Vector3d center = new Vector3d(feet).sub(new Vector3d(flat).mul(BACK)).add(new Vector3d(right).mul(sl[0])).add(0, sl[1] + bob, 0);
            Vector3d dir = new Vector3d(target).sub(center);
            if (dir.lengthSquared() < 1e-4) dir.set(flat);
            dir.normalize();
            l.tail = new Vector3d(center).sub(new Vector3d(dir).mul(HOVER_LENGTH / 2));
            l.tip = new Vector3d(center).add(new Vector3d(dir).mul(HOVER_LENGTH / 2));
        }

        /** Point vise : le premier ennemi sur la ligne du regard, sinon le premier bloc opaque, sinon RANGE devant. */
        private static Vector3d aimPoint(Ref<EntityStore> c, Vector3d eye, Vector3d look, Store<EntityStore> s) {
            List<Ref<EntityStore>> foes = line(c, eye, look, RANGE[7], 1.5, s);
            if (!foes.isEmpty()) {
                Vector3d p = SpellFx.position(foes.get(0), s);
                if (p != null) return p.add(0, 1.0, 0);
            }
            double d = reach(SpellFx.world(s), eye, look, RANGE[7], 1);
            return new Vector3d(eye).add(new Vector3d(look).mul(d));
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Lance l : lances) {
                if (l == null) continue;
                SpellFx.remove(l.core, cb);
                SpellFx.remove(l.aura, cb);
            }
            for (Shot sh : shots) sh.cleanup(cb);
            fades.clear(cb);
        }
    }
    // ===================================================================================================================
    // Outils
    // ===================================================================================================================

    /** Ennemis dont le centre est a moins de radius de la ligne from + dir * [0, length], du plus proche au plus loin. */
    static List<Ref<EntityStore>> line(Ref<EntityStore> caster, Vector3d from, Vector3d dir, double length, double radius, Store<EntityStore> s) {
        Vector3d f = new Vector3d(dir).normalize();
        Vector3d mid = new Vector3d(from).add(new Vector3d(f).mul(length / 2));
        List<Ref<EntityStore>> out = new ArrayList<>();
        Map<Ref<EntityStore>, Double> along = new java.util.HashMap<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, length / 2 + radius + 1.5, s)) {
            Vector3d p = SpellFx.position(e, s);
            if (p == null) continue;
            Vector3d to = new Vector3d(p).add(0, 0.9, 0).sub(from);
            double t = to.dot(f);
            if (t < -0.5 || t > length + 0.5) continue;
            Vector3d perp = new Vector3d(to).sub(new Vector3d(f).mul(t));
            if (perp.length() > radius + 0.6) continue;
            along.put(e, t);
            out.add(e);
        }
        out.sort(Comparator.comparingDouble(along::get));
        return out;
    }

    /**
     * Distance jusqu'au premier obstacle dans la direction dir (max au plus). mode 0 : tout bloc plein ; 1 : bloc plein et
     * opaque (feuilles, herbe, verre traverses) ; 2 : rien n'arrete.
     */
    static double reach(World world, Vector3d from, Vector3d dir, double max, int mode) {
        if (world == null || mode == 2) return max;
        Vector3d f = new Vector3d(dir).normalize();
        for (double d = 0.6; d <= max; d += 0.3) {
            Vector3d p = new Vector3d(from).add(new Vector3d(f).mul(d));
            try {
                BlockType t = WorldBlockUtil.getBlockType(world, (int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.z));
                if (t == null || t.getMaterial() != BlockMaterial.Solid) continue;
                if (mode == 1 && t.getOpacity() != com.hypixel.hytale.protocol.Opacity.Solid) continue;
                return Math.max(1.0, d);
            } catch (Exception ignored) { }
        }
        return max;
    }

    private static Vector3d flat(Vector3d v) {
        Vector3d f = new Vector3d(v.x, 0, v.z);
        return f.lengthSquared() < 1e-6 ? new Vector3d(0, 0, 1) : f.normalize();
    }
}
