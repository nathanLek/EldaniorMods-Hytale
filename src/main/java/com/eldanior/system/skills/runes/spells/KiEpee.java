package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Ki de l'Epee (Combat, voie martiale du Clan Luo) : competence a bascule, comme la Rune d'Aura. Un clic eveille le
 * Ki, un second clic l'apaise ; il s'eteint aussi quand le mana ne suffit plus a le nourrir (cout preleve chaque seconde).
 * Tant qu'il est actif, avec une arme a lame (epee, katana, dague ; toute arme et mains nues au palier 8) :
 *   1 Apprenti        Eveil du Ki : +8 % de degats, vitesse x1,05, aura blanc pale ;
 *   2 Disciple        Circulation : +12 %, -10 % de degats subis, endurance +3 %/s, eclat de Ki au 4e coup (+20 %) ;
 *   3 Expert          Lame impregnee : +18 %, degats de Ki +10 % a chaque coup (traversent l'armure), eclat +30 % ;
 *   4 Maitre          Projection : +22 % (plus de lame de Ki au 4e coup depuis le 2026-11-10 : lames au Saint et au-dela) ;
 *   5 Grand Maitre    Ki solidifie : +26 %, Ki +15 %, -15 % de degats subis, lames violettes ;
 *   6 Saint           Transcendance : +32 %, une lame de Ki a chaque coup, 20 % d'echo (seconde coupe), en garde la lame
 *                     tranche les projectiles ; Ki d'or ;
 *   7 Maitre Supreme  Hwakyung : +40 %, Ki +20 %, -20 %, lames geantes blanc nacre, fleur de Ki au 4e coup (rayon 3) ;
 *   8 Dieu Martial    Retour a la simplicite : +50 %, Ki +25 %, aura presque invisible, coupe de zone (6 blocs) au 4e coup.
 * Le 4e coup = chaque 4e coup enchaine (moins de 1,5 s entre deux coups). Mana par seconde : 3, 4, 5, 6, 7, 8, puis 5 et 4.
 * Le palier est relu chaque seconde (une promotion prend effet sans relancer le Ki). Activer la Meditation martiale coupe le Ki.
 * Branches : CombatStatsSystem (damageMultiplier), DivineDamageSystem -> onOwnerHit / onOwnerHurt.
 * Fiche de conception : scripts/runes/specs/Rune_KiEpee.json
 */
public final class KiEpee implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    static final int[] MANA = {3, 4, 5, 6, 7, 8, 5, 4};
    static final float[] BONUS = {0.08f, 0.12f, 0.18f, 0.22f, 0.26f, 0.32f, 0.40f, 0.50f};
    static final float[] PIERCE = {0f, 0f, 0.10f, 0.10f, 0.15f, 0.15f, 0.20f, 0.25f};
    static final float[] REDUCTION = {0f, 0.10f, 0.10f, 0.10f, 0.15f, 0.15f, 0.20f, 0.20f};
    static final long SECOND_MS = 1_000, COMBO_MS = 1_500, BLADE_GAP_MS = 250, ECHO_DELAY_MS = 300;
    static final float STAMINA_PER_S = 0.03f, ECHO_CHANCE = 0.20f;
    /** Degats de la lame de Ki lancee a chaque coup (Saint, Maitre Supreme, Dieu Martial). */
    static final float[] SWING_BLADE = {0, 0, 0, 0, 0, 45, 60, 75};
    /** Taille des lames de Ki lancees a chaque coup (retour du 2026-11-10 : la taille change au Maitre Supreme et au Dieu Martial). */
    static final float[] BLADE_SCALE = {1f, 1f, 1f, 1f, 1f, 1.0f, 1.8f, 2.6f};
    static final double FISSURE = 12.0;

    /**
     * Sort KiEpeeCoup_<H|G|D|V>, insere dans chaque coup du kata des katanas Luo (gen_luo_katanas.py, KATA_ORIENT) : si le Ki
     * de l'Epee est actif au Saint ou au-dela, le coup projette une lame de Ki inclinee comme le coup (horizontale, vers la
     * gauche, vers la droite, verticale ; retour du 2026-11-10 : elles etaient toutes horizontales). 250 ms minimum entre deux.
     * KiEpeeCoup (sans suffixe) = horizontale, garde pour les interactions plus anciennes.
     */
    public static final class Coup implements RuneSpell {
        private final String orient;
        public Coup(String orient) { this.orient = orient; }
        public static List<Coup> all() { return List.of(new Coup(""), new Coup("H"), new Coup("G"), new Coup("D"), new Coup("V")); }

        @Override public String id() { return orient.isEmpty() ? "KiEpeeCoup" : "KiEpeeCoup_" + orient; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            LuoPassives.payCoup(caster, store);                                 // chaque coup du kata : 5 % d'endurance
            Active ki = RuneSpellRuntime.find(SpellFx.uuid(caster, store), Active.class);
            if (ki == null || ki.stop || ki.tier < 6 || !MartialKi.holdsBlade(caster, store, ki.tier)) return null;
            ki.swing(caster, System.currentTimeMillis(), orient.isEmpty() ? "H" : orient, 1f, store, cb);
            return null;
        }
    }

    /**
     * Sort KiEpeeCharge, insere dans le coup charge (degaine foudroyant, gen_luo_katanas.py) : retour du 2026-11-10, le coup
     * charge lance lui aussi une lame de Ki, pour les classes qui en lancent (Maitre Martial et au-dela), plus grande (x1,4)
     * et plus forte qu'un coup du kata.
     */
    public static final class Charge implements RuneSpell {
        @Override public String id() { return "KiEpeeCharge"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!LuoPassives.payHeld(caster, store)) return null;               // coup maintenu : 50 % d'endurance
            Active ki = RuneSpellRuntime.find(SpellFx.uuid(caster, store), Active.class);
            // retour du 2026-11-10 (nuit, 2e) : lames de Ki seulement a partir du Saint Martial (le Maitre en lancait une)
            if (ki == null || ki.stop || ki.tier < 6 || !MartialKi.holdsBlade(caster, store, ki.tier)) return null;
            ki.lastBlade = 0;                                                   // le coup charge passe toujours
            ki.swing(caster, System.currentTimeMillis(), "H", CHARGE_SCALE, store, cb);
            return null;
        }
    }
    /**
     * Lame de Ki du palier (Saint et au-dela) lancee dans le regard, sans l'ecart minimal entre deux lames : rafale du Bokken
     * (une lame par mouvement, demande du 2026-11-10) et PNJ martiaux. null sous le Saint Martial. Le sort appelant doit faire
     * avancer l'instance rendue (tick) jusqu'a ce qu'elle se termine.
     */
    static Instance launchBlade(Ref<EntityStore> caster, int tier, Vector3d dir, String orient, float boost,
                                java.util.function.Consumer<Ref<EntityStore>> onHit, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (tier < 6) return null;
        Vector3d feet = SpellFx.position(caster, store);
        if (feet == null || dir == null) return null;
        float scale = BLADE_SCALE[Math.min(8, tier) - 1] * boost;
        if (tier >= 8) MartialTerrain.fissure(SpellFx.world(store), feet, SpellFx.forward(caster, store), FISSURE * boost, store, cb);
        return Tenryumaru.kiBlade(caster, feet, dir, MartialKi.bladeKey(tier, orient), 38, 10 * boost, 1.5 * scale,
                SWING_BLADE[Math.min(8, tier) - 1] * boost, scale, onHit, store, cb);
    }

    /** Le Ki de l'Epee deploye de ce lanceur (null s'il ne l'est pas). */
    static Active active(Ref<EntityStore> caster, Store<EntityStore> store) {
        Active ki = RuneSpellRuntime.find(SpellFx.uuid(caster, store), Active.class);
        return ki == null || ki.stop ? null : ki;
    }

    static final float CHARGE_SCALE = 1.4f;
    /** Duree d'une attaque d'une autre arme au-dela de laquelle elle compte comme un coup maintenu. */
    static final long HELD_MS = 800;
    /** Degats de la lame du coup charge pour Maitre et Grand Maitre (au Saint et au-dela : SWING_BLADE x CHARGE_SCALE). */
    static final float CHARGE_BLADE_LOW = 40f;
    static final double MELEE_REACH = 6.0, FLOWER_RADIUS = 3.0, ZONE_RADIUS = 6.0;

    // ===== Assets =====
    static final String VISUAL = "Ki_Visual_";
    private static final String FX_BURST = "Impact_Sword_Charged", FX_GOLD = "Gilgamesh_Sword_Burst", FX_RING = "Eldanior_Pas_Celeste";
    private static final String SFX_WAKE = "SFX_Ability_Wind_Strike_Imbue", SFX_SLASH = "SFX_Ability_Wind_Strike_Slash",
            SFX_HIT = "SFX_Sword_T2_Impact", SFX_BIG = "SFX_Longsword_Special_Impact";

    @Override public String id() { return "KiEpee"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Active running = RuneSpellRuntime.find(uuid, Active.class);
        if (running != null) {                                                 // second clic : le Ki s'apaise
            running.stop = true;
            MartialKi.notify(caster, store, "<color:gray>Votre Ki s'apaise.</color>", NotificationStyle.Default);
            return null;
        }
        MeditationMartiale.Calm calm = RuneSpellRuntime.find(uuid, MeditationMartiale.Calm.class);
        if (calm != null) calm.stop = true;                                    // on ne medite pas le Ki deploye
        int tier = MartialKi.tier(caster, store);
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < MANA[tier - 1]) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana pour éveiller votre Ki.</color>", NotificationStyle.Warning);
            return null;
        }
        Vector3d pos = SpellFx.position(caster, store);
        if (pos != null) {
            SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_WAKE, pos, cb);
        }
        SpellFx.effect(caster, VISUAL + tier, cb);
        MartialKi.notify(caster, store, "<color:aqua>Ki de l'Épée</color> <color:gray>— " + VoieMartiale.rank(tier)
                + " (" + MANA[tier - 1] + " mana/s)</color>", NotificationStyle.Success);
        return new Active(caster, tier, System.currentTimeMillis());
    }

    /** Multiplicateur de degats du Ki actif (CombatStatsSystem), 1 sans Ki ou sans arme a lame. */
    public static float damageMultiplier(Ref<EntityStore> attacker, Store<EntityStore> store) {
        if (attacker == null || !attacker.isValid()) return 1f;
        Active ki = RuneSpellRuntime.find(SpellFx.uuid(attacker, store), Active.class);
        if (ki == null || ki.stop || !MartialKi.holdsBlade(attacker, store, ki.tier)) return 1f;
        return 1f + BONUS[ki.tier - 1];
    }

    static final class Active implements Instance {
        final Ref<EntityStore> caster;
        volatile boolean stop;
        volatile int tier;
        volatile boolean guarding;
        long lastSecond, lastHit, lastBlade;
        /** Attaques en cours (id de chaine -> debut) : nouvelle attaque = lame (Saint et +), attaque tenue = coup charge. */
        final Map<Integer, Long> chainStart = new java.util.HashMap<>();
        int combo;
        final Queue<Object[]> hits = new ConcurrentLinkedQueue<>();        // {cible, montant}
        final Map<Ref<EntityStore>, Long> own = new ConcurrentHashMap<>();  // cibles touchees par le Ki lui-meme
        final List<Object[]> echoes = new ArrayList<>();                    // {cible, montant, instant}
        final List<Instance> blades = new ArrayList<>();

        Active(Ref<EntityStore> caster, int tier, long now) { this.caster = caster; this.tier = tier; this.lastSecond = now; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            Long until = own.get(target);
            if (until != null && until > System.currentTimeMillis()) return;    // coup du Ki lui-meme
            if (hits.size() < 32) hits.add(new Object[]{target, amount});
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (stop) return amount;
            if (tier >= 6 && guarding && causeId != null && causeId.toLowerCase().contains("projectile")) return 0f;   // projectile tranche
            return amount * (1f - REDUCTION[tier - 1]);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (stop) return true;
            if (now - lastSecond >= SECOND_MS) {
                lastSecond = now;
                tier = MartialKi.tier(caster, store);
                int cost = MANA[tier - 1];
                if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
                    MartialKi.notify(caster, store, "<color:gray>Votre Ki s'éteint : mana épuisé.</color>", NotificationStyle.Warning);
                    return true;
                }
                MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -cost, store);
                SpellFx.effect(caster, VISUAL + tier, cb);
                if (tier >= 2) SpellFx.restore(caster, DefaultEntityStatTypes.getStamina(), STAMINA_PER_S, cb);
                own.values().removeIf(t -> t < now);
            }
            guarding = tier >= 6 && !SpellFx.chains(caster, store, InteractionType.Secondary).isEmpty();
            boolean blade = MartialKi.holdsBlade(caster, store, tier);
            Vector3d me = SpellFx.position(caster, store);
            // Saint et au-dela : chaque coup d'une autre arme lance une lame horizontale. Les katanas Luo lancent la leur par
            // KiEpeeCoup_<inclinaison> (kata) : les compter ici prenait leur place (ecart de 250 ms) et rendait toutes les
            // lames horizontales (retour du 2026-11-10, soir).
            String held = ArtefactWeapons.held(caster, store);
            if (tier >= 6 && blade && (held == null || LuoKatanas.of(held) == null)) {
                java.util.Set<Integer> current = SpellFx.chains(caster, store, InteractionType.Primary);
                for (Integer id : current)
                    if (chainStart.putIfAbsent(id, now) == null) { swing(caster, now, "H", 1f, store, cb); break; }
                // coup maintenu (retour du 2026-11-10 nuit) : une attaque tenue plus de HELD_MS qui se termine = coup charge,
                // lame de Ki x1,4 des le Maitre Martial (les katanas Luo et Tenryumaru ont leur sort KiEpeeCharge)
                for (var it = chainStart.entrySet().iterator(); it.hasNext(); ) {
                    var en = it.next();
                    if (current.contains(en.getKey())) continue;
                    it.remove();
                    if (now - en.getValue() >= HELD_MS) { lastBlade = 0; swing(caster, now, "H", CHARGE_SCALE, store, cb); }
                }
            } else chainStart.clear();
            for (Object[] h; (h = hits.poll()) != null; ) {
                @SuppressWarnings("unchecked") Ref<EntityStore> target = (Ref<EntityStore>) h[0];
                if (!blade || me == null || target == null || !target.isValid()) continue;
                Vector3d p = SpellFx.position(target, store);
                if (p == null || p.distance(me) > MELEE_REACH) continue;        // seulement les coups de l'arme
                onStrike(caster, target, p, (float) h[1], now, store, cb);
            }
            for (Iterator<Object[]> it = echoes.iterator(); it.hasNext(); ) {
                Object[] e = it.next();
                if (now < (long) e[2]) continue;
                it.remove();
                @SuppressWarnings("unchecked") Ref<EntityStore> target = (Ref<EntityStore>) e[0];
                Vector3d p = SpellFx.position(target, store);
                if (p == null) continue;
                SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 1.1, 0), 1.0f, store, cb);
                SpellFx.sound(SFX_HIT, p, cb);
                MartialKi.kiDamage(caster, target, (float) e[1], "Physical", null, own, cb);
            }
            for (Iterator<Instance> it = blades.iterator(); it.hasNext(); ) {
                Instance b = it.next();
                if (b.tick(caster, now, store, cb)) { b.cleanup(cb); it.remove(); }
            }
            return false;
        }

        /**
         * Saint et au-dela (retour du 2026-11-10) : chaque coup de l'arme, qu'il touche ou non, projette une lame de Ki dans le
         * regard (Tenryumaru.kiBlade) ; geante au Maitre Supreme, plus forte au Dieu Martial.
         */
        void swing(Ref<EntityStore> caster, long now, String orient, float boost, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - lastBlade < BLADE_GAP_MS) return;
            lastBlade = now;
            Vector3d feet = SpellFx.position(caster, store);
            if (feet == null) return;
            float scale = BLADE_SCALE[tier - 1] * boost;                        // lames plus grandes au Maitre Supreme, au Dieu Martial
            if (tier >= 8)                                                      // Dieu Martial : la lame fend le sol devant lui
                MartialTerrain.fissure(SpellFx.world(store), feet, SpellFx.forward(caster, store), FISSURE * boost, store, cb);
            float dmg = (tier >= 6 ? SWING_BLADE[tier - 1] : CHARGE_BLADE_LOW) * boost;
            Instance b = Tenryumaru.kiBlade(caster, feet, SpellFx.lookDirection(caster, store), MartialKi.bladeKey(tier, orient),
                    38, 10 * boost, 1.5 * scale, dmg, scale, t -> own.put(t, System.currentTimeMillis() + 400), store, cb);
            if (b != null) blades.add(b);
        }

        /** Un coup de l'arme du lanceur, avec le Ki deploye. */
        private void onStrike(Ref<EntityStore> caster, Ref<EntityStore> target, Vector3d p, float amount, long now,
                              Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            combo = now - lastHit <= COMBO_MS ? combo + 1 : 1;
            lastHit = now;
            boolean finisher = combo % 4 == 0;
            Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
            float pierce = PIERCE[tier - 1];
            if (pierce > 0) MartialKi.kiDamage(caster, target, amount * pierce, "Physical", null, own, cb);
            if (finisher && tier >= 2) {                                      // eclat de Ki
                MartialKi.kiDamage(caster, target, amount * (tier >= 3 ? 0.30f : 0.20f), "Physical", null, own, cb);
                SpellFx.fx(FX_BURST, chest, 1.3f, store, cb);
                SpellFx.sound(SFX_HIT, chest, cb);
            }
            // (lame du 4e coup du Maitre et du Grand Maitre retiree le 2026-11-10 nuit : lames de Ki au Saint et au-dela)
            if (tier >= 6 && ThreadLocalRandom.current().nextFloat() < ECHO_CHANCE)
                echoes.add(new Object[]{target, amount * 0.5f, now + ECHO_DELAY_MS});
            if (finisher && tier == 7) {                                      // fleur de Ki
                SpellFx.fx(FX_RING, new Vector3d(p).add(0, 0.2, 0), 2.0f, store, cb);
                SpellFx.fx(FX_GOLD, chest, 1.6f, store, cb);
                SpellFx.sound(SFX_BIG, chest, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, FLOWER_RADIUS, store))
                    MartialKi.kiDamage(caster, e, amount, "Physical", new Vector3d(0, 4, 0), own, cb);
            }
            if (finisher && tier >= 8) {                                      // coupe du ciel et de la terre
                Vector3d me = SpellFx.position(caster, store);
                if (me == null) return;
                SpellFx.fx(FX_RING, new Vector3d(me).add(0, 0.2, 0), 3.0f, store, cb);
                SpellFx.sound(SFX_SLASH, me, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, me, ZONE_RADIUS, store)) {
                    Vector3d q = SpellFx.position(e, store);
                    if (q == null) continue;
                    Vector3d away = new Vector3d(q.x - me.x, 0, q.z - me.z);
                    if (away.lengthSquared() < 1e-4) away.set(1, 0, 0);
                    SpellFx.fx(FX_BURST, new Vector3d(q).add(0, 1.1, 0), 1.0f, store, cb);
                    MartialKi.kiDamage(caster, e, amount * 1.2f, "Physical", away.normalize().mul(6).add(0, 2, 0), own, cb);
                }
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Instance b : blades) b.cleanup(cb);
            blades.clear();
            if (caster == null || !caster.isValid()) return;
            for (int t = 1; t <= 8; t++) {                                      // l'aura s'eteint avec le Ki
                try { EffectsManager.removeEffect(caster, VISUAL + t, cb); } catch (Exception ignored) { }
            }
        }

        @Override
        public void abort() {
            for (Instance b : blades) b.abort();
            blades.clear();
        }
    }
}
