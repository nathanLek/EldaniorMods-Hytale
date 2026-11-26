package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Les effets des passifs (refonte du 2026-11-24) : une poignee de mecaniques, chacune avec une valeur par rang.
 * Avant, chaque rang etait une classe a part (Frappe Instinctive, Frappe de Predateur...) avec le meme code et d'autres
 * chiffres, souvent different de sa description. Les tableaux sont indexes par rang : v[0] = rang 1 ... v[5] = rang 6.
 *
 * Maitrise : l'effet est augmente de 10 % (MASTERY). Aucun passif ne coute de mana ni n'a de recharge (role des runes),
 * aucun ne joue de particule ni n'envoie de notification (decision du 2026-11-24).
 *
 * Attention : jamais de StatConfig ni de DefaultEntityStatTypes en champ static ici (classe chargee au demarrage).
 */
public final class PassiveEffects {

    private PassiveEffects() {}

    /** Bonus de l'effet quand le passif est maitrise. */
    public static final float MASTERY = 1.10f;

    /** Passif sans effet de combat (Vol, Vision Nocturne, Bienveillance : gere ailleurs). */
    public static final IPassiveCombatSkill AUCUN = new IPassiveCombatSkill() {};

    // ================================================================================================= outils

    private static float v(float[] t, int rank) { return t[Math.max(1, Math.min(t.length, rank)) - 1]; }

    private static float m(boolean mastered) { return mastered ? MASTERY : 1f; }

    private static boolean roll(float percent) { return ThreadLocalRandom.current().nextFloat() * 100f < percent; }

    /** Facteur d'une reduction de dégâts (en %), maitrise comprise, jamais plus de 90 %. */
    private static float cut(float percent, boolean mastered) { return 1f - Math.min(0.90f, percent * m(mastered) / 100f); }

