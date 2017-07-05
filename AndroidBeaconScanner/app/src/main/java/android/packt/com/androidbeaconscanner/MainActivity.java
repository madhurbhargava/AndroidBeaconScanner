package android.packt.com.androidbeaconscanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import google.UrlDevice;

public class MainActivity extends AppCompatActivity implements BleUrlDeviceDiscoverer.DeviceReporter {


    private String TAG = "BeaconScanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BleUrlDeviceDiscoverer deviceDiscoverer = new BleUrlDeviceDiscoverer(this);
        deviceDiscoverer.startScanImpl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void reportUrlDevice(UrlDevice device) {
        ((TextView)findViewById(R.id.textView2)).setText("Received Beacon broadcast: "+device.getUrl());
    }

}
