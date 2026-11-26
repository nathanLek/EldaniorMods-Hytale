#!/usr/bin/env python3
"""Convertit une map Minecraft 1.12 (format Anvil, ids numériques) en données de blocs Hytale.

Sortie : un fichier .dat lu par le générateur Java `DownstonWorldGen` (scripts/carte/java),
qui fabrique ensuite une vraie sauvegarde solo Hytale.

Usage :
    python3 -I scripts/carte/mc_vers_hytale.py "<dossier map MC>" <sortie.dat> [--assets "<Assets Hytale>/Server"]

Format du .dat (gros-boutiste) :
    "DNST" | int version=1 | int chunkMinX | int chunkMinZ | int chunksX | int chunksZ | int hauteur
    | int tailleTable | (UTF nom, byte rotation, byte fluide) * tailleTable
    | spawnX, spawnY, spawnZ (int)
    | pour chaque chunk Hytale (z puis x) : int longueur | zlib(octets[hauteur*32*32], ordre y, z, x)
L'indice 0 de la table = air.
"""
import argparse, glob, gzip, io, os, struct, sys, zlib

# ---------------------------------------------------------------- lecture NBT / Anvil
def _r(f, n):
    b = f.read(n)
    if len(b) < n:
        raise EOFError
    return b

def _payload(f, t):
    if t == 1: return struct.unpack('>b', _r(f, 1))[0]
    if t == 2: return struct.unpack('>h', _r(f, 2))[0]
    if t == 3: return struct.unpack('>i', _r(f, 4))[0]
    if t == 4: return struct.unpack('>q', _r(f, 8))[0]
    if t == 5: return struct.unpack('>f', _r(f, 4))[0]
    if t == 6: return struct.unpack('>d', _r(f, 8))[0]
    if t == 7:
        n = struct.unpack('>i', _r(f, 4))[0]; return _r(f, n)
    if t == 8:
        n = struct.unpack('>H', _r(f, 2))[0]; return _r(f, n).decode('utf-8', 'replace')
    if t == 9:
        et = _r(f, 1)[0]; n = struct.unpack('>i', _r(f, 4))[0]
        return [_payload(f, et) for _ in range(n)]
    if t == 10:
        d = {}
        while True:
            tt = _r(f, 1)[0]
            if tt == 0: return d
            nl = struct.unpack('>H', _r(f, 2))[0]
            name = _r(f, nl).decode('utf-8', 'replace')  # lire le nom AVANT la valeur
            d[name] = _payload(f, tt)
    if t == 11:
        n = struct.unpack('>i', _r(f, 4))[0]; return list(struct.unpack('>%di' % n, _r(f, 4 * n)))
    if t == 12:
        n = struct.unpack('>i', _r(f, 4))[0]; return list(struct.unpack('>%dq' % n, _r(f, 8 * n)))
    raise ValueError('tag NBT inconnu %d' % t)

def _root(data):
    f = io.BytesIO(data); t = _r(f, 1)[0]
    _r(f, struct.unpack('>H', _r(f, 2))[0])
    return _payload(f, t)

def region_chunks(path):
    with open(path, 'rb') as fh:
        data = fh.read()
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], 'big')
        if off == 0:
            continue
        p = off * 4096
        ln = struct.unpack('>i', data[p:p + 4])[0]
        comp = data[p + 4]
        raw = data[p + 5:p + 4 + ln]
        raw = zlib.decompress(raw) if comp == 2 else gzip.decompress(raw)
        yield _root(raw)['Level']

# ---------------------------------------------------------------- correspondance des blocs
# Rotations Hytale : index = roll*16 + pitch*4 + yaw (0 = aucune).
# Tronc couché selon Z : pitch 90 (4) ; selon X : pitch 90 + yaw 90 (5).
# Lierre (Plant_Vine_Wall, support au nord par défaut) : yaw 1 = ouest, 2 = sud, 3 = est.
AIR = None
WATER = ('Water_Source', 0, 1)

LOGS = {(17, 0): 'Oak', (17, 1): 'Fir', (17, 2): 'Birch', (17, 3): 'Jungle',
        (162, 0): 'Palo', (162, 1): 'Redwood'}
LEAVES = {(18, 0): 'Oak', (18, 1): 'Fir', (18, 2): 'Birch', (18, 3): 'Jungle',
          (161, 0): 'Palo', (161, 1): 'Redwood'}
FLOWERS_38 = ['Red', 'Orchid_Blue', 'Purple', 'White', 'Red2', 'Orange', 'White2', 'Pink', 'Yellow2']
DOUBLE_175 = ['Plant_Flower_Common_Yellow2', 'Plant_Flower_Common_Purple2', 'Plant_Grass_Lush_Tall',
              'Plant_Fern_Tall', 'Plant_Flower_Common_Red2', 'Plant_Flower_Common_Pink2']
