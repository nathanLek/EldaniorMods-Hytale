"""Icone de l'onglet Groupe du menu systeme (2026-11-12) : trois silhouettes dorees (aucun objet du jeu n'evoque un groupe).

Sortie : src/main/resources/Common/UI/Custom/System/TabIconGroupe@2x.png (76 px, affichee en 38 dans SystemPage.ui,
bouton de 70 : une icone plus grande depassait du cadre ; marge transparente autour des silhouettes).
Python pur (zlib), sans dependance. Usage : python3 scripts/system/gen_icone_groupe.py [chemin_de_sortie]
"""
import os, struct, sys, zlib

N = 76           # taille finale (2x)
K = N / 104      # dessin prevu pour 104 px, ramene a N avec une marge (MARGE) tout autour
MARGE = 4
SS = 4           # sur-echantillonnage pour l'anticrenelage
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", "..",
      "src/main/resources/Common/UI/Custom/System/TabIconGroupe@2x.png")

OUTLINE = (42, 26, 8)
BACK = (176, 124, 44)       # silhouettes de derriere
BACK_HI = (214, 160, 70)
FRONT = (240, 190, 84)      # silhouette de devant
FRONT_HI = (255, 226, 140)


def figure(cx, head_y, head_r, body_y, body_rx, body_ry, bottom):
    k = (N - 2 * MARGE) / 104
    cx, head_y, head_r, body_y, body_rx, body_ry, bottom = (MARGE + cx * k, MARGE + head_y * k, head_r * k, MARGE + body_y * k,
                                                            body_rx * k, body_ry * k, MARGE + bottom * k)
    """Fonction d'appartenance : 0 dehors, 1 dans la silhouette (tete ronde + epaules en demi-ellipse)."""
    def inside(x, y, grow=0.0):
        if (x - cx) ** 2 + (y - head_y) ** 2 <= (head_r + grow) ** 2:
            return True
        if y <= bottom + grow and ((x - cx) / (body_rx + grow)) ** 2 + ((y - body_y) / (body_ry + grow)) ** 2 <= 1:
            return True
        return False
    return inside


# (fonction, couleur, reflet) dans l'ordre de dessin : les deux de derriere, puis celle de devant
figs = [
    (figure(27, 35, 11, 86, 21, 25, 92), BACK, BACK_HI),
    (figure(77, 35, 11, 86, 21, 25, 92), BACK, BACK_HI),
    (figure(52, 40, 14, 96, 27, 32, 100), FRONT, FRONT_HI),
]

img = [[(0, 0, 0, 0)] * N for _ in range(N)]
for py in range(N):
    for px in range(N):
        acc = [0.0, 0.0, 0.0]; alpha = 0.0
        for sy in range(SS):
            for sx in range(SS):
                x = px + (sx + 0.5) / SS; y = py + (sy + 0.5) / SS
                col = None
                for f, c, hi in figs:
                    if f(x, y, 3.0 * K):
                        col = OUTLINE
                    if f(x, y):
                        # reflet en haut a gauche de chaque forme
                        col = hi if f(x + 3 * K, y + 3 * K) and not f(x - 3 * K, y - 3 * K) else c
                if col:
                    acc = [a + b for a, b in zip(acc, col)]; alpha += 1
        if alpha:
            img[py][px] = tuple(int(a / alpha) for a in acc) + (int(255 * alpha / (SS * SS)),)

raw = b"".join(b"\x00" + bytes(v for p in row for v in p) for row in img)
def chunk(t, d): return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", N, N, 8, 6, 0, 0, 0)) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
os.makedirs(os.path.dirname(os.path.abspath(OUT)), exist_ok=True)
open(OUT, "wb").write(png)
print("ecrit", os.path.abspath(OUT))
