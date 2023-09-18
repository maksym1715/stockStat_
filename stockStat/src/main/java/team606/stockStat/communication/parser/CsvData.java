package team606.stockStat.communication.parser;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "csv_data")
@Getter
@Setter
public class CsvData {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private UploadInfo uploadInfoId;
    private double close;
    private double volume;
    private double open;
    private double high;
    private double low;
        
}
