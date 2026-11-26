package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kits propres des katanas du Clan Luo (plan valide le 2026-11-06, docs/plan-clan-luo.md) : passifs et mouvements.
 *   - Bokken du Dojo : contre parfait (garde levee dans les COUNTER_MS avant le coup : coup annule, degaine de riposte),
 *     XP de combat +50 % en le maniant (DeathXPSystem) ;
 *   - Katana de fer : garde moins couteuse (JSON, gen_luo_katanas.py) ;
 *   - Aonami : chaque coup repousse ; pas chasse lateral en fin de combo ;
 *   - Tsukikage : x1,5 dans le dos ; fondu d'ombre (invisible 0,8 s) au degaine ;
 *   - Shirasagi : le 4e coup eblouit (JSON) ; bond en arriere en fin de combo ;
 *   - Hien : combo plus rapide (JSON), Elan de l'hirondelle : +5 % de degats par coup enchaine (5 max) et vitesse de
 *     deplacement accrue des 3 coups ; ruee courte au degaine ;
 *   - commun : pas en avant au 4e coup du combo Luo (LuoPas).
 * Branches : ArtefactWeapons.modifyDamage / onHit, DivineDamageSystem (contre), DeathXPSystem (bokken), TenryumaruSystem
 * (suivi de la garde), interactions Luo_* (sorts LuoPas, LuoFinCombo, LuoDegaineMouvement).
 */
public final class LuoPassives {

    private LuoPassives() {}

    static final String BOKKEN = "Eldanior_Weapon_Sword_Katana_Bokken", AONAMI = "Eldanior_Weapon_Sword_Katana_Aonami";
    static final String TSUKIKAGE = "Eldanior_Weapon_Sword_Katana_Tsukikage", SHIRASAGI = "Artefact_Sword_Katana_Shirasagi";
    static final String HIEN = "Artefact_Sword_Katana_Hien";

    static final long COUNTER_MS = 400, ELAN_MS = 2_500;
    static final int ELAN_MAX = 5;
    static final float ELAN_STEP = 0.05f, COUNTER_DAMAGE = 30f;

    /** Debut de la garde (clic droit) de chaque joueur, mis a jour par TenryumaruSystem. */
    private static final Map<Ref<EntityStore>, Long> guardSince = new ConcurrentHashMap<>();
    /** Elan de l'hirondelle : {nombre de coups enchaines, instant du dernier}. */
    private static final Map<Ref<EntityStore>, long[]> elan = new ConcurrentHashMap<>();

    /** Katana Luo tenu (forme au fourreau ou au clair), identifiant de reference (forme au fourreau), sinon null. */
    static String katana(Ref<EntityStore> ref, Store<EntityStore> store) {
        String held = ArtefactWeapons.held(ref, store);
        LuoKatanas.Katana k = LuoKatanas.of(held);
        return k != null ? k.id() : null;
    }

    // ===================== Garde et contre parfait (Bokken) =====================

    /** Suivi de la garde : appele a chaque passage de TenryumaruSystem pour un joueur qui tient un katana Luo. */
    static void trackGuard(Ref<EntityStore> ref, Store<EntityStore> store) {
        boolean guarding = !SpellFx.chains(ref, store, InteractionType.Secondary).isEmpty();
        if (guarding) guardSince.putIfAbsent(ref, System.currentTimeMillis());
        else guardSince.remove(ref);
    }

    /** Contre parfait du Bokken : true si le coup est annule (riposte lancee). */
    public static boolean counter(Ref<EntityStore> target, Ref<EntityStore> attacker, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (attacker == null || !attacker.isValid() || !BOKKEN.equals(katana(target, store))) return false;
        Long since = guardSince.get(target);
        if (since == null || System.currentTimeMillis() - since > COUNTER_MS) return false;
        guardSince.remove(target);
        Vector3d me = SpellFx.position(target, store), foe = SpellFx.position(attacker, store);
        if (me == null || foe == null) return true;
        Vector3d dir = new Vector3d(foe).sub(me);
        dir.y = 0;
        if (dir.lengthSquared() > 1e-4) dir.normalize();
        AnimationUtils.playAnimation(target, AnimationSlot.Action, LuoKatanas.isDrawn(ArtefactWeapons.held(target, store))
                ? "Tenryumaru_Degainee" : "Tenryumaru_Fourreau", "Degainer", true, store);
        SpellFx.damage(target, attacker, "Physical", COUNTER_DAMAGE, new Vector3d(dir).mul(6).add(0, 2.5, 0), "Impact_Strong", cb);
        SpellFx.fx("Impact_Sword_Charged", new Vector3d(foe).add(0, 1.1, 0), 1.2f, store, cb);
        SpellFx.sound("SFX_Sword_T2_Impact", foe, cb);
        return true;
    }

