#!/usr/bin/env python3
"""Relève le relief d'une map Minecraft récente (1.18+, format Anvil avec palettes) colonne par colonne.

Pour chaque colonne de blocs : hauteur du sol (sans arbres ni plantes), hauteur de la surface de l'eau (s'il y en a),
matière du sol (herbe, sable, neige, roche...) et biome Minecraft. Sert de base à la carte Hytale (carte_depuis_mc.py).

Usage :
    python3 -I scripts/carte/mc_releve.py "<dossier du monde MC>" <dossier de sortie> [--proc 8]

Sortie (dans <dossier de sortie>) :
    releve.json : emprise (x0, z0, largeur, hauteur en blocs), liste des biomes et des matières
    sol.i16, eau.i16 : int16 petit-boutiste, une valeur par colonne (ligne par ligne, z puis x) ; eau = -32768 si sec
    matiere.u8, biome.u8 : un octet par colonne
"""
import argparse, glob, gzip, io, json, multiprocessing, os, re, struct, sys, zlib
from array import array

NONE = -32768
MATIERES = ['vide', 'herbe', 'terre', 'sable', 'gravier', 'roche', 'neige', 'glace', 'argile', 'autre']

# ---------------------------------------------------------------- NBT
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
            name = _r(f, nl).decode('utf-8', 'replace')
            d[name] = _payload(f, tt)
    if t == 11:
        n = struct.unpack('>i', _r(f, 4))[0]
        a = array('i', _r(f, 4 * n)); a.byteswap(); return a
    if t == 12:
        n = struct.unpack('>i', _r(f, 4))[0]
        a = array('q', _r(f, 8 * n)); a.byteswap(); return a
    raise ValueError('tag NBT inconnu %d' % t)

def _root(data):
    f = io.BytesIO(data); t = _r(f, 1)[0]
    _r(f, struct.unpack('>H', _r(f, 2))[0])
    return _payload(f, t)

def unpack(longs, bits, count):
    """Entrées non chevauchantes (format 1.16+)."""
    per = 64 // bits
    mask = (1 << bits) - 1
    out = []
    for L in longs:
        L &= 0xFFFFFFFFFFFFFFFF
        for _ in range(per):
            out.append(L & mask)
            L >>= bits
            if len(out) == count:
                return out
    out.extend([0] * (count - len(out)))
    return out

# ---------------------------------------------------------------- classes de blocs
PASSE = re.compile(r'(_log|_wood|_stem|_hyphae|leaves|sapling|_grass$|^grass$|fern|flower|tulip|orchid|allium|bluet|'
                   r'daisy|dandelion|poppy|lilac|rose_bush|peony|sunflower|bush|vine|mushroom|_roots|azalea|'
                   r'dripleaf|moss_carpet|snow$|lily_pad|seagrass|kelp|sugar_cane|bamboo|cactus|dead_bush|'
                   r'sweet_berry|pumpkin|melon|cocoa|lichen|pink_petals|leaf_litter|wildflowers|firefly|'
                   r'cactus_flower|torch|fence|_wall$|carpet|_button|pressure_plate|_sign|banner|lantern|'
                   r'water|bubble_column|lava|air$|ice$|frosted_ice)')

def matiere(name):
    n = name[10:] if name.startswith('minecraft:') else name
    if n in ('grass_block', 'moss_block', 'mycelium', 'podzol'): return 1
    if 'dirt' in n or n in ('mud', 'farmland', 'dirt_path', 'rooted_dirt', 'packed_mud'): return 2
    if 'sand' in n and 'stone' not in n: return 3
    if n == 'gravel' or n == 'suspicious_gravel': return 4
    if n in ('snow_block', 'powder_snow'): return 6
    if 'ice' in n: return 7
    if 'clay' in n or 'terracotta' in n: return 8
    if any(k in n for k in ('stone', 'andesite', 'diorite', 'granite', 'tuff', 'calcite', 'deepslate', 'basalt',
                            'blackstone', 'dripstone', 'cobble', 'obsidian', 'ore')): return 5
    return 9

def est_eau(name):
    return name in ('minecraft:water', 'minecraft:bubble_column', 'minecraft:seagrass', 'minecraft:tall_seagrass',
                    'minecraft:kelp', 'minecraft:kelp_plant')

