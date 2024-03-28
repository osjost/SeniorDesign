package com.example.cytocheck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestAdapter extends ArrayAdapter<RequestInfo> {
    public RequestAdapter(Context context, List<RequestInfo> requestList) {
        super(context, 0, requestList);
        // Sort the requestList based on messageType
        Collections.sort(requestList, new Comparator<RequestInfo>() {
            @Override
            public int compare(RequestInfo request1, RequestInfo request2) {
                // Compare the messageType field
                return request1.getMessageType().equals("emergency") ? -1 : 1;
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RequestInfo requestInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_request, parent, false);
        }

        // Lookup view for data population
        TextView textViewRequestName = convertView.findViewById(R.id.textViewRequestName);

        // Populate the data into the template view using the data object
        textViewRequestName.setText("Message id: " + requestInfo.getId());

        // Return the completed view to render on screen
        return convertView;
    }
}
