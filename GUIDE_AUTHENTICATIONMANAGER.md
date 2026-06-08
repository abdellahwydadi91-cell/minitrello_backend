# 🔐 AuthenticationManager - Guide Complet

> **Où trouver AuthenticationManager et comment il fonctionne**

---

## 📍 OÙ TROUVER AuthenticationManager

### Localisation dans le projet

```
minitrello/
└── src/main/java/org/minitrello/backend/
    ├── 📂 controller/
    │   └── AuthController.java          ← UTILISE AuthenticationManager (ligne 26)
    │
    └── 📂 security/
        └── SecurityConfig.java          ← CRÉE AuthenticationManager (ligne 49-51)
```

---

## 🎯 OÙ EST CRÉÉ AuthenticationManager

### Fichier: `SecurityConfig.java`

```java
// Location: src/main/java/.../security/SecurityConfig.java

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // ✅ CRÉATION D'AuthenticationManager (ligne 49-51)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

**Explication** :
- `@Bean` → Spring crée une instance unique
- `AuthenticationConfiguration` → Configuration automatique de Spring Security
- `getAuthenticationManager()` → Retourne le manager créé

---

## 🔗 OÙ EST UTILISÉ AuthenticationManager

### Fichier: `AuthController.java`

```java
// Location: src/main/java/.../controller/AuthController.java

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // ✅ INJECTION D'AuthenticationManager (ligne 26)
    @Autowired
    AuthenticationManager authenticationManager;

    // ✅ UTILISATION lors du login (ligne 34-36)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // L'AuthenticationManager valide les identifiants
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), 
                loginRequest.getMotDePasse()
            )
        );
        
        // Si on arrive ici, l'authentification a réussi
        // ...
    }
}
```

---

## 🔍 FLUX COMPLET D'UTILISATION

```
1. Frontend envoie:
   POST /api/auth/login
   { "email": "user@ex.com", "motDePasse": "password123" }

                         ↓

2. AuthController.authenticateUser() reçoit

                         ↓

3. Crée UsernamePasswordAuthenticationToken:
   ┌─────────────────────────────────────────┐
   │ UsernamePasswordAuthenticationToken      │
   ├─────────────────────────────────────────┤
   │ principal   : "user@ex.com"             │
   │ credentials : "password123" (plain)     │
   │ authenticated : false (pas encore)      │
   └─────────────────────────────────────────┘

                         ↓

4. Appelle authenticationManager.authenticate(token):
   
   authenticationManager.authenticate(
       new UsernamePasswordAuthenticationToken(
           "user@ex.com",      ← email/username
           "password123"       ← mot de passe en clair
       )
   )

                         ↓

5. AuthenticationManager délègue à DaoAuthenticationProvider:
   
   DaoAuthenticationProvider:
   ├─ Appelle UserDetailsService.loadUserByUsername("user@ex.com")
   │  └─ Charge l'utilisateur depuis la BD
   │
   ├─ Appelle PasswordEncoder.matches(plainPassword, hashedPassword)
   │  └─ Compare "password123" avec le hash BCrypt en BD
   │
   └─ Si OK → Crée nouveau token AUTHENTIFIÉ

                         ↓

6. Retourne Authentication objet:
   ┌─────────────────────────────────────────┐
   │ Authentication (AUTHENTIFIÉ)            │
   ├─────────────────────────────────────────┤
   │ principal   : UserDetails (user)        │
   │ credentials : null (effacé pour sécurité)
   │ authenticated : true ✅                 │
   │ authorities : [ROLE_MEMBRE]             │
   └─────────────────────────────────────────┘

                         ↓

7. AuthController stocke dans SecurityContextHolder:
   SecurityContextHolder.getContext().setAuthentication(authentication)

                         ↓

8. Génère JWT et retourne au frontend

```

---

## 📊 DIAGRAMME: RÔLE D'AuthenticationManager

```
┌─────────────────────────────────────────────────────────────┐
│                    AuthenticationManager                     │
│          (Gestionnaire central d'authentification)            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  INPUT:                      PROCESSING:        OUTPUT:      │
│  ┌──────────────┐            ┌─────────────┐   ┌──────────┐ │
│  │ Email        │     →      │ Délègue à   │   │ Rôles    │ │
│  │ Mot de passe │            │ Providers   │   │ Perms    │ │
│  └──────────────┘            └─────────────┘   └──────────┘ │
│                                     ↓                        │
│                          DaoAuthenticationProvider           │
│                                     ├─ Charge user           │
│                                     ├─ Compare password      │
│                                     └─ Charge authorities    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 CODE COMPLET D'AUTHENTIFICATION

### SecurityConfig.java (Création)

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ✅ 1. Password Encoder (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 2. Authentication Provider (combine User + Password)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);        // Où chercher l'utilisateur?
        authProvider.setPasswordEncoder(passwordEncoder());             // Comment vérifier le password?
        return authProvider;
    }

    // ✅ 3. Authentication Manager (utilise le provider)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ✅ 4. Enregistrer le provider
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());  // ← Enregistrer provider
        http.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### AuthController.java (Utilisation)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;  // ← Injecté depuis @Bean

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // ✅ Utiliser l'AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),         // username
                        loginRequest.getMotDePasse()      // password en clair
                )
        );

        // À ce stade, authentication.authenticated = true ✅
        
        // Stocker dans le contexte de sécurité
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Générer JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Charger l'utilisateur complet
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // Retourner réponse avec JWT
        return ResponseEntity.ok(new JwtResponse(jwt, utilisateur.getId(), utilisateur.getEmail(), utilisateur.getRole().name()));
    }
}
```

---

## 🔄 CHAÎNE DE PROVIDERS

```
AuthenticationManager
    ↓
