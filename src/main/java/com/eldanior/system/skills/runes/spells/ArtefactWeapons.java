package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Effets passifs des armes-artefacts qu'on ne peut pas exprimer en JSON, appliques par CombatStatsSystem aux degats d'un
 * joueur qui tient l'arme (catalogue : scripts/artefacts/weapon_catalog.py). Les effets a l'impact simples (givre, poison,
 * feu, vol de vie...) restent dans les JSON des armes.
 *   - Excalibur : +50 % contre les morts-vivants et les creatures du Neant ;
 *   - Gram : +60 % contre les dragons et les creatures colossales (boite de collision haute ou large) ;
 *   - Masamune : +15 % de chance de coup critique ;
 *   - Lame du Roi Dechu : +40 % contre une cible sous la moitie de ses points de vie ;
 *   - Ryukiri (Runkandel) : +100 % contre les dragons ; le porteur subit 40 % de degats de feu en moins.
 * Armes divines (scripts/artefacts/gen_divines.py, signatures dans ArmesDivines) :
 *   - Ouas de Ra : embrase, +25 % contre les morts-vivants ; Baguette de Circe : affaiblit ;
 *   - Egide : le porteur subit 25 % de degats en moins ; Durandal : +30 % quand le porteur est sous 50 % de vie ;
 *   - Hache de Skadi : ralentit, 15 % de gel ; Labrys : +35 % sur une cible en pleine sante ;
 *   - Faux de Thanatos : vol de vie 12 %, fauche les cibles sous 10 % de vie ;
 *   - Mjollnir : 20 % de foudre (+50 %, choc) ; Sharur : un coup sur quatre x2,5 ;
 *   - Arc d'Artemis : +25 % de chance critique ; Ruyi Jingu Bang : 20 % d'etourdissement ;
 *   - Gourdin d'Heracles : +50 % contre les betes et les colosses ; Massue du Dagda : soigne les allies proches (10 % des degats) ;
 *   - Carnwennan : +60 % dans le dos ; Guandao : 20 % d'onde (50 % des degats aux ennemis voisins) ;
 *   - Arc d'Apollon : embrase, soigne l'archer de 6 % des degats ;
 *   - Aigles du Desert (legendaire) : Rythme du Pistolero, +6 % de degats par touche enchainee (+60 % max).
 */
public final class ArtefactWeapons {

    private ArtefactWeapons() {}

    public static final String EXCALIBUR = "Artefact_Sword_Excalibur", GRAM = "Artefact_Sword_Gram";
    public static final String MASAMUNE = "Artefact_Sword_Masamune", ROI_DECHU = "Artefact_Sword_Roi_Dechu";
    public static final String RYUKIRI = "Artefact_Sword_Ryukiri";
    public static final String RA = "Artefact_Staff_Ouas_Ra", CIRCE = "Artefact_Wand_Circe", EGIDE = "Artefact_Shield_Egide";
    public static final String DURANDAL = "Artefact_Longsword_Durandal", SKADI = "Artefact_Axe_Skadi", LABRYS = "Artefact_Battleaxe_Labrys";
    public static final String THANATOS = "Artefact_Battleaxe_Faux_Thanatos", MJOLLNIR = "Artefact_Mace_Mjollnir", SHARUR = "Artefact_Mace_Sharur";
    public static final String ARTEMIS = "Artefact_Shortbow_Artemis", RUYI = "Artefact_Staff_Ruyi_Jingu_Bang", HERACLES = "Artefact_Club_Heracles";
    public static final String DAGDA = "Artefact_Club_Dagda", CARNWENNAN = "Artefact_Daggers_Carnwennan", GUANDAO = "Artefact_Spear_Guandao";
    public static final String APOLLON = "Artefact_Shortbow_Apollon";
    public static final String AIGLES = "Artefact_Handgun_Aigles_Desert";   // Colts de cavalerie akimbo (Green Colt) : Rythme du Pistolero (AiglesDuDesert)
    private static final String[] BEASTS = {"Wolf", "Bear", "Boar", "Lion", "Tiger", "Spider", "Scorpion", "Croc", "Raptor", "Rex", "Hyena",
            "Fox", "Yeti", "Mammoth", "Rhino", "Shark", "Toad", "Snake", "Cobra", "Leopard", "Sabertooth", "Trillodon", "Emberwulf"};
    /** Compteur de coups (Sharur) et derniere onde (Guandao, evite qu'une onde en declenche une autre). */
    private static final java.util.Map<Ref<EntityStore>, Integer> SHARUR_HITS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final java.util.Map<Ref<EntityStore>, Long> GUANDAO_WAVE = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final float RYUKIRI_DRAGON_BONUS = 2.0f, RYUKIRI_FIRE_TAKEN = 0.6f;
    /** Dragons reconnus au nom de leur role (Snapdragon est une plante : exclue ; Ryu_Celeste = Boss Mondial Kokuō-Ryū). */
    private static final String[] DRAGONS = {"Dragon", "Wyvern", "Wyrm", "Drake", "Ryu_Celeste"};

