package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ExportHeaders.EXPORT_METADATA_COMMENT;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ExportHeaders.EXPORT_METADATA_START;

/**
 * Export activity
 */
public class ExportActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_RESULT_CODE = 10;
    private static final int REQUEST_TABLES_RESULT_CODE = 20;

    private static final String TAG = "ExportActivity";
    private static final int MAX_PROGRESS = 100;
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
    private CheckBox chkExportTalbeInfo;
    private CheckBox chkExportMultiple;
    private ExportOperation exportTask;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        tExportFile = (EditText) findViewById(R.id.tExportFile);
        btnExport = (Button) findViewById(R.id.bExportStart);
        listView = (ListView) findViewById(R.id.lExportListView);
        addButton = (FloatingActionButton) findViewById(R.id.bExportAddTables);
        chkExportMultiple = (CheckBox) findViewById(R.id.chkExportMulti);
        chkExportTalbeInfo = (CheckBox) findViewById(R.id.chkExportMeta);
        progressBar = (ProgressBar) findViewById(R.id.ExportProgressbar);

        initView();
    }

    /**
     * Init list view
     */
    private void initView() {
        progressBar.setMax(MAX_PROGRESS);
        tExportFile.setKeyListener(null);
        btnExport.setEnabled(false);
        tables = new ArrayList<>();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSelectTables();
            }
        });
        chkExportMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputOk();
            }
        });

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

    /**
     * Calls select tables activity
     */
    private void runSelectTables() {
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_SELECTED, tables);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, true);
        startActivityForResult(myIntent, REQUEST_TABLES_RESULT_CODE);
    }

    /**
     * Called upon ok press
     *
     * @param view
     */
    public void onOk(View view) {
        progressBar.setVisibility(View.VISIBLE);
        ExportStorage es = new ExportStorage(tables, chkExportTalbeInfo.isChecked(), chkExportMultiple.isChecked(), expFile);
        exportTask = new ExportOperation(es);
        exportTask.execute();
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
                adapter.setAllUpdated((ArrayList<Table>) data.getSerializableExtra(ListSelector.RETURN_LISTS));
                checkInputOk();
            }
        }
    }

    /**
     * Validate input & set export button accordingly
     */
    private void checkInputOk() {
        Log.d(TAG, "tables:" + tables.size());
        btnExport.setEnabled(tables.size() > 1 && expFile != null && (chkExportMultiple.isChecked() || (!chkExportMultiple.isChecked() && tables.size() == 2)));
    }

    /**
     * Export task class
     */
    private class ExportOperation extends AsyncTask<Integer, Integer, String> {
        private final ExportStorage es;
        private final Database db;

        /**
         * Creates a new ExportOperation
         * @param es
         */
        public ExportOperation(ExportStorage es) {
            this.es = es;
            db = new Database(getApplicationContext());
        }

        @Override
        protected String doInBackground(Integer... params) {
            Log.d(TAG,"Starting background task");
            try (FileWriter fw = new FileWriter(es.file);
                 //TODO: enforce UTF-8
                 BufferedWriter writer = new BufferedWriter(fw);
                 CSVPrinter printer = new CSVPrinter(writer, DEFAULT);
            ) {
                int i = 0;
                for (Table tbl : es.tables) {
                    if(tbl.getId() == ID_RESERVED_SKIP){
                        continue;
                    }
                    Log.d(TAG,"exporting tbl "+tbl.toString());
                    if (es.exportTableInfo) {
                        printer.printRecord(EXPORT_METADATA_START);
                        printer.printComment(EXPORT_METADATA_COMMENT);
                        printer.print(tbl.getName());
                        printer.print(tbl.getNameA());
                        printer.print(tbl.getNameB());
                        printer.println();
                    }
                    List<Entry> vocables = db.getVocablesOfTable(tbl);

                    for(Entry ent : vocables){
                        printer.print(ent.getAWord());
                        printer.print(ent.getBWord());
                        printer.print(ent.getTip());
                        printer.println();
                    }
                    i++;
                    publishProgress((es.tables.size()/MAX_PROGRESS)*i);
                }
                Log.d(TAG,"closing all");
                printer.close();
                writer.close();
                fw.close();
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            Log.d(TAG,"updating progress");
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            finish();
        }
    }

    /**
     * Export storage class
     */
    private class ExportStorage {
        public final ArrayList<Table> tables;
        public final boolean exportTableInfo;
        public final boolean exportMultiple;
        public final File file;

        /**
         * New export storage
         * @param tables
         * @param exportTableInfo
         * @param exportMultiple
         * @param file
         */
        public ExportStorage(ArrayList<Table> tables, boolean exportTableInfo, boolean exportMultiple, File file) {
            this.tables = tables;
            this.exportTableInfo = exportTableInfo;
            this.exportMultiple = exportMultiple;
            this.file = file;
        }
    }
}
