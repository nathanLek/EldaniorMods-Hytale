package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Moteur commun des sorts de rune : registre, file de lancement, instances actives par joueur.
 *
 * Garanties :
 *  - un sort qui leve une exception est arrete et nettoye, sans faire tomber le monde ;
 *  - un meme lancement n'est execute qu'une fois (file videe a chaque tick) ;
 *  - deconnexion : instances abandonnees et file videe ;
 *  - lanceur invalide : instances nettoyees.
 */
public final class RuneSpellRuntime {

    private RuneSpellRuntime() {}

    private static final Map<String, RuneSpell> spells = new ConcurrentHashMap<>();
    private static final Map<UUID, Queue<RuneSpell>> pending = new ConcurrentHashMap<>();
    private static final Map<UUID, List<RuneSpell.Instance>> active = new ConcurrentHashMap<>();
    /** Clics recus par l'evenement souris (thread reseau), livres au prochain tick du joueur. */
    private record Click(boolean left, Ref<EntityStore> target) {}
    private static final Map<UUID, Queue<Click>> clicks = new ConcurrentHashMap<>();
    /** Interactions deja vues par joueur (detection des nouveaux clics) et dernier clic livre par bouton (anti-doublon). */
    private static final Map<UUID, Set<Integer>> seenPrimary = new ConcurrentHashMap<>(), seenSecondary = new ConcurrentHashMap<>();
    private static final Map<UUID, long[]> lastClick = new ConcurrentHashMap<>();
    private static final long CLICK_DEDUP_MS = 250;
    private static final double CLICK_TARGET_RANGE = 30.0;
    /** Entites a retirer des qu'un CommandBuffer est disponible (sorts abandonnes a la deconnexion). */
    private static final Queue<Ref<EntityStore>> orphans = new ConcurrentLinkedQueue<>();

    public static void register(RuneSpell spell) {
        if (spells.putIfAbsent(spell.id(), spell) != null) EldaniorLogger.warn("[RuneSpell] Sort deja enregistre : " + spell.id());
    }

    public static RuneSpell get(String id) { return id == null ? null : spells.get(id); }

    /** Appele par l'interaction JSON : le sort part au prochain tick du lanceur. */
    static void queue(Ref<EntityStore> caster, String spellId) {
        RuneSpell spell = get(spellId);
        if (spell == null) { EldaniorLogger.warn("[RuneSpell] Sort inconnu : " + spellId + " (enregistre-le dans RuneSpells)"); return; }
        UUID uuid = SpellFx.uuid(caster, caster.getStore());
        if (uuid == null) return;
        pending.computeIfAbsent(uuid, k -> new ConcurrentLinkedQueue<>()).add(spell);
    }

    /** A appeler depuis Instance.abort() : l'entite sera retiree au prochain tick d'un joueur du meme monde. */
    static void removeLater(Ref<EntityStore> ref) {
        if (ref != null) orphans.add(ref);
    }

    /** Evenement souris global (enregistre dans EldaniorSystem) : retenu seulement si un sort actif du joueur ecoute les clics. */
    public static void onMouseButton(com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent e) {
        try {
            var mb = e.getMouseButton();
            if (mb == null || mb.state != com.hypixel.hytale.protocol.MouseButtonState.Pressed || e.getPlayerRefComponent() == null) return;
            boolean left = mb.mouseButtonType == com.hypixel.hytale.protocol.MouseButtonType.Left;
            if (!left && mb.mouseButtonType != com.hypixel.hytale.protocol.MouseButtonType.Right) return;
            UUID uuid = e.getPlayerRefComponent().getUuid();
            List<RuneSpell.Instance> list = uuid == null ? null : active.get(uuid);
            if (list == null || list.stream().noneMatch(RuneSpell.Instance::listensClicks)) return;
            clicks.computeIfAbsent(uuid, k -> new ConcurrentLinkedQueue<>()).add(new Click(left, e.getTargetEntityRef()));
        } catch (Exception ex) {
            EldaniorLogger.error("[RuneSpell] clic", ex);
        }
    }

    /** Coups « echo » (rejoues par un sort) a ne pas renvoyer au sort : evite les boucles infinies de copie. */
    private record Echo(Ref<EntityStore> target, float amount, long until) {}
    private static final Queue<Echo> echoes = new ConcurrentLinkedQueue<>();

