package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.guards.AdventurerTier;
import com.eldanior.system.guards.AdventurerType;
import com.eldanior.system.guards.GuardManager;
import com.eldanior.system.guards.GuardType;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Sorts des aventuriers recrutes : de vraies runes du jeu, comme celles des joueurs. Ce sont les runes de sa classe
 * (ClassModel.getRuneIds, declarees comme ses passifs ; classe de base de son type s'il est ordinaire). En combat, a intervalle regulier (9 s au rang E, 5 s au rang S), il lance une rune prete dont la
 * situation s'y prete (ennemis au contact, cible a distance...). Lancement :
 *   - rune du jeu (Boule de feu, Fracas...) : son interaction racine Root_Ability_*, sur l'aventurier (sans emplacement
 *     de rune, son cout est nul : la recharge est geree ici) ;
 *   - rune du mod (moteur RuneSpell) : le sort lui-meme, l'aventurier etant le lanceur ; ses instances avancent ici.
 * Les degats passent par CombatStatsSystem : passifs, classe et statut de l'aventurier les renforcent. Ennemis : sa cible
 * verrouillee et les monstres ; jamais son joueur, son groupe, les gardes ni les invocations (isFoe, aussi utilise par
 * SpellFx.isEnemy quand le lanceur est un aventurier).
 */
public class AdventurerMagic extends EntityTickingSystem<EntityStore> {

    private static final long CALM_RESET_MS = 5_000, FIRST_CAST_DELAY_MS = 3_000, RETRY_MS = 1_000;
    private static final double CAST_RANGE = 25.0;

    /** Quand une rune se lance : buff au contact, zone autour de lui, cible a distance, cone, zone sous la cible, domaine. */
    public enum Use { MELEE_BUFF, NEAR_AOE, TARGET, CONE, ZONE, DOMAIN }

    /**
     * Rune d'un aventurier : l'objet rune (icone), son nom, sa rarete, ce qu'elle fait, sa recharge pour lui, quand la
     * lancer (et a quelle distance de sa cible), et son lancement : sort du moteur RuneSpell (spell) ou interaction racine
     * du jeu (root).
     */
    public record Rune(String itemId, String name, Rarity rarity, String description, long cooldownMs,
                       Use use, double minDist, double maxDist, String spell, String root) {}

    private static Rune ability(String rune, String name, Rarity rarity, String desc, int cdSeconds, Use use, double minDist, double maxDist) {
        return new Rune("Rune_" + rune, name, rarity, desc, cdSeconds * 1000L, use, minDist, maxDist, null, "Root_Ability_" + rune);
    }

    private static Rune spell(String rune, String name, Rarity rarity, String desc, int cdSeconds, Use use, double minDist, double maxDist) {
        return new Rune("Rune_" + rune, name, rarity, desc, cdSeconds * 1000L, use, minDist, maxDist, rune, null);
    }

    /** Runes qu'un aventurier sait lancer, par objet rune ; celles de sa classe hors de cette liste sont ignorees. */
    private static final Map<String, Rune> KNOWN = new LinkedHashMap<>();

    private static void known(Rune r) { KNOWN.put(r.itemId(), r); }

