package org.minitrello.backend.service;

import org.minitrello.backend.dto.ColonneDTO;
import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.repository.ColonneRepository;
import org.minitrello.backend.repository.ProjetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ColonneService {

    @Autowired
    private ColonneRepository colonneRepository;

    @Autowired
    private ProjetRepository projetRepository;

    // ✅ Méthode utilitaire : Colonne → ColonneDTO
    private ColonneDTO toDTO(Colonne colonne) {
        ColonneDTO dto = new ColonneDTO();
        dto.setId(colonne.getId());
        dto.setNom(colonne.getNom());
        dto.setCouleur(colonne.getCouleur());
        dto.setPosition(colonne.getPosition());
        dto.setProjetId(colonne.getProjet().getId());
        return dto;
    }

    /**
     * Créer une nouvelle colonne
     */
    public ColonneDTO creerColonne(Colonne colonne, Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (colonneRepository.existsByNomAndProjet(colonne.getNom(), projet)) {
            throw new RuntimeException("Une colonne avec ce nom existe déjà dans ce projet");
        }

        colonne.setProjet(projet);

        List<Colonne> colonnesExistantes = colonneRepository.findByProjetOrderByPositionAsc(projet);
        colonne.setPosition(colonnesExistantes.size());

        return toDTO(colonneRepository.save(colonne));
    }

    /**
     * Récupérer une colonne par ID
     */
    @Transactional(readOnly = true)
    public Optional<ColonneDTO> obtenirColonneParId(Long id) {
        return colonneRepository.findById(id).map(this::toDTO);
    }

    /**
     * Mettre à jour une colonne
     */
    public ColonneDTO mettreAJourColonne(Long id, Colonne colonneMiseAJour) {
        return colonneRepository.findById(id).map(colonne -> {
            colonne.setNom(colonneMiseAJour.getNom());
            colonne.setCouleur(colonneMiseAJour.getCouleur());
            return toDTO(colonneRepository.save(colonne));
        }).orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
    }

    /**
     * Supprimer une colonne
     */
    public void supprimerColonne(Long id) {
        colonneRepository.deleteById(id);
    }

    /**
     * Récupérer toutes les colonnes d'un projet
     */
    @Transactional(readOnly = true)
    public List<ColonneDTO> obtenirColonnesParProjet(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return colonneRepository.findByProjetOrderByPositionAsc(projet)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une colonne d'un projet spécifique
     */
    @Transactional(readOnly = true)
    public Optional<ColonneDTO> obtenirColonneParIdEtProjet(Long colonneId, Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return colonneRepository.findByIdAndProjet(colonneId, projet).map(this::toDTO);
    }

    /**
     * Réorganiser les colonnes
     */
    public void reorganiserColonnes(Long projetId, List<Long> colonneIds) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        for (int i = 0; i < colonneIds.size(); i++) {
            Long colonneId = colonneIds.get(i);
            Colonne colonne = colonneRepository.findById(colonneId)
                    .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));

            if (!colonne.getProjet().equals(projet)) {
                throw new RuntimeException("La colonne n'appartient pas à ce projet");
            }

            colonne.setPosition(i);
            colonneRepository.save(colonne);
        }
    }

    /**
     * Vérifier si une colonne appartient à un projet
     */
    @Transactional(readOnly = true)
    public boolean verifierAppartenance(Long colonneId, Long projetId) {
        Colonne colonne = colonneRepository.findById(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        return colonne.getProjet().equals(projet);
    }
}