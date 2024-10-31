package com.pshs.ams.models.dto.strand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MostPopularStrandDTO {
    private Integer strandId;
    private String strandName;
    private Long studentCount;
}