    static {
        known(ability("GroundSlam", "Fracas Terrestre", Rarity.RARE,
                "Frappe le sol : une onde de terre blesse les ennemis proches.", 12, Use.NEAR_AOE, 0, 4));
        known(ability("Enrage", "Rage", Rarity.RARE,
                "Entre en rage : ses dégâts augmentent quelques secondes.", 25, Use.MELEE_BUFF, 0, 4));
        known(ability("AuraBlade", "Aura Tranchante", Rarity.RARE,
                "Projette une lame de vent tranchante qui repousse.", 10, Use.TARGET, 2, 12));
        known(ability("Fireball", "Boule de Feu", Rarity.RARE,
                "Boule de feu qui explose sur sa cible.", 6, Use.TARGET, 2, 22));
        known(ability("ChargedShot", "Tir Chargé", Rarity.RARE,
                "Flèche d'eau chargée, plus puissante à pleine charge.", 10, Use.TARGET, 4, 22));
        known(ability("ChainHook", "Grappin", Rarity.RARE,
                "Harponne sa cible et l'attire à lui.", 12, Use.TARGET, 6, 18));
        known(ability("WindStrike", "Frappe du Vent", Rarity.RARE,
                "Lame de vent tranchante lancée sur sa cible.", 8, Use.TARGET, 2, 12));
        known(ability("PoisonImbue", "Lames Empoisonnées", Rarity.RARE,
                "Enduit ses dagues de poison : ses coups empoisonnent.", 20, Use.MELEE_BUFF, 0, 4));
        known(spell("Bourrasque", "Bourrasque", Rarity.COMMON,
                "Cône de vent devant lui : dégâts de vent, ennemis repoussés.", 15, Use.CONE, 0, 7));
        known(spell("PuitsGravitationnel", "Puits Gravitationnel", Rarity.UNIQUE,
                "Cratère sous sa cible : elle est clouée au sol 10 s.", 45, Use.ZONE, 0, 25));
        known(spell("AutoriteGravitationnelle", "Autorité Gravitationnelle", Rarity.LEGENDARY,
                "Domaine de 30 s : jusqu'à 10 ennemis autour de lui cloués au sol.", 120, Use.DOMAIN, 0, 20));
    }

    /** Classe de base de chaque type : ses runes sont celles des aventuriers ordinaires (sans classe). */
    private static final Map<AdventurerType, String> BASE_CLASS = Map.of(
            AdventurerType.GUERRIER, "warrior", AdventurerType.MAGE, "mage",
            AdventurerType.ARCHER, "archer", AdventurerType.ASSASSIN, "assassin");

    /**
     * Runes de l'aventurier de ce poste : celles de sa classe (ClassModel.getRuneIds, comme ses passifs), ou de la classe de
     * base de son type s'il est ordinaire.
     */
    public static List<Rune> runes(GuardManager.Post p) {
        ClassModel cls = AdventurerTier.classOf(p);
        if (cls == null) cls = ClassManager.get(BASE_CLASS.get(AdventurerType.of(p.getSkinSeed())));
        if (cls == null) return List.of();
        List<Rune> out = new ArrayList<>();
        for (String id : cls.getRuneIds()) {
            Rune r = KNOWN.get(id);
            if (r != null) out.add(r);
        }
        return out;
    }

    private static int rankIndex(int level) {
        return Math.max(0, "EDCBAS".indexOf(getAdventureRank(level)));
    }

    private static final Map<Ref<EntityStore>, Long> lastCast = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastFight = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Long> lastAura = new ConcurrentHashMap<>();
    /** Recharge de chaque rune, par aventurier. */
    private static final Map<Ref<EntityStore>, Map<String, Long>> runeReady = new ConcurrentHashMap<>();
    /** Sorts du moteur RuneSpell en cours, par aventurier lanceur. */
    private static final Map<Ref<EntityStore>, List<RuneSpell.Instance>> instances = new ConcurrentHashMap<>();
    /** Runes dont l'interaction racine a refuse de demarrer (signalees une fois). */
    private static final Set<String> refused = ConcurrentHashMap.newKeySet();

    /** Effet de la potion de soin d'un aventurier (AdventurerBehavior). */
    public static void potionFx(Vector3d at, Store<EntityStore> store) {
        SpellFx.fx("Apollo_Heal_Pulse", new Vector3d(at).add(0, 1, 0), 1.0f, store, store);
        SpellFx.sound("SFX_Deployable_Totem_Heal_Spawn", at, store);
    }

