package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "offices")
@Getter
@Setter
@NoArgsConstructor
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Назва офісу не може бути порожньою")
    private String name;

    @NotBlank(message = "Місто не може бути порожнім")
    private String city;

    @NotBlank(message = "Адреса не може бути порожньою")
    private String address;

    @CreatedDate
    @Column(updatable = false)   // ніколи не змінюється вручну
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}