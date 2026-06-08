# 🚀 MINI TRELLO - FEUILLE DE TRICHE SOUTENANCE

> **Document à consulter rapidement pendant la soutenance**

---

## 📋 ARCHITECTURE EN 30 SECONDES

```
Frontend (React)                Backend (Spring Boot)              Base de données (PostgreSQL)
    ↓                               ↓                                  ↓
React:5173  ────JWT───→  Controllers ────→ Services ────→ Repositories ────→ PostgreSQL:5432
    ↓                         (REST API)        (Model)      (JPA)
    └─ 📁️ /uploads/taches/  ← Images stockées physiquement
```

**Stack technique** :
- ✅ **Java 17** + **Spring Boot 3.2.5**
- ✅ **PostgreSQL** (Base de données)
- ✅ **JWT** (Authentification sans session)
- ✅ **JPA/Hibernate** (ORM)
- ✅ **Maven** (Gestion dépendances)

---

## 🔐 AUTHENTIFICATION JWT EN 3 ÉTAPES

### 1️⃣ LOGIN
```
Frontend envoie : {"email": "user@ex.com", "motDePasse": "123"}
       ↓ POST /api/auth/login
Backend retourne : {
    "token": "eyJhbGc...",    ← JWT signé (24h expiration)
    "id": 1,
    "email": "user@ex.com",
    "role": "MEMBRE"          ← CHEF_PROJET ou MEMBRE
}
Frontend stocke le token dans localStorage
```

### 2️⃣ CHAQUE REQUÊTE SUIVANTE
```
Frontend ajoute en header : Authorization: Bearer eyJhbGc...
       ↓ JwtAuthenticationFilter vérifie la signature
Backend charge l'utilisateur et son rôle
       ↓
Accès autorisé ✅ ou refusé ❌
```

### 3️⃣ RÔLES
```
┌──────────────────────────────────────┐
│ CHEF_PROJET                          │
├──────────────────────────────────────┤
│ • Voit TOUS les projets              │
│ • /api/projets/tous → ALL projets    │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│ MEMBRE (défaut)                      │
├──────────────────────────────────────┤
│ • Voit uniquement SES projets        │
│ • /api/projets → ses projets         │
└──────────────────────────────────────┘
```

---

## 📡 API REST PAR RESSOURCE

### 🔓 AUTHENTIFICATION (Publique)

| Action | Méthode | Route | Token? |
|--------|---------|-------|--------|
| Login | `POST` | `/api/auth/login` | ❌ Non |
| Register | `POST` | `/api/auth/register` | ❌ Non |

**Exemple Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@ex.com", "motDePasse":"pass"}'
```

---

### 👤 UTILISATEURS (Protégé)

| Action | Méthode | Route | Token? |
|--------|---------|-------|--------|
| Lister tous | `GET` | `/api/utilisateurs` | ✅ Oui |
| Récupérer 1 | `GET` | `/api/utilisateurs/{id}` | ✅ Oui |
| Profil connecté | `GET` | `/api/utilisateurs/moi` | ✅ Oui |
| Modifier | `PUT` | `/api/utilisateurs/{id}` | ✅ Oui |
| Changer rôle | `PATCH` | `/api/utilisateurs/{id}/role?role=CHEF_PROJET` | ✅ Oui |
| Supprimer | `DELETE` | `/api/utilisateurs/{id}` | ✅ Oui |

**Exemple changement de rôle:**
```bash
curl -X PATCH "http://localhost:8080/api/utilisateurs/2/role?role=CHEF_PROJET" \
  -H "Authorization: Bearer $JWT"
```

---

### 📊 PROJETS (Protégé)

| Action | Méthode | Route | Logique |
|--------|---------|-------|--------|
| Mes projets | `GET` | `/api/projets` | Utilisateur = propriétaire ou membre |
| Voir détail | `GET` | `/api/projets/{id}` | Vérifier accès |
| Tous (CHEF) | `GET` | `/api/projets/tous` | CHEF → tous / MEMBRE → ses projets |
| Créer | `POST` | `/api/projets` | Utilisateur = propriétaire auto |
| Modifier | `PUT` | `/api/projets/{id}` | Vérifier propriétaire |
| Supprimer | `DELETE` | `/api/projets/{id}` | Vérifier propriétaire |
| Ajouter membre | `POST` | `/api/projets/{id}/membres/{membreId}` | Vérifier propriétaire |
| Retirer membre | `DELETE` | `/api/projets/{id}/membres/{membreId}` | Vérifier propriétaire |

**Exemple créer projet:**
```bash
curl -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Mon Projet",
    "description": "Description",
    "dateDebut": "2024-01-01",
    "dateFin": "2024-12-31"
  }'
