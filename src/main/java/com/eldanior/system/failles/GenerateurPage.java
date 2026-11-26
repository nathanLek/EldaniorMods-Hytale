package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
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
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Fenetre d'un generateur de monstres (Failles/Generateur.ui) : famille, monstre, nombre ; enregistrer ou retirer. */
public class GenerateurPage extends InteractiveCustomUIPage<GenerateurPage.Data> {

    private final String world;
    private final Vector3i pos;

    public GenerateurPage(@Nonnull PlayerRef playerRef, String world, Vector3i pos) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.world = world;
        this.pos = pos;
    }

    /** Familles de monstres : valeur, libelle, prefixes des roles du jeu. */
    private static final String[][] FAMILIES = {
            {"squelettes", "Squelettes", "Skeleton"},
            {"morts", "Morts-vivants", "Zombie,Ghoul,Wraith,Risen,Hound_Bleached,Shadow_Knight"},
            {"gobelins", "Gobelins", "Goblin,Dungeon_Goblin"},
            {"trorks", "Trorks", "Trork"},
            {"outlanders", "Outlanders", "Outlander"},
            {"scaraks", "Scaraks", "Scarak"},
            {"golems", "Golems et esprits", "Golem,Spirit"},
            {"betes", "Bêtes", "Bear,Wolf,Tiger,Raptor,Rex,Spider,Crocodile,Toad,Emberwulf,Fen_,Yeti,Hyena,Boar,Snake,Scorpion,Bat"},
            {"dragons", "Dragons", "Dragon"},
            {"tous", "Tous les monstres", ""}};

    private static DropdownEntryInfo entry(String label, String value) {
        return new DropdownEntryInfo(LocalizableString.fromString(label), value);
    }

    private static boolean noise(String r) {
        String l = r.toLowerCase();
        return l.contains("template") || l.startsWith("test") || l.contains("_test") || l.contains("debug") || l.contains("edible")
                || l.contains("companion") || l.contains("spectator") || l.contains("_static") || l.contains("_manual") || l.contains("tamed");
    }

    static List<String> roles(String family) {
        String prefixes = "";
        for (String[] f : FAMILIES) if (f[0].equals(family)) prefixes = f[2];
        TreeSet<String> out = new TreeSet<>();
        try {
            for (String r : NPCPlugin.get().getRoleTemplateNames(false)) {
                if (r == null || noise(r)) continue;
                if (prefixes.isEmpty()) { out.add(r); continue; }
                for (String p : prefixes.split(",")) if (r.startsWith(p)) { out.add(r); break; }
            }
        } catch (Exception e) { EldaniorLogger.error("[Generateurs] liste des monstres", e); }
        return new ArrayList<>(out);
    }

    private static String familyOf(String role) {
        if (role == null) return "squelettes";
        for (String[] f : FAMILIES) {
            if (f[2].isEmpty()) continue;
            for (String p : f[2].split(",")) if (role.startsWith(p)) return f[0];
        }
        return "tous";
    }

    private static List<DropdownEntryInfo> roleEntries(String family) {
        List<DropdownEntryInfo> out = new ArrayList<>();
        for (String r : roles(family)) out.add(entry(r.replace('_', ' '), r));
        return out;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Failles/Generateur.ui");
        World w = Universe.get().getWorld(world);
        Generateurs.Config c = w != null ? Generateurs.get(w, pos.x(), pos.y(), pos.z()) : null;
        String fam = familyOf(c != null ? c.role() : null);
        List<DropdownEntryInfo> fams = new ArrayList<>();
        for (String[] f : FAMILIES) fams.add(entry(f[1], f[0]));
        ui.set("#GenFamily.Entries", fams);
        ui.set("#GenFamily.Value", fam);
        List<DropdownEntryInfo> roles = roleEntries(fam);
        ui.set("#GenRole.Entries", roles);
        if (c != null) ui.set("#GenRole.Value", c.role());
        else if (!roles.isEmpty()) ui.set("#GenRole.Value", roles.get(0).value());
        ui.set("#GenCount.Value", c != null ? c.count() : 3);
        String where = FailleManager.editInstanceOf(world) != null ? "Modèle de faille : chaque faille ouverte reprend ce générateur."
                : "Monde : " + world + ".";
        ui.set("#GenState.Text", (c != null ? "Réglé : " + c.count() + " × " + c.role().replace('_', ' ') + ".  " : "Pas encore réglé.  ") + where);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#GenFamily", EventData.of("Action", "family").append("@Family", "#GenFamily.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GenSave", EventData.of("Action", "save")
                .append("@Role", "#GenRole.Value").append("@Count", "#GenCount.Value"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GenRemove", EventData.of("Action", "remove"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        if ("family".equals(data.action)) {
            UICommandBuilder ui = new UICommandBuilder();
            List<DropdownEntryInfo> roles = roleEntries(data.family == null ? "tous" : data.family);
            ui.set("#GenRole.Entries", roles);
            if (!roles.isEmpty()) ui.set("#GenRole.Value", roles.get(0).value());
            sendUpdate(ui);
            return;
        }
        this.close();
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null || !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;
        World w = Universe.get().getWorld(world);
        if (w == null) return;
        if ("remove".equals(data.action)) {
            w.execute(() -> {
                Generateurs.set(w, pos.x(), pos.y(), pos.z(), null);
                com.eldanior.system.util.WorldBlockUtil.setBlock(w, pos.x(), pos.y(), pos.z(), "Empty");
                pr.sendMessage(Message.raw("[Générateur] Retiré.").color("#e8a050"));
            });
            return;
        }
        if (data.role == null || data.role.isEmpty()) { pr.sendMessage(Message.raw("[Générateur] Choisissez un monstre.").color("#e0574a")); return; }
        int n = Math.max(1, Math.min(20, data.count));
        Generateurs.set(w, pos.x(), pos.y(), pos.z(), new Generateurs.Config(data.role, n));
        pr.sendMessage(Message.raw("[Générateur] " + n + " × " + data.role.replace('_', ' ') + " : apparaissent à l'approche d'un joueur, reviennent 1 h après leur mort.")
                .color("#00e7b6"));
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("@Family", Codec.STRING), (d, v) -> d.family = v, d -> d.family).add()
                .append(new KeyedCodec<>("@Role", Codec.STRING), (d, v) -> d.role = v, d -> d.role).add()
                .append(new KeyedCodec<>("@Count", Codec.INTEGER), (d, v) -> d.count = v, d -> d.count).add()
                .build();
        public String action, family, role;
        public int count;
    }
}
