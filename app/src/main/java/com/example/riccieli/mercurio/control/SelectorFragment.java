/*
package com.example.riccieli.mercurio.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.riccieli.mercurio.BluetoothService;
import com.example.riccieli.mercurio.R;

import java.nio.channels.Selector;

import static android.content.ContentValues.TAG;
import static com.example.riccieli.mercurio.BluetoothService.SWITCH_CMD;


public class SelectorFragment extends Fragment {
    private static String IBINDER = "ibinder";
    private BluetoothService mBluetoothService;

    public static SelectorFragment newInstance(IBinder mBinderService) {
        SelectorFragment fragment = new SelectorFragment();
        Bundle args = new Bundle();
        args.putBinder(IBINDER, mBinderService);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            IBinder mBinderService = getArguments().getBinder(IBINDER);
            mBluetoothService =  ((BluetoothService.LocalBinder) mBinderService).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Service is not initialized!");
            } else {
                Log.d(TAG, "Service is initialized!");
            }
        }
    }
    private IntentFilter makeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
       // intentFilter.addAction(SWITCH_CMD);
        return intentFilter;
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SWITCH_CMD.equals(action)) {

            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().registerReceiver(mReceiver, makeReceiver());
    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDetach();
    }
}
*/
