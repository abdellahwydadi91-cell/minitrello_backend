package org.minitrello.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjetDTO {
    private Long id;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDateTime dateCreation;
    private Long proprietaireId;
    private String proprietaireNom;
    private String proprietairePrenom;
    private List<Long> membreIds;

    // Liste complète des membres (nécessaire pour le modal membres)
    private List<MembreDTO> membres;

    private String role;
    private String statut;

    @Data
    public static class MembreDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }
}