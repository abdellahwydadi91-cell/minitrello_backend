# 📚 Guide Complet MiniTrello - Backend pour la Soutenance

> **Projet Mini Trello** - Gestion de tâches inspirée de Trello  
> **Développeur** : [Votre Nom]  
> **Date soutenance** : [À remplir]

---

## 📋 Table des matières

1. [Architecture générale](#1-architecture-générale)
2. [Authentification & Sécurité](#2-authentification--sécurité)
3. [Routes & API REST](#3-routes--api-rest)
4. [Logique métier](#4-logique-métier)
5. [Modèles de données](#5-modèles-de-données)
6. [Gestion des erreurs](#6-gestion-des-erreurs)
7. [5 Points techniques clés](#7-5-points-techniques-clés)

---

## 1. Architecture générale

### 🏗️ Framework et Stack Technique

**Framework** : **Spring Boot 3.2.5** (avec Java 17)

```
✅ Spring Boot Starter Web          → Serveur REST HTTP
✅ Spring Data JPA                  → ORM (Object-Relational Mapping)
✅ Spring Security                  → Authentification & Autorisation
✅ PostgreSQL                       → Base de données
✅ JJWT (JSON Web Token)            → Génération et validation des tokens JWT
✅ Lombok                           → Réduction du code boilerplate
✅ SpringDoc OpenAPI (Swagger)      → Documentation API interactive
✅ Commons FileUpload               → Gestion des uploads de fichiers
```

### 📁 Structure du projet

```
backend/
├── src/main/java/org/minitrello/backend/
│   ├── MinitrelloApplication.java           ← Point d'entrée Spring Boot
│   ├── controller/                          ← Endpoints REST
│   │   ├── AuthController.java              (Login, Register)
│   │   ├── ProjetController.java            (Gestion des projets)
│   │   ├── ColonneController.java           (Gestion des colonnes)
│   │   ├── TacheController.java             (Gestion des tâches + upload images)
│   │   └── UtilisateurController.java       (Gestion des utilisateurs)
│   ├── service/                             ← Logique métier
│   │   ├── ProjetService.java
│   │   ├── ColonneService.java
│   │   ├── TacheService.java
│   │   └── UtilisateurService.java
│   ├── entity/                              ← Modèles JPA (Base de données)
│   │   ├── Utilisateur.java
│   │   ├── Projet.java
│   │   ├── Colonne.java
│   │   ├── Tache.java
│   │   ├── Commentaire.java
│   │   └── StatutTache.java (Enum)
│   ├── repository/                          ← Accès base de données (DAO)
│   │   ├── UtilisateurRepository.java
│   │   ├── ProjetRepository.java
│   │   ├── ColonneRepository.java
│   │   └── TacheRepository.java
│   ├── dto/                                 ← Objets de transfert
│   │   ├── ProjetDTO.java
│   │   ├── TacheDTO.java
│   │   ├── JwtResponse.java
│   │   └── LoginRequest.java
│   ├── security/                            ← Authentification JWT
│   │   ├── JwtUtils.java                    (Génération & validation tokens)
│   │   ├── JwtAuthenticationFilter.java     (Filtre pour chaque requête)
│   │   ├── SecurityConfig.java              (Configuration Spring Security)
│   │   └── UserDetailsServiceImpl.java       (Chargement utilisateur)
│   └── config/                              ← Configurations optionnelles
└── pom.xml                                  ← Dépendances Maven

uploads/taches/                              ← Dossier d'upload des images
```

### 🔄 Flux général d'une requête

```
1. Frontend (React) génère une requête avec le JWT en header
                        ↓
2. Spring reçoit la requête
                        ↓
3. JwtAuthenticationFilter vérifie le JWT
                        ↓
4. SecurityConfig autorise ou refuse l'accès
                        ↓
5. Controller traite la requête
                        ↓
6. Service exécute la logique métier
                        ↓
7. Repository interagit avec PostgreSQL
                        ↓
8. Response retournée au frontend en JSON
```

---

## 2. Authentification & Sécurité

### 🔐 Comment fonctionne l'authentification JWT

#### **A. Inscription (POST /api/auth/register)**

📝 **Code dans AuthController.java** :

```java
@PostMapping("/register")
public ResponseEntity<?> registerUser(@Valid @RequestBody Utilisateur utilisateur) {
    // ✅ 1. Vérifier que l'email n'existe pas déjà
    if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
        return ResponseEntity.badRequest().body("Erreur: Email déjà utilisé!");
    }

    // ✅ 2. Créer un nouvel utilisateur
    Utilisateur newUtilisateur = new Utilisateur();
    newUtilisateur.setNom(utilisateur.getNom());
    newUtilisateur.setPrenom(utilisateur.getPrenom());
    newUtilisateur.setEmail(utilisateur.getEmail());
    
    // ✅ 3. Hasher le mot de passe (Bcrypt)
    newUtilisateur.setMotDePasse(encoder.encode(utilisateur.getMotDePasse()));
    
    // ✅ 4. Assigner le rôle par défaut
    newUtilisateur.setRole(utilisateur.getRole() != null ? 
        utilisateur.getRole() : Utilisateur.Role.MEMBRE);

    // ✅ 5. Sauvegarder en base de données
    utilisateurRepository.save(newUtilisateur);

    return ResponseEntity.ok("Utilisateur enregistré avec succès!");
}
```

**Explication ligne par ligne** :
- **Ligne 5** : Empêche les doublons (sécurité)
- **Ligne 10-12** : Crée une entité Utilisateur vierge
- **Ligne 16** : `encoder.encode()` → Utilise **BCrypt** pour hasher le mot de passe (irréversible)
- **Ligne 19-20** : Le rôle par défaut est `MEMBRE` si non spécifié
- **Ligne 24** : Sauvegarde en base de données PostgreSQL

#### **B. Connexion (POST /api/auth/login)**

```java
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    // ✅ 1. Authentifier l'utilisateur (Spring Security vérifie email + mot de passe)
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), 
            loginRequest.getMotDePasse()
        )
    );

    // ✅ 2. Stocker l'authentification dans le contexte de sécurité
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // ✅ 3. Générer un token JWT
    String jwt = jwtUtils.generateJwtToken(authentication);

    // ✅ 4. Récupérer les infos de l'utilisateur
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername()).orElseThrow();

    // ✅ 5. Retourner le JWT + infos utilisateur
    return ResponseEntity.ok(new JwtResponse(jwt, utilisateur.getId(), utilisateur.getEmail(), utilisateur.getRole().name()));
}
```

**Réponse du serveur** (JwtResponse.java) :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "utilisateur@example.com",
  "role": "MEMBRE"
}
```

#### **C. Génération du token JWT (JwtUtils.java)**

```java
public String generateJwtToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    return Jwts.builder()
        // Subject = email de l'utilisateur
        .setSubject(userPrincipal.getUsername())
        
        // Date d'émission
        .setIssuedAt(new Date())
        
        // Date d'expiration (24h par défaut)
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        
        // Signature avec une clé secrete HMAC-SHA256
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        
        // Encoder en base64
        .compact();
}
```

**Structure du JWT** :
```
Header:   { "alg": "HS256", "typ": "JWT" }
Payload:  { "sub": "user@email.com", "iat": 1234567890, "exp": 1234654290 }
Signature: HMACSHA256(header + payload, SECRET_KEY)
```

#### **D. Vérification du token (JwtAuthenticationFilter.java)**

Pour chaque requête protégée :

```java
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    // ✅ 1. Extraire le token du header Authorization
    final String authHeader = request.getHeader("Authorization");
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        // Pas de token → passer à la suite (Spring Security rejettera si endpoint protégé)
        filterChain.doFilter(request, response);
        return;
    }

    try {
        // ✅ 2. Extraire le JWT (en retirant "Bearer ")
        final String jwt = authHeader.substring(7);

        // ✅ 3. Extraire le username du JWT
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        // ✅ 4. Charger l'utilisateur depuis la base de données
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ✅ 5. Valider la signature du JWT
        if (jwtUtils.validateJwtToken(jwt)) {
            // Créer un token d'authentification valide
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()  // Charger les rôles
                );

            // ✅ 6. Marquer comme authentifié dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    } catch (Exception e) {
        // Token invalide ou expiré
        SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
}
```

### 🛡️ Gestion des rôles

**Entité Utilisateur.java** :

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Role role = Role.MEMBRE;

public enum Role {
    CHEF_PROJET,  // Accès à tous les projets
    MEMBRE        // Accès uniquement à ses projets
}
```

**Conversion en autorités Spring (UserDetailsServiceImpl.java)** :

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

    // Convertir CHEF_PROJET → ROLE_CHEF_PROJET
    List<GrantedAuthority> authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
    );

    return new User(utilisateur.getEmail(), utilisateur.getMotDePasse(), authorities);
}
```

**Utilisation des rôles dans les contrôleurs** :

```java
@GetMapping("/tous")
public ResponseEntity<List<ProjetDTO>> getTousMesProjets() {
    Utilisateur u = getUtilisateurConnecte();

    // ✅ Chef de projet → voir TOUS les projets
    if (u.getRole() == Utilisateur.Role.CHEF_PROJET) {
        return ResponseEntity.ok(projetService.obtenirTousLesProjets());
    }
    
    // ✅ Membre → voir uniquement SES projets
    return ResponseEntity.ok(projetService.obtenirProjetsByUtilisateur(u.getId()));
}
```

### 🔒 Configuration de sécurité (SecurityConfig.java)

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // ✅ Activer CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        // ✅ Désactiver CSRF (API REST sans sessions)
        .csrf(csrf -> csrf.disable())
        
        // ✅ Sessions sans état (STATELESS pour JWT)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // ✅ Autorisations
        .authorizeHttpRequests(authz -> authz
            // Swagger public
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            
            // Auth public
            .requestMatchers("/api/auth/**").permitAll()
            
            // Upload public (GET seulement)
            .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
            
            // TOUT LE RESTE nécessite authentification
            .anyRequest().authenticated()
        );

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

## 3. Routes & API REST

### 📡 **ENDPOINTS COMPLETS**

#### **🔓 AUTHENTIFICATION (Publique)**

| Méthode | Route | Description | Protégée | Exemple |
|---------|-------|-------------|----------|---------|
| `POST` | `/api/auth/login` | Se connecter | ❌ Non | `{"email":"user@ex.com", "motDePasse":"123"}` |
| `POST` | `/api/auth/register` | S'inscrire | ❌ Non | `{"nom":"Dupont", "prenom":"Jean", "email":"jean@ex.com", "motDePasse":"123"}` |

**Curl exemple** :
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com", "motDePasse":"password123"}'
```

---

#### **👤 UTILISATEURS (Protégé)**

| Méthode | Route | Description | Rôle requis | Protégée |
|---------|-------|-------------|------------|----------|
| `GET` | `/api/utilisateurs` | Lister tous les utilisateurs | Aucun | ✅ Oui |
| `GET` | `/api/utilisateurs/{id}` | Récupérer un utilisateur | Aucun | ✅ Oui |
| `GET` | `/api/utilisateurs/moi` | Profil de l'utilisateur connecté | Aucun | ✅ Oui |
| `PUT` | `/api/utilisateurs/{id}` | Modifier profil utilisateur | Aucun | ✅ Oui |
| `DELETE` | `/api/utilisateurs/{id}` | Supprimer utilisateur | Aucun | ✅ Oui |
| `PATCH` | `/api/utilisateurs/{id}/role` | Changer le rôle | Admin ou propriétaire | ✅ Oui |
| `PATCH` | `/api/utilisateurs/{id}/mot-de-passe` | Changer mot de passe | Propriétaire du compte | ✅ Oui |

**Exemple avec JWT** :
```bash
curl -X GET http://localhost:8080/api/utilisateurs/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

#### **📊 PROJETS (Protégé)**

| Méthode | Route | Description | Logique spéciale | Protégée |
|---------|-------|-------------|------------------|----------|
| `GET` | `/api/projets` | Mes projets (propriétaire ou membre) | Utilisateur connecté | ✅ Oui |
| `GET` | `/api/projets/{id}` | Détail d'un projet | Vérifier accès | ✅ Oui |
| `GET` | `/api/projets/tous` | Mes projets (CHEF voit tous) | CHEF_PROJET voit tous les projets | ✅ Oui |
| `GET` | `/api/projets/all` | Tous les projets | Accessible à tous | ✅ Oui |
| `POST` | `/api/projets` | Créer projet | Utilisateur = propriétaire | ✅ Oui |
| `PUT` | `/api/projets/{id}` | Modifier projet | Vérifier propriétaire | ✅ Oui |
| `DELETE` | `/api/projets/{id}` | Supprimer projet | Vérifier propriétaire | ✅ Oui |
| `POST` | `/api/projets/{id}/membres/{membreId}` | Ajouter membre | Vérifier propriétaire | ✅ Oui |
| `DELETE` | `/api/projets/{id}/membres/{membreId}` | Retirer membre | Vérifier propriétaire | ✅ Oui |

**Exemple de création** :
```bash
# Créer un projet
curl -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Mon Projet",
    "description": "Description du projet",
    "dateDebut": "2024-01-01",
    "dateFin": "2024-12-31"
  }'
```

---

#### **📝 COLONNES (Protégé)**

| Méthode | Route | Description | Protégée |
|---------|-------|-------------|----------|
| `GET` | `/api/colonnes/projet/{projetId}` | Colonnes d'un projet | ✅ Oui |
| `GET` | `/api/colonnes/{id}` | Détail d'une colonne | ✅ Oui |
| `POST` | `/api/colonnes/{projetId}` | Créer colonne | ✅ Oui |
| `PUT` | `/api/colonnes/{id}` | Modifier colonne | ✅ Oui |
| `DELETE` | `/api/colonnes/{id}` | Supprimer colonne | ✅ Oui |
| `POST` | `/api/colonnes/{projetId}/reorganiser` | Réorganiser (drag & drop) | ✅ Oui |

**Exemple de réorganisation** (Pour le drag & drop) :
```bash
curl -X POST http://localhost:8080/api/colonnes/5/reorganiser \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '[10, 11, 12]'  # IDs des colonnes dans le nouvel ordre
```

---

#### **✅ TÂCHES (Protégé)**

| Méthode | Route | Description | Protégée |
|---------|-------|-------------|----------|
| `GET` | `/api/taches/colonne/{colonneId}` | Tâches d'une colonne | ✅ Oui |
| `GET` | `/api/taches/{id}` | Détail d'une tâche | ✅ Oui |
| `GET` | `/api/taches/mes-taches` | Mes tâches assignées | ✅ Oui |
| `GET` | `/api/taches/assignees/{utilisateurId}` | Tâches assignées à X | ✅ Oui |
| `POST` | `/api/taches/{colonneId}` | Créer tâche | ✅ Oui |
| `PUT` | `/api/taches/{id}` | Modifier tâche | ✅ Oui |
| `DELETE` | `/api/taches/{id}` | Supprimer tâche | ✅ Oui |
| `PATCH` | `/api/taches/{id}/deplacer` | Déplacer vers autre colonne | ✅ Oui |
| `POST` | `/api/taches/{colonneId}/reorganiser` | Réorganiser dans colonne | ✅ Oui |
| `POST` | `/api/taches/{id}/assignes/{assigneId}` | Assigner utilisateur | ✅ Oui |
| `DELETE` | `/api/taches/{id}/assignes/{assigneId}` | Retirer assignation | ✅ Oui |
| `PATCH` | `/api/taches/{id}/statut` | Changer statut (A_FAIRE → EN_COURS → TERMINEE) | ✅ Oui |
| `POST` | `/api/taches/{id}/upload-image` | Upload image (multipart) | ✅ Oui |
| `POST` | `/api/taches/{id}/upload-image-base64` | Upload image (base64 - Swagger) | ✅ Oui |
| `DELETE` | `/api/taches/{id}/images/{imageIndex}` | Supprimer une image | ✅ Oui |
| `GET` | `/api/taches/{id}/images` | Récupérer images d'une tâche | ✅ Oui |

**Exemple de changement de statut** :
```bash
curl -X PATCH "http://localhost:8080/api/taches/5/statut?statut=EN_COURS" \
  -H "Authorization: Bearer <JWT>"
```

**Exemple de drag & drop (déplacement)** :
```bash
# Déplacer tâche ID 15 vers colonne ID 8
curl -X PATCH "http://localhost:8080/api/taches/15/deplacer?colonneId=8" \
  -H "Authorization: Bearer <JWT>"
```

---

## 4. Logique métier

### 🎯 **Création d'un projet**

**Flux complet dans ProjetService.java** :

```java
public ProjetDTO creerProjet(Projet projet, Long proprietaireId) {
    // ✅ 1. Vérifier que le propriétaire existe
    Utilisateur proprietaire = findUtilisateur(proprietaireId);
    
    // ✅ 2. Assigner automatiquement le propriétaire
    projet.setProprietaire(proprietaire);

    // ✅ 3. Initialiser les colonnes par défaut
    if (projet.getColonnes() != null) {
        for (int i = 0; i < projet.getColonnes().size(); i++) {
            projet.getColonnes().get(i).setPosition(i + 1);
            projet.getColonnes().get(i).setProjet(projet);
        }
    }

    // ✅ 4. Créer en base de données
    return toDTO(projetRepository.save(projet));
}
```

**Qui est propriétaire ?** :
- L'utilisateur qui crée le projet
- Le propriétaire a tous les droits (modifier, supprimer, ajouter membres)

---

### 👥 **Ajout/Suppression de membres**

**Ajouter un membre (ProjetService.java)** :

```java
public ProjetDTO ajouterMembreAuProjet(Long projetId, Long membreId) {
    // ✅ 1. Récupérer le projet
    Projet projet = findProjet(projetId);
    
    // ✅ 2. Récupérer l'utilisateur à ajouter
    Utilisateur membre = findUtilisateur(membreId);
    
    // ✅ 3. Éviter les doublons
    if (!projet.getMembres().contains(membre)) {
        projet.getMembres().add(membre);
    }
    
    // ✅ 4. Sauvegarder
    return toDTO(projetRepository.save(projet));
}
```

**Structure en base de données** :
```
Table: membres_projet
┌────────────┬──────────────┐
│ projet_id  │ utilisateur_id│
├────────────┼──────────────┤
│    1       │      2       │  ← Utilisateur 2 ajouté au projet 1
│    1       │      3       │  ← Utilisateur 3 ajouté au projet 1
└────────────┴──────────────┘
```

---

### 📋 **Gestion des colonnes**

**Création automatique de colonnes** :

```java
public ColonneDTO creerColonne(Colonne colonne, Long projetId) {
    Projet projet = projetRepository.findById(projetId)
        .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

    // ✅ 1. Vérifier qu'une colonne avec ce nom n'existe pas déjà
    if (colonneRepository.existsByNomAndProjet(colonne.getNom(), projet)) {
        throw new RuntimeException("Une colonne avec ce nom existe déjà");
    }

    colonne.setProjet(projet);

    // ✅ 2. Calculer la position automatiquement
    List<Colonne> colonnesExistantes = colonneRepository
        .findByProjetOrderByPositionAsc(projet);
    colonne.setPosition(colonnesExistantes.size());  // Position = nombre de colonnes

    return toDTO(colonneRepository.save(colonne));
}
```

**Réorganisation (Drag & Drop)** :

```java
public void reorganiserColonnes(Long projetId, List<Long> colonneIds) {
    Projet projet = projetRepository.findById(projetId)
        .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

    // ✅ Pour chaque ID reçu, mettre à jour la position
    for (int i = 0; i < colonneIds.size(); i++) {
        Long colonneId = colonneIds.get(i);
        Colonne colonne = colonneRepository.findById(colonneId)
            .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));

        // Vérifier appartenance
        if (!colonne.getProjet().equals(projet)) {
            throw new RuntimeException("La colonne n'appartient pas à ce projet");
        }

        // ✅ Mettre à jour la position
        colonne.setPosition(i);
        colonneRepository.save(colonne);
    }
}
```

---

### ✅ **Gestion des tâches**

**Création de tâche (TacheService.java)** :

```java
public Tache creerTache(Tache tache, Long colonneId, Long utilisateurId) {
    // ✅ 1. Vérifier l'accès au projet
    Colonne colonne = colonneRepository.findByIdWithProjet(colonneId)
        .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));

    Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    // ✅ 2. Vérifier que l'utilisateur est membre du projet
    if (!peutAccederAuProjet(utilisateur, colonne.getProjet())) {
        throw new AccessDeniedException("Vous n'etes pas membre de ce projet");
    }

    // ✅ 3. Créer la tâche dans la colonne
    return creerTacheDansColonne(tache, colonne);
}

