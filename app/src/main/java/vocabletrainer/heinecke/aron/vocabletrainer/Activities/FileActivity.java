package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.FileListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formater;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;

/**
 * File activity for file request
 */
public class FileActivity extends AppCompatActivity {
    /**
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    /**
     * Param key for activity to call afterwards
     */
    public static final String PARAM_NEXT_ACTIVITY = "next_activity";
    /**
     * Param key under which the selected file is <b>passed to the next activity</b><br>
     *     File is passed as string containing the absolute path
     */
    public static final String PARAM_PASSED_FILE = "file";
    /**
     * Param key for write flag<br>
     *     Pass as true to get a save-as activity, otherwise read file "dialog"
     */
    public static final String PARAM_WRITE_FLAG = "write_flag";
    /**
     * Param key for short message to display
     */
    public static final String PARAM_MESSAGE = "message";

    private static final String TAG = "FileActivity";
    private ListView listView;
    private ArrayList<BasicFileEntry> entries;
    private FileListAdapter adapter;
    private Formater fmt;
    private boolean write;
    private Class nextActivity;
    private EditText tFileName;
    private File currentDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        fmt = new Formater();

        TextView msg = (TextView) findViewById(R.id.tFileMsg);
        tFileName = (EditText) findViewById(R.id.tFileName);

        Intent intent = getIntent();
        msg.setText(intent.getStringExtra(PARAM_MESSAGE));
        write = intent.getBooleanExtra(PARAM_WRITE_FLAG,false);
        nextActivity = (Class) intent.getSerializableExtra(PARAM_NEXT_ACTIVITY);

        tFileName.setVisibility(write ? View.VISIBLE : View.GONE);

        initListView();
    }

    //TODO: add last-folder save/load

    /**
     * Setup listview
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listViewFiles);

        listView.setLongClickable(true);

        entries = new ArrayList<>(20); // just a good guess
        TEMP_add_file();
        adapter = new FileListAdapter(this, entries,this);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
                Toast.makeText(FileActivity.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();

            }

        });
    }

    private void TEMP_add_file(){
        String extState = Environment.getExternalStorageState();
        if(!extState.equals(Environment.MEDIA_MOUNTED)||extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.e(TAG,"media state: "+extState);
            Toast.makeText(FileActivity.this, "No storage storage found / writeable! Unable to continue", Toast.LENGTH_LONG).show();
        }
        else{
            File sd = Environment.getExternalStorageDirectory();
            File[] sdDirList = sd.listFiles();
            if(sdDirList == null){
                Log.wtf(TAG,"Missing permission to access external storage, dirlist = null");
            }else{
                for(File file : sdDirList){
                    FileEntry fe = new FileEntry(file,fmt);
                    entries.add(fe);
                }
            }
        }
//        FilenameFilter filter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String filename) {
//                File sel = new File(dir, filename);
//                // Filters based on whether the file is hidden or not
//                return (sel.isFile() || sel.isDirectory())
//                        && !sel.isHidden();
//
//            }
//        };
//        String[] fList = dataDirectory.list(filter);
//        Log.d(TAG,""+(fList == null));
//                FileEntry fe = new FileEntry(file,fmt);
//                entries.add(fe);
    }

    /**
     * Load default or last path / file into dialog
     */
    private void getBasisDir(){
        //TODO: load from storage, check whether path still valid
        // check also for full-file path or just directory

    }

}
