package com.eldanior.system.guards;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Competences passives des aventuriers : celles de leur classe (AdventurerTier.classOf). Appliquees par CombatStatsSystem :
 * <ul>
 *   <li>bonus de stats des passifs (intelligence, force, vitalite, endurance, critique, esquive) convertis pour un PNJ ;</li>
 *   <li>passifs de combat : leur propre code onAttack / onDefend, sans donnees de joueur (null). Ceux qui en ont besoin
 *       (mana, series de coups...) echouent au premier essai et sont ensuite ignores.</li>
 * </ul>
 */
public final class AdventurerPassives {

    private AdventurerPassives() {}

    /** Bonus de stats de ses passifs, convertis pour un PNJ. */
    private record Stats(float power, float toughness, float flatDamage, float flatReduce, float crit, float dodge, float regen) {}

    private static final Map<String, Stats> STATS = new ConcurrentHashMap<>();
    /** Passifs qui lisent des donnees de joueur : jamais rappeles pour un aventurier. */
    private static final Set<PassiveSkill> PLAYER_ONLY = ConcurrentHashMap.newKeySet();
    /** Recharges des passifs qui en ont une (par aventurier et passif). */
    private static final Map<String, Long> COOLDOWNS = new ConcurrentHashMap<>();

    /** Passifs de l'aventurier : ceux de sa classe. */
    static List<PassiveSkill> of(GuardManager.Post p) {
        ClassModel cls = AdventurerTier.classOf(p);
        return cls != null ? cls.getSkillsPassiveIds() : List.of();
    }

    /** Noms des passifs, pour la fenetre de recrutement. */
    public static String names(GuardManager.Post p) {
        StringBuilder sb = new StringBuilder();
        for (PassiveSkill s : of(p)) sb.append(sb.length() > 0 ? ", " : "").append(s.getDisplayName());
        return sb.toString();
    }

    private static Stats stats(GuardManager.Post p) {
        return STATS.computeIfAbsent(p.id + ":" + p.level, k -> {
            float power = 1f, toughness = 1f, flatDamage = 0f, flatReduce = 0f, crit = 0f, dodge = 0f, regen = 1f;
            for (PassiveSkill s : of(p)) {
                IPassiveCombatSkill l = s.getLogic();
                if (l == null) continue;
                power += (l.getFlatStatBonus(StatConfig.INTELLIGENCE) + l.getFlatStatBonus(StatConfig.STRENGTH_DAMAGE) * 2) / 200f;
                power *= l.getStatMultiplier(StatConfig.INTELLIGENCE) * l.getStatMultiplier(StatConfig.STRENGTH_DAMAGE);
                toughness += (l.getFlatStatBonus(StatConfig.VITALITY) + l.getFlatStatBonus(StatConfig.ENDURANCE)) / 200f;
                toughness *= l.getStatMultiplier(StatConfig.VITALITY);
                flatReduce += l.getFlatStatBonus(StatConfig.ENDURANCE_DEFENSE);
                crit += l.getFlatStatBonus(StatConfig.LUCK_CRITICAL) + (l.getStatMultiplier(StatConfig.LUCK_CRITICAL) - 1f) * 100f;
                dodge += l.getFlatStatBonus(StatConfig.DODGE_CHANCE) + (l.getStatMultiplier(StatConfig.DODGE_CHANCE) - 1f) * 100f;
                regen *= l.getRegenMultiplier(StatConfig.VITALITY);
            }
            return new Stats(power, toughness, 0f, Math.min(35f, flatReduce), Math.min(80f, crit), Math.min(50f, dodge), regen);
        });
    }

    /** Multiplicateur de regeneration naturelle de ses passifs (AdventurerSystem.limitRegen). */
    public static float regenMultiplier(GuardManager.Post p) {
        return p.type == GuardType.AVENTURIER ? stats(p).regen() : 1f;
    }

    private static float health(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap map = ref != null && ref.isValid() ? store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()) : null;
        var hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
        return hp != null ? hp.get() : 0f;
    }

    /** Soin fait par ses passifs : le plafond de regeneration du compagnon (limitRegen) le laisse passer. */
    private static void keepHeal(GuardManager.Post p, float before, Ref<EntityStore> ref, Store<EntityStore> store) {
        float after = health(ref, store);
        if (after > before && p.lastHp > 0) p.lastHp += after - before;
    }

    /** Poste de l'aventurier (null si ce PNJ n'en est pas un). */
    private static GuardManager.Post adventurer(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        NPCEntity npc = ref != null && ref.isValid() ? store.getComponent(ref, NPCEntity.getComponentType()) : null;
        if (npc == null || GuardType.fromRole(npc.getNPCTypeId()) != GuardType.AVENTURIER) return null;
        return GuardManager.postOf(ref, store);
    }

    private interface Call { boolean run(IPassiveCombatSkill logic); }

    /** Lance le code d'un passif ; s'il exige des donnees de joueur (exception), il ne sera plus essaye. */
    private static void run(GuardManager.Post p, PassiveSkill s, Call call) {
        IPassiveCombatSkill logic = s.getLogic();
        if (logic == null || PLAYER_ONLY.contains(s)) return;
        String key = p.id + ":" + s.name();
        long now = System.currentTimeMillis();
        Long until = COOLDOWNS.get(key);
        if (until != null && until > now) return;
        try {
            if (call.run(logic) && s.getCooldownSeconds() > 0) COOLDOWNS.put(key, now + (long) (s.getCooldownSeconds() * 1000));
        } catch (Exception e) {
            PLAYER_ONLY.add(s);
        }
    }

    /** L'aventurier frappe : statut et classe, bonus de stats, passifs de combat, critique (x2). */
    public static void onAttack(Damage damage, Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        GuardManager.Post p = adventurer(attacker, store);
        if (p == null) return;
        Stats st = stats(p);
        damage.setAmount((damage.getAmount() + st.flatDamage()) * st.power() * AdventurerTier.power(p));
        float before = health(attacker, store);
        for (PassiveSkill s : of(p)) run(p, s, l -> l.onAttack(damage, null, store, attacker, victim, false));
        keepHeal(p, before, attacker, store);
        if (st.crit() > 0 && ThreadLocalRandom.current().nextFloat() * 100 < st.crit()) damage.setAmount(damage.getAmount() * 2f);
    }

    /** L'aventurier est frappe : esquive, resistance (statut, classe, stats), passifs de defense. */
    public static void onDefend(Damage damage, Ref<EntityStore> victim, Store<EntityStore> store) {
        GuardManager.Post p = adventurer(victim, store);
        if (p == null) return;
        Stats st = stats(p);
        if (st.dodge() > 0 && ThreadLocalRandom.current().nextFloat() * 100 < st.dodge()) { damage.setCancelled(true); return; }
        Ref<EntityStore> attacker = damage.getSource() instanceof Damage.EntitySource es ? es.getRef() : null;
        float amount = damage.getAmount() / (st.toughness() * AdventurerTier.toughness(p)) - st.flatReduce();
        damage.setAmount(Math.max(1f, amount));
        float before = health(victim, store);
        for (PassiveSkill s : of(p)) run(p, s, l -> l.onDefend(damage, null, store, attacker, victim, false));
        keepHeal(p, before, victim, store);
        if (!damage.isCancelled() && damage.getAmount() < 1f) damage.setAmount(1f);
    }
}