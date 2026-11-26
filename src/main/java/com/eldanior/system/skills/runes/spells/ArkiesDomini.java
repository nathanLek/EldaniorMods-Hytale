package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.hud.ArkiesDominiHud;
import com.eldanior.system.skills.system.InvisibilityManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Rune Arkies Domini (Tenebres, Legendaire), competence de l'Observateur (classe unique du Roi et de l'Empereur Luo ;
 * demandee le 2026-11-16, nommee le 2026-11-17). Rune a bascule : un clic l'ouvre, un second clic la referme ; elle preleve
 * chaque seconde 1,5 % du mana maximal de base (10 au moins). Tant qu'elle est active :
 *  - Caracteristiques x2 (PlayerLevelData.statMultiplier, lu par getTotal* et StatConfig) ; la vie et le mana courants ne
 *    bougent pas a l'activation (sinon activer / couper en boucle soignerait, comme l'Aura l'evite).
 *  - Tout voir (melange du Sens martial) : domaine de RADIUS blocs ; chaque presence (joueurs et PNJ, allies compris) est
 *    enveloppee d'une silhouette vue du seul lanceur (les MARKS plus proches), les presences cachees par un mur ont leur
 *    double rouge devant le mur (WallGhosts, GHOSTS plus proches), les invisibles sont reveles.
 *  - Tout entendre : arrivees et departs des joueurs, combats (vie qui baisse) et morts dans le domaine, rapportes dans le
 *    chat avec la distance et le cote (devant, derriere, gauche, droite).
 *  - Un regard : l'entite visee au viseur, meme derriere un mur, donne sa carte complete dans le HUD (ArkiesDominiHud),
 *    avec son modele en 3D (2026-11-17 : portrait Eldanior_Visage_<modele> dans un ItemPreviewComponent, voir Visages ;
 *    remplace la miniature privee qui flottait dans le monde). L'Oeil voit a travers Sans Visage (vrai nom, vraie classe).
 * Fiche de conception : scripts/runes/specs/Rune_ArkiesDomini.json
 */
