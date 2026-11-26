package com.eldanior.system.territory.laws;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.guards.LawGuards;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application des lois des domaines (DomainLaws), chaque seconde pour chaque joueur hors admins :
 *  - couvre-feu : avertissement a l'entree dans l'heure interdite, {@link #CURFEW_GRACE_MS} pour rentrer chez soi ou sortir,
 *    puis amende (pieces, puis compte en banque) versee au tresor du domaine, de nouveau toutes les {@link #FINE_EVERY_MS} ;
 *    impayable : les gardes s'en chargent ;
 *  - armes rengainees : {@link #ARMS_GRACE_MS} pour rengainer, puis amende ;
 *  - frontieres : droit d'entree preleve apres {@link #BORDER_GRACE_MS} (valable 2 jours), ou frontieres fermees : les gardes
 *    du domaine attaquent ; un etranger dans le groupe d'un habitant proche passe ;
 *  - bannis : les gardes attaquent a vue apres {@link #BAN_GRACE_MS}.
 * Les paiements se font hors du tick (cb.run : « Store is currently processing! »).
 */
public class DomainLawSystem extends DelayedEntitySystem<EntityStore> {

    static final long CURFEW_GRACE_MS = 60_000, ARMS_GRACE_MS = 10_000, BORDER_GRACE_MS = 10_000, BAN_GRACE_MS = 5_000,
            FINE_EVERY_MS = 5 * 60_000, FORGET_MS = 30_000;
    static final double GUARD_RANGE = 40, ESCORT_RANGE = 48, HOSTILE_SECONDS = 6;

    /** Une infraction suivie : domaine, debut, avertie, derniere amende, derniere attaque des gardes. */
    private static final class Offence {
        String domain;
        long since, lastSeen, finedAt, attackAt;
        boolean warned;

        boolean reset(String id, long now) {
            boolean fresh = !id.equals(domain) || now - lastSeen > FORGET_MS;
            if (fresh) { domain = id; since = now; finedAt = 0; warned = false; }
            lastSeen = now;
            return fresh;
        }
    }

    private static final class State {
        final Offence curfew = new Offence(), arms = new Offence(), border = new Offence(), ban = new Offence();
    }

    private final Map<UUID, State> states = new ConcurrentHashMap<>();

    public DomainLawSystem() { super(1f); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        try {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            World world = store.getExternalData().getWorld();
            if (player == null || pr == null || t == null || world == null || data == null) return;
            com.eldanior.system.guards.CityStewards.weekly();                            // gages des Baillis (une fois par minute)
            if (pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;
            UUID uuid = UUIDExtractor.getUUID(pr);
            if (uuid == null) return;
            Vector3d pos = new Vector3d(t.getPosition());
            List<ParcelData> chain = DomainLaws.chain(world.getName(), pos.x, pos.y, pos.z);
            if (chain.isEmpty()) return;
            long now = System.currentTimeMillis();
            State st = states.computeIfAbsent(uuid, k -> new State());

            // ---- bannis ----
            ParcelData banned = DomainLaws.banHere(chain, uuid);
            if (banned != null) {
                Offence o = st.ban;
                o.reset(banned.getId(), now);
                if (!o.warned) {
                    o.warned = true;
                    NotificationHelper.showEventTitle(pr, banned.getName(), "Vous êtes banni de ces terres", true);
                    NotificationHelper.sendDanger(pr, "Vous êtes banni de " + banned.getName() + ". Partez : les gardes vous attaqueront à vue.");
                }
                if (now - o.since >= BAN_GRACE_MS) guards(o, ref, pos, banned, store, now);
                return;                                                                  // le reste ne compte plus
            }

            // ---- frontieres ----
            ParcelData border = DomainLaws.borderHere(chain, uuid, data);
            if (border != null && !escorted(uuid, world, pos, border)) borderCheck(st.border, border, ref, pr, player, data, pos, store, cb, now);

            boolean home = DomainLaws.atHome(chain, uuid);

            // ---- couvre-feu ----
            WorldTimeResource time = store.getResource(WorldTimeResource.getResourceType());
            int hour = time != null ? time.getCurrentHour() : 12;
            ParcelData curfew = DomainLaws.curfewHere(chain, hour);
            if (curfew != null && !home && !DomainLaws.official(curfew, data)) {
                Offence o = st.curfew;
                o.reset(curfew.getId(), now);
                if (!o.warned) {
                    o.warned = true;
                    NotificationHelper.showEventTitle(pr, "Couvre-feu à " + curfew.getName(), "Rentrez chez vous ou quittez la ville", true);
                    NotificationHelper.sendWarning(pr, "Couvre-feu à " + curfew.getName() + " (" + DomainLaws.curfewLabel(curfew) + "). Rentrez chez "
                            + "vous (votre maison, votre chambre louée) ou quittez la ville : amende de " + CoinWallet.format(DomainLaws.fine(curfew))
                            + " dans " + CURFEW_GRACE_MS / 1000 + " secondes.");
                }
                if (now - o.since >= CURFEW_GRACE_MS && (o.finedAt == 0 || now - o.finedAt >= FINE_EVERY_MS)) {
                    o.finedAt = now;
                    fine(o, curfew, "le couvre-feu", ref, pr, pos, store, cb);
                }
            }

            // ---- armes rengainees ----
            ParcelData arms = DomainLaws.armsHere(chain);
            ItemStack hand = player.getInventory().getActiveHotbarItem();
            if (arms != null && hand != null && !hand.isEmpty() && DomainLaws.isWeapon(hand.getItemId()) && !DomainLaws.official(arms, data)) {
                Offence o = st.arms;
                o.reset(arms.getId(), now);
                if (!o.warned) {
                    o.warned = true;
                    NotificationHelper.sendWarning(pr, "À " + arms.getName() + ", les armes se portent rengainées. Rangez la vôtre : amende de "
                            + CoinWallet.format(DomainLaws.fine(arms)) + " dans " + ARMS_GRACE_MS / 1000 + " secondes.");
                }
                if (now - o.since >= ARMS_GRACE_MS && (o.finedAt == 0 || now - o.finedAt >= FINE_EVERY_MS)) {
                    o.finedAt = now;
                    fine(o, arms, "une arme dégainée", ref, pr, pos, store, cb);
                }
            } else if (st.arms.domain != null && now - st.arms.lastSeen > 1_500) st.arms.warned = false;   // rengainee : on repart de zero
        } catch (Exception e) {
            EldaniorLogger.error("[Lois] application", e);
        }
    }

    private void borderCheck(Offence o, ParcelData d, Ref<EntityStore> ref, PlayerRef pr, Player player, PlayerLevelData data, Vector3d pos,
                             Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        UUID uuid = UUIDExtractor.getUUID(pr);
        DomainLaws.Border b = DomainLaws.border(d);
        if (b == DomainLaws.Border.TOLL && DomainLaws.paidUntil(d, uuid) > now) return;
        boolean fresh = o.reset(d.getId(), now);
        if (fresh || !o.warned) {
            o.warned = true;
            if (b == DomainLaws.Border.TOLL) {
                NotificationHelper.showEventTitle(pr, d.getName(), "Droit d'entrée : " + CoinWallet.format(DomainLaws.toll(d)), true);
                NotificationHelper.sendWarning(pr, "Entrer sur les terres de " + d.getName() + " coûte " + CoinWallet.format(DomainLaws.toll(d))
                        + " (valable 2 jours). Restez " + BORDER_GRACE_MS / 1000 + " secondes et il sera prélevé ; sinon faites demi-tour.");
            } else {
                NotificationHelper.showEventTitle(pr, d.getName(), "Frontières fermées aux étrangers", true);
                NotificationHelper.sendDanger(pr, "Les frontières de " + d.getName() + " sont fermées. Faites demi-tour : les gardes attaqueront dans "
                        + BORDER_GRACE_MS / 1000 + " secondes.");
            }
        }
        if (now - o.since < BORDER_GRACE_MS) return;
        if (b == DomainLaws.Border.TOLL && o.finedAt == 0) {
            o.finedAt = now;                                                             // une seule tentative de paiement par entree
            long toll = DomainLaws.toll(d);
            String domainId = d.getId();
            cb.run(s -> {
                if (!ref.isValid()) return;
                var type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData pd = s.getComponent(ref, type);
                Player pl = s.getComponent(ref, Player.getComponentType());
                ParcelData dom = ParcelManager.get(domainId);
                if (pd == null || pl == null || dom == null) return;
                if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(pl, pd, toll)) {
                    NotificationHelper.sendDanger(pr, "Vous n'avez pas de quoi payer le droit d'entrée de " + dom.getName() + " ("
                            + CoinWallet.format(toll) + ") : les gardes vous chassent.");
                    return;
                }
                s.putComponent(ref, type, pd);
                dom.addTreasury(toll);
                DomainLaws.markPaid(dom, uuid, System.currentTimeMillis() + DomainLaws.TOLL_VALID_MS);
                ParcelManager.save();
                NotificationHelper.sendSuccess(pr, "Droit d'entrée de " + dom.getName() + " payé : " + CoinWallet.format(toll) + ". Bienvenue (2 jours).");
            });
            return;
        }
        if (b == DomainLaws.Border.TOLL && DomainLaws.paidUntil(d, uuid) > now) return;
        if (b == DomainLaws.Border.TOLL && now - o.finedAt < 2_000) return;             // paiement en cours
        guards(o, ref, pos, d, store, now);
    }

    /** Amende : pieces puis banque, au tresor du domaine ; impayable : les gardes s'en chargent. */
    private void fine(Offence o, ParcelData d, String why, Ref<EntityStore> ref, PlayerRef pr, Vector3d pos, Store<EntityStore> store,
                      CommandBuffer<EntityStore> cb) {
        long amount = DomainLaws.fine(d);
        String domainId = d.getId();
        cb.run(s -> {
            if (!ref.isValid()) return;
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData pd = s.getComponent(ref, type);
            Player pl = s.getComponent(ref, Player.getComponentType());
            ParcelData dom = ParcelManager.get(domainId);
            if (pd == null || pl == null || dom == null) return;
            if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(pl, pd, amount)) {
                NotificationHelper.sendDanger(pr, "Amende de " + CoinWallet.format(amount) + " pour " + why + " à " + dom.getName()
                        + " : vous ne pouvez pas la payer. Les gardes vous arrêtent !");
                o.attackAt = 0;
                LawGuards.setOn(ref, pos, GUARD_RANGE, dom, HOSTILE_SECONDS * 2, s);
                return;
            }
            s.putComponent(ref, type, pd);
            dom.addTreasury(amount);
            ParcelManager.save();
            NotificationHelper.sendWarning(pr, "Amende de " + CoinWallet.format(amount) + " pour " + why + " à " + dom.getName()
                    + ", versée au trésor du domaine.");
            String house = DomainLaws.house(dom);
            if (!house.isEmpty()) com.eldanior.system.titles.nobility.family.FamilyRegistry.log(house, pr.getUsername() + " paie une amende de "
                    + CoinWallet.format(amount) + " (" + why + ", " + dom.getName() + ")");
        });
    }

    private static void guards(Offence o, Ref<EntityStore> ref, Vector3d pos, ParcelData d, Store<EntityStore> store, long now) {
        if (now - o.attackAt < 1_900) return;
        o.attackAt = now;
        LawGuards.setOn(ref, pos, GUARD_RANGE, d, HOSTILE_SECONDS, store);
    }

    /** Dans le groupe d'un habitant du domaine, present a moins de ESCORT_RANGE blocs. */
    private static boolean escorted(UUID uuid, World world, Vector3d pos, ParcelData d) {
        Party party = PartyManager.getParty(uuid);
        if (party == null) return false;
        for (UUID m : party.getMemberUUIDs()) {
            if (m.equals(uuid)) continue;
            PlayerRef mr = Universe.get().getPlayer(m);
            Ref<EntityStore> r = mr != null ? mr.getReference() : null;
            if (r == null || !r.isValid() || r.getStore().getExternalData().getWorld() != world) continue;
            PlayerLevelData md = r.getStore().getComponent(r, EldaniorSystem.get().getPlayerLevelDataType());
            TransformComponent t = r.getStore().getComponent(r, TransformComponent.getComponentType());
            if (t == null || t.getPosition().distanceSquared(pos) > ESCORT_RANGE * ESCORT_RANGE) continue;
            if (DomainLaws.insider(d, m, md)) return true;
        }
        return false;
    }

    /** Couvre-feu en cours a cette position (boutiques fermees) : nom du domaine, null sinon. */
    public static String curfewAt(String world, Vector3d pos, Store<EntityStore> store) {
        WorldTimeResource time = store.getResource(WorldTimeResource.getResourceType());
        if (time == null) return null;
        ParcelData d = DomainLaws.curfewHere(DomainLaws.chain(world, pos.x, pos.y, pos.z), time.getCurrentHour());
        return d != null ? d.getName() : null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
