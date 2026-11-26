"""Failles-donjons, rang D : le biome exterieur « La Caldeira » (2026-11-24, demande de l'utilisateur : un biome propre au donjon,
qui ne ressemble pas au cimetiere du rang F ; l'utilisateur construit lui-meme le donjon, on fournit l'environnement).

Une cuvette de cendres fermee par une falaise volcanique (au-dela : des sommets dechiquetes, decor), traversee par une riviere de
lave dans une gorge qui se jette dans un lac de lave ; orgues basaltiques, arches et pics volcaniques du jeu, ruines calcinees,
cristaux rouges, fumerolles, bois tordus en feu, ossements. Au centre : une ESPLANADE vide et plate (le chantier du donjon).
Le mur Barrier invisible suit le bord de la zone (pas de panneaux d'energie : la falaise ferme deja la vue).

Utilise par gen_donjons.py (D["exterior"] == "caldeira") : caldera(D, rng, load, check, solid_names) renvoie
(blocs, rotations, cases de remplissage, fluides, sol praticable, reperes).
"""
import glob, math, os

HALF = 100          # zone jouable : -100 .. +100
OUTER = 60          # decor au-dela du mur
CX, CZ, CR = 0, 8, 36            # esplanade (chantier du donjon) : centre, rayon plat ; raccord jusqu'a CR + 12
ARRIVAL = (0, -60)               # arrivee des joueurs, au nord, face a l'esplanade
RIVER = [(-64, -28), (-52, -6), (-57, 22), (-38, 52), (-8, 67), (24, 63), (44, 54)]   # source -> lac
LAKE = (57, 47, 14, 10)          # centre x, z, demi-axes
BASALT = [(55, -48, 16), (-75, 2, 13)]                                                 # orgues : centre x, z, rayon
ARCHES = [(74, 8), (-38, -66)]                                                         # grandes arches de lave (est, nord-ouest)


