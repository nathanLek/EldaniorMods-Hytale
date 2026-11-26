"""Lecture des assets vanilla depuis Assets.zip : PNG de tout format courant (palette, gris, RVB, RVBA) rendus en RGBA 8 bits.
Le lecteur rune_aura_icons/png.py n'accepte que le RGBA, or une partie des textures vanilla sont en palette.
"""
import os, struct, zipfile, zlib

ZIP = zipfile.ZipFile(os.path.expanduser("~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip"))


def vanilla(path):
    """Lit un PNG vanilla de n'importe quel format courant (palette, gris, RVB, RVBA) et le rend en RGBA 8 bits."""
    data = ZIP.read(path)
    pos, idat, palette, alpha = 8, b"", [], b""
    while pos < len(data):
        size = struct.unpack(">I", data[pos:pos + 4])[0]
        kind, chunk = data[pos + 4:pos + 8], data[pos + 8:pos + 8 + size]
        pos += 12 + size
        if kind == b"IHDR":
            w, h, depth, color = struct.unpack(">IIBB", chunk[:10])
        elif kind == b"PLTE":
            palette = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif kind == b"tRNS":
            alpha = chunk
        elif kind == b"IDAT":
            idat += chunk
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color]
    stride = (w * channels * depth + 7) // 8
    bpp = max(1, channels * depth // 8)
    raw = zlib.decompress(idat)
    prev, px, i = bytearray(stride), [], 0
    for _ in range(h):
        kind = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if kind == 1:
                line[x] = (line[x] + a) & 255
            elif kind == 2:
                line[x] = (line[x] + b) & 255
            elif kind == 3:
                line[x] = (line[x] + ((a + b) >> 1)) & 255
            elif kind == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        row = []
        for x in range(w):
            if depth == 8:
                v = line[x * channels:(x + 1) * channels]
            else:                                                     # palette ou gris sur 1, 2 ou 4 bits
                bit = x * depth
                v = [(line[bit // 8] >> (8 - depth - bit % 8)) & ((1 << depth) - 1)]
            if color == 3:
                row.append(list(palette[v[0]]) + [alpha[v[0]] if v[0] < len(alpha) else 255])
            elif color == 6:
                row.append(list(v))
            elif color == 2:
                row.append(list(v) + [255])
            elif color == 4:
                row.append([v[0], v[0], v[0], v[1]])
            else:
                g = v[0] * 255 // ((1 << depth) - 1)
                row.append([g, g, g, 255])
        px.append(row)
        prev = line
    return w, h, px
