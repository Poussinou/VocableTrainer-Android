package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
     * File is passed as string containing the absolute path
     */
    public static final String PARAM_PASSED_FILE = "file";
    /**
     * Param key for write flag<br>
     * Pass as true to get a save-as activity, otherwise read file "dialog"
     */
    public static final String PARAM_WRITE_FLAG = "write_flag";
    /**
     * Param key for short message to display
     */
    public static final String PARAM_MESSAGE = "message";

    private static final String TAG = "FileActivity";
    private ListView listView;
    private EditText tFileName;
    private TextView tCurrentDir;

    private ArrayList<BasicFileEntry> entries;
    private FileListAdapter adapter;
    private Formater fmt;
    private boolean write;
    private Class nextActivity;
    private File currentDir;
    private String basicDir; // user invisible part to remove

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        fmt = new Formater();

        TextView msg = (TextView) findViewById(R.id.tFileMsg);
        tFileName = (EditText) findViewById(R.id.tFileName);
        tCurrentDir = (TextView) findViewById(R.id.tCurrentDir);

        Intent intent = getIntent();
        msg.setText(intent.getStringExtra(PARAM_MESSAGE));
        write = intent.getBooleanExtra(PARAM_WRITE_FLAG, false);
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
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setLongClickable(false);

        entries = new ArrayList<>(20); // just a good guess
//        TEMP_add_file();
        adapter = new FileListAdapter(this, entries, this);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
                BasicFileEntry entry = entries.get(pos);
                if(entry.getTypeID() == BasicFileEntry.TYPE_FILE) {
                    view.setSelected(true);
                }else if(entry.getTypeID() == BasicFileEntry.TYPE_DIR){
                    currentDir = ((FileEntry) entry).getFile();
                    changeDir();
                }else if(entry.getTypeID() == BasicFileEntry.TYPE_UP){
                    goUp();
                }
//                Toast.makeText(FileActivity.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
            }

        });
        setBasicDir();
        changeDir();
    }

    /**
     * Go on directory up in navigation, if possible
     */
    private void goUp(){
        if(currentDir.getAbsolutePath().equals(basicDir)){
            //TODO: go to overview
            Log.d(TAG,"cancel go up");
        }else{
            currentDir = currentDir.getParentFile();
            changeDir();
        }
    }

    /**
     * Action for OK button press
     * @param view
     */
    public void onOkPressed(View view){
//        boolean fileIsValid = false;
//        File cFile;
//        if(write){
//            cFile = new File(currentDir,tFileName.getText().toString());
//            if(cFile.isDirectory()){
//                //TODO: dir error dialog
//            }else if(cFile.exists()){
//                //TODO: exists error dialog
//            }else{
//                fileIsValid = true;
//            }
//        }else{
//
//        }
//
//        if(fileIsValid) {
//            Intent intent = new Intent(FileActivity.this, nextActivity);
//            intent.putExtra(PARAM_PASSED_FILE, cFile);
//            this.startActivity(intent);
//        }
    }

    /**
     * Checks current media state
     *
     * @return true when media is ready
     */
    private boolean checkMediaState() {
        String extState = Environment.getExternalStorageState();
        if (!extState.equals(Environment.MEDIA_MOUNTED) || extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.e(TAG, "media state: " + extState);
            Toast.makeText(FileActivity.this, "No storage storage found / writeable! Unable to continue", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Load default or last path / file into dialog
     */
    private void setBasicDir() {
        //TODO: load from storage, check whether path still valid
        // check also for full-file path or just directory
        this.currentDir = Environment.getExternalStorageDirectory();
        this.basicDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Change directory in view to the one specified in currentDir<br>
     *     if currentDir is null, we're assuming that the overview is required
     */
    private void changeDir() {
        entries.clear();
        if (checkMediaState()) {
            if (currentDir != null) {
                File[] files = currentDir.listFiles();
                if (files == null) {
                    Log.e(TAG, "null file list!");
                    Toast.makeText(FileActivity.this, "Unable to show files. Nullpointer", Toast.LENGTH_LONG).show();
                } else {
                    entries.add(new BasicFileEntry("..","",BasicFileEntry.TYPE_UP,true)); // go back entry
                    for (File file : files) {
                        entries.add(new FileEntry(file, fmt));
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();

        String newDirLabel = currentDir.getAbsolutePath().replaceFirst(basicDir,"");
        if(newDirLabel.length() == 0)
            newDirLabel = "/";
        tCurrentDir.setText(newDirLabel);
    }

}
