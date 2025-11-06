package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBoardResponse {
    private String status;
    private List<ProjectSummary> projects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSummary {
        private String id;
        private String title;
        private String customer;
    }
}

