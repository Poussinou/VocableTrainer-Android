package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter.STARTING_ITEM;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * List selector activity
 */
public class ListSelector extends AppCompatActivity {

    private static final String TAG = "ListSelector";

    /**
     * Set whether multi-select is enabled or not<br>
     * Boolean expected
     */
    public static final String PARAM_MULTI_SELECT = "multiselect";

    /**
     * Param key for return of selected lists<br>
     * This key contains a {@link Table} object or a {@link List} of {@link Table}
     */
    public static final String RETURN_LISTS = "selected";

    /**
     * Pass this flag as true to call this as an deletion activity
     */
    public static final String PARAM_DELETE_FLAG = "delete";

    /**
     * Optional Param key for already selected lists, available when multiselect is set<br>
     *     Expect a {@link List} of {@link Table}
     */
    public static final String PARAM_SELECTED = "selected";

    private boolean multiselect;
    private ListView listView;
    private TableListAdapter adapter;
    private boolean delete;
    Database db;
    List<Table> tables;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this.getBaseContext());
        setContentView(R.layout.activity_list_selector);
        Intent intent = getIntent();
        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        delete = intent.getBooleanExtra(PARAM_DELETE_FLAG, false);

        // setup listview
        initListView();
        loadTables((ArrayList<Table>) intent.getSerializableExtra(PARAM_SELECTED));
    }

    @Override
    public void onResume(){
        super.onResume();
        loadTables(null);
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    /**
     * Load tables from db
     * @param tickedTables already selected tables, can be null
     */
    private void loadTables(List<Table> tickedTables) {
        tables = db.getTables();
        adapter.setAllUpdated(tables);
        Log.d(TAG,"before smth");
        if(tickedTables != null){
            //TODO: introduce hashmap and get rid of this hack
            ArrayList<Integer> ticked = new ArrayList<>(tickedTables.size());
            for(Table tbl : tickedTables){
                ticked.add(tbl.getId());
            }
            Log.d(TAG,"ticked: "+ticked.size()+" items:"+tables.size());

            for(int i = STARTING_ITEM; i < tables.size(); i++){
                Log.d(TAG,"checking item "+i);
                if(ticked.contains(tables.get(i).getId())){
                    listView.setItemChecked(i, true);
                    Log.d(TAG,"setting marker to "+i);
                }
            }
        }
    }

    /**
     * Setup list view
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listVIewLstSel);
//        listView.setLongClickable(true);

        ArrayList<Table> tables = new ArrayList<>();
        adapter = new TableListAdapter(this, R.layout.table_list_view, tables, multiselect);

        listView.setAdapter(adapter);

        if (multiselect) {
            setTitle(R.string.ListSelector_Title_Training);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setItemsCanFocus(false);

            Button btn = (Button) findViewById(R.id.btnOkSelect);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Table> selectedTables = new ArrayList<Table>(10);
                    final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    int chkItemsCount = checkedItems.size();

                    for (int i = 0; i < chkItemsCount; ++i) {
                        if (checkedItems.valueAt(i)) {
                            selectedTables.add(adapter.getItem(checkedItems.keyAt(i)));
                        }
                    }

                    Log.d(TAG, "returning with " + selectedTables.size() + " selected items");

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RETURN_LISTS,selectedTables);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            });
        } else {
            if (delete) {
                setTitle(R.string.ListSelector_Title_Delete);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Table table = (Table) adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            showDeleteDialog(table);
                        }
                    }

                });
            } else {
                setTitle(R.string.ListSelector_Title_Edit);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Table table = (Table) adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_LISTS,table);
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        }
                    }

                });
            }
        }

    }

    /**
     * Show delete dialog for table
     *
     * @param tableToDelete
     */
    private void showDeleteDialog(final Table tableToDelete) {
        final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

        finishedDiag.setTitle(R.string.ListSelector_Diag_delete_Title);
        finishedDiag.setMessage(String.format(getText(R.string.ListSelector_Diag_delete_Msg).toString(),
                tableToDelete.getName(),tableToDelete.getNameA(),tableToDelete.getNameB()));

        finishedDiag.setPositiveButton(R.string.ListSelector_Diag_delete_btn_Delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                db.deleteTable(tableToDelete);
                adapter.removeEntryUpdated(tableToDelete);
            }
        });

        finishedDiag.setNegativeButton(R.string.ListSelector_Diag_delete_btn_Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });

        finishedDiag.show();
    }
}
