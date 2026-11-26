"""Thes et onguents du Clan Luo, vendus par l'herboriste et la maison de the (marchand Luo, scripts/guards/gen_marchand_luo.py).

  Luo_The_Vert          Commun      the vert de la vallee : endurance rendue (Potion_Stamina_Instant_Small)
  Luo_The_Jasmin        Rare        the au jasmin des Mille Cerisiers : regeneration (Potion_Health_Regen_Small)
  Luo_The_Jade          Epique      the de jade imperial : regeneration et endurance (Potion_Health_Regen + Stamina_Instant)
  Luo_The_Eclipse       Legendaire  the noir de l'Eclipse : grande regeneration et force (Potion_Health_Regen_Large + Eld_Effet_Rage)
  Luo_Onguent_Cerisier  Rare        onguent de fleur de cerisier : soin immediat (Potion_Health_Instant_Lesser)
  Luo_Onguent_Dragon    Unique      onguent du Dragon d'Or : soin et peau de pierre (Potion_Health_Instant + Eld_Effet_Peau_Pierre)

Les thes sont des chawan (bols) fumants, les onguents des pots de laque au couvercle ; dessin et modele 3D par le moteur des
butins (scripts/butin/sprites.py : icone 64x64, sprite extrude). Consommes comme une potion (clic droit, animation de boisson).
Aucune recette ni butin : seuls les marchands du clan les vendent.

Usage : python3 scripts/consommables/gen_luo_consommables.py [dossier_apercu]
"""
import json, math, os, sys, zlib

HERE = os.path.dirname(os.path.abspath(__file__))
for folder in ("butin", "rune_aura_icons", "armures"):
    sys.path.insert(0, os.path.join(HERE, "..", folder))
import png  # noqa: E402
import sprites  # noqa: E402
from sprites import Art, GOLD, dk, lt  # noqa: E402
import gen_butin  # noqa: E402
import familles  # noqa: E402

RES = os.path.join(HERE, "..", "..", "src", "main", "resources")
PREVIEW = sys.argv[1] if len(sys.argv) > 1 else None
LACQUER, LACQUER_RED, CERAMIC = "#1c1820", "#8e1a26", "#d8cdb8"


def chawan(a, glaze, rim, tea, steam=True, foot=None):
    """Bol a the vu de trois quarts : pied, corps evase, the a la surface, vapeur."""
    a.L(foot or dk(glaze, .35), "grain", 4).rect(38, 76, 62, 86)
    a.L(glaze, "smoke", 12, gloss=.8).blob([(14, 44), (86, 44), (80, 64), (66, 78), (34, 78), (20, 64)])
    a.L(rim, "metal", 4, gloss=1).ell(50, 44, 37, 9)
    a.L(tea, "glow", 6).ell(50, 45, 32, 6.5)
    a.L(lt(tea, .5), "none", 2).ell(40, 44, 7, 1.6)
    if steam:
        v = a.L("#f4f0ea", "glow", 3)
        for x0, ph in ((40, 0), (54, 1.6), (64, 3)):
            v.tube([(x0 + 4 * math.sin(ph + t * .5), 36 - t * 4.2) for t in range(7)], 2.2, .8)


def pot(a, body, lid, band, knob, mon=None):
    """Pot d'onguent de laque : corps rond, bande d'or, couvercle bombe et bouton."""
    a.L(body, "metal", 12, gloss=1).blob([(18, 50), (82, 50), (84, 72), (70, 88), (30, 88), (16, 72)])
    a.L(band, "metal", 3, gloss=1).rect(17, 49, 83, 55)
    a.L(lid, "metal", 10, gloss=1).blob([(20, 50), (28, 36), (50, 30), (72, 36), (80, 50)])
    a.L(knob, "facets", 4, gloss=1).ell(50, 28, 7, 5)
    if mon:
        a.L(mon, "glow", 3).ell(50, 70, 8).ell(50, 70, 5, val=0)


def the_vert(a):
    chawan(a, "#5d7a4a", "#c8b890", "#8fbf4a")


def the_jasmin(a):
    chawan(a, "#e8e0d4", "#d8a8b8", "#e8d870")
    f = a.L("#f0b8c8", "glow", 3)
    for i in range(5):                                       # fleur de cerisier sur le bol
        t = i * 2 * math.pi / 5 - 1.57
        f.ell(32 + 5 * math.cos(t), 62 + 5 * math.sin(t), 3)
    a.L("#f8e070", "glow", 2).ell(32, 62, 1.6)


def the_jade(a):
    chawan(a, "#2f8a6a", GOLD, "#b8e070")
    a.L(GOLD, "metal", 2, gloss=1).tube([(22, 60), (78, 60)], 1.4, 1.4)


def the_eclipse(a):
    h = a.L("#ffd860", "glow", 3)
    h.ell(50, 40, 44, 44)
    h.ell(50, 40, 40, 40, val=0)
    chawan(a, "#141018", GOLD, "#3a1a10", foot=GOLD)
    a.L("#0a0810", "void", 4, glow="#ffcc40").ell(50, 45, 9, 3.4)


def onguent_cerisier(a):
    pot(a, "#e0a8b8", LACQUER_RED, GOLD, "#f4d0d8", mon="#fff0f4")


def onguent_dragon(a):
    pot(a, LACQUER, GOLD, "#b81c28", GOLD, mon=GOLD)