TERRACOTTA_159 = {0: 'White', 1: 'Orange', 2: 'Purple', 3: 'Blue', 4: 'Yellow', 5: 'Lime', 6: 'Pink',
                  7: 'Grey', 8: 'GreyLight', 9: 'Cyan', 10: 'Purple', 11: 'Blue', 12: 'Beige',
                  13: 'Green', 14: 'Red', 15: 'Black'}
SIMPLE = {
    (1, 0): 'Rock_Stone', (1, 1): 'Rock_Slate', (1, 2): 'Rock_Slate', (1, 3): 'Rock_Calcite',
    (1, 4): 'Rock_Calcite', (1, 5): 'Rock_Shale', (1, 6): 'Rock_Shale',
    (2, 0): 'Soil_Grass', (3, 0): 'Soil_Dirt', (3, 1): 'Soil_Dirt_Dry', (3, 2): 'Soil_Dirt',
    (4, 0): 'Rock_Stone_Cobble', (7, 0): 'Rock_Bedrock',
    (12, 0): 'Soil_Sand', (12, 1): 'Soil_Sand_Red', (13, 0): 'Soil_Gravel',
    (24, 0): 'Rock_Sandstone', (24, 1): 'Rock_Sandstone', (24, 2): 'Rock_Sandstone',
    (31, 1): 'Plant_Grass_Lush_Short', (31, 2): 'Plant_Fern', (32, 0): 'Plant_Bush_Dead',
    (37, 0): 'Plant_Flower_Common_Yellow',
    (44, 3): 'Rock_Stone_Cobble_Half', (48, 0): 'Rock_Stone_Cobble_Mossy',
    (78, 0): 'Soil_Snow', (80, 0): 'Soil_Snow', (82, 0): 'Soil_Clay',
    (121, 0): 'Rock_Calcite', (172, 0): 'Soil_Clay_Orange',
    (126, 1): 'Wood_Fir_Trunk_Half', (126, 9): 'Wood_Fir_Trunk_Half',
}
# Brindilles des arbres WorldPainter (barrières, barreaux) noyées dans le feuillage : remplacées par des feuilles.
TWIGS = {85: 'Plant_Leaves_Oak', 101: 'Plant_Leaves_Oak', 188: 'Plant_Leaves_Fir', 189: 'Plant_Leaves_Birch',
         190: 'Plant_Leaves_Jungle', 191: 'Plant_Leaves_Redwood', 192: 'Plant_Leaves_Palo'}
# Petits détails sans équivalent (boutons, cacao, champignons, barrière invisible) : retirés.
DROPPED = {0, 39, 40, 77, 127, 143, 166}

def map_block(b, d):
    """(id, data) Minecraft -> (nom Hytale, rotation, fluide) ou None (air)."""
    if b in DROPPED:
        return AIR
    if b in (8, 9):
        return WATER
    if b in (17, 162):
        kind = LOGS.get((b, d & 3), 'Oak')
        axis = d >> 2
        if axis == 3:
            return ('Wood_%s_Trunk_Full' % kind, 0, 0)
        return ('Wood_%s_Trunk' % kind, {0: 0, 1: 5, 2: 4}[axis], 0)
    if b in (18, 161):
        return ('Plant_Leaves_%s' % LEAVES.get((b, d & 3), 'Oak'), 0, 0)
    if b == 38:
        return ('Plant_Flower_' + ('Orchid_Blue' if d == 1 else 'Common_' + FLOWERS_38[min(d, 8)]), 0, 0)
    if b == 175:
        return AIR if d & 8 else (DOUBLE_175[min(d, 5)], 0, 0)
    if b == 159:
        return ('Soil_Clay_' + TERRACOTTA_159[d], 0, 0)
    if b == 106:
        for bit, yaw in ((4, 0), (2, 1), (1, 2), (8, 3)):
            if d & bit:
                return ('Plant_Vine_Wall', yaw, 0)
        return ('Plant_Vine_Hanging', 0, 0)
    if b in TWIGS:
        return (TWIGS[b], 0, 0)
    if (b, d) in SIMPLE:
        return (SIMPLE[(b, d)], 0, 0)
    if (b, 0) in SIMPLE:
        return (SIMPLE[(b, 0)], 0, 0)
    return 'INCONNU'

