package vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer;

import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Preview parser handler<br>
 *     Limiting amount of entries parsed per list
 */
public class PreviewParser implements ImportHandler {
    private List<Entry> list;
    private static int PARSE_LIMIT = 5;
    private int parsed_limiter = 0;
    private final Table tbl = null;
    private int tblCount = 0;

    @Override
    public void newTable(String name, String columnA, String columnB) {
        list.add(new Entry(columnA,columnB,name,ID_RESERVED_SKIP, tbl, -2L));
        parsed_limiter = 0;
        tblCount++;
    }

    @Override
    public void newEntry(String A, String B, String Tipp) {
        if(parsed_limiter < PARSE_LIMIT){
            list.add(newEntry());
            parsed_limiter++;
        }
    }

    /**
     * Is parsed list raw data without list metadata
     * @return
     */
    public boolean isRawData(){
        return tblCount == 0;
    }

    /**
     * Is parsed list a multilist
     * @return
     */
    public boolean isMultiList(){
        return tblCount > 1;
    }
}
