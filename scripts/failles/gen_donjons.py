#!/usr/bin/env python3
"""Failles-donjons : les donjons des failles (docs/plan-failles-donjons.md).

Chaque donjon est une chaine de pieces VANILLA de donjon (Server/Prefabs/Dungeon/...), collees sans rotation par le code
(failles/FailleDungeon) dans le monde vide de la faille, puis une couche « sceau » generee ici :
  - roche autour de l'air des pieces (leurs coques sont faites pour la roche d'une grotte, pas pour le vide) ;
  - passages creuses a chaque jonction (portes de largeurs differentes) ;
  - coffres du jeu retires (Block_Spawner_Block, coffres, generateurs de prefabs) : les recompenses sont NOS coffres au tresor,
    poses par le code a la mort du boss (decision de l'utilisateur, 2026-11-22) ;
  - barriere (Sceau) a l'entree de la salle du boss, ouverte avec la Cle du Sceau lachee par le mini-boss ;
  - pour le cimetiere (rang F) : toute la surface (sol, tombes, arbres morts, cloture, falaise autour).

Les pieces d'un terrier gobelin s'enchainent vers -X (porte d'entree +X = ancre), celles de la crypte vers -Z (entree +Z).

Sorties, par donjon <id> :
  - src/main/resources/Server/Prefabs/Eldanior/Failles/<Id>_Sceau.prefab.json
  - src/main/resources/eldanior/failles/<id>.json   (pieces, salles, points d'apparition, repères) lu par le code
  - <apercu>/<id>.png                                (vu de dessus, a verifier)
Le script verifie que la salle du boss est atteignable depuis l'arrivee (parcours de l'air).

Usage : python3 -I scripts/failles/gen_donjons.py [dossier Prefabs vanilla] [dossier d'apercu]
"""
import collections, json, math, os, random, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
import png  # noqa: E402

VANILLA = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser("~/Desktop/Assets Hytale/Server/Prefabs")
ITEMS = os.path.join(os.path.dirname(VANILLA), "Item", "Items")
PREVIEW = sys.argv[2] if len(sys.argv) > 2 else os.path.join(ROOT, "build", "failles")
RES = os.path.join(ROOT, "src", "main", "resources")

SEAL = "Rock_Stone"
BARRIER = "Eldanior_Faille_Barriere"
REMOVE_WORDS = ("chest", "coffer", "block_spawner", "prefab_spawner")
SOLID_PREFIX = ("Rock_Stone", "Rock_Basalt", "Rock_Shale", "Rock_Slate", "Rock_Marble", "Rock_Calcite", "Rock_Sandstone",
                "Soil_", "Ore_", "Rock_Bedrock")

