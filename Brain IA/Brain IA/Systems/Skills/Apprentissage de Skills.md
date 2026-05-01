# Apprentissage de Skills

#skills #parchemin #apprentissage #evolution

## 3 Modes d'acquisition

### 1. Inne (classe)
- Chaque classe tier 1 a 2-3 skills passifs automatiques
- Exemple : Warrior → INSTINCTIVE_STRIKE + IRON_RESOLVE
- Active des le choix de classe

### 2. Parchemin (item consommable)
- Item `Parchemin_[Skill]` trouve dans les coffres
- Consomme via `ConsumableItemSkillInteraction`
- Le skill est ajoute aux `unlockedSkills` du joueur
- Un parchemin superieur **remplace** l'inferieur de meme categorie

### 3. Evolution de classe (tier 2)
- Certaines classes tier 2 ajoutent des skills supplementaires
- Actives automatiquement au choix de l'evolution

## Flow d'apprentissage (parchemin)
1. Le joueur a un parchemin dans son hotbar
2. Il utilise l'item (clic droit)
3. `ConsumableItemSkillInteraction` verifie :
   - Le joueur ne possede pas deja le skill
   - Le joueur a la bonne classe (si skill lie a une classe)
4. Si OK → skill ajoute, parchemin consomme
5. Notification : "§a Competence apprise : [nom du skill]"

## Gestion des skills actifs
- Un joueur peut avoir plusieurs skills debloques
- Il peut activer/desactiver chaque skill via le GUI Competences
- Les skills actifs sont dans `enabledSkills`
- Les skills debloques sont dans `unlockedSkills`

## Fichiers cles
- `skills/interaction/ConsumableItemSkillInteraction.java` — consommation
- `config/Player/PlayerLevelData.java` — `unlockedSkills`, `enabledSkills`

## Liens
- [[Categories de Skills]] - Les 11 categories
- [[Rarites et Distribution]] - Ou trouver les parchemins
- [[../Skills]] - Vue d'ensemble