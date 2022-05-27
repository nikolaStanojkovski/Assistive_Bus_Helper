package mk.ukim.finki.assistivebushelper.util

import android.content.Context
import android.net.ConnectivityManager

@Suppress("DEPRECATION")
class InternetUtils {

    companion object {

        fun hasActiveInternetConnection(context: Context?): Boolean {
            var haveConnectedWifi = false
            var haveConnectedMobile = false

            if (context == null)
                return false

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val netInfo = cm!!.allNetworkInfo
            for (ni in netInfo) {
                if (ni.typeName.equals(
                        "WIFI",
                        ignoreCase = true
                    )
                ) if (ni.isConnected) haveConnectedWifi = true
                if (ni.typeName.equals(
                        "MOBILE",
                        ignoreCase = true
                    )
                ) if (ni.isConnected) haveConnectedMobile = true
            }
            return haveConnectedWifi || haveConnectedMobile
        }
    }
}