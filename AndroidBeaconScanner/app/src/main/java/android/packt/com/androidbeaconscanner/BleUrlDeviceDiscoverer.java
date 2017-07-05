package android.packt.com.androidbeaconscanner;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.SystemClock;

import org.json.JSONException;

import java.util.List;

import google.EddystoneBeacon;
import google.ScanRecord;
import google.UrlDevice;

class BleUrlDeviceDiscoverer implements BluetoothAdapter.LeScanCallback {

    public interface DeviceReporter {
        void reportUrlDevice(UrlDevice device);
    }
    private DeviceReporter reporter;

    private static final String TAG = BleUrlDeviceDiscoverer.class.getSimpleName();
    private static final ParcelUuid URIBEACON_SERVICE_UUID =
            ParcelUuid.fromString("0000FED8-0000-1000-8000-00805F9B34FB");
    private static final ParcelUuid EDDYSTONE_URL_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBluetoothAdapter;
    private Parcelable[] mScanFilterUuids;
    private Context mContext;
    private long mScanStartTime;

    private static final String SCANTIME_KEY = "scantime";
    private static final String TYPE_KEY = "type";
    private static final String PUBLIC_KEY = "public";
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String RSSI_KEY = "rssi";
    private static final String TXPOWER_KEY = "tx";


    public BleUrlDeviceDiscoverer(MainActivity mainActivity) {
        mContext = mainActivity;
        reporter = mainActivity;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(
                Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mScanFilterUuids = new ParcelUuid[]{EDDYSTONE_URL_SERVICE_UUID};
    }

    private boolean leScanMatches(ScanRecord scanRecord) {
        if (mScanFilterUuids == null) {
            return true;
        }
        List services = scanRecord.getServiceUuids();
        if (services != null) {
            for (Parcelable uuid : mScanFilterUuids) {
                if (services.contains(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanBytes) {
        ScanRecord scanRecord = ScanRecord.parseFromBytes(scanBytes);
        if (!leScanMatches(scanRecord)) {
            return;
        }

        byte[] urlServiceData = scanRecord.getServiceData(EDDYSTONE_URL_SERVICE_UUID);
        byte[] uriServiceData = scanRecord.getServiceData(URIBEACON_SERVICE_UUID);

        EddystoneBeacon beacon = EddystoneBeacon.parseFromServiceData(urlServiceData, uriServiceData);
        if (beacon == null) {
            return;
        }
        UrlDevice urlDevice = null;
        try {
            urlDevice = createUrlDeviceBuilder(TAG + device.getAddress() + beacon.getUrl(),
                    beacon.getUrl())
                    .setRssi(rssi)
                    .setTxPower(beacon.getTxPowerLevel())
                    .setDeviceType("ble")
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reporter.reportUrlDevice(urlDevice);
    }


    public synchronized void startScanImpl() {

        mScanStartTime = SystemClock.elapsedRealtime();
        mBluetoothAdapter.startLeScan(this);
    }


    public synchronized void stopScanImpl() {

        mBluetoothAdapter.stopLeScan(this);
    }

    protected UrlDeviceBuilder createUrlDeviceBuilder(String id, String url) throws JSONException {
        return new UrlDeviceBuilder(id, url)
                .setScanTimeMillis(SystemClock.elapsedRealtime() - mScanStartTime);
    }

    static class UrlDeviceBuilder extends UrlDevice.Builder {

        /**
         * Constructor for the UrlDeviceBuilder.
         * @param id The id of the UrlDevice.
         * @param url The url of the UrlDevice.
         */
        public UrlDeviceBuilder(String id, String url) {
            super(id, url);
        }

        /**
         * Set the device type.
         * @return The builder with type set.
         */
        public UrlDeviceBuilder setDeviceType(String type) throws JSONException {
            addExtra(TYPE_KEY, type);
            return this;
        }

        /**
         * Setter for the ScanTimeMillis.
         * @param timeMillis The scan time of the UrlDevice.
         * @return The builder with ScanTimeMillis set.
         */
        public UrlDeviceBuilder setScanTimeMillis(long timeMillis) throws JSONException {
            addExtra(SCANTIME_KEY, timeMillis);
            return this;
        }

        /**
         * Set the public key to false.
         * @return The builder with public set to false.
         */
        public UrlDeviceBuilder setPrivate() throws JSONException {
            addExtra(PUBLIC_KEY, false);
            return this;
        }

        /**
         * Set the public key to true.
         * @return The builder with public set to true.
         */
        public UrlDeviceBuilder setPublic() throws JSONException {
            addExtra(PUBLIC_KEY, true);
            return this;
        }

        /**
         * Set the title.
         * @param title corresonding to UrlDevice.
         * @return The builder with title
         */
        public UrlDeviceBuilder setTitle(String title) throws JSONException {
            addExtra(TITLE_KEY, title);
            return this;
        }

        /**
         * Set the description.
         * @param description corresonding to UrlDevice.
         * @return The builder with description
         */
        public UrlDeviceBuilder setDescription(String description) throws JSONException {
            addExtra(DESCRIPTION_KEY, description);
            return this;
        }

        /**
         * Setter for the RSSI.
         * @param rssi The RSSI of the UrlDevice.
         * @return The builder with RSSI set.
         */
        public UrlDeviceBuilder setRssi(int rssi) throws JSONException {
            addExtra(RSSI_KEY, rssi);
            return this;
        }

        /**
         * Setter for the TX power.
         * @param txPower The TX power of the UrlDevice.
         * @return The builder with TX power set.
         */
        public UrlDeviceBuilder setTxPower(int txPower) throws JSONException {
            addExtra(TXPOWER_KEY, txPower);
            return this;
        }
    }
}
