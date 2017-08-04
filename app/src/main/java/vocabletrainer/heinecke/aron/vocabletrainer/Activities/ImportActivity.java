package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer.ImportFetcher;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer.Importer;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Importer.PreviewParser;
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
    private static final int REQUEST_LIST_SELECT_CODE = 2;
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

    private Spinner spFormat;
    private Spinner spSingleList;
    private Spinner spSingelRaw;
    private Spinner spImportMultilist;
    private Button bSelectList;
    private EditText etList;
    private EditText etFile;
    private Button bImportOk;
    private ListView list;
    private ConstraintLayout singleLayout;
    private TextView tImportMessage;
    private boolean isRawData = false;
    private boolean isMultilist = true;
    private PreviewParser previewParser;


    File impFile;
    List<Entry> lst;
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

        lst = new ArrayList<>();
        adapter = new EntryListAdapter(this, lst, getApplicationContext());

        spSingelRaw = (Spinner) findViewById(R.id.spImportSingleRaw);
        spSingleList = (Spinner) findViewById(R.id.spImportSingleMetadata);
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
        spAdapterMultilist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.ADD,"Merge existing lists"));
        spAdapterMultilist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.IGNORE,"Ignore existing lists"));

        spAdapterRawlist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.CREATE,"Create new list"));
        spAdapterRawlist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.ADD,"Merge into list"));

        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.REPLACE,"Replace list"));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.ADD,"Add to list"));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(IMPORT_LIST_MODE.CREATE,"Create new list"));

        spFormat.setAdapter(spAdapterFormat);
        spImportMultilist.setAdapter(spAdapterMultilist);
        spSingleList.setAdapter(spAdapterSinglelist);
        spSingelRaw.setAdapter(spAdapterRawlist);

        spFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshParsing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSingleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spImportMultilist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSingelRaw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private CSVFormat getFormatSelected(){
        return spAdapterFormat.getItem(spFormat.getSelectedItemPosition()).getObject();
    }

    /**
     * Refresh preview parsing, change view accordingly
     */
    private void refreshParsing(){
        if(impFile != null && impFile.exists()) {
            CSVFormat format = getFormatSelected();
            final PreviewParser dataHandler = new PreviewParser(lst);
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Updating");
//            alert.setMessage("");

            final TextView tw = new EditText(this);
            LinearLayout rl = new TableLayout(this);
            rl.addView(tw);
            alert.setView(rl);

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //TODO: add cancel option
                }
            });
            final AlertDialog dialog = alert.show();
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
//                    dialog.dismiss();
                    isMultilist = dataHandler.isMultiList();
                    isRawData = dataHandler.isRawData();
                    refreshView();
                    adapter.notifyDataSetChanged();
                    previewParser = dataHandler;
                    return null;
                }
            };
            ImportFetcher imp = new ImportFetcher(format, impFile, dataHandler,0,dialog,tw,callable);
            lst.clear();
            Log.d(TAG,"Starting task");
            imp.execute(0); // 0 is just to pass smth
        }
    }

    /**
     * Returns the {@link IMPORT_LIST_MODE} of the relevant adapter
     * @return
     */
    private IMPORT_LIST_MODE getListMode(){
        if(isMultilist){
            return spAdapterMultilist.getItem(spImportMultilist.getSelectedItemPosition()).getObject();
        }else if(isRawData){
            return spAdapterRawlist.getItem(spSingelRaw.getSelectedItemPosition()).getObject();
        }else{
            return spAdapterSinglelist.getItem(spSingleList.getSelectedItemPosition()).getObject();
        }
    }

    /**
     * Called when import was clickeds
     * @param view
     */
    public void onImport(View view){
        CSVFormat format = getFormatSelected();
        final Importer dataHandler = new Importer(getApplicationContext(),previewParser,getListMode(),targetList);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Importing");
//            alert.setMessage("");

        final TextView tw = new EditText(this);
        LinearLayout rl = new TableLayout(this);
        rl.addView(tw);
        alert.setView(rl);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO: add cancel option
            }
        });
        final AlertDialog dialog = alert.show();
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
//                    dialog.dismiss();
                return null;
            }
        };
        ImportFetcher imp = new ImportFetcher(format, impFile, dataHandler,previewParser.getAmountRows(),dialog,tw,callable);
        lst.clear();
        Log.d(TAG,"Starting task");
        imp.execute(0); // 0 is just to pass smth
    }

    /**
     * Refresh visibility of all options based on the input<br>
     *     also calls checkInput
     */
    private void refreshView(){
        if(!isMultilist){
            IMPORT_LIST_MODE mode;
            if(isRawData){
                mode = spAdapterRawlist.getItem(spSingelRaw.getSelectedItemPosition()).getObject();
            }else{
                mode = spAdapterSinglelist.getItem(spSingleList.getSelectedItemPosition()).getObject();
            }
            boolean isCreate = mode == IMPORT_LIST_MODE.CREATE;

            bSelectList.setVisibility(isCreate ? View.GONE : View.VISIBLE);
            etList.setVisibility(isCreate ? View.GONE : View.VISIBLE);
        }

        singleLayout.setVisibility(isMultilist ? View.GONE : View.VISIBLE);
        spImportMultilist.setVisibility(isMultilist ? View.VISIBLE : View.GONE);
        spSingelRaw.setVisibility(isRawData ? View.VISIBLE : View.GONE);
        spSingleList.setVisibility(isRawData ? View.GONE : View.VISIBLE);

        String text;
        if (isRawData)
            text = "Raw data list detected";
        else if (isMultilist)
            text = "Mult-list detected";
        else
            text = "Single list detected";
        tImportMessage.setText(text);

        checkInput();
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
     * Called on list select click
     *
     * @param view
     */
    public void selectList(View view){
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT,false);
        myIntent.putExtra(ListSelector.PARAM_DELETE_FLAG,false);
        myIntent.putExtra(ListSelector.PARAM_SELECTED,targetList);
        startActivityForResult(myIntent, REQUEST_LIST_SELECT_CODE);
    }

    /**
     * Verify user input and enable import button if appropriate
     */
    private void checkInput(){
        boolean is_ok = true;
        if(impFile == null){
            is_ok = false;
        }
        if(isMultilist){

        }else if(isRawData && targetList == null && spAdapterRawlist.getItem(spSingelRaw.getSelectedItemPosition()).getObject() == IMPORT_LIST_MODE.ADD){
            is_ok = false;
        }else if(targetList == null && spAdapterSinglelist.getItem(spSingleList.getSelectedItemPosition()).getObject() != IMPORT_LIST_MODE.CREATE){ // single list
            is_ok = false;
        }

        bImportOk.setEnabled(is_ok);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FILE_RESULT_CODE) {
                Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                impFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                etFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                checkInput();
                refreshParsing();
            }else if(requestCode == REQUEST_LIST_SELECT_CODE){
                Log.d(TAG,"got list");
                targetList = (Table) data.getSerializableExtra(ListSelector.RETURN_LISTS);
                etList.setText(targetList.getName());
                checkInput();
            }
        }
    }
}
