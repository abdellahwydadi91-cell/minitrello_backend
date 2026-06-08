# 📂 FICHIERS DU PROJET - NAVIGATION RAPIDE

> **Guide pour trouver rapidement n'importe quel fichier et comprendre son rôle**

---

## 🎯 FICHIERS PAR CATÉGORIE

### 🔴 FICHIERS CRITIQUES À CONNAÎTRE

```
✅ DOIT CONNAÎTRE (Top Priorité)

° MinitrelloApplication.java
  Lieu    : src/main/java/.../MinitrelloApplication.java
  Rôle    : Point d'entrée Spring Boot
  À faire : Vérifier que @SpringBootApplication est présent
  
° SecurityConfig.java
  Lieu    : src/main/java/.../security/SecurityConfig.java
  Rôle    : Configuration sécurité (CORS, JWT, authorization)
  À faire : Comprendre le filterChain et les permitAll()
  
° JwtUtils.java
  Lieu    : src/main/java/.../security/JwtUtils.java
  Rôle    : Génération et validation des JWT
  À faire : Connaître generateJwtToken() et validateJwtToken()
  
° JwtAuthenticationFilter.java
  Lieu    : src/main/java/.../security/JwtAuthenticationFilter.java
  Rôle    : Filtre exécuté sur chaque requête
  À faire : Comprendre doFilterInternal()
  
° AuthController.java
  Lieu    : src/main/java/.../controller/AuthController.java
  Rôle    : Endpoints login/register
  À faire : Connaître les réponses login et register
```

---

### 🟠 FICHIERS DE LA LOGIQUE MÉTIER

#### 📡 Contrôleurs (Endpoints REST)

```
ProjetController.java
├─ Path : src/main/java/.../controller/
├─ Class : @RestController @RequestMapping("/api/projets")
├─ Methods : getMesProjets(), getTousMesProjets(), creer(), modifier(), supprimer()
├─ Description : Gère l'accès aux endpoints projets
└─ Important : getUtilisateurConnecte() extrait l'utilisateur connecté du JWT

ColonneController.java
├─ Path : src/main/java/.../controller/
├─ Class : @RestController @RequestMapping("/api/colonnes")
├─ Methods : getByProjet(), creer(), modifier(), supprimer(), reorganiser()
├─ Description : Gère les endpoints colonnes
└─ Important : reorganiser() pour le drag & drop

TacheController.java
├─ Path : src/main/java/.../controller/
├─ Class : @RestController @RequestMapping("/api/taches")
├─ Methods : +15 endpoints pour CRUD, drag & drop, upload images
├─ Description : Plus complexe, inclut upload et drag & drop
└─ Important : uploadImage(), deplacer(), changerStatut()

UtilisateurController.java
├─ Path : src/main/java/.../controller/
├─ Class : @RestController @RequestMapping("/api/utilisateurs")
├─ Methods : getAll(), getById(), getMoi(), modifier(), changerRole()
├─ Description : Gère les endpoints utilisateurs
└─ Important : changerRole() pour modifier rôles
```

#### 🔧 Services (Logique métier)

```
ProjetService.java
├─ Path : src/main/java/.../service/
├─ Methods : creerProjet(), mettreAJourProjet(), ajouterMembreAuProjet(), verifierAcces()
├─ @Transactional : Toutes les mises à jour en transactions
└─ Key : toDTO() convertit Projet → ProjetDTO

ColonneService.java
├─ Path : src/main/java/.../service/
├─ Methods : creerColonne(), reorganiserColonnes(), obtenirColonnesParProjet()
├─ Position tracking: Gère automatiquement les positions
└─ Key : verifyAppartenance() vérifie que colonne appartient au projet

TacheService.java
├─ Path : src/main/java/.../service/
├─ Methods : creerTache(), deplacerTache(), reorganiserTaches(), uploadImage(), deleteImage()
├─ File I/O    : Gère upload/suppression fichiers physiquement
└─ Key : File uploads + UUID generation + permission checking

UtilisateurService.java
├─ Path : src/main/java/.../service/
├─ Methods : obtenirUtilisateurParEmail(), changerRole(), changerMotDePasse()
├─ Password  : Utilise PasswordEncoder.encode()
└─ Key : Aucun mot de passe n'est jamais retourné
```

