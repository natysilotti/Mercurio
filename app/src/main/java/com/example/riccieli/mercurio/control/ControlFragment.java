package com.example.riccieli.mercurio.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.riccieli.mercurio.BluetoothService;
import com.example.riccieli.mercurio.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.riccieli.mercurio.BluetoothService.COMBOBOX_CMD;
import static com.example.riccieli.mercurio.BluetoothService.DISPLAY_CMD;
import static com.example.riccieli.mercurio.BluetoothService.TEXT_DISPLAY;
import static com.example.riccieli.mercurio.BluetoothService.ITENS_OF_COMBOBOX;
import static com.example.riccieli.mercurio.BluetoothService.EXTRA_DATA_TEXT;
import static com.example.riccieli.mercurio.BluetoothService.LABEL_COMBOBOX;
import static com.example.riccieli.mercurio.BluetoothService.LABEL_DISPLAY;
import static com.example.riccieli.mercurio.BluetoothService.LABEL_RANGE;
import static com.example.riccieli.mercurio.BluetoothService.LABEL_SWITCH;
import static com.example.riccieli.mercurio.BluetoothService.MIN_MAX_RANGE;
import static com.example.riccieli.mercurio.BluetoothService.NUMBER_OF_ITENS_COMBOBOX;
import static com.example.riccieli.mercurio.BluetoothService.SWITCH_CMD;
import static com.example.riccieli.mercurio.BluetoothService.NUMBER_OF_SWITCHES;
import static com.example.riccieli.mercurio.BluetoothService.TEXT_CMD;
import static com.example.riccieli.mercurio.BluetoothService.RANGE_CMD;
import static com.example.riccieli.mercurio.BluetoothService.UPDATE_DISPLAY_CMD;
import static com.example.riccieli.mercurio.BluetoothService.SET_RANGE_CMD;
import static com.example.riccieli.mercurio.BluetoothService.UPDATE_RANGE;
import static com.example.riccieli.mercurio.BluetoothService.UPDATE_TEXT_DISPLAY;
import static com.example.riccieli.mercurio.BluetoothService.VALUES_SWITCH;

public class ControlFragment extends Fragment {

    private View view;
    private String TAG = "ControlFragment";
    private static String IBINDER = "ibinder";
    private int min;
    private int max;

    private Boolean TURN_ON_OFF = false;
    private Boolean FlagText = false;
    private Boolean FlagRange = false;

