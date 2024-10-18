package com.pshs.ams.models.dto.strand;

public class MostPopularStrandDTO {
    private Integer strandId;
    private String strandName;
    private Long studentCount;

    public MostPopularStrandDTO(Integer strandId, String strandName, Long studentCount) {
        this.strandId = strandId;
        this.strandName = strandName;
        this.studentCount = studentCount;
    }

    // Getters and setters
}
