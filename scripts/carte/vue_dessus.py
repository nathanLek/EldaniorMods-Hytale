#!/usr/bin/env python3
"""Vue du dessus d'un monde généré, à partir du relevé écrit par CarteOutil (-Dcarte.dump).

Usage :
    python3 -I scripts/carte/vue_dessus.py <relevé.bin> <sortie.png> [--masque <Mask.png> --span 12000] [--pas 2]

--masque trace en fin trait les limites des zones prévues (pour comparer le prévu au généré).
--pas N : un pixel pour N blocs (réduit l'image).
"""
import argparse, struct, zlib

def couleur(nom):
    n = nom.lower()
    table = [
        ('snow', (240, 244, 248)), ('ice', (190, 220, 240)), ('sand_red', (200, 110, 60)), ('sand', (222, 204, 150)),
        ('sandstone_red', (190, 100, 60)), ('sandstone', (214, 190, 130)), ('gravel', (140, 136, 130)),
        ('grass_dry', (176, 170, 90)), ('grass_cold', (120, 150, 120)), ('grass_burnt', (110, 90, 60)),
        ('grass_deep', (70, 120, 50)), ('grass_wet', (80, 130, 70)), ('grass_sunny', (150, 180, 70)), ('grass', (110, 160, 70)),
        ('leaves_fir', (40, 90, 70)), ('leaves_redwood', (60, 90, 50)), ('leaves_cedar', (50, 95, 75)),
        ('leaves_autumn', (200, 120, 50)), ('leaves_amber', (210, 150, 60)), ('leaves_maple', (190, 80, 40)),
        ('leaves_birch', (150, 190, 90)), ('leaves_aspen', (180, 190, 80)), ('leaves_bamboo', (120, 200, 80)),
        ('leaves_wisteria', (180, 140, 210)), ('leaves', (60, 120, 50)), ('bamboo', (130, 190, 90)),
        ('trunk', (110, 85, 60)), ('wood', (120, 95, 65)), ('mud', (100, 80, 60)), ('clay', (180, 120, 90)),
        ('dirt', (130, 100, 70)), ('soil', (130, 110, 80)),
        ('basalt', (70, 70, 75)), ('slate', (90, 95, 100)), ('shale', (110, 110, 105)), ('calcite', (220, 220, 210)),
        ('chalk', (230, 228, 215)), ('marble', (230, 230, 230)), ('quartzite', (200, 190, 180)), ('rock', (125, 125, 125)),
        ('cactus', (90, 150, 70)), ('rubble', (130, 130, 125)), ('flower', (200, 160, 200)), ('fern', (70, 140, 60)), ('bush', (70, 130, 60)),
        ('plant', (90, 150, 70)),
    ]
    for k, c in table:
        if k in n:
            return c
    return (200, 0, 200)

def lire(path):
    with open(path, 'rb') as f:
        data = f.read()
    assert data[:4] == b'CRTE'
    x0, z0, w, h, n = struct.unpack('>iiiii', data[4:24])
    p = 24
    noms = []
    for _ in range(n):
        ln = struct.unpack('>H', data[p:p + 2])[0]; p += 2
        noms.append(data[p:p + ln].decode('utf-8')); p += ln
    cols = struct.iter_unpack('>hhb', data[p:p + w * h * 5])
    top, hei, eau = [], [], []
    for t, y, e in cols:
        top.append(t); hei.append(y); eau.append(e)
    return x0, z0, w, h, noms, top, hei, eau

def lire_png_rgb(path):
    with open(path, 'rb') as f:
        data = f.read()
    p = 8; W = H = 0; idat = b''
    while p < len(data):
        ln = struct.unpack('>I', data[p:p + 4])[0]; t = data[p + 4:p + 8]; d = data[p + 8:p + 8 + ln]; p += 12 + ln
        if t == b'IHDR':
            W, H = struct.unpack('>II', d[:8])
        elif t == b'IDAT':
            idat += d
    raw = zlib.decompress(idat)
    rows = []
    for j in range(H):          # nos masques sont écrits sans filtre (type 0)
        row = raw[j * (W * 3 + 1) + 1:(j + 1) * (W * 3 + 1)]
        rows.append(row)
    return W, H, rows

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('releve'); ap.add_argument('sortie')
    ap.add_argument('--masque'); ap.add_argument('--span', type=int, default=12000)
    ap.add_argument('--pas', type=int, default=1)
    a = ap.parse_args()
    x0, z0, w, h, noms, top, hei, eau = lire(a.releve)
    cols = [couleur(nm) for nm in noms]
    inconnus = sorted({noms[t] for t in set(top) if cols[t] == (200, 0, 200)})
    k = a.pas
    W, H = w // k, h // k
    img = bytearray(W * H * 3)
    mask = lire_png_rgb(a.masque) if a.masque else None
    for j in range(H):
        for i in range(W):
            o = (j * k) * w + i * k
            y = hei[o]
            if eau[o]:
                fond = 0
                c = (40, 90, 170)
            else:
                c = cols[top[o]]
            ol = o - k if i > 0 else o
            ou = o - k * w if j > 0 else o
            d = (hei[o] - hei[ol]) + (hei[o] - hei[ou])
            f = max(0.45, min(1.5, 1 + d * 0.18 / k))
            if eau[o]:
                f = 1.0
            c = [min(255, int(v * f)) for v in c]
            if mask:
                MW, MH, rows = mask
                def mpx(bx, bz):
                    mx = int((bx + a.span / 2) * MW / a.span); mz = int((bz + a.span / 2) * MH / a.span)
                    mx = max(0, min(MW - 1, mx)); mz = max(0, min(MH - 1, mz))
                    return rows[mz][mx * 3:mx * 3 + 3]
                bx, bz = x0 + i * k, z0 + j * k
                if mpx(bx, bz) != mpx(bx + k, bz) or mpx(bx, bz) != mpx(bx, bz + k):
                    c = [255, 60, 60]
            img[(j * W + i) * 3:(j * W + i) * 3 + 3] = bytes(c)
    raw = b''.join(b'\x00' + bytes(img[j * W * 3:(j + 1) * W * 3]) for j in range(H))
    def ch(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    with open(a.sortie, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 2, 0, 0, 0))
                + ch(b'IDAT', zlib.compress(raw, 9)) + ch(b'IEND', b''))
    hs = [hei[i] for i in range(len(hei)) if not eau[i]]
    print('vue', W, 'x', H, 'écrite :', a.sortie, '| hauteur sol min/max', min(hs) if hs else '-', max(hs) if hs else '-',
          '| eau %d %%' % (100 * sum(eau) // len(eau)))
    if inconnus:
        print('blocs sans couleur (magenta) :', inconnus[:30])

if __name__ == '__main__':
    main()