#### 💾 Repositories (Accès BD)

```
UtilisateurRepository extends JpaRepository<Utilisateur, Long>
├─ Query : findByEmail(String email) → custom query
└─ Use   : Toujours utilisé pour chercher utilisateur

ProjetRepository extends JpaRepository<Projet, Long>
├─ Queries : findByProprietaire(), findProjetsByUtilisateur(), findAllWithDetails()
└─ Use    : Requêtes complexes avec @Query

ColonneRepository extends JpaRepository<Colonne, Long>
├─ Queries : findByProjet(), findByIdAndProjet(), existsByNomAndProjet()
└─ Use    : Vérifications existence + requêtes spécialisées

TacheRepository extends JpaRepository<Tache, Long>
├─ Queries : findByColonna(), findTachesByAssigne(), findByIdWithRelations()
├─ Fetch   : LEFT JOIN FETCH pour charger relations
└─ Use    : Requêtes avec eager loading des assignés
```

---

### 🟡 FICHIERS DE DONNÉES

#### 📦 Entités JPA

```
Utilisateur.java
├─ Colonne  : id, nom, prenom, email, mot_de_passe, role, avatar, dateCreation
├─ Enum    : Role { CHEF_PROJET, MEMBRE }
├─ Relations: 1 propriétaire, N assignés aux tâches
└─ Security: @JsonProperty(access = WRITE_ONLY) sur mot_de_passe

Projet.java
├─ Colonne    : id, titre, description, dateDebut, dateFin, dateCreation
├─ Father Key : proprietaire_id (ManyToOne)
├─ Relations  : OneToMany colonnes, ManyToMany membres
└─ Default    : dateCreation = now()

Colonne.java
├─ Colonne   : id, nom, couleur, position
├─ Foreign Key : projet_id
├─ Relations   : OneToMany taches
└─ Important   : position pour l'ordre

Tache.java
├─ Colonne    : id, titre, description, priorite, statut, position, dateEcheance, dateCreation
├─ Foreign Key: colonne_id
├─ Relations  : ManyToMany assignés, OneToMany commentaires, ElementCollection imageUrls
├─ Enum      : StatutTache { A_FAIRE, EN_COURS, TERMINEE }, Priorite { BASSE, MOYENNE, HAUTE, URGENTE }
└─ Important  : imageUrls stocke les URLs de fichiers

Commentaire.java
├─ Colonne    : id, contenu, dateCreation, dateModification
├─ Foreign Key: tache_id, auteur_id
└─ Relations  : ManyToOne tache, ManyToOne auteur

StatutTache.java
└─ Enum   : A_FAIRE, EN_COURS, TERMINEE
```

#### 📨 DTOs (Transfert données)

```
ProjetDTO.java
├─ Champs : id, titre, description, proprietaireId, proprietaireNom, membreIds
├─ Nested : MembreDTO { id, nom, prenom, email }
└─ Use    : Réponse API /api/projets

TacheDTO.java
├─ Champs : id, titre, description, priorite, statut, position, colonneId, projetId
├─ Nested : assigneIds, assigneNoms, imageUrls
└─ Use    : Réponse API /api/taches

JwtResponse.java
├─ Champs : token, type, id, email, role
└─ Use    : Réponse login (/api/auth/login)

LoginRequest.java
├─ Champs : email, motDePasse
└─ Use    : Requête login
```

---

### 🟢 FICHIERS DE SÉCURITÉ

