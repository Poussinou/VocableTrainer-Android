package vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer;

import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ExportHeaders.EXPORT_METADATA_START;

/**
 * Importer class
 */
public class Importer {


    private final static String TAG = "Importer";
    private File source;
    private CSVFormat format;
    private ImportHandler handler;

    public Importer(final CSVFormat format, final File source, final ImportHandler handler, final boolean) {
        this.source = source;
        this.format = format;
        this.handler = handler;
    }

    /**
     * Run parser
     */
    public void parse() {
        synchronized (source) {
            try (
                    FileReader reader = new FileReader(source);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    CSVParser parser = new CSVParser(bufferedReader, format)
            ) {
                boolean tbl_start = false;
                for (CSVRecord record : parser) {
                    Log.d(TAG,"processing "+record.toString());
                    if(record.size() < 2){
                        Log.w(TAG,"ignoring following entry: "+record.toString());
                    }
                    String v1 = record.get(0);
                    String v2 = record.get(1);
                    String v3 = record.get(2);
                    if(tbl_start){
                        handler.newTable(v1,v2,v3);
                        tbl_start = false;
                    }else if(tbl_start = v1.equals(EXPORT_METADATA_START[0]) && v2.equals(EXPORT_METADATA_START[1]) && v3.equals(EXPORT_METADATA_START[2])){
                        //do nothing
                    }else{
                        handler.newEntry(v1,v2,v3);
                    }
                }
                parser.close();
                bufferedReader.close();
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }
}
