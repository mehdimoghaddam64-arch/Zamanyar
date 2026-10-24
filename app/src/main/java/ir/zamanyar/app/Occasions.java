package ir.zamanyar.app;
import java.util.*;

/** Offline Iranian Solar Hijri occasions. Lunar-based dates bundled only for year 1405. */
public final class Occasions {
 public static final class Occasion {
  public final String title;
  public final boolean holiday;
  public final boolean lunar;
  public Occasion(String title,boolean holiday,boolean lunar){this.title=title;this.holiday=holiday;this.lunar=lunar;}
 }
 private static final Map<String,ArrayList<Occasion>> fixed=new HashMap<>();
 private static final Map<String,ArrayList<Occasion>> year1405=new HashMap<>();
 private static String key(int m,int d){return m+"/"+d;}
 private static void add(Map<String,ArrayList<Occasion>> to,int m,int d,String label,boolean holiday,boolean lunar){
  String k=key(m,d);ArrayList<Occasion> items=to.get(k);if(items==null){items=new ArrayList<>();to.put(k,items);}items.add(new Occasion(label,holiday,lunar));
 }
 private static void f(int m,int d,String label,boolean holiday){add(fixed,m,d,label,holiday,false);}
 private static void q(int m,int d,String label,boolean holiday){add(year1405,m,d,label,holiday,true);}
 static {
  f(1,1,"آغاز نوروز",true);f(1,2,"عید نوروز",true);f(1,3,"عید نوروز",true);f(1,4,"عید نوروز",true);
  f(1,12,"روز جمهوری اسلامی ایران",true);f(1,13,"روز طبیعت",true);f(1,29,"روز ارتش جمهوری اسلامی ایران",false);
  f(2,1,"روز بزرگداشت سعدی",false);f(2,10,"روز ملی خلیج فارس",false);f(2,12,"روز معلم",false);f(2,25,"روز بزرگداشت فردوسی",false);f(2,28,"روز بزرگداشت خیام",false);
  f(3,3,"سالروز آزادسازی خرمشهر",false);f(3,14,"رحلت امام خمینی (ره)",true);f(3,15,"قیام ۱۵ خرداد",true);
  f(4,7,"روز قوه قضائیه",false);f(4,14,"روز قلم",false);
  f(5,17,"روز خبرنگار",false);f(5,28,"سالروز کودتای ۲۸ مرداد",false);
  f(6,8,"روز مبارزه با تروریسم",false);f(6,17,"قیام ۱۷ شهریور",false);f(6,31,"آغاز هفته دفاع مقدس",false);
  f(7,7,"روز آتش‌نشانی و ایمنی",false);f(7,20,"روز بزرگداشت حافظ",false);
  f(8,1,"روز آمار و برنامه‌ریزی",false);f(8,13,"روز دانش‌آموز",false);f(8,24,"روز کتاب و کتابخوانی",false);
  f(9,5,"روز بسیج مستضعفان",false);f(9,7,"روز نیروی دریایی",false);f(9,10,"روز مجلس",false);f(9,12,"روز قانون اساسی",false);f(9,16,"روز دانشجو",false);f(9,25,"روز پژوهش",false);f(9,30,"شب یلدا",false);
  f(10,5,"سالروز زلزله بم",false);f(10,19,"روز شهدای پرواز ۷۵۲",false);
  f(11,12,"بازگشت امام خمینی (ره)",false);f(11,19,"روز نیروی هوایی",false);f(11,22,"پیروزی انقلاب اسلامی ایران",true);
  f(12,5,"روز مهندس",false);f(12,15,"روز درختکاری",false);f(12,29,"روز ملی شدن صنعت نفت ایران",true);
  // 1405 lunar holidays and commemorations, mapped to their published Persian-calendar dates.
  q(1,1,"عید سعید فطر",true);q(1,2,"تعطیل عید سعید فطر (روز دوم)",true);
  q(1,25,"شهادت امام جعفر صادق (ع)",true);
  q(3,6,"عید سعید قربان",true);q(3,14,"عید سعید غدیر",true);
  q(4,3,"تاسوعای حسینی",true);q(4,4,"عاشورای حسینی",true);
  q(5,13,"اربعین حسینی",true);q(5,21,"رحلت پیامبر اکرم (ص) و شهادت امام حسن (ع)",true);
  q(5,22,"شهادت امام رضا (ع)",true);q(5,30,"شهادت امام حسن عسکری (ع)",true);
  q(6,8,"میلاد پیامبر اکرم (ص) و امام جعفر صادق (ع)",true);
  q(8,22,"شهادت حضرت فاطمه زهرا (س)",true);
  q(9,9,"ولادت حضرت فاطمه زهرا (س) و روز مادر",false);
  q(10,2,"ولادت امام علی (ع) و روز پدر",true);q(10,16,"مبعث پیامبر اکرم (ص)",true);
  q(11,4,"ولادت حضرت مهدی (عج) و نیمه شعبان",true);
  q(12,9,"شهادت امام علی (ع)",true);q(12,19,"عید سعید فطر",true);q(12,20,"تعطیل عید سعید فطر (روز دوم)",true);
 }
 public static List<Occasion> forDate(int year,int month,int day){
  ArrayList<Occasion> result=new ArrayList<>();List<Occasion> regular=fixed.get(key(month,day));if(regular!=null)result.addAll(regular);
  if(year==1405){List<Occasion> special=year1405.get(key(month,day));if(special!=null)result.addAll(special);}return result;
 }
 public static boolean holiday(int year,int month,int day){for(Occasion o:forDate(year,month,day))if(o.holiday)return true;return false;}
}
