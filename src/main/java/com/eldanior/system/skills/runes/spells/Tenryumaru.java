package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tenryumaru, Lame du Premier Serment : relique du Clan Luo. Modele, animations, objets et interactions generes par
 * scripts/artefacts/gen_tenryumaru.py (+ tenryumaru_anims.py). Reservee au Patriarche des Luo (MasterySystem), qui la recoit
 * toujours (FamilyKeySystem).
 *
 * Deux objets pour un seul katana :
 *   - {@link #ITEM} : lame au fourreau, tenu dans la main gauche (jeu d'animations Tenryumaru_Fourreau) ;
 *   - {@link #ITEM_DRAWN} : lame au clair dans la main droite, fourreau toujours dans la gauche (Tenryumaru_Degainee).
 * Le premier coup (Tenryumaru_Degainer, Premier Serment ou signature) degaine puis appelle {@link Degaine}, qui remplace l'objet
 * tenu par l'objet degaine. {@link TenryumaruSystem} le rengaine (animation Rengainer, puis retour a l'objet au fourreau) apres
 * {@link TenryumaruSystem#IDLE_MS} sans attaquer, et rengaine aussitot un objet degaine qui n'est plus en main.
 *
 * Sorts :
 *   - FlashCeleste (maintien de l'attaque, recharge 8 s) : le porteur fond sur chaque ennemi a portee comme un eclair d'or (teleportations
 *     successives, trainee d'or) et le tranche au passage, revient a son point de depart, rengaine (noto) ; au claquement
 *     de la garde, chaque ennemi touche recoit dix slashs celestes au meme instant. Intouchable pendant tout le sort.
 *   - DixSerments (signature) : dix clones autour de la zone visee executent chacun le Flash Celeste ;
 *   - TenryuCoup* : une lame d'or a chaque coup d'epee (combo de base, degaine) ; chaque passage d'eclair en lance une aussi.
 */
public final class Tenryumaru {

    private Tenryumaru() {}

    public static final String ITEM = "Artefact_Sword_Tenryumaru";
    public static final String ITEM_DRAWN = "Artefact_Sword_Tenryumaru_Degainee";

    /** true pour les deux formes du katana (au fourreau, au clair). */
    public static boolean isItem(String id) {
        return ITEM.equals(id) || ITEM_DRAWN.equals(id);
    }

    /** Identifiant de reference (forme au fourreau) : la forme degainee compte comme la relique elle-meme. */
    public static String base(String id) {
        return ITEM_DRAWN.equals(id) ? ITEM : id;
    }

    private static final String FX_GOLD = "Gilgamesh_Sword_Burst", FX_SLASH = "Impact_Sword_Charged", FX_RING = "Eldanior_Pas_Celeste";
    private static final String SFX_SLASH = "SFX_Ability_Wind_Strike_Slash", SFX_HIT = "SFX_Sword_T2_Impact", SFX_BOOM = "SFX_Global_Weather_Thunder";

    // ===================== Changement de forme =====================

    /** Remplace, dans l'emplacement actif de la barre, la forme from par la forme to (durabilite et metadonnees gardees). */
    static boolean swapActive(Ref<EntityStore> ref, String from, String to, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        InventoryComponent hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (player == null || hotbar == null || player.getInventory() == null) return false;
        short slot = player.getInventory().getActiveHotbarSlot();
        if (slot < 0) return false;
        ItemContainer container = hotbar.getInventory();
        ItemStack stack = container.getItemStack(slot);
        if (stack == null || stack.isEmpty() || !from.equals(stack.getItemId())) return false;
        container.setItemStackForSlot(slot, swap(stack, to));
        hotbar.markDirty();
        TenryumaruSystem.syncSheath(ref, store);                                 // fourreau plein / vide au meme instant
        return true;
    }

    /** Clic droit bref, lame au clair : la lame retourne au fourreau (noto, puis objet au fourreau). */
    public static final class Rengaine implements RuneSpell {
        @Override public String id() { return "TenryumaruRengaine"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Player player = store.getComponent(caster, Player.getComponentType());
            ItemStack held = player == null || player.getInventory() == null ? null : player.getInventory().getActiveHotbarItem();
            if (held != null && LuoKatanas.isDrawn(held.getItemId())) TenryumaruSystem.sheatheNow(caster, store);
            return null;
        }
    }

    static ItemStack swap(ItemStack from, String id) {
        return new ItemStack(id, Math.max(1, from.getQuantity()), from.getDurability(), from.getMaxDurability(), from.getMetadata());
    }

    /** Fin du degaine (dernier pas des interactions de l'objet au fourreau) : la lame est desormais au clair. */
    public static final class Degaine implements RuneSpell {
        @Override public String id() { return "TenryumaruDegaine"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Player player = store.getComponent(caster, Player.getComponentType());
            ItemStack held = player == null || player.getInventory() == null ? null : player.getInventory().getActiveHotbarItem();
            LuoKatanas.Katana k = held == null ? null : LuoKatanas.of(held.getItemId());
            if (k != null && swapActive(caster, k.id(), k.drawn(), store)) TenryumaruSystem.drawn(caster);
            return null;
        }
    }

    // ===================== Lames d'or =====================

    /** Reglages d'une lame : modele (inclinaison), vitesse et portee (blocs), rayon de touche, degats, echelle, murs traverses,
     *  seconde coupe differee (0 = aucune). */
    private record Blade(String key, double speed, double range, double hitRadius, float damage, float scale,
                         boolean throughWalls, float lateCut, float energy) {
        String config() { return "Projectile_Config_Tenryumaru_Lame_" + key; }
        String model() { return "Tenryumaru_Lame_" + key; }
    }

    /** Lame d'or des coups de base, et de chaque passage d'eclair (joueur et clones). */
    private static final Blade COUP_H = new Blade("H", 36, 12, 1.5, 25f, 1.0f, false, 0, 1.5f);
    private static final Blade FLASH_BLADE = new Blade("H", 40, 10, 1.5, 20f, 0.9f, false, 0, 1.0f);
    /** Lame des clones : pas d'energie (la signature ne se recharge pas elle-meme). */
    private static final Blade CLONE_BLADE = new Blade("H", 40, 10, 1.5, 20f, 0.9f, false, 0, 0f);

    /** Energie de signature gagnee par le porteur (lue a l'appel : jamais en champ statique, cf. StatConfig). */
    static void energy(Ref<EntityStore> owner, float amount, Store<EntityStore> store) {
        if (amount <= 0 || owner == null || !owner.isValid()) return;
        var stats = store.getComponent(owner, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
        int id = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getSignatureEnergy();
        if (stats != null && stats.get(id) != null) stats.addStatValue(id, amount);
    }

    /**
     * Lame d'air de couleur lancee par un autre sort (voie martiale : Ki de l'Epee, Croissant de Ki). key = cle du modele
     * Tenryumaru_Lame_<key> (H/G/D/V or, Aonami_H bleu, Tsukikage_H violet, Shirasagi_H blanc, Hien_H rouge). La lame traverse
     * les ennemis, s'arrete sur un mur, et ses coups ne sont pas recopies par les sorts qui ecoutent les coups du lanceur.
     * L'instance renvoyee est a faire avancer par le sort appelant (tick puis cleanup), ou null.
     */
    static RuneSpell.Instance kiBlade(Ref<EntityStore> owner, Vector3d feet, Vector3d dir, String key, double speed, double range,
                                      double hitRadius, float damage, float scale, java.util.function.Consumer<Ref<EntityStore>> onHit,
                                      Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (owner == null || feet == null || dir == null) return null;
        Flight f = Flight.launchFrom(owner, feet, new Vector3d(dir).normalize(),
                new Blade(key, speed, range, hitRadius, damage, scale, false, 0, 0f), store, cb);
        if (f != null) { f.echo = true; f.hitHook = onHit; }
        return f;
    }

    /** Lame d'air d'or (un sort par inclinaison de coup). */
    public static final class AirBlade implements RuneSpell {
        private final String id;
        private final Blade blade;

        private AirBlade(String id, Blade blade) { this.id = id; this.blade = blade; }

        public static List<AirBlade> all() {
            return List.of(
                    // une lame d'or a chaque coup d'epee (combo normal, degaine)
                    new AirBlade("TenryuCoupHorizontal", COUP_H), new AirBlade("TenryuCoupGauche", new Blade("G", 36, 12, 1.5, 25f, 1.0f, false, 0, 1.5f)),
                    new AirBlade("TenryuCoupDroit", new Blade("D", 36, 12, 1.5, 25f, 1.0f, false, 0, 1.5f)),
                    new AirBlade("TenryuCoupVertical", new Blade("V", 36, 12, 1.5, 25f, 1.0f, false, 0, 1.5f)));
        }

        @Override public String id() { return id; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            return Flight.launch(caster, blade, store, cb);
        }
    }

    private static final class Flight implements RuneSpell.Instance {
        private static final double STEP = 0.5;
        private static final long MAX_MS = 3_000, CUT_MS = 400, TRAIL_MS = 60;

        final Blade blade;
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        final List<Object[]> cuts = new ArrayList<>();             // {cible, instant de la seconde coupe}
        long last, lastTrail;
        double travelled;
        boolean scaled, flying = true;
        boolean returns, returned;                                    // Ressac (Aonami) : la lame revient une fois
        boolean echo;                                                 // lame de Ki : ses coups ne sont pas recopies (markEcho)
        java.util.function.Consumer<Ref<EntityStore>> hitHook;        // lame de Ki : le sort lanceur est prevenu de chaque touche

        private Flight(Blade blade, Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.blade = blade; this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        static Flight launch(Ref<EntityStore> caster, Blade blade, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            return launchFrom(caster, at, SpellFx.lookDirection(caster, store).normalize(), blade, store, cb);
        }

        /** Lame lancee depuis les pieds d'un acteur (joueur ou clone) dans la direction dir ; degats attribues a owner. */
        static Flight launchFrom(Ref<EntityStore> owner, Vector3d feet, Vector3d dir, Blade blade, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d from = new Vector3d(feet).add(0, 1.3, 0).add(new Vector3d(dir).mul(1.2));
            Ref<EntityStore> caster = owner;
            Ref<EntityStore> model = SpellFx.spawnModel(caster, blade.config(), from, dir, cb);
            SpellFx.notSaved(model, store, cb);
            SpellFx.sound(SFX_SLASH, from, cb);
            SpellFx.fx(FX_GOLD, from, 0.5f * blade.scale(), store, cb);                // eclat d'or au depart de la lame
            return new Flight(blade, from, new Vector3d(dir), model, System.currentTimeMillis());
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (flying) fly(caster, now, store, cb);
            for (Iterator<Object[]> it = cuts.iterator(); it.hasNext(); ) {
                Object[] c = it.next();
                if (now < (long) c[1]) continue;
                it.remove();
                @SuppressWarnings("unchecked") Ref<EntityStore> target = (Ref<EntityStore>) c[0];
                Vector3d p = target.isValid() ? SpellFx.position(target, store) : null;
                if (p == null || caster == null || !caster.isValid()) continue;
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                SpellFx.fx(FX_SLASH, chest, 1.4f, store, cb);
                SpellFx.fx(FX_GOLD, chest, 1.0f, store, cb);
                SpellFx.sound(SFX_HIT, chest, cb);
                SpellFx.damage(caster, target, "Physical", blade.lateCut(), new Vector3d(0, 3.0, 0), "Impact_Strong", cb);
            }
            return !flying && cuts.isEmpty();
        }

        private void fly(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (travelled >= blade.range() && returns && !returned) {   // demi-tour : elle peut toucher une seconde fois
                returned = true;
                travelled = 0;
                dir.negate();
                hit.clear();
            }
            if (now - startAt >= MAX_MS || travelled >= blade.range()) { stop(cb); return; }
            if (!scaled && model != null && model.isValid()) {                    // le porteur n'existe qu'au tick suivant sa creation
                if (blade.scale() != 1f) SpellFx.setModelScale(model, blade.model(), blade.scale(), 1.2 * blade.scale(), cb);
                scaled = true;
            }
            World world = SpellFx.world(store);
            double move = blade.speed() * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                if (!blade.throughWalls()) {
                    int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                    if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                        SpellFx.fx(FX_GOLD, pos, 0.8f * blade.scale(), store, cb);
                        stop(cb);                                                 // un mur arrete la lame
                        return;
                    }
                }
                if (caster == null || !caster.isValid()) continue;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, blade.hitRadius() + 1, store)) {
                    if (hit.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || new Vector3d(p).add(0, 1, 0).distance(pos) > blade.hitRadius()) continue;
                    hit.add(e);
                    if (hitHook != null) hitHook.accept(e);
                    if (echo) RuneSpellRuntime.markEcho(e, blade.damage());
                    SpellFx.damage(caster, e, "Physical", blade.damage(), new Vector3d(dir).mul(4).add(0, 2, 0), "Impact_Light", cb);
                    energy(caster, blade.energy(), store);
                    SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1, 0), 0.9f, store, cb);
                    SpellFx.sound(SFX_HIT, p, cb);
                    if (blade.lateCut() > 0) cuts.add(new Object[]{e, now + CUT_MS});
                }
            }
            travelled += move;
            if (blade.throughWalls() && now - lastTrail >= TRAIL_MS) {               // sillage d'or du slash divin
                SpellFx.fx(FX_GOLD, pos, 0.9f, store, cb);
                lastTrail = now;
            }
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
        }

        private void stop(CommandBuffer<EntityStore> cb) {
            flying = false;
            SpellFx.remove(model, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(model, cb);
            cuts.clear();
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(model);
        }
    }

    // ===================== Eclair d'or : parcours commun (Flash Celeste et clones des Dix Serments) =====================

    /** Un parcours d'eclair : l'acteur fond sur chaque cible tour a tour (teleportation, trainee d'or, coup, lame d'or), puis
     *  revient a son point de depart. Les degats sont attribues au proprietaire (le joueur). */
    private static final class Route {
        static final double STOP_BEFORE = 1.4, TRAIL_STEP = 1.1;
        static final long STEP_MS = 170;
        private static final String[] SWINGS = {"SwingLeft", "SwingRight", "SwingDown"};

        final Ref<EntityStore> actor;
        final Vector3d home, look;
        final List<Ref<EntityStore>> targets, hit = new ArrayList<>();
        final float arriveDamage, arriveEnergy;
        final Blade blade;
        final String restAnim;                                            // pose prise au retour (clones : posture iai), ou null
        int index;
        long nextAt;
        boolean done;

        Route(Ref<EntityStore> actor, Vector3d home, Vector3d look, List<Ref<EntityStore>> targets, float arriveDamage,
              float arriveEnergy, Blade blade, String restAnim, long startAt) {
            this.actor = actor; this.home = home; this.look = look; this.targets = targets; this.arriveDamage = arriveDamage;
            this.arriveEnergy = arriveEnergy; this.blade = blade; this.restAnim = restAnim; this.nextAt = startAt;
        }

        /** Avance d'un pas si c'est l'heure ; les lames d'or lancees sont ajoutees a blades. */
        void step(Ref<EntityStore> owner, long now, List<Flight> blades, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (done || now < nextAt || actor == null || !actor.isValid()) { if (actor == null || !actor.isValid()) done = true; return; }
            Vector3d from = SpellFx.position(actor, store);
            if (from == null) { done = true; return; }
            while (index < targets.size()) {
                Ref<EntityStore> t = targets.get(index++);
                Vector3d p = t.isValid() ? SpellFx.position(t, store) : null;
                if (p == null) continue;
                Vector3d dir = new Vector3d(p).sub(from);
                dir.y = 0;
                if (dir.lengthSquared() < 0.01) dir.set(look);
                dir.normalize();
                Vector3d dest = new Vector3d(p).sub(new Vector3d(dir).mul(STOP_BEFORE));
                trail(from, dest, store, cb);
                SpellFx.teleport(actor, dest, SpellFx.facing(dir, false), store, cb);
                CloneFx.leash(actor, dest, store);
                AnimationUtils.playAnimation(actor, AnimationSlot.Action, "Tenryumaru_Degainee", SWINGS[hit.size() % SWINGS.length], true, store);
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                SpellFx.fx(FX_SLASH, chest, (float) Math.toDegrees(Math.atan2(dir.x, dir.z)), 0f, 1.2f, store, cb);
                SpellFx.fx(FX_RING, new Vector3d(dest).add(0, 0.1, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_SLASH, chest, cb);
                if (owner != null && owner.isValid()) {
                    SpellFx.damage(owner, t, "Physical", arriveDamage, new Vector3d(dir).add(0, 1.0, 0), "Impact_Light", cb);
                    energy(owner, arriveEnergy, store);
                }
                Flight blade = this.blade == null ? null : Flight.launchFrom(owner, dest, dir, this.blade, store, cb);   // une lame d'or
                if (blade != null) blades.add(blade);
                hit.add(t);
                nextAt = now + STEP_MS;
                return;
            }
            trail(from, home, store, cb);                                        // retour au point de depart
            SpellFx.teleport(actor, home, SpellFx.facing(look, false), store, cb);
            CloneFx.leash(actor, home, store);
            SpellFx.fx(FX_RING, new Vector3d(home).add(0, 0.1, 0), 1.6f, store, cb);
            if (restAnim != null) AnimationUtils.playAnimation(actor, AnimationSlot.Action, "Tenryumaru_Degainee", restAnim, true, store);
            done = true;
        }

        /** Eclair d'or : trainee d'eclats entre deux points. */
        static void trail(Vector3d a, Vector3d b, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double d = a.distance(b);
            int n = (int) Math.min(14, Math.max(1, d / TRAIL_STEP));
            for (int i = 0; i <= n; i++) {
                Vector3d q = new Vector3d(a).lerp(b, i / (double) n).add(0, 1.0, 0);
                SpellFx.fx(FX_GOLD, q, 0.45f, store, cb);
            }
        }

        /** Ordre glouton : toujours l'ennemi le plus proche de la position courante. */
        static List<Ref<EntityStore>> order(Vector3d start, List<Ref<EntityStore>> pool, int max, Store<EntityStore> store) {
            List<Ref<EntityStore>> left = new ArrayList<>(pool), out = new ArrayList<>();
            Vector3d at = new Vector3d(start);
            while (!left.isEmpty() && out.size() < max) {
                Ref<EntityStore> best = null;
                double bestD = Double.MAX_VALUE;
                for (Ref<EntityStore> e : left) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    double d = p.distanceSquared(at);
                    if (d < bestD) { bestD = d; best = e; }
                }
                if (best == null) break;
                left.remove(best);
                out.add(best);
                at = SpellFx.position(best, store);
            }
            return out;
        }
    }

    /** Slashs celestes au meme instant : sur chaque cible, hits coups de perHit, autant d'eclats de lame, gerbe d'or,
     *  effet optionnel (givre, embrasement...). */
    private static void celestialBurst(Ref<EntityStore> owner, Map<Ref<EntityStore>, Float> perHit, Vector3d center,
                                       Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        celestialBurst(owner, perHit, center, 10, null, store, cb);
    }

    private static void celestialBurst(Ref<EntityStore> owner, Map<Ref<EntityStore>, Float> perHit, Vector3d center, int hits,
                                       String effect, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        SpellFx.sound2dNear(SFX_BOOM, center, 32, store);
        SpellFx.shake(center, 16, "Impact_Strong", store);
        for (Map.Entry<Ref<EntityStore>, Float> en : perHit.entrySet()) {
            Ref<EntityStore> t = en.getKey();
            Vector3d p = t.isValid() ? SpellFx.position(t, store) : null;
            if (p == null || owner == null || !owner.isValid()) continue;
            Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
            for (int i = 0; i < hits; i++) {
                Vector3d at = new Vector3d(chest).add(rnd.nextDouble(-0.4, 0.4), rnd.nextDouble(-0.5, 0.6), rnd.nextDouble(-0.4, 0.4));
                SpellFx.fx(FX_SLASH, at, rnd.nextFloat() * 360f, rnd.nextFloat() * 160f - 80f, 1.3f, store, cb);
                boolean last = i == hits - 1;
                SpellFx.damage(owner, t, "Physical", en.getValue(), last ? new Vector3d(0, 4.0, 0) : null, last ? "Impact_Strong" : null, cb);
            }
            if (effect != null) SpellFx.effect(t, effect, cb);
            SpellFx.fx(FX_GOLD, chest, 1.6f, store, cb);
            SpellFx.fx(FX_RING, new Vector3d(p).add(0, 0.1, 0), 1.8f, store, cb);
            SpellFx.sound(SFX_HIT, chest, cb);
        }
    }

    private static void tickBlades(List<Flight> blades, Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Iterator<Flight> it = blades.iterator(); it.hasNext(); ) {
            Flight f = it.next();
            if (f.tick(owner, now, store, cb)) { f.cleanup(cb); it.remove(); }
        }
    }

    // ===================== Maintien : Flash Celeste =====================

    public static final class FlashCeleste implements RuneSpell {
        /** Recharge du Flash Celeste (attaque maintenue) : sans elle, le porteur serait intouchable en continu. */
        static final long COOLDOWN_MS = 8_000;
        static final double RADIUS = 14.0;
        static final int MAX_TARGETS = 10;
        static final float ARRIVE_DAMAGE = 35f, CELESTIAL_DAMAGE = 22f;
        private static final Map<Ref<EntityStore>, Long> readyAt = new ConcurrentHashMap<>();

        @Override public String id() { return "FlashCeleste"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!LuoPassives.heldAllowed(caster, store)) return null;            // endurance du coup maintenu
            Vector3d origin = SpellFx.position(caster, store);
            if (origin == null) return null;
            long now = System.currentTimeMillis();
            Long ready = readyAt.get(caster);
            if (ready != null && now < ready) {
                com.hypixel.hytale.server.core.universe.PlayerRef pr = store.getComponent(caster, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
                if (pr != null) com.eldanior.system.Leveling.utils.NotificationHelper.sendNotification(pr,
                        "<color:gold>Flash Céleste : encore " + ((ready - now) / 1000 + 1) + " s.</color>",
                        com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Warning);
                return null;
            }
            readyAt.put(caster, now + COOLDOWN_MS);
            Vector3d look = SpellFx.forward(caster, store);
            List<Ref<EntityStore>> pool = SpellFx.enemiesAround(caster, new Vector3d(origin).add(0, 1, 0), RADIUS, store);
            SpellFx.protectFromFall(caster, 6_000, store);
            SpellFx.fx(FX_RING, new Vector3d(origin).add(0, 0.1, 0), 2.2f, store, cb);
            SpellFx.fx(FX_GOLD, new Vector3d(origin).add(0, 1.1, 0), 1.2f, store, cb);
            TenryumaruSystem.drawn(caster);
            return new Flash(new Route(caster, origin, look, Route.order(origin, pool, MAX_TARGETS, store), ARRIVE_DAMAGE, 2f,
                    FLASH_BLADE, null, now), LuoKatanas.of(ITEM));
        }
    }

    private static final class Flash implements RuneSpell.Instance {
        static final long RETURN_PAUSE_MS = 160, NOTO_MS = 440, MAX_MS = 5_000;

        enum Phase { DASH, NOTO, BURST, BLADES }

        final Route route;
        final LuoKatanas.Katana katana;
        final List<Flight> blades = new ArrayList<>();
        final long startAt = System.currentTimeMillis();
        Phase phase = Phase.DASH;
        long nextAt;

        Flash(Route route, LuoKatanas.Katana katana) { this.route = route; this.katana = katana; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            tickBlades(blades, caster, now, store, cb);
            if (caster == null || !caster.isValid()) return true;
            if (phase == Phase.DASH || phase == Phase.NOTO) TenryumaruSystem.drawn(caster);  // pas de rengainage automatique
            switch (phase) {
                case DASH -> {
                    route.step(caster, now, blades, store, cb);
                    if (route.done || now - startAt > MAX_MS) { nextAt = now + RETURN_PAUSE_MS; phase = Phase.NOTO; }
                }
                case NOTO -> {
                    if (now < nextAt) return false;
                    AnimationUtils.playAnimation(caster, AnimationSlot.Action, "Tenryumaru_Degainee", "Rengainer", true, store);
                    nextAt = now + NOTO_MS;
                    phase = Phase.BURST;
                }
                case BURST -> {
                    if (now < nextAt) return false;
                    Map<Ref<EntityStore>, Float> per = new java.util.HashMap<>();
                    for (Ref<EntityStore> t : route.hit) per.put(t, katana.burstDamage());
                    celestialBurst(caster, per, route.home, katana.burstHits(), katana.burstEffect(), store, cb);
                    swapActive(caster, katana.drawn(), katana.id(), store);
                    state.remove(caster);
                    phase = Phase.BLADES;
                }
                case BLADES -> { return blades.isEmpty(); }
            }
            return false;
        }

        /** Intouchable pendant l'eclair et le rengainage. */
        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return phase == Phase.BLADES ? amount : 0f;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { blades.forEach(f -> f.cleanup(cb)); blades.clear(); }

        @Override
        public void abort() { blades.forEach(Flight::abort); }
    }

    // ===================== Katanas ordinaires du Clan Luo =====================

    /** Katana Luo tenu (forme au fourreau ou au clair), sinon null. */
    private static LuoKatanas.Katana heldKatana(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        ItemStack held = player == null || player.getInventory() == null ? null : player.getInventory().getActiveHotbarItem();
        return held == null ? null : LuoKatanas.of(held.getItemId());
    }

    /** Attaque maintenue d'un katana Luo (posture iai puis degaine) : coupe large devant le porteur, et a partir de Rare une
     *  lame d'air de la couleur du katana. */
    public static final class LuoIai implements RuneSpell {
        static final double REACH = 2.2, RADIUS = 2.6;

        @Override public String id() { return "LuoIai"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!LuoPassives.heldAllowed(caster, store)) return null;            // endurance du coup maintenu
            LuoKatanas.Katana k = heldKatana(caster, store);
            Vector3d pos = SpellFx.position(caster, store);
            if (k == null || pos == null) return null;
            if (Kinryu.is(k)) return Kinryu.souffle(caster, store, cb);                // Souffle du Dragon d'Or
            Vector3d fwd = SpellFx.forward(caster, store);
            if (LuoPassives.TSUKIKAGE.equals(k.id()) && eclipse(caster, k, pos, fwd, store, cb)) return null;
            if (LuoPassives.HIEN.equals(k.id())) return new Hirondelle(caster, k, pos, fwd);
            Vector3d center = new Vector3d(pos).add(0, 1.0, 0).add(new Vector3d(fwd).mul(REACH));
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                SpellFx.damage(caster, e, "Physical", k.iaiDamage(), new Vector3d(fwd).mul(4).add(0, 1.5, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), (float) Math.toDegrees(Math.atan2(fwd.x, fwd.z)), 0f, 1.1f, store, cb);
                energy(caster, 3f, store);
            }
            SpellFx.sound(SFX_SLASH, center, cb);
            if (k.iaiBlade() == null) return null;
            Blade blade = new Blade(k.iaiBlade(), 38, 16, 1.8, k.iaiBladeDamage(), 1.4f, false, 0, 1.5f);
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            List<Flight> volley = new ArrayList<>();
            if (LuoPassives.SHIRASAGI.equals(k.id())) {                       // Neige Silencieuse : eventail de trois lames
                for (double yaw : new double[]{-20, 0, 20}) {
                    Vector3d d = new Vector3d(look).rotateY(Math.toRadians(yaw));
                    Flight f = Flight.launchFrom(caster, pos, d, blade, store, cb);
                    if (f != null) volley.add(f);
                }
            } else {
                Flight f = Flight.launchFrom(caster, pos, look, blade, store, cb);
                if (f != null) {
                    f.returns = LuoPassives.AONAMI.equals(k.id());              // Ressac : la vague revient
                    volley.add(f);
                }
            }
            return volley.isEmpty() ? null : new Volley(volley);
        }

        /** Eclipse (Tsukikage) : invisible, reapparait derriere l'ennemi le plus proche devant soi et le tranche. */
        private static boolean eclipse(Ref<EntityStore> caster, LuoKatanas.Katana k, Vector3d pos, Vector3d fwd,
                                       Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> best = null;
            double bestD = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(0, 1, 0), 9.0, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d to = new Vector3d(p).sub(pos);
                to.y = 0;
                if (to.lengthSquared() < 1e-4 || to.normalize().dot(new Vector3d(fwd.x, 0, fwd.z).normalize()) < 0.3) continue;
                double d = p.distanceSquared(pos);
                if (d < bestD) { bestD = d; best = e; }
            }
            if (best == null) return false;
            Vector3d p = SpellFx.position(best, store);
            Vector3d dir = new Vector3d(p).sub(pos);
            dir.y = 0;
            dir.normalize();
            Vector3d behind = new Vector3d(p).add(new Vector3d(dir).mul(1.3));
            SpellFx.effect(caster, "Luo_Ombre_Lune", cb);
            SpellFx.fx("Nyx_Void", new Vector3d(pos).add(0, 1, 0), 0.9f, store, cb);
            SpellFx.teleport(caster, behind, SpellFx.facing(new Vector3d(dir).negate(), false), store, cb);
            SpellFx.fx("Nyx_Void", new Vector3d(behind).add(0, 1, 0), 0.9f, store, cb);
            SpellFx.damage(caster, best, "Physical", k.iaiDamage() * 1.5f, new Vector3d(dir).mul(-4).add(0, 1.5, 0), "Impact_Strong", cb);
            SpellFx.effect(best, "Arme_Givre", cb);
            SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_SLASH, p, cb);
            energy(caster, 3f, store);
            return true;
        }
    }

    /** Lames d'air lancees ensemble (eventail de Shirasagi, Ressac d'Aonami). */
    private static final class Volley implements RuneSpell.Instance {
        final List<Flight> blades;

        Volley(List<Flight> blades) { this.blades = blades; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            tickBlades(blades, caster, now, store, cb);
            return blades.isEmpty();
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { blades.forEach(f -> f.cleanup(cb)); blades.clear(); }

        @Override public void abort() { blades.forEach(Flight::abort); }
    }

    /** Vol de l'hirondelle (Hien) : ruee de RANGE blocs qui traverse les ennemis (degats, embrasement), trainee de feu ;
     *  le porteur est intouchable pendant la ruee. */
    private static final class Hirondelle implements RuneSpell.Instance {
        static final double RANGE = 9.0, SPEED = 28.0, HIT_RADIUS = 2.0;
        static final long MAX_MS = 450, TRAIL_MS = 50;

        final LuoKatanas.Katana katana;
        final Vector3d origin, dir;
        final long startAt = System.currentTimeMillis();
        final java.util.Set<Ref<EntityStore>> hit = new HashSet<>();
        long lastTrail;
        boolean done;

        Hirondelle(Ref<EntityStore> caster, LuoKatanas.Katana katana, Vector3d origin, Vector3d fwd) {
            this.katana = katana; this.origin = origin;
            this.dir = new Vector3d(fwd.x, 0, fwd.z);
            if (dir.lengthSquared() < 1e-4) dir.set(0, 0, 1);
            dir.normalize();
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = caster.isValid() ? SpellFx.position(caster, store) : null;
            if (pos == null || now - startAt > MAX_MS || SpellFx.horizontalDistance(pos, origin) >= RANGE) {
                if (caster.isValid()) SpellFx.push(caster, new Vector3d(dir).mul(1.0), false, cb);
                return true;
            }
            SpellFx.push(caster, new Vector3d(dir).mul(SPEED), false, cb);
            if (now - lastTrail >= TRAIL_MS) {
                SpellFx.fx("Impact_Fire", new Vector3d(pos).add(0, 0.6, 0), 0.7f, store, cb);
                lastTrail = now;
            }
            Vector3d center = new Vector3d(pos).add(0, 1, 0);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, HIT_RADIUS + 1, store)) {
                if (hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || new Vector3d(p).add(0, 1, 0).distance(center) > HIT_RADIUS) continue;
                hit.add(e);
                SpellFx.damage(caster, e, "Physical", katana.iaiDamage(), new Vector3d(dir).mul(3).add(0, 2, 0), "Impact_Strong", cb);
                SpellFx.effect(e, "Arme_Embrasement", cb);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 1.0f, store, cb);
                energy(caster, 3f, store);
            }
            return false;
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) { return 0f; }
    }

    /** Signature d'un katana Luo, Iaigiri : un Flash Celeste a l'echelle du katana (cibles, portee, slashs). */
    public static final class LuoIaigiri implements RuneSpell {
        /** 2026-11-11 : tous les katanas hors famille prennent la Fente du Ciel du Bokken (specialites plus tard). */
        static final boolean FENTE_FOR_ALL = true;

        @Override public String id() { return "LuoIaigiri"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            LuoKatanas.Katana k = heldKatana(caster, store);
            Vector3d origin = SpellFx.position(caster, store);
            if (k == null || origin == null) return null;
            if (Kinryu.is(k)) return Kinryu.eclipse(caster, store, cb);                // Eclipse du Dieu-Dragon
            // Fente du Ciel (8 versions) : le Bokken, puis tous les katanas hors famille (2026-11-11) ; Kinryu-O garde l'Eclipse
            if (FENTE_FOR_ALL) return BokkenSignature.start(caster, store, cb);
            // ancien Iaigiri des katanas (eclair sur plusieurs cibles), garde pour les specialites a venir
            long now = System.currentTimeMillis();
            List<Ref<EntityStore>> pool = SpellFx.enemiesAround(caster, new Vector3d(origin).add(0, 1, 0), k.iaigiriRadius(), store);
            Blade blade = k.iaiBlade() == null ? null : new Blade(k.iaiBlade(), 40, 10, 1.5, k.iaiBladeDamage() * 0.5f, 0.9f, false, 0, 0f);
            SpellFx.protectFromFall(caster, 5_000, store);
            SpellFx.fx(FX_RING, new Vector3d(origin).add(0, 0.1, 0), 1.6f, store, cb);
            TenryumaruSystem.drawn(caster);
            return new Flash(new Route(caster, origin, SpellFx.forward(caster, store), Route.order(origin, pool, k.iaigiriTargets(), store),
                    k.arrive(), 0f, blade, null, now), k);
        }
    }

    // ===================== Signature : les Dix Serments =====================

    /** Dix clones du porteur apparaissent en cercle autour de la zone visee ; chacun execute le Flash Celeste sur les ennemis
     *  de la zone (apparition en cercle, 1,5 s d'attente, departs en quinconce), revient a sa place et rengaine avec les autres ; au claquement des dix gardes, chaque ennemi touche recoit
     *  dix slashs celestes, d'autant plus forts que de clones l'ont frappe. Le porteur, en posture iai, est intouchable. */
    public static final class DixSerments implements RuneSpell {
        static final int CLONES = 10, MAX_PER_CLONE = 4;
        static final double RANGE = 40.0, FALLBACK = 12.0, RING = 7.0, ZONE = 12.0;
        static final float ARRIVE_DAMAGE = 20f, CELESTIAL_PER_CLONE = 8f;
        private static final String ROLE = "Eldanior_Doppel";

        @Override public String id() { return "DixSerments"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            World world = SpellFx.world(store);
            if (pos == null || world == null) return null;
            long now = System.currentTimeMillis();
            Vector3d center = SpellFx.targetPoint(caster, store, RANGE, FALLBACK);
            CloneFx.Look look = CloneFx.Look.of(caster, store);
            ItemStack blade = new ItemStack(ITEM_DRAWN, 1);
            Serments s = new Serments(caster, center, now);
            for (int i = 0; i < CLONES; i++) {
                double a = i * 2 * Math.PI / CLONES;
                Vector3d at = SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * RING, 0, Math.sin(a) * RING));
                Vector3d face = new Vector3d(center).sub(at);
                face.y = 0;
                if (face.lengthSquared() < 1e-4) face.set(0, 0, 1);
                face.normalize();
                s.homes.add(at);
                s.faces.add(face);
                s.npcs.add(CloneFx.spawn(world, ROLE, caster, at, SpellFx.facing(face, false), look, blade, 0f, null, true, now));
                SpellFx.fx(FX_GOLD, new Vector3d(at).add(0, 1.0, 0), 1.0f, store, cb);
                SpellFx.fx(FX_RING, new Vector3d(at).add(0, 0.1, 0), 1.4f, store, cb);
            }
            SpellFx.fx(FX_RING, new Vector3d(center).add(0, 0.1, 0), 4.0f, store, cb);
            SpellFx.sound2dNear(SFX_BOOM, center, 40, store);
            AnimationUtils.playAnimation(caster, AnimationSlot.Action, animSet(caster, store), "IaiCharging", true, store);
            return s;
        }
    }

    /** Jeu d'animations de la forme tenue (au fourreau ou au clair). */
    private static String animSet(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        ItemStack held = player == null || player.getInventory() == null ? null : player.getInventory().getActiveHotbarItem();
        return held != null && ITEM_DRAWN.equals(held.getItemId()) ? "Tenryumaru_Degainee" : "Tenryumaru_Fourreau";
    }

    private static final class Serments implements RuneSpell.Instance {
        static final long SPAWN_WAIT_MS = 1_500, GATHER_MS = 1_500, QUINCONCE_MS = 350, NOTO_MS = 440, MAX_DASH_MS = 9_000;
        /** Ordre de depart en quinconce autour du cercle : chaque clone part apres le precedent, en croisant le cercle. */
        static final int[] ORDER = {0, 5, 2, 7, 4, 9, 6, 1, 8, 3};

        enum Phase { SPAWN, GATHER, DASH, NOTO, BLADES }

        final Ref<EntityStore> caster;
        final Vector3d center;
        final long startAt;
        final List<CloneFx.Npc> npcs = new ArrayList<>();
        final List<Vector3d> homes = new ArrayList<>(), faces = new ArrayList<>();
        final List<Route> routes = new ArrayList<>();
        final List<Flight> blades = new ArrayList<>();
        Phase phase = Phase.SPAWN;
        long nextAt, dashStart;

        Serments(Ref<EntityStore> caster, Vector3d center, long startAt) { this.caster = caster; this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            tickBlades(blades, caster, now, store, cb);
            if (caster == null || !caster.isValid()) return true;
            switch (phase) {
                case SPAWN -> {                                                  // les dix apparaissent en cercle, posture iai
                    boolean all = npcs.stream().allMatch(n -> n.ref != null || n.failed);
                    if (!all && now - startAt < SPAWN_WAIT_MS) return false;
                    for (int i = 0; i < npcs.size(); i++) {
                        Ref<EntityStore> ref = npcs.get(i).ref;
                        if (ref == null || !ref.isValid()) continue;
                        SpellFx.teleport(ref, homes.get(i), SpellFx.facing(faces.get(i), false), store, cb);
                        CloneFx.leash(ref, homes.get(i), store);
                        AnimationUtils.playAnimation(ref, AnimationSlot.Action, "Tenryumaru_Degainee", "IaiCharging", true, store);
                    }
                    nextAt = now + GATHER_MS;
                    phase = Phase.GATHER;
                }
                case GATHER -> {                                                 // 1,5 s d'attente, puis l'attaque
                    if (now < nextAt) return false;
                    planRoutes(now, store);
                    dashStart = now;
                    phase = Phase.DASH;
                }
                case DASH -> {
                    boolean done = true;
                    for (Route r : routes) { r.step(caster, now, blades, store, cb); done &= r.done; }
                    if (done || now - dashStart > MAX_DASH_MS) {
                        for (Route r : routes)                                   // revenus en cercle, les dix rengainent ensemble
                            if (r.actor.isValid()) AnimationUtils.playAnimation(r.actor, AnimationSlot.Action, "Tenryumaru_Degainee", "Rengainer", true, store);
                        nextAt = now + NOTO_MS;
                        phase = Phase.NOTO;
                    }
                }
                case NOTO -> {
                    if (now < nextAt) return false;
                    Map<Ref<EntityStore>, Float> per = new java.util.HashMap<>();
                    for (Route r : routes) for (Ref<EntityStore> t : r.hit) per.merge(t, DixSerments.CELESTIAL_PER_CLONE, Float::sum);
                    celestialBurst(caster, per, center, store, cb);
                    AnimationUtils.stopAnimation(caster, AnimationSlot.Action, store);
                    dissolve(store, cb);
                    phase = Phase.BLADES;
                }
                default -> { return blades.isEmpty(); }
            }
            return false;
        }

        /** Parcours des clones, departs en quinconce ; chaque clone prefere les ennemis que personne n'a encore choisis. */
        private void planRoutes(long now, Store<EntityStore> store) {
            List<Ref<EntityStore>> pool = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(center).add(0, 1, 0), DixSerments.ZONE, store))
                if (!RuneSpellRuntime.isSummon(e)) pool.add(e);
            Map<Ref<EntityStore>, Integer> load = new java.util.HashMap<>();
            int k = 0;
            for (int i : ORDER) {
                if (i >= npcs.size()) continue;
                Ref<EntityStore> ref = npcs.get(i).ref;
                if (ref == null || !ref.isValid()) continue;
                List<Ref<EntityStore>> path = new ArrayList<>();
                Vector3d at = new Vector3d(homes.get(i));
                while (path.size() < DixSerments.MAX_PER_CLONE) {
                    Ref<EntityStore> best = null;
                    double bestScore = Double.MAX_VALUE;
                    for (Ref<EntityStore> e : pool) {
                        if (path.contains(e)) continue;
                        Vector3d p = SpellFx.position(e, store);
                        if (p == null) continue;
                        double score = load.getOrDefault(e, 0) * 1_000 + p.distanceSquared(at);
                        if (score < bestScore) { bestScore = score; best = e; }
                    }
                    if (best == null) break;
                    path.add(best);
                    load.merge(best, 1, Integer::sum);
                    at = SpellFx.position(best, store);
                }
                routes.add(new Route(ref, homes.get(i), faces.get(i), path, DixSerments.ARRIVE_DAMAGE, 0f, CLONE_BLADE, "IaiCharging",
                        now + (long) k * QUINCONCE_MS));
                k++;
            }
        }

        /** Les clones se dissipent en lumiere d'or. */
        private void dissolve(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (CloneFx.Npc n : npcs) {
                Ref<EntityStore> ref = n.ref;
                if (ref == null) continue;
                Vector3d p = ref.isValid() ? SpellFx.position(ref, store) : null;
                if (p != null) SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1.0, 0), 1.2f, store, cb);
                SpellFx.remove(ref, cb);
                RuneSpellRuntime.forgetSummon(ref);
                n.ref = null;
            }
        }

        /** Le porteur, en posture iai au centre de la manoeuvre, est intouchable jusqu'au claquement des gardes. */
        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return phase == Phase.BLADES ? amount : 0f;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            blades.forEach(f -> f.cleanup(cb));
            blades.clear();
            for (CloneFx.Npc n : npcs) {
                SpellFx.remove(n.ref, cb);
                RuneSpellRuntime.forgetSummon(n.ref);
            }
        }

        @Override
        public void abort() {
            blades.forEach(Flight::abort);
            for (CloneFx.Npc n : npcs) { RuneSpellRuntime.removeLater(n.ref); RuneSpellRuntime.forgetSummon(n.ref); }
        }
    }

    // ===================== Etat par joueur (rengainage) =====================

    static final Map<Ref<EntityStore>, long[]> state = new ConcurrentHashMap<>();   // {derniere action, instant du rengainage}
}