    /** A appeler juste avant d'infliger un coup copie : ce coup ne sera pas renvoye a onOwnerHit. */
    static void markEcho(Ref<EntityStore> target, float amount) {
        echoes.add(new Echo(target, amount, System.currentTimeMillis() + 1_000));
    }

    /** Invocations des sorts (clones, doubles) : leur lanceur, et si elles sont intouchables. */
    private record Summon(Ref<EntityStore> owner, boolean invulnerable) {}
    private static final Map<Ref<EntityStore>, Summon> summons = new ConcurrentHashMap<>();

    static void registerSummon(Ref<EntityStore> owner, Ref<EntityStore> summon, boolean invulnerable) {
        if (owner != null && summon != null) summons.put(summon, new Summon(owner, invulnerable));
    }

    static void unregisterSummon(Ref<EntityStore> summon) {
        if (summon != null) summons.remove(summon);
    }

    /** Invocations mortelles qui ont recu un coup fatal : le sort les dissipe au lieu de les laisser mourir (pas de butin). */
    private static final Set<Ref<EntityStore>> dying = ConcurrentHashMap.newKeySet();

    static boolean isSummon(Ref<EntityStore> ref) { return ref != null && summons.containsKey(ref); }

    /**
     * Role PNJ d'une invocation de sort (clones, double) : les systemes des monstres (niveau, PV virtuels, plaque de nom,
     * reduction de degats) doivent l'ignorer, sinon la copie trahit le lanceur et perd ses PV calcules par le sort.
     */
    public static boolean isSummonRole(String npcTypeId) {
        return npcTypeId != null && (npcTypeId.startsWith("Eldanior_Clone_") || npcTypeId.equals("Eldanior_Doppel"));
    }

    static boolean isDying(Ref<EntityStore> summon) { return summon != null && dying.contains(summon); }

    static void forgetSummon(Ref<EntityStore> summon) {
        unregisterSummon(summon);
        if (summon != null) dying.remove(summon);
    }

    /**
     * Appele par DivineDamageSystem : true si le coup doit etre annule (invocation intouchable, coup entre un lanceur et ses
     * propres invocations ou entre deux invocations du meme lanceur, ou coup fatal sur une invocation : elle se dissipe).
     */
    public static boolean blocksDamage(Ref<EntityStore> attacker, Ref<EntityStore> target, float amount, Store<EntityStore> store) {
        if (summons.isEmpty() || target == null) return false;
        Summon st = summons.get(target);
        if (st != null && (st.invulnerable() || dying.contains(target))) return true;
        Summon sa = attacker != null ? summons.get(attacker) : null;
        if (attacker != null && (sa != null || st != null)) {
            Ref<EntityStore> oa = sa != null ? sa.owner() : attacker, ot = st != null ? st.owner() : target;
            if (oa.equals(ot)) return true;
        }
        if (st == null) return false;
        try {
            var stats = store.getComponent(target, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
            var hp = stats != null ? stats.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth()) : null;
            if (hp != null && hp.get() - amount <= 1f) { dying.add(target); return true; }
        } catch (Exception e) {
            EldaniorLogger.error("[RuneSpell] vie d'une invocation", e);
        }
        return false;
    }

    /** Appele par DivineDamageSystem pour chaque coup inflige par une entite (montant brut). */
    public static void onDamageDealt(Ref<EntityStore> attacker, Ref<EntityStore> target, float amount) {
        if (active.isEmpty() || attacker == null || !attacker.isValid()) return;
        long now = System.currentTimeMillis();
        echoes.removeIf(e -> e.until() < now);
        for (Echo e : echoes) {
            if (e.target().equals(target) && Math.abs(e.amount() - amount) < 0.01f) { echoes.remove(e); return; }
        }
        UUID uuid = SpellFx.uuid(attacker, attacker.getStore());
        List<RuneSpell.Instance> list = uuid == null ? null : active.get(uuid);
        if (list == null) return;
        for (RuneSpell.Instance instance : list) {
            try { instance.onOwnerHit(target, amount); } catch (Exception ex) { EldaniorLogger.error("[RuneSpell] coup", ex); }
        }
    }

