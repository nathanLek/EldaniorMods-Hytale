# Permissions et Roles des Parcelles

#territoire #permissions #roles #protection

## Roles
| Role | Description |
|------|------------|
| OWNER | Proprietaire, tous les droits |
| OFFICER | Officier, peut construire/casser |
| MEMBER | Membre, peut interagir |
| VISITOR | Visiteur, peut entrer |

## Permissions par role
| Role | BUILD | BREAK | INTERACT | PVP | ENTER |
|------|-------|-------|----------|-----|-------|
| OWNER | oui | oui | oui | oui | oui |
| OFFICER | oui | oui | oui | non | oui |
| MEMBER | non | non | oui | non | oui |
| VISITOR | non | non | non | non | oui |

## Non-membres par type de parcelle
| Type | INTERACT | ENTER | BUILD/BREAK |
|------|----------|-------|-------------|
| KINGDOM | oui | oui | non |
| TERRITORY | oui | oui | non |
| CITY | oui | oui | non |
| PLOT | selon protection | selon protection | non |
| HOUSING | selon protection | selon protection | non |
| ROOM | selon protection | selon protection | non |
| FARM | oui (tous) | oui | oui (BREAK) |

## Cas special : Location
Pendant une location active :
- Le **locataire** a les permissions **OWNER**
- Le **proprietaire** perd **TOUTES** ses permissions (meme ENTER)
- A l'expiration du bail → retour aux permissions normales

## Protection par defaut
- `protectedByDefault = true` → bloque les non-membres
- `protectedByDefault = false` → acces libre (sauf BUILD)

## Admin bypass
Les admins passent outre toutes les verifications de permissions.

## Fichiers cles
- `territory/ParcelData.java` — `hasPermission()` methode
- `territory/ParcelPermission.java` — enum des permissions
- `territory/ParcelRole.java` — enum des roles
- `territory/events/ParcelBreakBlockEvent.java` — protection blocs

## Liens
- [[Types et Hierarchie]] - Types de parcelles
- [[Achat et Location]] - Systeme de location
- [[../Territoires]] - Vue d'ensemble