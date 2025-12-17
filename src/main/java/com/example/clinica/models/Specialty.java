package com.example.clinica.models;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specialty_id")
    private Integer specialtyId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public Specialty() {
    }

    public Specialty(Integer specialtyId, String name, String description) {
        this.specialtyId = specialtyId;
        this.name = name;
        this.description = description;
    }

    public Integer getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Integer specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
