package team606.stockStat.communication.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CorrelationRequest {
    private List<String> indexs;
    private String from;
    private String to;
}
