package com.eldanior.system.luo;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.guards.LuoNpcs;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Territoire du Clan Luo et inclinaison des PNJ, chaque seconde pour chaque joueur.
 *
 * Territoire (parcelles dont la famille est luo) : un etranger est laisse en paix s'il porte la plaque a laquelle il a droit
 * (LuoPlaques.admitted : membre du clan ou marchand agree), s'il est admin, ou s'il est dans le groupe d'un membre du clan
 * present a moins de {@link #ESCORT_RANGE} blocs. Sinon : avertissement a l'entree, {@link #GRACE_MS} de grace, puis les
 * combattants Luo (garde, Ombre, disciple) a moins de {@link #ATTACK_RANGE} blocs le prennent pour cible tant qu'il reste.
 * Ressortir moins de {@link #FORGET_MS} ne rend pas la grace.
 *
 * Inclinaison : tenir en main sa plaque de Patriarche, de Doyen ou de Seigneur fait s'incliner les PNJ Luo a moins de
 * {@link #BOW_RANGE} blocs (profondement devant le Patriarche), une fois par minute et par personne.
 */
public class LuoClanSystem extends DelayedEntitySystem<EntityStore> {

    static final double BOW_RANGE = 6.0, ATTACK_RANGE = 40.0, ESCORT_RANGE = 48.0;
    static final long GRACE_MS = 10_000, FORGET_MS = 30_000, BOW_COOLDOWN_MS = 60_000;
    private static final double HOSTILE_SECONDS = 6.0;

    private static final class Trespass {
        final long since;
        long lastInside, lastAttack;
        boolean warned, attacked;
        Trespass(long now) { since = now; lastInside = now; }
    }

    private final Map<UUID, Trespass> trespass = new ConcurrentHashMap<>();
    private final Map<String, Long> bows = new ConcurrentHashMap<>();                 // pnj|joueur -> derniere inclinaison
    private volatile long lastCleanup;

    public LuoClanSystem() { super(1f); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        try {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            World world = store.getExternalData().getWorld();
            if (player == null || pr == null || t == null || world == null) return;
            UUID uuid = UUIDExtractor.getUUID(pr);
            if (uuid == null) return;
            Vector3d pos = new Vector3d(t.getPosition());
            long now = System.currentTimeMillis();
            if (now - lastCleanup > BOW_COOLDOWN_MS) {
                lastCleanup = now;
                bows.values().removeIf(at -> now - at > BOW_COOLDOWN_MS);
            }

            LuoPlaques.Plaque held = LuoPlaques.held(player);
            boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
            if (held != null && held.commandsBow() && (admin || held == LuoPlaques.owned(data, uuid))) {
                bowAround(uuid, pos, held, store, now);
            }

            if (!onLuoLand(world.getName(), pos)) {
                Trespass tr = trespass.get(uuid);
                if (tr != null && now - tr.lastInside > FORGET_MS) trespass.remove(uuid);
                return;
            }
            if (admin || LuoPlaques.admitted(player, data, uuid) || escorted(uuid, world, pos)) {
                trespass.remove(uuid);
                return;
            }
            Trespass tr = trespass.computeIfAbsent(uuid, k -> new Trespass(now));
            tr.lastInside = now;
            if (!tr.warned) {
                tr.warned = true;
                NotificationHelper.showEventTitle(pr, "Territoire du Clan Luo", "Étranger sans plaque : quittez la vallée", true);
                NotificationHelper.sendDanger(pr, "Vous entrez sur le territoire du Clan Luo sans plaque d'identification. Faites demi-tour : les gardes attaqueront dans "
                        + GRACE_MS / 1000 + " secondes.");
            }
            if (now - tr.since < GRACE_MS || now - tr.lastAttack < 1_900) return;
            tr.lastAttack = now;
            int n = 0;
            for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(pos, ATTACK_RANGE, store))) {
                if (!LuoNpcs.isFighter(e, store)) continue;
                LuoNpcs.attack(e, ref, HOSTILE_SECONDS, store);
                n++;
            }
            if (n > 0 && !tr.attacked) {
                tr.attacked = true;
                NotificationHelper.sendDanger(pr, "Les gens du Clan Luo vous prennent pour cible !");
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Luo] territoire", e);
        }
    }

    /** Position sur une parcelle du Clan Luo (les parcelles enfants heritent de la famille de leur parent). */
    public static boolean onLuoLand(String world, Vector3d pos) {
        ParcelData parcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);
        return parcel != null && LuoPlaques.FAMILY.equalsIgnoreCase(parcel.getFamilyId());
    }

    /**
     * Empire du Clan Luo (choix de l'utilisateur, 2026-11-10 : un petit empire independant) : le marquisat Luo dont fait partie
     * la parcelle (elle-meme ou un ancetre), null hors de l'empire.
     */
    public static ParcelData luoRealm(ParcelData parcel) {
        for (int depth = 0; parcel != null && depth < 16; depth++) {
            if (parcel.getType() == com.eldanior.system.territory.ParcelType.GRAND_TERRITORY
                    && LuoPlaques.FAMILY.equalsIgnoreCase(parcel.getFamilyId())) return parcel;
            String parent = parcel.getParentId();
            parcel = parent != null && !parent.isEmpty() ? ParcelManager.get(parent) : null;
        }
        return null;
    }

    /** Duche a l'interieur de l'empire Luo : ni titre a l'entree ni marque sur la carte, seul le marquisat compte. */
    public static boolean hiddenInLuoRealm(ParcelData parcel) {
        return parcel != null && parcel.getType() == com.eldanior.system.territory.ParcelType.TERRITORY && luoRealm(parcel) != null;
    }

    /** Dans le groupe d'un membre du clan proche (meme monde, moins de ESCORT_RANGE blocs) ? */
    private static boolean escorted(UUID uuid, World world, Vector3d pos) {
        Party party = PartyManager.getParty(uuid);
        if (party == null) return false;
        for (UUID m : party.getMemberUUIDs()) {
            if (m.equals(uuid)) continue;
            PlayerRef mr = Universe.get().getPlayer(m);
            Ref<EntityStore> ref = mr != null ? mr.getReference() : null;
            if (ref == null || !ref.isValid() || ref.getStore().getExternalData().getWorld() != world) continue;
            PlayerLevelData d = ref.getStore().getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            TransformComponent t = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
            if (d == null || t == null || !LuoClans.isEmpire(d.getNobleFamilyId())) continue;   // Clan Luo et ses clans
            if (t.getPosition().distanceSquared(pos) <= ESCORT_RANGE * ESCORT_RANGE) return true;
        }
        return false;
    }

    private void bowAround(UUID player, Vector3d pos, LuoPlaques.Plaque plaque, Store<EntityStore> store, long now) {
        Vector3d eye = new Vector3d(pos).add(0, 1.6, 0);
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(pos, BOW_RANGE, store))) {
            if (!LuoNpcs.isLuo(e, store)) continue;
            UUIDComponent u = store.getComponent(e, UUIDComponent.getComponentType());
            if (u == null) continue;
            String key = u.getUuid() + "|" + player;
            Long last = bows.get(key);
            if (last != null && now - last < BOW_COOLDOWN_MS) continue;
            if (LuoNpcs.bow(e, eye, plaque.bowGesture(), store)) bows.put(key, now);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