    private static final float EXCALIBUR_BONUS = 1.5f, GRAM_BONUS = 1.6f, ROI_DECHU_BONUS = 1.4f;
    private static final float MASAMUNE_CRIT = 0.15f, ROI_DECHU_THRESHOLD = 0.5f;
    private static final double COLOSSAL_HEIGHT = 2.6, COLOSSAL_WIDTH = 1.8;     // un joueur : ~1,8 x 0,6 blocs
    static final String[] DARK = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost", "Void", "Shadow", "Spectr"};

    /** Identifiant de l'arme tenue par un joueur, sinon null. */
    static String held(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        var inv = player != null ? player.getInventory() : null;
        ItemStack item = inv != null ? inv.getActiveHotbarItem() : null;
        return item != null && !item.isEmpty() ? item.getItemId() : null;
    }

    /** Chance de critique supplementaire de l'arme tenue (Masamune). */
    public static boolean bonusCrit(Ref<EntityStore> attacker, Store<EntityStore> store) {
        String w = held(attacker, store);
        float chance = MASAMUNE.equals(w) ? MASAMUNE_CRIT : ARTEMIS.equals(w) ? 0.25f : 0f;
        return chance > 0 && ThreadLocalRandom.current().nextFloat() < chance;
    }

    /** Degats apres le bonus de l'artefact tenu contre cette cible. */
    public static float modifyDamage(float damage, Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        String weapon = held(attacker, store);
        if (weapon == null || victim == null || !victim.isValid()) return damage;
        LuoKatanas.Katana luo = LuoKatanas.of(weapon);                  // katanas du Clan Luo : LuoPassives
        if (luo != null) return LuoPassives.modifyDamage(damage, luo.id(), attacker, victim, store);
        if (Mousquet.isMusket(weapon)) return damage * Mousquet.meleeMultiplier(attacker, victim, store);   // force : baionnette (balles deja calculees)
        return switch (weapon) {
            case EXCALIBUR -> isDark(victim, store) ? damage * EXCALIBUR_BONUS : damage;
            case GRAM -> isColossal(victim, store) ? damage * GRAM_BONUS : damage;
            case ROI_DECHU -> healthRatio(victim, store) < ROI_DECHU_THRESHOLD ? damage * ROI_DECHU_BONUS : damage;
            case RYUKIRI -> isDragon(victim, store) ? damage * RYUKIRI_DRAGON_BONUS : damage;
            case RA -> isDark(victim, store) ? damage * 1.25f : damage;
            case DURANDAL -> healthRatio(attacker, store) < 0.5f ? damage * 1.3f : damage;
            case LABRYS -> healthRatio(victim, store) >= 0.99f ? damage * 1.35f : damage;
            case THANATOS -> healthRatio(victim, store) < 0.10f ? Math.max(damage, health(victim, store) + 1f) : damage;
            case MJOLLNIR -> {
                if (ThreadLocalRandom.current().nextFloat() >= 0.20f) yield damage;
                Vector3d p = SpellFx.position(victim, store);
                if (p != null) { SpellFx.fx("Taranis_Bolt", p, 1.0f, store, store); SpellFx.fx("Taranis_Electric_Burst", p.add(0, 1, 0), 0.8f, store, store); }
                com.eldanior.system.config.Effects.EffectsManager.applyEffect(victim, "Arme_Choc", store);
                yield damage * 1.5f;
            }
            case SHARUR -> {
                int n = SHARUR_HITS.merge(attacker, 1, Integer::sum);
                if (n % 4 != 0) yield damage;
                Vector3d p = SpellFx.position(victim, store);
                if (p != null) SpellFx.fx("Gilgamesh_Sword_Burst", p.add(0, 1, 0), 1.0f, store, store);
                yield damage * 2.5f;
            }
            case HERACLES -> isColossal(victim, store) || roleContains(victim, store, BEASTS) ? damage * 1.5f : damage;
            case CARNWENNAN -> isBehind(attacker, victim, store) ? damage * 1.6f : damage;
            case AIGLES -> damage * AiglesDuDesert.rhythmBonus(attacker);
            default -> damage;
        };
    }

