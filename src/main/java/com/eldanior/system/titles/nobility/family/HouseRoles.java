package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Rangs de maison automatiques (2026-11-18, regle de l'utilisateur) :
 *  - Doyen = Duc : un membre Duc est Doyen de sa maison ; et les seigneurs des terres vassales sont Doyens d'office de la
 *    maison suzeraine (vassalDukes) : Marquis -> maison Eldanior, Ducs -> maison du Marquisat, Comtes -> maison du Duche ;
 *  - Noble = Comte ou Baron ; Chevalier Royal = Chevalier (titre de noblesse) ;
 *  - le Patriarche ne choisit que Recrue, Chevalier et Seigneur.
 * sync() est appele toutes les 5 s (LuoPlaqueSystem) : le rang enregistre suit le titre de noblesse.
 */
public final class HouseRoles {

    private HouseRoles() {}

    /** Dernier rang ecrit par joueur (evite de reecrire et de consigner toutes les 5 s si l'ecriture tarde). */
    private static final java.util.Map<UUID, String> PENDING = new java.util.concurrent.ConcurrentHashMap<>();

    /** Met le rang du joueur a jour d'apres son titre de noblesse ; ecriture differee (cb.run). */
    public static void sync(Ref<EntityStore> ref, PlayerRef pr, PlayerLevelData data, UUID uuid, CommandBuffer<EntityStore> cb) {
        if (data == null || data.getNobleFamilyId() == null || data.getNobleFamilyId().isEmpty()) return;
        FamilyRole now = data.getFamilyRole();
        FamilyRole eff = FamilyRole.effective(now, NobilityRank.fromString(data.getNobilityRank()));
        if (eff == null) return;
        if (eff == now) { PENDING.remove(uuid); return; }                                   // a jour
        String fam = FamilyManager.migrateId(data.getNobleFamilyId());
        final String code = eff.code();
        String key = fam + ":" + code;
        if (key.equals(PENDING.put(uuid, key))) return;                                   // ecriture deja demandee
        cb.run(s -> {
            PlayerLevelData cur = s.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (cur == null) return;
            PlayerLevelData copy = (PlayerLevelData) cur.clone();
            if (copy == null) return;
            copy.setStatus(code);
            s.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        });
        if (eff == FamilyRole.ELDER) FamilyManager.restoreElder(fam, uuid);
        else if (now == FamilyRole.ELDER) FamilyManager.removeElder(fam, uuid);
        FamilyRegistry.touch(fam, uuid, pr.getUsername(), code, data.getLevel(), data.getPlayerClassId());
        FamilyRegistry.log(fam, pr.getUsername() + " devient " + eff.label() + (eff.automatic() ? " (titre de noblesse)" : ""));
        pr.sendMessage(Message.raw("Votre titre de noblesse vous donne le rang de " + eff.label() + " dans votre maison."));
    }

    // ===== Doyens d'office : seigneurs des terres vassales =====
    // 2026-11-18 (regle de l'utilisateur) : les Marquis sont Doyens d'office de la maison Eldanior (Royaume), les Ducs Doyens
    // de la maison de leur Marquisat, les Comtes Doyens de la maison de leur Duche.

    /** Terre suzeraine d'une terre : Ville -> Territoire, Territoire -> Grand Territoire, Grand Territoire -> Royaume. */
    public static ParcelData suzerain(ParcelData land) {
        if (land == null) return null;
        ParcelType want = switch (land.getType()) {
            case CITY -> ParcelType.TERRITORY;
            case TERRITORY -> ParcelType.GRAND_TERRITORY;
            case GRAND_TERRITORY -> ParcelType.KINGDOM;
            default -> null;
        };
        if (want == null) return null;
        Set<String> seen = new HashSet<>();
        for (String id = land.getParentId(); id != null && !id.isEmpty() && seen.add(id); ) {
            ParcelData p = ParcelManager.get(id);
            if (p == null) return null;
            if (p.getType() == want) return p;
            id = p.getParentId();
        }
        return null;
    }

    private static String houseOf(ParcelData p) {
        return p == null || p.getFamilyId().isEmpty() ? null : FamilyManager.migrateId(p.getFamilyId());
    }

    /** Maison suzeraine de cette maison (celle de la terre au-dessus de sa terre principale) ; null sinon. */
    public static String superiorHouse(String house) {
        if (house == null) return null;
        String fam = FamilyManager.migrateId(house);
        String up = houseOf(suzerain(ParcelManager.getFamilyParcel(fam)));
        return up != null && !up.equals(fam) ? up : null;
    }

    static String titleOf(ParcelType t) {
        return switch (t) {
            case GRAND_TERRITORY -> "Marquis";
            case TERRITORY -> "Duc";
            case CITY -> "Comte";
            default -> "Seigneur";
        };
    }

    /** Patriarche d'une maison (registre, sinon maitre de sa terre principale) ; null s'il n'y en a pas. */
    public static UUID patriarchOf(String house) {
        if (house == null || !FamilyManager.isFamilyTaken(FamilyManager.migrateId(house))) return null;
        for (FamilyRegistry.Entry e : FamilyRegistry.members(house)) if (FamilyRole.of(e.role()) == FamilyRole.PATRIARCH) return e.uuid();
        ParcelData main = ParcelManager.getFamilyParcel(FamilyManager.migrateId(house));
        return main != null ? main.getOwnerUUID() : null;
    }

    /** Doyen d'office : seigneur (uuid, nom), sa maison (peut etre vide pour un Comte sans maison), titre et terre. */
    public record Duke(UUID uuid, String name, String house, FamilyRegistry.Entry entry, String title, String land) {}

    /**
     * Doyens d'office de cette maison : Patriarches des maisons dont la terre principale releve d'une terre de la maison, et
     * maitres des villes de ses Duches (Comtes), une seule fois chacun.
     */
    public static List<Duke> vassalDukes(String house) {
        List<Duke> out = new ArrayList<>();
        if (house == null) return out;
        String fam = FamilyManager.migrateId(house);
        Set<UUID> seen = new HashSet<>();
        for (NobleFamilyModel f : FamilyManager.getAll()) {
            if (f.getId().equals(fam) || !fam.equals(superiorHouse(f.getId()))) continue;
            UUID u = patriarchOf(f.getId());
            if (u == null || !seen.add(u)) continue;
            FamilyRegistry.Entry entry = null;
            for (FamilyRegistry.Entry e : FamilyRegistry.members(f.getId())) if (e.uuid().equals(u)) entry = e;
            ParcelData main = ParcelManager.getFamilyParcel(f.getId());
            String name = entry != null ? entry.name() : main != null && main.getOwnerName() != null ? main.getOwnerName() : "Patriarche " + f.getDisplayName();
            out.add(new Duke(u, name, f.getId(), entry, main != null ? titleOf(main.getType()) : "Seigneur",
                    main != null ? main.getName() : f.getDisplayName()));
        }
        for (ParcelData city : ParcelManager.getByType(ParcelType.CITY)) {                  // Comtes : maitres des villes de nos Duches
            UUID u = city.getOwnerUUID();
            if (u == null || seen.contains(u)) continue;
            ParcelData duchy = suzerain(city);
            if (duchy == null || !fam.equals(houseOf(duchy))) continue;
            String cityHouse = houseOf(city);
            if (cityHouse != null && !cityHouse.equals(fam)) continue;                      // ville d'une autre maison : son Patriarche
            seen.add(u);
            out.add(new Duke(u, city.getOwnerName() != null && !city.getOwnerName().isEmpty() ? city.getOwnerName() : "Comte de " + city.getName(),
                    cityHouse != null ? cityHouse : "", null, "Comte", city.getName()));
        }
        return out;
    }

    /** Ce joueur est-il Doyen d'office de cette maison (Patriarche d'une de ses maisons ducales) ? */
    public static boolean isVassalDuke(UUID uuid, String house) {
        if (uuid == null) return false;
        for (Duke d : vassalDukes(house)) if (d.uuid().equals(uuid)) return true;
        return false;
    }
}