# L'utilisateur connecté devient propriétaire automatiquement
```

---

### 📝 COLONNES (Protégé)

| Action | Méthode | Route | Spécial |
|--------|---------|-------|---------|
| Lister | `GET` | `/api/colonnes/projet/{projetId}` | Triées par position |
| Créer | `POST` | `/api/colonnes/{projetId}` | Position auto = nb colonnes |
| Modifier | `PUT` | `/api/colonnes/{id}` | Nom, couleur |
| Supprimer | `DELETE` | `/api/colonnes/{id}` | Cascade → tâches supprimées |
| Réorganiser | `POST` | `/api/colonnes/{projetId}/reorganiser` | **Drag & Drop** |

**Exemple réorganisation (Drag & Drop):**
```bash
# Envoyer IDs dans le nouvel ordre
curl -X POST http://localhost:8080/api/colonnes/5/reorganiser \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '[10, 11, 12]'  # Colonnes 10, 11, 12 dans ce nouvel ordre
```

---

### ✅ TÂCHES (Protégé)

| Action | Méthode | Route | Spécial |
|--------|---------|-------|---------|
| Lister dans colonne | `GET` | `/api/taches/colonne/{colonneId}` | Triées par position |
| Récupérer détail | `GET` | `/api/taches/{id}` | Avec relations |
| Mes tâches assignées | `GET` | `/api/taches/mes-taches` | Utilisateur connecté |
| Créer tâche | `POST` | `/api/taches/{colonneId}` | Position auto = nb tâches |
| Modifier | `PUT` | `/api/taches/{id}` | Titre, description, priorité, statut, date |
| Supprimer | `DELETE` | `/api/taches/{id}` | Cascade → images supprimées |
| **Déplacer** | `PATCH` | `/api/taches/{id}/deplacer?colonneId=8` | **Drag & Drop horizontalement** |
| **Réorganiser** | `POST` | `/api/taches/{colonneId}/reorganiser` | **Drag & Drop verticalement** |
| Changer statut | `PATCH` | `/api/taches/{id}/statut?statut=EN_COURS` | A_FAIRE → EN_COURS → TERMINEE |
| Assigner user | `POST` | `/api/taches/{id}/assignes/{assigneId}` | Peut avoir plusieurs assignés |
| Retirer assigné | `DELETE` | `/api/taches/{id}/assignes/{assigneId}` | - |
| **Upload image** | `POST` | `/api/taches/{id}/upload-image` | Multipart, génère UUID |
| Upload base64 | `POST` | `/api/taches/{id}/upload-image-base64` | Pour Swagger UI |
| Supprimer image | `DELETE` | `/api/taches/{id}/images/{index}` | Supprime aussi fichier disque |
| Lister images | `GET` | `/api/taches/{id}/images` | URLs stockées en BD |

**Exemple drag & drop tâche (changement colonne):**
```bash
# Déplacer tâche 15 de sa colonne actuelle vers colonne 8
curl -X PATCH "http://localhost:8080/api/taches/15/deplacer?colonneId=8" \
  -H "Authorization: Bearer $JWT"
```

**Exemple réorganisation tâches dans colonne:**
```bash
# Réorganiser tâches dans colonne 3 dans cet ordre: [20, 18, 22]
curl -X POST http://localhost:8080/api/taches/3/reorganiser \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '[20, 18, 22]'
```

**Exemple upload image:**
```bash
curl -X POST http://localhost:8080/api/taches/5/upload-image \
  -H "Authorization: Bearer $JWT" \
  -F "image=@/path/to/image.jpg"
# Retour: tâche avec URL image = /uploads/taches/52ab442e-6c77-4698...jpg
```

**Exemple changer statut:**
```bash
curl -X PATCH "http://localhost:8080/api/taches/15/statut?statut=EN_COURS" \
  -H "Authorization: Bearer $JWT"
# Statuts: A_FAIRE, EN_COURS, TERMINEE
```

---

## 💾 BASE DE DONNÉES - RELATIONS

```
UTILISATEURS (id, nom, prenom, email, mot_de_passe, role, avatar, date_creation)
    ↑
    │ 1 Propriétaire
    │
PROJETS (id, titre, description, date_debut, date_fin, proprietaire_id, date_creation)
    ↑
    │ N Colonnes
    │
COLONNES (id, nom, couleur, position, projet_id)
    ↑
    │ N Tâches
    │
TACHES (id, titre, description, priorite, statut, date_echeance, position, colonne_id, date_creation)
    ├─ ↕ Many-to-Many ← UTILISATEURS (assignés à une tâche)
    ├─ ↕ N COMMENTAIRES
    └─ ↕ N TACHE_IMAGES (URLs de fichiers)