# ---------------------------------------------------------------- conversion
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('map')
    ap.add_argument('sortie')
    ap.add_argument('--assets', default=os.path.expanduser('~/Desktop/Assets Hytale/Server'))
    a = ap.parse_args()

    regions = sorted(glob.glob(os.path.join(a.map, 'region', '*.mca')))
    if not regions:
        sys.exit('aucun fichier region/*.mca dans ' + a.map)

    # 1er passage : emprise de la map (en chunks Minecraft de 16).
    xs, zs = [], []
    for p in regions:
        for L in region_chunks(p):
            xs.append(L['xPos']); zs.append(L['zPos'])
    mx0, mz0 = min(xs) * 16, min(zs) * 16
    mx1, mz1 = (max(xs) + 1) * 16, (max(zs) + 1) * 16
    # Centrer la map sur l'origine Hytale, aligné sur les chunks Hytale (32).
    offx = -((mx0 + mx1) // 2) // 32 * 32
    offz = -((mz0 + mz1) // 2) // 32 * 32
    cmin_x, cmin_z = (mx0 + offx) // 32, (mz0 + offz) // 32
    cmax_x, cmax_z = (mx1 - 1 + offx) // 32, (mz1 - 1 + offz) // 32
    ncx, ncz = cmax_x - cmin_x + 1, cmax_z - cmin_z + 1
    H = 256
    print('map MC x %d..%d z %d..%d -> Hytale chunks x %d..%d z %d..%d (%d chunks), décalage %d,%d'
          % (mx0, mx1 - 1, mz0, mz1 - 1, cmin_x, cmax_x, cmin_z, cmax_z, ncx * ncz, offx, offz))

    palette = [None]           # 0 = air
    pindex = {None: 0}
    cache = {}
    unknown = {}
    chunks = {}                # (cx, cz) -> bytearray(H*32*32)

    def pal(key):
        if key not in pindex:
            pindex[key] = len(palette); palette.append(key)
            if len(palette) > 255:
                sys.exit('plus de 255 blocs différents : passer le format en 16 bits')
        return pindex[key]

    for p in regions:
        print('lecture', os.path.basename(p), flush=True)
        for L in region_chunks(p):
            hx0 = L['xPos'] * 16 + offx; hz0 = L['zPos'] * 16 + offz
            key = (hx0 // 32 - cmin_x, hz0 // 32 - cmin_z)
            buf = chunks.get(key)
            if buf is None:
                buf = chunks[key] = bytearray(H * 1024)
            lx0, lz0 = hx0 % 32, hz0 % 32
            for s in L['Sections']:
                B = s['Blocks']; D = s['Data']; Y = s['Y'] * 16
                for idx in range(4096):
                    b = B[idx]
                    if b == 0:
                        continue
                    d = (D[idx >> 1] >> ((idx & 1) * 4)) & 15
                    v = cache.get((b, d))
                    if v is None:
                        m = map_block(b, d)
                        if m == 'INCONNU':
                            unknown[(b, d)] = unknown.get((b, d), 0) + 1
                            m = AIR
                        v = cache[(b, d)] = pal(m)
                    if v:
                        y = Y + (idx >> 8)
                        buf[(y << 10) | ((lz0 + ((idx >> 4) & 15)) << 5) | (lx0 + (idx & 15))] = v

    if unknown:
        print('blocs sans correspondance (mis en air) :', sorted(unknown.items()))

    # Vérifier que chaque nom existe bien dans les assets du jeu.
    names = set()
    for root, _, files in os.walk(a.assets):
        for f in files:
            if f.endswith('.json'):
                names.add(f[:-5])
    missing = sorted({k[0] for k in palette[1:] if k[0] not in names})
    if missing:
        sys.exit('noms Hytale introuvables dans les assets : %s' % missing)

    # Point d'apparition : spawn Minecraft (level.dat) décalé, posé sur le sol.
    lvl = _root(gzip.open(os.path.join(a.map, 'level.dat')).read())['Data']
    sx, sz = lvl['SpawnX'] + offx, lvl['SpawnZ'] + offz
    if not (cmin_x * 32 <= sx < (cmax_x + 1) * 32 and cmin_z * 32 <= sz < (cmax_z + 1) * 32):
        sx, sz = 0, 0
    buf = chunks.get((sx // 32 - cmin_x, sz // 32 - cmin_z))
    sy = 100
    if buf:
        col = ((sz % 32) << 5) | (sx % 32)
        for y in range(H - 1, -1, -1):
            k = palette[buf[(y << 10) | col]]
            if k and not k[0].startswith('Plant_'):
                sy = y + 1; break
    print('spawn Hytale', sx, sy, sz)

    with open(a.sortie, 'wb') as out:
        out.write(b'DNST' + struct.pack('>iiiiii', 1, cmin_x, cmin_z, ncx, ncz, H))
        out.write(struct.pack('>i', len(palette)))
        for k in palette:
            name, rot, fluid = k if k else ('Empty', 0, 0)
            nb = name.encode('utf-8')
            out.write(struct.pack('>H', len(nb)) + nb + struct.pack('>bb', rot, fluid))
        out.write(struct.pack('>iii', sx, sy, sz))
        empty = bytearray(H * 1024)
        for cz in range(ncz):
            for cx in range(ncx):
                z = zlib.compress(bytes(chunks.get((cx, cz), empty)), 6)
                out.write(struct.pack('>i', len(z)) + z)
    print('écrit', a.sortie, os.path.getsize(a.sortie) // 1024, 'Ko,', len(palette) - 1, 'blocs différents')

if __name__ == '__main__':
    main()