private Tache creerTacheDansColonne(Tache tache, Colonne colonne) {
    tache.setColonne(colonne);
    
    // ✅ Calculer la position automatiquement
    List<Tache> tachesExistantes = tacheRepository
        .findByColonneOrderByPositionAsc(colonne);
    tache.setPosition(tachesExistantes.size());
    
    return tacheRepository.save(tache);
}
```

**Déplacement de tâche (Drag & Drop)** :

```java
public Tache deplacerTache(Long tacheId, Long nouvelleColonneId) {
    // ✅ 1. Récupérer la tâche
    Tache tache = tacheRepository.findById(tacheId)
        .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

    // ✅ 2. Récupérer la nouvelle colonne
    Colonne nouvelleColonne = colonneRepository.findById(nouvelleColonneId)
        .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));

    // ✅ 3. Mettre à jour l'appartenance
    tache.setColonne(nouvelleColonne);
    tacheRepository.save(tache);

    // ✅ 4. Recharger avec toutes les relations (pour le DTO)
    return tacheRepository.findByIdWithColonneAndProjet(tacheId)
        .orElseThrow(() -> new RuntimeException("Tâche non trouvée après save"));
}
```

**Changement de statut** :

```java
@PatchMapping("/{id}/statut")
public ResponseEntity<TacheDTO> changerStatut(@PathVariable Long id,
                                             @RequestParam String statut) {
    // ✅ 1. Récupérer la tâche avec relations
    Tache tache = tacheService.obtenirTacheParIdAvecRelations(id)
        .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

    // ✅ 2. Convertir le string en enum StatutTache(A_FAIRE, EN_COURS, TERMINEE)
    tache.setStatut(StatutTache.valueOf(statut));

    // ✅ 3. Sauvegarder
    tacheService.sauvegarderStatut(tache);

    // ✅ 4. Recharger et retourner
    Tache tacheComplete = tacheService.obtenirTacheParIdAvecRelations(id)
        .orElseThrow();
    return ResponseEntity.ok(toDTO(tacheComplete));
}
```

**Statuts disponibles** (StatutTache.java) :
```java
public enum StatutTache {
    A_FAIRE,     // 🔵 Nouvelle tâche
    EN_COURS,    // 🟡 Tâche en cours
    TERMINEE     // 🟢 Tâche terminée
}
```

---

### 📸 **Upload et gestion d'images**

**Upload d'une image (TacheService.java)** :

```java
public Tache uploadImage(Long tacheId, MultipartFile file) {
    Tache tache = tacheRepository.findById(tacheId)
        .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

    try {
        // ✅ 1. Créer le dossier d'upload s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);  // "./uploads/taches/"
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // ✅ 2. Valider le fichier
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // ✅ 3. Vérifier que c'est une image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Seules les images sont acceptées");
        }

        // ✅ 4. Générer un nom unique (UUID)
        String fileName = UUID.randomUUID().toString() + ".jpg";

        // ✅ 5. Sauvegarder le fichier physiquement
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // ✅ 6. Ajouter l'URL au modèle de tâche
        String imageUrl = "/uploads/taches/" + fileName;
        tache.getImageUrls().add(imageUrl);

        // ✅ 7. Sauvegarder en base de données
        tacheRepository.save(tache);

        return tacheRepository.findByIdWithColonneAndProjet(tacheId)
            .orElseThrow();

    } catch (IOException e) {
        throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage());
    }
}
```

**Dossier physique sur le serveur** :
```
backend/
└── uploads/
    └── taches/
        ├── 52ab442e-6c77-4698-9648-66c3100da22a.png
        ├── a1b2c3d4-e5f6-1234-abcd-ef5678901234.jpg
        └── ...