```
UserDetailsServiceImpl.java
├─ Implémente : UserDetailsService
├─ Method    : loadUserByUsername(String email)
├─ Action    : Charge utilisateur et ses autorités (rôles)
└─ Important : Convertit Role → "ROLE_" + role.name()

SecurityConfig.java
├─ Bean #1   : PasswordEncoder() → BCryptPasswordEncoder
├─ Bean #2   : AuthenticationProvider() → DaoAuthenticationProvider
├─ Bean #3   : CorsConfigurationSource() → CORS config
├─ Bean #4   : SecurityFilterChain() → Autorisations HTTP
└─ Pipeline  : CORS → CSRF disabled → STATELESS sessions → Autorisations → JWT Filter
```

---

### ⚙️ FICHIERS DE CONFIGURATION

```
application.properties
├─ server.port=8080
├─ spring.datasource.* (PostgreSQL)
├─ spring.jpa.* (Hibernate config)
├─ app.jwt.secret (JWT key)
├─ app.jwt.expiration (JWT timeout)
├─ file.upload-dir (Dossier uploads)
└─ spring.servlet.multipart.* (Upload size limits)

pom.xml
├─ spring-boot-starter-* (Spring Boot dépendances)
├─ postgresql (Driver DB)
├─ jjwt-* (JWT)
├─ lombok (Boilerplate reduction)
├─ springdoc-openapi (Swagger)
└─ spring-boot-starter-test (Tests)
```

---

### 🧪 FICHIERS DE TEST

```
MinitrelloApplicationTests.java
├─ Test      : Spring Boot context loads
└─ Status    : Vérifier l'appli démarre

ProjetControllerIntegrationTest.java
├─ Test      : Endpoints /api/projets
├─ Includes  : GET, POST, PUT, DELETE
└─ Mock      : Services + BD mockés

TacheControllerIntegrationTest.java
├─ Test      : Endpoints /api/taches
├─ Includes  : Drag & drop, upload image, changement statut
└─ Mock      : Services + BD mockés

ProjetServiceTest.java
├─ Test      : Logique métier projets
├─ Includes  : création, modification, gestion membres
└─ Mock      : Repositories

JwtUtilsTest.java
├─ Test      : Génération/validation JWT
├─ Includes  : generateToken(), validateToken(), expiration
└─ Mock      : Aucun
```

---

### 📚 FICHIERS DE DOCUMENTATION (Soutenance)

```
INDEX_DOCUMENTS.md ← LIRE EN PREMIER
├─ Guide complet de tous les docs
├─ Checklist avant soutenance
└─ Guide de recherche rapide

QUICK_START.md ← LIRE EN DEUXIÈME
├─ 5 minutes pour avoir l'app fonctionnelle
├─ Étapes simples
└─ Troubleshooting

ARCHITECTURE_FICHIERS.md ← LIRE EN TROISIÈME
├─ Arborescence du projet
├─ Explications par fichier/dossier
└─ Architecture par couches

GUIDE_SOUTENANCE_BACKEND.md
├─ Documentation exhaustive (~40 pages)
├─ Explication ligne par ligne du code
└─ Référence complète

FEUILLE_TRICHE_SOUTENANCE.md
├─ Résumé rapide (~4 pages)
├─ À imprimer et avoir en main
└─ Aide-mémoire

SCRIPT_DEMO.md
├─ Toutes les commandes curl
├─ Scénarios de démo
└─ Prêt à copier-coller

RESUME_1PAGE.md
├─ Format diaporama (12 slides)
├─ Points clés uniquement
└─ Pour présentation
```

---

## 🗺️ NAVIGATION RECOMMANDÉE PAR OBJECTIF

### Objectif: "Je dois présenter demain"
1. ✅ Lire QUICK_START.md (5 min)
2. ✅ Lire RESUME_1PAGE.md (5 min)
3. ✅ Imprimer FEUILLE_TRICHE_SOUTENANCE.md
4. ✅ Tester SCRIPT_DEMO.md (10 min)
5. ✅ C'est bon!

