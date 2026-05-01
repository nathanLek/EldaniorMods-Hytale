# Plugin Hytale - Eldanior System

> Plugin RPG complet pour Hytale Server. Systeme de classes, noblesse, territoires, economie, quetes et plus.
> **Version** : 0.0.2 | **405 classes** | **301 skills** | **214 effets visuels** | **34 consommables**

---

## Systemes de jeu
- [[Systems/Leveling]] - Progression, XP, niveaux, formules
- [[Systems/Combat]] - Calcul des degats, esquive, critique, PvP zone
- [[Systems/Classes]] - Classes et evolutions (Tier 1 + Tier 2, gacha)
- [[Systems/Skills]] - 301 competences passives avec effets visuels
- [[Systems/Noblesse]] - Hierarchie feodale (Roi > Marquis > Duc > Comte > Baron > Chevalier)
- [[Systems/Eglise]] - Hierarchie religieuse (Pape > Cardinal > Archeveque > Pretre)
- [[Systems/Titres]] - Titres et achievements avec bonus
- [[Systems/Territoires]] - Royaumes, territoires, villes, parcelles, logements
- [[Systems/Economie]] - Taxes 12%, impots, tresorerie, transactions
- [[Systems/Quetes]] - Quetes principales, secondaires, journalieres
- [[Systems/NPC et Dialogues]] - Interaction NPC et systeme de dialogue
- [[Systems/Donjons]] - Instances, portails, boss (4 tiers)
- [[Systems/Echanges]] - Systeme de troc entre joueurs (Marchands)
- [[Systems/Guildes]] - Guildes avec tresorerie et hierarchie
- [[Systems/Familles]] - Familles nobles avec blasons et passifs
- [[Systems/Duels]] - Systeme de duel PvP
- [[Systems/Groupe]] - Systeme de groupe (party)
- [[Systems/Inventaire]] - Coffre personnel (45 slots)
- [[Systems/Consommables]] - Items consommables (pillules, elixirs, decrets)
- [[Systems/Shop]] - Boutique et Marche Noir
- [[Systems/Classements]] - Leaderboards (Mobs, PvP, Duels, Guildes)
- [[Systems/Coffres au tresor]] - Coffres avec loot aleatoire

## Configuration
- [[Config/StatConfig]] - 21 stats du joueur (formules, caps)
- [[Config/Raretes]] - 6 niveaux de rarete (couleurs, poids)
- [[Config/Armes]] - 13 types d'armes et maitrises
- [[Config/LootTables]] - 5 tables de loot (probabilites)
- [[Config/Monnaie]] - 5 types de pieces (1 - 10 000 Or)
- [[Config/Dignite et Aura]] - Aura noble (ralentit les mobs)
- [[Config/Localisation]] - Fichier de langue (300+ traductions)

## Mobs
- [[Mobs/Configuration Mobs]] - 22 familles de mobs, XP, niveaux, boss

## Architecture technique
- [[Architecture/ECS Systems]] - 28 systemes ECS (tick + event)
- [[Architecture/GUI SystemScreen]] - Interface unifiee 16 onglets
- [[Architecture/Persistence]] - Sauvegarde (EntityStore + Properties)
- [[Architecture/Interactions]] - 5 types d'interactions items

## Utilitaires
- [[Utilitaires/Notifications]] - Notifications, titres, TinyMsg
- [[Utilitaires/Effets Visuels]] - 100+ effets Hytale + 214 mappings skills
- [[Utilitaires/CombinedHud]] - HUD combine quete + groupe

## Processus de creation
- [[Processus/Creer un skill]] - 7 etapes pour ajouter une competence
- [[Processus/Creer une classe]] - Structure et enregistrement
- [[Processus/Creer un consommable]] - JSON + registre + loot
- [[Processus/Creer une parcelle]] - Selection Tool ou commandes

## References
- [[Commandes/Liste des commandes]] - Toutes les commandes /es
- [[Items/Consommables]] - Liste des 34 items consommables
- [[Classes/Arbre des classes]] - Arbre d'evolution complet (405 classes)
- [[Classes/Classes Tier 1]] - Detail des 5 classes de base
- [[Classes/Familles/Familles Royales]] - 9 familles nobles et passifs

---

## Evolutions & Optimisations
> [[Eldanior - Evolutions]] - Bugs, optimisations, features manquantes, roadmap

---
#eldanior #hytale #rpg #documentation #plugin