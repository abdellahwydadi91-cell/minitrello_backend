# 🎓 MINI TRELLO - PRÉSENTATION 10 MINUTES

## 📌 CONTEXTE
- **Projet** : MiniTrello (Gestion de tâches style Trello)
- **Backend** : Spring Boot 3.2.5 + Java 17
- **BD** : PostgreSQL
- **Frontend** : React (localhost:5173)
- **API** : REST + JWT
- **Port** : 8080

---

## 🏗️ ARCHITECTURE (Diapo 1)

```
React Frontend              Spring Boot Backend          PostgreSQL
    ↓                            ↓                           ↓
   5173           REST + JWT     8080                       5432
    │            ←────────────→   │
    │ Login      Controllers      │ Data
    └─ Token     Services         │ Entities
      stored ────→ Repositories   │ Tables
```

**Technologies clés** :
- ✅ Spring Security + JWT Token
- ✅ JPA/Hibernate (ORM)
- ✅ Maven
- ✅ Swagger/OpenAPI

---

## 🔐 AUTHENTIFICATION JWT (Diapo 2)

### Flux de sécurité

```mermaid
1. Register
   ├─ Email unique ?
   ├─ Mot de passe hashé (BCrypt)
   └─ Rôle par défaut = MEMBRE

2. Login
   ├─ Email + mot de passe
   ├─ Génère JWT (clé secrète HMAC-SHA256)
   ├─ Expiration = 24 heures
   └─ Retourne : token + user data

3. Requêtes suivantes
   ├─ Header: Authorization: Bearer <JWT>
   ├─ JwtAuthenticationFilter valide
   ├─ Charge autorités Spring (ROLE_CHEF_PROJET, ROLE_MEMBRE)
   └─ Requête exécutée si autorisée
```

**Avantage JWT** : Stateless = scalable, pas de session serveur

---

## 👥 RÔLES & ACCÈS (Diapo 3)

| Rôle | Accès |
|------|-------|
| **CHEF_PROJET** | Voir TOUS les projets, gérer utilisateurs |
| **MEMBRE** | Voir uniquement ses projets, créer tâches |

**Vérification** : À chaque requête, le backend vérifie le rôle via le JWT

---

## 📊 MODÈLE DE DONNÉES (Diapo 4)

```
UTILISATEURS (1)
     ↓ propriétaire
     |
PROJETS (N colonnes, N membres)
     ├─ COLONNES (N tâches, ordonnées par position)
     │   └─ TÂCHES (N assignés, N images, N commentaires)
     │
     └─ MEMBRES_PROJET ← Table M:M (Many-to-Many)

Énums :
─ Statuts tâche : A_FAIRE → EN_COURS → TERMINEE
─ Priorités : BASSE, MOYENNE, HAUTE, URGENTE
─ Rôles : CHEF_PROJET, MEMBRE
```

---

## 🔌 APIs REST (Diapo 5)

### Endpoints principaux

```
🔓 Publics (sans token)
  POST   /api/auth/login           Connexion
  POST   /api/auth/register        Inscription

🔒 Protégés (avec token JWT)
  GET    /api/projets              Mes projets
  POST   /api/projets              Créer projet
  
  GET    /api/colonnes/projet/{id} Colonnes du projet
  POST   /api/colonnes/{id}/reorganiser  [Drag & Drop]
  
  GET    /api/taches/colonne/{id}  Tâches de la colonne
  PATCH  /api/taches/{id}/deplacer [Drag & Drop - horizontal]
  POST   /api/taches/{id}/reorganiser    [Drag & Drop - vertical]
  PATCH  /api/taches/{id}/statut         Changer statut
  POST   /api/taches/{id}/upload-image   Uploader image
  
  GET    /api/utilisateurs         Lister users
  PATCH  /api/utilisateurs/{id}/role    Changer rôle
```

**Total** : ~40 routes

---

## ✅ FONCTIONNALITÉS PRINCIPALES (Diapo 6)

### 1️⃣ Gestion de projets
- Créer, modifier, supprimer
- Ajouter/retirer membres (Many-to-Many)
- Propriétaire automatique = créateur

### 2️⃣ Organisation en colonnes
- Colonnes ordonnées par position
- Drag & Drop réorganisation = mise à jour positions

### 3️⃣ Tâches
- Créer, modifier, supprimer
- Drag & Drop horizontal (colonne → colonne)
- Drag & Drop vertical (position dans colonne)
- Assigner à plusieurs utilisateurs
- Upload images (UUID sécurisé)
- Statut A_FAIRE → EN_COURS → TERMINEE

### 4️⃣ Authentification & Sécurité
- JWT Token 24h
- Rôles (CHEF_PROJET / MEMBRE)
- Mot de passe BCrypt
- Vérification accès projet

---

## 🛡️ GESTION DES ERREURS (Diapo 7)

