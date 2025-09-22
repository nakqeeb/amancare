package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitTypeStatsResponse {

    private VisitType visitType;
    private String visitTypeArabic;
    private Long count;
    private Double percentage;
}
