package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRolesResponse {
    private String roleId;
    private String roleName;
    private Integer userCount;
    private String permissions;
    private String status;  // 'Active' | 'Inactive'
}