    /** Son de la potion que boit un aventurier. */
    public static void drinkSound(Vector3d at, Store<EntityStore> store) {
        SpellFx.sound("SFX_Health_Potion_High_Drink", at, store);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> me = chunk.getReferenceTo(index);
        if (!me.isValid()) return;
        NPCEntity npc = store.getComponent(me, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null || GuardType.fromRole(npc.getRoleName()) != GuardType.AVENTURIER) return;
        tickInstances(me, store, cb);
        GuardManager.Post post = GuardManager.adventurerPost(me, store);
        if (post == null) return;
        aura(me, post, store, cb);                                                                  // libre ou recrute
        if (!GuardType.isCompanionRole(npc.getRoleName()) || post.getOwner() == null) return;     // sorts : recrutes seulement
        long now = System.currentTimeMillis();
        Vector3d at = SpellFx.position(me, store);
        if (at == null) return;
        String state = StateSupport.get(me, store).getStateName();
        Ref<EntityStore> target = MarkedEntitySupport.get(me, store).getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        Vector3d tp = target != null && target.isValid() ? SpellFx.position(target, store) : null;
        boolean fighting = !state.startsWith("Idle") && !state.startsWith("ReturnHome") && tp != null && tp.distance(at) <= CAST_RANGE;
        if (!fighting) {
            Long lf = lastFight.get(me);
            if (lf != null && now - lf > CALM_RESET_MS) { lastFight.remove(me); lastCast.remove(me); }
            return;
        }
        lastFight.put(me, now);
        long every = 9_000 - rankIndex(post.getLevel()) * 800L;
        Long last = lastCast.get(me);
        if (last == null) { lastCast.put(me, now - every + FIRST_CAST_DELAY_MS); return; }      // entree en combat
        if (now - last < every) return;
        sweep();
        Rune rune = choose(me, post, at, tp, now, store);
        if (rune == null) { lastCast.put(me, now - every + RETRY_MS); return; }                   // rien d'utile : on reessaie
        lastCast.put(me, now);
        runeReady.computeIfAbsent(me, k -> new ConcurrentHashMap<>()).put(rune.itemId(), now + rune.cooldownMs());
        try {
            cast(me, rune, tp, store, cb);
        } catch (Exception e) {
            EldaniorLogger.error("[Aventurier] rune " + rune.itemId(), e);
        }
    }

    /** Premiere rune prete dont la situation s'y prete (ordre tourne a chaque lancer, pour varier). */
    private static Rune choose(Ref<EntityStore> me, GuardManager.Post post, Vector3d at, Vector3d tp, long now, Store<EntityStore> store) {
        List<Rune> known = runes(post);
        if (known.isEmpty()) return null;
        Map<String, Long> ready = runeReady.getOrDefault(me, Map.of());
        double dist = tp.distance(at);
        int start = (int) Math.floorMod(now / 1000, (long) known.size());
        for (int i = 0; i < known.size(); i++) {
            Rune r = known.get((start + i) % known.size());
            if (ready.getOrDefault(r.itemId(), 0L) > now) continue;
            boolean fits = switch (r.use()) {
                case MELEE_BUFF, TARGET, CONE, ZONE -> dist >= r.minDist() && dist <= r.maxDist();
                case NEAR_AOE -> !enemiesAround(me, at, r.maxDist() + 1, store).isEmpty();
                case DOMAIN -> enemiesAround(me, at, r.maxDist(), store).size() >= 3;
            };
            if (fits) return r;
        }
        return null;
    }

    /** Lance la rune : il se tourne vers sa cible, puis sort du moteur RuneSpell ou interaction racine du jeu. */
    private static void cast(Ref<EntityStore> me, Rune rune, Vector3d tp, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        CloneFx.faceTowards(me, new Vector3d(tp).add(0, 1.0, 0), store);
        if (rune.spell() != null) {
            RuneSpell spell = RuneSpellRuntime.get(rune.spell());
            if (spell == null) return;
            RuneSpell.Instance instance = spell.start(me, store, cb);
            if (instance != null) instances.computeIfAbsent(me, k -> new CopyOnWriteArrayList<>()).add(instance);
            return;
        }
        RootInteraction root = RootInteraction.getAssetMap().getAsset(rune.root());
        InteractionManager manager = store.getComponent(me, InteractionModule.get().getInteractionManagerComponent());
        if (root == null || manager == null) {
            if (refused.add(rune.root())) EldaniorLogger.warn("[Aventurier] rune " + rune.root() + " introuvable ou PNJ sans interactions");
            return;
        }
        InteractionContext ctx = InteractionContext.forInteraction(manager, me, InteractionType.Ability1, store);
        if (!manager.tryStartChain(me, cb, InteractionType.Ability1, ctx, root) && refused.add(rune.root()))
            EldaniorLogger.warn("[Aventurier] la rune " + rune.root() + " n'a pas demarre sur un aventurier");
    }

