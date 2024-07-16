import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import models.MonthSummary;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ReportGenerator implements RequestHandler<Object, String> {
    DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    S3Client s3Client = S3Client.create();
    private static final String CSV_HEADER = "Username,Month,Duration\n";
    private static final String S3_BUCKET_NAME = "bucket-jeovanis-herrera";

    @Override
    public String handleRequest(Object input, Context context) {
        String month = LocalDate.now().getMonth().toString();
        StringBuilder csvOutput = new StringBuilder(CSV_HEADER);

        dynamoDbClient.scanPaginator(ScanRequest.builder().tableName("report-database").build()).stream()
                .flatMap(scanResponse -> scanResponse.items().stream())
                .forEach(item -> {
                    String username = item.get("Username").s();
                    List<MonthSummary> durationByMonth = parseMonthSummaries(item.get("DurationByMonth").l());

                    for (MonthSummary monthSummary : durationByMonth) {
                        if (monthSummary.getMonth().equals(month)) {
                            String line = username + "," + monthSummary.getMonth() + "," + monthSummary.getDuration() + "\n";
                            csvOutput.append(line);
                        }
                    }
                });

        s3Client.putObject(
                PutObjectRequest.builder().bucket(S3_BUCKET_NAME).key("report.csv").build(),
                RequestBody.fromString(csvOutput.toString()));
        log.info("CSV Report generation and upload completed on {}", LocalDateTime.now());
        return "CSV Report generation and upload complete!";
    }

    private List<MonthSummary> parseMonthSummaries(List<AttributeValue> rawSummaries) {
        Gson gson = new Gson();

        Type mapType = new TypeToken<Map<String, String>>(){}.getType();

        return rawSummaries.stream()
                .map(AttributeValue::m)
                .map(map -> gson.toJson(map, mapType))
                .map(json -> gson.fromJson(json, MonthSummary.class))
                .collect(Collectors.toList());
    }
}