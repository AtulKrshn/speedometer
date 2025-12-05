package com.orca.tracker.util


import com.google.android.gms.maps.model.LatLng

object PolylineUtil {
    fun encode(points: List<LatLng>): String {
        var lastLat = 0
        var lastLng = 0
        val result = StringBuilder()

        for (point in points) {
            val lat = (point.latitude * 1e5).toInt()
            val lng = (point.longitude * 1e5).toInt()

            val dLat = lat - lastLat
            val dLng = lng - lastLng

            encode(dLat, result)
            encode(dLng, result)

            lastLat = lat
            lastLng = lng
        }

        return result.toString()
    }

    fun decode(encoded: String): List<LatLng> {
        val points = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var result = 1
            var shift = 0
            var b: Int

            do {
                b = encoded[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)

            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            result = 1
            shift = 0

            do {
                b = encoded[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)

            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            points.add(LatLng(lat / 1e5, lng / 1e5))
        }

        return points
    }

    private fun encode(value: Int, result: StringBuilder) {
        var v = if (value < 0) (value shl 1).inv() else value shl 1
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f)) + 63).toChar())
            v = v shr 5
        }
        result.append((v + 63).toChar())
    }
}