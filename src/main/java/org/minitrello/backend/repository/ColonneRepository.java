package org.minitrello.backend.repository;

import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColonneRepository extends JpaRepository<Colonne, Long> {

    List<Colonne> findByProjetOrderByPositionAsc(Projet projet);

    Optional<Colonne> findByIdAndProjet(Long id, Projet projet);

    boolean existsByNomAndProjet(String nom, Projet projet);

    // ✅ AJOUTER cette méthode
    @Query("SELECT c FROM Colonne c LEFT JOIN FETCH c.projet p LEFT JOIN FETCH p.proprietaire LEFT JOIN FETCH p.membres WHERE c.id = :id")
    Optional<Colonne> findByIdWithProjet(@Param("id") Long id);
}