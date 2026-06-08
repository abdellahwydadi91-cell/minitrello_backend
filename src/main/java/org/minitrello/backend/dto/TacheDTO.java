package org.minitrello.backend.dto;

import lombok.Data;
import org.minitrello.backend.entity.StatutTache;
import org.minitrello.backend.entity.Tache.Priorite;
import java.time.LocalDate;
import java.util.List;

@Data
public class TacheDTO {
    private Long id;
    private String titre;
    private String description;
    private Priorite priorite;
    private StatutTache statut;
    private LocalDate dateEcheance;
    private Integer position;
    private Long colonneId;

    // Ajoutés pour MesTaches.jsx
    private Long projetId;
    private String projetTitre;
    private String colonneTitre;
    private Long assigneId;

    // Ajoutés pour correspondre au toDTO() de TacheController
    private List<Long> assigneIds;
    private List<String> assigneNoms;

    // NOUVEAU : Liste des URLs des images
    private List<String> imageUrls;
}