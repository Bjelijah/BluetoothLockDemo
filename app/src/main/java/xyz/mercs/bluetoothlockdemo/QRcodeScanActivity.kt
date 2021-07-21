package xyz.mercs.bluetoothlockdemo

import android.content.Intent
import android.os.Vibrator
import android.view.View
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.cbj.sdk.libbase.utils.LOG
import com.cbj.sdk.libui.mvp.BaseActivity
import com.cbj.sdk.libui.mvp.inflate
import xyz.mercs.bluetoothlockdemo.databinding.MiddleQrcodeScanActivityBinding

/**
 * @date 2021/7/21
 * @author elijah
 * @Description
 */
class QRcodeScanActivity:BaseActivity() ,QRCodeView.Delegate{

    companion object {
        const val REQ = 100
        const val RES = 101
    }

    private val mBinding:MiddleQrcodeScanActivityBinding by inflate()
    override fun deinitView() {
    }

    override fun getView(): View = mBinding.root

    override fun initView() {
        mBinding.zxingview.also {
            it.setDelegate(this)
            it.startCamera()
            it.scanBoxView.isOnlyDecodeScanBoxArea = false
            it.changeToScanQRCodeStyle()
            it.setType(BarcodeType.TWO_DIMENSION,null)
            it.startSpotAndShowRect()
        }

    }

    override fun onRestart() {
        super.onRestart()
        mBinding.zxingview.also {
            it.startCamera()
            it.startSpotAndShowRect()
        }
    }

    override fun onStop() {
        mBinding.zxingview.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.zxingview.onDestroy()
    }

    private fun vibrate(){
        var vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onScanQRCodeSuccess(result: String?) {
        LOG.I("123","onScanQRCodeSuccess  $result")
        vibrate()
        try {
            parseQR(result)
        }catch (e:Exception){
            e.printStackTrace()
        }
        mBinding.zxingview.postDelayed({
            if (!isFinishing){
                mBinding.zxingview.startSpot()
            }
        },3000)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        var tipText = mBinding.zxingview.scanBoxView.tipText
        var ambientBrightnessTip = "\n环境过暗，请打开闪光灯"
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mBinding.zxingview.scanBoxView.tipText = tipText + ambientBrightnessTip
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                mBinding.zxingview.scanBoxView.tipText = tipText
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {

    }

    private fun parseQR(qr:String?){
        LOG.I("123","qr")

        setResult(RES, Intent().putExtra("qr",qr))
        finish()
    }
}