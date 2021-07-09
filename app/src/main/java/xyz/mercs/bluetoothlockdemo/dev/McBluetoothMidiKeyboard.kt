package xyz.mercs.bluetoothlockdemo.dev

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.set

class McBluetoothMidiKeyboard(var mContext:Context,var mCb:IMidiEvent) {
    interface IMidiEvent{
        fun onConnect()
        fun onDisconnect()
        fun onScan(names:ArrayMap<String,String>)
    }

    var mIsConnect:Boolean = false


    val ACTION_GATT_DISCONNECTED =
        "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    val UUID_HEART_RATE_MEASUREMENT =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val mDevs = ArrayMap<String,String>()
    var mBluetoothDeviceAddress:String?=null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private val mGattCallback = object :BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                mBluetoothGatt?.discoverServices()
                mCb?.onConnect()
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                mCb?.onDisconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            Log.e("123","onServicesDiscovered   status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS){
                GetGattServices(getSupportedGattServices())
            }
        }
    }
    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return  mBluetoothGatt?.services
    }
    private fun GetGattServices(gattServices: List<BluetoothGattService?>?) {
        if (gattServices == null) return
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList()
        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            gattServiceData.add(currentServiceData)
            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService!!.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()
            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
        if (mGattCharacteristics != null) {
            characteristic = mGattCharacteristics.get(2).get(1) //对应硬件character
            val charaProp: Int = characteristic!!.getProperties()
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) { // If there is an active notification on a characteristic, clear
// it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    setCharacteristicNotification(
                        mNotifyCharacteristic!!, false
                    )
                    mNotifyCharacteristic = null
                }
                readCharacteristic(characteristic)
            }
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
                setCharacteristicNotification(
                    characteristic!!, true
                )
            }
        }
    }
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mAdapter == null || mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mAdapter == null || mBluetoothGatt == null) {
            Log.e("123","writeCharacter error BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.writeCharacteristic(characteristic)
        //fixme by cbj add
    }
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        if (mAdapter == null || mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    private val mScanCallback = object :ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            var dev = result?.device
            Log.i("123","onScanResult  dev=${dev?.address}  name=${dev?.name}")
            if (TextUtils.isEmpty(dev?.address))return
            if (!mDevs.contains(dev?.address)){
                mDevs[dev?.address] = dev?.name?:dev?.address
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("123","onScanFailed $errorCode")
            if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                mAdapter?.disable()
                mAdapter?.enable()
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.e("123","onBatchScanResults")
        }
    }

    private var mAdapter:BluetoothAdapter?=null


    fun init():McBluetoothMidiKeyboard{
        var mgr = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mAdapter = mgr.adapter
        return this
    }

    fun isBlueEnable():Boolean{
        return mAdapter?.isEnabled?:false
    }

    fun enableBlue(){
        mAdapter?.enable()
    }

    fun scan(deviceName:String?):McBluetoothMidiKeyboard{
//        Log.e("123","enable= ${mAdapter?.isEnabled}")
        if (mAdapter==null)return this
        mDevs.clear()
        var scanner = mAdapter?.bluetoothLeScanner
        var filters = ArrayList<ScanFilter>()
        if (deviceName!=null) {
            var filter = ScanFilter.Builder()
                .setDeviceName(deviceName)
                .build()
            filters.add(filter)
        }
        scanner!!.startScan(filters,
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build(),
            mScanCallback)
        Observable.timer(5,TimeUnit.SECONDS)
            .subscribe {
//                Log.e("123","stop scan")
                scanner!!.stopScan(mScanCallback)


                mCb?.onScan(mDevs)
            }
        return this
    }

    fun connect(address:String):Boolean{
        // Previously connected device.  Try to reconnect.
//        Log.i("123", "Connect  address = $address")
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            return mBluetoothGatt?.connect()?:false
        }
        val device = mAdapter?.getRemoteDevice(address) ?: return false
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback)
        mBluetoothDeviceAddress = address
        return true
    }

    fun disconnect(){
        mBluetoothGatt?.disconnect()
        mCb?.onDisconnect()

    }



    fun writeData(a: Int): Boolean {
        val b = ByteArray(4)
        for (i in 0..3) {
            b[i] = (a shr i * 8 and 0xff).toByte()
        }
//        Log.i(
//            "123",
//            "cbjwrite data " + String.format(
//                "0x%x  0x%x   0x%x  0x%x",
//                b[0],
//                b[1],
//                b[2],
//                b[3]
//            )+"   characteristic=$characteristic"
//        )
        return if (characteristic != null) {
            characteristic!!.value = b //a
            //往蓝牙模块写入数据
//            Log.e("123","writeCharacteristic")
            writeCharacteristic(characteristic)
            true
        } else false
    }


}