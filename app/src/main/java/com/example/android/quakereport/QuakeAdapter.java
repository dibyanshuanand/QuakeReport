package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class QuakeAdapter extends ArrayAdapter<Quake> {

    public QuakeAdapter(@NonNull Context context, @NonNull List<Quake> quakes) {
        super(context, 0, quakes);
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View quakeListView = convertView;
        if (quakeListView == null) {

            quakeListView = LayoutInflater.from(getContext())
                    .inflate(R.layout.quake, parent, false);
        }

        Quake currentQuake = getItem(position);

        TextView magTextView = (TextView) quakeListView.findViewById(R.id.mag);
        // Used DecimalFormat class to format the obtained magnitude value to pattern
        // specified in the DecimalFormat initialization
        DecimalFormat magFormatter = new DecimalFormat("0.0");
        magTextView.setText(magFormatter.format(currentQuake.getMagnitude(getContext())));

        /** Separate the location obtained from JSON response into Primary Location and Offset.
         *  Can also be done using String.split() : "requires regex expression".
         */
        String time = currentQuake.getLocation(getContext());
        String secLoc, primLoc;
        if (time.contains(" of ")) {
            secLoc = time.substring(0, time.indexOf("of")+2);
            primLoc = time.substring(time.indexOf("of")+3);
        } else {
            secLoc = "Near the";
            primLoc = time;
        }

        TextView primLocTextView = (TextView) quakeListView.findViewById(R.id.primLocation);
        primLocTextView.setText(primLoc);
        TextView secLocTextView = (TextView) quakeListView.findViewById(R.id.secLocation);
        secLocTextView.setText(secLoc);

        TextView dateTextView = (TextView) quakeListView.findViewById(R.id.date);
        dateTextView.setText(currentQuake.getDate(getContext()));

        TextView timeTextView = (TextView) quakeListView.findViewById(R.id.time);
        timeTextView.setText(currentQuake.getTime(getContext()));

        /** Set the correct background color on the magnitude circle
         *  Fetch the background from the TextView, which is a GradientDrawable
         */
        GradientDrawable magnitudeCircle = (GradientDrawable) magTextView.getBackground();


        /** Get the correct backgrund color based on the current earthquake magnitude     */
        int magnitudeColor = getMagnitudeColor(currentQuake.getMagnitude(getContext()));

        /** Set the color on the magnitude circle    */
        magnitudeCircle.setColor(magnitudeColor);

        View baseLayout = quakeListView.findViewById(R.id.quake_item);

        return quakeListView;
    }

    /** Helper method to get the corresponding background color for magnitude circle
     * @param magnitude Magnitude of current earthquake extracted from JSON
     * @return Background color resource ID
     */
    private int getMagnitudeColor(double magnitude) {
        int magnitudeColorResourceId;

        switch((int)magnitude) {
            case 0:
            case 1: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude1);
                    break;
            case 2: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude2);
                    break;
            case 3: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude3);
                    break;
            case 4: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude4);
                    break;
            case 5: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude5);
                    break;
            case 6: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude6);
                    break;
            case 7: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude7);
                    break;
            case 8: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude8);
                    break;
            case 9: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude9);
                    break;
            default: magnitudeColorResourceId = ContextCompat.getColor(getContext(),
                        R.color.magnitude10plus);
        }

        return magnitudeColorResourceId;
    }
}