# ===========================================================================================================================
# Donjons. Monstres : [role, nombre, niveau min, max]. stars : nombre de monstres etoiles tires AU HASARD parmi ceux de la salle
# (1 a 3 etoiles, decision de l'utilisateur 2026-11-23). miniboss / boss : [role, niveau, echelle, nom].
# ===========================================================================================================================
DUNGEONS = [
    {
        "id": "terrier_duc_e", "Id": "Terrier_Duc_E", "rank": "E", "name": "Le Terrier du Duc", "theme": "gobelin",
        "instance": "Eldanior_Faille_Terrier", "axis": "x", "minutes": 15,
        "keyName": "Clé de boss ED", "door": "Eldanior_Porte_Boss_ED", "keyChest": ["ChestTreasureCleBossED", "cle_boss_ed"],
        "chestTable": "donjon_tier1",
        "chain": [
            ("arrivee",    "Dungeon/Goblin_Lair/Prefabs_Mine/Room/Y/Goblin_Lair_Mine_Room_Y_001", None),
            ("galerie",    "Dungeon/Goblin_Lair/Prefabs_Mine/Corridor/Long/Goblin_Lair_Mine_Corridor_Long_005", None),
            ("galerie",    "Dungeon/Goblin_Lair/Prefabs_Mine/Corridor/Shift/Goblin_Lair_Mine_Corridor_Shift_001", None),
            ("galerie",    "Dungeon/Goblin_Lair/Prefabs_Mine/Transition_Goblin/Stairs/Goblin_Lair_Mine_Transition_Goblin_Stairs_001", None),
            ("campement",  "Dungeon/Goblin_Lair/Prefabs_Goblin/Room/Large1/Goblin_Lair_Room_Large1_Large1_001", None),
            ("couloir",    "Dungeon/Goblin_Lair/Prefabs_Goblin/Straight/Length_11/Goblin_Lair_Straight_Length_11_01_001", None),
            ("fosse",      "Dungeon/Goblin_Lair/Prefabs_Goblin/Room/Large2/Goblin_Lair_Room_Large2_Large2_001", None),
            ("descente",   "Dungeon/Goblin_Lair/Prefabs_Goblin/Straight/Length_24_Stairs/Goblin_Lair_Straight_Length_24_Stairs_001", None),
            ("antichambre", "Dungeon/Goblin_Lair/Prefabs_Goblin/Room/Boss_Entrance/Goblin_Lair_Room_Boss_Entrance_001", None),
            ("boss",       "Dungeon/Goblin_Lair/Prefabs_Goblin/Room/Boss/Goblin_Lair_Room_Boss_001", None),
        ],
        "rooms": {
            "arrivee":     {"name": "Arrivée", "safe": True, "mobs": []},
            "galerie":     {"name": "Galerie de mine", "mobs": [
                ["Dungeon_Goblin_Miner", 2, 60, 66], ["Dungeon_Goblin_Lobber", 2, 60, 66], ["Dungeon_Goblin_Thief", 1, 62, 68]]},
            "campement":   {"name": "Le Campement", "stars": 1, "mobs": [
                ["Dungeon_Goblin_Scrapper", 4, 64, 74], ["Dungeon_Goblin_Thief", 2, 64, 72], ["Dungeon_Goblin_Lobber", 2, 66, 74],
                ["Dungeon_Goblin_Mace", 2, 68, 76], ["Dungeon_Goblin_Miner", 1, 66, 72], ["Dungeon_Goblin_Guardian", 1, 74, 78]]},
            "couloir":     {"name": "Passage", "mobs": [["Dungeon_Goblin_Scrapper", 2, 66, 72]]},
            "fosse":       {"name": "La Fosse du Glouton", "stars": 1, "mobs": [
                ["Dungeon_Goblin_Scrapper", 3, 70, 78], ["Dungeon_Goblin_Burner", 2, 72, 78], ["Goblin_Glutton", 1, 90, 100]],
                "keychest": True},
            "descente":    {"name": "Descente", "mobs": [["Dungeon_Goblin_Lobber", 4, 72, 80]]},
            "antichambre": {"name": "Antichambre du Trône", "stars": 1, "mobs": [
                ["Dungeon_Goblin_Guardian", 4, 75, 85], ["Dungeon_Goblin_Mace", 2, 75, 82], ["Dungeon_Goblin_Burner", 1, 76, 84]]},
            "boss":        {"name": "Trône du Duc", "mobs": [["Dungeon_Goblin_Guardian", 3, 80, 88]],
                            "boss": ["Goblin_Duke", 120, 1.2, "Le Duc Gobelin"], "bossMarker": "Goblin_Duke",
                            "reinforcements": [["Dungeon_Goblin_Scrapper_Enrage", 2, 80, 86], ["Dungeon_Goblin_Lobber", 2, 80, 86]]},
        },
        "overflow": [["Dungeon_Goblin_Scrapper", 4], ["Dungeon_Goblin_Lobber", 2], ["Goblin_Glutton", 1]],
    },
    {
        "id": "cimetiere_f", "Id": "Cimetiere_F", "rank": "F", "name": "Le Cimetière des Oubliés", "theme": "mort-vivant",
        "instance": "Eldanior_Faille_Cimetiere", "axis": "z", "minutes": 12,
        "keyName": "Clé de boss ED", "door": "Eldanior_Porte_Boss_ED", "keyChest": ["ChestTreasureCleBossED", "cle_boss_ed"],
        "chestTable": "donjon_common",
        "surface": True,
        "chain": [
            # mausolee vanilla (et son arbre) pose sur l'escalier vanilla qui descend a la crypte : meme ancre (0, 0, 0)
            ("escalier",   "Dungeon/Cursed_Crypt/Entrance/Crypt/Crypt_Entrance_001_Tree", "same"),
            ("escalier",   "Dungeon/Cursed_Crypt/Entrance/Stairs/Crypt_Entrance_Stairs_001", {"exit": [4, -36, -5]}),
            ("ossuaire",   "Dungeon/Cursed_Crypt/Cursed/C_Large/Long/Crypt_Cursed_C_Large_Long_001", None),
            ("necropole",  "Dungeon/Cursed_Crypt/Cursed/Rooms/Hub/Crypt_Cursed_Rooms_Hub_001", None),
            ("passage",    "Dungeon/Cursed_Crypt/Cursed/C_Small/Long/Crypt_Cursed_C_Small_Long_001", None),
            ("tombeau",    "Dungeon/Cursed_Crypt/Tomb/Entrance/Crypt_Tomb_Entrance_001", None),
            ("gisants",    "Dungeon/Cursed_Crypt/Tomb/Hallway/Crypt_Tomb_Hallway_001", None),
            ("antichambre", "Dungeon/Cursed_Crypt/Tomb/Boss_Entrance/Crypt_Tomb_Boss_Entrance_001", None),
            ("boss",       "Dungeon/Cursed_Crypt/Tomb/Boss/Crypt_Tomb_Boss_001", None),
        ],
        "rooms": {
            "cimetiere":   {"name": "La Forêt-Cimetière", "spacing": 14, "mobs": [
                ["Skeleton", 12, 3, 12], ["Skeleton_Scout", 5, 6, 14], ["Skeleton_Archer", 5, 8, 16], ["Skeleton_Fighter", 4, 8, 16]]},
            "escalier":    {"name": "Le Mausolée", "mobs": [["Skeleton", 2, 10, 18]]},
            "ossuaire":    {"name": "Couloir des Ossuaires", "mobs": [["Skeleton_Fighter", 3, 14, 22]]},
            "necropole":   {"name": "La Nécropole", "stars": 1, "mobs": [
                ["Skeleton_Soldier", 3, 18, 28], ["Skeleton_Archer", 2, 18, 26], ["Skeleton_Mage", 2, 20, 28], ["Skeleton_Knight", 1, 30, 40]],
                "keychest": True},
            "passage":     {"name": "Passage étroit", "mobs": [["Skeleton_Scout", 2, 22, 30]]},
            "tombeau":     {"name": "Entrée du Tombeau", "mobs": [["Skeleton_Ranger", 2, 26, 34], ["Skeleton_Fighter", 2, 26, 34]]},
            "gisants":     {"name": "Galerie des Gisants", "stars": 1, "mobs": [["Skeleton_Knight", 2, 30, 40], ["Skeleton_Mage", 2, 30, 38]]},
            "antichambre": {"name": "Antichambre du Roi", "mobs": [["Skeleton_Soldier", 3, 36, 46]]},
            "boss":        {"name": "Tombeau du Roi Squelette", "mobs": [["Skeleton_Knight", 2, 44, 50]],
                            "boss": ["Skeleton_Elite", 55, 1.5, "Le Roi Squelette"], "bossMarker": "Eldanior_Faille_Boss_Roi_Squelette",
                            "reinforcements": [["Skeleton", 3, 40, 48], ["Skeleton_Archer", 1, 42, 48]]},
        },
        "overflow": [["Skeleton", 4], ["Skeleton_Archer", 2], ["Skeleton_Knight", 1]],
    },
    {
        # Rang D (2026-11-24, docs/plan-failles-donjons.md § 15). EXTERIEUR SEUL (demande de l'utilisateur : un biome propre au
        # donjon, qui ne ressemble pas au cimetiere du F ; il construit le donjon lui-meme, en edition, sur l'esplanade du centre) :
        # la Caldeira (scripts/failles/biome_caldeira.py). Teintes, meteo, particules et ambiance : gen_failles.py.
        "id": "necropole_d", "Id": "Necropole_D", "rank": "D", "name": "La Nécropole Calcinée", "theme": "calciné",
        "instance": "Eldanior_Faille_Necropole", "minutes": 15, "keyName": "Clé de boss ED",
        "exterior": "caldeira", "surfaceRoom": "caldeira", "seed": 24,
        "rooms": {
            "caldeira": {"name": "La Caldeira", "spacing": 12, "stars": 2, "mobs": [
                ["Skeleton_Burnt_Soldier", 8, 225, 255], ["Skeleton_Burnt_Archer", 5, 210, 240], ["Skeleton_Burnt_Lancer", 4, 220, 250],
                ["Skeleton_Incandescent_Head", 4, 215, 245], ["Skeleton_Burnt_Gunner", 3, 215, 245],
                ["Skeleton_Incandescent_Footman", 3, 225, 260], ["Skeleton_Burnt_Knight", 2, 225, 255],
                ["Skeleton_Incandescent_Mage", 2, 245, 280], ["Emberwulf", 2, 250, 330]],
                # boss provisoire au centre de l'esplanade : a deplacer avec le poste « Boss de donjon » une fois le donjon construit
                "boss": ["Skeleton_Burnt_Praetorian", 300, 1.8, "Le Prétorien Calciné"],
                "reinforcements": [["Skeleton_Incandescent_Footman", 2, 240, 260], ["Skeleton_Burnt_Archer", 1, 235, 245]]},
        },
        "overflow": [["Skeleton_Burnt_Soldier", 4], ["Skeleton_Burnt_Archer", 2], ["Skeleton_Burnt_Knight", 1]],
    },
]


def block_names():
    names = set()
    for root in (ITEMS, os.path.join(RES, "Server", "Item", "Items")):
      for d, _, files in os.walk(root):
        for f in files:
            if f.endswith(".json"):
                names.add(f[:-5])
    return names


