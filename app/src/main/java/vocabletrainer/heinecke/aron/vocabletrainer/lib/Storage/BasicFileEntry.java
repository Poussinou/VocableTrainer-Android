package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import java.io.File;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formater;

/**
 * Basic File entry only holding size & name
 */
public class BasicFileEntry {
    final String size;
    final String name;
    final int typeID;

    /**
     * New Basic FileEntry
     * @param name entry name column
     * @param size String for size column
     * @param typeID int for specifying the type of this entry (fe.: virtual entry)
     */
    public BasicFileEntry(final String name, final String size, final int typeID){
        this.name = name;
        this.size = size;
        this.typeID = typeID;
    }

    public String getName(){
        return name;
    }

    public String getSize() {
        return size;
    }
}