def caldera(D, rng, load, check, vanilla_prefabs):
    out, rot, fill, fluids = {}, {}, {}, []
    reserved = set()             # colonnes ou l'on ne pose aucun decor (esplanade, arrivee, lave et ses berges)

    def noise(x, z):
        return math.sin(x * 0.37 + 0.4) * math.cos(z * 0.29 + 1.2) + 0.6 * math.sin((x - z) * 0.23) + 0.4 * math.sin(x * 0.11 + z * 0.17)

    def raw_height(x, z):
        r = math.hypot(x, z)
        th = math.atan2(z, x)
        rr = r * (1 + 0.07 * math.sin(3 * th + 1.1) + 0.04 * math.sin(5 * th + 2.3))
        h = 1.3 * math.sin(x * 0.083 + z * 0.041) + 0.9 * math.sin(z * 0.121 - x * 0.057 + 1.7) + 0.5 * math.sin((x + z) * 0.19)
        if rr > 68:                                                     # la falaise de la caldeira
            t = (rr - 68) / 32
            h += min(t, 1.6) ** 1.7 * 30 + t * noise(x, z) * 5
            if t > 1.6:                                                 # au-dela : sommets dechiquetes
                h += 6 * abs(noise(x * 0.7, z * 0.7)) * min(1.0, (t - 1.6) * 2)
        dc = math.hypot(x - CX, z - CZ)
        if dc < CR:
            h = 0
        elif dc < CR + 12:
            k = (dc - CR) / 12
            h = h * (k * k * (3 - 2 * k))
        return h

    R = HALF + OUTER
    H = {(x, z): round(raw_height(x, z)) for x in range(-R - 1, R + 2) for z in range(-R - 1, R + 2)}

    # ---------------------------------------------------------------- riviere et lac de lave (niveau unique : pas d'ecoulement)
    lava = set()

    def seg_dist(px, pz, a, b):
        ax, az = a; bx, bz = b
        dx, dz = bx - ax, bz - az
        t = max(0.0, min(1.0, ((px - ax) * dx + (pz - az) * dz) / (dx * dx + dz * dz)))
        return math.hypot(px - ax - t * dx, pz - az - t * dz), t

    for i in range(len(RIVER) - 1):
        a, b = RIVER[i], RIVER[i + 1]
        for x in range(min(a[0], b[0]) - 5, max(a[0], b[0]) + 6):
            for z in range(min(a[1], b[1]) - 5, max(a[1], b[1]) + 6):
                d, t = seg_dist(x, z, a, b)
                w = 1.6 + 0.9 * math.sin((i + t) * 2.1) ** 2
                if d <= w:
                    lava.add((x, z))
    lx, lz, la, lb = LAKE
    for x in range(lx - la - 1, lx + la + 2):
        for z in range(lz - lb - 1, lz + lb + 2):
            e = ((x - lx) / la) ** 2 + ((z - lz) / lb) ** 2 + 0.12 * math.sin(x * 0.7) * math.cos(z * 0.6)
            if e <= 1:
                lava.add((x, z))
    sx, sz = RIVER[0]                                                   # la source : une mare ronde
    for x in range(sx - 4, sx + 5):
        for z in range(sz - 4, sz + 5):
            if math.hypot(x - sx, z - sz) <= 3.6:
                lava.add((x, z))
    L = min(H[c] for c in lava) - 1                                     # niveau de la lave
    for c in lava:
        H[c] = L - 1                                                    # lit de la lave
    near_lava = {}
    for (x, z) in lava:
        for dx in range(-6, 7):
            for dz in range(-6, 7):
                k = (x + dx, z + dz)
                if k not in lava:
                    d = math.hypot(dx, dz)
                    if d < near_lava.get(k, 99):
                        near_lava[k] = d
    for k, d in near_lava.items():
        if d <= 3:
            reserved.add(k)
    reserved |= lava

    # esplanade et couloir d'arrivee : aucun decor
    for x in range(CX - CR - 6, CX + CR + 7):
        for z in range(CZ - CR - 6, CZ + CR + 7):
            if math.hypot(x - CX, z - CZ) <= CR + 4:
                reserved.add((x, z))
    for x in range(ARRIVAL[0] - 5, ARRIVAL[0] + 6):
        for z in range(ARRIVAL[1] - 6, CZ - CR + 2):
            reserved.add((x, z))

    def inside(x, z, m=0):
        return abs(x) <= HALF - m and abs(z) <= HALF - m

    # ---------------------------------------------------------------- terrain (colonnes : coque pleine jusqu'aux voisins les plus bas)
    def surface(x, z, h, slope):
        if (x, z) in lava:
            return "Rock_Magma_Cooled"
        d = near_lava.get((x, z))
        if d is not None and d <= 1.5:
            return "Rock_Volcanic_Cracked_Incandescent" if rng.random() < 0.35 else "Rock_Magma_Cooled"
        if d is not None and d <= 3.5:
            return rng.choice(("Rock_Magma_Cooled", "Soil_Magma_Cooled_Gravel", "Rock_Volcanic"))
        if slope >= 3 or h >= 22:
            return "Rock_Basalt" if noise(x * 0.5, z * 0.5) > 0.9 else "Rock_Volcanic"
        if slope == 2:
            return "Rock_Volcanic" if rng.random() < 0.6 else "Soil_Volcanic_Gravel"
        if math.hypot(x - CX, z - CZ) <= CR:                          # esplanade : cendre tassee et gravier
            return "Soil_Volcanic_Gravel" if rng.random() < 0.22 else "Soil_Ash"
        n = math.sin(x * 0.083 + z * 0.041) + 0.7 * math.sin(z * 0.121 - x * 0.057 + 1.7)
        if n > 1.1:
            return "Soil_Sand_Ashen"                                    # cretes des dunes de cendre
        if n < -1.2:
            return rng.choice(("Soil_Volcanic_Gravel", "Soil_Magma_Cooled_Gravel"))
        if noise(x, z) > 1.35:
            return "Rock_Volcanic"                                      # affleurements
        return "Soil_Ash"

    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            h = H[(x, z)]
            nb = [H[(x + 1, z)], H[(x - 1, z)], H[(x, z + 1)], H[(x, z - 1)]]
            slope = max(abs(h - v) for v in nb)
            low = min(min(nb) - 1, h - 2)
            if (x, z) in near_lava or (x, z) in lava:
                low = min(low, L - 2)
            top = surface(x, z, h, slope)
            out[(x, h, z)] = check(top)
            for y in range(low, h):
                soil = top.startswith("Soil") and y >= h - 2
                out[(x, y, z)] = "Soil_Dirt_Burnt" if soil else ("Rock_Basalt" if (y + x) % 7 == 0 and y < h - 3 else "Rock_Volcanic")
    for (x, z) in lava:
        out[(x, L - 1, z)] = "Rock_Volcanic_Cracked_Lava"
        fluids.append({"x": x, "y": L, "z": z, "name": "Lava_Source", "level": 1})

    # ---------------------------------------------------------------- mur : Barrier invisible (la falaise ferme deja la vue)
    top_wall = max(H[(x, z)] for x in range(-HALF - 1, HALF + 2) for z in (-HALF - 1, HALF + 1)) + 12
    top_wall = max(top_wall, max(H[(x, z)] for z in range(-HALF - 1, HALF + 2) for x in (-HALF - 1, HALF + 1)) + 12)
    for a in range(-HALF - 1, HALF + 2):
        for (x, z) in ((a, -HALF - 1), (a, HALF + 1), (-HALF - 1, a), (HALF + 1, a)):
            for y in range(H[(x, z)] + 1, top_wall):
                out[(x, y, z)] = "Barrier"

    # ---------------------------------------------------------------- orgues basaltiques (cellules de Voronoi a hauteurs differentes)
    cols = 0
    for (bx, bz, br) in BASALT:
        pts = []
        for gx in range(bx - br, bx + br + 1, 3):
            for gz in range(bz - br, bz + br + 1, 3):
                px, pz = gx + rng.uniform(-1, 1), gz + rng.uniform(-1, 1)
                d = math.hypot(px - bx, pz - bz)
                if d <= br:
                    pts.append((px, pz, max(1, round((1 - d / br) * 11 * (0.45 + 0.55 * rng.random())))))
        for x in range(bx - br, bx + br + 1):
            for z in range(bz - br, bz + br + 1):
                th = math.atan2(z - bz, x - bx)
                if (x, z) in reserved or not inside(x, z, 2) or math.hypot(x - bx, z - bz) > br * (0.82 + 0.18 * math.sin(3 * th + bx)):
                    continue
                best = min(pts, key=lambda p: (p[0] - x) ** 2 + (p[1] - z) ** 2)
                if math.hypot(best[0] - bx, best[1] - bz) > br:
                    continue
                h = H[(x, z)]
                for y in range(h + 1, h + 1 + best[2]):
                    out[(x, y, z)] = "Rock_Basalt"
                reserved.add((x, z))
                cols += 1

    # ---------------------------------------------------------------- prefabs du jeu (arches, pics, rochers, ruines, cristaux, bois)
    def files(rel, pattern="*.prefab.json"):
        return sorted(glob.glob(os.path.join(vanilla_prefabs, rel, "**", pattern), recursive=True))

    def loadf(path):
        import json
        with open(path, encoding="utf-8") as f:
            return json.load(f)

    REMAP = {"Rock_Stone": "Rock_Volcanic", "Rock_Stone_Cobble": "Rock_Volcanic_Cobble", "Rock_Stone_Brick": "Rock_Volcanic_Brick",
             "Plant_Bush_Crystal": "Plant_Bramble_Dead_Lavathorn", "Soil_Grass": "Soil_Ash", "Soil_Dirt": "Soil_Dirt_Burnt",
             "Plant_Grass_Sharp": None, "Plant_Grass_Dry": None}
    SKIP = ("chest", "coffer", "block_spawner", "prefab_spawner", "spawner")

    def place(path, x, z, sink=1, need_free=True, margin=1):
        d = loadf(path)
        blocks = [b for b in d["blocks"] if b["name"] != "Empty" and not any(w in b["name"].lower() for w in SKIP)]
        if not blocks:
            return False
        ax, az = d.get("anchorX", 0), d.get("anchorZ", 0)
        foot = {(b["x"] - ax + x, b["z"] - az + z) for b in blocks}
        if any(not inside(c[0], c[1], margin) for c in foot):
            return False
        if need_free and any(c in reserved for c in foot):
            return False
        ymin = min(b["y"] for b in blocks)
        base = min(H[c] for c in foot) + 1 - sink
        for b in blocks:
            k = (b["x"] - ax + x, b["y"] - ymin + base, b["z"] - az + z)
            n = REMAP.get(b["name"], b["name"])
            if n is None:
                continue
            out[k] = check(n)
            if "rotation" in b:
                rot[k] = b["rotation"]
            else:
                rot.pop(k, None)
            if "filler" in b:
                fill[k] = b["filler"]
            else:
                fill.pop(k, None)
        reserved.update(foot)
        return True

    counts = {}

    def scatter(name, paths, n, rmin=0, rmax=999, sink=1, tries=60, where=None):
        placed = 0
        for _ in range(n * tries):
            if placed >= n:
                break
            if where:
                x, z = where()
            else:
                x, z = rng.randint(-HALF + 6, HALF - 6), rng.randint(-HALF + 6, HALF - 6)
            r = math.hypot(x, z)
            if r < rmin or r > rmax or (x, z) in reserved:
                continue
            if place(rng.choice(paths), x, z, sink=sink):
                placed += 1
        counts[name] = placed

    arches = files("Rock_Formations/Arches/Wastes_Lava")
    for (x, z) in ARCHES:                                               # deux grandes arches sur la pente de la falaise
        for path in rng.sample(arches, len(arches)):
            if place(path, x, z, sink=6, need_free=False, margin=0):
                counts["arches de lave"] = counts.get("arches de lave", 0) + 1
                break
    scatter("arches de cendre", files("Rock_Formations/Arches/Wastes_Ash"), 2, rmin=48, rmax=70, sink=3)
    scatter("pics", files("Rock_Formations/Rocks/Volcanic/Spiked"), 26, rmin=40, rmax=92, sink=2)
    scatter("aiguilles", files("Cave/Stalagmites/Rock_Volcanic_Large", "*Floor*.prefab.json"), 18, rmin=42, sink=1)
    scatter("rochers geants", files("Rock_Formations/Rocks/Volcanic", "*Giant*.prefab.json"), 8, rmin=44, rmax=80, sink=3)
    scatter("rochers", files("Rock_Formations/Rocks/Volcanic", "*Large_50*.prefab.json"), 14, rmin=42, sink=2)
    scatter("ruines calcinees", files("Monuments/Incidental/Volcanic"), 7, rmin=44, rmax=86, sink=1, tries=200)

    def by_lava():
        k = rng.choice(list(near_lava))
        return k
    scatter("cristaux rouges", files("Rock_Formations/Rocks/Volcanic/Lava_Lakes_Small_Crystal"), 12, sink=1, where=by_lava)
    scatter("bois en feu", files("Plants/Twisted_Wood/Fire"), 14, rmin=40, sink=0)
    burnt = []
    for f in files("Trees/Burnt_dead")[:10]:
        burnt.append(f)
    scatter("troncs brules", burnt, 22, rmin=42, sink=0)
    scatter("ronces de lave", files("Plants/Bush/Dead_Lavathorn"), 30, rmin=38, sink=0)

    # ---------------------------------------------------------------- fumerolles (fumee du jeu sur une fissure incandescente)
    fum = 0
    for _ in range(400):
        if fum >= 18:
            break
        x, z = rng.randint(-88, 88), rng.randint(-88, 88)
        if (x, z) in reserved or math.hypot(x, z) < 40 or math.hypot(x, z) > 82:
            continue
        h = H[(x, z)]
        if any(abs(H[(x + dx, z + dz)] - h) > 1 for dx in (-1, 0, 1) for dz in (-1, 0, 1)):
            continue
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                out[(x + dx, H[(x + dx, z + dz)], z + dz)] = "Rock_Volcanic_Cracked_Lava" if dx == 0 or dz == 0 else "Rock_Magma_Cooled"
                reserved.add((x + dx, z + dz))
        out[(x, h + 1, z)] = check("VFX_Dungeon_Goblin_Smoke_Pipe_Large")
        fum += 1
    counts["fumerolles"] = fum

    # ---------------------------------------------------------------- petit decor : ronces, gravats, ossements, roseaux de lave
    for x in range(-HALF + 2, HALF - 1):
        for z in range(-HALF + 2, HALF - 1):
            h = H[(x, z)]
            k = (x, h + 1, z)
            if k in out or (x, z) in lava or math.hypot(x - CX, z - CZ) <= CR + 2:
                continue
            d = near_lava.get((x, z))
            rr = rng.random()
            if d is not None and d <= 1.5 and out.get((x, h, z), "").startswith(("Rock_Magma", "Rock_Volcanic")):
                if rr < 0.35:
                    out[k] = check("Plant_Reeds_Lava")
                continue
            if (x, z) in reserved:
                continue
            if rr < 0.035:
                out[k] = check("Plant_Bramble_Dead_Lavathorn")
            elif rr < 0.06:
                out[k] = check(rng.choice(("Rubble_Volcanic", "Rubble_Volcanic", "Rubble_Volcanic_Medium", "Rubble_Magma_Cooled")))
            elif rr < 0.066:
                out[k] = check(rng.choice(("Deco_Bone_Pile", "Deco_Bone_Skulls")))
            elif rr < 0.075 and out.get((x, h, z)) == "Soil_Ash":
                out[k] = check("Plant_Grass_Dry")

    # ---------------------------------------------------------------- sol praticable, reperes
    floor = []
    for x in range(-HALF + 4, HALF - 3):
        for z in range(-HALF + 4, HALF - 3):
            if (x, z) in lava or (x, z) in near_lava and near_lava[(x, z)] <= 2:
                continue
            h = H[(x, z)]
            if (x, h + 1, z) in out or (x, h + 2, z) in out:
                continue
            if max(abs(h - H[(x + dx, z + dz)]) for dx in (-1, 0, 1) for dz in (-1, 0, 1)) > 1:
                continue
            floor.append((x, h + 1, z))
    marks = {
        "arrival": (ARRIVAL[0], H[ARRIVAL] + 1, ARRIVAL[1]),
        "exitStart": (ARRIVAL[0] + 4, H[(ARRIVAL[0] + 4, ARRIVAL[1] - 2)] + 1, ARRIVAL[1] - 2),
        "boss": (CX, 1, CZ), "exitEnd": (CX, 1, CZ - 6),
        "esplanade": {"center": [CX, 0, CZ], "radius": CR},
        "lava": L,
    }
    prof = [H[(0, z)] for z in range(-100, 101, 10)]
    print(f"     relief nord -> sud (x = 0) : {prof} ; coins {[H[c] for c in ((-100, -100), (100, 100))]} ; dehors {H[(0, -150)]}")
    print(f"     caldeira : lave {len(lava)} cases (niveau {L}), orgues {cols} colonnes, " +
          ", ".join(f"{k} {v}" for k, v in counts.items()))
    return out, rot, fill, fluids, floor, marks