```

---

## 5. Modèles de données

### 🗂️ **Schéma de la base de données**

#### **Table : utilisateurs**

```sql
CREATE TABLE utilisateurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,        -- Hashé en BCrypt
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBRE', -- CHEF_PROJET, MEMBRE
    avatar VARCHAR(255),
    date_creation TIMESTAMP DEFAULT NOW()
);
```

**Entité Java** :
```java
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Jamais retourner le pwd
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBRE;

    @Column(updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public enum Role {
        CHEF_PROJET, MEMBRE
    }
}
```

---

#### **Table : projets**

```sql
CREATE TABLE projets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    date_debut DATE,
    date_fin DATE,
    proprietaire_id BIGINT NOT NULL,          -- Foreign Key
    date_creation TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (proprietaire_id) REFERENCES utilisateurs(id)
);
```

**Entité Java** :
```java
@Entity
@Table(name = "projets")
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    // ✅ RELATION : Un projet a UN propriétaire
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Utilisateur proprietaire;

    // ✅ RELATION : Un projet a PLUSIEURS colonnes
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Colonne> colonnes = new ArrayList<>();

    // ✅ RELATION : Un projet a PLUSIEURS membres (Many-to-Many)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "membres_projet",
        joinColumns = @JoinColumn(name = "projet_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private Set<Utilisateur> membres = new HashSet<>();
}
```

---

#### **Table : colonnes**

```sql
CREATE TABLE colonnes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    couleur VARCHAR(7) DEFAULT '#0052CC',     -- Couleur hexadécimale
    position INTEGER NOT NULL,
    projet_id BIGINT NOT NULL,                 -- Foreign Key
    FOREIGN KEY (projet_id) REFERENCES projets(id)
);
```

**Entité Java** :
```java
@Entity
@Table(name = "colonnes")
public class Colonne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 7)
    private String couleur = "#0052CC";

    @Column(nullable = false)
    private Integer position;

    // ✅ RELATION : Plusieurs colonnes pour 1 projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    // ✅ RELATION : Une colonne contient plusieurs tâches
    @OneToMany(mappedBy = "colonne", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Tache> taches = new ArrayList<>();
}
```

---

#### **Table : taches**

```sql
CREATE TABLE taches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    priorite VARCHAR(50) DEFAULT 'MOYENNE',  -- BASSE, MOYENNE, HAUTE, URGENTE
    statut VARCHAR(50) DEFAULT 'A_FAIRE',    -- A_FAIRE, EN_COURS, TERMINEE
    date_echeance DATE,
    position INTEGER NOT NULL,
    colonne_id BIGINT NOT NULL,               -- Foreign Key
    date_creation TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (colonne_id) REFERENCES colonnes(id)
);
```

**Entité Java** :
```java
@Entity
@Table(name = "taches")
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priorite priorite = Priorite.MOYENNE;

    @Enumerated(EnumType.STRING)
    private StatutTache statut = StatutTache.A_FAIRE;

    private LocalDate dateEcheance;

    @Column(nullable = false)
    private Integer position;

    // ✅ RELATION : Plusieurs tâches dans 1 colonne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colonne_id", nullable = false)
    private Colonne colonne;

    // ✅ RELATION : Une tâche assignée à plusieurs utilisateurs
    @ManyToMany
    @JoinTable(
        name = "tache_assignees",
        joinColumns = @JoinColumn(name = "tache_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<Utilisateur> assignes = new ArrayList<>();

    // ✅ RELATION : Une tâche a plusieurs commentaires
    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    // ✅ NOUVEAU : Stockage des URLs des images
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tache_images", joinColumns = @JoinColumn(name = "tache_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    public enum Priorite {
        BASSE, MOYENNE, HAUTE, URGENTE
    }
}
```

---

#### **Table : commentaires**

```sql
CREATE TABLE commentaires (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenu TEXT NOT NULL,
    date_creation TIMESTAMP DEFAULT NOW(),
    date_modification TIMESTAMP,
    tache_id BIGINT NOT NULL,                 -- Foreign Key
    auteur_id BIGINT NOT NULL,                -- Foreign Key
    FOREIGN KEY (tache_id) REFERENCES taches(id),
    FOREIGN KEY (auteur_id) REFERENCES utilisateurs(id)
);
```

**Entité Java** :
```java
@Entity
@Table(name = "commentaires")
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateModification;

    // ✅ RELATION : Plusieurs commentaires pour 1 tâche
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    // ✅ RELATION : Commentaire rédigé par 1 utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;
}
```

---

#### **Table de jonction : membres_projet** (Many-to-Many)

```sql
CREATE TABLE membres_projet (
    projet_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (projet_id, utilisateur_id),
    FOREIGN KEY (projet_id) REFERENCES projets(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
);
```

---

#### **Table de jonction : tache_assignees** (Many-to-Many)

```sql
CREATE TABLE tache_assignees (
    tache_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (tache_id, utilisateur_id),
    FOREIGN KEY (tache_id) REFERENCES taches(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
);
```

---

#### **Table : tache_images** (ElementCollection)

```sql
CREATE TABLE tache_images (
    tache_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (tache_id) REFERENCES taches(id)
);
```

---

### 📊 **Diagramme des relations**

```
                    ┌─────────────────────┐
                    │   UTILISATEURS      │
                    ├─────────────────────┤
                    │ id (PK)             │
                    │ nom                 │
                    │ prenom              │
                    │ email (UNIQUE)      │
                    │ mot_de_passe        │
                    │ role (ENUM)         │◄─────────────────────┐
                    │ date_creation       │                      │ CREATEUR
                    └─────────────────────┘                      │
                         ▲                                        │
                         │                                        │
                         │ ASSIGNE                        ┌───────┴──────────────┐
                         │ (Many-to-Many)                 │                      │
                         │                                │                      │
                    ┌────┴────────────────┐         ┌─────┴──────────────┐     │
                    │  TACHE_ASSIGNEES    │         │     PROJETS        │     │
                    ├─────────────────────┤         ├────────────────────┤     │
                    │ tache_id (FK)       │         │ id (PK)            │     │
                    │ utilisateur_id (FK) │         │ titre              │     │
                    └─────────────────────┘         │ description        │────► proprietaire_id (FK)
                         ▲                          │ date_debut         │
                         │                          │ date_fin           │
                         │                          │ date_creation      │
                         │                          │ proprietaire_id    │
                    ┌────┴────────────────┐         └────────────────────┘
                    │      TACHES         │              ▲
                    ├─────────────────────┤              │
                    │ id (PK)             │              │ CONTIENT
                    │ titre               │              │ (One-to-Many)
                    │ description         │              │
                    │ priorite (ENUM)     │         ┌────┴────────────────┐
                    │ statut (ENUM)       │         │   COLONNES          │
                    │ date_echeance       │         ├─────────────────────┤
                    │ position            │         │ id (PK)             │
                    │ colonne_id (FK)  ───┼─────────│ nom                 │
                    │ date_creation       │◄────────│ couleur             │
                    └─────────────────────┘         │ position            │
                         │                          │ projet_id (FK)      │
                         │                          └─────────────────────┘
                         │
                    ┌────┴────────────────┐
                    │  TACHE_IMAGES       │
                    ├─────────────────────┤
                    │ tache_id (FK)       │
                    │ image_url           │
                    └─────────────────────┘
```

---

## 6. Gestion des erreurs

### 🚨 **Codes d'erreur HTTP retournés**

| Code | Signification | Exemple |
|------|---------------|---------|
| `200` | ✅ OK | Requête réussie |
| `201` | ✅ Created | Ressource créée |
| `204` | ✅ No Content | Suppression réussie |
| `400` | ❌ Bad Request | Email invalide, données manquantes |
| `401` | ❌ Unauthorized | Token JWT manquant ou invalide |
| `403` | ❌ Forbidden | Pas d'accès (ex: ancien propriétaire accédant à son project) |
| `404` | ❌ Not Found | Ressource inexistante |
| `500` | ❌ Server Error | Erreur serveur |

### 🔍 **Exemples de réponses d'erreur**

**Erreur 401 : Token JWT invalide**

```json
{
  "message": "Token JWT invalide ou expiré",
  "status": 401,
  "timestamp": "2024-05-13T10:30:45Z"
}
```

**Erreur 400 : Email déjà utilisé**

```json
{
  "message": "Erreur: Email déjà utilisé!",
  "status": 400
}
```

**Erreur 404 : Projet non trouvé**

```json
{
  "message": "Projet non trouvé: 999",
  "status": 404,
  "timestamp": "2024-05-13T10:35:12Z"
}
```

**Erreur 403 : Accès non autorisé**

```json
{
  "message": "Vous n'êtes pas membre de ce projet",
  "status": 403
}
```

### 🛡️ **Gestion dans les Services**

**Exemple dans ProjetService.java** :

```java
private Utilisateur findUtilisateur(Long id) {
    return utilisateurRepository.findById(id)
        // ✅ Lever une exception avec code HTTP 404
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, 
            "Utilisateur non trouvé: " + id
        ));
}

