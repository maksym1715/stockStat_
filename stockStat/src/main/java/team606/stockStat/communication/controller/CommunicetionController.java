package team606.stockStat.communication.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.CsvParser;

@RestController
@RequestMapping("/communication")
@RequiredArgsConstructor
public class CommunicetionController {

	@Autowired
	private CsvParser csvParser;
	
	@PostMapping("/parser")
	public List<CsvData> parseCsv(@RequestParam MultipartFile file, @RequestParam String fromDate,
			@RequestParam String toDate) throws IOException, CsvException, java.text.ParseException {
		return csvParser.parseCsvFile(file, fromDate, toDate);
	}
	
	

}
