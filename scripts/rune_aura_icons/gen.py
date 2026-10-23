import sys, math
sys.path.insert(0, __import__('os').path.dirname(__import__('os').path.abspath(__file__))); import png

GOLD = (255, 210, 63); CORE = (255, 247, 214); DEEP = (138, 90, 0)

def star_sdf(x, y, r_long, r_short, n=4):
    """Distance signee approx. a une etoile 2n branches (n longues + n courtes)."""
    a = math.atan2(y, x); d = math.hypot(x, y)
    seg = math.pi / n
    t = (a % (2 * seg)) / seg          # 0..2 sur une paire de branches
    k = abs(t - 1)                     # 1 = pointe longue, 0 = pointe courte (decalee)
    # rayon de bord : interpolation entre branches longues (k=1) et creux
    long_tip = r_long * (k ** 3)
    short_tip = r_short * ((1 - k) ** 3)
    edge = max(long_tip, short_tip, r_long * 0.22)
    return d - edge

def blend(dst, col, a):
    a = max(0.0, min(1.0, a))
    if a <= 0: return
    da = dst[3] / 255.0
    oa = a + da * (1 - a)
    for i in range(3):
        dst[i] = (col[i] * a + dst[i] * da * (1 - a)) / oa if oa > 0 else 0
    dst[3] = oa * 255

def add(dst, col, a):
    for i in range(3): dst[i] = min(255, dst[i] + col[i] * a)

def emblem(px, w, h, cx, cy, R, ss=3, glow=True):
    """Halo + anneau + etoile rayonnante, centre (cx,cy), taille R (rayon de l'anneau)."""
    for y in range(h):
        for x in range(w):
            # supersampling
            ring_a = star_a = core_a = 0.0
            for sy in range(ss):
                for sx in range(ss):
                    fx = x + (sx + 0.5) / ss - cx; fy = y + (sy + 0.5) / ss - cy
                    d = math.hypot(fx, fy)
                    if abs(d - R) < R * 0.09: ring_a += 1
                    if star_sdf(fx, fy, R * 0.82, R * 0.45) < 0: star_a += 1
                    if d < R * 0.2: core_a += 1
            n = ss * ss; ring_a /= n; star_a /= n; core_a /= n
            p = px[y][x]
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if glow:
                g = math.exp(-((d - R) / (R * 0.28)) ** 2) * 0.55 + math.exp(-(d / (R * 0.55)) ** 2) * 0.6
                if p[3] > 0: add(p, GOLD, g * 0.55)
            blend(p, DEEP, ring_a * 0.9)
            blend(p, GOLD, ring_a * 0.75)
            blend(p, GOLD, star_a)
            blend(p, CORE, core_a + star_a * max(0, 1 - d / (R * 0.6)) * 0.8)

def ability_icon(path, size=128):
    """Icone HUD / bench : fond sombre + rayons + embleme d'aura."""
    c = size / 2
    px = [[[0, 0, 0, 255] for _ in range(size)] for _ in range(size)]
    for y in range(size):
        for x in range(size):
            dx, dy = x + 0.5 - c, y + 0.5 - c; d = math.hypot(dx, dy) / c
            base = [42 * (1 - d) + 12 * d, 26 * (1 - d) + 8 * d, 8 * (1 - d) + 18 * d]
            ray = (math.cos(math.atan2(dy, dx) * 12) * 0.5 + 0.5) ** 6 * max(0, 1 - d) * 0.35
            px[y][x] = [base[0] + 255 * ray, base[1] + 190 * ray, base[2] + 60 * ray, 255]
    emblem(px, size, size, c, c, size * 0.30)
    # contour sombre leger
    for y in range(size):
        for x in range(size):
            e = min(x, y, size - 1 - x, size - 1 - y)
            if e < 3:
                for i in range(3): px[y][x][i] *= 0.55 + 0.15 * e
    png.write(path, size, size, px)

def item_icon(src, dst, cx=35.5, cy=33.3, gx=33, gy=34):
    """Icone inventaire : on garde la pierre, on efface le glyphe rouge (centre gx,gy) et on grave l'embleme au centre de la pierre (cx,cy)."""
    w, h, px = png.read(src)
    def warmth(p): return max(0.0, min(1.0, (p[0] - p[2] - 15) / 90.0)) if p[3] > 0 else 0
    stone = [p for row in px for p in row if p[3] > 200 and warmth(p) == 0 and math.hypot(px.index(row) - cy, 0) < 99]
    sc = [sum(p[i] for p in stone) / len(stone) for i in range(3)]
    for y in range(h):
        for x in range(w):
            p = px[y][x]; k = warmth(p)
            if k > 0 and math.hypot(x - gx, y - gy) < 22:
                for i in range(3): p[i] = p[i] * (1 - k) + sc[i] * 0.8 * k
    emblem(px, w, h, cx, cy, 10.5, ss=4)
    png.write(dst, w, h, px)

def texture(src, dst):
    """Texture 3D : glyphes rouges/oranges recolores en or (meme forme)."""
    import colorsys
    w, h, px = png.read(src)
    for row in px:
        for p in row:
            if p[3] == 0: continue
            r, g, b = (v / 255 for v in p[:3]); hh, s, v = colorsys.rgb_to_hsv(r, g, b)
            if s > 0.35 and (hh < 0.12 or hh > 0.9):          # rouges / oranges
                nr, ng, nb = colorsys.hsv_to_rgb(0.125, min(1, s * 0.9), min(1, v * 1.1))
                p[0], p[1], p[2] = nr * 255, ng * 255, nb * 255
    png.write(dst, w, h, px)

if __name__ == '__main__':
    out = sys.argv[1]; src = sys.argv[2]
    ability_icon(out + '/ability.png')
    item_icon(src + '/Common/Icons/ItemsGenerated/Rune_Enrage.png', out + '/item.png')
    texture(src + '/Common/Items/Runes/Rune_Textures/Rune_Ability_Enrage.png', out + '/texture.png')
    print('ok')
