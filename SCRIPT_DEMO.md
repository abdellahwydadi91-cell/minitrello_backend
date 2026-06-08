# 🎬 SCRIPT DE DÉMONSTRATION - MINI TRELLO

> **Commandes prêtes à copier-coller pour montrer le backend en soutenance**

**Prérequis** :
- Backend lancé : `mvn spring-boot:run -DskipTests` (port 8080)
- PostgreSQL actif
- cURL ou Postman disponible
- Variables d'environnement configurées (voir section Export)

---

## 🔐 ÉTAPE 1 : AUTHENTIFICATION

### 📝 Inscription d'un utilisateur

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "motDePasse": "SecurePassword123!",
    "role": "MEMBRE"
  }'

# Réponse : 
# {"message": "Utilisateur enregistré avec succès!"}
```

### 🔑 Login et récupération du token

```bash
# Remplacer par l'email/mot de passe d'un utilisateur réel
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.dupont@example.com",
    "motDePasse": "SecurePassword123!"
  }'

# Réponse :
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqZWFuLmR1cG9udEBleGFtcGxlLmNvbSIsImlhdCI6MTcxMzA3NjU0MCwiZXhwIjoxNzEzMTYyOTQwfQ.xXzZ...",
    "type": "Bearer",
    "id": 1,
    "email": "jean.dupont@example.com",
    "role": "MEMBRE"
  }
```

### 💾 Sauvegarder le token dans une variable

```bash
# ⬇️ ⬇️ IMPORTANT - Exécuter APRÈS le login ⬇️ ⬇️

# Option 1 : Avec jq (JSON parser)
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.dupont@example.com",
    "motDePasse": "SecurePassword123!"
  }' | jq -r '.token')

echo "Token sauvegardé: $JWT"

# Option 2 : Copier-coller manuel
# Remplacer $JWT par le token reçu au login
JWT="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...."
```

---

## 👤 ÉTAPE 2 : GESTION DES UTILISATEURS

### 📋 Récupérer le profil connecté

```bash
curl -X GET http://localhost:8080/api/utilisateurs/moi \
  -H "Authorization: Bearer $JWT"

# Réponse :
# {
#   "id": 1,
#   "nom": "Dupont",
#   "prenom": "Jean",
#   "email": "jean.dupont@example.com",
#   "role": "MEMBRE",
#   "dateCreation": "2024-05-13T10:30:00"
# }
```

### 📋 Lister tous les utilisateurs

```bash
curl -X GET http://localhost:8080/api/utilisateurs \
  -H "Authorization: Bearer $JWT"

# Retourne: Array de tous les utilisateurs
```

### 🔄 Changer le rôle d'un utilisateur

```bash
# Promouvoir utilisateur ID=1 en CHEF_PROJET
curl -X PATCH "http://localhost:8080/api/utilisateurs/1/role?role=CHEF_PROJET" \
  -H "Authorization: Bearer $JWT"

# Réponse : Utilisateur avec nouveau rôle → CHEF_PROJET
```

---

## 📊 ÉTAPE 3 : GESTION DES PROJETS

### ✨ Créer un projet

```bash
curl -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Projet E-Commerce",
    "description": "Application de vente en ligne",
    "dateDebut": "2024-05-01",
    "dateFin": "2024-08-31",
    "colonnes": [
      {
        "nom": "À faire",
        "couleur": "#FF6B6B",
        "position": 0
      },
      {
        "nom": "En cours",
        "couleur": "#FFA500",
        "position": 1
      },
      {
        "nom": "Fait",
        "couleur": "#51CF66",
        "position": 2
      }
    ]
  }'

# Réponse : Projet créé avec ID
# { "id": 5, "titre": "Projet E-Commerce", "proprietaireId": 1, ... }

PROJECT_ID=5  # ← Sauvegarder cet ID
```

### 📋 Récupérer mes projets

```bash
curl -X GET http://localhost:8080/api/projets \
  -H "Authorization: Bearer $JWT"

# Retourne: Array des projets où l'utilisateur est propriétaire ou membre
```

### 📋 Voir tous les projets (si CHEF_PROJET)

```bash
curl -X GET http://localhost:8080/api/projets/tous \
  -H "Authorization: Bearer $JWT"

