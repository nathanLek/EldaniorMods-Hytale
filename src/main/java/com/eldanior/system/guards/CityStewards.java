package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.territory.laws.DomainLaws;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3d;

/**
 * Baillis (2026-11-19, choix de l'utilisateur : « combiner la borne et le Bailli ») : un Comte gere sa ville depuis la borne de
 * sa ville, et peut engager un Bailli (PNJ, metier BAILLI) qui ouvre le meme registre. Le Bailli est paye {@link #WAGE} par
 * semaine sur le tresor de la ville (la premiere semaine a l'embauche) ; tresor vide = il s'en va. Donnees dans les lois de la
 * ville : bailli (id du poste), bailliPaid (derniere paie).
 */
public final class CityStewards {

    private CityStewards() {}

    public static final long WAGE = 2_000, WEEK_MS = 7L * 24 * 3600 * 1000;
    private static volatile long lastCheck;

    /** Ville (zone de type Ville) qui contient la position, null sinon. */
    public static ParcelData cityAt(String world, double x, double y, double z) {
        for (ParcelData p : DomainLaws.chain(world, x, y, z)) if (p.getType() == ParcelType.CITY) return p;
        return null;
    }

    public static ParcelData cityOf(GuardManager.Post p) {
        return p != null ? cityAt(p.getWorld(), p.x, p.y, p.z) : null;
    }

    public static boolean hasBailli(ParcelData city) {
        String id = city.law("bailli");
        return !id.isEmpty() && GuardManager.getPosts().stream().anyMatch(p -> p.getId().equals(id));
    }

    /** Engage un Bailli a cette position (thread du monde). Message d'erreur, ou null si c'est fait. */
    public static String hire(ParcelData city, World world, Vector3d pos, float yaw) {
        if (hasBailli(city)) return city.getName() + " a déjà son Bailli.";
        if (city.getTreasury() < WAGE) return "Le trésor de " + city.getName() + " doit couvrir une semaine de gages (" + WAGE + " or).";
        GuardManager.Post post = GuardManager.place(GuardType.COMMERCANT, world, pos, yaw, null, null, 0, null, null, NpcTrades.Metier.BAILLI);
        if (post == null) return "Le Bailli n'a pas pu prendre son poste ici.";
        city.addTreasury(-WAGE);
        city.setLaw("bailli", post.getId());
        city.setLaw("bailliPaid", String.valueOf(System.currentTimeMillis()));
        ParcelManager.save();
        return null;
    }

    /** Renvoie le Bailli de la ville (thread du monde de son poste). */
    public static void dismiss(ParcelData city) {
        String id = city.law("bailli");
        city.setLaw("bailli", "");
        city.setLaw("bailliPaid", "");
        ParcelManager.save();
        if (id.isEmpty()) return;
        for (GuardManager.Post p : GuardManager.getPosts()) {
            if (!p.getId().equals(id)) continue;
            World w = Universe.get().getWorld(p.getWorld());
            if (w != null) w.execute(() -> GuardManager.removeById(id, w));
        }
    }

    /** Paie des Baillis, une fois par minute au plus (appele depuis DomainLawSystem). */
    public static void weekly() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < 60_000) return;
        lastCheck = now;
        try {
            for (ParcelData city : ParcelManager.getByType(ParcelType.CITY)) {
                if (city.law("bailli").isEmpty()) continue;
                if (!hasBailli(city)) { city.setLaw("bailli", ""); city.setLaw("bailliPaid", ""); ParcelManager.save(); continue; }
                long paid = city.lawLong("bailliPaid", now);
                if (now - paid < WEEK_MS) continue;
                if (city.getTreasury() >= WAGE) {
                    city.addTreasury(-WAGE);
                    city.setLaw("bailliPaid", String.valueOf(paid + WEEK_MS));
                    ParcelManager.save();
                } else {
                    dismiss(city);
                    tellOwner(city, "Le trésor de " + city.getName() + " ne peut plus payer son Bailli : il a rendu sa charge.");
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Bailli] gages", e);
        }
    }

    private static void tellOwner(ParcelData city, String text) {
        if (city.getOwnerUUID() == null) return;
        PlayerRef pr = Universe.get().getPlayer(city.getOwnerUUID());
        if (pr != null) pr.sendMessage(Message.raw(text).color("#e8c060"));
    }

    /** Ce que le Bailli dit a un passant : les lois de la ville. */
    public static String greeting(ParcelData city) {
        if (city == null) return "Je n'ai pas de ville à servir.";
        StringBuilder sb = new StringBuilder("Bienvenue à " + city.getName() + ". ");
        int[] c = DomainLaws.curfew(city);
        sb.append(c != null ? "Couvre-feu de " + c[0] + " h à " + c[1] + " h. " : "Pas de couvre-feu. ");
        if (DomainLaws.armsBanned(city)) sb.append("Les armes se portent rengainées. ");
        DomainLaws.Border b = DomainLaws.border(city);
        if (b != DomainLaws.Border.OPEN) sb.append("Frontières : ").append(b.label).append(". ");
        sb.append("Le registre de la ville est l'affaire du Comte.");
        return sb.toString();
    }
}
