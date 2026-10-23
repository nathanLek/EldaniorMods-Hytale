"""Texture du cercle magique rouge du Courroux de Muspelheim (quad 128x128, fond transparent).
Usage : python3 gen_sky_circle.py <fichier_sortie.png>"""
import sys, os, math, random
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png

RED = (255, 40, 30); CORE = (255, 190, 150)

def seg_dist(px, py, ax, ay, bx, by):
    vx, vy = bx - ax, by - ay; wx, wy = px - ax, py - ay
    t = max(0.0, min(1.0, (wx * vx + wy * vy) / (vx * vx + vy * vy)))
    return math.hypot(px - (ax + vx * t), py - (ay + vy * t))

def main(path, size=128, ss=3, seed=11, color=None, core=None):
    global RED, CORE
    if color: RED = color
    if core: CORE = core
    rnd = random.Random(seed); c = size / 2; R = size / 2 - 2
    # Runes : petits traits disposes sur la bande entre les anneaux
    runes = []
    n = 28
    for i in range(n):
        a = i / n * 2 * math.pi
        for _ in range(3):
            r1 = R * rnd.uniform(0.74, 0.86); r2 = R * rnd.uniform(0.74, 0.86)
            da = rnd.uniform(-0.05, 0.05)
            runes.append((c + math.cos(a) * r1, c + math.sin(a) * r1, c + math.cos(a + da) * r2, c + math.sin(a + da) * r2))
    # Hexagramme inscrit
    star = []
    for k in range(6):
        a1 = k / 6 * 2 * math.pi - math.pi / 2; a2 = ((k + 2) % 6) / 6 * 2 * math.pi - math.pi / 2
        rs = R * 0.66
        star.append((c + math.cos(a1) * rs, c + math.sin(a1) * rs, c + math.cos(a2) * rs, c + math.sin(a2) * rs))
    px = [[[0, 0, 0, 0] for _ in range(size)] for _ in range(size)]
    for y in range(size):
        for x in range(size):
            acc = 0.0
            for sy in range(ss):
                for sx in range(ss):
                    fx = x + (sx + .5) / ss; fy = y + (sy + .5) / ss
                    d = math.hypot(fx - c, fy - c)
                    v = 0.0
                    if abs(d - R * 0.97) < R * 0.03: v = 1.0                      # anneau exterieur epais
                    elif abs(d - R * 0.90) < R * 0.008: v = 0.9                  # filet
                    elif abs(d - R * 0.70) < R * 0.012: v = 0.95                 # anneau interieur
                    elif abs(d - R * 0.66) < R * 0.006: v = 0.8
                    elif abs(d - R * 0.18) < R * 0.012: v = 0.9                  # petit cercle central
                    if v < 1.0 and R * 0.72 < d < R * 0.88:
                        for (ax, ay, bx, by) in runes:
                            if seg_dist(fx, fy, ax, ay, bx, by) < 0.7: v = max(v, 0.85); break
                    if v < 1.0 and d < R * 0.67:
                        for (ax, ay, bx, by) in star:
                            if seg_dist(fx, fy, ax, ay, bx, by) < 0.75: v = max(v, 0.9); break
                    acc += v
            a = acc / (ss * ss)
            if a > 0:
                d = math.hypot(x + .5 - c, y + .5 - c) / R
                col = [RED[i] + (CORE[i] - RED[i]) * max(0.0, 1 - d * 3) for i in range(3)]
                px[y][x] = [col[0], col[1], col[2], 255 * min(1.0, a * 1.15)]
    png.write(path, size, size, px)

if __name__ == '__main__':
    # Usage : gen_sky_circle.py <sortie.png> [storm]  (storm = version electrique blanc-bleu du Sanctuaire de Taranis)
    if len(sys.argv) > 2 and sys.argv[2] == 'storm':
        main(sys.argv[1], color=(150, 210, 255), core=(255, 255, 230))
    else:
        main(sys.argv[1])
    print('ok')