    /** Tick du lanceur (DivineSpellsSystem). */
    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (int n = orphans.size(); n > 0; n--) {
            Ref<EntityStore> ref = orphans.poll();
            if (ref == null) break;
            if (!ref.isValid()) continue;
            if (ref.getStore() == store) SpellFx.remove(ref, cb); else orphans.add(ref);
        }
        Queue<RuneSpell> queue = pending.get(uuid);
        if (queue != null) {
            for (RuneSpell spell; (spell = queue.poll()) != null; ) {
                try {
                    RuneSpell.Instance instance = spell.start(caster, store, cb);
                    if (instance != null) active.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(instance);
                } catch (Exception e) {
                    EldaniorLogger.error("[RuneSpell] " + spell.id() + " : lancement", e);
                }
            }
            if (queue.isEmpty()) pending.remove(uuid);
        }
        List<RuneSpell.Instance> list = active.get(uuid);
        Queue<Click> pendingClicks = clicks.remove(uuid);
        if (list == null) { seenPrimary.remove(uuid); seenSecondary.remove(uuid); return; }
        boolean listening = list.stream().anyMatch(RuneSpell.Instance::listensClicks);
        List<Click> all = new ArrayList<>();
        if (pendingClicks != null) all.addAll(pendingClicks);
        if (listening) pollInteractions(uuid, caster, store, all); else { seenPrimary.remove(uuid); seenSecondary.remove(uuid); }
        long nowClick = System.currentTimeMillis();
        long[] last = lastClick.computeIfAbsent(uuid, k -> new long[]{0, 0});
        for (Click c : all) {
            int b = c.left() ? 0 : 1;
            if (nowClick - last[b] < CLICK_DEDUP_MS) continue;                                   // meme clic vu par les deux voies
            last[b] = nowClick;
            for (RuneSpell.Instance instance : list) {
                if (!instance.listensClicks()) continue;
                try { instance.onClick(c.left(), c.target(), caster, store, cb); } catch (Exception e) { EldaniorLogger.error("[RuneSpell] clic", e); }
            }
        }
        long now = System.currentTimeMillis();
        for (RuneSpell.Instance instance : list) {
            boolean done;
            try {
                done = !caster.isValid() || instance.tick(caster, now, store, cb);
            } catch (Exception e) {
                EldaniorLogger.error("[RuneSpell] sort interrompu sur erreur", e);
                done = true;
            }
            if (!done) continue;
            try { instance.cleanup(cb); } catch (Exception e) { EldaniorLogger.error("[RuneSpell] nettoyage", e); }
            list.remove(instance);
        }
        if (list.isEmpty()) active.remove(uuid);
    }

    /**
     * Nouveaux clics detectes par les chaines d'interaction (Primary = clic gauche, Secondary = clic droit) : fonctionne
     * meme quand l'evenement souris n'est pas emis (rune en main). La premiere lecture sert de reference (pas de clic).
     */
    private static void pollInteractions(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, List<Click> out) {
        Set<Integer> primary = SpellFx.chains(caster, store, com.hypixel.hytale.protocol.InteractionType.Primary);
        Set<Integer> secondary = SpellFx.chains(caster, store, com.hypixel.hytale.protocol.InteractionType.Secondary);
        Set<Integer> oldP = seenPrimary.put(uuid, primary), oldS = seenSecondary.put(uuid, secondary);
        boolean left = oldP != null && primary.stream().anyMatch(id -> !oldP.contains(id));
        boolean right = oldS != null && secondary.stream().anyMatch(id -> !oldS.contains(id));
        if (!left && !right) return;
        Ref<EntityStore> target = SpellFx.targetLiving(caster, store, CLICK_TARGET_RANGE);
        if (left) out.add(new Click(true, target));
        if (right) out.add(new Click(false, target));
    }

    public static void handleDisconnect(UUID uuid) {
        pending.remove(uuid);
        clicks.remove(uuid);
        seenPrimary.remove(uuid);
        seenSecondary.remove(uuid);
        lastClick.remove(uuid);
        List<RuneSpell.Instance> list = active.remove(uuid);
        if (list == null) return;
        for (RuneSpell.Instance instance : list) {
            try { instance.abort(); } catch (Exception e) { EldaniorLogger.error("[RuneSpell] abandon", e); }
        }
    }
}