public final class ArkiesDomini implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    static final double RADIUS = 100;
    static final float STAT_MULTIPLIER = 2f;
    /** Mana preleve chaque seconde : part du mana maximal de base (sans le x2), au moins MANA_MIN. */
    static final float MANA_SHARE = 0.015f;
    static final int MANA_MIN = 10;
    static final int MARKS = 40, GHOSTS = 12, HEARD = 40;
    static final long SECOND_MS = 1_000, DRAW_MS = 150, SONAR_MS = 3_000, LOOK_MS = 150, PROFILE_MS = 500;
    /** Combat entendu : baisse de vie d'au moins COMBAT_DROP du maximum en une seconde ; au plus COMBAT_LINES lignes par seconde. */
    static final float COMBAT_DROP = 0.03f;
    static final int COMBAT_LINES = 2;
    static final double EYE = 1.6;

    // ===== Assets (verifies : rune_tool assets) =====
    private static final String FX_START = "Eldanior_Pas_Celeste", FX_REVEAL = "Gilgamesh_Sword_Burst", FX_SONAR = "Ki_Anneau_T8",
            FX_MARK_RING = "Ki_Anneau_T1", SFX_START = "SFX_Ability_Wind_Strike_Imbue";
    private static final double[] PART_Y = {1.65, 1.1, 0.45};
    private static final String[] PART_FX = {"Sens_Silhouette_Tete", "Sens_Silhouette_Corps", "Sens_Silhouette_Jambes"};

    @Override public String id() { return "ArkiesDomini"; }

    /** true si la rune est ouverte pour ce joueur (filet de securite du x2 : PlayerNameplateSystem). */
    public static boolean isActive(UUID uuid) {
        return uuid != null && RuneSpellRuntime.find(uuid, Active.class) != null;
    }

    /** Presence visee par l'Oeil (meme derriere un mur) tant que la rune est ouverte, sinon null (Sentence Silencieuse). */
    public static Ref<EntityStore> lookedAt(UUID uuid) {
        Active a = uuid == null ? null : RuneSpellRuntime.find(uuid, Active.class);
        Ref<EntityStore> r = a != null ? a.looked : null;
        return r != null && r.isValid() ? r : null;
    }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Active running = RuneSpellRuntime.find(uuid, Active.class);
        if (running != null) {
            running.stop = true;
            MartialKi.notify(caster, store, "<color:gray>Arkies Domini se referme : vos sens et votre force reviennent à la normale.</color>",
                    NotificationStyle.Default);
            return null;
        }
        PlayerLevelData data = store.getComponent(caster, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return null;
        int cost = cost(data);
        if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
            MartialKi.notify(caster, store, "<color:red>Pas assez de mana.</color>", NotificationStyle.Warning);
            return null;
        }
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        cb.run(s -> setMultiplier(caster, s, STAT_MULTIPLIER));            // differe : le recalcul ecrit dans le monde (voir setMultiplier)
        SpellFx.fx(FX_START, new Vector3d(pos).add(0, 0.1, 0), 2.0f, store, cb);
        SpellFx.fxFor(FX_SONAR, new Vector3d(pos).add(0, 0.05, 0), 6.0f, caster, store);
        SpellFx.sound(SFX_START, pos, cb);
        Active a = new Active(caster, uuid, System.currentTimeMillis());
        a.hud = ArkiesDominiHud.attach(caster, store);
        a.scan(caster, pos, store);
        MartialKi.notify(caster, store, "<color:gold>Arkies Domini</color> <color:gray>— caractéristiques x2, domaine de "
                + (int) RADIUS + " blocs : " + a.tracked.size() + " présence(s). Visez pour lire un profil.</color>", NotificationStyle.Success);
        return a;
    }

    /** Mana preleve chaque seconde : 1,5 % du mana maximal de base (sans le x2), au moins 10. */
    static int cost(PlayerLevelData data) {
        float m = data.getStatMultiplier();
        double baseMax = data.getMaxMana() / (m > 0 ? m : 1f);
        return Math.max(MANA_MIN, (int) Math.round(baseMax * MANA_SHARE));
    }

    /**
     * Pose le multiplicateur des caracteristiques et recalcule les stats ; la vie et le mana courants sont gardes (bornes par
     * le nouveau maximum) : aucun soin gratuit a l'activation.
     * JAMAIS pendant le tick d'un systeme (start, tick) : StatCalculator ecrit dans le monde (« Store is currently
     * processing! »), le x2 restait pose sans recalcul (stats incoherentes, soin a l'activation, degats faux ; vu le
     * 2026-11-17). Toujours passer par cb.run(s -> setMultiplier(ref, s, m)).
     */
    static void setMultiplier(Ref<EntityStore> ref, Store<EntityStore> store, float m) {
        if (ref == null || !ref.isValid()) return;
        try {
            PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (d == null || d.getStatMultiplier() == m) return;
            int hp = DefaultEntityStatTypes.getHealth(), mana = DefaultEntityStatTypes.getMana();
            float hpNow = MartialKi.stat(ref, hp, store), manaNow = MartialKi.stat(ref, mana, store);
            d.setStatMultiplier(m);
            StatCalculator.updatePlayerStats(ref, store, d);
            EntityStatMap map = store.getComponent(ref, EntityStatMap.getComponentType());
            if (map == null) return;
            for (int[] s : new int[][]{{hp}, {mana}}) {
                float before = s[0] == hp ? hpNow : manaNow;
                var v = map.get(s[0]);
                if (v != null && before >= 0) map.setStatValue(s[0], Math.min(before, v.getMax()));
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Arkies Domini] multiplicateur", e);
        }
    }

    static final class Active implements Instance {
        final Ref<EntityStore> caster;
        final UUID uuid;
        final long startAt;
        volatile boolean stop;
        long lastSecond, lastDraw, lastSonar, lastLook, lastProfile;
        int draws;
        List<Ref<EntityStore>> tracked = new ArrayList<>();
        final WallGhosts walls = new WallGhosts();
        ArkiesDominiHud hud;
        // ecoute
        final Map<UUID, String> playersInside = new HashMap<>();
        final Map<Ref<EntityStore>, float[]> heard = new HashMap<>();   // {vie, max} a la derniere seconde
        final Map<Ref<EntityStore>, String> names = new HashMap<>();
        boolean primed;
        // regard
        Ref<EntityStore> looked;

        Active(Ref<EntityStore> caster, UUID uuid, long now) { this.caster = caster; this.uuid = uuid; this.startAt = now; this.lastSecond = now; }

        /** Presences du domaine (joueurs et PNJ, sauf le lanceur), des plus proches aux plus lointaines ; invisibles reveles. */
        void scan(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store) {
            List<Ref<EntityStore>> all = new ArrayList<>();
            for (Ref<EntityStore> e : TargetUtil.getAllEntitiesInSphere(me, RADIUS, store)) {
                if (e == null || !e.isValid() || e.equals(caster) || walls.isGhost(e)) continue;
                if (store.getComponent(e, Player.getComponentType()) == null && store.getComponent(e, NPCEntity.getComponentType()) == null) continue;
                UUID id = SpellFx.uuid(e, store);
                if (id != null && InvisibilityManager.isInvisible(id)) {
                    InvisibilityManager.removeInvisibility(id, e, store);
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null) SpellFx.fxFor(FX_REVEAL, new Vector3d(p).add(0, 1.2, 0), 1.0f, caster, store);
                    MartialKi.notify(e, store, "<color:red>Votre présence a été percée à jour.</color>", NotificationStyle.Warning);
                }
                all.add(e);
            }
            all.sort(Comparator.comparingDouble(e -> {
                Vector3d p = SpellFx.position(e, store);
                return p == null ? Double.MAX_VALUE : p.distanceSquared(me);
            }));
            tracked = all;
        }

        // ===================== VOIR =====================

        /** Silhouettes et anneaux sur les MARKS presences les plus proches, vus du seul lanceur. */
        private void draw(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store) {
            double r2 = RADIUS * RADIUS;
            boolean ring = ++draws % 4 == 0;
            int n = 0;
            for (Ref<EntityStore> e : tracked) {
                if (n >= MARKS) break;
                Vector3d p = e != null && e.isValid() ? SpellFx.position(e, store) : null;
                if (p == null || p.distanceSquared(me) > r2) continue;
                n++;
                for (int i = 0; i < PART_Y.length; i++)
                    SpellFx.fxFor(PART_FX[i], new Vector3d(p).add(0, PART_Y[i], 0), 1.0f, caster, store);
                if (ring) SpellFx.fxFor(FX_MARK_RING, new Vector3d(p).add(0, 0.05, 0), 0.6f, caster, store);
            }
        }

        // ===================== ENTENDRE =====================

        /** Chaque seconde : arrivees et departs des joueurs, combats (vie qui baisse) et morts dans le domaine. */
        private void listen(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store) {
            PlayerRef pr = store.getComponent(caster, PlayerRef.getComponentType());
            if (pr == null) return;
            Vector3d look = SpellFx.lookDirection(caster, store);
            Map<UUID, String> now = new HashMap<>();
            Map<UUID, Vector3d> where = new HashMap<>();
            for (Ref<EntityStore> e : tracked) {
                PlayerRef other = e != null && e.isValid() ? store.getComponent(e, PlayerRef.getComponentType()) : null;
                if (other == null) continue;
                now.put(other.getUuid(), other.getUsername());
                Vector3d p = SpellFx.position(e, store);
                if (p != null) where.put(other.getUuid(), p);
            }
            if (primed) {
                for (var en : now.entrySet())
                    if (!playersInside.containsKey(en.getKey()))
                        hear(pr, en.getValue() + " entre dans votre domaine — " + place(me, look, where.get(en.getKey())), new Color(0xB58AD8));
                for (var en : playersInside.entrySet())
                    if (!now.containsKey(en.getKey())) hear(pr, en.getValue() + " quitte votre domaine.", new Color(0x8A7FA0));
            }
            playersInside.clear();
            playersInside.putAll(now);

            // combats et morts : vie des HEARD presences les plus proches
            int lines = 0;
            Set<Ref<EntityStore>> seen = new HashSet<>();
            int n = 0;
            for (Ref<EntityStore> e : tracked) {
                if (n++ >= HEARD) break;
                if (e == null || !e.isValid()) continue;
                seen.add(e);
                float[] hp = health(e, store);
                if (hp == null) continue;
                float[] before = heard.put(e, hp);
                names.computeIfAbsent(e, k -> name(k, store));
                if (!primed || before == null || hp[1] <= 0) continue;
                Vector3d p = SpellFx.position(e, store);
                if (hp[0] <= 0 && before[0] > 0) {
                    hear(pr, names.get(e) + " est tombé — " + place(me, look, p), new Color(0xE05050));
                } else if (lines < COMBAT_LINES && before[0] - hp[0] >= COMBAT_DROP * hp[1]) {
                    lines++;
                    int pct = Math.round((before[0] - hp[0]) / hp[1] * 100);
                    hear(pr, "Bruits de combat : " + names.get(e) + " blessé (−" + pct + " %) — " + place(me, look, p), new Color(0xE0A050));
                }
            }
            for (var it = heard.entrySet().iterator(); it.hasNext(); ) {       // sortis du domaine ou retires du monde : oublies
                var en = it.next();                                             // (une mort est deja rapportee : vie a 0)
                if (seen.contains(en.getKey())) continue;
                names.remove(en.getKey());
                it.remove();
            }
            primed = true;
        }

        private static void hear(PlayerRef pr, String text, Color color) {
            try { pr.sendMessage(Message.raw("[Arkies Domini] " + text).color(color)); } catch (Exception ignored) { }
        }

        /** Distance et cote d'un point par rapport au regard du lanceur (« 34 m, devant à gauche, au-dessus »). */
        static String place(Vector3d me, Vector3d look, Vector3d p) {
            if (p == null) return "position inconnue";
            Vector3d to = new Vector3d(p).sub(me);
            int dist = (int) Math.round(to.length());
            Vector3d fwd = new Vector3d(look.x, 0, look.z);
            if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
            fwd.normalize();
            Vector3d right = new Vector3d(-fwd.z, 0, fwd.x);                  // meme convention que AiglesDuDesert / ArmesDivines
            Vector3d flat = new Vector3d(to.x, 0, to.z);
            String side = "";
            if (flat.lengthSquared() > 1) {
                flat.normalize();
                double f = flat.dot(fwd), r = flat.dot(right);
                String fb = f > 0.38 ? "devant" : f < -0.38 ? "derrière" : "";
                String lr = r > 0.38 ? "à droite" : r < -0.38 ? "à gauche" : "";
                side = fb.isEmpty() ? lr : lr.isEmpty() ? fb : fb + " " + lr;
            }
            String height = to.y > 6 ? "au-dessus" : to.y < -6 ? "en dessous" : "";
            StringBuilder sb = new StringBuilder().append(dist).append(" m");
            if (!side.isEmpty()) sb.append(", ").append(side);
            if (!height.isEmpty()) sb.append(", ").append(height);
            return sb.toString();
        }

        // ===================== UN REGARD =====================

        /** Presence sous le viseur, meme derriere un mur (dans le domaine) ; null si aucune. */
        private Ref<EntityStore> aimed(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store) {
            Vector3d eye = new Vector3d(me).add(0, EYE, 0), dir = SpellFx.lookDirection(caster, store);
            Ref<EntityStore> best = null;
            double bestT = Double.MAX_VALUE;
            for (Ref<EntityStore> e : tracked) {
                Vector3d p = e != null && e.isValid() ? SpellFx.position(e, store) : null;
                if (p == null) continue;
                var mc = store.getComponent(e, ModelComponent.getComponentType());
                double h = mc != null && mc.getModel() != null ? Math.max(0.5, mc.getModel().getEyeHeight()) : 1.6;
                Vector3d to = new Vector3d(p).add(0, h * 0.55, 0).sub(eye);
                double t = to.dot(dir);
                if (t < 0.5 || t > RADIUS) continue;
                double perp = new Vector3d(to).sub(new Vector3d(dir).mul(t)).length();
                if (perp > Math.max(0.6 + h * 0.35, t * 0.03)) continue;         // tolerance qui s'elargit avec la distance
                if (t < bestT) { bestT = t; best = e; }
            }
            return best;
        }

        /** Carte de la presence visee (ArkiesDominiHud.Profile, style carte de quete) ; null = carte cachee. */
        private void profile(Ref<EntityStore> e, Vector3d me, Store<EntityStore> store) {
            if (hud == null) return;
            if (e == null || !e.isValid()) { hud.show(null); return; }
            Vector3d p = SpellFx.position(e, store);
            Vector3d look = SpellFx.lookDirection(caster, store);
            float[] hp = health(e, store);
            float hpNow = hp != null ? hp[0] : 0, hpMax = hp != null ? hp[1] : 0;
            var mc = store.getComponent(e, ModelComponent.getComponentType());
            String modelId = mc != null && mc.getModel() != null ? mc.getModel().getModelAssetId() : null;
            String portrait = com.eldanior.system.classes.observateur.Visages.portrait(modelId);
            String dist = place(me, look, p);
            boolean wall = hidden(e, me, store);
            List<String> rows = new ArrayList<>();
            PlayerRef other = store.getComponent(e, PlayerRef.getComponentType());
            if (other != null) {
                PlayerLevelData d = store.getComponent(e, EldaniorSystem.get().getPlayerLevelDataType());
                UUID oid = other.getUuid();
                String sub = "", classLine = "Sans classe", rankLine = "Sans guilde";
                List<String> stats = null;
                com.eldanior.system.quest.QuestDifficulty rank = null;
                int level = -1;
                float mana = -1, manaMax = -1;
                var mm = store.getComponent(e, EntityStatMap.getComponentType());
                var mv = mm == null ? null : mm.get(DefaultEntityStatTypes.getMana());
                if (mv != null) { mana = mv.get(); manaMax = mv.getMax(); }
                if (d != null) {
                    level = d.getLevel();
                    rank = rankOf(level);
                    ClassModel real = ClassManager.get(d.getPlayerClassId());
                    ClassModel shown = SansVisage.shownClass(oid, d);
                    classLine = (shown != null ? shown.getDisplayName() : "Sans classe")
                            + (shown != real && real != null ? "  (vraie : " + real.getDisplayName() + ")" : "");
                    stats = List.of(String.valueOf(d.getTotalStrength()), String.valueOf(d.getTotalVitality()),
                            String.valueOf(d.getTotalIntelligence()), String.valueOf(d.getTotalEndurance()),
                            String.valueOf(d.getTotalAgility()), String.valueOf(d.getTotalLuck()));
                    var noble = com.eldanior.system.titles.nobility.NobilityRank.fromString(d.getNobilityRank());
                    String fam = d.getNobleFamilyId();
                    var family = fam != null && !fam.isEmpty() ? com.eldanior.system.titles.nobility.family.FamilyManager.get(
                            com.eldanior.system.titles.nobility.family.FamilyManager.migrateId(fam)) : null;
                    sub = (noble != null ? noble.getDisplayName() : "Roturier")
                            + (family != null ? "  ·  Maison " + family.getDisplayName() + (d.isPatriarch() ? " (Patriarche)" : "") : "");
                    var guild = d.hasGuild() ? com.eldanior.system.guild.GuildManager.getPlayerGuild(oid) : null;
                    if (guild != null) rankLine = "Guilde " + guild.getName() + (d.isGuildChef() ? " (chef)" : "");
                    var throne = com.eldanior.system.classes.observateur.ObservateurManager.throneHeldBy(oid);
                    if (throne != null) rows.add("Observateur du " + throne.label);
                    var mask = SansVisage.mask(oid);
                    if (mask != null) rows.add("Déguisé en " + mask.label());
                    String alias = com.eldanior.system.classes.observateur.ObservateurManager.alias(oid, d);
                    if (alias != null) rows.add("Se fait appeler « " + alias + " »");
                    if (d.isPK() || d.getBounty() > 0)
                        rows.add((d.isPK() ? "Criminel" : "Recherché") + (d.getBounty() > 0 ? "  ·  prime " + com.eldanior.system.shop.CoinWallet.format(d.getBounty()) : ""));
                    var church = com.eldanior.system.titles.church.ChurchRank.fromString(d.getChurchRank());
                    if (church != null && !"LAIQUE".equals(d.getChurchRank())) rows.add("Église : " + church.getDisplayName());
                    rows.add("Fortune : " + com.eldanior.system.shop.CoinWallet.format(d.getMoney()));
                }
                Player pl = store.getComponent(e, Player.getComponentType());
                var held = pl != null && pl.getInventory() != null ? pl.getInventory().getActiveHotbarItem() : null;
                if (held != null && !held.isEmpty()) rows.add("En main : " + pretty(held.getItemId()));
                // l'Oeil voit a travers Sans Visage : modele porte en 3D, mais vrai nom au titre et vraie classe
                hud.show(new ArkiesDominiHud.Profile(ArkiesDominiHud.Kind.PLAYER, "Joueur" + (level >= 0 ? "  ·  Niveau " + level : ""),
                        other.getUsername(), sub, rank, portrait, hpNow, hpMax, mana, manaMax, stats, classLine, rankLine,
                        trim(rows), dist, wall));
                return;
            }
            NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
            String role = npc != null ? npc.getNPCTypeId() : null;
            var mob = store.getComponent(e, EldaniorSystem.get().getMobLevelDataType());
            int level = mob != null && mob.isStatsApplied() ? mob.getLevel() : -1;
            var guard = role != null ? com.eldanior.system.guards.GuardType.fromRole(role) : null;
            UUID id = SpellFx.uuid(e, store);
            boolean contract = com.eldanior.system.boards.MonsterContracts.isContractMob(e, store);
            boolean dungeonBoss = id != null && com.eldanior.system.boards.DungeonBosses.isBoss(id);
            boolean boss = SentenceSilencieuse.isBoss(e, store);
            com.eldanior.system.quest.QuestDifficulty rank = level >= 0 ? rankOf(level) : null;
            String classLine = guard != null ? guard.getLabel() : dungeonBoss ? "Dungeon Boss" : contract ? "Monstre étoilé (contrat)"
                    : boss ? "Boss" : "Créature sauvage";
            String rankLine = boss ? "Ne peut être exécuté" : "Exécution sous 40 % de vie";
            if (modelId != null && !modelId.equals(role)) rows.add("Apparence : " + pretty(modelId));
            if (contract && !"Monstre étoilé (contrat)".equals(classLine)) rows.add("Monstre étoilé (contrat)");
            hud.show(new ArkiesDominiHud.Profile(guard != null ? ArkiesDominiHud.Kind.NPC : ArkiesDominiHud.Kind.BEAST,
                    (guard != null ? "PNJ du royaume" : "Créature") + (level >= 0 ? "  ·  Niveau " + level : ""),
                    name(e, store), role != null ? pretty(role) : "", rank, portrait, hpNow, hpMax, -1, -1, null,
                    classLine, rankLine, trim(rows), dist, wall));
        }

        /** Rang de menace d'un niveau : echelle des quetes (F a S), SS et EX au-dela du niveau 999. */
        private static com.eldanior.system.quest.QuestDifficulty rankOf(int level) {
            return com.eldanior.system.quest.QuestDifficulty.max(com.eldanior.system.quest.QuestManager.difficultyFromLevel(level),
                    com.eldanior.system.quest.QuestDifficulty.forMonsterLevel(level));
        }

        private static List<String> trim(List<String> rows) {
            return rows.size() <= ArkiesDominiHud.ROWS ? rows : rows.subList(0, ArkiesDominiHud.ROWS);
        }

        private static boolean hidden(Ref<EntityStore> e, Vector3d me, Store<EntityStore> store) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) return false;
            Vector3d eye = new Vector3d(me).add(0, EYE, 0), mid = new Vector3d(p).add(0, 1.0, 0);
            return WallGhosts.firstWall(SpellFx.world(store), eye, mid, eye.distance(mid)) >= 0;
        }

        // ===================== CYCLE =====================

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (stop) return true;
            Vector3d me = SpellFx.position(caster, store);
            if (me == null) return true;
            if (now - lastSecond >= SECOND_MS) {
                lastSecond = now;
                PlayerLevelData data = store.getComponent(caster, EldaniorSystem.get().getPlayerLevelDataType());
                if (data == null) return true;
                int cost = cost(data);
                if (MartialKi.stat(caster, DefaultEntityStatTypes.getMana(), store) < cost) {
                    MartialKi.notify(caster, store, "<color:gray>Arkies Domini se referme : mana épuisé.</color>", NotificationStyle.Warning);
                    return true;
                }
                MartialKi.addStat(caster, DefaultEntityStatTypes.getMana(), -cost, store);
                if (data.getStatMultiplier() != STAT_MULTIPLIER) cb.run(s -> setMultiplier(caster, s, STAT_MULTIPLIER));   // fiche remplacee entre-temps
                scan(caster, me, store);
                listen(caster, me, store);
            }
            if (now - lastDraw >= DRAW_MS) { lastDraw = now; draw(caster, me, store); }
            walls.update(caster, me, tracked, RADIUS, GHOSTS, store, cb);
            if (now - lastSonar >= SONAR_MS) {                                  // onde du domaine, vue du seul lanceur
                lastSonar = now;
                SpellFx.fxFor(FX_SONAR, new Vector3d(me).add(0, 0.08, 0), 14.0f, caster, store);
            }
            if (now - lastLook >= LOOK_MS) {
                lastLook = now;
                Ref<EntityStore> aim = aimed(caster, me, store);
                if (aim != looked || now - lastProfile >= PROFILE_MS) {
                    looked = aim;
                    lastProfile = now;
                    profile(aim, me, store);
                }
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            walls.cleanup(cb);
            Ref<EntityStore> c = caster;
            cb.run(store -> {
                ArkiesDominiHud.detach(c, store);
                setMultiplier(c, store, 1f);
            });
        }

        @Override
        public void abort() {
            walls.abort();
        }
    }

    // ===================== OUTILS =====================

    /** {vie, vie max}, ou null si illisible. */
    static float[] health(Ref<EntityStore> e, Store<EntityStore> store) {
        try {
            EntityStatMap map = store.getComponent(e, EntityStatMap.getComponentType());
            var v = map == null ? null : map.get(DefaultEntityStatTypes.getHealth());
            return v == null ? null : new float[]{v.get(), v.getMax()};
        } catch (Exception ex) { return null; }
    }

    /** Nom d'une presence : joueur (vrai nom), sinon le nom affiche, sinon l'espece. */
    static String name(Ref<EntityStore> e, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(e, PlayerRef.getComponentType());
        if (pr != null) return pr.getUsername();
        Nameplate np = store.getComponent(e, Nameplate.getComponentType());
        if (np != null && np.getText() != null && !np.getText().isBlank()) return np.getText();
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        return npc != null ? pretty(npc.getNPCTypeId()) : "Inconnu";
    }

    /** « Eldanior_Weapon_Sword_Katana » -> « Weapon Sword Katana ». */
    static String pretty(String id) {
        if (id == null) return "?";
        return id.replaceFirst("^(Eldanior|Elda)_", "").replace('_', ' ');
    }
}
