package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.BoolRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.PermissionManager.MY_PERMISSIONS_REQUEST_READ_STORAGE;

/**
 * Activity to request permissions
 */
public class PermActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 10000;
    public static final String PARAM_NEW_ACTIVITY = "next_activity";
    public static final String PARAM_PERMISSION = "permissions";
    public static final String PARAM_MESSAGE = "message";
    private final static String TAG = "PermActivity";

    private String permission;
    private String message;
    private Class nextActivity;
    private Button bRetry;

    /**
     * Check whether we have this permission or not<br>
     * should be called before this activity to check whether this is necessary
     *
     * @param context
     * @param perm
     * @return true when context has specified permission
     */
    public static boolean hasPermission(Context context, String perm) {
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED)
        ;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perm);

        Intent intent = getIntent();
        // handle passed params
        permission = intent.getStringExtra(PARAM_PERMISSION);
        nextActivity = (Class) intent.getSerializableExtra(PARAM_NEW_ACTIVITY);
        message = intent.getStringExtra(PARAM_MESSAGE);

        if (permission == null || message == null || nextActivity == null) {
            Log.wtf(TAG, "missing parameters");
        }

        TextView tMsg = (TextView) findViewById(R.id.tPermMsg);
        tMsg.setText(message);
        bRetry = (Button) findViewById(R.id.bPermReqAgain);
        bRetry.setVisibility(View.INVISIBLE);

        //TODO: allow ressource IDs as message

        requestPerm();
    }

    private void showNextActivity() {
        Intent intent = new Intent(PermActivity.this, nextActivity);
        this.startActivity(intent);
    }

    /**
     * Called upon retry click
     * @param view
     */
    public void onRetry(View view){
        requestPerm();
    }

    /**
     * Wrapper around requestPerm for unique calls
     */
    private void requestPerm(){
        requestPerm(getApplicationContext(),this,permission);
    }

    /**
     * Request permissions for generic context & activity
     * @param context
     * @param activity
     * @param perm
     */
    private static void requestPerm(Context context, Activity activity, String perm) {
        if (!hasPermission(context,perm)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    perm)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{perm},
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
                    showNextActivity();
                } else { // allow for retry
                    bRetry.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }
}
