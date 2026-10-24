package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aventurier : recrutement contre de l'or. Interagir (touche d'utilisation) avec un aventurier libre ouvre sa fenetre
 * (RecruitScreen : presentation, niveau, prix selon le niveau, duree). « Recruter » : l'or est preleve, l'aventurier
 * recrutable disparait et son double « compagnon » apparait a cote du joueur. Le compagnon suit son joueur (point de laisse
 * place derriere lui, recherche de chemin) et combat ce que le joueur frappe et ceux qui le frappent. Un aventurier par
 * joueur. Fin du contrat (duree ecoulee, joueur deconnecte) : il retourne a son poste ; mort : il y reapparait.
 */
public class AdventurerSystem extends EntityTickingSystem<EntityStore> {

    private static final double FOLLOW_BEHIND = 2.5, FOLLOW_SIDE = 1.2, CATCH_UP = 40.0;
    private static final double FIGHT_SECONDS = 15.0, DANGER_RANGE = 14.0;
    /**
     * Regeneration identique a celle des joueurs (GlobalRegenSystem : taux VITALITY par tick, 20 ticks par seconde).
     * Lue a l'usage, JAMAIS dans un champ static : ce systeme est cree au demarrage du plugin, avant le chargement des types
     * de stats ; initialiser StatConfig a ce moment fige de mauvais identifiants de stats (la mana du vol lisait une autre stat).
     */
    private static float regenPerSecond() {
        return com.eldanior.system.config.configs.StatConfig.VITALITY.getRegenRate();
    }
    private static final Set<Integer> inInteraction = ConcurrentHashMap.newKeySet();   // PNJ deja traites pour l'interaction en cours

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null || !GuardType.isRecruitRole(npc.getRoleName())) return;
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        int key = System.identityHashCode(npc);
        if (state == null || !state.contains("$Interaction")) { inInteraction.remove(key); return; }
        if (!inInteraction.add(key)) return;                                            // deja traite pour cette interaction
        MarkedEntitySupport marks = MarkedEntitySupport.get(ref, store);
        Ref<EntityStore>[] targets = marks != null ? marks.getEntityTargets() : null;
        if (targets == null) return;
        for (Ref<EntityStore> t : targets) {
            Player player = t != null && t.isValid() ? store.getComponent(t, Player.getComponentType()) : null;
            PlayerRef pr = player != null ? store.getComponent(t, PlayerRef.getComponentType()) : null;
            if (pr == null) continue;
            GuardManager.Post post = GuardManager.postOf(ref, store);
            if (post != null)
            {
                PlayerLevelData data = store.getComponent(t, EldaniorSystem.get().getPlayerLevelDataType());
                player.getPageManager().openCustomPage(t, store, new RecruitScreen(pr, post.id, post.level,
                        GuardManager.priceOf(post), data != null ? data.getMoney() : 0,
                        GuardManager.nameplate(post)));
            }
            return;
        }
    }

    /** Bouton « Recruter » de la fenetre : verifications, paiement, puis remplacement par le compagnon. */
    static void confirm(String postId, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        GuardManager.Post post = GuardManager.post(postId);
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        UUIDComponent u = store.getComponent(playerRef, UUIDComponent.getComponentType());
        if (post == null || pr == null || u == null) return;
        UUID player = u.getUuid();
        if (GuardManager.companionsOf(player).size() >= GuardManager.MAX_COMPANIONS) {
            say(pr, "Vous avez deja " + GuardManager.MAX_COMPANIONS + " aventuriers a vos cotes.");
            return;
        }
        if (post.owner != null || !post.isAlive()) { say(pr, "Cet aventurier n'est plus disponible."); return; }
        long price = GuardManager.priceOf(post);
        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || !data.removeMoney(price)) {
            say(pr, "Aventurier : « Revenez avec " + price + " pieces d'or. » (vous avez " + (data != null ? data.getMoney() : 0) + " or)");
            return;
        }
        World world = store.getExternalData().getWorld();
        TransformComponent pt = store.getComponent(playerRef, TransformComponent.getComponentType());
        Vector3d near = pt != null ? new Vector3d(pt.getPosition()) : post.position();
        world.execute(() -> recruit(post, player, price, near, world));               // hors du tick : retrait et apparition
        say(pr, "L'aventurier (niveau " + post.level + ") vous suit pendant " + GuardManager.CONTRACT_MS / 60_000 + " minutes (-" + price + " or).");
    }

    /** Remplace l'aventurier recrutable par son compagnon, a cote du joueur. */
    private static void recruit(GuardManager.Post post, UUID player, long price, Vector3d near, World world) {
        try {
            Store<EntityStore> store = world.getEntityStore().getStore();
            removeCurrent(post, world);
            post.lastHp = 0;                                                                  // nouveau contrat : vie pleine
            post.owner = player;
            post.contractEnd = System.currentTimeMillis() + GuardManager.CONTRACT_MS;
            if (GuardManager.spawnAs(post, world, AdventurerType.of(post.skinSeed).getCompanionRole(), new Vector3d(near).add(1.5, 0, 1.5)) == null) {
                post.owner = null;                                                            // echec : on rend l'or
                PlayerRef pr = Universe.get().getPlayer(player);
                Ref<EntityStore> pref = pr != null ? pr.getReference() : null;
                PlayerLevelData data = pref != null ? store.getComponent(pref, EldaniorSystem.get().getPlayerLevelDataType()) : null;
                if (data != null) data.addMoney(price);
                GuardManager.spawn(post, world);
            }
            if (post.owner != null) AdventurerBehavior.onRecruited(post);
            GuardManager.saveLater();
        } catch (Exception e) {
            EldaniorLogger.error("[Aventurier] recrutement", e);
        }
    }

    /**
     * Chaque seconde : le compagnon suit son joueur (point de laisse derriere lui, a gauche pour le premier, a droite pour le
     * second ; le role y marche ou court avec la recherche de chemin) ; trop loin (teleportation...), il le rejoint. Il
     * guette les dangers (monstres pres de son joueur ou de lui) et rejoint le groupe de son joueur.
     */
    static void follow(GuardManager.Post p, Ref<EntityStore> ref, NPCEntity npc, Vector3d at, World world, Store<EntityStore> store) {
        limitRegen(p, ref, store);
        p.hpRatio = hpRatio(ref, store);
        syncParty(p);
        PlayerRef pr = Universe.get().getPlayer(p.owner);
        Ref<EntityStore> owner = pr != null ? pr.getReference() : null;
        if (owner == null || !owner.isValid() || owner.getStore() != store) return;           // autre monde : checkContracts
        TransformComponent t = store.getComponent(owner, TransformComponent.getComponentType());
        if (t == null) return;
        Vector3d op = new Vector3d(t.getPosition());
        float yaw = t.getRotation().yaw();
        double fx = PhysicsMath.headingX(yaw), fz = PhysicsMath.headingZ(yaw);
        double side = GuardManager.companionsOf(p.owner).indexOf(p) == 0 ? FOLLOW_SIDE : -FOLLOW_SIDE;
        Vector3d spot = new Vector3d(op.x - fx * FOLLOW_BEHIND - fz * side, op.y, op.z - fz * FOLLOW_BEHIND + fx * side);
        if (at.distance(op) > CATCH_UP) {                                                    // il le rejoint, avec la vie qu'il avait
            removeCurrent(p, world);
            Ref<EntityStore> again = GuardManager.spawnAs(p, world, AdventurerType.of(p.skinSeed).getCompanionRole(), spot);
            if (again != null && p.lastHp > 0) setHealth(again, p.lastHp, store);
            return;
        }
        npc.setLeashPoint(spot);
        if (AdventurerBehavior.tick(p, ref, at, op, owner, store)) return;              // repli en cours : il ne combat pas
        guard(p, ref, owner, op, at, store);
    }

    /** Detection du danger : le monstre le plus proche de son joueur (ou de lui) devient sa cible, s'il est libre. */
    private static void guard(GuardManager.Post p, Ref<EntityStore> ref, Ref<EntityStore> owner, Vector3d ownerPos, Vector3d at, Store<EntityStore> store) {
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        if (state == null || !state.startsWith("Idle")) return;                              // deja en combat
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Vector3d center : new Vector3d[]{ownerPos, at})
            for (Ref<EntityStore> e : new java.util.ArrayList<>(com.hypixel.hytale.server.core.util.TargetUtil.getAllEntitiesInSphere(center, DANGER_RANGE, store))) {
                NPCEntity n = e != null && e.isValid() ? store.getComponent(e, NPCEntity.getComponentType()) : null;
                if (n == null || GuardType.fromRole(n.getRoleName()) != null) continue;
                com.hypixel.hytale.server.npc.role.support.WorldSupport ws = com.hypixel.hytale.server.npc.role.support.WorldSupport.get(e, store);
                if (ws == null || ws.getDefaultPlayerAttitude() != com.hypixel.hytale.server.core.asset.type.attitude.Attitude.HOSTILE) continue;
                TransformComponent et = store.getComponent(e, TransformComponent.getComponentType());
                if (et == null) continue;
                double d = et.getPosition().distanceSquared(ownerPos);
                if (d < bestD) { bestD = d; best = e; }
            }
        if (best == null) return;
        AdventurerBehavior.warn(p, best, store);
        NpcAttitudes.hostile(ref, best, FIGHT_SECONDS, store);
        GuardManager.call(ref, best, store);
    }

    /** Le compagnon est membre du groupe de son joueur (il le suit s'il en change, et le quitte s'il n'en a plus). */
    private static void syncParty(GuardManager.Post p) {
        com.eldanior.system.party.Party now = com.eldanior.system.party.PartyManager.getParty(p.owner);
        if (p.party != null && p.party != now) { p.party.removeMember(p.guard); p.party = null; }
        if (now != null && p.guard != null && !now.isMember(p.guard) && now.addMember(p.guard, GuardManager.nameplate(p))) p.party = now;
        else if (now != null && p.guard != null && now.isMember(p.guard)) p.party = now;
    }

    private static void leaveParty(GuardManager.Post p) {
        if (p.party != null && p.guard != null) p.party.removeMember(p.guard);
        p.party = null;
    }

    /**
     * Vie de l'aventurier recrute : meme regeneration qu'un joueur (regenPerSecond() de sa vie max par seconde). Le gabarit
     * remet la vie au maximum a chaque retour au point de laisse (qui suit le joueur) : ce soin est annule ici.
     */
    private static void limitRegen(GuardManager.Post p, Ref<EntityStore> ref, Store<EntityStore> store) {
        var stats = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
        int health = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth();
        var hp = stats != null ? stats.get(health) : null;
        if (hp == null) return;
        float cur = hp.get(), allowed = p.lastHp + hp.getMax() * regenPerSecond() * AdventurerPassives.regenMultiplier(p);
        if (p.lastHp > 0 && cur > allowed) { stats.setStatValue(health, allowed); cur = allowed; }
        p.lastHp = cur;
    }

    private static void setHealth(Ref<EntityStore> ref, float value, Store<EntityStore> store) {
        var stats = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null) stats.setStatValue(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), value);
    }

    private static float hpRatio(Ref<EntityStore> ref, Store<EntityStore> store) {
        var stats = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
        var hp = stats != null ? stats.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth()) : null;
        return hp != null && hp.getMax() > 0 ? hp.get() / hp.getMax() : 1f;
    }

    /**
     * Appele par GuardAlertSystem a chaque coup : si un joueur frappe ou est frappe, ses aventuriers et ceux de son groupe
     * s'en prennent a l'autre.
     */
    static void onOwnerFight(Ref<EntityStore> player, Ref<EntityStore> enemy, ComponentAccessor<EntityStore> store) {
        if (enemy == null || !enemy.isValid()) return;
        UUID u = PostMovement.uuid(player, store);
        if (u == null) return;
        java.util.List<GuardManager.Post> defenders = new java.util.ArrayList<>(GuardManager.companionsOf(u));
        com.eldanior.system.party.Party party = com.eldanior.system.party.PartyManager.getParty(u);
        if (party != null)
            for (UUID m : party.getMemberUUIDs()) if (!m.equals(u)) defenders.addAll(GuardManager.companionsOf(m));
        if (defenders.isEmpty()) return;
        World world = store.getExternalData().getWorld();
        if (world == null) return;
        for (GuardManager.Post p : defenders) {
            if (AdventurerBehavior.isRetreating(p)) continue;
            Ref<EntityStore> companion = p.guard != null ? world.getEntityStore().getRefFromUUID(p.guard) : null;
            if (companion == null || !companion.isValid() || companion.equals(enemy)) continue;
            if (GuardManager.blocksFriendlyFire(companion, enemy, store)) continue;             // pas contre un allie
            NpcAttitudes.hostile(companion, enemy, FIGHT_SECONDS, store);
            GuardManager.call(companion, enemy, store);
        }
    }

    /** Toutes les 5 s : contrat expire ou joueur deconnecte -> l'aventurier retourne a son poste. */
    static void checkContracts() {
        long now = System.currentTimeMillis();
        for (GuardManager.Post p : GuardManager.getPosts()) {
            if (p.owner == null || p.respawnAt > 0) continue;
            PlayerRef pr = Universe.get().getPlayer(p.owner);
            boolean offline = pr == null || pr.getReference() == null;
            if (!offline && now < p.contractEnd) continue;
            if (!offline) AdventurerBehavior.talk(p, "Mission accomplie. Ce fut un honneur, je retourne à mon poste.", true);
            World world = Universe.get().getWorld(p.world);
            if (world == null) continue;
            world.execute(() -> {
                removeCurrent(p, world);
                p.owner = null;
                p.contractEnd = 0;
                GuardManager.spawn(p, world);
                GuardManager.saveLater();
            });
        }
    }

    /** Le compagnon est mort : fin du contrat (GuardManager le fera reapparaitre a son poste). */
    static void onCompanionDeath(GuardManager.Post p) {
        leaveParty(p);
        PlayerRef pr = Universe.get().getPlayer(p.owner);
        if (pr != null) AdventurerBehavior.talk(p, "Je… ne peux plus continuer… Je vous attendrai à mon poste.", true);
        p.owner = null;
        p.contractEnd = 0;
    }

    private static void removeCurrent(GuardManager.Post p, World world) {
        leaveParty(p);
        if (p.guard == null) return;
        Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(p.guard);
        if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        p.guard = null;
    }

    private static void say(PlayerRef pr, String text) {
        if (pr != null) pr.sendMessage(Message.raw(text));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
