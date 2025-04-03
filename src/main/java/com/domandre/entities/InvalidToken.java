package com.domandre.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "invalid_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidToken {
    @Id
    private String token;
    @Temporal(TemporalType.TIME)
    private Date expiration;
}