package org.minitrello.backend.repository;

import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByColonneOrderByPositionAsc(Colonne colonne);

    Optional<Tache> findByIdAndColonne(Long id, Colonne colonne);

    List<Tache> findByDateEcheanceBeforeAndDateEcheanceIsNotNull(LocalDate date);

    @Query("SELECT t FROM Tache t WHERE :utilisateur MEMBER OF t.assignes")
    List<Tache> findTachesByAssigne(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT DISTINCT t FROM Tache t " +
            "LEFT JOIN FETCH t.colonne c " +
            "LEFT JOIN FETCH c.projet " +
            "LEFT JOIN FETCH t.assignes " +
            "WHERE :utilisateur MEMBER OF t.assignes")
    List<Tache> findTachesAssigneesAvecRelations(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT DISTINCT t FROM Tache t " +
            "LEFT JOIN FETCH t.assignes " +
            "WHERE t.colonne = :colonne " +
            "ORDER BY t.position ASC")
    List<Tache> findByColonneWithAssignesOrderByPositionAsc(@Param("colonne") Colonne colonne);

    List<Tache> findByTitreContainingIgnoreCase(String titre);

    // ✅ CORRECTION : Ajouter LEFT JOIN FETCH t.imageUrls
    @Query("SELECT t FROM Tache t " +
            "LEFT JOIN FETCH t.colonne c " +
            "LEFT JOIN FETCH c.projet " +
            "LEFT JOIN FETCH t.assignes " +
            "LEFT JOIN FETCH t.imageUrls " +  // ← AJOUTER CETTE LIGNE
            "WHERE t.id = :id")
    Optional<Tache> findByIdWithColonneAndProjet(@Param("id") Long id);
}