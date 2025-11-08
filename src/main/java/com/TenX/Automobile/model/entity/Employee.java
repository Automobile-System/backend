package com.TenX.Automobile.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;



@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name="employees")
@PrimaryKeyJoinColumn(name = "user_id")
public class Employee extends UserEntity {

    @Column(name="employee_id", unique = true,updatable = false, nullable = false)
    private String employeeId;

    @Column(name= "specialty")
    private String specialty;



}