private Projet findProjet(Long id) {
    return projetRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, 
            "Projet non trouvé: " + id
        ));
}
```

**Exemple dans TacheService.java** :

```java
private boolean peutAccederAuProjet(Utilisateur utilisateur, Projet projet) {
    if (projet == null || utilisateur == null || utilisateur.getId() == null) {
        return false;
    }
    
    // ✅ Vérifie que l'utilisateur est propriétaire OU membre
    if (utilisateur.equals(projet.getProprietaire())) {
        return true;
    }
    
    return projet.getMembres().contains(utilisateur);
}

// Utilisation :
if (!peutAccederAuProjet(utilisateur, colonne.getProjet())) {
    throw new AccessDeniedException("Vous n'êtes pas membre de ce projet");
}
```

---

## 7. 5 Points techniques clés

### 1️⃣ **Système d'authentification JWT (JSON Web Token)**

**Explication simple pour la soutenance** :

> "L'authentification JWT fonctionne en trois étapes :
> 
> 1. **Login** → L'utilisateur envoie email + mot de passe
> 2. **Génération** → Le serveur crée un token JWT contenant l'email de l'utilisateur, signé avec une clé secrète
> 3. **Utilisation** → À chaque requête suivante, le client envoie ce token dans le header `Authorization: Bearer <JWT>`, et le serveur le vérifie sans accéder à la base de données
> 
> **Avantage** : Sans état (stateless) → Pas de session stockée, scalable pour un déploiement en microservices."

**Code clé** (JwtUtils.java) :
```java
public String generateJwtToken(Authentication authentication) {
    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())  // Email
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))  // 24h
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Signature HMAC-SHA256
        .compact();
}
```

---

### 2️⃣ **Gestion des rôles et contrôle d'accès**

**Explication simple** :

> "Il existe deux rôles dans l'application :
> 
> - **CHEF_PROJET** → Peut voir TOUS les projets de l'application
> - **MEMBRE** → Ne voit que ses propres projets (en tant que propriétaire ou membre)
> 
> À chaque requête, le serveur vérifie le rôle de l'utilisateur et adapte les résultats retournés. Par exemple, `/api/projets/tous` retourne :
> - Tous les projets (pour CHEF_PROJET)
> - Seulement ses projets (pour MEMBRE)"

**Code clé** (ProjetController.java) :
```java
@GetMapping("/tous")
public ResponseEntity<List<ProjetDTO>> getTousMesProjets() {
    Utilisateur u = getUtilisateurConnecte();

    if (u.getRole() == Utilisateur.Role.CHEF_PROJET) {
        return ResponseEntity.ok(projetService.obtenirTousLesProjets());
    }
    return ResponseEntity.ok(projetService.obtenirProjetsByUtilisateur(u.getId()));
}
```

---

### 3️⃣ **Relations Many-to-Many et gestion des membres**

**Explication simple** :

> "Un projet peut avoir plusieurs membres, et un utilisateur peut être membre de plusieurs projets. C'est une relation Many-to-Many.
> 
> En base de données, cela crée une **table de jonction** (`membres_projet`) qui stocke les paires (projet_id, utilisateur_id).
> 
> **Quand on ajoute un membre** :
> 1. On cherche le projet
> 2. On cherche l'utilisateur
> 3. On ajoute une ligne dans la table `membres_projet`
> 4. On sauvegarde"

**Code clé** (Projet.java) :
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "membres_projet",
    joinColumns = @JoinColumn(name = "projet_id"),
    inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
)
private Set<Utilisateur> membres = new HashSet<>();  // ← HashSet pour éviter les doublons
```

