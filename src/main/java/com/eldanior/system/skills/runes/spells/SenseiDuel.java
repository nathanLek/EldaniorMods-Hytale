package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Duel d'entrainement contre un Sensei (2026-11-10, demande de l'utilisateur). Le Sensei du poste est un PNJ civil
 * (invulnerable, il ne se bat pas) : pour le duel, il est cache a tous (PrivateVisibility) et un combattant a son image
 * (modele, skin, nom, tenue, katana) apparait a sa place, avec le role de combat des clones (Eldanior_Clone_Lumiere), qui
 * attaque la cible qu'on lui marque (LockedTarget) ; il est rendu hostile au seul joueur defie.
 * Sans mort : le joueur ne descend jamais sous 1 PV (onOwnerHurt), il perd s'il tombe sous LOSE_PCT de sa vie ; il gagne si
 * le Sensei tombe sous LOSE_PCT (ou meurt). Forfait a plus de LEASH blocs ; nul au bout de MAX_MS. A la fin : le combattant
 * disparait, le Sensei reapparait.
 *
 * 2026-11-10 (demande de l'utilisateur) : le Sensei est Grand Maitre Martial, Saint Martial ou Maitre Supreme
 * (MartialRanks.senseiLevel) et son combattant utilise toutes les techniques de son rang (MartialNpcs.enlist : aura de Ki,
 * pas leger, projection d'epee, lames de Ki, rafale, Incarnation de l'Ame) et de son clan. En duel il manie le Bokken du Dojo ;
 * si on l'attaque (riposte, Request.lethal), il tire un katana de fer et se bat pour de vrai : pas de filet a 1 PV, fin quand
 * l'agresseur meurt, s'enfuit (LEASH_RIPOSTE), ou quand le Sensei tombe sous LOSE_PCT (il rompt le combat).
 */
public final class SenseiDuel implements RuneSpell {

    /** Defi en attente : lance au prochain passage du moteur des sorts (RuneSpellRuntime.queue). */
    public record Request(Ref<EntityStore> sensei, List<String> armor, String weapon, String speaker, int level,
                          com.eldanior.system.luo.LuoClans.Clan clan, boolean lethal, String postId, boolean toDeath) {}

    /** Sensei dont le combattant est sorti (duel ou riposte) : un seul a la fois. */
    private static final Map<Ref<EntityStore>, Long> BUSY = new ConcurrentHashMap<>();

    /** true si ce Sensei est deja en combat (duel ou riposte) ; oublie apres la duree maximale (defi jamais lance). */
    public static boolean busy(Ref<EntityStore> sensei) {
        long now = System.currentTimeMillis();
        BUSY.entrySet().removeIf(e -> !e.getKey().isValid() || now - e.getValue() > MAX_MS + 10_000);
        return sensei != null && BUSY.containsKey(sensei);
    }

    private static final Map<UUID, Request> REQUESTS = new ConcurrentHashMap<>();
    private static final String ROLE = "Eldanior_Clone_Lumiere", HP_MODIFIER = "Eldanior_Sensei_Vie";
    private static final float LOSE_PCT = 0.15f, HP_FACTOR = 2.5f, MIN_HP = 500f;
    private static final double LEASH = 30.0, LEASH_RIPOSTE = 45.0;
    private static final long MAX_MS = 180_000, SPAWN_TIMEOUT_MS = 4_000, REASSERT_MS = 1_000;

    @Override public String id() { return "SenseiDuel"; }

    /** Le joueur defie le Sensei. false s'il est deja en duel ou si la demande est invalide. */
    public static boolean challenge(Ref<EntityStore> player, Request request, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(player, store);
        if (uuid == null || request == null || request.sensei() == null || !request.sensei().isValid()) return false;
        if (RuneSpellRuntime.find(uuid, Duel.class) != null || busy(request.sensei())) return false;
        BUSY.put(request.sensei(), System.currentTimeMillis());
        REQUESTS.put(uuid, request);
        RuneSpellRuntime.queue(player, "SenseiDuel");
        return true;
    }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(caster, store);
        Request r = uuid != null ? REQUESTS.remove(uuid) : null;
        if (r == null) return null;
        if (!r.sensei().isValid()) { BUSY.remove(r.sensei()); return null; }
        Vector3d at = SpellFx.position(r.sensei(), store), me = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (at == null || me == null || world == null) { BUSY.remove(r.sensei()); return null; }
        CloneFx.Look look = CloneFx.Look.of(r.sensei(), store);
        Duel d = new Duel(caster, r, new Vector3d(at), System.currentTimeMillis());
        float hp = Math.max(MIN_HP, CloneFx.maxHealth(caster, store) * HP_FACTOR);
        Rotation3f rot = SpellFx.facing(new Vector3d(me).sub(at), false);
        world.execute(() -> {
            try {
                Store<EntityStore> s = world.getEntityStore().getStore();
                var pair = NPCPlugin.get().spawnNPC(s, ROLE, null, d.origin, rot);
                Ref<EntityStore> ref = pair != null ? pair.first() : null;
                if (ref == null || !ref.isValid()) { d.failed = true; return; }
                if (look.model() != null) s.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(look.model()));
                if (look.skin() != null) s.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(look.skin()));
                s.putComponent(ref, Nameplate.getComponentType(), new Nameplate((look.nameplate() != null ? look.nameplate() : "Sensei")
                        + (r.lethal() ? "" : " — Duel")));
                NPCEntity npc = s.getComponent(ref, NPCEntity.getComponentType());
                var inv = npc != null ? npc.getInventory() : null;
                if (inv != null && inv.getArmor() != null)
                    for (String id : r.armor()) {
                        short slot = id.endsWith("_Head") ? 0 : id.endsWith("_Chest") ? (short) 1 : id.endsWith("_Hands") ? (short) 2 : (short) 3;
                        inv.getArmor().setItemStackForSlot(slot, new ItemStack(id, 1));
                    }
                // katana : forme au clair (la forme au fourreau n'a rien en main droite : katana invisible, retour du 2026-11-11)
                // et fourreau vide en main gauche, comme les PNJ du clan
                LuoKatanas.Katana kat = LuoKatanas.of(r.weapon());
                CloneFx.setWeapon(ref, r.weapon() == null ? null : new ItemStack(kat != null ? kat.drawn() : r.weapon(), 1), s);
                if (kat != null && inv != null && inv.getUtility() != null) {
                    inv.getUtility().setItemStackForSlot((short) 0, new ItemStack(kat.sheathEmpty(), 1));
                    inv.setActiveUtilitySlot(ref, (byte) 0, s);
                }
                // statistiques des monstres selon le niveau (retour du 2026-11-11) : degats +DAMAGE_PER_LEVEL par niveau
                // (CombatStatsSystem lit MobLevelData) ; la vie suit plus bas
                s.putComponent(ref, com.eldanior.system.EldaniorSystem.get().getMobLevelDataType(),
                        new com.eldanior.system.config.configs.Mobs.MobLevelData(r.level(), ROLE, true));
                EntityStatMap stats = s.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
                if (stats != null) {
                    int health = DefaultEntityStatTypes.getHealth();
                    var cur = stats.get(health);
                    // vie des monstres du meme niveau (base + HP_PER_LEVEL par niveau), au moins celle prevue face au joueur
                    float levelHp = cur != null ? cur.getMax() + r.level() * com.eldanior.system.config.configs.MobsWorldConfig.HP_PER_LEVEL : 0f;
                    if (cur != null) stats.putModifier(health, HP_MODIFIER,
                            new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, Math.max(hp, levelHp) - cur.getMax()));
                    stats.maximizeStatValue(health);
                }
                MartialNpcs.enlist(ref, r.level(), r.clan());                  // techniques de son rang et de son clan
                d.fighter = ref;
            } catch (Exception e) {
                d.failed = true;
                EldaniorLogger.error("[Sensei] duel : combattant", e);
            }
        });
        PrivateVisibility.show(r.sensei(), r.sensei());                          // le PNJ civil se retire (cache a tous)
        SpellFx.fx("Ki_Anneau_T6", new Vector3d(at).add(0, 0.05, 0), 2.0f, store, cb);
        SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", at, cb);
        if (r.toDeath()) {                                                       // disciple (2026-11-13) : a mort, katana de fer
            SpellFx.sound("SFX_Sword_T2_Swing_Down", at, cb);
            MartialKi.notify(caster, store, "<color:red>" + r.speaker() + " : « Lame contre lame. Un seul de nous deux repartira. »</color>"
                    + " <color:gray>— duel au katana de fer, à mort.</color>", NotificationStyle.Danger);
        } else if (r.lethal()) {
            SpellFx.sound("SFX_Sword_T2_Swing_Down", at, cb);
            MartialKi.notify(caster, store, "<color:red>" + r.speaker() + " : « Tu lèves la main sur ton maître ? Alors goûte à l'acier. »</color>",
                    NotificationStyle.Danger);
        } else
            MartialKi.notify(caster, store, "<color:gold>" + r.speaker() + " : « En garde ! »</color> <color:gray>— duel au Bokken, sans mort : le premier "
                    + "sous 15 % de sa vie a perdu ; s'éloigner de plus de " + (int) LEASH + " blocs = abandon.</color>", NotificationStyle.Success);
        return d;
    }

    static final class Duel implements Instance {
        final Ref<EntityStore> player;
        final Request req;
        final Vector3d origin;
        final long startAt;
        volatile Ref<EntityStore> fighter;
        volatile boolean failed, lost;
        long lastAssert;
        /** Part de vie du combattant au dernier passage : s'il disparait entre deux passages, dit s'il etait a l'agonie. */
        float lastFighterPct = 1f;

        Duel(Ref<EntityStore> player, Request req, Vector3d origin, long now) {
            this.player = player; this.req = req; this.origin = origin; this.startAt = now;
        }

        /** Le joueur ne meurt pas : le coup qui le ferait passer sous 1 PV le laisse a 1 PV et lui fait perdre le duel. */
        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (req.lethal()) return amount;                                     // riposte : le Sensei ne retient plus ses coups
            try {
                float hp = MartialKi.stat(player, DefaultEntityStatTypes.getHealth(), player.getStore());
                float max = CloneFx.maxHealth(player, player.getStore());
                if (hp - amount <= max * LOSE_PCT) {
                    lost = true;
                    return Math.max(0f, Math.min(amount, hp - 1f));
                }
            } catch (Exception ignored) { }
            return amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (failed || fighter == null && now - startAt > SPAWN_TIMEOUT_MS) {
                MartialKi.notify(caster, store, "<color:gray>Le Sensei n'a pas pu relever le défi.</color>", NotificationStyle.Warning);
                return true;
            }
            if (fighter == null) return false;
            Vector3d me = SpellFx.position(caster, store);
            // 2026-11-13 : le combattant n'est pas une invocation (RuneSpellRuntime.isDying ne le voit pas). Une victoire exige sa
            // vraie mort (composant de mort, vie a 0, ou disparition alors qu'il etait a l'agonie) : un combattant retire pour
            // une autre raison (chunk decharge quand le joueur reapparait loin, par exemple) n'est plus compte comme tue.
            if (playerDown(caster, store))                                       // le joueur est mort : defaite, avant tout le reste
                return end(caster, store, req.lethal()
                        ? "<color:red>" + req.speaker() + " : « Que la leçon te serve, dans l'autre vie. »</color>" + (req.toDeath() ? " <color:gray>— duel perdu</color>" : "")
                        : "<color:gray>Le combat s'arrête.</color> <color:gray>— duel perdu</color>");
            boolean slain = fighterDead(store);
            boolean gone = !fighter.isValid();
            if (gone && !slain)                                                  // disparu sans mourir : ni victoire, ni defaite
                return end(caster, store, "<color:gray>" + req.speaker() + " rompt le combat.</color> <color:gray>— duel interrompu</color>");
            if (!gone && !slain) {
                float fh = MartialKi.stat(fighter, DefaultEntityStatTypes.getHealth(), store), fm = CloneFx.maxHealth(fighter, store);
                if (fh >= 0f && fm > 0f) lastFighterPct = fh / fm;
            }
            if (req.lethal()) {
                if (slain) {
                    if (req.toDeath()) {                                          // disciple tue : il tombe vraiment (son poste le fera revenir)
                        com.eldanior.system.guards.GuardManager.killedInDuel(req.postId());
                        return won(caster, store, "<color:gold>" + req.speaker() + " tombe sous ta lame.</color> <color:gray>— duel gagné</color>");
                    }
                    return end(caster, store, "<color:gray>Le Sensei s'efface dans un souffle de Ki.</color>");
                }
                float hp = MartialKi.stat(fighter, DefaultEntityStatTypes.getHealth(), store), max = CloneFx.maxHealth(fighter, store);
                if (!req.toDeath() && max > 0 && hp <= max * LOSE_PCT)
                    return end(caster, store, "<color:gold>" + req.speaker() + " : « Assez ! Ta lame est forte, mais ton cœur est mauvais. »</color>");
                if (me == null || me.distance(origin) > LEASH_RIPOSTE)
                    return end(caster, store, "<color:gray>" + req.speaker() + " rengaine son katana : « Cours, lâche. »</color>"
                            + (req.toDeath() ? " <color:gray>— abandon</color>" : ""));
                if (now - startAt > MAX_MS) return end(caster, store, "<color:gray>" + req.speaker() + " rengaine son katana.</color>");
            } else {
            if (lost) return end(caster, store, "<color:red>" + req.speaker() + " : « Relève-toi. Ta garde est encore trop ouverte. »</color> <color:gray>— duel perdu</color>");
            if (slain)
                return won(caster, store, "<color:gold>" + req.speaker() + " : « ... Bien. Tu as compris la leçon. »</color> <color:gray>— duel gagné</color>");
            float fhp = MartialKi.stat(fighter, DefaultEntityStatTypes.getHealth(), store), fmax = CloneFx.maxHealth(fighter, store);
            if (fmax > 0 && fhp <= fmax * LOSE_PCT)
                return won(caster, store, "<color:gold>" + req.speaker() + " : « Il suffit ! Je m'incline, ta lame est juste. »</color> <color:gray>— duel gagné</color>");
            if (me == null || me.distance(origin) > LEASH)
                return end(caster, store, "<color:gray>Vous avez quitté le duel : abandon.</color>");
            if (now - startAt > MAX_MS) return end(caster, store, "<color:gray>Le temps est écoulé : match nul.</color>");
            }
            if (now - lastAssert >= REASSERT_MS) {                              // le combattant ne vise que le joueur defie
                lastAssert = now;
                com.eldanior.system.util.NpcAttitudes.hostile(fighter, caster, 10, store);
                CloneFx.mark(fighter, "LockedTarget", caster, store);
                CloneFx.setState(fighter, "Combat", "Default", store);
            }
            return false;
        }

        /** Le combattant est vraiment mort : composant de mort, vie a 0, ou disparu alors qu'il etait sous 5 % de sa vie. */
        private boolean fighterDead(Store<EntityStore> store) {
            Ref<EntityStore> f = fighter;
            if (f == null) return false;
            if (!f.isValid()) return lastFighterPct <= 0.05f;
            if (store.getComponent(f, com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) != null) return true;
            float hp = MartialKi.stat(f, DefaultEntityStatTypes.getHealth(), store);
            return hp >= 0f && hp <= 0.01f;
        }

        /** Le joueur est mort (composant de mort ou plus de vie) : RuneSpellRuntime.isDying ne vaut que pour les invocations. */
        private static boolean playerDown(Ref<EntityStore> caster, Store<EntityStore> store) {
            if (caster == null || !caster.isValid()) return true;
            if (store.getComponent(caster, com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) != null) return true;
            float hp = MartialKi.stat(caster, DefaultEntityStatTypes.getHealth(), store);   // -1 : illisible, pas mort
            return hp >= 0f && hp <= 0.01f;
        }

        /** Duel d'entrainement gagne : fin, et l'acte « Vaincre le Sensei » des quetes en chapitres avance. */
        private boolean won(Ref<EntityStore> caster, Store<EntityStore> store, String message) {
            try {
                com.hypixel.hytale.server.core.universe.PlayerRef pr = store.getComponent(caster,
                        com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
                UUID me = pr != null ? com.eldanior.system.config.UUIDExtractor.getUUID(pr) : null;
                if (com.eldanior.system.guards.MartialDuels.typeOf(req.postId()) == com.eldanior.system.guards.GuardType.SENSEI)
                    com.eldanior.system.quest.chapitres.ChapterQuests.onSenseiDuelWon(me);   // « Vaincre le Sensei »
                // quetes en chapitres (2026-11-13) : un PNJ poste vaincu compte pour les Epreuves du Dojo
                String progress = com.eldanior.system.quest.chapitres.ChapterQuests.onMartialDuelWon(me, req.postId());
                if (progress != null) message = message + " <color:gray>— " + progress + "</color>";
            } catch (Exception ignored) { }
            return end(caster, store, message);
        }

        private boolean end(Ref<EntityStore> caster, Store<EntityStore> store, String message) {
            MartialKi.notify(caster, store, message, NotificationStyle.Default);
            return true;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> f = fighter;
            MartialNpcs.release(f, cb);                                         // epees de Ki, lames, doubles du combattant
            if (f != null && f.isValid()) {
                Vector3d p = SpellFx.position(f, f.getStore());
                if (p != null) SpellFx.fx("Ki_Anneau_T6", new Vector3d(p).add(0, 0.05, 0), 1.5f, f.getStore(), cb);
                SpellFx.remove(f, cb);
            }
            PrivateVisibility.forget(req.sensei());                              // le Sensei reapparait
            BUSY.remove(req.sensei());
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(fighter);
            PrivateVisibility.forget(req.sensei());
            BUSY.remove(req.sensei());
        }
    }
}
