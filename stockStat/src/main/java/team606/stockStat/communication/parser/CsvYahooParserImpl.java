package team606.stockStat.communication.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Service
public class CsvYahooParserImpl implements CsvParser{

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
    public List<CsvData> parseCsvFile(MultipartFile file, String fromDate, String toDate) throws IOException, ParseException, CsvException{
        Date fromDateObj = dateFormat.parse(fromDate);
        Date toDateObj = dateFormat.parse(toDate);

        List<CsvData> dataList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                CsvData data = new CsvData();
                UploadInfo uploadInfo = new UploadInfo();
                uploadInfo.setDate(line[0]);
                uploadInfo.setSource(line[1]);
                data.setUploadInfoId(uploadInfo);
                try {
                    data.setClose(Double.parseDouble(line[2]));
                    data.setVolume(Double.parseDouble(line[3]));
                    data.setOpen(Double.parseDouble(line[4]));
                    data.setHigh(Double.parseDouble(line[5]));
                    data.setLow(Double.parseDouble(line[6]));
                    
                } catch (NumberFormatException e) {
                    e.printStackTrace(); 
                }                
               
                String dateString = uploadInfo.getDate();
                Date dataDate = null;

                try {
                    dataDate = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
      
                
                if (dataDate != null && dataDate.compareTo(fromDateObj) >= 0 && dataDate.compareTo(toDateObj) <= 0) {
                    dataList.add(data);
                }
            }
        }

        return dataList;
    }

}