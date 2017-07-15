package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_EDITOR_LIST = 10;
    private final static int REQUEST_DELETE_LIST = -1;
    private final static int REQUEST_TRAINER_LIST = 20;
    private static boolean showedDialog = false;
    public static final String PREFS_NAME = "voc_prefs";
    private static final String P_KEY_ALPHA_DIALOG = "showedAlphaDialog";
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        showedDialog = settings.getBoolean(P_KEY_ALPHA_DIALOG, false);
        if(!showedDialog) {
            final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

            finishedDiag.setTitle("Warning");
            finishedDiag.setMessage("This software is an alpha state. This includes, but not limited to, data loss, destroying your phone, eating your children and burning your dog! You have been warned.");

            finishedDiag.setPositiveButton("TLDR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    showedDialog = true;
                }
            });

            finishedDiag.setNegativeButton("Get me outta here", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    System.exit(0);
                }
            });

            finishedDiag.show();
        }
        btnContinue = (Button) findViewById(R.id.bLastSession);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            switch(requestCode){
                //Todo: rewrite both activities to use the returning list selector directly
                case REQUEST_EDITOR_LIST: {
                    Intent myIntent = new Intent(this, EditorActivity.class);
                    myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, false);
                    myIntent.putExtra(EditorActivity.PARAM_TABLE, data.getSerializableExtra(ListSelector.RETURN_LISTS));
                    this.startActivity(myIntent);
                }
                break;
                case REQUEST_TRAINER_LIST: {
                    Intent myIntent = new Intent(this, TrainerSettings.class);
                    myIntent.putExtra(ListSelector.RETURN_LISTS, data.getSerializableExtra(ListSelector.RETURN_LISTS));
                    this.startActivity(myIntent);
                }
                break;
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_ALPHA_DIALOG, showedDialog);
        editor.commit();
    }

    @Override
    protected void onResume(){
        super.onResume();
        btnContinue.setEnabled(new Database(getBaseContext()).isSessionStored());
    }

    /**
     * Open trainer to continue the last session
     * @param view
     */
    public void continueSession(View view){
        Intent myIntent = new Intent(this, TrainerActivity.class);
        myIntent.putExtra(TrainerActivity.PARAM_RESUME_SESSION_FLAG, true);
        this.startActivity(myIntent);
    }

    /**
     * Open new table intent
     *
     * @param view
     */
    public void showNewTable(View view) {
        Intent myIntent = new Intent(this, EditorActivity.class);
        myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, true);
        this.startActivity(myIntent);
    }

    /**
     * Open edit table intent
     *
     * @param view
     */
    public void showEditTable(View view) {
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, false);
        this.startActivityForResult(myIntent, REQUEST_EDITOR_LIST);
    }

    /**
     * Open trainer intent
     * @param view
     */
    public void showTrainer(View view){
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, true);
        this.startActivityForResult(myIntent, REQUEST_TRAINER_LIST);
    }

    /**
     * Open list delete
     * @param view
     */
    public void showDeleteTable(View view){
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(ListSelector.PARAM_DELETE_FLAG, true);
        this.startActivity(myIntent);
    }

    /**
     * Open about activity
     * @param view
     */
    public void showAbout(View view){
        Intent myIntent = new Intent(this, AboutActivity.class);
        this.startActivity(myIntent);
    }

    /**
     * Open export activity
     * @param view
     */
    public void showExport(View view){
        if(PermActivity.hasPermission(getApplicationContext(),ExportActivity.REQUIRED_PERMISSION)) {
            Intent myIntent = new Intent(this, ExportActivity.class);
            this.startActivity(myIntent);
        }else{
            Intent myIntent = new Intent(this, PermActivity.class);
            myIntent.putExtra(PermActivity.PARAM_NEW_ACTIVITY,ExportActivity.class);
            myIntent.putExtra(PermActivity.PARAM_PERMISSION, ExportActivity.REQUIRED_PERMISSION);
            myIntent.putExtra(PermActivity.PARAM_MESSAGE,"Permission required to load/write CSV files for export/import.");
            this.startActivity(myIntent);
        }
    }

}
