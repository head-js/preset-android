package com.lisitede.preset.preset.lynx

import android.content.Context
import com.lynx.tasm.provider.AbsTemplateProvider
import java.io.ByteArrayOutputStream
import java.io.IOException

class AssetsTemplateProvider(context: Context) : AbsTemplateProvider() {

    private val appContext = context.applicationContext

    override fun loadTemplate(uri: String, callback: Callback) {
        Thread {
            try {
                appContext.assets.open(uri).use { input ->
                    ByteArrayOutputStream().use { output ->
                        val buffer = ByteArray(4096)
                        var len: Int
                        while (input.read(buffer).also { len = it } != -1) {
                            output.write(buffer, 0, len)
                        }
                        callback.onSuccess(output.toByteArray())
                    }
                }
            } catch (e: IOException) {
                callback.onFailed(e.message)
            }
        }.start()
    }
}
