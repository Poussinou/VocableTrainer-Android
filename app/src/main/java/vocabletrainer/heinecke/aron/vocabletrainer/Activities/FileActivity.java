package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.FileListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formater;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.MainActivity.PREFS_NAME;

/**
 * File activity for file requests<br>
 * <b>requires WRITE_EXTERNAL_STORAGE</b><br>
 * To be called as startActivityForResult
 */
public class FileActivity extends AppCompatActivity {
    private static final String P_KEY_FA_LAST_DIR = "last_directory";
    private static final String P_KEY_FA_LAST_FILENAME = "last_filename";

    /**
     * Param key under which the selected file is returned to the next activity<br>
     * File is passed as string containing the absolute path<br>
     *     Type: File
     */
    public static final String RETURN_FILE = "file";
    /**
     * Param key for return of user friendly formated file path<br>
     * only containing the normal user-visible storage path<br>
     *     Type: String
     */
    public static final String RETURN_FILE_USER_NAME = "user_file_path";
    /**
     * Param key for write flag<br>
     * Pass as true to get a save-as activity, otherwise read file "dialog"
     */
    public static final String PARAM_WRITE_FLAG = "write_flag";
    /**
     * Param key for short message to display
     */
    public static final String PARAM_MESSAGE = "message";
    /**
     * Optional param key for default file name, used upon write flag set true
     */
    public static final String PARAM_DEFAULT_FILENAME = "default_filename";

    private static final String TAG = "FileActivity";
    private ListView listView;
    private EditText tFileName;
    private TextView tCurrentDir;
    private Button bOk;

    private ArrayList<BasicFileEntry> entries;
    private FileListAdapter adapter;
    private Formater fmt;
    private boolean write;
    private File currentDir;
    private BasicFileEntry selectedEntry;
    private String basicDir; // user invisible part to remove
    private String defaultFileName;
    private File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"init");
        setContentView(R.layout.activity_file);
        fmt = new Formater();

        TextView msg = (TextView) findViewById(R.id.tFileMsg);
        tFileName = (EditText) findViewById(R.id.tFileName);
        tCurrentDir = (TextView) findViewById(R.id.tCurrentDir);
        bOk = (Button) findViewById(R.id.bFileOk);

        Intent intent = getIntent();
        msg.setText(intent.getStringExtra(PARAM_MESSAGE));
        write = intent.getBooleanExtra(PARAM_WRITE_FLAG, false);

        tFileName.setVisibility(write ? View.VISIBLE : View.GONE);

        String defaultName = intent.getStringExtra(PARAM_DEFAULT_FILENAME);
        defaultFileName = defaultName == null ? "file.xy" : defaultName;

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
                if (entry.getTypeID() == BasicFileEntry.TYPE_FILE) {
                    Log.d(TAG, "selected: " + entry.getName());
                    view.setSelected(true);
                    selectedEntry = entry;
                    bOk.setEnabled(true);
                    if (write) {
                        tFileName.setText(entry.getName());
                    }
                } else if (entry.getTypeID() == BasicFileEntry.TYPE_DIR) {
                    currentDir = ((FileEntry) entry).getFile();
                    changeDir();
                } else if (entry.getTypeID() == BasicFileEntry.TYPE_UP) {
                    goUp();
                }
            }

        });
        setBasicDir();
        changeDir();
    }

    /**
     * Go on directory up in navigation, if possible
     */
    private void goUp() {
        if (currentDir.getAbsolutePath().equals(basicDir)) {
            //TODO: go to overview
            Log.d(TAG, "cancel go up");
        } else {
            currentDir = currentDir.getParentFile();
            changeDir();
        }
    }

    /**
     * Action for Cancel button press
     *
     * @param view
     */
    public void onCancelPressed(View view) {
        cancel();
    }

    @Override
    public void onBackPressed(){
        cancel();
    }

    /**
     * Cancel file activity
     */
    private void cancel(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    /**
     * Action for OK button press
     *
     * @param view
     */
    public void onOkPressed(View view) {
        if (write || selectedEntry != null) {
            selectedFile = write ? new File(currentDir, tFileName.getText().toString()) : ((FileEntry)selectedEntry).getFile();
            Log.d(TAG,"file:"+selectedFile.getAbsolutePath());
            if (write) {
                if (selectedFile.isDirectory()) { // required !?
                    selectedFile = null;
                } else if (selectedFile.exists()) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle("File exists already");
                    alert.setMessage("Do you really want to delete to delete %f".replace("%f",selectedFile.getName()));

                    alert.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            useFile();
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            selectedFile = null;
                        }
                    });
                    alert.show();
                }else{
                    useFile();
                }
            }

            if(!write && selectedFile != null){
                useFile();
            }
        }
    }

    private void useFile(){
        if(selectedFile != null){
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RETURN_FILE,selectedFile);
            returnIntent.putExtra(RETURN_FILE_USER_NAME,tCurrentDir.getText().toString()+File.separator+selectedFile.getName());
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
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
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        currentDir = new File(settings.getString(P_KEY_FA_LAST_DIR,""));
        if(!currentDir.exists()){ // old value not valid anymore
            Log.w(TAG,"old path is invalid");
            currentDir = Environment.getExternalStorageDirectory();
        }
        currentDir.mkdirs(); // mkdirs, we're sure to have a valid path
        this.basicDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        tFileName.setText(settings.getString(P_KEY_FA_LAST_FILENAME,defaultFileName));
    }

    /**
     * Change directory in view to the one specified in currentDir<br>
     * if currentDir is null, we're assuming that the overview is required
     */
    private void changeDir() {
        selectedEntry = null;
        if (!write) {
            bOk.setEnabled(false);
        }
        entries.clear();
        if (checkMediaState()) {
            if (currentDir != null) {
                File[] files = currentDir.listFiles();
                if (files == null) {
                    Log.e(TAG, "null file list!");
                    Toast.makeText(FileActivity.this, "Unable to show files. Nullpointer", Toast.LENGTH_LONG).show();
                } else {
                    entries.add(new BasicFileEntry("..", "", BasicFileEntry.TYPE_UP, true)); // go back entry
                    for (File file : files) {
                        entries.add(new FileEntry(file, fmt));
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();

        String newDirLabel = currentDir.getAbsolutePath().replaceFirst(basicDir, "");
        if (newDirLabel.length() == 0)
            newDirLabel = "/";
        tCurrentDir.setText(newDirLabel);
    }

    @Override
    protected void onStop(){
        super.onStop();
        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(P_KEY_FA_LAST_FILENAME,tFileName.getText().toString());
        editor.putString(P_KEY_FA_LAST_DIR,currentDir.getAbsolutePath());
        editor.apply();
    }
}