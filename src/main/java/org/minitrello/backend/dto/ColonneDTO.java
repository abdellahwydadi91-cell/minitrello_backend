package org.minitrello.backend.dto;

import lombok.Data;

@Data
public class ColonneDTO {
    private Long id;
    private String nom;
    private String couleur;
    private Integer position;
    private Long projetId;  // juste l'id du projet, pas l'objet entier
}