    /** XP de combat du Bokken du Dojo : x1,5 en le maniant. */
    public static boolean trainingBonus(Ref<EntityStore> killer, Store<EntityStore> store) {
        return BOKKEN.equals(katana(killer, store));
    }

    // ===================== Degats et impacts =====================

    /** Degats apres le passif du katana Luo tenu (appele par ArtefactWeapons.modifyDamage). */
    static float modifyDamage(float damage, String katana, Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        if (TSUKIKAGE.equals(katana) && ArtefactWeapons.isBehindPublic(attacker, victim, store)) return damage * 1.5f;
        if (HIEN.equals(katana)) {
            long[] e = elan.get(attacker);
            int n = e != null && System.currentTimeMillis() - e[1] <= ELAN_MS ? (int) e[0] : 0;
            return damage * (1f + ELAN_STEP * n);
        }
        return damage;
    }

    /** Effets a l'impact du katana Luo tenu (appele par ArtefactWeapons.onHit). */
    static void onHit(String katana, Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (AONAMI.equals(katana)) {                                         // la vague repousse
            Vector3d a = SpellFx.position(attacker, store), v = SpellFx.position(victim, store);
            if (a == null || v == null) return;
            Vector3d dir = new Vector3d(v).sub(a);
            dir.y = 0;
            if (dir.lengthSquared() < 1e-4) return;
            SpellFx.push(victim, dir.normalize().mul(7).add(0, 2.5, 0), false, cb);
            SpellFx.fx("Ability_Ice_Impact", new Vector3d(v).add(0, 1, 0), 0.6f, store, cb);
        } else if (HIEN.equals(katana)) {                                    // elan de l'hirondelle
            long now = System.currentTimeMillis();
            long[] e = elan.computeIfAbsent(attacker, r -> new long[]{0, 0});
            e[0] = now - e[1] <= ELAN_MS ? Math.min(ELAN_MAX, e[0] + 1) : 1;
            e[1] = now;
            if (e[0] >= 3) SpellFx.effect(attacker, "Luo_Elan_Hirondelle", cb);
        }
    }

    // ===================== Endurance des attaques (demande du 2026-11-11) =====================
    // Chaque coup d'un katana Luo (kata, combo du Bokken) coute COUP_COST de l'endurance maximale (2 % -> 5 % le 2026-11-11,
    // « augmente la consommation ») ; le coup maintenu (degaine
    // foudroyant, rafale du Bokken, Flash Celeste de Tenryumaru) en coute HELD_COST et echoue s'il en reste moins. Les PNJ ne
    // paient rien. Le sort KiEpeeCharge (insere dans tous les coups maintenus) encaisse le cout ; les sorts du coup maintenu qui
    // suivent (LuoIai, FlashCeleste, LuoBokkenRafale) lisent heldAllowed.
    public static final float COUP_COST = 0.05f, HELD_COST = 0.50f;
    private static final long HELD_WINDOW_MS = 1_500;
    private static final Map<Ref<EntityStore>, Long> heldPaid = new ConcurrentHashMap<>(), heldRefused = new ConcurrentHashMap<>();

    private static boolean npc(Ref<EntityStore> ref) { return MartialKi.NPC_TIER.containsKey(ref); }

