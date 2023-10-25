package team606.stockStat.communication.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;
import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodRequest;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.CsvParser;
import team606.stockStat.communication.service.IndexService;
import team606.stockStat.communication.service.PeriodService;

@RestController
@RequestMapping("/communication")
@RequiredArgsConstructor
public class CommunicetionController {
	
	private final IndexService indexService;
	private final PeriodService periodService;

	@Autowired
	private CsvParser csvParser;
	
	@PostMapping("/parser")
	public List<CsvData> parseCsv(@RequestParam MultipartFile file, @RequestParam String fromDate,
			@RequestParam String toDate) throws IOException, CsvException, java.text.ParseException {
		return csvParser.parseCsvFile(file, fromDate, toDate);
	}
	
	 @GetMapping("/index/{indexName}")
	    public ResponseEntity<String> getTimeHistoryForIndex(@PathVariable String indexName) {
	        String response = indexService.getTimeHistoryForIndex(indexName);
	        return ResponseEntity.ok(response);
	    }
    
	 @GetMapping("/index")
	    public ResponseEntity<List<String>> getAllIndexes() {
	        List<String> indexes = indexService.getAllIndexes();
	        return ResponseEntity.ok(indexes);
	    }
    
    @PostMapping("/period")
    public ResponseEntity<List<PeriodData>> getPeriodBetween(@RequestBody PeriodRequest request) {
        List<PeriodData> periodData = periodService.getPeriodBetween(request);
        return ResponseEntity.ok(periodData);
    }
    
 

}
