package com.residence.repair.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@DiscriminatorValue("TENANT")
@Data
@EqualsAndHashCode(callSuper = true)
public class Tenant extends User{
    @Column(nullable = false)
    private String residenceName;

    @Column(nullable = false)
    private String roomNumber;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<RepairOrder> orders;
}