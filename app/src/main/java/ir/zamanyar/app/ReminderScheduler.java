package ir.zamanyar.app;
import android.app.*;import android.content.*;import android.os.Build;import java.util.*;import java.text.*;
public class ReminderScheduler {
 public static void rescheduleAll(Context context){Db db=new Db(context);for(ContentValues v:db.all("همه")){try{schedule(context,v.getAsLong("id"),v);}catch(Exception ignored){}}db.close();}
 public static void schedule(Context context,long id,ContentValues v){try{
  String date=v.getAsString("date"),time=v.getAsString("time");if(date==null||time==null)return;
  SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);fmt.setLenient(false);
  int minutes=0;try{minutes=Integer.parseInt(v.getAsString("reminder_minutes"));}catch(Exception ignored){}if(minutes<0)return;
  long when=fmt.parse(date+" "+time).getTime()-minutes*60000L;
  if(when<=System.currentTimeMillis()){
   Calendar cursor=Calendar.getInstance();cursor.add(Calendar.DATE,-2);
   String from=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cursor.getTime());
   String next=null;
   for(int tries=0;tries<370;tries++){
    next=Recurrence.next(v,from);if(next==null)return;
    when=fmt.parse(next+" "+time).getTime()-minutes*60000L;
    if(when>System.currentTimeMillis()+1000L)break;
    Calendar following=Calendar.getInstance();following.setTime(fmt.parse(next+" "+time));following.add(Calendar.DATE,1);
    from=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(following.getTime());
   }
   if(when<=System.currentTimeMillis()+1000L)return;
  }
  Intent i=new Intent(context,ReminderReceiver.class);i.putExtra("id",(int)id);i.putExtra("title",v.getAsString("title"));
  PendingIntent pi=PendingIntent.getBroadcast(context,(int)id,i,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
  ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
 }catch(Exception ignored){}}
}
