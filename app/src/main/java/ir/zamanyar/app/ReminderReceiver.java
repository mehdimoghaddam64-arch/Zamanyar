package ir.zamanyar.app;
import android.app.*;
import android.content.*;
import android.os.Build;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
  String channel="zamanyar_reminders";
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(channel,"یادآوری برنامه‌ها",NotificationManager.IMPORTANCE_HIGH));
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,channel):new Notification.Builder(context);
  b.setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("یادآوری زمان‌یار").setContentText(intent.getStringExtra("title")).setAutoCancel(true);
  Intent open=new Intent(context,MainActivity.class);open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  b.setContentIntent(PendingIntent.getActivity(context,0,open,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0)));
  nm.notify(intent.getIntExtra("id",0),b.build());
  long id=intent.getIntExtra("id",0);
  Db db=new Db(context);
  for(android.content.ContentValues entry:db.all("همه")){
   Long entryId=entry.getAsLong("id");
   if(entryId!=null && entryId==id){
    String rule=entry.getAsString("repeat_rule");
    if(rule!=null && !rule.equals("بدون تکرار"))ReminderScheduler.schedule(context,id,entry);
    break;
   }
  }
  db.close();
 }
}
