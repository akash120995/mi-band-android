package com.example.aakash.band;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.RealtimeStepsNotifyListener;
import com.zhaoxiaodan.miband.model.BatteryInfo;
import com.zhaoxiaodan.miband.model.VibrationMode;

public class MainActivity extends AppCompatActivity {

    BluetoothDevice device;
    ScanCallback scanCallback;
    MiBand miBand;
    TextView text;

    private String batteryStatus;
    private int rssi;
    private boolean isRealtime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        miBand = new MiBand(this);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (result.getDevice().getName().equals("MI1A") || result.getDevice().getName().equals("MI")) {
                    progressBar.setVisibility(View.GONE);
                    device = result.getDevice();
                    connectBand();
                    stopListener();
                }

            }
        };
        MiBand.startScan(scanCallback);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Connecting...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if(!isRealtime){
                    stepCount();
                    miBand.enableRealtimeStepsNotify();
                    Toast.makeText(MainActivity.this, "Enabled", Toast.LENGTH_SHORT).show();
                    isRealtime = true;
                } else {
                    miBand.disableRealtimeStepsNotify();
                    Toast.makeText(MainActivity.this, "Disabled", Toast.LENGTH_SHORT).show();
                    isRealtime = false;
                }

            }
        });

        text = (TextView) findViewById(R.id.text);

        Button batteryButton = (Button) findViewById(R.id.batteryButton);
        batteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBattery();
                text.setText(batteryStatus);
            }
        });

        Button rssiButton = (Button) findViewById(R.id.rssiButton);
        rssiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRssi();
                text.setText("" + rssi);
            }
        });
    }

    private void pairBand(){
        miBand.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Paired", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFail(int errorCode, String msg) {

            }
        });
    }

    private void connectBand() {
        miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);
                    }
                });
            }

            @Override
            public void onFail(int errorCode, String msg) {

            }
        });
    }

    private void getBattery() {
        miBand.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo batteryInfo = (BatteryInfo) data;
                batteryStatus = batteryInfo.toString();
            }

            @Override
            public void onFail(int errorCode, String msg) {

            }
        });
    }

    private void getRssi() {
        miBand.readRssi(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                rssi = (int) data;
            }

            @Override
            public void onFail(int errorCode, String msg) {

            }
        });
    }

    private void stepCount() {
        miBand.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(final int steps) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText("" + steps);
                    }
                });
            }
        });
    }

    private void stopListener() {
        MiBand.stopScan(scanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_pair) {
            pairBand();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
