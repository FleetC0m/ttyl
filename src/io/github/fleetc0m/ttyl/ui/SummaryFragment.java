package io.github.fleetc0m.ttyl.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import io.github.fleetc0m.ttyl.R;

/**
 * Holding a series of cards which briefly display the past activities and responses.
 */
public class SummaryFragment extends Fragment {

    private ListView mSummaryList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.summary_fragment, container, false);
        mSummaryList = (ListView) view.findViewById(R.id.summary_list);
        mSummaryList.setAdapter(new DemoAdapter());
        return view;
    }

    private void populateSummaryCards(LayoutInflater inflater, ListView summaryList) {
        for (int i = 0; i < 3; i++) {
            View cardView = inflater.inflate(R.layout.summary_card, null, false);
            TextView titleView = (TextView) cardView.findViewById(R.id.summary_card_title_view);
            titleView.setText("You were busy from 9:00am to 3:00pm today");
            TextView moreInfoView = (TextView) cardView.findViewById(R.id.summary_card_more_info);
            moreInfoView.setText(
                    "You missed 3 SMS messages from Leo\n1 phone call from your boss.");
            summaryList.addView(cardView);
        }
    }

    private class DemoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View cardView = getActivity().getLayoutInflater().inflate(
                    R.layout.summary_card, null, false);
            TextView titleView = (TextView) cardView.findViewById(R.id.summary_card_title_view);
            titleView.setText("You were busy");
            TextView moreInfoView = (TextView) cardView.findViewById(R.id.summary_card_more_info);
            moreInfoView.setText(
                    "From 9:00am to 3:00pm\nYou missed 3 SMS messages from Leo\n1 phone call from your boss.");
            Button button = (Button) cardView.findViewById(R.id.summary_card_open_sms_button);
            button.setText("Open in Hangouts");
            return cardView;
        }
    }
}
