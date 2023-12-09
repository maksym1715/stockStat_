package team606.stockStat.communication.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.UploadInfoRepository;

@Service
public class CsvYahooParserImpl implements CsvParser{

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
    private CsvDataRepository csvDataRepository; 
    @Autowired
    private UploadInfoRepository uploadInfoRepository; 

	@Override
    public List<CsvData> parseCsvFile(MultipartFile file, String fromDate, String toDate) throws IOException, ParseException, CsvException{
        Date fromDateObj = dateFormat.parse(fromDate);
        Date toDateObj = dateFormat.parse(toDate);
        String indexName = file.getOriginalFilename().split("\\.")[0];
        List<CsvData> dataList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            reader.readNext();
            String[] line = reader.readNext();
            while ((line = reader.readNext()) != null) {
                CsvData data = new CsvData();
                UploadInfo uploadInfo = new UploadInfo();
                uploadInfo.setDate(LocalDate.parse(line[0]));
                uploadInfo.setSource(indexName);
                uploadInfo = uploadInfoRepository.save(uploadInfo); 
                data.setUploadInfoId(uploadInfo);
                try {
                    data.setClose(Double.parseDouble(line[2]));
                    data.setVolume(Double.parseDouble(line[3]));
                    data.setOpen(Double.parseDouble(line[4]));
                    data.setHigh(Double.parseDouble(line[5]));
                    data.setLow(Double.parseDouble(line[6]));
                    data.setUploadInfoId(uploadInfo);
                } catch (NumberFormatException e) {
                    e.printStackTrace(); 
                }                
               
                LocalDate dateString = uploadInfo.getDate();
                Date dataDate = null;

                try {
                    dataDate = dateFormat.parse(String.valueOf(dateString));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                
                if (dataDate != null && dataDate.compareTo(fromDateObj) >= 0 && dataDate.compareTo(toDateObj) <= 0) {
                    dataList.add(data);
                }
            }
        }
        
        csvDataRepository.saveAll(dataList);

        return dataList;
    }

}