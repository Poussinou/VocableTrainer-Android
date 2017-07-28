package vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer;

/**
 * ImportHandler for detecting the type of import file provided
 */
public class TypeDetector implements ImportHandler {
    private int new_table_count = 0;


    @Override
    public void newTable(String name, String columnA, String columnB) {
        new_table_count++;
    }

    @Override
    public void newEntry(String A, String B, String Tipp) {

    }

    public boolean isMultilist(){
        return new_table_count > 1;
    }

    public boolean isRawData() {
        return new_table_count == 0;
    }
}
