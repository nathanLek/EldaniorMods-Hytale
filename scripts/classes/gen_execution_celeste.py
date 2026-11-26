"""Assets de l'Execution Celeste (rune de famille du Clan Luo, 2026-11-11) : remanences dorees du lanceur.

  ModelVFX Execution_Remanence  corps translucide or, liseré lumineux (comme Pas_Leger_Fantome, plus opaque)
  Effet    Execution_Remanence  pose sur chaque image remanente (porteur Projectile_Config_Chronos_Ghost habille du lanceur)
  ModelVFX Execution_Ame / effet Execution_Ame : ame de Ki blanc nacre du Dieu Martial, derriere le lanceur

Usage : python3 scripts/classes/gen_execution_celeste.py [src/main/resources]
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "src", "main", "resources")


def write(rel, data):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def vfx(name, color, opacity):
    write("Server/Entity/ModelVFX/%s.json" % name, {
        "SwitchTo": "PostColor", "EffectDirection": "BottomUp", "AnimationDuration": 0.12, "CurveType": "QuartOut",
        "NoiseScale": {"X": 20.0, "Y": 20.0}, "PostColor": color, "PostColorOpacity": opacity,
        "HighlightColor": color, "HighlightThickness": 2.0, "UseBloomOnHighlight": True})


def effect(name, vfx_id, top, bottom, duration, particles, comment):
    app = {"ModelVFXId": vfx_id, "EntityTopTint": top, "EntityBottomTint": bottom}
    if particles:
        app["Particles"] = [{"SystemId": particles, "TargetEntityPart": "Entity"}]
    write("Server/Entity/Effects/Eldanior/Ki/%s.json" % name, {"$Comment": comment + " (gen_execution_celeste.py).",
                                                               "Duration": duration, "OverlapBehavior": "Overwrite",
                                                               "Debuff": False, "ApplicationEffects": app})


def main():
    vfx("Execution_Remanence", "#ffcf4a", 0.6)
    effect("Execution_Remanence", "Execution_Remanence", "#fff4c8", "#ffb52e", 3.0, None,
           "Execution Celeste : image remanente doree du lanceur")
    vfx("Execution_Ame", "#f4f1ff", 0.45)
    effect("Execution_Ame", "Execution_Ame", "#ffffff", "#e8d9ff", 4.0, "Ki_Hwakyung",
           "Execution Celeste, Dieu Martial : ame de Ki blanc nacre")
    print("Execution Celeste : 2 ModelVFX, 2 effets.")


if __name__ == "__main__":
    main()
