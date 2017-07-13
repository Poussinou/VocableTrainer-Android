package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Export activity
 */
public class ExportActivity extends AppCompatActivity {

    private static final String TAG = "ExportActivity";

    /**
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_FILE_RESULT_CODE = 100;
    private EditText tExportFile;
    private Button btnExport;
    private File expFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        tExportFile = (EditText) findViewById(R.id.tExportFile);
        tExportFile.setKeyListener(null);
        btnExport = (Button) findViewById(R.id.bExportStart);
        btnExport.setEnabled(false);
    }

    /**
     * Called on cancel button click
     * @param view
     */
    public void onCancel(View view){
        finish();
    }

    /**
     * Called on file select click
     * @param view
     */
    public void selectFile(View view){
        Intent myIntent = new Intent(this,FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG,true);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE,"Select export save file.");
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME,"list.csv");
        startActivityForResult(myIntent,REQUEST_FILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Log.d(TAG,"got file:"+data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                expFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                tExportFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
            }
        }
    }
}
