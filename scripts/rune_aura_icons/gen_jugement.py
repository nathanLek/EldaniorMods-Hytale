"""Images de la Rune du Courroux de Muspelheim (meteorite enflammee).
Usage : python3 gen_jugement.py <dossier_sortie> <dossier_contenant_Common/...extrait d'Assets.zip>"""
import sys, math, os, random, colorsys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png
from gen import blend, add

# Palette feu : du coeur blanc a la queue rouge sombre
FIRE = [(0.0, (255, 250, 225)), (0.18, (255, 214, 92)), (0.45, (255, 128, 28)), (0.75, (196, 40, 12)), (1.0, (90, 10, 6))]
ROCK = (58, 34, 26)

def fire_color(t):
    t = max(0.0, min(1.0, t))
    for (a, ca), (b, cb) in zip(FIRE, FIRE[1:]):
        if t <= b:
            k = (t - a) / (b - a)
            return tuple(ca[i] + (cb[i] - ca[i]) * k for i in range(3))
    return FIRE[-1][1]

def meteor(px, w, h, hx, hy, R, L, ss=3, alpha=1.0, rock=True):
    """Meteorite : tete (rocher + bord incandescent) en (hx,hy), queue de longueur L vers le haut-droite."""
    d = (1 / math.sqrt(2), -1 / math.sqrt(2))            # direction de la queue (haut-droite)
    lead = (-d[0], -d[1])                                 # sens de chute (bas-gauche)
    for y in range(max(0, int(hy - L - R - 4)), min(h, int(hy + R + 4))):
        for x in range(max(0, int(hx - R - 4)), min(w, int(hx + L + R + 4))):
            tail_a = head_a = rock_a = 0.0; tail_t = 0.0
            for sy in range(ss):
                for sx in range(ss):
                    fx = x + (sx + .5) / ss - hx; fy = y + (sy + .5) / ss - hy
                    t = fx * d[0] + fy * d[1]; s = abs(-fx * d[1] + fy * d[0])
                    if 0 < t < L and s < R * (1 - t / L) ** 0.7: tail_a += 1; tail_t += t / L
                    r = math.hypot(fx, fy)
                    if r < R: head_a += 1
                    # rocher decale vers l'avant : le bord arriere reste incandescent
                    if math.hypot(fx - lead[0] * R * 0.25, fy - lead[1] * R * 0.25) < R * 0.72: rock_a += 1
            n = ss * ss
            p = px[y][x]
            if tail_a:
                t = tail_t / tail_a
                blend(p, fire_color(0.15 + t * 0.85), tail_a / n * alpha * (1 - t * 0.6))
            # halo lumineux autour de la tete
            g = math.exp(-(math.hypot(x + .5 - hx, y + .5 - hy) / (R * 1.6)) ** 2)
            add(p, (255, 150, 50), g * 0.55 * alpha)
            if head_a: blend(p, fire_color(0.08), head_a / n * alpha)
            if rock and rock_a:
                # rocher sombre, chauffe au rouge sur son pourtour (degrade de chaleur, pas de motif)
                fx, fy = x + .5 - hx - lead[0] * R * 0.25, y + .5 - hy - lead[1] * R * 0.25
                heat = min(1.0, (math.hypot(fx, fy) / (R * 0.72)) ** 2.2)
                col = tuple(ROCK[i] + ((255, 110, 30)[i] - ROCK[i]) * heat for i in range(3))
                blend(p, col, rock_a / n * alpha * 0.95)

def ability_icon(path, size=128, seed=7):
    rnd = random.Random(seed); c = size / 2
    px = [[[0, 0, 0, 255] for _ in range(size)] for _ in range(size)]
    for y in range(size):
        for x in range(size):
            d = math.hypot(x + .5 - c * 0.7, y + .5 - c * 1.3) / size
            px[y][x] = [30 * (1 - d) + 5, 12 * (1 - d) + 3, 34 * (1 - d) + 10, 255]
    for _ in range(60):                                   # etoiles
        x, y = rnd.randrange(size), rnd.randrange(size); b = rnd.uniform(0.3, 1.0)
        add(px[y][x], (255, 240, 220), b * 0.8)
    meteor(px, size, size, size * 0.80, size * 0.30, size * 0.045, size * 0.28, alpha=0.55)   # petite, fond
    meteor(px, size, size, size * 0.22, size * 0.45, size * 0.035, size * 0.22, alpha=0.45)
    meteor(px, size, size, size * 0.40, size * 0.66, size * 0.13, size * 0.62)                # principale
    for y in range(size):                                 # lueur d'impact en bas
        for x in range(size):
            g = math.exp(-((y - size) / (size * 0.18)) ** 2) * math.exp(-((x - size * 0.35) / (size * 0.45)) ** 2)
            add(px[y][x], (255, 90, 20), g * 0.6)
    for y in range(size):
        for x in range(size):
            e = min(x, y, size - 1 - x, size - 1 - y)
            if e < 3:
                for i in range(3): px[y][x][i] *= 0.55 + 0.15 * e
    png.write(path, size, size, px)

def item_icon(src, dst, cx=35.5, cy=33.3, gx=33, gy=34):
    """Pierre de rune vanilla, glyphe rouge efface, meteorite gravee au centre (meme centrage que la Rune d'Aura)."""
    w, h, px = png.read(src)
    def warmth(p): return max(0.0, min(1.0, (p[0] - p[2] - 15) / 90.0)) if p[3] > 0 else 0
    stone = [p for row in px for p in row if p[3] > 200 and warmth(p) == 0]
    sc = [sum(p[i] for p in stone) / len(stone) for i in range(3)]
    for y in range(h):
        for x in range(w):
            p = px[y][x]; k = warmth(p)
            if k > 0 and math.hypot(x - gx, y - gy) < 22:
                for i in range(3): p[i] = p[i] * (1 - k) + sc[i] * 0.8 * k
    # meteorite centree : la tete en bas-gauche, la queue vers le haut-droite
    meteor(px, w, h, cx - 4.0, cy + 4.0, 4.6, 15, ss=4, rock=False)
    png.write(dst, w, h, px)

def texture(src, dst):
    """Texture 3D : glyphes recolores en orange incandescent."""
    w, h, px = png.read(src)
    for row in px:
        for p in row:
            if p[3] == 0: continue
            hh, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in p[:3]))
            if s > 0.35 and (hh < 0.12 or hh > 0.9):
                r, g, b = colorsys.hsv_to_rgb(0.07, min(1, s), min(1, v * 1.15))
                p[0], p[1], p[2] = r * 255, g * 255, b * 255
    png.write(dst, w, h, px)

if __name__ == '__main__':
    out, src = sys.argv[1], sys.argv[2]
    ability_icon(out + '/jugement_ability.png')
    item_icon(src + '/Common/Icons/ItemsGenerated/Rune_Enrage.png', out + '/jugement_item.png')
    texture(src + '/Common/Items/Runes/Rune_Textures/Rune_Ability_Enrage.png', out + '/jugement_texture.png')
    print('ok')
