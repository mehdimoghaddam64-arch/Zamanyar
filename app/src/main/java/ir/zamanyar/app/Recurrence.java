package ir.zamanyar.app;
import android.content.ContentValues;
import java.text.*;
import java.util.*;
public final class Recurrence {
 private static Date parse(String s){try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);f.setLenient(false);return f.parse(s);}catch(Exception e){return null;}}
 public static boolean occurs(ContentValues e,String iso){
  String start=e.getAsString("date"),rule=e.getAsString("repeat_rule");
  Date a=parse(start),b=parse(iso);if(a==null||b==null||b.before(a))return false;
  if(start.equals(iso))return true;
  if(rule==null||rule.equals("بدون تکرار"))return false;
  Calendar x=Calendar.getInstance(),y=Calendar.getInstance();x.setTime(a);y.setTime(b);
  if(rule.equals("روزانه"))return true;
  if(rule.equals("هفتگی"))return x.get(Calendar.DAY_OF_WEEK)==y.get(Calendar.DAY_OF_WEEK);
  if(rule.equals("ماهانه")||rule.equals("سالانه")){
   int[] jx=Jalali.fromGregorian(x.get(Calendar.YEAR),x.get(Calendar.MONTH)+1,x.get(Calendar.DAY_OF_MONTH));
   int[] jy=Jalali.fromGregorian(y.get(Calendar.YEAR),y.get(Calendar.MONTH)+1,y.get(Calendar.DAY_OF_MONTH));
   return jx[2]==jy[2] && (rule.equals("ماهانه")||jx[1]==jy[1]);
  }
  return false;
 }
 public static String next(ContentValues e,String afterIso){
  Calendar c=Calendar.getInstance();Date d=parse(afterIso);if(d==null)return null;c.setTime(d);
  for(int i=0;i<370;i++){String iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());if(occurs(e,iso))return iso;c.add(Calendar.DATE,1);}return null;
 }
}
