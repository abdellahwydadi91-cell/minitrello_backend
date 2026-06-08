package org.minitrello.backend.repository;

import org.minitrello.backend.entity.Commentaire;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findByTacheOrderByDateCreationDesc(Tache tache);

    List<Commentaire> findByAuteur(Utilisateur auteur);

    long countByTache(Tache tache);
}

