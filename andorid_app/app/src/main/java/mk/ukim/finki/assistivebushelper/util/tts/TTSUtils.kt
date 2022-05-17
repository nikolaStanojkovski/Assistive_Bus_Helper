package mk.ukim.finki.assistivebushelper.util.tts

import com.chaquo.python.PyObject

class TTSUtils {
    companion object {
        var synthesize_object: PyObject? = null
        var model: PyObject? = null
        var vocoder: PyObject? = null
    }
}