VANILLA_BLOCKS = block_names()
MARKERS = {f[:-5] for d, _, fs in os.walk(os.path.join(os.path.dirname(VANILLA), "NPC", "Spawn", "Markers")) for f in fs if f.endswith(".json")}
MARKERS.add("Eldanior_Faille_Boss_Roi_Squelette")           # balise du mod (gen_failles.py)


def marker_id(name):
    if name not in MARKERS:
        raise SystemExit(f"balise d'apparition inconnue : {name}")
    return name


def check(name):
    if name not in VANILLA_BLOCKS and not name.startswith(("Eldanior_", "*")) and name != "Empty":
        raise SystemExit(f"bloc inconnu : {name}")
    return name


def is_solid(name):
    low = name.lower()
    if any(w in low for w in ("stalactite", "half", "stairs", "slab", "pathway", "rubble", "_grass", "beam", "fence", "roof")):
        return False
    return name.startswith(SOLID_PREFIX) or ("Planks" in name and "Half" not in name) or name.endswith("_Trunk") or "Brick" in name


def load(rel):
    with open(os.path.join(VANILLA, rel + ".prefab.json"), encoding="utf-8") as f:
        return json.load(f)


def door_on_face(blocks, axis, sign):
    """Air (Empty) qui touche la face (axe x/z, signe -1 = face min) : (coord face, u min, u max, y min, y max) ou None."""
    ai, ui = (0, 2) if axis == "x" else (2, 0)
    vals = [(b["x"], b["y"], b["z"]) for b in blocks]
    at = (min if sign < 0 else max)(v[ai] for v in vals)
    cells = [(b["x"], b["y"], b["z"]) for b in blocks if b["name"] == "Empty" and (b["x"], b["y"], b["z"])[ai] == at]
    if not cells:
        return None
    return at, min(c[ui] for c in cells), max(c[ui] for c in cells), min(c[1] for c in cells), max(c[1] for c in cells)


# ===========================================================================================================================
# Surface du rang F (2026-11-23, demande de l'utilisateur) : FORET-CIMETIERE de 200 x 200, zone fermee par un mur Barrier
# invisible + panneaux d'energie violets (comme la frontiere du royaume, KingdomBarrierSystem), brouillard (meteo de l'instance),
# mausolee vanilla au CENTRE qui descend a la crypte et au boss. Origine = ancre du mausolee ; sol en y = -1 au centre.
# ===========================================================================================================================
HALF = 100                       # zone de -100 a +100
OUTER = 60                       # terrain de decor au-dela du mur (la carte n'est pas coupee ; le brouillard fait le reste)
WALL_H = 48                      # hauteur du mur (au-dessus du sol le plus bas)
TREES = {                        # familles d'arbres vanilla (Prefabs/Trees) et poids
    "Ash_Dead": 30, "Fir_Dead": 22, "Burnt_dead": 14, "Dry_Dead": 12, "Ash_twisted": 7, "Ash_swamp_dead": 4, "Oak_Moss": 3,
}


def tree_pool(trees):
    import glob
    pool = []
    for fam, w in trees.items():
        files = sorted(glob.glob(os.path.join(VANILLA, "Trees", fam, "**", "*.prefab.json"), recursive=True))
        if not files:
            continue
        loaded = []
        for f in files[:8]:
            with open(f, encoding="utf-8") as fh:
                d = json.load(fh)
            loaded.append((d["anchorX"], d["anchorY"], d["anchorZ"], d["blocks"]))
        pool.append((fam, w, loaded))
    return pool


