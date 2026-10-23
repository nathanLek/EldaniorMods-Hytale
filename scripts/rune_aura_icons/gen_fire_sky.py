"""Ciel embrase du Courroux de Muspelheim : atlas de 3 nuages de feu (corps rouge-noir, bords incandescents eclaires
par en dessous, braises) et tourbillon de feu en spirale. Usage : python3 gen_fire_sky.py <dossier_sortie>"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
out = sys.argv[1]
N = 128
def smooth(t): return t * t * (3 - 2 * t)
def make_noise(seed):
    rnd = random.Random(seed)
    octs = [([[rnd.random() for _ in range(n + 1)] for _ in range(n + 1)], n, w) for n, w in ((4, .5), (8, .27), (16, .15), (32, .08))]
    def f(u, v):
        s = 0
        for g, n, w in octs:
            fx, fy = u * n, v * n; ix, iy = int(fx), int(fy); tx, ty = smooth(fx - ix), smooth(fy - iy)
            a = g[iy][ix] + (g[iy][ix + 1] - g[iy][ix]) * tx; b = g[iy + 1][ix] + (g[iy + 1][ix + 1] - g[iy + 1][ix]) * tx
            s += (a + (b - a) * ty) * w
        return s
    return f
def cloud(seed):
    body, heat = make_noise(seed), make_noise(seed + 100)
    c = (N - 1) / 2; px = []
    for y in range(N):
        row = []
        for x in range(N):
            u, v = x / N, y / N
            n = body(u, v); hnz = heat(u, v)
            d = math.hypot(x - c, y - c) / c
            edge = max(0.0, min(1.0, (1 - d) * 1.8 + (n - 0.5) * 1.2))
            dens = max(0.0, min(1.0, (n - 0.21) * 2.3)) * edge
            rim = max(0.0, 1 - abs(dens - 0.35) / 0.3)            # bords des volutes : incandescents
            hot = max(0.0, (hnz - 0.62) * 4)                      # poches de braise
            r = 45 + 70 * n + 200 * rim * 0.8 + 255 * hot
            g = 8 + 15 * n + 95 * rim * 0.8 + 170 * hot
            b = 5 + 25 * rim * 0.5 + 60 * hot
            row.append([min(255, r), min(255, g), min(255, b), 255 * min(1.0, dens * 1.2 + rim * 0.15 * edge)])
        px.append(row)
    return px
tiles = [cloud(s) for s in (7, 23, 31)]
png.write(os.path.join(out, "Fire_Clouds_Atlas.png"), N * 3, N, [sum((t[y] for t in tiles), []) for y in range(N)])
# Tourbillon : 4 bras de feu en spirale, coeur blanc-jaune
c = (N - 1) / 2; px = []
for y in range(N):
    row = []
    for x in range(N):
        d = math.hypot(x - c, y - c) / c; a = math.atan2(y - c, x - c)
        if d > 1: row.append([0, 0, 0, 0]); continue
        arm = (0.5 + 0.5 * math.cos(4 * (a + d * 5.5))) ** 3
        core = max(0.0, 1 - d / 0.25)
        fade = min(1.0, (1 - d) / 0.25)
        k = min(1.0, arm * (1 - d) * 1.6 + core)
        row.append([255, 70 + 170 * k, 10 + 120 * k ** 2, 255 * min(1.0, (arm * 0.85 + core) * fade)])
    px.append(row)
png.write(os.path.join(out, "Fire_Vortex.png"), N, N, px)
print("ok")
