package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

/*
 * Import Activity
 */
public class ImportActivity extends AppCompatActivity {
    /**s
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_FILE_RESULT_CODE = 1;
    private static final String TAG = "ImportActivity";

    /**
     * Import list handling mode
     */
    public enum IMPORT_LIST_MODE {
        /**
         * Replace existing list's vocables
         */
        REPLACE,
        /**
         * Add to existing lists
         */
        ADD,
        /**
         * Ignore existing lists
         */
        IGNORE,
        /**
         * Create new list
         */
        CREATE
    }

    Spinner spFormat;
    Spinner spSingleMetadata;
    Spinner spSingelRaw;
    Spinner spImportMultilist;
    Button bSelectList;
    EditText etList;
    EditText etFile;
    Button bImportOk;
    ListView list;
    ConstraintLayout singleLayout;
    TextView tImportMessage;


    File impFile;
    EntryListAdapter adapter;
    Table targetList;
    ArrayAdapter<GenericSpinnerEntry<CSVFormat>> spAdapterFormat;
    ArrayAdapter<GenericSpinnerEntry<IMPORT_LIST_MODE>> spAdapterMultilist;
    ArrayAdapter<GenericSpinnerEntry<IMPORT_LIST_MODE>> spAdapterSinglelist;
    ArrayAdapter<GenericSpinnerEntry<IMPORT_LIST_MODE>> spAdapterRawlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        adapter = new EntryListAdapter(this, new ArrayList<Entry>(), getApplicationContext());

        spSingelRaw = (Spinner) findViewById(R.id.spImportSingleRaw);
        spSingleMetadata = (Spinner) findViewById(R.id.spImportSingleMetadata);
        spImportMultilist = (Spinner) findViewById(R.id.spImportMultiple);
        singleLayout = (ConstraintLayout) findViewById(R.id.cImportNonMultilist);
        tImportMessage = (TextView) findViewById(R.id.tImportMsg);
        etList = (EditText) findViewById(R.id.tImportList);
        bSelectList = (Button) findViewById(R.id.bImportSelectList);
        etFile = (EditText) findViewById(R.id.tImportPath);
        bImportOk = (Button) findViewById(R.id.bImportOk);
        list = (ListView) findViewById(R.id.lstImportPreview);
        spFormat = (Spinner) findViewById(R.id.spImportFormat);

        list.setAdapter(adapter);

        bImportOk.setEnabled(false);
        etList.setKeyListener(null);
        etFile.setKeyListener(null);

        initSpinner();
    }

    /**
     * Setup spinners
     */
    private void initSpinner(){
        spAdapterFormat = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        spAdapterMultilist = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item);
        spAdapterSinglelist = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item);
        spAdapterRawlist= new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item);

        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.DEFAULT, "Default"));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.EXCEL, "Excel"));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.RFC4180, "RFC4180"));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.TDF, "Tabs"));

        spAdapterMultilist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.REPLACE,"Replace existing lists"));
        spAdapterMultilist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.REPLACE,"Merge existing lists"));
        spAdapterMultilist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.REPLACE,"Ignore existing lists"));

        spAdapterRawlist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.CREATE,"Create new list"));
        spAdapterRawlist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.ADD,"Merge into list"));

        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.REPLACE,"Replace list"));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.ADD,"Add to list"));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.CREATE,"Create new list"));

        spFormat.setAdapter(spAdapterFormat);
        spImportMultilist.setAdapter(spAdapterMultilist);
        spSingleMetadata.setAdapter(spAdapterSinglelist);
        spSingelRaw.setAdapter(spAdapterRawlist);
    }

    /**
     * Called on file select click
     *
     * @param view
     */
    public void selectFile(View view) {
        Intent myIntent = new Intent(this, FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG, false);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE, "Select import sources file.");
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME, "list.csv");
        startActivityForResult(myIntent, REQUEST_FILE_RESULT_CODE);
    }

    /**
     * Verify user input and enable import button if appropriate
     */
    private void checkInputOk(){
        bImportOk.setEnabled(impFile != null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FILE_RESULT_CODE) {
                Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                impFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                etFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                checkInputOk();
            }
        }
    }


    /**
     * Refreshes preview of import parsing
     */
    private void refreshParsePreview(){
        CSVFormat format = spAdapterFormat.getItem(spFormat.getSelectedItemPosition()).getObject();

        try(
            final Reader reader = new FileReader(impFile);
            final CSVParser parser = new CSVParser(reader, format)
        ){
            for (final CSVRecord record : parser) {
                //TODO

            }
        } catch (Exception e){
                Log.e(TAG,"",e);
        }
    }
}
