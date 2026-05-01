# Validation Effets Visuels

#optimisation #haute #effets #skills

## Probleme double

### 1. Assets non verifies au demarrage
Les 214 mappings skill→effet referencent des noms d'assets Hytale (`Red_Flash`, `Stoneskin`, `Intangible_Dark`...) qui ne sont **jamais valides** au demarrage.

**Fichier** : `config/Effects/SkillEffectConfig.java` lignes 19-323

Si un asset n'existe pas :
- L'effet silencieusement ne se declenche pas (catch ignored)
- Aucun message d'erreur
- Le joueur ne voit jamais l'effet → experience degradee

### 2. ~50 skills sans mapping d'effet
Sur 301 skills, environ 50 n'ont aucune entree dans `ATTACKER_EFFECTS` ou `VICTIM_EFFECTS` :
- Tous les skills "Vie" (Heart of Oak, Perseverance...)
- Certains skills "Resistance" (Tenacity, Hardening...)
- Plusieurs skills Rare et Uncommon

## Assets potentiellement invalides
| Nom asset | Utilise par | Risque |
|-----------|------------|--------|
| `Red_Flash` | 19 skills (critiques) | A verifier |
| `Stoneskin` | 31 skills (defense) | Non standard |
| `Intangible_Dark` | 10 skills (neant) | Non standard |
| `Stun` | 3 skills | Peut ne pas exister |
| `Immune` | 11 skills | A verifier |

## Correction proposee

### Validation au demarrage
```java
public static void validateEffects() {
    int valid = 0, invalid = 0;
    Set<String> allEffects = new HashSet<>();
    allEffects.addAll(ATTACKER_EFFECTS.values());
    allEffects.addAll(VICTIM_EFFECTS.values());
    
    for (String effectName : allEffects) {
        if (EffectsManager.getEffectAsset(effectName) != null) {
            valid++;
        } else {
            invalid++;
            System.err.println("[Eldanior] Effet invalide: " + effectName);
        }
    }
    System.out.println("[Eldanior] Effets valides: " + valid + "/" + (valid + invalid));
}
```

### Completer les mappings manquants
- Lister tous les PassiveSkill sans mapping
- Assigner un effet par defaut par categorie (ex: tous les skills Vie → `Heal` ou `Regen`)

## Priorite
**HAUTE** — Affecte l'experience visuelle de 50+ competences

## Liens
- [[Systems/Skills]] - 301 competences passives
- [[Utilitaires/Effets Visuels]] - Liste des effets Hytale