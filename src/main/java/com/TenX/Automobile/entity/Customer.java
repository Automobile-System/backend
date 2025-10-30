package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;



@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name="customers")
@PrimaryKeyJoinColumn(name="user_id")
public class Customer extends UserEntity {


    @Column(name="customer_id", unique = true,updatable = false, nullable = false)
    private String customerId;


}