    /** Coup ordinaire : retire COUP_COST de l'endurance maximale (jamais bloquant). */
    public static void payCoup(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || npc(ref)) return;
        int st = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getStamina();
        float max = CloneFx.maxStat(ref, st, store);
        if (max > 0) MartialKi.addStat(ref, st, -max * COUP_COST, store);
    }

    /** Coup maintenu : retire HELD_COST de l'endurance maximale ; false (et message) s'il n'en reste pas assez. */
    public static boolean payHeld(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || npc(ref)) return true;
        long now = System.currentTimeMillis();
        int st = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getStamina();
        float max = CloneFx.maxStat(ref, st, store), cur = MartialKi.stat(ref, st, store);
        if (max <= 0 || cur < 0) { heldPaid.put(ref, now); return true; }
        if (cur + 0.01f < max * HELD_COST) {
            heldRefused.put(ref, now);
            MartialKi.notify(ref, store, "<color:gray>Endurance insuffisante : le coup maintenu demande la moitié de votre endurance.</color>",
                    com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Warning);
            return false;
        }
        MartialKi.addStat(ref, st, -max * HELD_COST, store);
        heldPaid.put(ref, now);
        heldRefused.remove(ref);
        return true;
    }

    /** Retire frac de l'endurance maximale ; false (et message) s'il n'en reste pas assez. Percee (2026-11-11). */
    public static boolean payFraction(Ref<EntityStore> ref, Store<EntityStore> store, float frac) {
        if (ref == null || npc(ref)) return true;
        int st = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getStamina();
        float max = CloneFx.maxStat(ref, st, store), cur = MartialKi.stat(ref, st, store);
        if (max <= 0 || cur < 0) return true;
        if (cur + 0.01f < max * frac) {
            MartialKi.notify(ref, store, "<color:gray>Endurance insuffisante.</color>",
                    com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Warning);
            return false;
        }
        MartialKi.addStat(ref, st, -max * frac, store);
        return true;
    }

    /** Suite d'un coup maintenu : true si KiEpeeCharge vient de l'encaisser (sinon on l'encaisse ici). */
    public static boolean heldAllowed(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || npc(ref)) return true;
        long now = System.currentTimeMillis();
        Long refused = heldRefused.get(ref);
        if (refused != null && now - refused <= HELD_WINDOW_MS) return false;
        Long paid = heldPaid.get(ref);
        if (paid != null && now - paid <= HELD_WINDOW_MS) return true;
        return payHeld(ref, store);
    }

    /** Sort LuoEndurance, insere dans chaque coup du combo du Bokken (gen_luo_katanas.py). */
    public static final class Endurance implements RuneSpell {
        @Override public String id() { return "LuoEndurance"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            payCoup(caster, store);
            return null;
        }
    }

    // ===================== Bokken du Dojo : sabre d'entrainement et combo de la voie martiale =====================

    /** Longueur du combo du Bokken selon le palier de la voie martiale : 2 hors de la voie, 3 (Apprenti) ... 8 (Saint et plus). */
    static int bokkenCombo(int tier) { return tier <= 0 ? 2 : KATA_LENGTH[Math.min(8, tier) - 1]; }

    /** Longueur du kata du Dragon d'Or par palier (Apprenti 3 ... Dieu Martial 12), meme liste que gen_luo_katanas.KATA_LENGTHS. */
    static final int[] KATA_LENGTH = {3, 4, 5, 6, 8, 9, 10, 12};

    private static final Map<Ref<EntityStore>, Long> tierMarked = new ConcurrentHashMap<>();

    /**
     * Pose l'effet invisible Voie_Combo_<N> (N = longueur du combo) sur le porteur du Bokken, rafraichi toutes les 0,5 s :
     * l'interaction Luo_Combo_Bokken choisit la chaine par EffectCondition (gen_luo_katanas.py). Appele par TenryumaruSystem.
     */
    static void trackTier(Ref<EntityStore> ref, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        String k = katana(ref, store);
        if (k == null) return;
        if (!BOKKEN.equals(k)) {                                               // kata du Dragon d'Or : longueur selon la classe
            long now = System.currentTimeMillis();
            Long last = tierMarked.get(ref);
            if (last != null && now - last < 500) return;
            tierMarked.put(ref, now);
            int tier = com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(MartialKi.data(ref, store));
            if (tier > 0) SpellFx.effect(ref, "Voie_Combo_" + KATA_LENGTH[tier - 1], cb);
            return;
        }
        long now = System.currentTimeMillis();
        Long last = tierMarked.get(ref);
        if (last != null && now - last < 500) return;
        tierMarked.put(ref, now);
        int n = bokkenCombo(com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(MartialKi.data(ref, store)));
        if (n > 2) SpellFx.effect(ref, "Voie_Combo_" + n, cb);
    }

    /**
     * Sabre d'entrainement : un coup porte avec le Bokken en main ne tue jamais (joueurs et PNJ) ; il laisse au moins 1 PV.
     * Appele a la fin de CombatStatsSystem et de DivineDamageSystem (les reductions suivantes ne font que baisser le coup).
     */
    public static void trainingCap(com.hypixel.hytale.server.core.modules.entity.damage.Damage damage, Ref<EntityStore> attacker,
                                   Ref<EntityStore> target, Store<EntityStore> store) {
        if (damage == null || damage.isCancelled() || attacker == null || target == null || !attacker.isValid() || !target.isValid()) return;
        if (!BOKKEN.equals(katana(attacker, store))) return;
        float hp = MartialKi.stat(target, com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), store);
        if (hp < 0) return;
        float max = hp - 1f;
        if (max <= 0f) { damage.setCancelled(true); return; }                 // deja a 1 PV : plus que des bleus
        if (damage.getAmount() > max) damage.setAmount(max);
    }

    /**
     * Rafale du Bokken (maintien de l'attaque, apres la posture chargee) : tout le combo du palier enchaine en un eclair, un
     * coup toutes les 0,11 s dans un arc de 3,5 blocs devant le porteur, un petit pas a chaque coup, le dernier est lourd.
     * A partir du Saint Martial (demande du 2026-11-10) : chaque mouvement lance une lame de Ki inclinee comme le coup
     * (KATA_ORIENT de gen_luo_katanas.py), la derniere plus grande ; seulement si le Ki de l'Epee est deploye (2026-11-11).
     */
    public static final class Rafale implements RuneSpell {
        /** Kata du Dragon d'Or (gen_luo_katanas.KATA), joue en rafale, et inclinaison de la lame de chaque coup. */
        private static final String[] ANIMS = {"LuoCoup1", "LuoCoup2", "LuoCoup3", "LuoCoup4", "SpinRightCharged", "Stab",
                "LuoKaiten", "SwingLeftSpinJumpCharged", "LuoTourbillon", "LuoSalto", "LuoTornade", "SpinRightStabDashCharged"};
        private static final String[] ORIENT = {"H", "G", "G", "V", "H", "V", "H", "G", "H", "V", "H", "D"};
        private static final long STEP_MS = 110;
        private static final double REACH = 3.5, HALF_ANGLE = Math.toRadians(45);
        private static final float LIGHT = 7f, HEAVY = 12f;

        @Override public String id() { return "LuoBokkenRafale"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!heldAllowed(caster, store)) return null;                       // endurance : la moitie du total
            int tier = MartialKi.tier(caster, store);
            boolean npc = MartialKi.NPC_TIER.containsKey(caster);
            int voie = npc ? tier : com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(MartialKi.data(caster, store));
            // 2026-11-11 : le systeme du Bokken s'etend a tous les katanas (sauf ceux de famille) : la rafale enchaine le kata
            // du katana tenu (Bokken : son combo), degats x la puissance du katana.
            LuoKatanas.Katana held = Percee.held(caster, store);
            boolean bokken = held == null || BOKKEN.equals(held.id());
            int n = bokken ? bokkenCombo(voie) : voie > 0 ? KATA_LENGTH[Math.min(8, voie) - 1] : 4;
            float power = Percee.power(held);
            long now = System.currentTimeMillis();
            java.util.List<Instance> blades = new java.util.ArrayList<>();
            java.util.Map<Ref<EntityStore>, Long> own = new ConcurrentHashMap<>();
            return new Instance() {
                int done;
                long next = now;

                @Override
                public boolean tick(Ref<EntityStore> c, long t, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
                    for (var it = blades.iterator(); it.hasNext(); ) {                // lames deja lancees
                        Instance bl = it.next();
                        if (bl.tick(c, t, s, b)) { bl.cleanup(b); it.remove(); }
                    }
                    if (done >= n) return blades.isEmpty();
                    if (t < next) return false;
                    next = t + STEP_MS;
                    boolean last = done == n - 1;
                    int i = done % ANIMS.length;
                    AnimationUtils.playAnimation(c, AnimationSlot.Action, "Tenryumaru_Degainee", ANIMS[i], true, s);
                    Vector3d f = forward(c, s), me = SpellFx.position(c, s);
                    SpellFx.push(c, new Vector3d(f).mul(last ? 7.0 : 3.0), false, b);
                    KiEpee.Active ki = KiEpee.active(c, s);
                    // Saint et au-dela : une lame par mouvement, seulement Ki de l'Epee deploye (demande du 2026-11-11) ; PNJ : toujours
                    if (voie >= 6 && (ki != null || npc)) {
                        float boost = last ? KiEpee.CHARGE_SCALE : 1f;
                        if (ki != null) { ki.lastBlade = 0; ki.swing(c, t, ORIENT[i], boost, s, b); }
                        else {
                            Instance bl = KiEpee.launchBlade(c, voie, SpellFx.lookDirection(c, s), ORIENT[i], boost,
                                    e -> own.put(e, System.currentTimeMillis() + 400), s, b);
                            if (bl != null) blades.add(bl);
                        }
                    }
                    if (me != null) {
                        SpellFx.sound(last ? "SFX_Sword_T2_Swing_Down" : "SFX_Light_Melee_T2_Lunge", me, b);
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(c, me, REACH, s)) {
                            Vector3d p = SpellFx.position(e, s);
                            if (p == null) continue;
                            Vector3d to = new Vector3d(p.x - me.x, 0, p.z - me.z);
                            if (to.lengthSquared() > 1e-4 && to.normalize().angle(f) > HALF_ANGLE) continue;
                            SpellFx.fx("Impact_Sword_Charged", new Vector3d(p).add(0, 1.1, 0), last ? 1.1f : 0.6f, s, b);
                            SpellFx.damage(c, e, "Physical", (last ? HEAVY : LIGHT) * power, new Vector3d(f).mul(last ? 6 : 2).add(0, last ? 2.5 : 0.5, 0),
                                    last ? "Impact_Strong" : "Impact_Light", b);
                        }
                    }
                    done++;
                    return done >= n && blades.isEmpty();
                }

                @Override
                public void cleanup(CommandBuffer<EntityStore> b) {
                    for (Instance bl : blades) { try { bl.cleanup(b); } catch (Exception ignored) { } }
                    blades.clear();
                }
            };
        }
    }

    // ===================== Mouvements =====================

    private static Vector3d forward(Ref<EntityStore> ref, Store<EntityStore> store) {
        Vector3d f = SpellFx.forward(ref, store);
        f.y = 0;
        return f.lengthSquared() < 1e-4 ? new Vector3d(0, 0, 1) : f.normalize();
    }

    /** 4e coup du combo Luo (karatake-wari) : un pas en avant. */
    public static final class Pas implements RuneSpell {
        @Override public String id() { return "LuoPas"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.push(caster, forward(caster, store).mul(7.0), false, cb);
            return null;
        }
    }

    /** Fin du combo : pas chasse lateral (Aonami), bond en arriere (Shirasagi). */
    public static final class FinCombo implements RuneSpell {
        @Override public String id() { return "LuoFinCombo"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            String k = katana(caster, store);
            Vector3d f = forward(caster, store);
            Vector3d pos = SpellFx.position(caster, store);
            if (AONAMI.equals(k)) {
                SpellFx.push(caster, new Vector3d(-f.z, 0, f.x).mul(9.0).add(0, 1.5, 0), false, cb);
                if (pos != null) SpellFx.fx("Ability_Ice_Impact", new Vector3d(pos).add(0, 0.3, 0), 0.8f, store, cb);
            } else if (SHIRASAGI.equals(k)) {
                SpellFx.protectFromFall(caster, 2_000, store);
                SpellFx.push(caster, new Vector3d(f).mul(-9.0).add(0, 5.0, 0), false, cb);
                if (pos != null) SpellFx.fx("Eldanior_Pas_Celeste", new Vector3d(pos).add(0, 0.1, 0), 1.0f, store, cb);
            }
            return null;
        }
    }

    /** Au degaine : ruee courte (Hien), fondu d'ombre (Tsukikage). */
    public static final class DegaineMouvement implements RuneSpell {
        @Override public String id() { return "LuoDegaineMouvement"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            String k = katana(caster, store);
            Vector3d pos = SpellFx.position(caster, store);
            if (HIEN.equals(k)) {
                SpellFx.push(caster, forward(caster, store).mul(14.0).add(0, 0.5, 0), false, cb);
                if (pos != null) SpellFx.fx("Impact_Fire", new Vector3d(pos).add(0, 0.6, 0), 0.8f, store, cb);
            } else if (TSUKIKAGE.equals(k)) {
                SpellFx.effect(caster, "Luo_Ombre_Lune", cb);
                if (pos != null) SpellFx.fx("Nyx_Void", new Vector3d(pos).add(0, 1.0, 0), 0.8f, store, cb);
            }
            return null;
        }
    }
}
