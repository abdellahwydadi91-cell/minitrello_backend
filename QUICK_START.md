# ⚡ QUICK START - MiniTrello Backend

> **5 minutes pour avoir le backend fonctionnel et prêt pour la démo**

---

## 🚀 Étape 1 : Démarrage du serveur (2 min)

### Prérequis ✅
- [ ] Java 17+ installé
- [ ] PostgreSQL lancé localement
- [ ] Base "mini_trello" créée

### Commande
```bash
cd C:\Users\BAB AL SAFA\Downloads\backend_mydblink\minitrello
mvn spring-boot:run -DskipTests
```

**Sortie attendue** :
```
...
Started MinitrelloApplication in X.XXs
Server started on port 8080 ✅
```

---

## 🌐 Étape 2 : Tester l'API (1 min)

### Swagger UI (Recommandé)
```
http://localhost:8080/swagger-ui.html
```
✅ Interface graphique - Pas besoin de cURL

### Ou en cURL
```bash
# Test simple
curl http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test","prenom":"User","email":"test@ex.com","motDePasse":"Test123"}'
```

---

## 📝 Étape 3 : Créer un utilisateur et obtenir JWT (1 min)

```bash
# 1. S'inscrire
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom":"Demo",
    "prenom":"User",
    "email":"demo@example.com",
    "motDePasse":"Demo123456"
  }'

# 2. Se connecter et récupérer le JWT
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"demo@example.com",
    "motDePasse":"Demo123456"
  }' | jq -r '.token')

# 3. Vérifier le token
echo $JWT
```

---

## 📊 Étape 4 : Créer un projet et tâches (1 min)

```bash
# Créer projet
PROJECT_ID=$(curl -s -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"titre":"Demo Project"}' | jq -r '.id')

echo "Projet créé: $PROJECT_ID"

# Créer colonne
COLONNE_ID=$(curl -s -X POST http://localhost:8080/api/colonnes/$PROJECT_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"nom":"À faire","couleur":"#FF6B6B"}' | jq -r '.id')

echo "Colonne créée: $COLONNE_ID"

# Créer tâche
TACHE_ID=$(curl -s -X POST http://localhost:8080/api/taches/$COLONNE_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"titre":"Ma première tâche","priorite":"HAUTE"}' | jq -r '.id')

echo "Tâche créée: $TACHE_ID"
```

---

## ✅ SUCCESS !

Si tout fonctionne, vous êtes prêt pour :
- ✅ Consulter les 4 documents de documentation
- ✅ Exécuter le SCRIPT_DEMO.md
- ✅ Faire la démonstration en direct

---

## 🔧 Troubleshooting Rapide

| Problème | Solution |
|----------|----------|
| Port 8080 utilisé | `netstat -ano \| findstr :8080` + tuer le processus |
| PostgreSQL pas connecté | `psql -U postgres` pour vérifier |
| Erreur "Database mini_trello not found" | Créer la BD : `createdb -U postgres mini_trello` |
| jq not found (Windows) | Télécharger depuis https://stedolan.github.io/jq/ |
| JWT non enregistré dans variable | Copier manuellement le token reçu |

---

## 📚 Docs Complètes

Après ce quick start, consultez :
1. **GUIDE_SOUTENANCE_BACKEND.md** - Complet & détaillé
2. **FEUILLE_TRICHE_SOUTENANCE.md** - Rapide à consulter
3. **SCRIPT_DEMO.md** - Toutes les commandes
4. **RESUME_1PAGE.md** - Diaporama

---

**Vous êtes maintenant prêt à présenter Mini Trello! 🎓**