    /** Fait avancer ses sorts du moteur RuneSpell (zones, domaines) ; nettoyes a leur fin ou sur erreur. */
    private static void tickInstances(Ref<EntityStore> me, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<RuneSpell.Instance> list = instances.get(me);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (RuneSpell.Instance instance : list) {
            boolean done;
            try {
                done = instance.tick(me, now, store, cb);
            } catch (Exception e) {
                EldaniorLogger.error("[Aventurier] sort de rune interrompu sur erreur", e);
                done = true;
            }
            if (!done) continue;
            try { instance.cleanup(cb); } catch (Exception e) { EldaniorLogger.error("[Aventurier] nettoyage d'un sort", e); }
            list.remove(instance);
        }
        if (list.isEmpty()) instances.remove(me);
    }

    /** Oublie les aventuriers disparus ; leurs sorts en cours sont abandonnes (entites retirees par le moteur). */
    private static void sweep() {
        lastCast.keySet().removeIf(r -> !r.isValid());
        lastFight.keySet().removeIf(r -> !r.isValid());
        runeReady.keySet().removeIf(r -> !r.isValid());
        instances.entrySet().removeIf(e -> {
            if (e.getKey().isValid()) return false;
            for (RuneSpell.Instance i : e.getValue()) {
                try { i.abort(); } catch (Exception ex) { EldaniorLogger.error("[Aventurier] abandon d'un sort", ex); }
            }
            return true;
        });
    }

    // ===== Outils =====

    /**
     * Aura des exceptionnels et des heros : effet attache a l'entite (particules sur son torse, 8 s), reapplique toutes les
     * 5 s. Il suit ses deplacements et disparait avec lui (ne pas poser de particules dans le monde : elles y restent).
     */
    private static void aura(Ref<EntityStore> me, GuardManager.Post post, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        AdventurerTier tier = AdventurerTier.of(post);
        if (tier == AdventurerTier.ORDINAIRE) return;
        long now = System.currentTimeMillis();
        Long last = lastAura.get(me);
        if (last != null && now - last < 5_000) return;
        lastAura.put(me, now);
        if (lastAura.size() > 64) lastAura.keySet().removeIf(r -> !r.isValid());
        com.eldanior.system.config.Effects.EffectsManager.applyEffect(me,
                tier == AdventurerTier.HEROS ? "Ability_Aura_Visual_6" : "Ability_Aura_Visual_5", cb);
    }

    /** Lanceur aventurier (ses sorts de rune visent selon isFoe). */
    static boolean isAdventurer(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid() || store.getComponent(ref, Player.getComponentType()) != null) return false;
        NPCEntity n = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        return n != null && GuardType.fromRole(n.getRoleName()) == GuardType.AVENTURIER;
    }

    /** Ennemi d'un aventurier : sa cible verrouillee et les monstres ; ni joueurs, ni allies, ni gardes, ni invocations. */
    static boolean isFoe(Ref<EntityStore> me, Ref<EntityStore> e, Store<EntityStore> store) {
        if (e == null || !e.isValid() || e.equals(me) || RuneSpellRuntime.isSummon(e)) return false;
        if (GuardManager.blocksFriendlyFire(me, e, store)) return false;                            // son joueur, son groupe
        Ref<EntityStore> locked = MarkedEntitySupport.get(me, store).getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
        if (e.equals(locked)) return true;
        if (store.getComponent(e, Player.getComponentType()) != null) return false;
        NPCEntity n = store.getComponent(e, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (n == null || GuardType.isAlly(n.getRoleName())) return false;                              // criminels : ennemis
        return GuardType.isOutlaw(n.getRoleName()) || WorldSupport.get(e, store).getDefaultPlayerAttitude() == Attitude.HOSTILE;
    }

    /** Ennemis autour d'un point (isFoe). */
    private static List<Ref<EntityStore>> enemiesAround(Ref<EntityStore> me, Vector3d center, double radius, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(center, radius, store)))
            if (isFoe(me, e, store)) out.add(e);
        return out;
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
