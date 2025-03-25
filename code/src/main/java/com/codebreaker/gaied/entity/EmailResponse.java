package com.codebreaker.gaied.entity;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailResponse {
    private String emailName;
    private List<Classification> classifications;
    private String reasoning;
    private String duplicateInfo;
    private String confidenceScore;
    private Map<String,String> keyProperties;
}
