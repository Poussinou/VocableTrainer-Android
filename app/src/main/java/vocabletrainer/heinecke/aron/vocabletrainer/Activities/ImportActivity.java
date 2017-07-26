package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

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

    Spinner spFormat;
    RadioButton rbDetectMetadata;
    RadioButton rbImportToList;
    CheckBox chkOverrideList;
    Button bSelectList;
    EditText etList;
    CheckBox chkReplaceLists;
    CheckBox chkMultiList;
    EditText etFile;
    Button bImportOk;
    ListView list;

    File impFile;
    EntryListAdapter adapter;
    Table targetList;
    ArrayAdapter<GenericSpinnerEntry<CSVFormat>> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        adapter = new EntryListAdapter(this, new ArrayList<Entry>(), getApplicationContext());

        rbDetectMetadata = (RadioButton) findViewById(R.id.rbImportMetadata);
        rbImportToList = (RadioButton) findViewById(R.id.rbImportToList);
        chkMultiList = (CheckBox) findViewById(R.id.chkImportMultilist);
        chkOverrideList = (CheckBox) findViewById(R.id.chkImportOverride);
        etList = (EditText) findViewById(R.id.tImportList);
        bSelectList = (Button) findViewById(R.id.bImportSelectList);
        chkReplaceLists = (CheckBox) findViewById(R.id.chkImportOverwrite);
//        chkReplaceLists = (CheckBox) findViewById(R.id.);
        etFile = (EditText) findViewById(R.id.tImportPath);
        bImportOk = (Button) findViewById(R.id.bImportOk);
        list = (ListView) findViewById(R.id.lstImportPreview);
        spFormat = (Spinner) findViewById(R.id.spImportFormat);

        list.setAdapter(adapter);

        bImportOk.setEnabled(false);
        etList.setKeyListener(null);
        etFile.setKeyListener(null);

        rbDetectMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbImportToList.setChecked(false);
                changeRadioButtons();
            }
        });
        rbImportToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbDetectMetadata.setChecked(false);
                changeRadioButtons();
            }
        });

        spinnerAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item);
        spFormat.setAdapter(spinnerAdapter);
        spinnerAdapter.add(new GenericSpinnerEntry<>(CSVFormat.DEFAULT,"Default"));
        spinnerAdapter.add(new GenericSpinnerEntry<>(CSVFormat.EXCEL,"Excel"));
        spinnerAdapter.add(new GenericSpinnerEntry<>(CSVFormat.RFC4180,"RFC4180"));
        spinnerAdapter.add(new GenericSpinnerEntry<>(CSVFormat.TDF,"Tabs"));

        changeRadioButtons();
    }

    /**
     * Change visibility of options according to radio button selection
     */
    private void changeRadioButtons(){
        boolean detectMetadata;
        if(detectMetadata = rbDetectMetadata.isChecked()){
            rbImportToList.setChecked(false);
        }else{
            rbImportToList.setChecked(true);
            rbDetectMetadata.setChecked(false);
        }
        bSelectList.setVisibility(detectMetadata ? View.GONE : View.VISIBLE);
        etList.setVisibility(detectMetadata ? View.GONE : View.VISIBLE);
        chkOverrideList.setVisibility(detectMetadata ? View.GONE : View.VISIBLE);
        chkReplaceLists.setVisibility(detectMetadata ? View.VISIBLE : View.GONE);
        chkMultiList.setVisibility(detectMetadata ? View.VISIBLE : View.GONE);
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
        CSVFormat format = spinnerAdapter.getItem(spFormat.getSelectedItemPosition()).getObject();

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
