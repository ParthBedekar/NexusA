package com.example.nexusa.Dto;

import com.example.nexusa.Model.Enums.ReviewStatus;
import lombok.Data;

@Data
public class ReviewDecisionDTO {
    private ReviewStatus status;  // PUBLISHED, REJECTED, REVISION_REQUESTED
    private String note;
}