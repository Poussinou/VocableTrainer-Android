package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;

/**
 * ArrayAdapter for generic spinner with one Method
 */
public class SpinnerArrayAdapter<T> extends ArrayAdapter {
    private ArrayList<GenericSpinnerEntry<T>> entries;
    private Context context;

    /**
     * Creates a new SpinnerAdapter
     * @param context
     * @param resource
     */
    public SpinnerArrayAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<GenericSpinnerEntry<T>> entries) {
        super(context, resource);
        this.context = context;
        this.entries = entries;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super
                .getView(position, convertView, parent);
        Typeface myTypeFace = Typeface.createFromAsset(context.getAssets(),
                "fonts/gilsanslight.otf");
        v.setTypeface(myTypeFace);
        v.setText(itemList.get(position));
        return v;
    }

    public TextView getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

        TextView v = (TextView) super
                .getView(position, convertView, parent);
        Typeface myTypeFace = Typeface.createFromAsset(context.getAssets(),
                "fonts/gilsanslight.otf");
        v.setTypeface(myTypeFace);
        v.setText(itemList.get(position));
        return v;
    }

}
