package com.codebreaker.gaied.entity;

import lombok.*;

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
    private String requestType;
    private String requestSubType;
    private String reasoning;
    private String confidenceScore;
    private Map<String,String> keyProperties;
}