ITEMS = [
    # id, nom, qualite, niveau, dessin, effets, description
    ("Luo_The_Vert", "Thé vert de la vallée", "Common", 20, the_vert, ["Potion_Stamina_Instant_Small"],
     "Cueilli sur les pentes de la vallée des Mille Cerisiers. Boire : rend un peu d'endurance."),
    ("Luo_The_Jasmin", "Thé au jasmin des Mille Cerisiers", "Rare", 90, the_jasmin, ["Potion_Health_Regen_Small"],
     "Thé parfumé servi aux hôtes du clan. Boire : régénère la santé."),
    ("Luo_The_Jade", "Thé de jade impérial", "Epic", 200, the_jade, ["Potion_Health_Regen", "Potion_Stamina_Instant"],
     "Réservé aux tables des Nobles. Boire : régénère la santé et rend de l'endurance."),
    ("Luo_The_Eclipse", "Thé noir de l'Éclipse", "Legendary", 500, the_eclipse, ["Potion_Health_Regen_Large", "Eld_Effet_Rage"],
     "Infusé une fois l'an, le jour de l'éclipse. Boire : grande régénération et fureur du Dragon d'Or."),
    ("Luo_Onguent_Cerisier", "Onguent de fleur de cerisier", "Rare", 80, onguent_cerisier, ["Potion_Health_Instant_Lesser"],
     "Baume des dojos du clan. Appliquer : soigne aussitôt."),
    ("Luo_Onguent_Dragon", "Onguent du Dragon d'Or", "Unique", 380, onguent_dragon, ["Potion_Health_Instant", "Eld_Effet_Peau_Pierre"],
     "Secret des Doyens. Appliquer : soigne et durcit la peau comme la pierre."),
]

CONSUME_ANIMS = {"Parent": "Item", "Animations": {"Consume": {
    "FirstPerson": "Characters/Animations/Items/Main_Handed/Item/Attacks/Drink/Drink_FPS.blockyanim",
    "ThirdPerson": "Characters/Animations/Items/Main_Handed/Item/Attacks/Drink/Drink.blockyanim", "Speed": 1, "Looping": True}}}


def item_json(iid, name, quality, level, effects, desc):
    return {
        "$Comment": "Consommable du Clan Luo, genere par scripts/consommables/gen_luo_consommables.py. Vendu par les marchands Luo.",
        "TranslationProperties": {"Name": name, "Description": desc},
        "Quality": quality, "ItemLevel": level, "Categories": [familles.category("Comestibles")],
        "Icon": "Icons/ItemsGenerated/%s.png" % iid,
        "IconProperties": {"Scale": 0.8, "Rotation": [0, 0, 0], "Translation": [0, -8]},
        "Model": "Items/Consommables/%s.blockymodel" % iid, "Texture": "Items/Consommables/%s.png" % iid,
        "PlayerAnimationsId": CONSUME_ANIMS,
        "Interactions": {"Secondary": "Root_Secondary_Consume_Potion"},
        "InteractionVars": {
            "RemoveEffect": {"Interactions": [{"Type": "Simple"}]},
            "Effect": {"Interactions": [{"Type": "Serial", "Interactions": [{"Type": "ApplyEffect", "EffectId": e} for e in effects]}]},
            "ConsumeSFX": {"Interactions": [{"Parent": "Consume_SFX", "Effects": {"LocalSoundEventId": "SFX_Health_Potion_Low_Drink"}}]},
            "ConsumedSFX": {"Interactions": [{"Parent": "Consumed_SFX", "Effects": {"LocalSoundEventId": "SFX_Potion_Drink_Success"}}]},
        },
        "Consumable": True, "Utility": {"Compatible": False},
        "Tags": {"Type": ["Potion"]}, "MaxStack": 10, "DropOnDeath": True,
    }


def main():
    models = os.path.join(RES, "Common", "Items", "Consommables")
    icons = os.path.join(RES, "Common", "Icons", "ItemsGenerated")
    os.makedirs(models, exist_ok=True)
    previews = []
    for iid, name, quality, level, draw, effects, desc in ITEMS:
        a = Art(zlib.crc32(iid.encode()), spin=4, zoom=(.97, 1.02), flip=False)
        draw(a)
        ic, sp = sprites.render(a, "#14101c")
        png.write(os.path.join(icons, iid + ".png"), 64, 64, ic)
        mdl, tex = gen_butin.model(sp)
        png.write(os.path.join(models, iid + ".png"), 64, 32, tex)
        with open(os.path.join(models, iid + ".blockymodel"), "w") as f:
            json.dump(mdl, f, separators=(",", ":"))
        path = os.path.join(RES, "Server", "Item", "Items", "Eldanior", "Luo", iid + ".json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(item_json(iid, name, quality, level, effects, desc), f, indent=2, ensure_ascii=False)
            f.write("\n")
        previews.append(ic)
    if PREVIEW:
        S = 4
        out = [[[40, 40, 48, 255] for _ in range(64 * S * len(previews))] for _ in range(64 * S)]
        for k, ic in enumerate(previews):
            for y in range(64):
                for x in range(64):
                    c = ic[y][x]
                    if c[3]:
                        for dy in range(S):
                            for dx in range(S):
                                out[y * S + dy][k * 64 * S + x * S + dx] = list(c[:3]) + [255]
        os.makedirs(PREVIEW, exist_ok=True)
        png.write(os.path.join(PREVIEW, "luo_consommables.png"), 64 * S * len(previews), 64 * S, out)
    print("Consommables Luo :", len(ITEMS))


if __name__ == "__main__":
    main()
