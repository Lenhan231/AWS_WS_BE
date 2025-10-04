package com.easybody.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "gyms")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Gym name is required")
    private String name;

    @Column(length = 2000)
    private String description;

    private String logoUrl;

    @Column(nullable = false)
    @NotBlank(message = "Address is required")
    private String address;

    @Column(nullable = false)
    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String country;
    private String postalCode;

    @Column(nullable = false)
    private String phoneNumber;

    private String email;
    private String website;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GymStaff> staff = new HashSet<>();

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GymPTAssociation> ptAssociations = new HashSet<>();

    @OneToMany(mappedBy = "gym")
    @Builder.Default
    private Set<Offer> offers = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

