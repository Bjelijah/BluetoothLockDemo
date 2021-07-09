package xyz.mercs.bluetoothlockdemo

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

/**
 * @date 2021/7/9
 * @author elijah
 * @Description
 */
class APP :Application() {
    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}