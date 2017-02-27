package com.example.riccieli.mercurio.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.riccieli.mercurio.R;

import java.util.ArrayList;

public class DeviceAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<BluetoothDevice> mData;

    public DeviceAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mData = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        mData.add(device);
        this.notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        this.notifyDataSetChanged();
    }

    public Boolean contains(BluetoothDevice device) {
        return mData.contains(device);
    }

    @Override
    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.item_device, null);
            holder 				= new ViewHolder();

            holder.device_name      = (TextView) convertView.findViewById(R.id.textView_name);
            holder.device_address = (TextView) convertView.findViewById(R.id.textView_address);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);
        String name = device.getName();
        if (name == null) {
            name = "Nome desconhecido ";
            holder.device_name.setTextColor(Color.RED);
        }

        holder.device_name.setText(name);
        holder.device_address.setText(device.getAddress());
        return convertView;
    }

    private static class ViewHolder {
        TextView device_name;
        TextView device_address;
    }

}
