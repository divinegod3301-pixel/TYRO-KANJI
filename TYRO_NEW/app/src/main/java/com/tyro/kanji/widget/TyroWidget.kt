package com.tyro.kanji.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.app.PendingIntent
import android.content.*
import android.widget.RemoteViews
import com.tyro.kanji.R
import com.tyro.kanji.data.KanjiCatalog
import com.tyro.kanji.data.KanjiLevels

class TyroWidget:AppWidgetProvider(){
 override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){ids.forEach{update(c,m,it)}}
 override fun onReceive(c:Context,i:Intent){super.onReceive(c,i);if(i.action==ACTION){val id=i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(id!=AppWidgetManager.INVALID_APPWIDGET_ID)update(c,AppWidgetManager.getInstance(c),id)}}
 private fun update(c:Context,m:AppWidgetManager,id:Int){val p=c.getSharedPreferences("tyro",0);val level=p.getString("level","ALL")?:"ALL";val pool=KanjiCatalog.items.filter{val l=KanjiLevels.levelFor(it.kanji);level=="ALL"||l==level};val old=p.getInt("widget_index",0);val next=if(pool.size<2)0 else (old+1)%pool.size;p.edit().putInt("widget_index",next).apply();val k=pool.getOrNull(next)?:KanjiCatalog.items.first();val v=RemoteViews(c.packageName,R.layout.widget_stub);v.setTextViewText(R.id.widget_text,"${k.kanji}  ${k.reading} • ${k.meaning}");val pi=PendingIntent.getBroadcast(c,9000,Intent(c,TyroWidget::class.java).setAction(ACTION).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.widget_text,pi);m.updateAppWidget(id,v)}
 companion object{const val ACTION="com.tyro.kanji.WIDGET_REFRESH"}
}
