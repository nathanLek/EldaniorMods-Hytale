# Cycle d'exécution d'une rune

| Étape | Où | Détail |
|---|---|---|
| 1. Conditions (classe) | `RuneManager` / `RuneSystem` | une rune non autorisée posée dans un slot retourne dans le sac (toutes les secondes) |
| 2. Mana, recharge | JSON `StatsCondition` (`UseAbilityCost`) + moteur Hytale | échec → `Ability_Cast_Fail` ; la recharge (`Cooldown` de l'item) bloque le relancement |
| 3. Animation | JSON `Simple` + `Effects` (incantation, `HorizontalSpeedMultiplier`) | durée = `chargeTime` |
| 4. Visuels/sons d'incantation | JSON `Effects.Particles` / `WorldSoundEventId` / `CameraEffect` | |
| 5. Déclenchement | JSON `{"Type": "RuneSpell", "Spell": id}` → `RuneSpellRuntime.queue` | exécuté au tick suivant du lanceur |
| 6–8. Effet, impact | `RuneSpell.start` puis `Instance.tick` | dégâts, soins, statuts, terrain, visuels |
| 9. Mana + recharge | JSON `ChangeStat` + `TriggerCooldown` | moment configurable : `cast.costMoment` |
| 10. Nettoyage | `Instance.cleanup(cb)` / `abort()` | entités temporaires, terrain (restauration programmée) |

## Moment de consommation (`cast.costMoment` dans la fiche)
- `start` (défaut, comme les sorts divins) : mana et recharge dès l'appui ; une incantation interrompue est perdue.
- `release` : mana et recharge juste avant l'effet ; une incantation interrompue ne coûte rien.

## Garanties fournies par `RuneSpellRuntime`
- Une exception dans `start`/`tick` arrête et nettoie **ce** sort seulement (le monde ne plante pas, contrairement aux sorts historiques).
- File vidée à chaque tick : un lancement n'est exécuté qu'une fois (pas de double mana : le JSON ne passe qu'une fois).
- Déconnexion : file vidée, `abort()` appelé (pas de `CommandBuffer` : ne pas tenter d'entités ici, le terrain se restaure seul).
- Lanceur invalide (mort, changement de monde) : instance terminée puis `cleanup`.

## Cas limites à traiter dans chaque sort
- **Une entité créée par le code (`spawnModel`, `spawnBlock`) n'existe qu'à la fin du tick** : son `Ref` est invalide pendant le tick
  de création. Ne jamais la considérer comme perdue tout de suite : garder un horodatage de création et un délai de grâce (~1 s,
  `SPAWN_GRACE_MS` dans `FujinWrath`). Sinon elle n'est plus suivie, donc jamais déplacée ni supprimée (bug « lames qui ne
  disparaissent pas » et « blocs jamais aspirés » de Fujin).
- `SpellFx.position(ref)` renvoie `null` si l'entité a été retirée (mort, chunk déchargé) : toujours tester.
  Les entités du sort (modèles, débris) peuvent être supprimées par le moteur si elles entrent dans un chunk non chargé.
- Cible morte en cours d'effet : `DeathComponent` présent → ignorer.
- Zone non chargée (téléportation lointaine) : `WorldBlockUtil.getBlockType` renvoie `null` → attendre ou annuler proprement.
- Joueur en vol / dans l'eau : ne pas poser de terrain sous lui sans vérifier `isEmpty` / fluide.
- Plusieurs lancements simultanés du même sort : chaque lancement a sa propre `Instance` (pas d'état statique partagé).
- Interruption volontaire (re-clic, changement d'arme) : prévoir un champ `cancelled` lu dans `tick` si la fiche le demande.
- Clics souris pendant un sort (contrôle) : enregistrer un `PlayerMouseButtonEvent` (voir `NyxVeil.onMouseButton`).