---

### 4️⃣ **Drag & Drop implémenté en backend**

**Explication simple** :

> "Le drag & drop est implémenté via des appels API simples :
> 
> **Déplacer une tâche d'une colonne à une autre** :
> - Frontend envoie : `PATCH /api/taches/5/deplacer?colonneId=8`
> - Backend met à jour `tache.colonne = nouvelleColonne` et sauvegarde
> 
> **Réorganiser l'ordre des tâches dans une colonne** :
> - Frontend envoie : `POST /api/taches/3/reorganiser` avec `[15, 12, 20]` (IDs dans le nouvel ordre)
> - Backend met à jour la position de chaque tâche : tâche 15 → position 0, tâche 12 → position 1, etc."

**Code clé** (TacheService.java) :
```java
public Tache deplacerTache(Long tacheId, Long nouvelleColonneId) {
    Tache tache = tacheRepository.findById(tacheId).orElseThrow();
    Colonne nouvelleColonne = colonneRepository.findById(nouvelleColonneId).orElseThrow();
    
    tache.setColonne(nouvelleColonne);  // ← Change de colonne
    tacheRepository.save(tache);
    
    return tacheRepository.findByIdWithColonneAndProjet(tacheId).orElseThrow();
}

public void reorganiserTaches(Long colonneId, List<Long> tacheIds) {
    for (int i = 0; i < tacheIds.size(); i++) {
        Tache tache = tacheRepository.findById(tacheIds.get(i)).orElseThrow();
        tache.setPosition(i);  // ← Change la position
        tacheRepository.save(tache);
    }
}
```

