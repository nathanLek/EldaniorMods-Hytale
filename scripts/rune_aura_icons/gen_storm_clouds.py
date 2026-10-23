# Couche de nuages d'orage (Sanctuaire de Taranis) : bruit fractal gris-noir, bords fondus. Usage : python3 gen_storm_clouds.py <sortie.png> [graine]
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
N = 128
random.seed(int(sys.argv[2]) if len(sys.argv) > 2 else 3)
def grid(n):
    return [[random.random() for _ in range(n + 1)] for _ in range(n + 1)]
octaves = [(grid(4), 4, 0.5), (grid(8), 8, 0.27), (grid(16), 16, 0.15), (grid(32), 32, 0.08)]
def smooth(t): return t * t * (3 - 2 * t)
def noise(g, n, x, y):
    fx, fy = x * n, y * n; ix, iy = int(fx), int(fy); tx, ty = smooth(fx - ix), smooth(fy - iy)
    a = g[iy][ix] + (g[iy][ix + 1] - g[iy][ix]) * tx
    b = g[iy + 1][ix] + (g[iy + 1][ix + 1] - g[iy + 1][ix]) * tx
    return a + (b - a) * ty
px = []
c = (N - 1) / 2
for y in range(N):
    row = []
    for x in range(N):
        u, v = x / N, y / N
        n = sum(noise(g, k, u, v) * w for g, k, w in octaves)          # ~0..1
        d = math.hypot(x - c, y - c) / c                               # 0 centre, 1 bord
        edge = max(0.0, min(1.0, (1 - d) * 1.8 + (n - 0.5) * 1.2))   # bord irregulier, pas un disque
        dens = max(0.0, min(1.0, (n - 0.28) * 2.2)) * edge
        shade = 30 + 55 * n                                             # gris-noir avec reliefs
        row.append([shade * 0.92, shade * 0.95, shade * 1.08, 255 * min(1.0, dens * 1.15)])
    px.append(row)
png.write(sys.argv[1], N, N, px)
