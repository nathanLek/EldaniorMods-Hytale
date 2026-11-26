"""Outils sans dépendance pour les cartes : lecture/écriture PNG, flous, transformée de distance."""
import struct, zlib
from array import array


def read_png(path):
    """PNG 8 bits RGB ou RGBA, non entrelacé -> (W, H, bytearray RGB)."""
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'pas un PNG'
    p = 8
    idat = []
    while p < len(data):
        ln = struct.unpack('>I', data[p:p + 4])[0]
        t = data[p + 4:p + 8]
        body = data[p + 8:p + 8 + ln]
        if t == b'IHDR':
            W, H, bd, ct, _, _, il = struct.unpack('>IIBBBBB', body)
            assert bd == 8 and ct in (2, 6) and il == 0, 'format PNG non pris en charge'
        elif t == b'IDAT':
            idat.append(body)
        p += 12 + ln
    bpp = 3 if ct == 2 else 4
    raw = zlib.decompress(b''.join(idat))
    stride = W * bpp
    out = bytearray(H * stride)
    prev = bytearray(stride)
    i = 0
    for y in range(H):
        f = raw[i]; i += 1
        line = bytearray(raw[i:i + stride]); i += stride
        if f == 1:
            for x in range(bpp, stride):
                line[x] = (line[x] + line[x - bpp]) & 255
        elif f == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 255
        elif f == 3:
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                line[x] = (line[x] + ((a + prev[x]) >> 1)) & 255
        elif f == 4:
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                b = prev[x]
                c = prev[x - bpp] if x >= bpp else 0
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 255
        out[y * stride:(y + 1) * stride] = line
        prev = line
    if bpp == 4:
        rgb = bytearray(W * H * 3)
        rgb[0::3] = out[0::4]; rgb[1::3] = out[1::4]; rgb[2::3] = out[2::4]
        out = rgb
    return W, H, out


def save_png(path, rgb, W, H):
    raw = b''.join(b'\x00' + bytes(rgb[j * W * 3:(j + 1) * W * 3]) for j in range(H))

    def ch(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 2, 0, 0, 0))
                + ch(b'IDAT', zlib.compress(raw, 9)) + ch(b'IEND', b''))


def box_blur(a, W, H, r, passes=1):
    """Flou boîte séparable (rayon r, bords répétés). Plusieurs passes ~ gaussien."""
    a = array('d', a)
    for _ in range(passes):
        out = array('d', bytes(8 * W * H))
        n = 2 * r + 1
        for y in range(H):
            o = y * W
            row = a[o:o + W]
            s = row[0] * (r + 1) + sum(row[min(W - 1, k)] for k in range(1, r + 1))
            for x in range(W):
                out[o + x] = s / n
                s += row[min(W - 1, x + r + 1)] - row[max(0, x - r)]
        a2 = array('d', bytes(8 * W * H))
        for x in range(W):
            col = out[x::W]
            s = col[0] * (r + 1) + sum(col[min(H - 1, k)] for k in range(1, r + 1))
            for y in range(H):
                a2[y * W + x] = s / n
                s += col[min(H - 1, y + r + 1)] - col[max(0, y - r)]
        a = a2
    return a


def distance(feature, W, H):
    """Distance (en pixels, chanfrein 1/1.414) à la cellule « feature » la plus proche, et son indice."""
    INF = 1e9
    d = array('d', [INF]) * (W * H)
    near = array('i', [-1]) * (W * H)
    for i, f in enumerate(feature):
        if f:
            d[i] = 0.0
            near[i] = i
    D = 1.41421356
    for y in range(H):
        o = y * W
        for x in range(W):
            i = o + x
            best = d[i]; bn = near[i]
            if x > 0 and d[i - 1] + 1 < best: best = d[i - 1] + 1; bn = near[i - 1]
            if y > 0:
                j = i - W
                if d[j] + 1 < best: best = d[j] + 1; bn = near[j]
                if x > 0 and d[j - 1] + D < best: best = d[j - 1] + D; bn = near[j - 1]
                if x < W - 1 and d[j + 1] + D < best: best = d[j + 1] + D; bn = near[j + 1]
            d[i] = best; near[i] = bn
    for y in range(H - 1, -1, -1):
        o = y * W
        for x in range(W - 1, -1, -1):
            i = o + x
            best = d[i]; bn = near[i]
            if x < W - 1 and d[i + 1] + 1 < best: best = d[i + 1] + 1; bn = near[i + 1]
            if y < H - 1:
                j = i + W
                if d[j] + 1 < best: best = d[j] + 1; bn = near[j]
                if x < W - 1 and d[j + 1] + D < best: best = d[j + 1] + D; bn = near[j + 1]
                if x > 0 and d[j - 1] + D < best: best = d[j - 1] + D; bn = near[j - 1]
            d[i] = best; near[i] = bn
    return d, near


def components(mask, W, H):
    """Composantes 4-connexes : (étiquettes, tailles)."""
    lab = array('i', [-1]) * (W * H)
    sizes = []
    for s in range(W * H):
        if not mask[s] or lab[s] >= 0:
            continue
        k = len(sizes)
        stack = [s]; lab[s] = k; n = 0
        while stack:
            i = stack.pop(); n += 1
            x = i % W
            for j in (i - W, i + W, i - 1 if x > 0 else -1, i + 1 if x < W - 1 else -1):
                if 0 <= j < W * H and mask[j] and lab[j] < 0:
                    lab[j] = k; stack.append(j)
        sizes.append(n)
    return lab, sizes


def smoothstep(e0, e1, v):
    t = (v - e0) / (e1 - e0)
    t = 0.0 if t < 0 else (1.0 if t > 1 else t)
    return t * t * (3 - 2 * t)