# Si CHEF_PROJET → Retourne TOUS les projets de l'app
# Si MEMBRE → Retourne seulement ses projets
```

### 👥 Ajouter un membre au projet

```bash
# Créer d'abord un 2e utilisateur (voir ÉTAPE 2)
# Puis ajouter au projet : projet 5, utilisateur 2

curl -X POST http://localhost:8080/api/projets/5/membres/2 \
  -H "Authorization: Bearer $JWT"

# Réponse : Projet avec le nouvel utilisateur dans members
```

### 👥 Retirer un membre du projet

```bash
curl -X DELETE http://localhost:8080/api/projets/5/membres/2 \
  -H "Authorization: Bearer $JWT"

# Utilisateur 2 est retiré du projet 5
```

### ✏️ Modifier un projet

```bash
curl -X PUT http://localhost:8080/api/projets/5 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Projet E-Commerce - MISE À JOUR",
    "description": "Application de vente en ligne v2",
    "dateDebut": "2024-05-01",
    "dateFin": "2024-09-30"
  }'
```

### 🗑️ Supprimer un projet

```bash
curl -X DELETE http://localhost:8080/api/projets/5 \
  -H "Authorization: Bearer $JWT"

# Projet 5 supprimé (cascade → colonnes et tâches supprimées)
```

---

## 📝 ÉTAPE 4 : GESTION DES COLONNES

### ✨ Créer une colonne

```bash
curl -X POST http://localhost:8080/api/colonnes/5 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "En validation",
    "couleur": "#4ECDC4",
    "position": 3
  }'

# Réponse : { "id": 10, "nom": "En validation", ... }
COLONNE_ID=10  # ← Sauvegarder cet ID
```

### 📋 Récupérer les colonnes d'un projet

```bash
curl -X GET http://localhost:8080/api/colonnes/projet/5 \
  -H "Authorization: Bearer $JWT"

# Retourne: Array des colonnes du projet 5, triées par position
```

### ✏️ Modifier une colonne

```bash
curl -X PUT http://localhost:8080/api/colonnes/10 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "En validation (QA)",
    "couleur": "#9B59B6"
  }'
```

### 🎯 **DÉMO DRAG & DROP : Réorganiser les colonnes**

```bash
# Les colonnes actuelles : ID 8, 9, 10, 11
# On doit les réorganiser dans l'ordre : 9, 11, 8, 10

curl -X POST http://localhost:8080/api/colonnes/5/reorganiser \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '[9, 11, 8, 10]'

# ✅ Résultat : Les colonnes se réorganisent avec les nouvelles positions
# Colonne 9 → position 0
# Colonne 11 → position 1
# Colonne 8 → position 2
# Colonne 10 → position 3
```

### 🗑️ Supprimer une colonne

```bash
curl -X DELETE http://localhost:8080/api/colonnes/10 \
  -H "Authorization: Bearer $JWT"

# Colonne 10 supprimée (cascade → tâches supprimées)
```

---

## ✅ ÉTAPE 5 : GESTION DES TÂCHES

### ✨ Créer une tâche

```bash
curl -X POST http://localhost:8080/api/taches/10 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Implémenter authentification JWT",
    "description": "Ajouter l'authentification JWT au backend",
    "priorite": "HAUTE",
    "statut": "A_FAIRE",
    "dateEcheance": "2024-05-20"
  }'

# Réponse : { "id": 15, "titre": "Implémenter...", ... }
TACHE_ID=15  # ← Sauvegarder cet ID
```

### 📋 Récupérer les tâches d'une colonne

```bash
curl -X GET http://localhost:8080/api/taches/colonne/10 \
  -H "Authorization: Bearer $JWT"

# Retourne: Array des tâches de la colonne 10, triées par position
```

### 📋 Récupérer mes tâches assignées

```bash
curl -X GET http://localhost:8080/api/taches/mes-taches \
  -H "Authorization: Bearer $JWT"

# Retourne: Array des tâches assignées à l'utilisateur connecté
```

### ✏️ Modifier une tâche

```bash
curl -X PUT http://localhost:8080/api/taches/15 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Implémenter authentification JWT - VÉRIFIÉE",
    "description": "Ajouter l'authentification JWT au backend (avec tests)",
    "priorite": "URGENTE",
    "statut": "EN_COURS",
    "dateEcheance": "2024-05-18"
  }'
