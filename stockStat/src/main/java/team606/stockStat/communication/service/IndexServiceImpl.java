package team606.stockStat.communication.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class IndexServiceImpl implements IndexService {

	@Override
    public String getTimeHistoryForIndex(String indexName) {
        String apiUrl = "http://localhost:8080"; 
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(apiUrl + "/communication/index/" + indexName, String.class);
    }

	@Override
    public List<String> getAllIndexes() {
        
        String apiUrl = "http://localhost:8080"; 
        RestTemplate restTemplate = new RestTemplate();
        String[] indexes = restTemplate.getForObject(apiUrl + "/communication/index", String[].class);
        return Arrays.asList(indexes);
    }

	

	

}