### Objectif: "Je dois comprendre complètement"
1. ✅ Lire ARCHITECTURE_FICHIERS.md (20 min)
2. ✅ Lire GUIDE_SOUTENANCE_BACKEND.md (45 min)
3. ✅ Explorer le code source (1h)
4. ✅ Exécuter SCRIPT_DEMO.md (20 min)
5. ✅ Tests unitaires (30 min)

### Objectif: "Je dois ajouter une feature"
1. ✅ Lire ARCHITECTURE_FICHIERS.md (20 min)
2. ✅ Localiser les fichiers concernés
3. ✅ Suivre le pattern existant
4. ✅ Ajouter tests
5. ✅ Vérifier avec Swagger UI

---

## 📊 TABLEAU RÉCAPITULATIF

| Fichier | Localisation | Type | Importance | Lire en premier | Pages |
|---------|--------------|------|-----------|-----------------|-------|
| MinitrelloApplication | src/main/java | Classe | 🔴 Critique | 1 | 1 |
| SecurityConfig | security/ | Config | 🔴 Critique | 2 | 3 |
| JwtUtils | security/ | Logique | 🔴 Critique | 3 | 2 |
| AuthController | controller/ | Contrôle | 🟠 Important | 4 | 3 |
| ProjetService | service/ | Business | 🟠 Important | 5 | 4 |
| Utilisateur | entity/ | Modèle | 🟠 Important | 6 | 2 |
| ProjetRepository | repository/ | DAO | 🟡 Normal | 7 | 2 |
| TacheController | controller/ | Contrôle | 🟡 Normal | 8 | 5 |

---

## 🎯 FICHIERS PAR CAS D'USAGE

### "Comment fonctionne le login?"
1. AuthController.authenticateUser() 
2. JwtUtils.generateJwtToken()
3. JwtResponse retourné

### "Comment fonctionne le drag & drop?"
1. TacheController.deplacer() / reorganiser()
2. TacheService.deplacerTache() / reorganiserTaches()
3. Positions mises à jour en BD

### "Comment se passe la sécurité?"
1. SecurityConfig.filterChain()
2. JwtAuthenticationFilter.doFilterInternal()
3. UserDetailsServiceImpl.loadUserByUsername()

### "Comment fonctionne l'upload d'image?"
1. TacheController.uploadImage()
2. TacheService.uploadImage()
3. Fichier sauvegardé dans ./uploads/taches/
4. URL stockée en BD

### "Comment créer un nouveau projet?"
1. ProjetController.creer()
2. ProjetService.creerProjet()
3. ProjetRepository.save()
4. BD: INSERT INTO projets

---

## 📝 FICHIERS À MODIFIER SI...

```
Ajouter nouvel endpoint?
→ Fichier: controller/*Controller.java

Modifier la logique métier?
→ Fichier: service/*Service.java

Ajouter une nouvelle entité?
→ Fichiers: entity/*.java + repository/*Repository.java

Modifier l'authentification?
→ Fichiers: security/SecurityConfig.java + JwtUtils.java

Changer la BD (PostgreSQL)?
→ Fichier: application.properties + entity/*.java

Ajouter des validations?
→ Fichiers: entity/*.java (annotations @Valid, @NotNull)
```

---

## ✅ CHECKLIST POUR MAÎTRISER LE PROJET

- [ ] Lire ARCHITECTURE_FICHIERS.md (arborescence)
- [ ] Ouvrir chaque contrôleur dans l'IDE
- [ ] Ouvrir chaque service dans l'IDE
- [ ] Ouvrir SecurityConfig.java (très important!)
- [ ] Ouvrir JwtUtils.java (authentification)
- [ ] Lire application.properties
- [ ] Exécuter SCRIPT_DEMO.md avec le terminal
- [ ] Tester les endpoints via Swagger UI
- [ ] Lancer les tests unitaires : `mvn test`
- [ ] Comprendre le flux données complet

---

**Vous êtes maintenant capable de naviguer complètement dans le projet! 🗺️**


