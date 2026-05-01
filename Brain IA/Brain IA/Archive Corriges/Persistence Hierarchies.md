# Persistence des Hierarchies (Noblesse, Eglise, Familles)

#bug #critique #persistence #hierarchie #CORRIGE

> **PARTIELLEMENT CORRIGE** le 2026-05-01 :
> - NobilityManager.saveTo()/loadFrom() ajoutes (Roi, compteurs, relations seigneur-chevalier)
> - ChurchManager.saveTo()/loadFrom() ajoutes (Pape, compteurs, relations maitre-acolyte)
> - PersistenceManager : saveHierarchies()/loadHierarchies() → hierarchies.properties
> - FamilyManager.takenFamilies : sauve/charge dans families.properties
> - Quetes actives : verifie — deja persistees via questData dans le CODEC
> - Duel history : duelHistoryData ajoute au CODEC avec serialisation/deserialisation
> - **TOUT RESOLU**

## Probleme (PARTIELLEMENT RESOLU)
Les **hierarchies sociales** (Noblesse et Eglise) sont stockees **uniquement en memoire**. Au restart du serveur, toutes les relations sont **perdues**.

---

## 1. NobilityManager — Hierarchie de noblesse NON persistee
**Fichier** : `titles/nobility/NobilityManager.java`

| Champ | Type | Persiste ? | Impact de la perte |
|-------|------|-----------|-------------------|
| `currentKingUUID` | UUID | NON | Plus de Roi au restart |
| `currentKingName` | String | NON | Nom du Roi perdu |
| `kingdomCounts` | Map | NON | Compteurs de rangs reset (ex: 4 Marquis max) |
| `lordOf` | Map | NON | Relations Chevalier→Seigneur perdues |
| `knightsOf` | Map | NON | Relations Seigneur→Chevaliers perdues |

**Ce qui EST sauve** (dans PlayerLevelData CODEC) :
- `nobilityRank` — le rang du joueur (Roi, Marquis, etc.)
- `nobleFamilyId` — sa famille
- `dignity` — ses points de dignite

**Ce qui est PERDU** :
- Qui est le Roi ? → On sait que quelqu'un est "Roi" dans son PlayerLevelData, mais le NobilityManager ne sait pas qui
- Combien de Marquis existent ? → Les limites (4 max) ne sont plus appliquees
- Qui est le seigneur de qui ? → Relations feodales detruites

### Scenario catastrophe
1. Serveur demarre, 1 Roi + 4 Marquis + 3 Ducs existent
2. Serveur restart
3. `kingdomCounts` est vide → le systeme pense qu'il n'y a aucun noble
4. Un joueur consomme un Decret Marquis → accepte (car compteur = 0/4)
5. Resultat : **5 Marquis** au lieu de 4 max

---

## 2. ChurchManager — Hierarchie d'eglise NON persistee
**Fichier** : `titles/church/ChurchManager.java`

| Champ | Type | Persiste ? | Impact |
|-------|------|-----------|--------|
| `currentPopeUUID` | UUID | NON | Plus de Pape au restart |
| `currentPopeName` | String | NON | Nom du Pape perdu |
| `churchCounts` | Map | NON | Compteurs reset |
| `acolytesOf` | Map | NON | Relations Acolyte→Maitre perdues |
| `masterOf` | Map | NON | Relations Maitre→Acolytes perdues |

**Meme probleme** : Les rangs sont dans PlayerLevelData, mais les relations et compteurs sont perdus.

---

## 3. FamilyManager — Familles prises NON persistees
**Fichier** : `titles/nobility/family/FamilyManager.java` ligne 15

```java
private static Set<String> takenFamilies;  // En memoire seulement !
```

**Ce qui EST sauve** : tresorerie et contribution des familles (via PersistenceManager)
**Ce qui est PERDU** : quelles familles sont prises → un joueur peut rejoindre une famille deja pleine

---

## 4. Historique de duel PERDU
**Fichier** : `config/Player/PlayerLevelData.java` ligne 67
```java
private transient List<String> duelHistory = new ArrayList<>();
```
Le champ est `transient` → jamais sauve dans le CODEC → historique des 10 derniers duels perdu a chaque restart.

---

## 5. Quetes actives PERDUES
**Fichier** : `quest/QuestManager.java` ligne 11
- `playerQuests` map — stocke les quetes en cours → NON persiste
- Un joueur avec une quete a 90% de completion la perd au restart
- Seuls les **cooldowns** sont sauves (via serialisation string dans PlayerLevelData)

---

## Correction proposee

### Pour NobilityManager et ChurchManager
```java
// Dans PersistenceManager.saveAll()
// Sauvegarder les hierarchies
props.setProperty("nobility.king.uuid", NobilityManager.getKingUUID().toString());
props.setProperty("nobility.king.name", NobilityManager.getKingName());
// Sauvegarder les compteurs
for (NobilityRank rank : NobilityRank.values()) {
    props.setProperty("nobility.count." + rank.name(), 
        String.valueOf(NobilityManager.getCount(rank)));
}
// Sauvegarder les relations seigneur-chevalier
for (Map.Entry<UUID, UUID> e : NobilityManager.getLordOf().entrySet()) {
    props.setProperty("nobility.lord." + e.getKey(), e.getValue().toString());
}
```

### Pour FamilyManager
```java
// Sauvegarder les familles prises
String taken = String.join(",", FamilyManager.getTakenFamilies());
props.setProperty("families.taken", taken);
```

### Pour les quetes actives
Serialiser les PlayerQuest dans le CODEC de PlayerLevelData, comme c'est fait pour les cooldowns.

### Pour l'historique de duel
Retirer le `transient` et ajouter au CODEC :
```java
.append(new KeyedCodec<>("DuelHistory", Codec.STRING), 
    (data, v) -> data.deserializeDuelHistory(v), 
    data -> data.serializeDuelHistory())
```

---

## Priorite
**CRITIQUE** — Perte de donnees a chaque restart serveur

## Liens
- [[Systems/Noblesse]] - Hierarchie feodale
- [[Systems/Eglise]] - Hierarchie religieuse
- [[Systems/Familles]] - Familles nobles
- [[Systems/Quetes]] - Progression des quetes
- [[Architecture/Persistence et Backup]] - Systeme de persistence general
