package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Armes a feu (2026-11-15, v2) : le mousquet (Weapon_Mousquet, facon Brown Bess), les trois mousquets legendaires et les
 * armes de leurs transformations. Modeles, munitions, items et interactions JSON : scripts/artefacts/gen_mousquets.py.
 *
 * Mousquets (tir : Eldanior_MousquetTir -> pullTrigger ; 1 Poudre a canon + 1 Balle de fer par coup) :
 *  - FORCE : degats de la balle, 70 a 0 point, 2 000 a 3 000 points (lineaire) ; vitesse de la balle (avec l'endurance) ;
 *  - AGILITE : rechargement 50 s / (1 + 49 x AGI / 3 000) : 1 s a 3 000 points (minimum) ;
 *  - ENDURANCE : portee sure 40 blocs + 760 x END / 3 000 (800 blocs a 3 000), precision au-dela 50 % -> 95 %,
 *    grossissement de la visee (paliers JSON, Eldanior_SeuilEndurance) et distance de vue des entites pendant la visee ;
 *  - Concentration (signature, energie pleine) : une balle a precision et degats doubles (pas de rechargement requis) ;
 *  - une balle qui tue annonce la distance du tir ; une balle au but charge la signature.
 * Legendaires : Black Bess (precision : portee et lunette x1,5), Green Colt (rapidite : rechargement /2), Blue Tiger
 * (force : degats x1,5, la balle traverse 2 ennemis). Leur signature est une transformation de 5 min (ArmeTransfo) :
 *  - SARS (Black Bess) : fusil de precision, chargeur de 5 Balles de sniper divines, portee 2 000 blocs ;
 *  - Colts de cavalerie akimbo (Green Colt) : AiglesDuDesert, chargeurs de 5 + 5 Balles Divines (coltTrigger) ;
 *  - Canon de char a main (Blue Tiger) : 1 Obus Divin par chargement, rechargement 5 s, 100 blocs, degats x5, traverse
 *    les ennemis en ligne droite et explose (cratere restaure apres une minute).
 */
public final class Mousquet {

    private Mousquet() {}

    public static final String ITEM = "Weapon_Mousquet", BAYONET_SUFFIX = "_Baionnette";
    public static final String POWDER = "Munition_Poudre_Canon", BALL = "Munition_Balle_Fer";
    public static final String SARS = "Artefact_Sniper_SARS", CANON = "Artefact_Canon_Main";
    public static final String SNIPER_AMMO = "Munition_Balle_Sniper_Divine", SHELL = "Munition_Obus_Divin", COLT_AMMO = "Munition_Balle_Divine";

    /** Mousquets : bonus propres (degats, rechargement, portee/precision, lunette, ennemis traverses) et transformation. */
    public enum Arme {
        MOUSQUET(ITEM, "Mousquet", 1f, 1f, 1f, 1f, 0, null),
        BLACK_BESS("Artefact_Mousquet_BlackBess", "Black Bess", 1.15f, 1f, 1.5f, 1.5f, 0, SARS),
        GREEN_COLT("Artefact_Mousquet_GreenColt", "Green Colt", 1.1f, 0.5f, 1f, 1f, 0, ArtefactWeapons.AIGLES),
        BLUE_TIGER("Artefact_Mousquet_BlueTiger", "Blue Tiger", 1.5f, 1f, 1f, 1f, 2, CANON);

        public final String id, name, transform;
        final float damage, reload, reach, scope;
        final int pierce;

        Arme(String id, String name, float damage, float reload, float reach, float scope, int pierce, String transform) {
            this.id = id; this.name = name; this.damage = damage; this.reload = reload; this.reach = reach; this.scope = scope;
            this.pierce = pierce; this.transform = transform;
        }
    }

    /** Mousquet correspondant a un objet (variante a baionnette comprise), sinon null. */
    public static Arme arme(String itemId) {
        if (itemId == null) return null;
        String base = itemId.endsWith(BAYONET_SUFFIX) ? itemId.substring(0, itemId.length() - BAYONET_SUFFIX.length()) : itemId;
        for (Arme a : Arme.values()) if (a.id.equals(base)) return a;
        return null;
    }

    public static boolean isMusket(String itemId) { return arme(itemId) != null; }

    /** Arme a feu : mousquets (et variantes a baionnette), SARS, canon de char, Colts de cavalerie. Mousquetaire seulement. */
    public static boolean isFirearm(String itemId) {
        return itemId != null && (isMusket(itemId) || SARS.equals(itemId) || CANON.equals(itemId) || ArtefactWeapons.AIGLES.equals(itemId));
    }

    /** Sac du tireur (barre, reserve, sac a dos) : munitions retirees et comptees ici. */
    static CombinedItemContainer bag(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        if (p != null && p.getInventory() != null) return p.getInventory().getCombinedBackpackStorageHotbar();
        return InventoryComponent.getCombined(store, ref, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
    }

    static int count(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, String id) {
        CombinedItemContainer b = bag(ref, store);
        return b == null ? 0 : b.countItemStacks(st -> st != null && id.equals(st.getItemId()));
    }

    // ----- chiffres -----
    static final int CAP = 3000;                                                 // points de stat au-dela desquels rien ne monte
    static final float MIN_DAMAGE = 70f, MAX_DAMAGE = 2000f;
    // portee sure : 10 blocs sans endurance, 500 a 3 000 points ; au-dela la balle file encore un quart de cette portee
    // (10 blocs au moins), jamais plus de 500 blocs (retours de l'utilisateur du 2026-11-15)
    static final double BASE_RANGE = 10, MAX_SAFE_RANGE = 500, OVERSHOOT = 10, MAX_FLIGHT = 500;
    static final float BASE_PRECISION = 0.5f, MAX_PRECISION = 0.95f;
    static final long BASE_RELOAD_MS = 50_000, MIN_RELOAD_MS = 1_000;
    static final double BASE_SPEED = 140, MAX_SPEED = 600;
    static final float CONCENTRATION = 2f;
    static final float BAYONET_STR_DIVISOR = 250f, BAYONET_MAX_MULT = 13f;
    static final float SIGNATURE_PER_HIT = 7f;                                   // energie max des mousquets : 20 -> 3 balles au but
    // SARS
    static final int SARS_MAG = 5;
    static final long SARS_BOLT_MS = 900, SARS_RELOAD_MS = 2_500;
    static final double SARS_RANGE = 2_000, SARS_SPEED = 900;
    static final float SARS_DAMAGE = 1.5f;
    // Canon de char
    static final long CANON_RELOAD_MS = 5_000;
    static final double CANON_RANGE = 100, CANON_SPEED = 120, CANON_BLAST = 4.0, CANON_CRATER = 2.2;
    static final float CANON_DAMAGE = 5f;
    // Colts (AiglesDuDesert)
    static final int COLT_MAG = 10;
    static final long COLT_RELOAD_MS = 600;

    private static final String CARRIER = "Projectile_Config_Balle_Mousquet", CARRIER_SNIPER = "Projectile_Config_Balle_Sniper_Divine";
    private static final String CARRIER_SHELL = "Projectile_Config_Obus_Divin";
    private static final String FX_MUZZLE = "RifleShooting", FX_SMOKE = "Musket", FX_IMPACT = "RifleShooting_Impact", FX_DEBRIS = "Gun_Impact";
    private static final String FX_GLINT = "Gilgamesh_Sword_Burst", FX_BLAST = "Explosion_Big";
    private static final String SFX_FLESH = "SFX_Blunderbuss_Hit", SFX_WALL = "SFX_Blunderbuss_Miss", SFX_LOADED = "SFX_Blunderbuss_Load";
    private static final String SFX_RELOAD = "SFX_Hand_Crossbow_T2_Reload_Start", SFX_BLAST = "SFX_Gobliterator_Barrel_Explosion";

    /** Instant (ms) ou l'arme a un coup (mousquets, canon) du joueur sera rechargee. */
    private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();
    /** Chargeurs (SARS, Colts) par joueur. */
    private static final Map<UUID, Mag> MAGS = new ConcurrentHashMap<>();
    /** Cibles touchees par une balle (degats deja calcules avec la force : ArtefactWeapons ne la reapplique pas). */
    private static final Map<Ref<EntityStore>, Long> BULLET_HITS = new ConcurrentHashMap<>();

    private static final class Mag {
        int rounds;
        int loading;                                                             // balles en cours de chargement
        long reloadEnd, nextShot;
        String weapon;
    }

    // =====================================================================================================================
    // Statistiques du tireur
    // =====================================================================================================================

    private static int points(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, StatConfig stat) {
        if (ref == null || !ref.isValid()) return 0;
        try {
            PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            return d == null ? 0 : Math.max(0, Math.min(CAP, stat.getTotalPoints(d, ClassManager.get(d.getPlayerClassId()))));
        } catch (Exception e) {
            return 0;
        }
    }

    static int strength(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) { return points(ref, store, StatConfig.STRENGTH_DAMAGE); }
    static int agility(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) { return points(ref, store, StatConfig.AGILITY_SPEED); }
    static int endurance(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) { return points(ref, store, StatConfig.ENDURANCE); }

    /** FORCE : degats d'une balle de mousquet (70 -> 2 000 a 3 000 points). */
    public static float bulletDamage(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        return MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * strength(ref, store) / CAP;
    }

    /** AGILITE : duree du rechargement d'un mousquet (ms), 1 s minimum. */
    static long reloadMs(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, Arme a) {
        double ms = BASE_RELOAD_MS / (1.0 + 49.0 * agility(ref, store) / CAP) * (a != null ? a.reload : 1f);
        return Math.max(MIN_RELOAD_MS, Math.round(ms));
    }

    /** ENDURANCE : portee de tir sur (blocs). */
    static double safeRange(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, Arme a) {
        double r = BASE_RANGE + (MAX_SAFE_RANGE - BASE_RANGE) * endurance(ref, store) / CAP;
        return r * (a != null ? a.reach : 1f);
    }

    /** ENDURANCE : chance de toucher au-dela de la portee sure. */
    static float farPrecision(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store, Arme a) {
        float p = BASE_PRECISION + (MAX_PRECISION - BASE_PRECISION) * endurance(ref, store) / CAP;
        if (a != null && a.reach > 1f) p += 0.1f;
        return Math.min(0.98f, p);
    }

    /** FORCE + ENDURANCE : vitesse de la balle (blocs/s), 140 -> 600. */
    static double bulletSpeed(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        return BASE_SPEED + (MAX_SPEED - BASE_SPEED) * (strength(ref, store) + endurance(ref, store)) / (2.0 * CAP);
    }

    /** Endurance qui compte pour les paliers de la lunette (Black Bess : x1,5). */
    static int scopeEndurance(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Arme a = arme(heldId(ref, store));
        return Math.round(endurance(ref, store) * (a != null ? a.scope : 1f));
    }

    /** Multiplicateur des degats au corps a corps d'un mousquet (baionnette) ; 1 pour une balle (deja calculee). */
    /** Marque une cible touchee par une balle (et purge les vieilles marques : cibles du canon, des Colts, mortes...). */
    private static void markBullet(Ref<EntityStore> e) {
        long now = System.currentTimeMillis();
        if (BULLET_HITS.size() > 128) BULLET_HITS.values().removeIf(t -> now - t > 2_000);
        BULLET_HITS.put(e, now);
    }

    public static float meleeMultiplier(Ref<EntityStore> attacker, Ref<EntityStore> victim, Store<EntityStore> store) {
        Long t = victim != null ? BULLET_HITS.remove(victim) : null;
        if (t != null && System.currentTimeMillis() - t < 1_000) return 1f;
        return Math.min(BAYONET_MAX_MULT, 1f + strength(attacker, store) / BAYONET_STR_DIVISOR);
    }

    // =====================================================================================================================
    // Detente (Eldanior_MousquetTir)
    // =====================================================================================================================

    /** Vrai si le coup part. concentration : signature des mousquets. */
    static boolean pullTrigger(Ref<EntityStore> shooter, CommandBuffer<EntityStore> cb, boolean concentration) {
        UUID uuid = SpellFx.uuid(shooter, cb);
        if (uuid == null) return false;
        PlayerRef player = cb.getComponent(shooter, PlayerRef.getComponentType());
        if (ArmeTransfo.expireHeld(shooter, cb)) return false;                // transformation finie : le mousquet revient
        String held = heldId(shooter, cb);
        if (SARS.equals(held)) return sarsTrigger(shooter, uuid, player, cb);
        if (CANON.equals(held)) return canonTrigger(shooter, uuid, player, cb);
        Arme a = arme(held);
        if (a == null) return false;
        long now = System.currentTimeMillis();
        Long ready = READY_AT.get(uuid);
        if (!concentration && ready != null && now < ready) {
            tell(player, "Rechargement du mousquet : encore " + (int) Math.ceil((ready - now) / 1000.0) + " s", false);
            return false;
        }
        CombinedItemContainer bag = bag(shooter, cb);
        ItemStack powder = new ItemStack(POWDER, 1), ball = new ItemStack(BALL, 1);
        if (bag == null || !bag.canRemoveItemStacks(List.of(powder, ball))) {
            tell(player, "Il faut 1 Poudre à canon et 1 Balle de fer pour tirer.", false);
            return false;
        }
        if (!bag.removeItemStack(powder).succeeded()) return false;
        if (!bag.removeItemStack(ball).succeeded()) {
            bag.addItemStack(powder);                                              // ne jamais perdre la poudre
            return false;
        }
        if (!concentration) READY_AT.put(uuid, now + reloadMs(shooter, cb, a));
        RuneSpellRuntime.queue(shooter, concentration ? "MousquetConcentration" : "MousquetBalle");
        return true;
    }

    /** SARS : culasse a verrou (0,9 s entre deux tirs), chargeur de 5 Balles de sniper divines rechargees en 2,5 s. */
    private static boolean sarsTrigger(Ref<EntityStore> shooter, UUID uuid, PlayerRef player, CommandBuffer<EntityStore> cb) {
        Mag m = mag(uuid, SARS);
        long now = System.currentTimeMillis();
        settle(m, now);
        if (m.reloadEnd > 0 || now < m.nextShot) return false;
        if (m.rounds <= 0 && !reload(shooter, player, m, SNIPER_AMMO, SARS_MAG, SARS_RELOAD_MS, "Balles de sniper divines", cb)) return false;
        if (m.rounds <= 0) { RuneSpellRuntime.queue(shooter, "ArmeRechargement"); return false; }   // le chargement commence
        m.rounds--;
        m.nextShot = now + SARS_BOLT_MS;
        RuneSpellRuntime.queue(shooter, "SarsBalle");
        if (m.rounds == 0) reload(shooter, player, m, SNIPER_AMMO, SARS_MAG, SARS_RELOAD_MS, "Balles de sniper divines", cb);
        return true;
    }

    /** Canon de char : un Obus Divin par coup, 5 s de rechargement. */
    private static boolean canonTrigger(Ref<EntityStore> shooter, UUID uuid, PlayerRef player, CommandBuffer<EntityStore> cb) {
        long now = System.currentTimeMillis();
        Long ready = READY_AT.get(uuid);
        if (ready != null && now < ready) {
            tell(player, "Rechargement du canon : encore " + (int) Math.ceil((ready - now) / 1000.0) + " s", false);
            return false;
        }
        CombinedItemContainer bag = bag(shooter, cb);
        if (bag == null || !bag.removeItemStack(new ItemStack(SHELL, 1)).succeeded()) {
            tell(player, "Il faut un Obus Divin pour tirer.", false);
            return false;
        }
        READY_AT.put(uuid, now + CANON_RELOAD_MS);
        RuneSpellRuntime.queue(shooter, "CanonObus");
        return true;
    }

    /** Colts de cavalerie (AiglesDuDesert) : chargeurs de 5 + 5 Balles Divines ; n balles par geste. */
    static boolean coltTrigger(Ref<EntityStore> shooter, CommandBuffer<EntityStore> cb, int n) {
        UUID uuid = SpellFx.uuid(shooter, cb);
        if (uuid == null) return false;
        PlayerRef player = cb.getComponent(shooter, PlayerRef.getComponentType());
        if (ArmeTransfo.expireHeld(shooter, cb)) return false;
        Mag m = mag(uuid, ArtefactWeapons.AIGLES);
        long now = System.currentTimeMillis();
        settle(m, now);
        if (m.reloadEnd > 0) return false;
        if (m.rounds < n && m.rounds <= 0) {
            reload(shooter, player, m, COLT_AMMO, COLT_MAG, COLT_RELOAD_MS, "Balles Divines", cb);
            return false;
        }
        if (m.rounds < n) return false;
        m.rounds -= n;
        if (m.rounds <= 0) reload(shooter, player, m, COLT_AMMO, COLT_MAG, COLT_RELOAD_MS, "Balles Divines", cb);
        return true;
    }

    private static Mag mag(UUID uuid, String weapon) {
        Mag m = MAGS.computeIfAbsent(uuid, k -> new Mag());
        if (!weapon.equals(m.weapon)) { m.weapon = weapon; m.rounds = 0; m.loading = 0; m.reloadEnd = 0; m.nextShot = 0; }
        return m;
    }

    private static void settle(Mag m, long now) {
        if (m.reloadEnd > 0 && now >= m.reloadEnd) { m.rounds += m.loading; m.loading = 0; m.reloadEnd = 0; }
    }

    /** Lance le chargement : retire jusqu'a `size` munitions de l'inventaire. Faux s'il n'y en a aucune. */
    private static boolean reload(Ref<EntityStore> shooter, PlayerRef player, Mag m, String ammo, int size, long ms, String label,
                                  CommandBuffer<EntityStore> cb) {
        CombinedItemContainer bag = bag(shooter, cb);
        int have = bag == null ? 0 : bag.countItemStacks(s -> s != null && ammo.equals(s.getItemId()));
        int take = Math.min(size - m.rounds, have);
        if (take <= 0) {
            if (m.rounds <= 0) tell(player, "Plus de " + label + " : impossible de recharger.", false);
            return false;
        }
        if (!bag.removeItemStack(new ItemStack(ammo, take)).succeeded()) return false;
        m.loading = take;
        m.reloadEnd = System.currentTimeMillis() + ms;
        tell(player, "Rechargement : " + (m.rounds + take) + "/" + size + " (" + (have - take) + " " + label + " en réserve)", true);
        Vector3d p = SpellFx.position(shooter, cb);
        if (p != null) SpellFx.sound(SFX_RELOAD, p, cb);
        return true;
    }

    /** Transformation : le chargeur de l'arme est rempli tout de suite (munitions retirees de l'inventaire). */
    static void prime(Ref<EntityStore> shooter, String weapon, CommandBuffer<EntityStore> cb) {
        UUID uuid = SpellFx.uuid(shooter, cb);
        if (uuid == null) return;
        PlayerRef player = cb.getComponent(shooter, PlayerRef.getComponentType());
        if (CANON.equals(weapon)) { READY_AT.remove(uuid); return; }
        boolean sars = SARS.equals(weapon);
        Mag m = mag(uuid, weapon);
        m.rounds = 0; m.loading = 0; m.reloadEnd = 0; m.nextShot = 0;
        if (reload(shooter, player, m, sars ? SNIPER_AMMO : COLT_AMMO, sars ? SARS_MAG : COLT_MAG, 0, sars ? "Balles de sniper divines" : "Balles Divines", cb))
            settle(m, System.currentTimeMillis() + 1);
    }

    /** Balles restantes dans le chargeur des Colts (pour l'affichage eventuel). */
    static int coltRounds(UUID uuid) {
        Mag m = uuid == null ? null : MAGS.get(uuid);
        return m == null || !ArtefactWeapons.AIGLES.equals(m.weapon) ? 0 : m.rounds;
    }

    private static String heldId(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Player p = ref != null && ref.isValid() ? store.getComponent(ref, Player.getComponentType()) : null;
        ItemStack item = p != null && p.getInventory() != null ? p.getInventory().getActiveHotbarItem() : null;
        return item != null && !item.isEmpty() ? item.getItemId() : null;
    }

    /** Jeu d'animations des armes a feu (Rifle vanilla + baionnette et rechargements, gen_mousquets.py). */
    static final String ANIM_SET = "Mousquet";

    /** Animation de rechargement du mousquet selon sa duree (rechargement lent, rapide, eclair). */
    static String musketReloadAnim(long reloadMs) {
        return reloadMs >= 2_600 ? "Rechargement" : reloadMs >= 1_300 ? "RechargementRapide" : "RechargementEclair";
    }

    static void playAnim(Ref<EntityStore> ref, String anim, Store<EntityStore> store) {
        try {
            if (ref != null && ref.isValid()) AnimationUtils.playAnimation(ref, AnimationSlot.Action, ANIM_SET, anim, true, store);
        } catch (Exception e) {
            EldaniorLogger.error("Mousquet: animation " + anim, e);
        }
    }

    /** Sort ArmeRechargement : chargeur vide au clic (SARS) : on recharge, animation du chargeur. */
    public static final class Rechargement implements RuneSpell {
        @Override public String id() { return "ArmeRechargement"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            playAnim(caster, "RechargementChargeur", store);
            return null;
        }
    }

    static void tell(PlayerRef player, String text, boolean success) {
        if (player == null) return;
        try {
            if (success) NotificationHelper.sendSuccess(player, text);
            else NotificationHelper.sendWarning(player, text);
        } catch (Exception e) {
            EldaniorLogger.error("Mousquet: notification", e);
        }
    }

    // =====================================================================================================================
    // Les balles
    // =====================================================================================================================

    /** Reglages d'une balle tiree : portee sure, portee maximale, precision au-dela, vitesse, degats, ennemis traverses. */
    private record Shot(String carrier, double safe, double max, float precision, double speed, float damage, int pierce,
                        boolean waitReload, boolean charges, String label, float muzzleScale) {}

    /** Sort MousquetBalle / MousquetConcentration : la balle part de la bouche. */
    public static final class Tir implements RuneSpell {
        private final boolean concentration;

        private Tir(boolean concentration) { this.concentration = concentration; }

        public static Tir normal() { return new Tir(false); }

        public static Tir concentration() { return new Tir(true); }

        @Override public String id() { return concentration ? "MousquetConcentration" : "MousquetBalle"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Arme a = arme(ArtefactWeapons.held(caster, store));
            if (a == null) a = Arme.MOUSQUET;
            float k = concentration ? CONCENTRATION : 1f;
            double safe = safeRange(caster, store, a) * k;
            float precision = Math.min(1f, farPrecision(caster, store, a) * k);
            safe = Math.min(MAX_FLIGHT, safe);
            Shot s = new Shot(CARRIER, safe, Math.min(MAX_FLIGHT, safe + Math.max(OVERSHOOT, safe * 0.25)), precision, bulletSpeed(caster, store),
                    bulletDamage(caster, store) * a.damage * k, a.pierce, !concentration, true,
                    concentration ? "Concentration" : a.name, concentration ? 1.8f : 1.3f);
            return Ball.fire(caster, s, store, cb);
        }
    }

    /** Sort SarsBalle : balle de sniper divine, droite et precise jusqu'a 2 000 blocs. */
    public static final class SarsTir implements RuneSpell {
        @Override public String id() { return "SarsBalle"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Shot s = new Shot(CARRIER_SNIPER, SARS_RANGE, SARS_RANGE, 1f, SARS_SPEED,
                    bulletDamage(caster, store) * SARS_DAMAGE, 1, false, false, "SARS", 1.5f);
            return Ball.fire(caster, s, store, cb);
        }
    }

    private static final class Ball implements RuneSpell.Instance {
        private static final double STEP = 0.35, HIT_RADIUS = 0.55;
        private static final long KILL_WATCH_MS = 1_500;

        final Shot shot;
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final boolean onTarget;
        final long readyAt, startAt, maxFlightMs;
        String reloadAnim;                                                    // animation de rechargement a jouer apres le tir
        long animAt;
        boolean animDone;
        final Set<Ref<EntityStore>> passed = new HashSet<>();
        final List<Object[]> victims = new ArrayList<>();                     // {cible, distance, instant}
        long last;
        double travelled;
        int hits;
        boolean flying = true, deviated, killed;

        private Ball(Shot shot, Vector3d pos, Vector3d dir, Ref<EntityStore> model, boolean onTarget, long readyAt) {
            this.shot = shot; this.pos = pos; this.dir = dir; this.model = model; this.onTarget = onTarget; this.readyAt = readyAt;
            this.startAt = System.currentTimeMillis(); this.last = startAt;
            this.maxFlightMs = (long) (shot.max() / shot.speed() * 1000) + 600;
        }

        static Ball fire(Ref<EntityStore> caster, Shot shot, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            Vector3d eye = new Vector3d(at).add(0, 1.55, 0);
            Vector3d right = new Vector3d(-look.z, 0, look.x);
            if (right.lengthSquared() < 1e-4) right.set(1, 0, 0);
            right.normalize();
            Vector3d muzzle = new Vector3d(eye).add(new Vector3d(look).mul(1.5)).add(right.mul(0.22)).add(0, -0.12, 0);
            // la trace part de l'oeil, dans l'axe du regard (visee exacte, ennemi colle au canon touche) ; flash a la bouche
            Vector3d start = new Vector3d(eye).add(new Vector3d(look).mul(0.3));
            Vector3d dir = new Vector3d(look);
            Ref<EntityStore> model = SpellFx.spawnModel(caster, shot.carrier(), muzzle, dir, cb);
            SpellFx.notSaved(model, store, cb);
            float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
            float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, dir.y))));
            SpellFx.fx(FX_MUZZLE, muzzle, yaw, pitch - 90f, shot.muzzleScale(), store, cb);       // flash
            SpellFx.fx(FX_SMOKE, muzzle, yaw, pitch - 90f, 1.0f, store, cb);                      // fumee, etincelles (axe +Y = tir)
            SpellFx.shake(muzzle, 3, "Impact_Strong", store);                                    // recul, pour le tireur
            boolean onTarget = ThreadLocalRandom.current().nextFloat() < shot.precision();
            UUID uuid = SpellFx.uuid(caster, store);
            Long ready = shot.waitReload() && uuid != null ? READY_AT.get(uuid) : null;
            Ball b = new Ball(shot, start, dir, model, onTarget, ready != null ? ready : 0L);
            if (ready != null) {                                                // mousquet : on recharge a la baguette
                b.reloadAnim = musketReloadAnim(ready - b.startAt);
                b.animAt = b.startAt + ("RechargementEclair".equals(b.reloadAnim) ? 250 : 450);
            } else if (uuid != null && SARS.equals(heldId(caster, store))) {
                Mag m = MAGS.get(uuid);
                if (m != null && m.reloadEnd > 0) { b.reloadAnim = "RechargementChargeur"; b.animAt = b.startAt + 350; }
            }
            return b;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (reloadAnim != null && !animDone && now >= animAt) {
                animDone = true;                                                  // pas pendant la visee (lunette)
                if (SpellFx.chains(caster, store, InteractionType.Secondary).isEmpty()) playAnim(caster, reloadAnim, store);
            }
            if (flying && (now - startAt >= maxFlightMs || travelled >= shot.max() || !fly(caster, now, store, cb))) {
                flying = false;
                SpellFx.remove(model, cb);
            }
            watchKills(caster, now, store);
            if (flying) return false;
            boolean watching = !killed && victims.stream().anyMatch(v -> now - (long) v[2] < KILL_WATCH_MS);
            if (watching) return false;
            if (readyAt <= 0) return reloadAnim == null || animDone;
            if (now < readyAt) return false;                                    // la balle est retombee : on attend le rechargement
            Vector3d p = SpellFx.position(caster, store);
            if (p != null) SpellFx.sound(SFX_LOADED, p, cb);
            tell(store.getComponent(caster, PlayerRef.getComponentType()), "Mousquet rechargé (" + count(caster, store, BALL) + " balles, "
                    + count(caster, store, POWDER) + " poudres).", true);
            return true;
        }

        /** Une cible touchee qui meurt dans la seconde et demie : message au tireur avec la distance du tir. */
        private void watchKills(Ref<EntityStore> caster, long now, Store<EntityStore> store) {
            if (killed) return;
            for (Object[] v : victims) {
                @SuppressWarnings("unchecked") Ref<EntityStore> e = (Ref<EntityStore>) v[0];
                if (now - (long) v[2] > KILL_WATCH_MS) continue;
                if (e.isValid() && ArtefactWeapons.healthRatio(e, store) > 0f) continue;
                killed = true;
                int d = (int) Math.round((double) v[1]);
                tell(store.getComponent(caster, PlayerRef.getComponentType()),
                        (shot.label().equals("Concentration") ? "Concentration : tir mortel à " : "Tir mortel à ") + d + (d > 1 ? " blocs !" : " bloc !"), true);
                return;
            }
        }

        /** Avance la balle ; faux quand elle s'arrete (touche, bloc, chunk non charge). */
        private boolean fly(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            World world = SpellFx.world(store);
            if (world == null) return false;
            double move = Math.min(shot.max() - travelled, shot.speed() * Math.min(0.2, (now - last) / 1000.0));
            last = now;
            Vector3d mid = new Vector3d(pos).add(new Vector3d(dir).mul(move / 2));
            List<Ref<EntityStore>> near = SpellFx.enemiesAround(caster, mid, move / 2 + HIT_RADIUS + 2, store);
            for (double done = 0; done < move; done += STEP) {
                double step = Math.min(STEP, move - done);
                pos.add(new Vector3d(dir).mul(step));
                travelled += step;
                boolean far = travelled > shot.safe();
                if (far && !onTarget && !deviated) deviate();                  // tir rate au-dela de la portee sure
                for (Ref<EntityStore> e : near) {
                    if (passed.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    double cy = Math.max(p.y + 0.2, Math.min(p.y + 1.8, pos.y));     // axe vertical du corps
                    if (new Vector3d(p.x, cy, p.z).distance(pos) > HIT_RADIUS) continue;
                    passed.add(e);
                    if (far && !onTarget) continue;                             // la balle siffle a cote
                    strike(caster, e, store, cb);
                    if (++hits > shot.pierce()) return false;
                }
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null) return false;                                    // chunk non charge
                if (t.getMaterial() == BlockMaterial.Solid && !TerrainRestore.isEmpty(world, x, y, z)) {   // herbes et fleurs : traversees
                    SpellFx.fx(FX_IMPACT, pos, 0.8f, store, cb);
                    SpellFx.fx(FX_DEBRIS, pos, 0.5f, store, cb);
                    SpellFx.sound(SFX_WALL, pos, cb);
                    return false;
                }
            }
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return true;
        }

        /** Balle qui part de travers (au-dela de la portee sure, precision ratee) : petite deviation visible. */
        private void deviate() {
            deviated = true;
            ThreadLocalRandom r = ThreadLocalRandom.current();
            double a = Math.toRadians(2.5 + r.nextDouble() * 3.5), b = r.nextDouble() * Math.PI * 2;
            Vector3d side = new Vector3d(-dir.z, 0, dir.x);
            if (side.lengthSquared() < 1e-4) side.set(1, 0, 0);
            side.normalize();
            Vector3d up = new Vector3d(side).cross(dir).normalize();
            dir.add(side.mul(Math.tan(a) * Math.cos(b))).add(up.mul(Math.tan(a) * Math.sin(b))).normalize();
        }

        private void strike(Ref<EntityStore> caster, Ref<EntityStore> e, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            markBullet(e);                     // la force est deja dans shot.damage()
            SpellFx.damage(caster, e, "Physical", shot.damage(), new Vector3d(dir).mul(3.5).add(0, 1.5, 0), "Impact_Strong", cb);
            SpellFx.fx(FX_IMPACT, pos, 1.2f, store, cb);
            SpellFx.sound(SFX_FLESH, pos, cb);
            victims.add(new Object[]{e, travelled, System.currentTimeMillis()});
            if (shot.charges() && hits == 0) Tenryumaru.energy(caster, SIGNATURE_PER_HIT, store);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(model, cb); }

        @Override public void abort() { RuneSpellRuntime.removeLater(model); }
    }

    // =====================================================================================================================
    // Canon de char a main : obus divin
    // =====================================================================================================================

    /** Sort CanonObus : l'obus file droit, traverse les ennemis (degats x5) et explose au premier mur ou a 100 blocs. */
    public static final class Obus implements RuneSpell {
        @Override public String id() { return "CanonObus"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            Vector3d eye = new Vector3d(at).add(0, 1.5, 0);
            Vector3d right = new Vector3d(-look.z, 0, look.x);
            if (right.lengthSquared() < 1e-4) right.set(1, 0, 0);
            right.normalize();
            Vector3d muzzle = new Vector3d(eye).add(new Vector3d(look).mul(1.9)).add(right.mul(0.25)).add(0, -0.1, 0);
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER_SHELL, muzzle, look, cb);
            SpellFx.notSaved(model, store, cb);
            float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
            float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, look.y))));
            SpellFx.fx(FX_MUZZLE, muzzle, yaw, pitch - 90f, 3.0f, store, cb);
            SpellFx.fx(FX_SMOKE, muzzle, yaw, pitch - 90f, 2.0f, store, cb);
            SpellFx.fx(FX_BLAST, muzzle, 0.35f, store, cb);
            SpellFx.shake(muzzle, 8, "Impact_Strong", store);
            SpellFx.push(caster, new Vector3d(look).mul(-6).add(0, 1.5, 0), true, cb);          // recul du canon
            float damage = bulletDamage(caster, store) * CANON_DAMAGE;
            return new Shell(new Vector3d(eye).add(new Vector3d(look).mul(0.6)), look, model, damage);
        }
    }

    private static final class Shell implements RuneSpell.Instance {
        private static final double STEP = 0.4, HIT_RADIUS = 1.1;
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final float damage;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        final List<Object[]> victims = new ArrayList<>();
        final long startAt;
        long last, blastAt;
        double travelled;
        boolean flying = true, killed, animDone;

        Shell(Vector3d pos, Vector3d dir, Ref<EntityStore> model, float damage) {
            this.pos = pos; this.dir = dir; this.model = model; this.damage = damage;
            this.startAt = System.currentTimeMillis(); this.last = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (flying && (now - startAt > 2_500 || travelled >= CANON_RANGE || !fly(caster, now, store, cb))) {
                flying = false;
                SpellFx.remove(model, cb);
                explode(caster, store, cb);
                blastAt = now;
            }
            if (!killed) {
                for (Object[] v : victims) {
                    @SuppressWarnings("unchecked") Ref<EntityStore> e = (Ref<EntityStore>) v[0];
                    if (e.isValid() && ArtefactWeapons.healthRatio(e, store) > 0f) continue;
                    killed = true;
                    int d = (int) Math.round((double) v[1]);
                    tell(store.getComponent(caster, PlayerRef.getComponentType()), "Obus mortel à " + d + (d > 1 ? " blocs !" : " bloc !"), true);
                    break;
                }
            }
            if (!animDone && now - startAt >= 800) { animDone = true; playAnim(caster, "RechargementChargeur", store); }
            return !flying && animDone && (killed || now - blastAt > 1_500);
        }

        private boolean fly(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            World world = SpellFx.world(store);
            if (world == null) return false;
            double move = Math.min(CANON_RANGE - travelled, CANON_SPEED * Math.min(0.2, (now - last) / 1000.0));
            last = now;
            Vector3d mid = new Vector3d(pos).add(new Vector3d(dir).mul(move / 2));
            List<Ref<EntityStore>> near = SpellFx.enemiesAround(caster, mid, move / 2 + HIT_RADIUS + 2, store);
            for (double done = 0; done < move; done += STEP) {
                double step = Math.min(STEP, move - done);
                pos.add(new Vector3d(dir).mul(step));
                travelled += step;
                for (Ref<EntityStore> e : near) {
                    if (hit.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    double cy = Math.max(p.y + 0.2, Math.min(p.y + 1.8, pos.y));
                    if (new Vector3d(p.x, cy, p.z).distance(pos) > HIT_RADIUS) continue;
                    hit.add(e);                                                    // l'obus perce : il continue sa ligne
                    markBullet(e);
                    SpellFx.damage(caster, e, "Physical", damage, new Vector3d(dir).mul(8).add(0, 4, 0), "Impact_Strong", cb);
                    SpellFx.fx(FX_IMPACT, pos, 2.0f, store, cb);
                    SpellFx.fx(FX_GLINT, pos, 0.6f, store, cb);
                    SpellFx.sound(SFX_FLESH, pos, cb);
                    victims.add(new Object[]{e, travelled});
                }
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null) return false;
                if (t.getMaterial() == BlockMaterial.Solid && !TerrainRestore.isEmpty(world, x, y, z)) return false;
            }
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return true;
        }

        /** Explosion : degats de zone (moitie de l'obus) et cratere restaure apres une minute. */
        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_BLAST, pos, 1.2f, store, cb);
            SpellFx.fx(FX_GLINT, pos, 1.2f, store, cb);
            SpellFx.sound(SFX_BLAST, pos, cb);
            SpellFx.shake(pos, 20, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, CANON_BLAST + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || p.distance(pos) > CANON_BLAST + 1) continue;
                Vector3d out = new Vector3d(p).sub(pos);
                out = out.lengthSquared() > 0.04 ? out.normalize().mul(9) : new Vector3d(dir).mul(9);
                markBullet(e);
                SpellFx.damage(caster, e, "Physical", hit.contains(e) ? damage * 0.25f : damage * 0.5f, out.add(0, 6, 0), "Impact_Strong", cb);
                if (!hit.contains(e)) victims.add(new Object[]{e, travelled});
            }
            World world = SpellFx.world(store);
            if (world != null) AiglesDuDesert.blast(world, pos, CANON_CRATER, store, cb);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(model, cb); }

        @Override public void abort() { RuneSpellRuntime.removeLater(model); }
    }

    // =====================================================================================================================
    // Visee : distance de vue des entites
    // =====================================================================================================================

    /** Joueurs qui visent, avec la distance (blocs) a laquelle ils doivent voir les entites. */
    private static final Map<UUID, Double> AIMING = new ConcurrentHashMap<>();
    private static final double ENTITY_THICKNESS = 0.6;                     // un humanoide : la plus fine des cibles utiles
    private static volatile Double savedLod;

    /**
     * Sort MousquetVisee (lance au debut de la visee) : tant que le clic droit est tenu, le serveur envoie au tireur les
     * entites lointaines que le tri par taille (EntityTrackerSystems.LODCull) cacherait au-dela de ~130 blocs. Le reglage
     * du moteur est global : il revient a sa valeur des que plus personne ne vise.
     */
    public static final class Visee implements RuneSpell {
        @Override public String id() { return "MousquetVisee"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            UUID uuid = SpellFx.uuid(caster, store);
            if (uuid == null || AIMING.containsKey(uuid)) return null;
            String held = ArtefactWeapons.held(caster, store);
            double reach = SARS.equals(held) ? SARS_RANGE : CANON.equals(held) ? CANON_RANGE * 2
                    : Math.min(MAX_FLIGHT, safeRange(caster, store, arme(held)) * 1.25 + OVERSHOOT);
            AIMING.put(uuid, Math.min(1_000, Math.max(160, reach)));
            applyLod();
            return new Aim(uuid, held);
        }
    }

    private static final class Aim implements RuneSpell.Instance {
        final UUID uuid;
        final String weapon;
        final long startAt = System.currentTimeMillis();

        Aim(UUID uuid, String weapon) { this.uuid = uuid; this.weapon = weapon; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (now - startAt < 300) return false;                              // la chaine de visee s'ouvre a peine
            String held = ArtefactWeapons.held(caster, store);
            return held == null || !held.equals(weapon) || SpellFx.chains(caster, store, InteractionType.Secondary).isEmpty();
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { stop(); }

        @Override public void abort() { stop(); }

        private void stop() {
            AIMING.remove(uuid);
            applyLod();
        }
    }

    private static synchronized void applyLod() {
        try {
            if (AIMING.isEmpty()) {
                if (savedLod != null) EntityTrackerSystems.LODCull.ENTITY_LOD_RATIO = savedLod;
                savedLod = null;
                return;
            }
            if (savedLod == null) savedLod = EntityTrackerSystems.LODCull.ENTITY_LOD_RATIO;
            double far = AIMING.values().stream().mapToDouble(Double::doubleValue).max().orElse(160);
            EntityTrackerSystems.LODCull.ENTITY_LOD_RATIO = Math.min(savedLod, ENTITY_THICKNESS / (far * far));
        } catch (Exception e) {
            EldaniorLogger.error("Mousquet: distance de vue", e);
        }
    }
}