# ---------------------------------------------------------------- un chunk
class Chunk:
    def __init__(self, level):
        self.min_y = level.get('yPos', -4) * 16
        self.sec = {s['Y']: s for s in level.get('sections', [])}
        self.cache = {}

    def block(self, x, y, z):
        sy = y >> 4
        dec = self.cache.get(sy)
        if dec is None:
            s = self.sec.get(sy)
            if s is None or 'block_states' not in s:
                dec = (['minecraft:air'], None)
            else:
                bs = s['block_states']
                pal = [p['Name'] for p in bs['palette']]
                if len(pal) == 1 or 'data' not in bs:
                    dec = (pal, None)
                else:
                    bits = max(4, (len(pal) - 1).bit_length())
                    dec = (pal, unpack(bs['data'], bits, 4096))
            self.cache[sy] = dec
        pal, idx = dec
        if idx is None:
            return pal[0]
        return pal[idx[((y & 15) * 16 + z) * 16 + x]]

    def biome(self, x, y, z, ids):
        s = self.sec.get(y >> 4)
        if s is None or 'biomes' not in s:
            return 0
        b = s['biomes']
        pal = b['palette']
        if len(pal) == 1 or 'data' not in b:
            n = pal[0]
        else:
            bits = (len(pal) - 1).bit_length()
            i = (((y & 15) >> 2) * 4 + (z >> 2)) * 4 + (x >> 2)
            per = 64 // bits
            L = b['data'][i // per] & 0xFFFFFFFFFFFFFFFF
            n = pal[(L >> (bits * (i % per))) & ((1 << bits) - 1)]
        if n not in ids:
            ids[n] = len(ids)
        return ids[n]

def region_job(args):
    path, cx0, cz0, W, H = args
    m = re.search(r'r\.(-?\d+)\.(-?\d+)\.mca$', path)
    rx, rz = int(m.group(1)), int(m.group(2))
    sol = array('h', [NONE]) * (512 * 512)
    eau = array('h', [NONE]) * (512 * 512)
    mat = bytearray(512 * 512)
    bio = bytearray(512 * 512)
    ids = {}
    with open(path, 'rb') as fh:
        data = fh.read()
    n = 0
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], 'big')
        if off == 0:
            continue
        p = off * 4096
        ln = struct.unpack('>i', data[p:p + 4])[0]
        comp = data[p + 4]
        raw = data[p + 5:p + 4 + ln]
        try:
            raw = zlib.decompress(raw) if comp == 2 else gzip.decompress(raw)
            lv = _root(raw)
        except Exception:
            continue
        hm = lv.get('Heightmaps', {})
        if 'MOTION_BLOCKING_NO_LEAVES' not in hm:
            continue
        ch = Chunk(lv)
        lx0, lz0 = (i % 32) * 16, (i // 32) * 16
        mb = unpack(hm['MOTION_BLOCKING_NO_LEAVES'], 9, 256)
        n += 1
        for z in range(16):
            for x in range(16):
                y = mb[z * 16 + x] + ch.min_y - 1
                wy = NONE
                # descendre à travers l'eau, les troncs et les plantes jusqu'au vrai sol
                lim = ch.min_y
                while y > lim:
                    b = ch.block(x, y, z)
                    if est_eau(b):
                        if wy == NONE:
                            wy = y
                    elif not PASSE.search(b[10:]):
                        break
                    elif b.endswith('ice') and wy == NONE:
                        wy = y                         # glace posée sur l'eau : on garde l'eau
                    y -= 1
                o = (lz0 + z) * 512 + lx0 + x
                sol[o] = y
                eau[o] = wy
                mat[o] = matiere(ch.block(x, y, z))
                bio[o] = ch.biome(x, y + 1, z, ids)
    names = sorted(ids, key=ids.get)
    return rx, rz, sol.tobytes(), eau.tobytes(), bytes(mat), bytes(bio), names, n

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('monde')
    ap.add_argument('sortie')
    ap.add_argument('--proc', type=int, default=max(1, os.cpu_count() - 2))
    a = ap.parse_args()
    files = glob.glob(os.path.join(a.monde, 'region', 'r.*.mca'))
    coords = [tuple(map(int, re.search(r'r\.(-?\d+)\.(-?\d+)\.mca$', f).groups())) for f in files]
    rx0 = min(c[0] for c in coords); rx1 = max(c[0] for c in coords)
    rz0 = min(c[1] for c in coords); rz1 = max(c[1] for c in coords)
    W = (rx1 - rx0 + 1) * 512
    H = (rz1 - rz0 + 1) * 512
    print('régions', len(files), 'emprise', W, 'x', H, 'blocs', flush=True)
    sol = array('h', [NONE]) * (W * H)
    eau = array('h', [NONE]) * (W * H)
    mat = bytearray(W * H)
    bio = bytearray(W * H)
    gnames = {}
    done = 0
    with multiprocessing.Pool(a.proc) as pool:
        for rx, rz, s, e, m, b, names, n in pool.imap_unordered(region_job, [(f, 0, 0, W, H) for f in files]):
            s = array('h', s); e = array('h', e)
            remap = bytes(gnames.setdefault(nm, len(gnames)) for nm in names) if names else b''
            ox, oz = (rx - rx0) * 512, (rz - rz0) * 512
            for z in range(512):
                o = (oz + z) * W + ox
                sol[o:o + 512] = s[z * 512:(z + 1) * 512]
                eau[o:o + 512] = e[z * 512:(z + 1) * 512]
                mat[o:o + 512] = m[z * 512:(z + 1) * 512]
                bio[o:o + 512] = b[z * 512:(z + 1) * 512].translate(remap.ljust(256, b'\0')) if remap else bytes(512)
            done += 1
            print('  %d/%d r.%d.%d (%d chunks)' % (done, len(files), rx, rz, n), flush=True)
    os.makedirs(a.sortie, exist_ok=True)
    for nm, arr in (('sol.i16', sol), ('eau.i16', eau)):
        with open(os.path.join(a.sortie, nm), 'wb') as f:
            arr.tofile(f)
    with open(os.path.join(a.sortie, 'matiere.u8'), 'wb') as f:
        f.write(mat)
    with open(os.path.join(a.sortie, 'biome.u8'), 'wb') as f:
        f.write(bio)
    json.dump({'x0': rx0 * 512, 'z0': rz0 * 512, 'largeur': W, 'hauteur': H, 'vide': NONE,
               'biomes': sorted(gnames, key=gnames.get), 'matieres': MATIERES},
              open(os.path.join(a.sortie, 'releve.json'), 'w'), indent=1)
    print('écrit dans', a.sortie)

if __name__ == '__main__':
    main()