MEMBRES_PROJET (projet_id, utilisateur_id)  ← Table de jonction Many-to-Many
```

---

## 🛡️ GESTION DES ERREURS

| Code | Signification | Cause |
|------|---------------|-------|
| `200` ✅ | OK | Requête réussie |
| `201` ✅ | Created | Ressource créée |
| `204` ✅ | No Content | Suppression réussie |
| `400` ❌ | Bad Request | Données invalides, email format incorrect |
| `401` ❌ | Unauthorized | JWT manquant, invalide ou expiré |
| `403` ❌ | Forbidden | Pas d'accès (ex: modifier projet d'un autre) |
| `404` ❌ | Not Found | Ressource inexistante |
| `500` ❌ | Server Error | Erreur serveur |

**Exemple erreur 403 :**
```json
{
  "message": "Vous n'êtes pas membre de ce projet",
  "status": 403
}
```

---

## 🎯 5 POINTS CLÉS À PRÉSENTER

### 1️⃣ JWT Token
> "L'authentification JWT permet une connexion sans session. Le token est généré au login, signé avec une clé secrète, et validé à chaque requête."

### 2️⃣ Rôles (CHEF_PROJET vs MEMBRE)
> "Les rôles contrôlent l'accès. Un CHEF_PROJET voit tous les projets, un MEMBRE voit uniquement ses projets."

### 3️⃣ Relations Many-to-Many
> "Un projet peut avoir plusieurs membres, un utilisateur peut être dans plusieurs projets. C'est une relation M:M gérée avec une table de jonction."

### 4️⃣ Drag & Drop via API
> "Le drag & drop est implémenté par deux endpoints simples : 
> - Déplacement horizontal = changer la colonne
> - Déplacement vertical = réorganiser les positions"

### 5️⃣ Upload d'images sécurisé
> "Les images sont sauvegardées sur disque avec un UUID unique (pas le nom de fichier original) pour éviter les collisions et les attaques."

---

## 🚀 COMMANDES UTILES

### Démarrer le serveur
```bash
# Développement
mvn spring-boot:run -DskipTests

# Production
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Entrer dans le shell PostgreSQL
```bash
psql -U postgres -d mini_trello
SELECT * FROM utilisateurs;
SELECT * FROM projets;
```

### Tester une API
```bash
# Swagger UI
http://localhost:8080/swagger-ui.html

# Postman / cURL
curl -X GET http://localhost:8080/api/utilisateurs \
  -H "Authorization: Bearer $JWT"
```

---

## 📊 STATUTS ÉNUMS

**Statut de tâche** :
```
A_FAIRE   🔵 (Nouveau)
EN_COURS  🟡 (En traitement)
TERMINEE  🟢 (Fini)
```

**Priorité de tâche** :
```
BASSE
MOYENNE
HAUTE
URGENTE
```

**Rôle utilisateur** :
```
CHEF_PROJET  (Accès admin)
MEMBRE       (Accès limité)
```

---

## 🔍 SCHÉMA SIMPLIFIÉ

```sql
-- USER
SELECT * FROM utilisateurs;
-- Colonnes: id, nom, prenom, email, mot_de_passe (BCrypt), role, date_creation

-- PROJECT
SELECT * FROM projets;
-- Colonnes: id, titre, description, date_debut, date_fin, proprietaire_id, date_creation

-- MEMBERS
SELECT * FROM membres_projet;
-- Colonnes: projet_id, utilisateur_id (Many-to-Many)

-- COLUMNS
SELECT * FROM colonnes;
-- Colonnes: id, nom, couleur, position, projet_id

-- TASKS
SELECT * FROM taches;
-- Colonnes: id, titre, description, priorite, statut, date_echeance, position, colonne_id, date_creation

-- TASK ASSIGNMENTS
SELECT * FROM tache_assignees;
-- Colonnes: tache_id, utilisateur_id (Many-to-Many)

-- TASK IMAGES
SELECT * FROM tache_images;
-- Colonnes: tache_id, image_url

-- COMMENTS
SELECT * FROM commentaires;
-- Colonnes: id, contenu, date_creation, date_modification, tache_id, auteur_id
```

---

## 💡 QUESTIONS ANTICIPÉES & RÉPONSES

### Q: Pourquoi JWT et pas des sessions?
> **A:** JWT est stateless (pas besoin de session côté serveur), plus scalable, et idéal pour une API REST utilisée par un frontend SPA.

### Q: Comment gérez-vous la sécurité du mot de passe?
> **A:** Le mot de passe est hashé en BCrypt (irréversible) avant stockage. À chaque login, BCrypt compare le mot de passe saisi avec le hash stocké.

### Q: Comment fonctionne le drag & drop?
> **A:** Simple : on envoie les IDs dans le nouvel ordre, le serveur met à jour la position de chaque élément en base de données.

### Q: Où sont stockées les images?
> **A:** Physiquement dans `./uploads/taches/` sur le serveur. L'URL est stockée en base de données. Les noms sont des UUIDs pour éviter les collisions.

### Q: Comment testez-vous les APIs?
> **A:** Via Swagger UI (`http://localhost:8080/swagger-ui.html`), Postman, ou cURL.

---

**Bonne soutenance! 🎓🚀**


