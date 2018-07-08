package com.prisyazhnuy.bluetoothvisualizer

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import android.content.BroadcastReceiver
import android.content.IntentFilter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val TAG = "MainActivity"
    }

    private val bluetoothAdapter by lazy { (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }

    val scanResults = mutableMapOf<String, BluetoothDevice>()

    private val leScanCallback = BluetoothAdapter.LeScanCallback { p0, p1, p2 -> addScanResult(p0, p1, p2) }

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result) {
                addScanResult(device, rssi, scanRecord.bytes)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach {
                with(it) {
                    addScanResult(device, rssi, scanRecord.bytes)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scanning Failed $errorCode")
        }
    }

    private val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

    private val bReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                // Create a new device item
                addScanResult(device, rssi.toInt(), byteArrayOf())
            }
        }
    }

    private fun addScanResult(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
        val deviceAddress = device.address
        Log.d(TAG, "addScanResult $deviceAddress  $device")
        Toast.makeText(this@MainActivity, "find device $deviceAddress  $device", Toast.LENGTH_LONG).show()
        scanResults[deviceAddress] = device
        val iBeacon = IBeacon.fromScanData(scanRecord, rssi)
        graph.addItem(iBeacon.getAccuracy().toInt())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        with(RxPermissions(this)) {
            request(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe { granted ->
                        if (granted) startBluetoothDiscovery() else finish()
                    }
        }
        registerReceiver(bReciever, filter)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_ENABLE_BT -> startBluetoothDiscovery()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
//        bluetoothAdapter.stopLeScan(leScanCallback)
        bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(bReciever)
        super.onStop()
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun startBluetoothScanner2() {
        if (bluetoothAdapter.isEnabled) bluetoothAdapter.startLeScan(leScanCallback) else enableBluetooth()
    }

    private fun startBluetoothScanner() {
        if (bluetoothAdapter.isEnabled) {
            val scanFilters = listOf(ScanFilter.Builder().build())
            val settings = ScanSettings.Builder()//.build()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(0)
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        }
                    }.build()
            bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
        } else {
            enableBluetooth()
        }
    }

    private fun startBluetoothDiscovery() {
        if (bluetoothAdapter.isEnabled) bluetoothAdapter.startDiscovery() else enableBluetooth()
    }
}