```

### 🎯 **DÉMO DRAG & DROP : Déplacer une tâche entre colonnes**

```bash
# Déplacer tâche 15 de sa colonne actuelle (10) vers la colonne 11
curl -X PATCH "http://localhost:8080/api/taches/15/deplacer?colonneId=11" \
  -H "Authorization: Bearer $JWT"

# ✅ Résultat : Tâche 15 se déplace dans la colonne 11
# C'est le "drag & drop horizontal"
```

### 🎯 **DÉMO DRAG & DROP : Réorganiser les tâches dans une colonne**

```bash
# Les tâches actuelles de la colonne 11 : ID 15, 16, 17
# On doit les réorganiser : 17, 15, 16

curl -X POST http://localhost:8080/api/taches/11/reorganiser \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '[17, 15, 16]'

# ✅ Résultat : Les tâches se réorganisent avec les nouvelles positions
# Tâche 17 → position 0 (en haut)
# Tâche 15 → position 1
# Tâche 16 → position 2 (en bas)
# C'est le "drag & drop vertical"
```

### 🔄 Changer le statut d'une tâche

```bash
# Passer le statut de "A_FAIRE" à "EN_COURS"
curl -X PATCH "http://localhost:8080/api/taches/15/statut?statut=EN_COURS" \
  -H "Authorization: Bearer $JWT"

# Statuts disponibles : A_FAIRE, EN_COURS, TERMINEE
```

### 👤 Assigner une tâche à un utilisateur

```bash
# Assigner tâche 15 à utilisateur 2
curl -X POST http://localhost:8080/api/taches/15/assignes/2 \
  -H "Authorization: Bearer $JWT"

# Utilisateur 2 est assigné à la tâche 15
```

### 👤 Retirer l'assignation d'un utilisateur

```bash
curl -X DELETE http://localhost:8080/api/taches/15/assignes/2 \
  -H "Authorization: Bearer $JWT"

# Utilisateur 2 n'est plus assigné à la tâche 15
```

### 🗑️ Supprimer une tâche

```bash
curl -X DELETE http://localhost:8080/api/taches/15 \
  -H "Authorization: Bearer $JWT"

# Tâche 15 supprimée (cascade → images supprimées)
```

---

## 📸 ÉTAPE 6 : UPLOAD D'IMAGES (Bonus démonstration)

### 📤 Créer une image test

```bash
# Créer une petite image PNG pour tester
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==" | base64 -d > test_image.png
```

### 📤 Upload image via multipart (Postman/cURL)

```bash
# Méthode 1 : Avec un fichier réel
curl -X POST http://localhost:8080/api/taches/15/upload-image \
  -H "Authorization: Bearer $JWT" \
  -F "image=@test_image.png"

# Réponse : Tâche avec l'URL image
# { "id": 15, "titre": "...", "imageUrls": ["/uploads/taches/52ab442e-6c77-4698-9648-66c3100da22a.png"] }
```

### 📤 Upload image via base64 (Swagger/cURL)

```bash
# Convertir image en base64
BASE64_IMAGE=$(base64 -w 0 test_image.png)

# Envoyer
curl -X POST http://localhost:8080/api/taches/15/upload-image-base64 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d "{
    \"image\": \"data:image/png;base64,$BASE64_IMAGE\"
  }"

# ✅ Image sauvegardée physiquement dans ./uploads/taches/
# ✅ URL stockée en base de données
```

### 📋 Récupérer les images d'une tâche

```bash
curl -X GET http://localhost:8080/api/taches/15/images \
  -H "Authorization: Bearer $JWT"

# Réponse :
# [
#   "/uploads/taches/52ab442e-6c77-4698-9648-66c3100da22a.png",
#   "/uploads/taches/a1b2c3d4-e5f6-1234-abcd-ef5678901234.jpg"
# ]
```

### 🖼️ Accéder à une image (depuis le navigateur)

```
http://localhost:8080/uploads/taches/52ab442e-6c77-4698-9648-66c3100da22a.png
```

### 🗑️ Supprimer une image

```bash
# Supprimer l'image à l'index 0
curl -X DELETE http://localhost:8080/api/taches/15/images/0 \
  -H "Authorization: Bearer $JWT"

# Image supprimée de la BD et du disque
```

---

## 🎬 SCRIPT COMPLET DE DÉMONSTRATION

**Copier-coller ce script entièrement pour une démo end-to-end rapidement** :

```bash
#!/bin/bash

