package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "venues_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenuesDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;

    @ElementCollection
    @CollectionTable(name = "venues_detail_images", joinColumns = @JoinColumn(name = "venues_detail_id"))
    @Column(name = "image")
    private List<String> images;

    @OneToOne(optional = false)
    @JoinColumn(name = "venues_id", unique = true, nullable = false)
    private Venues venues;

    @OneToMany(mappedBy = "venuesDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Service> services;
}
