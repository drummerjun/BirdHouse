package com.junyenhuang.birdhouse;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.DatePicker;

import java.text.DecimalFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private Context context;
    private int mYear;
    private int mMonth;
    private int mDay;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mYear = year;
        mMonth = month;
        mDay = dayOfMonth;

        StringBuilder sb = new StringBuilder();
        sb.append("/" + String.valueOf(mYear)
                + "/" + new DecimalFormat("00").format(mMonth + 1)
                + "/" + new DecimalFormat("00").format(mDay)
        );
        Intent intent = new Intent(Constants.ACTION_DATE_SELECTED);
        intent.putExtra(Constants.EXTRA_DATE, sb.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