echo "=========================================="
echo "DÉMONSTRATION MINI TRELLO"
echo "=========================================="

# 1. INSCRIPTION D'UN UTILISATEUR
echo -e "\n📝 Étape 1 : Inscription"
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Demo",
    "prenom": "User",
    "email": "demo@example.com",
    "motDePasse": "Demo123456"
  }' | jq .
sleep 1

# 2. LOGIN ET RÉCUPÉRATION DU TOKEN
echo -e "\n🔑 Étape 2 : Login"
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "motDePasse": "Demo123456"
  }' | jq -r '.token')
echo "JWT obtenu: ${JWT:0:50}..."
sleep 1

# 3. CRÉER UN PROJET
echo -e "\n📊 Étape 3 : Création d'un projet"
PROJECT=$(curl -s -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Démo Mini Trello",
    "description": "Projet de démonstration"
  }' | jq .)
PROJECT_ID=$(echo $PROJECT | jq -r '.id')
echo "Projet créé : ID=$PROJECT_ID"
sleep 1

# 4. CRÉER DES COLONNES
echo -e "\n📝 Étape 4 : Création des colonnes"
COL1=$(curl -s -X POST http://localhost:8080/api/colonnes/$PROJECT_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"nom":"À faire", "couleur":"#FF6B6B"}' | jq -r '.id')

COL2=$(curl -s -X POST http://localhost:8080/api/colonnes/$PROJECT_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"nom":"En cours", "couleur":"#FFA500"}' | jq -r '.id')

COL3=$(curl -s -X POST http://localhost:8080/api/colonnes/$PROJECT_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Fait", "couleur":"#51CF66"}' | jq -r '.id')

echo "Colonnes créées : $COL1, $COL2, $COL3"
sleep 1

# 5. CRÉER DES TÂCHES
echo -e "\n✅ Étape 5 : Création des tâches"
TASK1=$(curl -s -X POST http://localhost:8080/api/taches/$COL1 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Implémenter login",
    "description": "Authentification JWT",
    "priorite": "HAUTE"
  }' | jq -r '.id')

TASK2=$(curl -s -X POST http://localhost:8080/api/taches/$COL1 \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Design interface",
    "description": "Créer les mockups",
    "priorite": "MOYENNE"
  }' | jq -r '.id')

echo "Tâches créées : $TASK1, $TASK2"
sleep 1

# 6. DÉPLACER UNE TÂCHE (DRAG & DROP)
echo -e "\n🎯 Étape 6 : Déplacer une tâche"
curl -s -X PATCH "http://localhost:8080/api/taches/$TASK1/deplacer?colonneId=$COL2" \
  -H "Authorization: Bearer $JWT" | jq '.statut'
echo "Tâche $TASK1 déplacée vers colonne $COL2 ✅"
sleep 1

# 7. CHANGER LE STATUT
echo -e "\n🔄 Étape 7 : Changer le statut d'une tâche"
curl -s -X PATCH "http://localhost:8080/api/taches/$TASK1/statut?statut=EN_COURS" \
  -H "Authorization: Bearer $JWT" | jq '.statut'
echo "Statut mis à jour : EN_COURS ✅"

echo -e "\n=========================================="
echo "DÉMONSTRATION TERMINÉE ✅"
echo "=========================================="
```

---

## 🌐 TESTER VIA SWAGGER UI

```
http://localhost:8080/swagger-ui.html
```

**Avantages** :
- ✅ Interface graphique
- ✅ Pas besoin de cURL
- ✅ Documentation intégrée
- ✅ Support des uploads de fichiers
- ✅ Copier-coller les requêtes cURL générées

---

## 🎯 POINTS DE DÉMONSTRATION CLÉS

### 1. Authentification JWT
Montrer le token reçu et son contenu (décoder sur jwt.io si besoin)

### 2. Rôles
Changer l'utilisateur en CHEF_PROJET et voir la différence dans `/api/projets/tous`

### 3. DRAG & DROP
Créer plusieurs tâches et les réorganiser dans une colonne

### 4. Upload d'image
Uploader une image et montrer qu'elle est accessible via `/uploads/taches/`

### 5. Transactions
Supprimer un projet et vérifier que les colonnes et tâches sont aussi supprimées (cascade)

---

**Bon courage pour la soutenance ! 🚀**


