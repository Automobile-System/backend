package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingTrendResponse {
    private String period;
    private String chartType;
    private List<String> labels;
    private List<Double> data;
    private Double averageRating;
    private Integer totalReviews;
    private String trend;
}
