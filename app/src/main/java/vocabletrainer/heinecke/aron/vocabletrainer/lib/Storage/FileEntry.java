package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import java.io.File;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formater;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;

/**
 * File Entry holding also a real file
 */
public class FileEntry extends BasicFileEntry {
    public static final int TYPE = 0;
    private final File file;

    /**
     * New FileEntry
     * @param file File
     * @param fmt Formater for length
     */
    public FileEntry(final File file, final Formater fmt){
        super(file.getName(), fmt.formatFileLength(file),TYPE);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public boolean isFile(){
        return this.file.isFile();
    }
}
