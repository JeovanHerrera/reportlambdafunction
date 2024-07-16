package models;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MonthSummary {
    private String month;
    private Double duration;
}