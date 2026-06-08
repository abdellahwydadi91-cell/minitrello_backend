package org.minitrello.backend.service;

import org.minitrello.backend.entity.Commentaire;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.CommentaireRepository;
import org.minitrello.backend.repository.TacheRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentaireService {

    @Autowired
    private CommentaireRepository commentaireRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Créer un nouveau commentaire
     */
    public Commentaire creerCommentaire(Commentaire commentaire, Long tacheId, Long auteurId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        Utilisateur auteur = utilisateurRepository.findById(auteurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        commentaire.setTache(tache);
        commentaire.setAuteur(auteur);
        commentaire.setDateCreation(LocalDateTime.now());

        return commentaireRepository.save(commentaire);
    }

    /**
     * Récupérer un commentaire par ID
     */
    @Transactional(readOnly = true)
    public Optional<Commentaire> obtenirCommentaireParId(Long id) {
        return commentaireRepository.findById(id);
    }

    /**
     * Mettre à jour un commentaire
     */
    public Commentaire mettreAJourCommentaire(Long id, Commentaire commentaireMiseAJour) {
        return commentaireRepository.findById(id).map(commentaire -> {
            commentaire.setContenu(commentaireMiseAJour.getContenu());
            commentaire.setDateModification(LocalDateTime.now());
            return commentaireRepository.save(commentaire);
        }).orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
    }

    /**
     * Supprimer un commentaire
     */
    public void supprimerCommentaire(Long id) {
        commentaireRepository.deleteById(id);
    }

    /**
     * Récupérer tous les commentaires d'une tâche
     */
    @Transactional(readOnly = true)
    public List<Commentaire> obtenirCommentairesParTache(Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        return commentaireRepository.findByTacheOrderByDateCreationDesc(tache);
    }

    /**
     * Récupérer tous les commentaires d'un auteur
     */
    @Transactional(readOnly = true)
    public List<Commentaire> obtenirCommentairesParAuteur(Long auteurId) {
        Utilisateur auteur = utilisateurRepository.findById(auteurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return commentaireRepository.findByAuteur(auteur);
    }

    /**
     * Compter les commentaires d'une tâche
     */
    @Transactional(readOnly = true)
    public long compterCommentairesDeTache(Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        return commentaireRepository.countByTache(tache);
    }

    /**
     * Vérifier si un utilisateur peut modifier un commentaire
     */
    @Transactional(readOnly = true)
    public boolean verifierProprietaire(Long commentaireId, Long utilisateurId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        return commentaire.getAuteur().getId().equals(utilisateurId);
    }
}

