package com.junyenhuang.birdhouse.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.items.Element;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ElementGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Element> mElements;
    private int mHouseID = -1;
    private int threshold = 1000;

    public ElementGridAdapter(ArrayList<Element> events, Context context, int id, int threshold) {
        mElements = events;
        this.context = context;
        mHouseID = id;
        this.threshold = threshold;
    }

    public void updateData(ArrayList<Element> events) {
        if(events != null) {
            mElements.clear();
            mElements.addAll(events);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if(mElements == null) {
            return 0;
        } else {
            return mElements.size();
        }
    }

    @Override
    public Element getItem(int position) {
        return mElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mElements.get(position).getIconID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.card_griditem, null);
            Element e = mElements.get(position);
            // set value into textview
            TextView textView = (TextView)gridView.findViewById(R.id.cvtext1);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.icon123);
            TextView textView2 = (TextView)gridView.findViewById(R.id.cvtext2);

            String name = e.getName();
            String type = e.getType();
            SpannableStringBuilder valuesSB = new SpannableStringBuilder();
            switch(e.getIconID()) {
                case 1: // switch 1
                case 2: // switch 2
                    if(name.contains("1")) {
                        name = context.getResources().getString(R.string.element_sw) + "1";
                        if (e.getValue().toString().equals("0")) {
                            Picasso.with(context).load(R.drawable.off).into(imageView);
                            valuesSB.append(context.getString(R.string.off));
                        } else {
                            Picasso.with(context).load(R.drawable.on).into(imageView);
                            valuesSB.append(context.getString(R.string.on));
                        }
                    } else if(name.contains("2")) {
                        name = context.getResources().getString(R.string.element_sw) + "2";
                        if (e.getValue().toString().equals("0")) {
                            Picasso.with(context).load(R.drawable.off).into(imageView);
                            valuesSB.append(context.getString(R.string.off));
                        } else {
                            Picasso.with(context).load(R.drawable.on).into(imageView);
                            valuesSB.append(context.getString(R.string.on));
                        }
                    }
                    break;
                case 3: // entry
                {
                    Calendar calendar = Calendar.getInstance();
                    int yyyy = calendar.get(Calendar.YEAR);
                    int mm = calendar.get(Calendar.MONTH) + 1;
                    int dd = calendar.get(Calendar.DAY_OF_MONTH);
                    StringBuilder sb = new StringBuilder();
                    sb.append("ENTRY_").append(yyyy)
                            .append(new DecimalFormat("00").format(mm))
                            .append(new DecimalFormat("00").format(dd));

                    String lastTrigger = context.getSharedPreferences("IRS", Context.MODE_PRIVATE).getString("ENTRY_" + mHouseID, "");
                    boolean trigger;
                    if (lastTrigger.equals(sb.toString())) {
                        trigger = true;
                    } else {
                        trigger = false;
                    }

                    name = context.getResources().getString(R.string.element_entry);
                    if (e.getValue().toString().equals("0")) {
                        if (trigger) {
                            Picasso.with(context).load(R.drawable.door_go).into(imageView);
                            String red = context.getString(R.string.logs_all).toUpperCase();
                            Spannable valueString = new SpannableString(red);
                            valueString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, red.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            valuesSB.append(valueString);
                        } else {
                            Picasso.with(context).load(R.drawable.door_off).into(imageView);
                            valuesSB.append(context.getString(R.string.normal));
                        }
                    } else {
                        context.getSharedPreferences("IRS", Context.MODE_PRIVATE).edit()
                                .putString("ENTRY_" + mHouseID, sb.toString()).apply();
                        Picasso.with(context).load(R.drawable.door_on).into(imageView);
                        String red = context.getString(R.string.detected);
                        Spannable valueString = new SpannableString(red);
                        valueString.setSpan(new ForegroundColorSpan(Color.RED), 0, red.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        valuesSB.append(valueString);
                    }
                    break;
                }
                case 4: // intrusion
                {
                    Calendar calendar = Calendar.getInstance();
                    int yyyy = calendar.get(Calendar.YEAR);
                    int mm = calendar.get(Calendar.MONTH) + 1;
                    int dd = calendar.get(Calendar.DAY_OF_MONTH);
                    StringBuilder sb = new StringBuilder();
                    sb.append("INTRUSION_").append(yyyy)
                            .append(new DecimalFormat("00").format(mm))
                            .append(new DecimalFormat("00").format(dd));

                    String lastTrigger = context.getSharedPreferences("IRS", Context.MODE_PRIVATE).getString("INTRUSION_" + mHouseID, "");
                    boolean trigger;
                    if (lastTrigger.equals(sb.toString())) {
                        trigger = true;
                    } else {
                        trigger = false;
                    }

                    name = context.getResources().getString(R.string.element_intrusion);
                    if (e.getValue().toString().equals("0")) {
                        if (trigger) {
                            Picasso.with(context).load(R.drawable.theif_go).into(imageView);
                            String red = context.getString(R.string.logs_all).toUpperCase();
                            Spannable valueString = new SpannableString(red);
                            valueString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, red.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            valuesSB.append(valueString);
                        } else {
                            Picasso.with(context).load(R.drawable.theif).into(imageView);
                            valuesSB.append(context.getString(R.string.normal));
                        }
                    } else {
                        context.getSharedPreferences("IRS", Context.MODE_PRIVATE).edit()
                                .putString("INTRUSION_" + mHouseID, sb.toString()).apply();
                        Picasso.with(context).load(R.drawable.theif_on).into(imageView);
                        String red = context.getString(R.string.detected);
                        Spannable valueString = new SpannableString(red);
                        valueString.setSpan(new ForegroundColorSpan(Color.RED), 0, red.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        valuesSB.append(valueString);
                    }
                    break;
                }
                case 5: // power
                    name = context.getResources().getString(R.string.element_power);
                    if(e.getValue().toString().equals("0")) {
                        Picasso.with(context).load(R.drawable.power_on).into(imageView);
                        valuesSB.append(context.getString(R.string.normal));
                    } else {
                        Picasso.with(context).load(R.drawable.power_off).into(imageView);
                        String red = context.getString(R.string.detected);
                        Spannable valueString = new SpannableString(red);
                        valueString.setSpan(new ForegroundColorSpan(Color.RED), 0, red.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        valuesSB.append(valueString);
                    }
                    break;
                case 6: // humidity
                case 7: // temperature
                case 8: // nh3
                    if(e.getType().equals("com.sensor.temperature")) {
                        name = context.getResources().getString(R.string.element_temp);
                        Picasso.with(context).load(R.drawable.temp).into(imageView);
                        valuesSB.append(e.getValue().toString() + (char)0x00B0 + e.getUnit());
                    } else if(e.getType().equals("com.sensor.humidity")){
                        if(e.getUnit().equals("%")) {
                            name = context.getResources().getString(R.string.element_humidity);
                            Picasso.with(context).load(R.drawable.humidity).into(imageView);
                            valuesSB.append(e.getValue().toString() + e.getUnit());
                        } else if(e.getUnit().toLowerCase().equals("ppm")) {
                            name = context.getResources().getString(R.string.element_nh3);
                            String unit = e.getUnit();
                            String value = e.getValue().toString();
                            if(unit.equals("ppm")) {
                                Picasso.with(context).load(R.drawable.nh3).into(imageView);
                                double ppm = Double.parseDouble(value);
                                if (ppm >= threshold) {
                                    Spannable valueString = new SpannableString(value);
                                    valueString.setSpan(new ForegroundColorSpan(Color.RED), 0, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    valuesSB.append(valueString).append(unit);
                                } else if (ppm > (threshold - 100)) {
                                    Spannable valueString = new SpannableString(value);
                                    valueString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    valuesSB.append(valueString).append(unit);
                                } else {
                                    valuesSB.append(value + unit);
                                }
                            }
                        }
                    }
                    break;
                case 9: // bird count
                    name = context.getResources().getString(R.string.element_sum);
                    Picasso.with(context).load(R.drawable.birds).into(imageView);
                    valuesSB.append(e.getValue().toString()
                            + context.getResources().getString(R.string.element_unit_count));
                    break;
            }
            textView.setText(name);
            textView2.setText(valuesSB, TextView.BufferType.SPANNABLE);
        } else {
            gridView = convertView;
        }
        return gridView;
    }
}