    /** Effets a l'impact des armes divines (apres le calcul des degats) : etats, vol de vie, soins, ondes. */
    public static void onHit(float dealt, Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store,
                             com.hypixel.hytale.component.CommandBuffer<EntityStore> cb) {
        String weapon = held(attacker, store);
        if (weapon == null || victim == null || !victim.isValid()) return;
        LuoKatanas.Katana luo = LuoKatanas.of(weapon);
        if (luo != null) { LuoPassives.onHit(luo.id(), attacker, victim, store, cb); return; }
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        switch (weapon) {
            case RA -> SpellFx.effect(victim, "Arme_Embrasement", cb);
            case APOLLON -> { SpellFx.effect(victim, "Arme_Embrasement", cb); SpellFx.heal(attacker, dealt * 0.06f, cb); }
            case AIGLES -> AiglesDuDesert.onRhythmHit(attacker, store, cb);
            case CIRCE -> SpellFx.effect(victim, "Elda_Faiblesse", cb);
            case SKADI -> SpellFx.effect(victim, rng.nextFloat() < 0.15f ? "Elda_Gel" : "Arme_Givre", cb);
            case RUYI -> { if (rng.nextFloat() < 0.20f) SpellFx.stun(victim, "Taranis_Foudroye", 1_000, store, cb); }
            case THANATOS -> SpellFx.heal(attacker, dealt * 0.12f, cb);
            case DAGDA -> {
                Vector3d p = SpellFx.position(attacker, store);
                if (p == null) return;
                for (Ref<EntityStore> a : SpellFx.alliesAround(attacker, p, 8, store)) SpellFx.heal(a, dealt * 0.10f, cb);
            }
            case GUANDAO -> {
                long now = System.currentTimeMillis();
                Long last = GUANDAO_WAVE.get(attacker);
                if ((last != null && now - last < 600) || rng.nextFloat() >= 0.20f) return;
                GUANDAO_WAVE.put(attacker, now);
                Vector3d p = SpellFx.position(victim, store);
                if (p == null) return;
                SpellFx.fx("Gilgamesh_Sword_Burst", new Vector3d(p).add(0, 1, 0), 1.2f, store, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(attacker, p, 3.5, store))
                    if (!e.equals(victim)) SpellFx.damage(attacker, e, "Physical", dealt * 0.5f, null, "Impact_Light", cb);
            }
            default -> { }
        }
    }

    static boolean isBehindPublic(Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        return isBehind(attacker, victim, store);
    }

    private static boolean isBehind(Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        Vector3d a = SpellFx.position(attacker, store), v = SpellFx.position(victim, store);
        if (a == null || v == null) return false;
        Vector3d look = SpellFx.forward(victim, store);
        Vector3d toAttacker = new Vector3d(a.x - v.x, 0, a.z - v.z);
        look.y = 0;
        if (look.lengthSquared() < 1e-4 || toAttacker.lengthSquared() < 1e-4) return false;
        return look.normalize().dot(toAttacker.normalize()) < -0.3;            // l'attaquant est derriere la cible
    }

    private static float health(Ref<EntityStore> e, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(e, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue h = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return h != null ? h.get() : 0f;
    }

    /** Degats subis par le porteur apres la protection de l'artefact tenu (Ryukiri : feu reduit). */
    public static float modifyTaken(float damage, String causeId, Ref<EntityStore> victim, Store<EntityStore> store) {
        String w = held(victim, store);
        if (EGIDE.equals(w)) return damage * 0.75f;                          // l'Egide protege de tout
        if (causeId == null || !RYUKIRI.equals(w)) return damage;
        return causeId.contains("Fire") || causeId.contains("Burn") || causeId.contains("Lava") ? damage * RYUKIRI_FIRE_TAKEN : damage;
    }

    static boolean isDragon(Ref<EntityStore> e, Store<EntityStore> store) {
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        String type = npc != null ? npc.getNPCTypeId() : null;
        return type != null && !type.contains("Snapdragon") && roleContains(e, store, DRAGONS);
    }

    /** Mort-vivant ou creature du Neant, reconnu au nom de son role. */
    static boolean isDark(Ref<EntityStore> e, Store<EntityStore> store) {
        return roleContains(e, store, DARK);
    }

    private static boolean isColossal(Ref<EntityStore> e, Store<EntityStore> store) {
        if (roleContains(e, store, new String[]{"Dragon", "Giant", "Golem", "Titan"})) return true;
        BoundingBox bb = store.getComponent(e, BoundingBox.getComponentType());
        Box box = bb != null ? bb.getBoundingBox() : null;
        return box != null && (box.height() >= COLOSSAL_HEIGHT || box.getMaximumThickness() >= COLOSSAL_WIDTH);
    }

    private static boolean roleContains(Ref<EntityStore> e, Store<EntityStore> store, String[] keys) {
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        String type = npc != null ? npc.getNPCTypeId() : null;
        if (type == null) return false;
        for (String k : keys) if (type.contains(k)) return true;
        return false;
    }

    static float healthRatio(Ref<EntityStore> e, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(e, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue health = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return health != null && health.getMax() > 0 ? health.get() / health.getMax() : 1f;
    }
}
