# Cahier des Charges - API MiniTrello

## Vue d'ensemble
MiniTrello est une application de gestion de projets collaborative avec un système de rôles basé sur Spring Boot et JWT.

## Architecture
- **Backend**: Spring Boot 3.x + Spring Security + JWT
- **Base de données**: MySQL/PostgreSQL
- **Authentification**: JWT (JSON Web Tokens)
- **Documentation API**: Swagger UI disponible sur `/swagger-ui.html`

---

## 🔐 Système d'Authentification

### Rôles Disponibles
- **CHEF_PROJET** 📊 : Chef de projet - Accès étendu à la gestion des projets
- **MEMBRE** 👤 : Membre - Accès standard aux fonctionnalités

### Endpoints d'Authentification
| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| `POST` | `/api/auth/login` | Connexion utilisateur | Public |
| `POST` | `/api/auth/register` | Inscription nouvel utilisateur | Public |

---

## 👥 Gestion des Utilisateurs

### Endpoints Utilisateur
| Méthode | Endpoint | Description | Rôles Requis |
|---------|----------|-------------|--------------|
| `GET` | `/api/utilisateurs` | Liste tous les utilisateurs | Authentifié |
| `GET` | `/api/utilisateurs/{id}` | Détails d'un utilisateur | Authentifié |
| `GET` | `/api/utilisateurs/moi` | Profil utilisateur connecté | Authentifié |
| `PUT` | `/api/utilisateurs/{id}` | Modifier un utilisateur | Propriétaire uniquement |
| `DELETE` | `/api/utilisateurs/{id}` | Supprimer un utilisateur | Propriétaire uniquement |
| `PATCH` | `/api/utilisateurs/{id}/role` | Changer le rôle | Non restreint (logique métier) |
| `PATCH` | `/api/utilisateurs/{id}/mot-de-passe` | Changer mot de passe | Propriétaire uniquement |

---

## 📁 Gestion des Projets

### Endpoints Projet
| Méthode | Endpoint | Description | Rôles Requis |
|---------|----------|-------------|--------------|
| `GET` | `/api/projets` | Mes projets | Authentifié |
| `GET` | `/api/projets/{id}` | Détails d'un projet | Membre du projet |
| `POST` | `/api/projets` | Créer un projet | Authentifié |
| `PUT` | `/api/projets/{id}` | Modifier un projet | Propriétaire ou CHEF_PROJET |
| `DELETE` | `/api/projets/{id}` | Supprimer un projet | Propriétaire uniquement |
| `GET` | `/api/projets/tous` | Tous mes projets | Authentifié |
| `GET` | `/api/projets/all` | Tous les projets | Authentifié |
| `POST` | `/api/projets/{id}/membres/{membreId}` | Ajouter un membre | Propriétaire ou CHEF_PROJET |
| `DELETE` | `/api/projets/{id}/membres/{membreId}` | Retirer un membre | Propriétaire ou CHEF_PROJET |

---

## 📋 Gestion des Colonnes

### Endpoints Colonne
| Méthode | Endpoint | Description | Rôles Requis |
|---------|----------|-------------|--------------|
| `GET` | `/api/colonnes/projet/{projetId}` | Colonnes d'un projet | Membre du projet |
| `GET` | `/api/colonnes/{id}` | Détails d'une colonne | Membre du projet |
| `POST` | `/api/colonnes/{projetId}` | Créer une colonne | Membre du projet |
| `PUT` | `/api/colonnes/{id}` | Modifier une colonne | Membre du projet |
| `DELETE` | `/api/colonnes/{id}` | Supprimer une colonne | Membre du projet |
| `POST` | `/api/colonnes/{projetId}/reorganiser` | Réorganiser les colonnes | Membre du projet |

---

## ✅ Gestion des Tâches

### Endpoints Tâche
| Méthode | Endpoint | Description | Rôles Requis |
|---------|----------|-------------|--------------|
| `GET` | `/api/taches/colonne/{colonneId}` | Tâches d'une colonne | Membre du projet |
| `GET` | `/api/taches/{id}` | Détails d'une tâche | Membre du projet |
| `GET` | `/api/taches/mes-taches` | Mes tâches assignées | Authentifié |
| `GET` | `/api/taches/assignees/{utilisateurId}` | Tâches d'un utilisateur | Authentifié |
| `POST` | `/api/taches/{colonneId}` | Créer une tâche | Membre du projet |
| `PUT` | `/api/taches/{id}` | Modifier une tâche | Membre du projet |
| `DELETE` | `/api/taches/{id}` | Supprimer une tâche | Membre du projet |
| `PATCH` | `/api/taches/{id}/deplacer` | Déplacer une tâche | Membre du projet |
| `POST` | `/api/taches/{colonneId}/reorganiser` | Réorganiser les tâches | Membre du projet |
| `POST` | `/api/taches/{id}/assignes/{assigneId}` | Assigner une tâche | Membre du projet |

---

## 🔒 Politiques de Sécurité

### Authentification
- **JWT Token** requis pour toutes les APIs sauf `/api/auth/**`
- Token inclus dans le header `Authorization: Bearer <token>`
- Expiration du token : 24 heures (configurable)

### Autorisation
- **Vérification automatique** de l'appartenance aux projets
- **Logique métier** dans les services pour valider les permissions
- **Pas d'annotations @PreAuthorize** explicites (logique centralisée)

### CORS
- **Origine autorisée** : `http://localhost:5173` (frontend)
- **Méthodes** : GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Headers** : Tous autorisés
- **Credentials** : Activés

---

## 📊 Règles Métier par Rôle

### CHEF_PROJET
- ✅ Peut créer des projets
- ✅ Peut modifier tous les projets
- ✅ Peut ajouter/retirer des membres aux projets
- ✅ Accès complet à toutes les fonctionnalités

### MEMBRE
- ✅ Peut créer des projets (devient propriétaire)
- ✅ Peut modifier ses propres projets uniquement
- ✅ Peut gérer les membres de ses projets uniquement
- ✅ Accès aux projets dont il est membre

---

## 🚀 Points d'Accès Publics

### Documentation
- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **API Docs** : `http://localhost:8080/v3/api-docs`

### Santé de l'application
- **Actuator** (si configuré) : `/actuator/health`

---

## 📝 Formats de Données

### Authentification
```json
// Login Request
{
  "email": "user@example.com",
  "motDePasse": "password123"
}

// Login Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id": 1,
  "email": "user@example.com",
  "role": "MEMBRE"
}
```

### Codes d'Erreur
- `400` : Données invalides
- `401` : Non authentifié
- `403` : Accès refusé
- `404` : Ressource non trouvée
- `500` : Erreur serveur

---

## 🛠️ Configuration de Développement

### Lancement
```bash
mvn spring-boot:run -DskipTests
```

### Base de données
- **URL** : Configuré dans `application.properties`
- **Port** : 8080 (par défaut)
- **Profil** : `dev` pour développement

---

## 🔄 Évolutions Futures

### Rôles Additionnels
- Possibilité d'ajouter de nouveaux rôles (DEVELOPPEUR, TESTEUR, etc.)

### Permissions Granulaires
- Système de permissions plus fin par fonctionnalité

### Audit Trail
- Traçabilité des actions utilisateurs

---

*Document généré le 2026-05-14 - Version 1.0*</content>
<parameter name="filePath">C:\Users\BAB AL SAFA\Downloads\backend_mydblink\minitrello\CAHIER_DES_CHARGES_API.md
