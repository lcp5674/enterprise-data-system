package com.enterprise.edams.analysis.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassification {

    private String subjectDomain;
    private String businessDomain;
    private String dataDomain;
    private IndicatorLayer indicatorLayer;
    private String reasoning;

    public enum IndicatorLayer {
        ATOMIC,
        DERIVED,
        CALCULATED
    }
}
