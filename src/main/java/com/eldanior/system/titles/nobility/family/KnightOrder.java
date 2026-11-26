package com.eldanior.system.titles.nobility.family;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ordre de chevalerie : une petite guilde dans la maison (2026-11-18). Hierarchie a sept rangs (KnightOrderRole) : le fondateur
 * est Grand Maitre ; les autres entrent Aspirants. Le Vice-Maitre tient lieu de l'ancien Lieutenant.
 */
public class KnightOrder {

    public static final int MAX_MEMBERS = 20;

    private final String id;
    private final String name;
    private final String familyId;
    private final String motto;
    private final UUID capitaineUUID;
    private final String capitaineName;

    // Stats d'ordre (thread-safe)
    private final AtomicInteger totalKills = new AtomicInteger(0);
    private final AtomicInteger totalMissions = new AtomicInteger(0);
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();
    /** Rang de chaque membre (le Grand Maitre n'y figure pas : c'est toujours le fondateur). */
    private final Map<UUID, KnightOrderRole> ranks = new ConcurrentHashMap<>();
    /** Noms connus des membres (pour les absents). */
    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    /** Recrutement sur demande ouvert (2026-11-18) : ferme, l'ordre choisit ses membres (ordre d'elite, enrolement par un officier). */
    private volatile boolean recruiting = true;
    /** Demandes d'entree en attente : joueur -> nom. */
    private final Map<UUID, String> candidates = new ConcurrentHashMap<>();
    private volatile String territoryId;
    private final long createdAt;

    public KnightOrder(String name, String motto, String familyId, UUID capitaineUUID, String capitaineName) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.motto = motto;
        this.familyId = familyId;
        this.capitaineUUID = capitaineUUID;
        this.capitaineName = capitaineName;
        this.createdAt = System.currentTimeMillis();
        this.members.add(capitaineUUID);
        if (capitaineName != null) names.put(capitaineUUID, capitaineName);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFamilyId() { return familyId; }
    public String getMotto() { return motto; }
    public UUID getCapitaineUUID() { return capitaineUUID; }
    public String getCapitaineName() { return capitaineName; }
    public String getTerritoryId() { return territoryId; }
    public long getCreatedAt() { return createdAt; }

    /** Vice-Maitre (ex-Lieutenant), null s'il n'y en a pas. */
    public UUID getLieutenantUUID() {
        for (var e : ranks.entrySet()) if (e.getValue() == KnightOrderRole.VICE_MAITRE) return e.getKey();
        return null;
    }

    public String getLieutenantName() {
        UUID u = getLieutenantUUID();
        return u != null ? nameOf(u) : null;
    }

    // Stats
    public int getTotalKills() { return totalKills.get(); }
    public int getTotalMissions() { return totalMissions.get(); }
    public void addKill() { this.totalKills.incrementAndGet(); }
    public void addMission() { this.totalMissions.incrementAndGet(); }

