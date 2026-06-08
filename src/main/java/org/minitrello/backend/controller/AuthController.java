package org.minitrello.backend.controller;

import org.minitrello.backend.dto.JwtResponse;
import org.minitrello.backend.dto.LoginRequest;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.minitrello.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        return ResponseEntity.ok(new JwtResponse(jwt,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            return ResponseEntity.badRequest().body("Erreur: Email déjà utilisé!");
        }

        // Créer un nouvel utilisateur
        Utilisateur newUtilisateur = new Utilisateur();
        newUtilisateur.setNom(utilisateur.getNom());
        newUtilisateur.setPrenom(utilisateur.getPrenom());
        newUtilisateur.setEmail(utilisateur.getEmail());
        newUtilisateur.setMotDePasse(encoder.encode(utilisateur.getMotDePasse()));
        newUtilisateur.setRole(utilisateur.getRole() != null ? utilisateur.getRole() : Utilisateur.Role.MEMBRE);

        utilisateurRepository.save(newUtilisateur);

        return ResponseEntity.ok("Utilisateur enregistré avec succès!");
    }
}
