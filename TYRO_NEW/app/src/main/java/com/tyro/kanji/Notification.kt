package com.tyro.kanji

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat

object Reminder {
 private const val CHANNEL="tyro_reminders"; private const val REQUEST=7781; private const val ACTION="com.tyro.kanji.REMIND"; private const val TWO_HOURS=2L*60*60*1000
 fun schedule(c:Context){val p=c.getSharedPreferences("tyro",0);p.edit().putBoolean("notify",true).apply();if(Build.VERSION.SDK_INT>=26)(c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel(CHANNEL,"TYRO study reminders",NotificationManager.IMPORTANCE_DEFAULT));val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager;am.cancel(pi(c));am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+TWO_HOURS,pi(c))}
 fun cancel(c:Context){(c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi(c));c.getSharedPreferences("tyro",0).edit().putBoolean("notify",false).apply()}
 fun test(c:Context){show(c)}
 private fun pi(c:Context)=PendingIntent.getBroadcast(c,REQUEST,Intent(c,NotificationReceiver::class.java).setAction(ACTION),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun show(c:Context){if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)return;val text=listOf("A tiny Kanji break? 🌸","Ready for one more Kanji? ✨","おつかれさま！ One small review is enough.").random();val n=NotificationCompat.Builder(c,CHANNEL).setSmallIcon(R.drawable.tyro_icon).setContentTitle("TYRO KANJI").setContentText(text).setAutoCancel(true).build();(c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify((System.currentTimeMillis()%100000).toInt(),n)}
}
class NotificationReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent?){if(i?.action=="com.tyro.kanji.REMIND"){Reminder.show(c);if(c.getSharedPreferences("tyro",0).getBoolean("notify",false))Reminder.schedule(c)}}}
