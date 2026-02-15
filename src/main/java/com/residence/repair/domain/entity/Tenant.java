package com.residence.repair.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@DiscriminatorValue("TENANT")
public class Tenant extends User{
    @Column(nullable = false)
    private String residenceName;

    @Column(nullable = false)
    private String roomNumber;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<RepairOrder> orders;
}