def cemetery(world, rng):
    out, floor = {}, []

    def noise(x, z):
        return (math.sin(x * 0.071 + 1.3) + math.sin(z * 0.063 + 0.4) + 0.6 * math.sin((x + z) * 0.11)
                + 0.4 * math.sin((x - z) * 0.17 + 2.0))

    def height(x, z):
        r = math.hypot(x, z)
        h = round(noise(x, z) * 0.9)
        if r < 24:
            return -1                                                          # esplanade du mausolee
        if r < 34:
            return -1 + round(h * (r - 24) / 10)
        return -1 + h

    building = lambda x, z: -15 <= x <= 15 and -7 <= z <= 24
    avenue = lambda x, z: abs(x) <= 1 and -96 <= z <= -6

    def ring(x, z):
        return abs(math.hypot(x, z) - 30) <= 1.0

    def put(k, n, force=False):
        if k in world and not force:
            return
        out[k] = check(n)

    tops = {}
    top_pick = [("Soil_Grass_Dry", 34), ("Soil_Dirt", 18), ("Soil_Mud_Dry", 12), ("Soil_Gravel_Mossy", 9), ("Soil_Mud", 9),
                ("Soil_Grass", 12), ("Soil_Grass_Deep", 6)]
    tot = sum(w for _, w in top_pick)

    def pick():
        r = rng.random() * tot
        for n, w in top_pick:
            r -= w
            if r <= 0:
                return n
        return top_pick[0][0]

    # --- sol (3 couches dans la zone, 2 au-dela du mur) ---
    for x in range(-HALF - OUTER, HALF + OUTER + 1):
        for z in range(-HALF - OUTER, HALF + OUTER + 1):
            h = height(x, z)
            out_zone = abs(x) > HALF + 1 or abs(z) > HALF + 1
            if out_zone:                                                       # au-dela : relief qui remonte doucement
                d = max(abs(x), abs(z)) - HALF
                h += min(6, d // 10)
            tops[(x, z)] = h
            if out_zone:
                put((x, h - 1, z), "Soil_Dirt"); put((x, h, z), pick())
                continue
            for y in range(h - 2, h):
                put((x, y, z), "Soil_Dirt" if y == h - 1 else "Rock_Stone")
            path = avenue(x, z) or ring(x, z)
            put((x, h, z), ("Soil_Pathway" if rng.random() < 0.8 else "Rock_Stone_Cobble_Mossy") if path else pick())
    # --- mur : Barrier invisible sur toute la hauteur + panneaux d'energie (comme le royaume) ---
    bottom = min(v for (x, z), v in tops.items() if abs(x) <= HALF + 1 and abs(z) <= HALF + 1) - 3
    for a in range(-HALF - 1, HALF + 2):
        for (x, z) in ((a, -HALF - 1), (a, HALF + 1), (-HALF - 1, a), (HALF + 1, a)):
            for y in range(bottom, bottom + WALL_H + 8):
                put((x, y, z), "Barrier", force=True)
    for a in range(-HALF + 6, HALF - 5, 12):
        for (x, z, rot) in ((a, -HALF, 0), (a, HALF, 0), (-HALF, a, 1), (HALF, a, 1)):
            put((x, tops[(x, z)] + 7, z), "VFX_Eldanior_Kingdom_Barrier")
            ROT[(x, tops[(x, z)] + 7, z)] = rot
    occupied = set()

    def free(x, z, r=2):
        return all((x + dx, z + dz) not in occupied for dx in range(-r, r + 1) for dz in range(-r, r + 1))

    # --- parcelles de tombes cloturees ---
    plots = []
    for _ in range(400):
        if len(plots) >= 9:
            break
        w, d = rng.randint(12, 20), rng.randint(10, 16)
        cx, cz = rng.randint(-80, 80), rng.randint(-80, 80)
        r = math.hypot(cx, cz)
        if r < 38 or r > 85 or abs(cx) < w // 2 + 4 and cz < 0:
            continue
        box = (cx - w // 2, cz - d // 2, cx + w // 2, cz + d // 2)
        if any(not (box[2] + 4 < b[0] or b[2] + 4 < box[0] or box[3] + 4 < b[1] or b[3] + 4 < box[1]) for b in plots):
            continue
        plots.append(box)
    graves = 0
    for (x0, z0, x1, z1) in plots:
        gate = rng.choice(("n", "s", "e", "w"))
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                occupied.add((x, z))
                edge = x in (x0, x1) or z in (z0, z1)
                mid = (x == (x0 + x1) // 2 and gate in "ns") or (z == (z0 + z1) // 2 and gate in "ew")
                if edge and not (mid and ((gate == "n" and z == z0) or (gate == "s" and z == z1) or (gate == "w" and x == x0) or (gate == "e" and x == x1))):
                    put((x, tops[(x, z)] + 1, z), "Wood_Blackwood_Fence")
        for gx in range(x0 + 2, x1 - 1, 3):
            for gz in range(z0 + 2, z1 - 2, 4):
                if rng.random() < 0.15:
                    continue
                h = tops[(gx, gz)]
                kind = rng.random()
                if kind < 0.55:
                    put((gx, h + 1, gz), "Rock_Stone_Brick_Pillar_Base"); put((gx, h + 2, gz), "Rock_Stone_Brick_Smooth_Half")
                elif kind < 0.8:
                    put((gx, h + 1, gz), "Rock_Stone_Brick_Mossy" if rng.random() < 0.5 else "Rock_Stone_Brick_Smooth")
                else:
                    put((gx, h + 1, gz), "Rock_Stone_Brick_Pillar_Base"); put((gx, h + 2, gz), "Rock_Stone_Brick_Pillar_Middle")
                    put((gx - 1, h + 2, gz), "Rock_Stone_Brick_Half"); put((gx + 1, h + 2, gz), "Rock_Stone_Brick_Half")
                for dz in (1, 2):
                    put((gx, tops[(gx, gz + dz)], gz + dz), "Soil_Mud" if rng.random() < 0.5 else "Soil_Dirt", force=True)
                graves += 1
    # --- tombes isolees dans la foret + petits tombeaux vanilla ---
    for _ in range(70):
        x, z = rng.randint(-95, 95), rng.randint(-95, 95)
        if math.hypot(x, z) < 34 or avenue(x, z) or not free(x, z, 1):
            continue
        h = tops[(x, z)]
        put((x, h + 1, z), rng.choice(("Rock_Stone_Brick_Mossy", "Rock_Stone_Brick_Pillar_Base", "Rubble_Stone_Medium")))
        occupied.add((x, z)); graves += 1
    tombs = 0
    for _ in range(60):
        if tombs >= 10:
            break
        x, z = rng.randint(-88, 88), rng.randint(-88, 88)
        if math.hypot(x, z) < 40 or abs(x) < 8 or not free(x, z, 6):
            continue
        d = load("Dungeon/Cursed_Crypt/Entrance/Parts/Tomb_Small/Crypt_Entrance_Parts_Tomb_Small_00" + str(rng.randint(1, 2)))
        h = tops[(x, z)]
        for b in d["blocks"]:
            if b["name"] != "Empty":
                put((b["x"] + x, b["y"] + h + 1, b["z"] + z), b["name"], force=True)
                if "rotation" in b:
                    ROT[(b["x"] + x, b["y"] + h + 1, b["z"] + z)] = b["rotation"]
        for dx in range(-6, 7):
            for dz in range(-5, 8):
                occupied.add((x + dx, z + dz))
        tombs += 1
    # --- ruines : pans de mur effondres ---
    for _ in range(10):
        x, z = rng.randint(-85, 85), rng.randint(-85, 85)
        if math.hypot(x, z) < 40 or not free(x, z, 4):
            continue
        along = rng.random() < 0.5
        for i in range(rng.randint(4, 8)):
            hgt = rng.randint(1, 4)
            cx, cz = (x + i, z) if along else (x, z + i)
            for y in range(hgt):
                put((cx, tops[(cx, cz)] + 1 + y, cz), "Rock_Stone_Brick_Mossy" if rng.random() < 0.6 else "Rock_Stone_Brick")
            occupied.add((cx, cz))
    # --- lanternes le long de l'allee ---
    for z in range(-92, -8, 8):
        for x in (-3, 3):
            h = tops[(x, z)]
            put((x, h + 1, z), "Rock_Stone_Brick_Pillar_Base"); put((x, h + 2, z), "Rock_Stone_Brick_Pillar_Middle"); put((x, h + 3, z), "Deco_Lantern")
            occupied.add((x, z))
    for x in range(-3, 4):
        for z in range(-97, -4):
            occupied.add((x, z))
    # --- foret : plus dense vers les bords ---
    pool = tree_pool(TREES)
    total = sum(w for _, w, _ in pool)
    trees = 0
    lim = HALF + OUTER - 6
    for gx in range(-lim, lim + 1, 7):
        for gz in range(-lim, lim + 1, 7):
            x, z = gx + rng.randint(-2, 2), gz + rng.randint(-2, 2)
            r = math.hypot(x, z)
            outside = abs(x) > HALF - 3 or abs(z) > HALF - 3
            if outside and (abs(x) < HALF + 5 and abs(z) < HALF + 5):      # pas dans le mur
                continue
            p = 0.75 if outside else max(0.12, min(0.85, (r - 30) / 45))
            if r < 30 or rng.random() > p or not free(x, z, 2) or building(x, z):
                continue
            pick_w = rng.random() * total
            for fam, w, variants in pool:
                pick_w -= w
                if pick_w <= 0:
                    break
            ax, ay, az, blocks = rng.choice(variants)
            h = tops[(x, z)]
            for b in blocks:
                k = (b["x"] - ax + x, b["y"] - ay + h + 1, b["z"] - az + z)
                inside_tree = abs(x) <= HALF and abs(z) <= HALF
                if b["name"] == "Empty" or (inside_tree and (abs(k[0]) > HALF or abs(k[2]) > HALF)) \
                        or (not inside_tree and abs(k[0]) <= HALF + 1 and abs(k[2]) <= HALF + 1) \
                        or abs(k[0]) > HALF + OUTER or abs(k[2]) > HALF + OUTER:
                    continue
                if k in out and not out[k].startswith(("Soil", "Rock_Stone", "Plant")):
                    continue
                put(k, b["name"], force=k[1] > h or out.get(k, "").startswith("Soil"))
                if "rotation" in b:
                    ROT[k] = b["rotation"]
            occupied.add((x, z)); trees += 1
    # --- vegetation morte, os, champignons ---
    for x in range(-HALF - OUTER, HALF + OUTER + 1):
        for z in range(-HALF - OUTER, HALF + OUTER + 1):
            h = tops[(x, z)]
            k = (x, h + 1, z)
            if (x, z) in occupied or k in out or k in world or avenue(x, z) or ring(x, z) or building(x, z):
                continue
            rr = rng.random()
            if rr < 0.20:
                out[k] = check(rng.choice(("Plant_Grass_Dry", "Plant_Grass_Dry", "Plant_Grass_Sharp", "Plant_Bush_Dead",
                                           "Plant_Bramble_Dead_Twisted", "Plant_Grass_Dry")))
            elif rr < 0.212:
                out[k] = check(rng.choice(("Deco_Bone_Pile", "Deco_Bone_Skulls", "Rubble_Stone")))
    # --- sol praticable (points d'apparition) ---
    for x in range(-HALF + 3, HALF - 2):
        for z in range(-HALF + 3, HALF - 2):
            h = tops[(x, z)]
            if (x, z) in occupied or building(x, z):
                continue
            if all(((x, h + dy, z) not in out or out[(x, h + dy, z)].startswith("Plant_Grass")) and (x, h + dy, z) not in world for dy in (1, 2)):
                floor.append((x, h + 1, z))
    print(f"     surface : {len(plots)} parcelles, {graves} tombes, {tombs} tombeaux, {trees} arbres")
    return out, floor


ROT = {}   # rotation des blocs poses par la couche sceau (remis a zero pour chaque donjon)


def build(D):
    ROT.clear()
    rng = random.Random(1503 + len(D["id"]))
    axis = D["axis"]
    ai = 0 if axis == "x" else 2
    world, owner, pieces = {}, {}, []
    pos = (0, 0, 0)
    junctions = []

    for i, (room, rel, opt) in enumerate(D["chain"]):
        d = load(rel)
        ax, ay, az = d.get("anchorX", 0), d.get("anchorY", 0), d.get("anchorZ", 0)
        same = opt == "same"
        dx, dy, dz = pos[0] - ax, pos[1] - ay, pos[2] - az
        blocks = d["blocks"]
        for b in blocks:
            k = (b["x"] + dx, b["y"] + dy, b["z"] + dz)
            if world.get(k) == "Empty" and b["name"] != "Empty" and owner.get(k) is not None and owner[k] != i and not same:
                continue                                                         # l'air de la piece precedente reste creuse
            world[k] = b["name"]
            owner[k] = i
        ents = []
        for e in d.get("entities", []):
            c = e.get("Components", {})
            t = c.get("Transform", {}).get("Position")
            m = (c.get("Model", {}).get("Model") or {}).get("Id")
            if t and m and m != "NPC_Path_Marker":
                ents.append({"model": m, "x": t["X"] + dx, "y": t["Y"] + dy, "z": t["Z"] + dz})
        xs = [b["x"] + dx for b in blocks]; ys = [b["y"] + dy for b in blocks]; zs = [b["z"] + dz for b in blocks]
        pieces.append({"room": room, "prefab": rel + ".prefab.json", "x": pos[0], "y": pos[1], "z": pos[2],
                       "min": [min(xs), min(ys), min(zs)], "max": [max(xs), max(ys), max(zs)], "entities": ents, "shift": (dx, dy, dz)})
        if same or i + 1 == len(D["chain"]):
            continue
        if isinstance(opt, dict) and "exit" in opt:
            ex, ey, ez = opt["exit"]
            nxt = (ex, ey, ez - 1) if axis == "z" else (ex - 1, ey, ez)
            face = ez if axis == "z" else ex
            center = (ex if axis == "z" else ez)
        else:
            door = door_on_face(blocks, axis, -1)
            if door is None:
                raise SystemExit(f"{rel} : pas de porte de sortie")
            at, u0, u1, y0, _ = door
            face = at + (dz if axis == "z" else dx)
            center = (u0 + u1) // 2 + (dx if axis == "z" else dz)
            floor_y = y0 - 1 + dy
            nxt = (center, floor_y, face - 1) if axis == "z" else (face - 1, floor_y, center)
        junctions.append((face, center, nxt[1]))
        pos = nxt
        print(f"  {i:2d} {room:12s} ancre {pieces[-1]['x']:5d} {pieces[-1]['y']:4d} {pieces[-1]['z']:4d}  -> sortie {nxt}")
    print(f"  {len(pieces) - 1:2d} {pieces[-1]['room']:12s} ancre {pieces[-1]['x']:5d} {pieces[-1]['y']:4d} {pieces[-1]['z']:4d}")

    overlay = {}
    # passages creuses aux jonctions (5 de large, 5 de haut, 4 de profondeur)
    for face, center, fy in junctions:
        for a in range(face - 2, face + 2):
            for u in range(center - 2, center + 3):
                for y in range(fy + 1, fy + 6):
                    k = (u, y, a) if axis == "z" else (a, y, u)
                    if world.get(k) != "Empty":
                        overlay[k] = "Empty"
                    world.setdefault(k, "Empty"); owner.setdefault(k, None)
                    if world[k] != "Empty":
                        world[k] = "Empty"
    # coffres et generateurs du jeu retires
    removed = 0
    for k, n in list(world.items()):
        if any(w in n.lower() for w in REMOVE_WORDS):
            overlay[k] = "Empty"; world[k] = "Empty"; removed += 1
    # surface (cimetiere)
    surf_floor = []
    if D.get("surface"):
        surf, surf_floor = cemetery(world, rng)
        overlay.update(surf)
    # sceau : roche autour de l'air et des blocs non pleins qui touchent le vide (sous le sol seulement pour un cimetiere)
    top_y = -5 if D.get("surface") else 10 ** 6
    seal = 0
    for (x, y, z), n in list(world.items()):
        if n != "Empty" and is_solid(n):
            continue
        for ddx in (-1, 0, 1):
            for ddy in (-1, 0, 1):
                for ddz in (-1, 0, 1):
                    k = (x + ddx, y + ddy, z + ddz)
                    if k[1] > top_y or k in world or k in overlay:
                        continue
                    overlay[k] = SEAL; seal += 1
    # barriere : air du plan d'entree de la salle du boss
    boss_piece = pieces[-1]
    plane = boss_piece["x"] if axis == "x" else boss_piece["z"]
    barrier = [list(k) for k, n in world.items() if n == "Empty" and k[ai] == plane and owner.get(k) == len(pieces) - 1]
    if not barrier:                                       # plan d'entree vide : la face juste devant
        barrier = [list(k) for k, n in world.items() if n == "Empty" and k[ai] == plane + 1]
    # 2026-11-23 : plus de Sceau pose (instances fixes, monstres des balises NPC_Spawn_Marker de l'utilisateur) ; l'emplacement
    # reste dans le plan, le bloc Eldanior_Faille_Barriere reste disponible en creatif.

    def final(k):
        return overlay.get(k, world.get(k))

    def walkable(k):
        x, y, z = k
        below = final((x, y - 1, z))
        return final(k) == "Empty" and final((x, y + 1, z)) == "Empty" and below is not None and below != "Empty" \
            and is_solid(below)

    # salles
    rooms = collections.OrderedDict()
    if D.get("surface"):
        xs = [k[0] for k in surf_floor]; zs = [k[2] for k in surf_floor]
        rooms["cimetiere"] = {"pieces": [], "min": [min(xs), -8, min(zs)], "max": [max(xs), 30, max(zs)], "floor": list(surf_floor)}
    for i, p in enumerate(pieces):
        r = rooms.setdefault(p["room"], {"pieces": [], "min": p["min"][:], "max": p["max"][:], "floor": []})
        r["pieces"].append(i)
        r["min"] = [min(a, b) for a, b in zip(r["min"], p["min"])]
        r["max"] = [max(a, b) for a, b in zip(r["max"], p["max"])]
    for k, i in owner.items():
        if i is not None and walkable(k):
            rooms[pieces[i]["room"]]["floor"].append(k)

    out_rooms = []
    for rid, r in rooms.items():
        cfg = D["rooms"][rid] if rid in D["rooms"] else {"name": rid, "mobs": []}
        if rid not in D["rooms"]:
            D["rooms"][rid] = cfg
        floor = r["floor"]
        if not floor:
            raise SystemExit(f"{D['id']} : salle {rid} sans sol praticable")
        cand = floor[:]
        rng.shuffle(cand)
        pts = []
        sp = cfg.get("spacing", 3) ** 2
        for k in cand:
            if all((k[0] - q[0]) ** 2 + (k[2] - q[2]) ** 2 >= sp for q in pts):
                pts.append(k)
            if len(pts) >= 60:
                break
        cx = sum(k[0] for k in floor) / len(floor); cz = sum(k[2] for k in floor) / len(floor)
        center = min(floor, key=lambda k: (k[0] - cx) ** 2 + (k[2] - cz) ** 2)
        o = {"id": rid, "name": cfg["name"], "safe": cfg.get("safe", False), "min": r["min"], "max": r["max"],
             "center": list(center), "spawns": [list(k) for k in pts], "mobs": cfg.get("mobs", []), "stars": cfg.get("stars", 0)}
        for key in ("miniboss", "boss", "reinforcements"):
            if key in cfg:
                o[key] = cfg[key]
        out_rooms.append(o)
        print(f"     salle {rid:12s} sol {len(floor):5d} points {len(pts):3d} centre {center}")
    roommap = {r["id"]: r for r in out_rooms}

    def nearest(rid, t, cond=None):
        fl = [k for k in rooms[rid]["floor"] if cond is None or cond(k)]
        return list(min(fl, key=lambda k: (k[0] - t[0]) ** 2 + (k[1] - t[1]) ** 2 * 4 + (k[2] - t[2]) ** 2))

    sgn = -1                                                                      # on avance vers -X ou -Z
    if D.get("surface"):
        arrival = nearest("cimetiere", (0, 0, -92))                         # au nord, au bout de l'allee
        exit_start = nearest("cimetiere", (6, 0, -94))
    else:
        first = pieces[0]
        arrival = nearest(first["room"], (first["x"] - 8, first["y"] + 1, first["z"]))
        exit_start = nearest(first["room"], (first["x"] - 3, first["y"] + 1, first["z"] + 7))
    bp = pieces[-1]
    marker = next((e for e in bp["entities"] if e["model"] in ("Goblin_Duke", "NPC_Spawn_Marker")), None)
    boss_at = nearest("boss", (marker["x"], marker["y"], marker["z"]) if marker else tuple(roommap["boss"]["center"]))
    # sortie : dans la salle du boss, 6 blocs apres la barriere ; coffres : 4 a 9 blocs du boss, du cote le plus profond
    bmid = [sum(k[i] for k in barrier) / len(barrier) for i in range(3)]
    exit_end = nearest("boss", (bmid[0] + (6 * sgn if axis == "x" else 0), bmid[1], bmid[2] + (6 * sgn if axis == "z" else 0)))
    chests = []
    cand = sorted(rooms["boss"]["floor"], key=lambda k: k[ai] * -sgn)
    cand = [k for k in cand if 16 <= (k[0] - boss_at[0]) ** 2 + (k[2] - boss_at[2]) ** 2 <= 81 and final((k[0], k[1] + 2, k[2])) == "Empty"]
    cand.sort(key=lambda k: (k[ai] - boss_at[ai]) * sgn, reverse=True)
    for k in cand:
        if all(abs(k[0] - q[0]) + abs(k[2] - q[2]) >= 3 for q in chests):
            chests.append(k)
        if len(chests) == 2:
            break

    # ---- porte de boss (porte du mod, cle de boss selon le rang) : posee par le code dans l'entree de la salle du boss ----
    plane_cells = [tuple(k) for k in barrier]
    us = sorted({k[2] if axis == "x" else k[0] for k in plane_cells})
    fy = min(k[1] for k in plane_cells)
    width = len(us)
    if axis == "z":                                    # rotation 0 : cases x-1 .. x+2 (vu au serveur isole)
        rot = 0
        main_u = us[0] + 1 + max(0, (width - 4) // 2)
        foot = {(main_u + d, fy + h) for d in range(-1, 3) for h in range(4)}
    else:                                              # rotation 1 : cases z-2 .. z+1
        rot = 1
        main_u = us[0] + 2 + max(0, (width - 4) // 2)
        foot = {(main_u + d, fy + h) for d in range(-2, 2) for h in range(4)}
    plane = plane_cells[0][ai]
    for k in plane_cells:                              # le reste de l'ouverture est mure
        u = k[2] if axis == "x" else k[0]
        if (u, k[1]) not in foot:
            overlay[k] = "Rock_Stone_Brick" if D.get("surface") else SEAL
    door = {"block": D["door"], "x": main_u if axis == "z" else plane, "y": fy, "z": plane if axis == "z" else main_u, "rotation": rot}
    # ---- coffres : cle de boss dans la salle "keychest", coffres au tresor pres du boss (butin propre a chaque joueur) ----
    chest_list = []
    for k in chests:
        overlay[tuple(k)] = "ChestTreasureDungeon"
        chest_list.append({"x": k[0], "y": k[1], "z": k[2], "block": "ChestTreasureDungeon", "table": D["chestTable"]})
    for rid, cfg in D["rooms"].items():
        if cfg.get("keychest") and rid in rooms:
            c = roommap[rid]["center"]
            kc = nearest(rid, tuple(c), lambda k: final((k[0], k[1] + 1, k[2])) == "Empty" and final((k[0], k[1] + 2, k[2])) == "Empty")
            overlay[tuple(kc)] = D["keyChest"][0]
            chest_list.append({"x": kc[0], "y": kc[1], "z": kc[2], "block": D["keyChest"][0], "table": D["keyChest"][1]})
    # ---- generateurs de monstres : le bloc unique Eldanior_Generateur_Monstres (failles/Generateurs), reglage {role, count}
    #      par case ; ce sont les reglages de depart d'un modele neuf (ou remis a zero), retouchables en jeu (F sur le bloc) ----
    GEN = "Eldanior_Generateur_Monstres"
    generateurs = {}
    taken = {tuple(k) for k in [arrival, exit_start, boss_at]} | {(c["x"], c["y"], c["z"]) for c in chest_list}
    for o in out_rooms:
        pts = [tuple(k) for k in o["spawns"] if tuple(k) not in taken
               and (k[0] - arrival[0]) ** 2 + (k[2] - arrival[2]) ** 2 >= 25 ** 2
               and final((k[0], k[1] + 1, k[2])) in (None, "Empty")]
        i = 0
        for m in o["mobs"]:
            left = m[1]
            while left > 0 and i < len(pts):
                k = pts[i]; i += 1
                n = min(3, left); left -= n
                overlay[k] = GEN
                generateurs[f"{k[0]}|{k[1]}|{k[2]}"] = {"role": m[0], "count": n}
    bcfg = D["rooms"]["boss"]
    overlay[tuple(boss_at)] = GEN
    generateurs[f"{boss_at[0]}|{boss_at[1]}|{boss_at[2]}"] = {"role": bcfg["boss"][0], "count": 1}
    print(f"  generateurs : {len(generateurs)} (boss {bcfg['boss'][0]}) ; porte {door} ; coffres {[c['block'] for c in chest_list]}")

    # parcours de l'air : la salle du boss doit etre atteignable depuis l'arrivee (porte ouverte)
    def passable(k):
        n = final(k)
        if n == BARRIER or (k[ai] == plane and ((k[2] if axis == "x" else k[0]), k[1]) in foot):
            return True
        if n is None:
            return D.get("surface") and -6 <= k[1] <= 40 and abs(k[0]) <= HALF and abs(k[2]) <= HALF
        return n == "Empty" or not is_solid(n)
    seen = {tuple(arrival)}
    q = collections.deque([tuple(arrival)])
    goal = tuple(boss_at)
    while q and goal not in seen and len(seen) < 3_000_000:
        x, y, z = q.popleft()
        for n in ((x + 1, y, z), (x - 1, y, z), (x, y + 1, z), (x, y - 1, z), (x, y, z + 1), (x, y, z - 1)):
            if n not in seen and passable(n):
                seen.add(n); q.append(n)
    reach = goal in seen
    print(f"  boss atteignable depuis l'arrivee : {'OUI' if reach else 'NON'} ({len(seen)} cases d'air parcourues)")
    if not reach:
        raise SystemExit("la salle du boss n'est pas atteignable : corriger la chaine")

    layout = {
        "id": D["id"], "name": D["name"], "rank": D["rank"], "theme": D["theme"], "instance": D["instance"],
        "minutes": D["minutes"], "keyName": D["keyName"],
        "overlay": f"Eldanior/Failles/{D['Id']}_Sceau.prefab.json",
        "pieces": [{"prefab": p["prefab"], "x": p["x"], "y": p["y"], "z": p["z"], "room": p["room"]} for p in pieces],
        "min": [min([p["min"][i] for p in pieces] + [k[i] for k in overlay]) - 1 for i in range(3)],
        "max": [max([p["max"][i] for p in pieces] + [k[i] for k in overlay]) + 1 for i in range(3)],
        "arrival": arrival, "exitStart": exit_start, "exitEnd": exit_end, "boss": boss_at,
        "chests": chest_list, "door": door, "generateurs": generateurs,
        "bossRole": bcfg["boss"][0], "bossName": bcfg["boss"][3],
        "rooms": out_rooms, "overflow": D["overflow"],
    }
    blocks = []
    for k, v in sorted(overlay.items()):
        b = {"x": k[0], "y": k[1], "z": k[2], "name": v}
        if k in ROT:
            b["rotation"] = ROT[k]
        blocks.append(b)
    ref = load(D["chain"][0][1])
    pref = {"version": ref.get("version", 8), "blockIdVersion": ref.get("blockIdVersion", 11), "anchorX": 0, "anchorY": 0,
            "anchorZ": 0, "blocks": blocks}
    p_out = os.path.join(RES, "Server", "Prefabs", "Eldanior", "Failles", f"{D['Id']}_Sceau.prefab.json")
    os.makedirs(os.path.dirname(p_out), exist_ok=True)
    with open(p_out, "w", encoding="utf-8") as f:
        json.dump(pref, f, separators=(",", ":"))
    l_out = os.path.join(RES, "eldanior", "failles", f"{D['id']}.json")
    os.makedirs(os.path.dirname(l_out), exist_ok=True)
    with open(l_out, "w", encoding="utf-8") as f:
        json.dump(layout, f, ensure_ascii=False, indent=1)
    print(f"  sceau {seal} roches, {removed} coffres retires, barriere {len(barrier)}, couche {len(overlay)} blocs ; "
          f"coffres {chests}, boss {boss_at}, arrivee {arrival}")

    # apercu vu de dessus (couleur = piece, plus clair = plus haut ; blanc = points d'apparition)
    os.makedirs(PREVIEW, exist_ok=True)
    x0, x1, z0, z1 = layout["min"][0], layout["max"][0], layout["min"][2], layout["max"][2]
    S = 3
    W, H = (x1 - x0 + 1) * S, (z1 - z0 + 1) * S
    px = [[[12, 12, 24, 255] for _ in range(W)] for _ in range(H)]
    pal = [(90, 160, 220), (220, 160, 90), (120, 200, 120), (200, 110, 200), (230, 220, 100), (100, 220, 210), (230, 120, 110),
           (170, 170, 240), (240, 170, 200), (200, 200, 200)]

    def paint(x, z, col):
        for a in range(S):
            for b in range(S):
                X, Z = (x - x0) * S + a, (z - z0) * S + b
                if 0 <= X < W and 0 <= Z < H:
                    px[Z][X] = col
    topc = {}
    for k, n in overlay.items():                                                  # surface d'abord
        if n not in ("Empty", SEAL) and (k[0], k[2]) not in topc or (n not in ("Empty", SEAL) and k[1] > topc[(k[0], k[2])][0]):
            topc[(k[0], k[2])] = (k[1], n)
    for (x, z), (y, n) in topc.items():
        col = [70, 60, 50] if n.startswith("Soil") else [110, 110, 115] if n.startswith("Rock") else [150, 120, 80] if "Wood" in n \
            else [180, 180, 190]
        paint(x, z, col + [255])
    air = {}
    for k, n in world.items():
        if n == "Empty" and owner.get(k) is not None:
            c = (k[0], k[2])
            if c not in air or k[1] < air[c][0]:
                air[c] = (k[1], owner[k])
    ymin, ymax = layout["min"][1], layout["max"][1]
    for (x, z), (y, i) in air.items():
        t = 0.45 + 0.55 * (y - ymin) / max(1, ymax - ymin)
        paint(x, z, [int(v * t) for v in pal[i % len(pal)]] + [255])

    def dot(k, col, r=1):
        for a in range(-r, r + 1):
            for b in range(-r, r + 1):
                paint(k[0] + a, k[2] + b, col + [255])
    for r in out_rooms:
        for k in r["spawns"]:
            dot(k, [255, 255, 255], 0)
    for k in barrier:
        dot(k, [255, 0, 255], 0)
    dot(arrival, [0, 255, 0]); dot(exit_start, [0, 150, 255]); dot(exit_end, [0, 150, 255]); dot(boss_at, [255, 0, 0], 2)
    for k in chests:
        dot(k, [255, 215, 0])
    out_png = os.path.join(PREVIEW, f"{D['id']}.png")
    png.write(out_png, W, H, px)
    print("  apercu :", out_png)


def build_exterior(D):
    """Donjon « exterieur seul » (rang D) : le biome, ses generateurs de monstres et le boss provisoire ; pas de pieces, de porte
    ni de coffres (l'utilisateur construit le donjon et pose ses coffres au tresor en edition)."""
    sys.path.insert(0, HERE)
    import biome_caldeira
    rng = random.Random(1503 + len(D["id"]) + D.get("seed", 0))
    out, rot, fill, fluids, floor, marks = biome_caldeira.caldera(D, rng, load, check, VANILLA)
    rid = D["surfaceRoom"]
    cfg = D["rooms"][rid]
    ec, er = marks["esplanade"]["center"], marks["esplanade"]["radius"]
    arrival, exit_start, boss_at, exit_end = (list(marks[k]) for k in ("arrival", "exitStart", "boss", "exitEnd"))
    cand = [k for k in floor if math.hypot(k[0] - ec[0], k[2] - ec[2]) > er + 4
            and (k[0] - arrival[0]) ** 2 + (k[2] - arrival[2]) ** 2 >= 25 ** 2]
    rng.shuffle(cand)
    pts, sp = [], cfg.get("spacing", 3) ** 2
    for k in cand:
        if all((k[0] - q[0]) ** 2 + (k[2] - q[2]) ** 2 >= sp for q in pts):
            pts.append(k)
    GEN = "Eldanior_Generateur_Monstres"
    generateurs, i = {}, 0
    for m in cfg["mobs"]:
        left = m[1]
        while left > 0 and i < len(pts):
            k = pts[i]; i += 1
            n = min(3, left); left -= n
            out[k] = GEN
            generateurs[f"{k[0]}|{k[1]}|{k[2]}"] = {"role": m[0], "count": n}
    out[tuple(boss_at)] = GEN
    generateurs[f"{boss_at[0]}|{boss_at[1]}|{boss_at[2]}"] = {"role": cfg["boss"][0], "count": 1}
    ks = list(out)
    lo = [min(k[i] for k in ks) - 1 for i in range(3)]
    hi = [max(k[i] for k in ks) + 1 for i in range(3)]
    room = {"id": rid, "name": cfg["name"], "safe": False, "min": [lo[0], lo[1], lo[2]], "max": [hi[0], hi[1], hi[2]],
            "center": [ec[0], 1, ec[2]], "spawns": [list(k) for k in pts[:60]], "mobs": cfg["mobs"], "stars": cfg.get("stars", 0)}
    for key in ("boss", "reinforcements"):
        if key in cfg:
            room[key] = cfg[key]
    layout = {
        "id": D["id"], "name": D["name"], "rank": D["rank"], "theme": D["theme"], "instance": D["instance"],
        "minutes": D["minutes"], "keyName": D["keyName"],
        "overlay": f"Eldanior/Failles/{D['Id']}_Sceau.prefab.json",
        "pieces": [], "min": lo, "max": hi,
        "arrival": arrival, "exitStart": exit_start, "exitEnd": exit_end, "boss": boss_at,
        "chests": [], "generateurs": generateurs, "bossRole": cfg["boss"][0], "bossName": cfg["boss"][3],
        "esplanade": marks["esplanade"], "rooms": [room], "overflow": D["overflow"],
    }
    blocks = []
    for k, v in sorted(out.items()):
        b = {"x": k[0], "y": k[1], "z": k[2], "name": v}
        if k in rot:
            b["rotation"] = rot[k]
        if k in fill:
            b["filler"] = fill[k]
        blocks.append(b)
    pref = {"version": 8, "blockIdVersion": 11, "anchorX": 0, "anchorY": 0, "anchorZ": 0, "blocks": blocks, "fluids": fluids}
    p_out = os.path.join(RES, "Server", "Prefabs", "Eldanior", "Failles", f"{D['Id']}_Sceau.prefab.json")
    with open(p_out, "w", encoding="utf-8") as f:
        json.dump(pref, f, separators=(",", ":"))
    l_out = os.path.join(RES, "eldanior", "failles", f"{D['id']}.json")
    with open(l_out, "w", encoding="utf-8") as f:
        json.dump(layout, f, ensure_ascii=False, indent=1)
    print(f"  {len(blocks)} blocs, {len(fluids)} cases de lave, {len(generateurs)} generateurs "
          f"({sum(g['count'] for g in generateurs.values())} monstres dont le boss) ; arrivee {arrival}, boss provisoire {boss_at}")

    # apercu vu de dessus, 1 px par bloc (couleur du bloc le plus haut ; lave en orange, generateurs en blanc)
    os.makedirs(PREVIEW, exist_ok=True)
    top = {}
    for (x, y, z), n in out.items():
        if n in ("Barrier",) or (x, z) in top and top[(x, z)][0] >= y:
            continue
        top[(x, z)] = (y, n)
    lava = {(f["x"], f["z"]) for f in fluids}
    W, Hh = hi[0] - lo[0] + 1, hi[2] - lo[2] + 1
    px = [[[10, 8, 8, 255] for _ in range(W)] for _ in range(Hh)]
    ymin, ymax = min(y for y, _ in top.values()), max(y for y, _ in top.values())

    def col(n):
        if n == GEN: return (255, 255, 255)
        if "Lava" in n or "Incandescent" in n: return (230, 90, 20)
        if n.startswith("Rock_Basalt"): return (60, 66, 84)
        if n.startswith("Rock_Magma"): return (90, 50, 40)
        if n.startswith(("Rock_Volcanic", "Rock_Slate")): return (70, 62, 60)
        if n == "Soil_Sand_Ashen": return (150, 140, 125)
        if n.startswith("Soil_Ash"): return (118, 110, 104)
        if n.startswith("Soil"): return (95, 85, 75)
        if "Fire" in n: return (255, 150, 40)
        if "Crystal" in n: return (230, 30, 40)
        if n.startswith("VFX"): return (200, 200, 255)
        return (130, 105, 80)
    for (x, z), (y, n) in top.items():
        c = (240, 110, 20) if (x, z) in lava else col(n)
        t = 0.55 + 0.45 * (y - ymin) / max(1, ymax - ymin)
        px[z - lo[2]][x - lo[0]] = [int(v * t) for v in c] + [255]
    png.write(os.path.join(PREVIEW, f"{D['id']}.png"), W, Hh, px)
    print("  apercu :", os.path.join(PREVIEW, f"{D['id']}.png"))


if __name__ == "__main__":
    for D in DUNGEONS:
        print(f"== {D['name']} (rang {D['rank']})")
        build_exterior(D) if D.get("exterior") else build(D)