| Code | Sens |
|------|------|
| `200` ✅ | OK |
| `401` ❌ | Token invalide/expiré |
| `403` ❌ | Pas d'accès (autre propriétaire) |
| `404` ❌ | Ressource inexistante |
| `500` ❌ | Erreur serveur |

**Retour** : JSON avec message d'erreur explicite

---

## 📸 HIGHLIGHT TECHNIQUE : UPLOAD D'IMAGES (Diapo 8)

**Processus sécurisé** :

```
1. Frontend upload → POST /api/taches/{id}/upload-image
2. Backend valide (type image/)
3. Génère UUID unique (52ab442e-6c77-4698-9648-66c3100da22a.png)
4. Sauvegarde physiquement dans ./uploads/taches/
5. Stocke URL en base (/uploads/taches/52ab442e...)
6. Frontend accède via HTTP (accessible public)
```

**Sécurité** :
- ✅ UUID empêche les collisions
- ✅ Validation type MIME
- ✅ Taille limite 10MB
- ✅ Suppression fichier lors delete tâche

---

## 🎯 DRAG & DROP IMPLÉMENTATION (Diapo 9)

### Horizontal (colonne à colonne)
```
PATCH /api/taches/15/deplacer?colonneId=8
└─ Met à jour : tache.colonne = colonne_8
```

### Vertical (réorganisation dans colonne)
```
POST /api/taches/8/reorganiser
Body: [20, 18, 22]
└─ Tâche 20 → position 0
└─ Tâche 18 → position 1
└─ Tâche 22 → position 2
```

**Données** : Seulement les IDs et positions → Léger et rapide

---

## 📊 RELATIONS BASE DE DONNÉES (Diapo 10)

### Many-to-Many : UTILISATEURS ↔ PROJETS
```sql
Table membres_projet (projet_id, utilisateur_id)
```

### Many-to-Many : UTILISATEURS ↔ TÂCHES
```sql
Table tache_assignees (tache_id, utilisateur_id)
```

### One-to-Many : PROJETS → COLONNES → TÂCHES
```
Cascade : Supprimer projet → Supprime colonnes/tâches
```

### ElementCollection : Tâches → Images
```sql
Table tache_images (tache_id, image_url)
```

---

## 🚀 MISE EN PRODUCTION (Diapo 11)

### Déploiement
```bash
# Build
mvn clean package -DskipTests

# Exécuter JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Configuration production
- JWT secret → Strong key (50+ chars)
- DB URL → Production PostgreSQL
- CORS origins → Frontend production URL
- Upload dir → Safe path avec permissions

---

## ❓ QUESTIONS PROBABLES (Diapo 12)

### Q1 : Comment gérez-vous les collisions UUID ?
**R**: UUID v4 garantit unicité quasi infinie (36^32 combinaisons)

### Q2 : Pourquoi Many-to-Many pour les membres ?
**R**: Un utilisateur peut être dans plusieurs projets

### Q3 : Pourquoi pas de session ?
**R**: JWT stateless = mieux pour microservices et API mobiles

### Q4 : Comment tester les APIs ?
**R**: Via Swagger UI (localhost:8080/swagger-ui.html) ou Postman

### Q5 : Que se passe si un utilisateur modifie le JWT ?
**R**: La signature HMAC devient invalide → JWT rejeté

---

## 📈 STATISTIQUES PROJET

| Métrique | Nombre |
|----------|--------|
| **Contrôleurs** | 5 |
| **Services** | 4 |
| **Entités JPA** | 6 |
| **Endpoints API** | ~40 |
| **Rôles** | 2 |
| **Statuts tâche** | 3 |
| **Priorités** | 4 |
| **Relations** | 5 (1:N, N:M, 1:1) |

---

## ✨ POINTS FORTS EXPLIQUES

1. **JWT Token** → Authentification sans session
2. **Rôles granulaires** → Contrôle d'accès dynamique
3. **Drag & Drop** → Implémentation simple via positions
4. **Relations M:M** → Flexibilité données
5. **Upload sécurisé** → UUID + validation
6. **Cascade delete** → Intégrité référentielle
7. **DTOs** → Séparation entités/API
8. **Transactions** → Cohérence données

---

## 🎬 DÉMO LIVE (5 minutes)

**Sequence propose** :
```
1. Swagger UI → explorer endpoints
2. Login → obtenir JWT
3. Créer projet → afficher dans base
4. Drag & Drop → réorganiser
5. Upload image → montrer fichier
6. Changer rôle → voir différence accès
```

---

## 📞 CONTACT & RESSOURCES

**Frontend** : http://localhost:5173
**Backend** : http://localhost:8080
**API Docs** : http://localhost:8080/swagger-ui.html
**Database** : localhost:5432 (PostgreSQL)

**Lancer le serveur** :
```bash
mvn spring-boot:run -DskipTests
```

---

**Prêt pour la soutenance ! 🎓**


