package com.example.cytocheck;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PatientAdapter extends ArrayAdapter<PatientInfo> {

    public PatientAdapter(Context context, List<PatientInfo> patientList) {
        super(context, 0, patientList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PatientInfo patientInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_patient, parent, false);
        }

        // Lookup view for data population
        TextView textViewPatientName = convertView.findViewById(R.id.textViewPatientName);

        // Populate the data into the template view using the data object
        textViewPatientName.setText(patientInfo.getName() + " with Patient id: " + patientInfo.getId());


        // Return the completed view to render on screen
        return convertView;
    }
}