package mk.ukim.finki.androidkotlinapplication

import com.chaquo.python.PyObject

class ModelDataHolder {
    companion object {
        var synthesize_object: PyObject? = null
        var model: PyObject? = null
        var vocoder: PyObject? = null
    }
}