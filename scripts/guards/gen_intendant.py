"""Reserve de l'intendant (2026-11-10, categorie « Achats » de sa fenetre) : une boutique par famille,
Eldanior_Intendant_<Famille>, ouverte aux dirigeants de la famille depuis l'intendant du territoire (FamilleTab, fam_reserve).

Contenu : les objets de la categorie creative de la famille (Eldanior_Familles.<Id>, ou Eldanior_Empire_Luo.<Id> pour les
cinq clans de l'Empire ; les pages communes Civils et Comestibles de l'Empire vont a la reserve Luo) utiles a la maison : banniere,
presentoir, porte de famille et tenues des membres. Exclus : cles (uniques), plaques (donnees par le rang), reliques
(Artefact_*), manuels et runes (Sensei), tenues de chef (Noble_*, Patriarche, Roi, Empereur, Seigneur, Divin) et tout ce qui
est Legendaire, Unique ou Divin. Prix : bareme des boutiques (gen_marchand_luo.price), payes en pieces par l'acheteur
(paiement par la tresorerie : a faire). Reassort tous les 2 jours.

Usage : python3 scripts/guards/gen_intendant.py [src/main/resources]
"""
import glob, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_marchand_luo as g  # noqa: E402

RES = g.RES
EXCLUDE = ("Eldanior_Cle_", "Eldanior_Plaque_", "Artefact_", "Eldanior_Manuel_", "Eldanior_Mouvement_", "Rune_", "Eldanior_Parchemin_",
           "Eldanior_Weapon_", "Armor_Kimono_Noble", "Armor_Runkandel_Patriarche", "Armor_Eldanior_Roi", "Armor_Kimono_Empereur",
           "Armor_Kimono_Seigneur", "Armor_Kimono_Divin", "Armor_Kimono_Garde_Royale", "Armor_Kimono_Alchimiste")
HIGH = {"Legendary", "Unique", "Divin"}
# pages communes de l'Empire Luo : Civils et Comestibles restent a la reserve du Clan Luo (comme avant le 2026-11-11)
EMPIRE_SHARED = {"Civils": "Luo", "Comestibles": "Luo", "ArtsMartiaux": None, "Runes": None}


def main():
    shops = {}
    for f in glob.glob(os.path.join(RES, "Server", "Item", "Items", "**", "*.json"), recursive=True):
        try:
            d = json.load(open(f, encoding="utf-8"))
        except Exception:
            continue
        iid = os.path.basename(f)[:-5]
        if iid.startswith(EXCLUDE) or d.get("Quality") in HIGH:
            continue
        for c in d.get("Categories") or []:
            if isinstance(c, str) and c.startswith(("Eldanior_Familles.", "Eldanior_Empire_Luo.")):
                family = c.split(".", 1)[1]
                family = EMPIRE_SHARED.get(family, family)
                if family:
                    shops.setdefault(family, []).append((d.get("ItemLevel") or 0, iid))
    for family, items in sorted(shops.items()):
        items.sort()
        slots = []
        for _, iid in items:
            deco = iid.startswith(("Eldanior_Banniere_", "Eldanior_Presentoir_", "Eldanior_Porte_"))
            slots.append(g.fixed(iid, 1, 4 if deco else 6))
        g.write("Server/BarterShops/Eldanior_Intendant_%s.json" % family, {
            "DisplayNameKey": "Réserve de la famille " + family, "RefreshInterval": {"Days": 2}, "RestockHour": 6, "TradeSlots": slots})
        print("Eldanior_Intendant_%-11s %d articles" % (family, len(slots)))


if __name__ == "__main__":
    main()
