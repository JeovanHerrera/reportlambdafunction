package models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TrainerSchedule {
    private String id;
    private String username;
    private List<MonthSummary> durationByMonth;
}
