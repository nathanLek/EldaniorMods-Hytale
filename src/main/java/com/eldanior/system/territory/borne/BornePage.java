package com.eldanior.system.territory.borne;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Fenetre d'une borne de territoire (Territoire/Borne.ui, scripts/territoire/gen_borne.py), admins seulement :
 * Zone (type, nom, famille), Dimensions (rectangle ou cylindre centre sur la borne, demi-largeurs / rayon, hauteurs),
 * Regles (PvP, protection par defaut, regeneration). Boutons : Creer / mettre a jour, Lier la zone existante (zone deja
 * creee par commande qui contient la borne : la borne la regle desormais), Supprimer la zone (la borne reste).
 * Controles : la zone tient dans son parent (plus petite zone qui contient la borne et peut l'accueillir), ne chevauche pas
 * une zone soeur du meme type, garde ses sous-zones a l'interieur ; le type d'une zone qui a des sous-zones ne change pas.
 * Cylindre : controles faits sur son carre englobant (prudent).
 */
public class BornePage extends InteractiveCustomUIPage<BornePage.Data> {

    private static final String YES = "oui", NO = "non", RECT = "rect", CYL = "cylindre";
    private final String key;

    public BornePage(@Nonnull PlayerRef playerRef, String key) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.key = key;
    }

    private static DropdownEntryInfo entry(String label, String value) {
        return new DropdownEntryInfo(LocalizableString.fromString(label), value);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Territoire/Borne.ui");
        ParcelData p = Bornes.zoneOf(key);
        ParcelData link = p == null ? Bornes.linkable(key) : null;
        List<DropdownEntryInfo> types = new ArrayList<>();
        for (ParcelType t : Bornes.TYPES) types.add(entry(t.getLabel(), t.name()));
        List<DropdownEntryInfo> families = new ArrayList<>(List.of(entry("Aucune (terres de la couronne)", "")));
        for (var f : FamilyManager.getAll()) families.add(entry(f.getDisplayName(), f.getId()));
        List<DropdownEntryInfo> yesNo = List.of(entry("Oui", YES), entry("Non", NO));
        ui.set("#BorneFamily.Entries", families);
        ui.set("#BorneShape.Entries", List.of(entry("Rectangle", RECT), entry("Cylindre", CYL)));
        ui.set("#BornePvp.Entries", yesNo);
        ui.set("#BorneProtect.Entries", yesNo);
        ui.set("#BorneAnnounce.Entries", yesNo);
        ParcelData src = p != null ? p : link;
        if (src != null && !Bornes.TYPES.contains(src.getType())) types.add(entry(src.getType().getLabel(), src.getType().name()));   // zone liee
        ui.set("#BorneType.Entries", types);
        ui.set("#BorneType.Value", src != null ? src.getType().name() : ParcelType.TERRITORY.name());
        ui.set("#BorneName.Value", src != null && src.getName() != null ? src.getName() : "");
        ui.set("#BorneFamily.Value", src != null ? src.getFamilyId() : "");
        ui.set("#BorneShape.Value", src != null && src.isCylinder() ? CYL : RECT);
        ui.set("#BorneSizeX.Value", src != null ? (src.getMaxX() - src.getMinX()) / 2 : 100);
        ui.set("#BorneSizeZ.Value", src != null ? (src.getMaxZ() - src.getMinZ()) / 2 : 100);
        ui.set("#BorneYMin.Value", src != null ? src.getMinY() : 0);
        ui.set("#BorneYMax.Value", src != null ? src.getMaxY() : 319);
        ui.set("#BornePvp.Value", src != null && src.isPvpEnabled() ? YES : NO);
        ui.set("#BorneProtect.Value", src == null || src.isProtectedByDefault() ? YES : NO);
        ui.set("#BorneRegen.Value", src != null && src.isRegen() ? src.getRegenDelaySec() : 0);
        ui.set("#BorneAnnounce.Value", src != null && "off".equals(src.getAnnounce()) ? NO : YES);
        ui.set("#BorneState.Text", p != null ? state(p)
                : link != null ? "Pas encore de zone sur cette borne. Zone existante ici : " + link.getName() + " (" + link.getType().getLabel()
                        + ") — « Lier » pour la régler par cette borne (« Mettre à jour » la recentre sur la borne)."
                : "Pas encore de zone : réglez-la puis « Créer ». La borne en sera le centre.");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BorneSave", EventData.of("Action", "save")
                .append("@Type", "#BorneType.Value").append("@Name", "#BorneName.Value").append("@Family", "#BorneFamily.Value")
                .append("@Shape", "#BorneShape.Value").append("@SizeX", "#BorneSizeX.Value").append("@SizeZ", "#BorneSizeZ.Value")
                .append("@YMin", "#BorneYMin.Value").append("@YMax", "#BorneYMax.Value").append("@Pvp", "#BornePvp.Value")
                .append("@Protect", "#BorneProtect.Value").append("@Regen", "#BorneRegen.Value")
                .append("@Announce", "#BorneAnnounce.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BorneLink", EventData.of("Action", "link"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BorneDelete", EventData.of("Action", "delete"), false);
    }

    private static String state(ParcelData p) {
        ParcelData parent = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
        var fam = p.getFamilyId().isEmpty() ? null : FamilyManager.get(p.getFamilyId());
        return p.getType().getLabel() + " « " + p.getName() + " »  ·  " + p.getId()
                + "  ·  " + (p.getMaxX() - p.getMinX() + 1) + " x " + (p.getMaxZ() - p.getMinZ() + 1) + (p.isCylinder() ? " (cylindre)" : "")
                + "  ·  dans " + (parent != null ? parent.getName() : "aucune zone")
                + (fam != null ? "  ·  famille " + fam.getDisplayName() : "");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        try {
            this.close();
            if (pr == null || !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;
            switch (data.action == null ? "" : data.action) {
                case "save" -> save(pr, data);
                case "link" -> link(pr);
                case "delete" -> {
                    ParcelData p = Bornes.zoneOf(key);
                    if (p == null) { pr.sendMessage(Message.raw("Cette borne n'a pas de zone.")); return; }
                    ParcelManager.deleteParcel(p.getId());
                    Bornes.refreshMap(Bornes.world(key));
                    pr.sendMessage(Message.raw(p.getType().getLabel() + " « " + p.getName() + " » supprimé ; ses sous-zones remontent à "
                            + "la zone parente. La borne reste posée (cassez-la pour la retirer)."));
                }
                default -> { }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Borne] fenetre", e);
            if (pr != null) pr.sendMessage(Message.raw("Erreur de la borne : " + e.getMessage()));
        }
    }

    private void link(PlayerRef pr) {
        if (Bornes.zoneOf(key) != null) { pr.sendMessage(Message.raw("Cette borne règle déjà une zone.")); return; }
        ParcelData p = Bornes.linkable(key);
        if (p == null) { pr.sendMessage(Message.raw("Aucune zone sans borne ne contient cette borne.")); return; }
        p.setBorne(key);
        if (p.getAnnounce().isEmpty()) p.setAnnounce("on");
        ParcelManager.save();
        Bornes.refreshMap(Bornes.world(key));
        pr.sendMessage(Message.raw("La borne règle maintenant " + p.getType().getLabel() + " « " + p.getName() + " ». Attention : casser la "
                + "borne supprimera cette zone."));
    }

    private void save(PlayerRef pr, Data d) {
        ParcelType type;
        try { type = ParcelType.valueOf(d.type); } catch (Exception e) { type = null; }
        ParcelData current = Bornes.zoneOf(key);
        if (type == null || !(Bornes.TYPES.contains(type) || (current != null && current.getType() == type))) {
            pr.sendMessage(Message.raw("Type de zone invalide : royaume, grand territoire, territoire, ville, forêt, arène ou parcelle "
                    + "(logements, chambres, fermes, mines et donjons se créent à la main, bloc par bloc)."));
            return;
        }
        String name = d.name == null ? "" : d.name.trim();
        if (name.isEmpty()) { pr.sendMessage(Message.raw("Donnez un nom à la zone.")); return; }
        boolean cyl = CYL.equals(d.shape);
        int sx = Math.max(1, d.sizeX), sz = cyl ? sx : Math.max(1, d.sizeZ);
        int y1 = Math.clamp(Math.min(d.yMin, d.yMax), -64, 400), y2 = Math.clamp(Math.max(d.yMin, d.yMax), -64, 400);
        String world = Bornes.world(key);
        int[] c = Bornes.coords(key);
        int x1 = c[0] - sx, x2 = c[0] + sx, z1 = c[2] - sz, z2 = c[2] + sz;
        ParcelData self = Bornes.zoneOf(key);

        // Parent : plus petite zone qui contient la borne (ni elle-meme, ni une de ses sous-zones)
        ParcelData parent = null;
        long best = Long.MAX_VALUE;
        for (ParcelData p : ParcelManager.getAll()) {
            if (!world.equals(p.getWorld()) || p == self || (self != null && descends(p, self)) || !p.contains(c[0], c[1], c[2])) continue;
            long v = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1);
            if (v < best) { best = v; parent = p; }
        }
        if (parent != null && !ParcelManager.canContain(parent.getType(), type)) {
            pr.sendMessage(Message.raw("Un " + type.getLabel() + " ne peut pas être dans un " + parent.getType().getLabel() + " (" + parent.getName() + ")."));
            return;
        }
        if (parent == null && type == ParcelType.GRAND_TERRITORY) { pr.sendMessage(Message.raw("Un Grand Territoire doit être dans un royaume.")); return; }
        if (parent != null && (x1 < parent.getMinX() || x2 > parent.getMaxX() || z1 < parent.getMinZ() || z2 > parent.getMaxZ())) {
            pr.sendMessage(Message.raw("La zone dépasse de " + parent.getName() + " : réduisez ses dimensions ou déplacez la borne."));
            return;
        }
        for (ParcelData p : ParcelManager.getAll()) {                          // pas de chevauchement entre soeurs du meme type
            if (p == self || p.getType() != type || !world.equals(p.getWorld())) continue;
            String pp = p.getParentId(), mine = parent != null ? parent.getId() : null;
            if (!java.util.Objects.equals(pp == null || pp.isEmpty() ? null : pp, mine)) continue;
            if (x1 <= p.getMaxX() && x2 >= p.getMinX() && z1 <= p.getMaxZ() && z2 >= p.getMinZ()) {
                pr.sendMessage(Message.raw("La zone chevauche " + p.getType().getLabel() + " « " + p.getName() + " »."));
                return;
            }
        }
        if (self != null) {                                                    // sous-zones : elles restent dedans
            List<String> children = ParcelManager.getChildrenOf(self.getId());
            if (!children.isEmpty() && self.getType() != type) {
                pr.sendMessage(Message.raw("Cette zone a des sous-zones : son type ne peut pas changer."));
                return;
            }
            for (String id : children) {
                ParcelData ch = ParcelManager.get(id);
                if (ch != null && (ch.getMinX() < x1 || ch.getMaxX() > x2 || ch.getMinZ() < z1 || ch.getMaxZ() > z2)) {
                    pr.sendMessage(Message.raw("La sous-zone « " + ch.getName() + " » dépasserait : agrandissez la zone."));
                    return;
                }
            }
        }

        ParcelData p = self;
        if (p == null) {
            String id = ParcelManager.createParcel(name, type, null, "", world, x1, y1, z1, x2, y2, z2, parent != null ? parent.getId() : null);
            p = id != null ? ParcelManager.get(id) : null;
            if (p == null) { pr.sendMessage(Message.raw("La zone n'a pas pu être créée (hiérarchie refusée).")); return; }
            p.setBorne(key);
            if (type == ParcelType.KINGDOM) {                                   // royaume : famille Eldanior et ses 4 marquisats
                if (d.family == null || d.family.isEmpty()) d.family = "eldanior";
                marquisates(p, world, pr);
            }
        } else {
            p.setName(name);
            p.setType(type);
            p.setBounds(x1, y1, z1, x2, y2, z2);
            p.setParentId(parent != null ? parent.getId() : null);
        }
        p.setCylinder(cyl);
        p.setPvpEnabled(YES.equals(d.pvp));
        p.setProtectedByDefault(!NO.equals(d.protect));
        p.setAnnounce(NO.equals(d.announce) ? "off" : "on");
        p.setRegen(d.regen > 0);
        if (d.regen > 0) p.setRegenDelaySec(Math.max(10, d.regen));
        String family = d.family == null ? "" : d.family;
        if (!family.equals(p.getFamilyId())) {
            if (family.isEmpty()) { p.setFamilyId(""); p.setOwnerName(""); }
            else ParcelManager.assignToFamily(p.getId(), family);
        }
        ParcelManager.save();
        Bornes.refreshMap(world);
        pr.sendMessage(Message.raw((self == null ? "Zone créée : " : "Zone mise à jour : ") + state(p)));
    }

    /**
     * Les quatre marquisats du royaume (grands territoires, rappel de l'utilisateur du 2026-11-10), dans les coins comme
     * /es pcreate KINGDOM : carres de 80 % de la demi-largeur, croix centrale neutre. Nord-ouest Zippel, nord-est Runkandel,
     * sud-ouest Luo, sud-est Valmontis.
     */
    private static void marquisates(ParcelData k, String world, PlayerRef pr) {
        int mx = (int) Math.round((k.getMaxX() - k.getMinX()) / 2.0 * 0.8), mz = (int) Math.round((k.getMaxZ() - k.getMinZ()) / 2.0 * 0.8);
        int x1 = k.getMinX(), x2 = k.getMaxX(), z1 = k.getMinZ(), z2 = k.getMaxZ(), y1 = k.getMinY(), y2 = k.getMaxY();
        String[][] corners = {{"Marquisat_Zippel", "zippel"}, {"Marquisat_Runkandel", "runkandel"}, {"Marquisat_Luo", "luo"},
                {"Marquisat_Valmontis", "valmontis"}};
        int[][] boxes = {{x1, z1, x1 + mx, z1 + mz}, {x2 - mx, z1, x2, z1 + mz}, {x1, z2 - mz, x1 + mx, z2}, {x2 - mx, z2 - mz, x2, z2}};
        for (int i = 0; i < 4; i++) {
            String id = ParcelManager.createParcel(corners[i][0], ParcelType.GRAND_TERRITORY, null, "", world,
                    boxes[i][0], y1, boxes[i][1], boxes[i][2], y2, boxes[i][3], k.getId());
            if (id != null) {
                ParcelManager.assignToFamily(id, corners[i][1]);
                ParcelData m = ParcelManager.get(id);
                if (m != null) m.setAnnounce("on");
            }
        }
        pr.sendMessage(Message.raw("Royaume : 4 marquisats créés (Zippel, Runkandel, Luo, Valmontis)."));
    }

    /** true si p est une sous-zone (a n'importe quel niveau) de root. */
    private static boolean descends(ParcelData p, ParcelData root) {
        String id = p.getParentId();
        for (int guard = 0; id != null && !id.isEmpty() && guard < 32; guard++) {
            if (id.equals(root.getId())) return true;
            ParcelData up = ParcelManager.get(id);
            id = up != null ? up.getParentId() : null;
        }
        return false;
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("@Type", Codec.STRING), (d, v) -> d.type = v, d -> d.type).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (d, v) -> d.name = v, d -> d.name).add()
                .append(new KeyedCodec<>("@Family", Codec.STRING), (d, v) -> d.family = v, d -> d.family).add()
                .append(new KeyedCodec<>("@Shape", Codec.STRING), (d, v) -> d.shape = v, d -> d.shape).add()
                .append(new KeyedCodec<>("@SizeX", Codec.INTEGER), (d, v) -> d.sizeX = v, d -> d.sizeX).add()
                .append(new KeyedCodec<>("@SizeZ", Codec.INTEGER), (d, v) -> d.sizeZ = v, d -> d.sizeZ).add()
                .append(new KeyedCodec<>("@YMin", Codec.INTEGER), (d, v) -> d.yMin = v, d -> d.yMin).add()
                .append(new KeyedCodec<>("@YMax", Codec.INTEGER), (d, v) -> d.yMax = v, d -> d.yMax).add()
                .append(new KeyedCodec<>("@Pvp", Codec.STRING), (d, v) -> d.pvp = v, d -> d.pvp).add()
                .append(new KeyedCodec<>("@Protect", Codec.STRING), (d, v) -> d.protect = v, d -> d.protect).add()
                .append(new KeyedCodec<>("@Regen", Codec.INTEGER), (d, v) -> d.regen = v, d -> d.regen).add()
                .append(new KeyedCodec<>("@Announce", Codec.STRING), (d, v) -> d.announce = v, d -> d.announce).add()
                .build();
        public String action, type, name, family, shape, pvp, protect, announce;
        public int sizeX, sizeZ, yMin, yMax, regen;
    }
}
