package com.TenX.Automobile.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "admins")
@PrimaryKeyJoinColumn(name="user_id")
public class Admin extends UserEntity {
    @Column(name = "admin_id", unique = true, updatable = false, nullable = false)
    private String adminId;


}
