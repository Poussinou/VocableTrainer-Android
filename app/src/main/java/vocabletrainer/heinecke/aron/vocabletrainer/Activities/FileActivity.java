package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.FileListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formater;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.PermissionManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.PermissionManager.MY_PERMISSIONS_REQUEST_READ_STORAGE;

public class FileActivity extends AppCompatActivity {
    private static final String TAG = "FileActivity";
    private ListView listView;
    private ArrayList<BasicFileEntry> entries;
    private FileListAdapter adapter;
    private Formater fmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        fmt = new Formater();

        initListView();


    }

    //TODO: add last-folder save/load

    /**
     * Setup listview
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listViewFiles);

        listView.setLongClickable(true);

        entries = new ArrayList<>();
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

    public void checkPermissionReadStorage(Context context, Activity activity){
        if (ContextCompat.checkSelfPermission(context,      Manifest.permission.READ_EXTERNAL_STORAGE) !=     PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                //premission to read storage
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(FileActivity.this, "We Need permission Storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void TEMP_add_file(){
        checkPermissionReadStorage(getApplicationContext(),this);
        String extState = Environment.getExternalStorageState();
        if(!extState.equals(Environment.MEDIA_MOUNTED)||extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.e(TAG,"media state:: "+extState);
        }
        else{
            File sd = Environment.getExternalStorageDirectory();
            File[] sdDirList = sd.listFiles();
            Log.d(TAG,"sdDirList == null?"+(sdDirList==null));
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

}