---

### 5️⃣ **Upload et gestion d'images avec UUID**

**Explication simple** :

> "Pour les uploads d'images, on ne peut pas simplement sauvegarder le nom du fichier envoyé par le client (risque de sécurité). 
> 
> **Processus** :
> 1. Le frontend envoie une image (ex: `photo.png`)
> 2. Le backend génère un **UUID** unique (ex: `52ab442e-6c77-4698-9648-66c3100da22a.png`)
> 3. L'image est sauvegardée physiquement dans le dossier `./uploads/taches/`
> 4. L'URL est stockée en base de données : `/uploads/taches/52ab442e-6c77-4698-9648-66c3100da22a.png`
> 5. Le frontend peut accéder à l'image via cette URL
> 
> **Sécurité** : Les noms UUID empêchent les collisions et les attaques par énumération."

**Code clé** (TacheService.java) :
```java
public Tache uploadImage(Long tacheId, MultipartFile file) {
    // ... validations ...

    // ✅ Générer un UUID unique
    String fileName = UUID.randomUUID().toString() + ".jpg";

    // ✅ Sauvegarder physiquement
    Path filePath = uploadPath.resolve(fileName);
    Files.copy(file.getInputStream(), filePath);

    // ✅ Ajouter l'URL en base de données
    String imageUrl = "/uploads/taches/" + fileName;
    tache.getImageUrls().add(imageUrl);

    tacheRepository.save(tache);
    return tache;
}
```

