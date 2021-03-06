package xyz.mercs.bluetoothlockdemo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.ArrayMap
import android.view.View
import android.widget.Button
import com.cbj.sdk.libbase.utils.LOG
import com.cbj.sdk.libui.mvp.BaseActivity
import com.cbj.sdk.libui.mvp.inflate
import com.inuker.bluetooth.library.BluetoothClient
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.mercs.bluetoothlockdemo.databinding.ActivityMainBinding
import xyz.mercs.bluetoothlockdemo.service.BlueService
import xyz.mercs.bluetoothlockdemo.util.Util
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    private val mTestMac = "C4:56:D2:3F:99:3B"

    private val mBinding: ActivityMainBinding by inflate()

    override fun deinitView() {
    }

    override fun getView(): View = mBinding.root

    override fun initView() {
        BLEManager.sInstance.init(this,mTestMac){
            LOG.I("123","we read ${Util.byte2HexStr(it)}")
            parseMsg(it)
        }
        mBinding.btn1.setOnClickListener {
            RxPermissions(this).request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).subscribe {
                LOG.I("123","openBlue")
//                openBlu()
                BLEManager.sInstance.connect()
            }
        }
        mBinding.btn2.setOnClickListener {
//            mBlueService?.connect()
            var tokenReq = Util.sendGetTokenProtocol()

            LOG.I("123","tokenReq=${Util.byte2HexStr(tokenReq)}")
            BLEManager.sInstance.write(tokenReq)
           // BLEManager.sInstance.read()
        }
        mBinding.btn3.setOnClickListener {
            BLEManager.sInstance.write(Util.openLock(token))
        }
        mBinding.btn4.setOnClickListener {

            RxPermissions(this).request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            ).subscribe {
                startActivityForResult(Intent(this,QRcodeScanActivity::class.java),QRcodeScanActivity.REQ)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QRcodeScanActivity.REQ && resultCode == QRcodeScanActivity.RES){
            var qr = data?.getStringExtra("qr")
            LOG.I("123","qr=$qr")
            BLEManager.sInstance.mac = qr
            BLEManager.sInstance.connect()
            doTaskDelay({
                var tokenReq = Util.sendGetTokenProtocol()
                BLEManager.sInstance.write(tokenReq)
            },1000){
                doTaskDelay({
                    BLEManager.sInstance.write(Util.openLock(token))
                },1000){

                }
            }

        }
    }

    private fun doTaskDelay(task:()->Unit,delayMs:Long,cb:()->Unit){
        Observable.timer(delayMs,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                task()
                cb()
            },{
                it.printStackTrace()
            })
    }


    var token = ByteArray(4)

    fun parseMsg(bs:ByteArray){
        if ((bs[0] == 0x06.toByte()) && (bs[1] == 0x02.toByte())){
            LOG.I("123","token = ${String.format("0x%x",bs[3])} ${String.format("0x%x",bs[4])}" +
                    " ${String.format("0x%x",bs[5])} ${String.format("0x%x",bs[6])}")
            token[0] = bs[3]
            token[1] = bs[4]
            token[2] = bs[5]
            token[3] = bs[6]

//            token[0] = bs[6]
//            token[1] = bs[5]
//            token[2] = bs[4]
//            token[3] = bs[3]

        }

    }

















    fun openBlu(){
        LOG.I("123","do open blue")
        bindService(Intent(this,BlueService::class.java),mConn,Context.BIND_AUTO_CREATE)
    }
    private var mBlueService:BlueService?=null
    private val mConn = object:ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            LOG.I("123","onServiceConnected")
            mBlueService = (service as BlueService.BlueBinder).getService()

            mBlueService?.init()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            mBlueService = null
        }

    }

}