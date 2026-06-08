package org.minitrello.backend.repository;

import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    @Query("SELECT DISTINCT p FROM Projet p " +
            "LEFT JOIN FETCH p.proprietaire " +
            "LEFT JOIN FETCH p.colonnes c " +
            "LEFT JOIN FETCH p.membres " +
            "WHERE p.proprietaire = :proprietaire")
    List<Projet> findByProprietaire(@Param("proprietaire") Utilisateur proprietaire);

    @Query("SELECT DISTINCT p FROM Projet p " +
            "LEFT JOIN FETCH p.proprietaire " +
            "LEFT JOIN FETCH p.colonnes c " +
            "LEFT JOIN FETCH p.membres " +
            "WHERE p.proprietaire = :u OR :u MEMBER OF p.membres")
    List<Projet> findProjetsByUtilisateur(@Param("u") Utilisateur utilisateur);

    @Query("SELECT DISTINCT p FROM Projet p " +
            "LEFT JOIN FETCH p.proprietaire " +
            "LEFT JOIN FETCH p.colonnes c " +
            "LEFT JOIN FETCH p.membres " +
            "WHERE LOWER(p.titre) LIKE LOWER(CONCAT('%', :titre, '%'))")
    List<Projet> findByTitreContainingIgnoreCase(@Param("titre") String titre);

    @Query("SELECT DISTINCT p FROM Projet p " +
            "LEFT JOIN FETCH p.proprietaire " +
            "LEFT JOIN FETCH p.colonnes c " +
            "LEFT JOIN FETCH p.membres " )
    List<Projet> findAllWithDetails();

    @Query("SELECT DISTINCT p FROM Projet p " +
            "LEFT JOIN FETCH p.proprietaire " +
            "LEFT JOIN FETCH p.colonnes c " +
            "LEFT JOIN FETCH p.membres " +
            "WHERE p.id = :id")
    Optional<Projet> findByIdWithDetails(@Param("id") Long id);
}