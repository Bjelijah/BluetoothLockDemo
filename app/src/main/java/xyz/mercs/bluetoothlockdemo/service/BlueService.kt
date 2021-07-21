package xyz.mercs.bluetoothlockdemo.service

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.ArrayMap
import android.util.Log
import com.cbj.sdk.libbase.utils.LOG
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class BlueService:Service() {

    private val mBlueBinder = BlueBinder()
    inner class BlueBinder:Binder(){
        fun getService():BlueService = this@BlueService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBlueBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        deinit()

        return super.onUnbind(intent)
    }

    val mBlueAdapter = BluetoothAdapter.getDefaultAdapter()!!


    var mDevice: BluetoothDevice?=null
    var mmSocket: BluetoothSocket ?=null
    private var mConnThread:ConnectedThread?=null

    var mLeCb = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        LOG.I("123","onLeScan $device $rssi  $scanRecord")

    }

    var mCb = object :ScanCallback(){
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            LOG.I("123","onBatchScanResults   results=$results")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
//            LOG.I("123","onScanResult  $callbackType  $result")
//            LOG.I("123","device name=${result?.scanRecord?.deviceName}")
            var macArr=result?.device?.address?.split(":")?:return
            if (!(macArr[0] == "CB" && macArr[1]=="57" && macArr[2]=="B3" && macArr[3]=="DA" && macArr[4] == "91" && macArr[5]=="FD"))
                return

            mDevice = result?.device
            if (mDevice==null){
                mDevice =  result?.device
            }
            if (mmSocket==null && mDevice!=null){
                LOG.I("123","uuid=${result?.scanRecord?.serviceUuids!![0].uuid}")
//                mmSocket = result?.device.createRfcommSocketToServiceRecord(result?.scanRecord?.serviceUuids!![0].uuid)
//                mmSocket = result?.device.createInsecureRfcommSocketToServiceRecord(result?.scanRecord?.serviceUuids!![0].uuid)
                var bond = result?.device.createBond()

                LOG.I("123","${  mBlueAdapter.bondedDevices}")

                result?.device?.connectGatt(this@BlueService,true,object:BluetoothGattCallback(){
                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        super.onServicesDiscovered(gatt, status)
                        LOG.I("123","$status")
                    }

                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt?,
                        status: Int,
                        newState: Int
                    ) {
                        super.onConnectionStateChange(gatt, status, newState)
//                        BluetoothGatt.STATE_CONNECTED

                        LOG.I("123","newStte=$newState")
                    }
                })

//                LOG.I("123","createSocket ok   bond=$bond")

            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            LOG.E("123","onScanFailed  $errorCode")
        }

    }


    fun init(){
        LOG.I("123","start Scan")
        mBlueAdapter.bluetoothLeScanner.startScan(mCb)
//        mBlueAdapter.startLeScan(mLeCb)
    }

    var mUUID = ""

    fun connect(){



        mmSocket?.use {
            it.connect()
            mConnThread = ConnectedThread(mmSocket!!)
            mConnThread!!.start()
        }
    }

    fun disconnect(){
        mmSocket?.close()
    }




    fun deinit(){
        mBlueAdapter.bluetoothLeScanner.stopScan(mCb)
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("123", "Input stream was disconnected", e)
                    break
                }
                LOG.I("123","size=$numBytes  ${mmBuffer.map { String.format("0x%x",it) }}")


                // Send the obtained bytes to the UI activity.
//                val readMsg = handler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1,
//                    mmBuffer)
//                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("123", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
//                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("toast", "Couldn't send data to the other device")
//                }
//                writeErrorMsg.data = bundle
//                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
//            val writtenMsg = handler.obtainMessage(
//                MESSAGE_WRITE, -1, -1, mmBuffer)
//            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("123", "Could not close the connect socket", e)
            }
        }
    }

}