    private static EntityStatMap stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        return ref != null && ref.isValid() ? store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()) : null;
    }

    private static EntityStatValue stat(Ref<EntityStore> ref, Store<EntityStore> store, int id) {
        EntityStatMap map = stats(ref, store);
        return map != null ? map.get(id) : null;
    }

    /** Part (0..1) de la stat sur son maximum ; -1 si inconnue. */
    private static float ratio(Ref<EntityStore> ref, Store<EntityStore> store, int id) {
        EntityStatValue s = stat(ref, store, id);
        return s != null && s.getMax() > 0 ? s.get() / s.getMax() : -1f;
    }

    /** Ajoute a une stat (sans depasser le maximum). */
    private static boolean restore(Ref<EntityStore> ref, Store<EntityStore> store, int id, float amount) {
        EntityStatMap map = stats(ref, store);
        EntityStatValue s = map != null ? map.get(id) : null;
        if (s == null) return false;
        map.setStatValue(id, Math.min(s.getMax(), s.get() + amount));
        return true;
    }

    private static int health() { return DefaultEntityStatTypes.getHealth(); }
    private static int mana() { return DefaultEntityStatTypes.getMana(); }
    private static int stamina() { return DefaultEntityStatTypes.getStamina(); }

    private static boolean isMonster(Ref<EntityStore> ref, Store<EntityStore> store) {
        return ref != null && ref.isValid() && store.getComponent(ref, EldaniorSystem.get().getMobLevelDataType()) != null;
    }

    // ================================================================================================= stats

    public enum Mode { FLAT, MULT, REGEN }

    /** Quand un passif de stats progresse en maitrise. */
    public enum Declencheur { AUCUN, TOUCHE, CRITIQUE }

    /** Une stat et sa valeur par rang ; FLAT = ajoutee, MULT = en %, REGEN = multiplicateur de regeneration. */
    public record Bonus(String stat, Mode mode, float[] values) {}

    public static Bonus bonus(String stat, Mode mode, float[] values) { return new Bonus(stat, mode, values); }

    /** Passif de stats : bonus permanents ; progresse quand on est touche, quand on fait un critique, ou autrement. */
    public static IPassiveCombatSkill stats(Declencheur trigger, Bonus... bonuses) {
        return new IPassiveCombatSkill() {
            @Override public float getFlatStatBonus(StatConfig stat, int rank, boolean mastered) {
                float total = 0f;
                for (Bonus b : bonuses) if (b.mode == Mode.FLAT && b.stat.equals(stat.name())) total += v(b.values, rank) * m(mastered);
                return total;
            }
            @Override public float getStatMultiplier(StatConfig stat, int rank, boolean mastered) {
                float mult = 1f;
                for (Bonus b : bonuses) if (b.mode == Mode.MULT && b.stat.equals(stat.name())) mult *= 1f + v(b.values, rank) * m(mastered) / 100f;
                return mult;
            }
            @Override public float getRegenMultiplier(StatConfig stat, int rank, boolean mastered) {
                float mult = 1f;
                for (Bonus b : bonuses) if (b.mode == Mode.REGEN && b.stat.equals(stat.name())) mult *= 1f + (v(b.values, rank) - 1f) * m(mastered);
                return mult;
            }
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                return trigger == Declencheur.CRITIQUE && data != null && data.wasLastAttackCrit();
            }
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                return trigger == Declencheur.TOUCHE && !d.isCancelled();
            }
        };
    }

    // ================================================================================================= attaque

    /** chance % d'infliger +bonus % ; au rang 6, chance de doubler l'impact. */
    public static IPassiveCombatSkill chanceDegats(float[] chance, float[] bonus, float doubleChance) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || !roll(v(chance, rank))) return false;
                float mult = 1f + v(bonus, rank) * m(mastered) / 100f;
                if (rank >= 6 && doubleChance > 0 && ThreadLocalRandom.current().nextFloat() < doubleChance) mult *= 2f;
                d.setAmount(d.getAmount() * mult);
                return true;
            }
        };
    }

    /** +v degats a chaque coup. */
    public static IPassiveCombatSkill degatsFixes(float[] flat) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled()) return false;
                d.setAmount(d.getAmount() + v(flat, rank) * m(mastered));
                return true;
            }
        };
    }

    /** chance % d'infliger +v degats (arcaniques). */
    public static IPassiveCombatSkill chanceDegatsFixes(float[] chance, float[] flat) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || !roll(v(chance, rank))) return false;
                d.setAmount(d.getAmount() + v(flat, rank) * m(mastered));
                return true;
            }
        };
    }

    /** +bonus % contre une cible a plus de 90 % de vie. */
    public static IPassiveCombatSkill ciblePleineVie(float[] bonus) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || ratio(vr, s, health()) < 0.90f) return false;
                d.setAmount(d.getAmount() * (1f + v(bonus, rank) * m(mastered) / 100f));
                return true;
            }
        };
    }

    /** Coups de suite sur la meme cible : +v % par coup, 5 fois au plus. */
    public static IPassiveCombatSkill coupsEnchaines(float[] perHit) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (data == null || d.isCancelled() || vr == null || !vr.isValid()) return false;
                UUIDComponent id = s.getComponent(vr, UUIDComponent.getComponentType());
                if (id == null) return false;
                UUID victim = id.getUuid();
                if (!victim.equals(data.getLastVictimUUID())) {
                    data.setLastVictimUUID(victim);
                    data.setHauntingThrustStacks(1);
                    return false;
                }
                int stacks = Math.min(5, data.getHauntingThrustStacks() + 1);
                data.setHauntingThrustStacks(stacks);
                d.setAmount(d.getAmount() * (1f + stacks * v(perHit, rank) * m(mastered) / 100f));
                return true;
            }
        };
    }

    /** +bonus % pendant 2 s apres avoir ete touche. */
    public static IPassiveCombatSkill riposte(float[] bonus) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (data == null || d.isCancelled() || System.currentTimeMillis() - data.getLastDamageTakenTime() > 2000) return false;
                d.setAmount(d.getAmount() * (1f + v(bonus, rank) * m(mastered) / 100f));
                return true;
            }
        };
    }

    /** +bonus % quand le mana depasse le seuil (%). */
    public static IPassiveCombatSkill manaHaut(float[] threshold, float[] bonus) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || ratio(a, s, mana()) * 100f < v(threshold, rank)) return false;
                d.setAmount(d.getAmount() * (1f + v(bonus, rank) * m(mastered) / 100f));
                return true;
            }
        };
    }

    /** +bonus % avec une arme dont l'identifiant contient l'un des mots (sauf les exclus, ex. pickaxe pour axe). */
    public static IPassiveCombatSkill arme(String[] words, String[] excluded, float[] bonus) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || a == null || !a.isValid()) return false;
                Player player = s.getComponent(a, Player.getComponentType());
                ItemStack held = player != null ? player.getInventory().getActiveHotbarItem() : null;
                if (held == null || held.isEmpty()) return false;
                String id = held.getItemId().toLowerCase(Locale.ROOT);
                for (String x : excluded) if (id.contains(x)) return false;
                for (String w : words) {
                    if (id.contains(w)) {
                        d.setAmount(d.getAmount() * (1f + v(bonus, rank) * m(mastered) / 100f));
                        return true;
                    }
                }
                return false;
            }
        };
    }

    // ================================================================================================= defense

    /** -v degats sur chaque coup recu (jamais sous 1). */
    public static IPassiveCombatSkill reductionFixe(float[] flat) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled()) return false;
                d.setAmount(Math.max(1f, d.getAmount() - v(flat, rank) * m(mastered)));
                return true;
            }
        };
    }

    /** -v % de degats recus. */
    public static IPassiveCombatSkill reduction(float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled()) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** chance % de reduire un coup de v %. */
    public static IPassiveCombatSkill chanceReduction(float[] chance, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || !roll(v(chance, rank))) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** -v % de degats recus des monstres. */
    public static IPassiveCombatSkill contreMonstres(float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || !isMonster(a, s)) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** Sous le seuil de vie (%) : -v % de degats recus. */
    public static IPassiveCombatSkill vieBasse(float[] threshold, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                float r = ratio(vr, s, health());
                if (d.isCancelled() || r < 0 || r * 100f >= v(threshold, rank)) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** Endurance au-dessus du seuil (%) : -v % de degats recus. */
    public static IPassiveCombatSkill enduranceHaute(float[] threshold, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || ratio(vr, s, stamina()) * 100f < v(threshold, rank)) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** Mana au-dessus du seuil (%) : -v % de degats recus. */
    public static IPassiveCombatSkill manaBouclier(float[] threshold, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || ratio(vr, s, mana()) * 100f < v(threshold, rank)) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /** Coups de plus de t degats reduits de v %. */
    public static IPassiveCombatSkill grosCoups(float[] threshold, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || d.getAmount() <= v(threshold, rank)) return false;
                d.setAmount(d.getAmount() * cut(v(percent, rank), mastered));
                return true;
            }
        };
    }

    /**
     * +chance % a l'esquive automatique (refonte du 2026-11-25) : s'ajoute a la chance de l'Agilite, un seul jet
     * (AutoDodge, plafond 30 %). La maitrise progresse a chaque esquive reussie (AutoDodge.creditPassives).
     */
    public static IPassiveCombatSkill esquive(float[] chance) {
        return new IPassiveCombatSkill() {
            @Override public float getFlatStatBonus(StatConfig stat, int rank, boolean mastered) {
                return stat == StatConfig.DODGE_CHANCE ? v(chance, rank) * m(mastered) : 0f;
            }
        };
    }

    // ================================================================================================= ressources

    /** chance % de rendre v d'endurance a chaque coup donne. */
    public static IPassiveCombatSkill enduranceAuCoup(float[] chance, float[] amount) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                return !d.isCancelled() && roll(v(chance, rank)) && restore(a, s, stamina(), v(amount, rank) * m(mastered));
            }
        };
    }

    /** chance % de rendre v de mana a chaque coup donne. */
    public static IPassiveCombatSkill manaAuCoup(float[] chance, float[] amount) {
        return new IPassiveCombatSkill() {
            @Override public boolean onAttack(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                return !d.isCancelled() && roll(v(chance, rank)) && restore(a, s, mana(), v(amount, rank) * m(mastered));
            }
        };
    }

    /** chance % de rendre v % de la vie max quand on est touche. */
    public static IPassiveCombatSkill soinTouche(float[] chance, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                if (d.isCancelled() || !roll(v(chance, rank))) return false;
                EntityStatValue hp = stat(vr, s, health());
                return hp != null && restore(vr, s, health(), hp.getMax() * v(percent, rank) * m(mastered) / 100f);
            }
        };
    }

    /** Endurance sous le seuil (%) : chance % de rendre v d'endurance quand on est touche. */
    public static IPassiveCombatSkill secondSouffle(float[] threshold, float[] chance, float[] amount) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                float r = ratio(vr, s, stamina());
                if (d.isCancelled() || r < 0 || r * 100f >= v(threshold, rank) || !roll(v(chance, rank))) return false;
                return restore(vr, s, stamina(), v(amount, rank) * m(mastered));
            }
        };
    }

    /** Sous le seuil de vie (%) : chaque coup recu rend v % de la vie max. */
    public static IPassiveCombatSkill soinVieBasse(float[] threshold, float[] percent) {
        return new IPassiveCombatSkill() {
            @Override public boolean onDefend(Damage d, PlayerLevelData data, Store<EntityStore> s, Ref<EntityStore> a, Ref<EntityStore> vr, int rank, boolean mastered) {
                EntityStatValue hp = stat(vr, s, health());
                if (d.isCancelled() || hp == null || hp.getMax() <= 0 || hp.get() / hp.getMax() * 100f >= v(threshold, rank)) return false;
                return restore(vr, s, health(), hp.getMax() * v(percent, rank) * m(mastered) / 100f);
            }
        };
    }

    // ================================================================================================= detection

    /** Instinct de Survie : portee des menaces xv, message de radar (DetectionSystem), joueurs invisibles reveles. */
    public static IPassiveCombatSkill radar(float[] threatMultiplier) {
        return new IPassiveCombatSkill() {
            @Override public float getStatMultiplier(StatConfig stat, int rank, boolean mastered) {
                return "THREAT_AWARENESS".equals(stat.name()) ? 1f + (v(threatMultiplier, rank) - 1f) * m(mastered) : 1f;
            }
            @Override public String getRadarMessage(List<RadarTarget> closest, int extraMobs, int extraPlayers) {
                int total = closest.size() + extraMobs + extraPlayers;
                return "<color:red>" + total + " présence(s) à proximité</color>";
            }
            @Override public NotificationStyle getRadarStyle() { return NotificationStyle.Warning; }
        };
    }
}
