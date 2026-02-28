package com.PlaNova.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chambre {
    private int idChambre;
    private String typeChambre;
    private double prixChambre;
    private String statutChambre;
    private String equipement; // Stocké comme String avec séparateur
    private int idHotel;
    private String nomHotel;
    private int capacite;
    private String description;

    // Liste d'équipements (pour faciliter la manipulation)
    private List<String> equipementList;

    // Constructeurs
    public Chambre() {
        this.equipementList = new ArrayList<>();
    }

    public Chambre(int idChambre, String typeChambre, double prixChambre,
                   String statutChambre, String equipement, int idHotel,
                   String nomHotel, int capacite, String description) {
        this.idChambre = idChambre;
        this.typeChambre = typeChambre;
        this.prixChambre = prixChambre;
        this.statutChambre = statutChambre;
        this.equipement = equipement;
        this.idHotel = idHotel;
        this.nomHotel = nomHotel;
        this.capacite = capacite;
        this.description = description;

        // Initialiser la liste d'équipements à partir de la chaîne
        this.equipementList = new ArrayList<>();
        if (equipement != null && !equipement.isEmpty()) {
            String[] items = equipement.split(",");
            for (String item : items) {
                if (item != null && !item.trim().isEmpty()) {
                    this.equipementList.add(item.trim());
                }
            }
        }
    }

    // Getters et Setters
    public int getIdChambre() {
        return idChambre;
    }

    public void setIdChambre(int idChambre) {
        this.idChambre = idChambre;
    }

    public String getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(String typeChambre) {
        this.typeChambre = typeChambre;
    }

    public double getPrixChambre() {
        return prixChambre;
    }

    public void setPrixChambre(double prixChambre) {
        this.prixChambre = prixChambre;
    }

    public String getStatutChambre() {
        return statutChambre;
    }

    public void setStatutChambre(String statutChambre) {
        this.statutChambre = statutChambre;
    }

    public String getEquipement() {
        return equipement;
    }

    public void setEquipement(String equipement) {
        this.equipement = equipement;
        // Mettre à jour la liste quand on change la chaîne
        updateEquipementList();
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Méthodes pour gérer la liste d'équipements
    public List<String> getEquipementList() {
        if (equipementList == null) {
            equipementList = new ArrayList<>();
        }
        return equipementList;
    }

    public void setEquipementList(List<String> equipementList) {
        this.equipementList = equipementList;
        // Mettre à jour la chaîne quand on change la liste
        updateEquipementString();
    }

    // Mettre à jour la liste à partir de la chaîne
    private void updateEquipementList() {
        if (equipementList == null) {
            equipementList = new ArrayList<>();
        } else {
            equipementList.clear();
        }

        if (equipement != null && !equipement.isEmpty()) {
            String[] items = equipement.split(",");
            for (String item : items) {
                if (item != null && !item.trim().isEmpty()) {
                    equipementList.add(item.trim());
                }
            }
        }
    }

    // Mettre à jour la chaîne à partir de la liste
    private void updateEquipementString() {
        if (equipementList != null && !equipementList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < equipementList.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(equipementList.get(i));
            }
            this.equipement = sb.toString();
        } else {
            this.equipement = "";
        }
    }

    // Méthode pour ajouter un équipement
    public void addEquipement(String equipement) {
        if (equipementList == null) {
            equipementList = new ArrayList<>();
        }
        if (equipement != null && !equipement.trim().isEmpty() && !equipementList.contains(equipement.trim())) {
            equipementList.add(equipement.trim());
            updateEquipementString();
        }
    }

    // Méthode pour supprimer un équipement
    public void removeEquipement(String equipement) {
        if (equipementList != null) {
            equipementList.remove(equipement);
            updateEquipementString();
        }
    }

    // Méthode pour obtenir uniquement les équipements sélectionnés (cochés)
    public List<String> getEquipementsSelectionnes() {
        return getEquipementList(); // Tous les équipements dans la liste sont considérés comme sélectionnés
    }

    @Override
    public String toString() {
        return "Chambre{" +
                "idChambre=" + idChambre +
                ", typeChambre='" + typeChambre + '\'' +
                ", prixChambre=" + prixChambre +
                ", statutChambre='" + statutChambre + '\'' +
                ", equipement='" + equipement + '\'' +
                ", idHotel=" + idHotel +
                ", nomHotel='" + nomHotel + '\'' +
                ", capacite=" + capacite +
                ", description='" + description + '\'' +
                '}';
    }
}