//package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Typeface;
//import android.support.annotation.LayoutRes;
//import android.support.annotation.NonNull;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import vocabletrainer.heinecke.aron.vocabletrainer.R;
//import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
//
///**
// * ArrayAdapter for generic spinner with one Method
// */
//public class SpinnerArrayAdapter<T> extends ArrayAdapter {
//    private ArrayList<GenericSpinnerEntry<T>> entries;
//    private Context context;
//    private LayoutInflater inflater;
//
//    /**
//     * Creates a new SpinnerAdapter
//     * @param activity
//     * @param resource
//     */
//    public SpinnerArrayAdapter(@NonNull Activity activity, @LayoutRes int resource, ArrayList<GenericSpinnerEntry<T>> entries) {
//        super(activity.getApplicationContext(),resource);
//        this.context = context;
//        this.entries = entries;
//    }
//
//    private class ViewHolder {
//        protected TextView colA;
//    }
//
//    @Override
//    public TextView getView(int position, View convertView, ViewGroup parent) {
//        final ViewHolder holder;
//        final GenericSpinnerEntry item = entries.get(position);
//
//        if (convertView == null) {
//            holder = new ViewHolder();
//
//            convertView = inflater.inflate(R.layout.entry_list_view, null);
//
//            holder.colA = (TextView) convertView.findViewById(R.id.);
//
//            convertView.setTag(holder);
//            convertView.setTag(R.id.entryFirstText, holder.colA);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        holder.colA.setText(item.getDisplayText());
//
//        return holder.colA;
//    }
//
//    @Override
//    public TextView getDropDownView(int position, View convertView,
//                                    ViewGroup parent) {
//
//        TextView v = (TextView) super
//                .getView(position, convertView, parent);
//        Typeface myTypeFace = Typeface.createFromAsset(context.getAssets(),
//                "fonts/gilsanslight.otf");
//        v.setTypeface(myTypeFace);
//        v.setText(itemList.get(position));
//        return v;
//    }
//
//}