    // Membres — retourne une copie immuable pour la thread-safety
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(new HashSet<>(members)); }
    public int getMemberCount() { return members.size(); }

    /** Nouveau membre : Aspirant. */
    public boolean addMember(UUID uuid) {
        if (members.size() >= MAX_MEMBERS) return false;
        members.add(uuid);
        if (!uuid.equals(capitaineUUID)) ranks.putIfAbsent(uuid, KnightOrderRole.ASPIRANT);
        return true;
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        ranks.remove(uuid);
    }

    public boolean hasMember(UUID uuid) { return members.contains(uuid); }

    public void rememberName(UUID uuid, String name) { if (uuid != null && name != null && !name.isEmpty()) names.put(uuid, name); }

    public String nameOf(UUID uuid) {
        if (uuid.equals(capitaineUUID)) return capitaineName;
        return names.get(uuid);
    }

    public Map<UUID, String> getNames() { return Collections.unmodifiableMap(names); }

    // Lieutenant (compatibilite) = Vice-Maitre
    public void setLieutenant(UUID uuid, String name) {
        if (uuid == null || !members.contains(uuid) || uuid.equals(capitaineUUID)) return;
        UUID old = getLieutenantUUID();
        if (old != null) ranks.put(old, KnightOrderRole.HAUT_COMMANDANT);
        ranks.put(uuid, KnightOrderRole.VICE_MAITRE);
        rememberName(uuid, name);
    }

    public void clearLieutenant() {
        UUID old = getLieutenantUUID();
        if (old != null) ranks.put(old, KnightOrderRole.HAUT_COMMANDANT);
    }

    // Roles
    public boolean isCapitaine(UUID uuid) { return capitaineUUID.equals(uuid); }

    public boolean isLieutenant(UUID uuid) { return getRoleOf(uuid) == KnightOrderRole.VICE_MAITRE; }

    public KnightOrderRole getRoleOf(UUID uuid) {
        if (uuid == null) return null;
        if (isCapitaine(uuid)) return KnightOrderRole.GRAND_MAITRE;
        if (!hasMember(uuid)) return null;
        KnightOrderRole r = ranks.get(uuid);
        return r != null ? r : KnightOrderRole.ASPIRANT;
    }

    /** Membres de ce rang. */
    public int count(KnightOrderRole r) {
        int n = 0;
        for (UUID u : members) if (getRoleOf(u) == r) n++;
        return n;
    }

    /** Membres tries du plus haut rang au plus bas, puis par nom. */
    public List<UUID> sortedMembers() {
        List<UUID> out = new ArrayList<>(members);
        out.sort(Comparator.comparingInt((UUID u) -> getRoleOf(u).ordinal())
                .thenComparing(u -> nameOf(u) != null ? nameOf(u) : u.toString(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    /**
     * Raison du refus (null : permis) de faire passer target au rang to, par actor (rang de l'acteur dans l'ordre ; le Patriarche
     * agit en Grand Maitre). knightly : le membre vise est Chevalier de la maison ou plus.
     */
    public String refuseRank(KnightOrderRole actor, UUID target, KnightOrderRole to, boolean knightly) {
        KnightOrderRole from = getRoleOf(target);
        if (actor == null || from == null || to == null) return "Rang inconnu.";
        if (from == KnightOrderRole.GRAND_MAITRE || to == KnightOrderRole.GRAND_MAITRE) return "Le Grand Maître est le fondateur de l'ordre.";
        if (from.ordinal() <= actor.ordinal()) return "Vous ne commandez qu'aux rangs sous le vôtre.";
        if (to.ordinal() <= actor.ordinal()) return "Vous ne pouvez élever personne au-delà du rang juste sous le vôtre.";
        if (to.officer() && !knightly) return "Les rangs d'officier (Commandant et plus) sont réservés aux Chevaliers de la maison.";
        if (to.max() > 0 && count(to) >= to.max()) return "Plus de place au rang de " + to.getDisplayName() + " (" + to.max() + " au plus).";
        return null;
    }

    public void setRank(UUID uuid, KnightOrderRole r) {
        if (uuid == null || r == null || uuid.equals(capitaineUUID) || !members.contains(uuid) || r == KnightOrderRole.GRAND_MAITRE) return;
        ranks.put(uuid, r);
    }

    public Map<UUID, KnightOrderRole> getRanks() { return Collections.unmodifiableMap(ranks); }

    // Recrutement
    public boolean isRecruiting() { return recruiting; }
    public void setRecruiting(boolean on) { this.recruiting = on; if (!on) candidates.clear(); }
    public Map<UUID, String> getCandidates() { return Collections.unmodifiableMap(candidates); }
    public void addCandidate(UUID uuid, String name) { if (uuid != null) candidates.put(uuid, name != null ? name : "?"); }
    public String removeCandidate(UUID uuid) { return uuid != null ? candidates.remove(uuid) : null; }

    // Territoire
    public void setTerritoryId(String territoryId) { this.territoryId = territoryId; }

    public String getFormattedName() { return "#FFD700" + name; }
}