    private BluetoothService mBluetoothService;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SWITCH_CMD.equals(action)) {
                String data = intent.getStringExtra(NUMBER_OF_SWITCHES);
                Integer switches = Integer.parseInt(data);

                String labels = intent.getStringExtra(LABEL_SWITCH);
                String label[] = labels.split(",");

                String values = intent.getStringExtra(VALUES_SWITCH);
                String value[] = values.split(",");

                switch (switches) {
                    case 3:
                        view.findViewById(R.id.layout_switch_3).setVisibility(View.VISIBLE);
                        TextView textSwitch3= (TextView) view.findViewById(R.id.tv_switch_3);
                        textSwitch3.setText(label[2]);
                        TextView textSwitch3ON= (TextView) view.findViewById(R.id.tv_switch_3_ON);
                        textSwitch3ON.setText(value[5]);
                        TextView textSwitch3OFF= (TextView) view.findViewById(R.id.tv_switch_3_OFF);
                        textSwitch3OFF.setText(value[4]);
                    case 2:
                        view.findViewById(R.id.layout_switch_2).setVisibility(View.VISIBLE);
                        TextView textSwitch2 = (TextView) view.findViewById(R.id.tv_switch_2);
                        textSwitch2.setText(label[1]);
                        TextView textSwitch2ON= (TextView) view.findViewById(R.id.tv_switch_2_ON);
                        textSwitch2ON.setText(value[3]);
                        TextView textSwitch2OFF= (TextView) view.findViewById(R.id.tv_switch_2_OFF);
                        textSwitch2OFF.setText(value[2]);
                    case 1:
                        view.findViewById(R.id.layout_switch_1).setVisibility(View.VISIBLE);
                        TextView textSwitch1 = (TextView) view.findViewById(R.id.tv_switch_1);
                        textSwitch1.setText(label[0]);
                        TextView textSwitch1ON= (TextView) view.findViewById(R.id.tv_switch_1_ON);
                        textSwitch1ON.setText(value[1]);
                        TextView textSwitch1OFF= (TextView) view.findViewById(R.id.tv_switch_1_OFF);
                        textSwitch1OFF.setText(value[0]);
                    default:
                        break;
                }
            }
            else if(TEXT_CMD.equals(action)){
                view.findViewById(R.id.tv_label_text).setVisibility(View.VISIBLE);
                view.findViewById(R.id.input_text).setVisibility(View.VISIBLE);
                view.findViewById(R.id.button_send).setVisibility(View.VISIBLE);
                String data = intent.getStringExtra(EXTRA_DATA_TEXT);
                TextView textViewText = (TextView) view.findViewById(R.id.tv_label_text);
                textViewText.setText(data);
            }
            else if(COMBOBOX_CMD.equals(action)){
                view.findViewById(R.id.tv_label_box).setVisibility(View.VISIBLE);
                view.findViewById(R.id.combo_box).setVisibility(View.VISIBLE);
                String data = intent.getStringExtra(LABEL_COMBOBOX);
                TextView textViewBox = (TextView) view.findViewById(R.id.tv_label_box);
                textViewBox.setText(data);

                Spinner comboBox = (Spinner) view.findViewById(R.id.combo_box);

                // Spinner click listener

                String dataNumberOfItens = intent.getStringExtra(NUMBER_OF_ITENS_COMBOBOX);
                Integer numberOfItens = Integer.parseInt(dataNumberOfItens);

                String dataItens = intent.getStringExtra(ITENS_OF_COMBOBOX);
                String itens[] = dataItens.split(",");

               // Spinner Drop down elements
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < numberOfItens; i++) {
                    list.add(itens[i]);
                }
                // Creating adapter for spinner
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);

                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // attaching data adapter to spinner
                comboBox.setAdapter(dataAdapter);
            }
            else if(RANGE_CMD.equals(action)) {
                view.findViewById(R.id.layout_range).setVisibility(View.VISIBLE);
                view.findViewById(R.id.button_set).setVisibility(View.VISIBLE);
                String data = intent.getStringExtra(LABEL_RANGE);
                TextView textViewRange = (TextView) view.findViewById(R.id.tv_label_range);
                textViewRange.setText(data);

                String values = intent.getStringExtra(MIN_MAX_RANGE);
                String value[] = values.split(",");

                TextView textRangeMax = (TextView) view.findViewById(R.id.tv_label_range_max);
                max =  Integer.valueOf(value[1]);
                textRangeMax.setText(value[1]);

                TextView textRangeMin = (TextView) view.findViewById(R.id.tv_label_range_min);
                min = Integer.valueOf(value[0]);
                textRangeMin.setText(value[0]);
            }
            else if(SET_RANGE_CMD.equals(action)){
                String data = intent.getStringExtra(UPDATE_RANGE);
                TextView textValueRange = (TextView) view.findViewById(R.id.tv_label_range_value);
                textValueRange.setText(data);
                SeekBar range = (SeekBar) view.findViewById(R.id.range);
                range.setProgress(Integer.valueOf(data));
                range.setMax(max);
            }

            else if(DISPLAY_CMD.equals(action)) {
                view.findViewById(R.id.layout_display).setVisibility(View.VISIBLE);
                view.findViewById(R.id.button_update).setVisibility(View.VISIBLE);
                String data = intent.getStringExtra(LABEL_DISPLAY);
                TextView textViewDisplay = (TextView) view.findViewById(R.id.tv_display_label);
                textViewDisplay.setText(data);

                String text = intent.getStringExtra(TEXT_DISPLAY);
                TextView textToDisplay = (TextView) view.findViewById(R.id.tv_display);
                textToDisplay.setText(text);
            }

            else if(UPDATE_DISPLAY_CMD.equals(action)) {
                String text = intent.getStringExtra(UPDATE_TEXT_DISPLAY);
                TextView textToDisplay = (TextView) view.findViewById(R.id.tv_display);
                textToDisplay.setText(text);
            }

        }
    };

    public ControlFragment() {
        // Required empty public constructor
    }

    public static ControlFragment newInstance(IBinder mBinderService) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        args.putBinder(IBINDER, mBinderService);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
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
        intentFilter.addAction(SWITCH_CMD);
        intentFilter.addAction(TEXT_CMD);
        intentFilter.addAction(COMBOBOX_CMD);
        intentFilter.addAction(RANGE_CMD);
        intentFilter.addAction(DISPLAY_CMD);
        intentFilter.addAction(UPDATE_DISPLAY_CMD);
        intentFilter.addAction(SET_RANGE_CMD);
        return intentFilter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_control, container, false);

        view.findViewById(R.id.layout_switch_1).setVisibility(View.GONE);
        view.findViewById(R.id.layout_switch_2).setVisibility(View.GONE);
        view.findViewById(R.id.layout_switch_3).setVisibility(View.GONE);
        view.findViewById(R.id.input_text).setVisibility(View.GONE);
        view.findViewById(R.id.tv_label_text).setVisibility(View.GONE);
        view.findViewById(R.id.tv_label_box).setVisibility(View.GONE);
        view.findViewById(R.id.combo_box).setVisibility(View.GONE);
        view.findViewById(R.id.layout_range).setVisibility(View.GONE);
        view.findViewById(R.id.layout_display).setVisibility(View.GONE);
        view.findViewById(R.id.button_send).setVisibility(View.GONE);
        view.findViewById(R.id.button_update).setVisibility(View.GONE);
        view.findViewById(R.id.button_set).setVisibility(View.GONE);

        Switch mySwitch1 = (Switch) view.findViewById(R.id.switch_1);
        mySwitch1.setChecked(false);
        mySwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton switch_1, boolean isChecked) {
                if (isChecked){
                    mBluetoothService.sendData("SWITCH1#ON;");
                } else{
                    mBluetoothService.sendData("SWITCH1#OFF;");
                }
            }
        });

        Switch mySwitch2 = (Switch) view.findViewById(R.id.switch_2);
        mySwitch2.setChecked(false);
        mySwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton switch_2, boolean isChecked) {
                if (isChecked){
                    mBluetoothService.sendData("SWITCH2#ON;");
                } else{
                    mBluetoothService.sendData("SWITCH2#OFF;");
                }
            }
        });

        Switch mySwitch3 = (Switch) view.findViewById(R.id.switch_3);
        mySwitch3.setChecked(false);
        mySwitch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton switch_3, boolean isChecked) {
                if (isChecked){
                    mBluetoothService.sendData("SWITCH3#ON;");
                } else{
                    mBluetoothService.sendData("SWITCH3#OFF;");
                }
            }
        });

        final Spinner comboBox = (Spinner) view.findViewById(R.id.combo_box);
        comboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                String str = adapter.getItemAtPosition(position).toString();
                String dataCOMBO = "SELECTED#" + str + ";";
                mBluetoothService.sendData(dataCOMBO);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        view.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FlagText) {
                            EditText editTextText = (EditText) view.findViewById(R.id.input_text);
                            String str = editTextText.getText().toString();
                            String dataTEXT = "TEXT#" + str + ";";
                            mBluetoothService.sendData(dataTEXT);
                        }

                    /*    if (TURN_ON_OFF) {
                            mBluetoothService.sendData("H");
                        } else {
                            mBluetoothService.sendData("L");
                        }
                        TURN_ON_OFF = !TURN_ON_OFF;*/
                    }
                });

        view.findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothService.sendData("UPDATE#OK;");
            }
        });

        view.findViewById(R.id.button_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothService.sendData("SET#OK;");
            }
        });


        // Inflate the layout for this fragment
        return view;
    }



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
