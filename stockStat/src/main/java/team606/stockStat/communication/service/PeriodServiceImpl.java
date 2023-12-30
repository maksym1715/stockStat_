package team606.stockStat.communication.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodRequest;

@Service
public class PeriodServiceImpl implements PeriodService {


    public List<PeriodData> getPeriodBetween(PeriodRequest request) {
        return null;
    }


}
