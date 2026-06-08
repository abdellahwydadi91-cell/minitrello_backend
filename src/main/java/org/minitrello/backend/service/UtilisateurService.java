package org.minitrello.backend.service;

import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Créer un nouvel utilisateur
     */
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public Optional<Utilisateur> obtenirUtilisateurParId(Long id) {
        return utilisateurRepository.findById(id);
    }

    /**
     * Récupérer un utilisateur par email
     */
    public Optional<Utilisateur> obtenirUtilisateurParEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    /**
     * Mettre à jour un utilisateur
     */
    public Utilisateur mettreAJourUtilisateur(Long id, Utilisateur utilisateurMiseAJour) {
        return utilisateurRepository.findById(id).map(utilisateur -> {

            // Mise à jour nom et prénom
            utilisateur.setNom(utilisateurMiseAJour.getNom());
            utilisateur.setPrenom(utilisateurMiseAJour.getPrenom());
            utilisateur.setAvatar(utilisateurMiseAJour.getAvatar());

            // Mise à jour de l'email si changé
            if (utilisateurMiseAJour.getEmail() != null &&
                    !utilisateurMiseAJour.getEmail().equals(utilisateur.getEmail())) {
                if (utilisateurRepository.existsByEmail(utilisateurMiseAJour.getEmail())) {
                    throw new RuntimeException("Email déjà utilisé par un autre compte");
                }
                utilisateur.setEmail(utilisateurMiseAJour.getEmail());
            }

            // Mise à jour du mot de passe si fourni
            if (utilisateurMiseAJour.getMotDePasse() != null &&
                    !utilisateurMiseAJour.getMotDePasse().isBlank()) {
                utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurMiseAJour.getMotDePasse()));
            }

            return utilisateurRepository.save(utilisateur);
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
    }

    /**
     * Supprimer un utilisateur
     */
    public void supprimerUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    /**
     * Récupérer tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    /**
     * Changer le rôle d'un utilisateur (Admin only)
     */
    public Utilisateur changerRole(Long id, Utilisateur.Role nouveauRole) {
        return utilisateurRepository.findById(id).map(utilisateur -> {
            utilisateur.setRole(nouveauRole);
            return utilisateurRepository.save(utilisateur);
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
    }

    /**
     * Changer le mot de passe
     */
    public void changerMotDePasse(Long id, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));

        if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }
}