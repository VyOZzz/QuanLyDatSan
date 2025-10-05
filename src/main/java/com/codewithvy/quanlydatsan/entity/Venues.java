package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numberOfCourt;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "venues", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Court> courts;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToOne(mappedBy = "venues", cascade = CascadeType.ALL, orphanRemoval = true)
    private VenuesDetail venuesDetail;
}
