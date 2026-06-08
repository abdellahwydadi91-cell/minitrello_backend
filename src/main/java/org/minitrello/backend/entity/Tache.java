package org.minitrello.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taches")
@Data
@NoArgsConstructor
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

    @Column(updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colonne_id", nullable = false)
    private Colonne colonne;

    // Membres assignés à cette tâche
    @ManyToMany
    @JoinTable(
            name = "tache_assignees",
            joinColumns = @JoinColumn(name = "tache_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<Utilisateur> assignes = new ArrayList<>();

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    // NOUVEAU : Liste des URLs des images - CORRIGÉ AVEC EAGER FETCHING
    @ElementCollection(fetch = FetchType.EAGER)  // ← AJOUT DE fetch = FetchType.EAGER
    @CollectionTable(name = "tache_images", joinColumns = @JoinColumn(name = "tache_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    public enum Priorite {
        BASSE, MOYENNE, HAUTE, URGENTE
    }
}