**Base de données** (table `tache_images`) :
```
┌────────┬──────────────────────────────────────────┐
│tache_id│          image_url                       │
├────────┼──────────────────────────────────────────┤
│   5    │ /uploads/taches/52ab442e-6c77-4698.png  │
│   5    │ /uploads/taches/a1b2c3d4-e5f6-1234.jpg  │
└────────┴──────────────────────────────────────────┘
```

---

## 📚 Informations supplémentaires

### 🔗 **Frontend - Backend Connection**

**Comment le frontend utilise le backend** :

1. **ApiClient.js** (Axios) :
```javascript
const api = axios.create({
    baseURL: 'http://localhost:8080/api'
});

// ✅ Intercepteur : injecter le JWT en header
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

2. **Exemples d'appels** :
```javascript
// Login
const response = await api.post('/auth/login', { email, motDePasse });
localStorage.setItem('token', response.data.token);

// Créer un projet
await api.post('/projets', { titre, description });

// Déplacer une tâche (Drag & Drop)
await api.patch(`/taches/5/deplacer?colonneId=8`);
```

### 🚀 **Lancement du serveur**

```bash
# Option 1 : Maven (développement)
mvn spring-boot:run -DskipTests

# Option 2 : JAR (production)
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### 🔍 **Accès à Swagger UI**

