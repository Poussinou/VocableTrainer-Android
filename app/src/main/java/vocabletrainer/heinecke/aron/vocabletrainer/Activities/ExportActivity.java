package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

/**
 * Export activity
 */
public class ExportActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_RESULT_CODE = 10;
    private static final int REQUEST_TABLES_RESULT_CODE = 20;

    private static final String TAG = "ExportActivity";
    /**
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private EditText tExportFile;
    private Button btnExport;
    private File expFile;
    private ListView listView;
    private FloatingActionButton addButton;
    private ArrayList<Table> tables;
    private TableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        tExportFile = (EditText) findViewById(R.id.tExportFile);
        btnExport = (Button) findViewById(R.id.bExportStart);
        listView = (ListView) findViewById(R.id.lExportListView);
        addButton = (FloatingActionButton) findViewById(R.id.bExportAddTables);

        tExportFile.setKeyListener(null);
        btnExport.setEnabled(false);
        tables = new ArrayList<>();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSelectTables();
            }
        });
        initList();
    }

    /**
     * Init list view
     */
    private void initList() {
        adapter = new TableListAdapter(this, R.layout.table_list_view, tables, false);
        listView.setAdapter(adapter);
        listView.setLongClickable(false);
//        listView.setClickable(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runSelectTables();
            }
        });
    }

    /**
     * Called on cancel button click
     *
     * @param view
     */
    public void onCancel(View view) {
        finish();
    }

    /**
     * Called on file select click
     *
     * @param view
     */
    public void selectFile(View view) {
        Intent myIntent = new Intent(this, FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG, true);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE, "Select export save file.");
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME, "list.csv");
        startActivityForResult(myIntent, REQUEST_FILE_RESULT_CODE);
    }

    /**
     * Called on table select click
     *
     * @param view
     */
    public void onSelectTables(View view) {
        runSelectTables();
    }

    private void runSelectTables() {
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_SELECTED, tables);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, true);
        startActivityForResult(myIntent, REQUEST_TABLES_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FILE_RESULT_CODE) {
                Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                expFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                tExportFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                checkInputOk();
            } else if (requestCode == REQUEST_TABLES_RESULT_CODE) {
                tables = (ArrayList<Table>) data.getSerializableExtra(ListSelector.RETURN_LISTS);
                checkInputOk();
            }
        }
    }

    /**
     * Validate input & set export button accordingly
     */
    private void checkInputOk(){
        btnExport.setEnabled(tables.size() > 0 && expFile != null);
    }
}
