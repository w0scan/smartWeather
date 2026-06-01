package com.ww.smartweather.data.model

data class Forecast(
    val date: String,
    val weatherCode: String,
    val lowTemp: Int,
    val highTemp: Int,
    val sunRiseSet: String = ""
) {
    val dayOfWeek: String
        get() {
            val cal = parseDateToCalendar() ?: return ""
            return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SUNDAY -> "周日"
                java.util.Calendar.MONDAY -> "周一"
                java.util.Calendar.TUESDAY -> "周二"
                java.util.Calendar.WEDNESDAY -> "周三"
                java.util.Calendar.THURSDAY -> "周四"
                java.util.Calendar.FRIDAY -> "周五"
                java.util.Calendar.SATURDAY -> "周六"
                else -> ""
            }
        }

    val displayDate: String
        get() {
            val cal = parseDateToCalendar() ?: return date.takeLast(5)
            val month = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
            val day = String.format("%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
            return "$month.$day"
        }

    private fun parseDateToCalendar(): java.util.Calendar? {
        return try {
            val sdf = when {
                date.length == 8 -> java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                date.contains("-") -> java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                else -> return null
            }
            val d = sdf.parse(date) ?: return null
            java.util.Calendar.getInstance().apply { time = d }
        } catch (e: Exception) {
            null
        }
    }
}