```
http://localhost:8080/swagger-ui.html
```

Permet de tester toutes les API directement dans le navigateur !

### 📊 **Statistiques du projet**

- **Contrôleurs** : 5 (Auth, Projet, Colonne, Tache, Utilisateur)
- **Services** : 4 (ProjetService, ColonneService, TacheService, UtilisateurService)
- **Entités JPA** : 6 (Utilisateur, Projet, Colonne, Tache, Commentaire, Statut)
- **Endpoints API** : ~40 routes REST
- **Rôles** : 2 (CHEF_PROJET, MEMBRE)
- **Statuts de tâches** : 3 (A_FAIRE, EN_COURS, TERMINEE)
- **Priorités de tâches** : 4 (BASSE, MOYENNE, HAUTE, URGENTE)

---

## ✅ Points à retenir pour la soutenance

1. **Spring Boot** → Framework HTTP pour les APIs REST
2. **JWT Token** → Authentification sans session
3. **PostgreSQL** → Persistance des données
4. **JPA/Hibernate** → Mapping Objet-Relationnel
5. **Rôles** → Contrôle d'accès (CHEF_PROJET vs MEMBRE)
6. **Many-to-Many** → Relation entre Utilisateur et Projet
7. **Drag & Drop** → Implémenté via réorganisation de positions
8. **Upload d'image** → UUID pour sécurité + stockage physique
9. **CORS** → Permet au frontend React (localhost:5173) d'accéder au backend
10. **Gestion d'erreurs** → Codes HTTP standards (400, 401, 404, 500)

---

## 🎬 **Script de démonstration pour la soutenance**

```bash
# 1. Vérifier que le serveur tourne
curl http://localhost:8080/swagger-ui.html

# 2. Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"Dupont", "prenom":"Jean", "email":"jean@test.com", "motDePasse":"123456"}'

# 3. Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jean@test.com", "motDePasse":"123456"}' | jq -r '.token')

# 4. Créer un projet
curl -X POST http://localhost:8080/api/projets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titre":"Mon Projet", "description":"Description"}'

# 5. Récupérer tous les projets
curl -X GET http://localhost:8080/api/projets \
  -H "Authorization: Bearer $TOKEN"
```

---

**Bonne soutenance ! 🎓**