├─ Essaie Provider 1: DaoAuthenticationProvider ✅ (configuré)
│
├─ Essaie Provider 2: ...
│
└─ Essaie Provider 3: ...
```

Dans notre cas, nous avons enregistré `DaoAuthenticationProvider` donc c'est celui-ci qui est utilisé.

---

## 🛡️ CLASSE AuthenticationManager (Spring Security)

```java
// Location: org.springframework.security.authentication.AuthenticationManager
// C'est une INTERFACE Spring Security

public interface AuthenticationManager {
    
    /**
     * Authentifie le token fourni, retournant l'authentification 
     * complète si réussie
     */
    Authentication authenticate(Authentication authentication)
            throws AuthenticationException;
}
```

**Implémentation concrète dans notre cas** :
- `ProviderManager` (implémentation par défaut de Spring)

---

## 📝 RÉSUMÉ: AuthenticationManager

| Aspect | Détail |
|--------|--------|
| **Classe** | `org.springframework.security.authentication.AuthenticationManager` |
| **Type** | Interface Spring Security |
| **Création** | Dans `SecurityConfig.java` avec `@Bean` |
| **Injection** | `@Autowired` dans `AuthController.java` |
| **Fonction** | Valider email + mot de passe, retourner `Authentication` |
| **Utilés dans** | `/api/auth/login` endpoint |
| **Dépend de** | `DaoAuthenticationProvider` + `UserDetailsService` + `PasswordEncoder` |

---

## 🔍 COMME TROUVER AuthenticationManager RAPIDEMENT

### Via IDE (IntelliJ)

```
1. Ctrl+Shift+F (Find in Path)
2. Taper: "AuthenticationManager"
3. Résultats:
   ✓ SecurityConfig.java ligne 49 (création)
   ✓ AuthController.java ligne 26 (injection)
```

### Via Terminal (Grep)

```bash
grep -r "AuthenticationManager" src/

# Résultats:
# src/.../controller/AuthController.java:10:import AuthenticationManager;
# src/.../security/SecurityConfig.java:7:import AuthenticationManager;
# src/.../security/SecurityConfig.java:49:public AuthenticationManager authenticationManager(...
# src/.../controller/AuthController.java:26:AuthenticationManager authenticationManager;
```

---

## 💡 POINTS CLÉS À RETENIR

✅ **Créé dans** : `SecurityConfig.java`
```java
@Bean
public AuthenticationManager authenticationManager(...) {
    return authConfig.getAuthenticationManager();
}
```

✅ **Injecté dans** : `AuthController.java`
```java
@Autowired
AuthenticationManager authenticationManager;
```

✅ **Utilisé pour** : Vérifier email + mot de passe au login
```java
Authentication auth = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(email, password)
);
```

✅ **Retourne** : Objet `Authentication` authentifié
```java
// Si succès: auth.isAuthenticated() == true ✅
// Si échec: Lève AuthenticationException ❌
```

---

## 🎯 QUESTION POSSIBLE DU JURY

**Q: Qu'est-ce qu'AuthenticationManager?**

**R**: "C'est un composant Spring Security qui authentifie les utilisateurs. Au login, on lui envoie le email et mot de passe, il les valide contre la BD, et retourne un objet Authentication contenant les autorités (rôles) de l'utilisateur. C'est ensuite utilisé pour générer le JWT."

**Q: Où est-il créé?**

**R**: "Dans la classe `SecurityConfig.java`, c'est un `@Bean` qui utilise `AuthenticationConfiguration` pour créer une instance unique d'`AuthenticationManager`."

**Q: Comment il fonctionne?**

**R**: "Il délègue à `DaoAuthenticationProvider` qui:
1. Charge l'utilisateur via `UserDetailsService`
2. Compare le mot de passe fourni avec le hash BCrypt en BD
3. Si valide, charge les rôles (autorités) de l'utilisateur
4. Retourne un nouvel `Authentication` objet authentifié"

---

## 📚 DOCUMENTATION OFFICIELLE

- Classe: `org.springframework.security.authentication.AuthenticationManager`
- Spring Security: https://spring.io/projects/spring-security
- JavaDoc: `AuthenticationManager` interface

---

**Maintenant vous savez exactement où trouver et comment utiliser AuthenticationManager! ✅**


