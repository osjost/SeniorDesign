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
    /* This class is used in conjunction with the Provider Activity and the Request Info class
    *  to adapt each inbox request to a scroll view on the provider's GUI */
    public RequestAdapter(Context context, List<RequestInfo> requestList) {
        super(context, 0, requestList);
        // Sort the requestList based on messageType
        Collections.sort(requestList, new Comparator<RequestInfo>() {
            @Override
            public int compare(RequestInfo request1, RequestInfo request2) {
                // Compare the messageType field
                String messageType1 = request1.getMessageType();
                String messageType2 = request2.getMessageType();

                if (messageType1.equals("emergency")) {
                    // If request1 is emergency, it should come before request2
                    return -1;
                } else if (messageType2.equals("emergency")) {
                    // If request2 is emergency, it should come before request1
                    return 1;
                } else if (messageType1.equals("breach")) {
                    // If request1 is breach, it should come before request2
                    return -1;
                } else if (messageType2.equals("breach")) {
                    // If request2 is breach, it should come before request1
                    return 1;
                } else {
                    // For all other cases, use natural ordering
                    return 0;
                }
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
        if (requestInfo.getMessageType().equals("breach")) {
            textViewRequestName.setText("Threshold Breach for Patient id: " + requestInfo.getSenderID());
        }
        else if (requestInfo.getMessageType().equals("emergency")) {
            textViewRequestName.setText("Emergency with Patient id: " + requestInfo.getSenderID());
        }
        else {
            textViewRequestName.setText("Association Request for Patient id: " + requestInfo.getSenderID());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
