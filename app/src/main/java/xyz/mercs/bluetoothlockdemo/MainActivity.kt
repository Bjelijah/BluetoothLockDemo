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
import com.tbruyelle.rxpermissions2.RxPermissions
import xyz.mercs.bluetoothlockdemo.databinding.ActivityMainBinding
import xyz.mercs.bluetoothlockdemo.service.BlueService

class MainActivity : BaseActivity() {

    private val mBinding: ActivityMainBinding by inflate()

    override fun deinitView() {
    }

    override fun getView(): View = mBinding.root

    override fun initView() {
        mBinding.btn1.setOnClickListener {
            RxPermissions(this).request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).subscribe {
                LOG.I("123","openBlue")
                openBlu()
            }
        }
        mBinding.btn2.setOnClickListener {
            mBlueService?.connect()
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