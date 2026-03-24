package com.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Column(nullable = false)
    private String password;

    @Column(name = "don_vi_id")
    private Long donViId;

    @Column(nullable = false, length = 50)
    private String role;
}