package ir.zamanyar.app;
import android.app.*;
import android.os.Build;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.*;
public class MainActivity extends Activity {
 Db db; LinearLayout base,body,nav; String tab="داشبورد";
 int ink=Color.rgb(33,37,62),purple=Color.rgb(92,71,194),muted=Color.rgb(115,120,140);
 int surface=0xfff6f5fc;
 String[] tabs={"داشبورد","تقویم","جلسه","وظیفه","رویداد","گزارش"};
 String search=""; int weekOffset=0,monthOffset=0; boolean monthMode=true; static final int BACKUP_CREATE=501,BACKUP_OPEN=502;
 int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(purple);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);db=new Db(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},12);show("داشبورد");}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);t.setTextDirection(View.TEXT_DIRECTION_RTL);t.setPadding(dp(4),dp(6),dp(4),dp(6));if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
 LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 void show(String t){
  tab=t;base=column();base.setBackgroundColor(surface);setContentView(base);
  LinearLayout head=column();head.setBackgroundColor(purple);head.setPadding(dp(22),dp(22),dp(22),dp(22));
  TextView title=text("زمان‌یار  •  "+t,24,Color.WHITE,true);head.addView(title);
  TextView sub=text("برنامه‌ریزی دقیق، ذهن آسوده",13,0xffe7e1ff,false);head.addView(sub);base.addView(head);
  ScrollView scroll=new ScrollView(this);body=column();body.setPadding(dp(14),dp(12),dp(14),dp(20));scroll.addView(body);base.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(t.equals("داشبورد"))dashboard();else if(t.equals("تقویم")){if(monthMode)monthPage();else calendarPage();}else if(t.equals("گزارش"))reportPage();else listing(t);
  nav=new LinearLayout(this);nav.setElevation(dp(9));nav.setBackgroundColor(Color.WHITE);nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  String[] icons={"⌂","▦","▣","☑","◉","▥"};for(int i=0;i<tabs.length;i++){final String s=tabs[i];TextView b=text(icons[i]+"\n"+s,11,s.equals(t)?purple:muted,s.equals(t));b.setGravity(Gravity.CENTER);nav.addView(b,new LinearLayout.LayoutParams(0,dp(66),1));b.setOnClickListener(v->show(s));}
  base.addView(nav);
 }
 void card(LinearLayout parent,View content){
  LinearLayout box=column();box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackground(bg(Color.WHITE,16));box.setElevation(dp(2));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);parent.addView(box,p);box.addView(content);
 }
 void dashboard(){
  ArrayList<ContentValues> all=db.all("همه");int meetings=0,tasks=0,events=0,done=0,overdue=0;
  String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
  for(ContentValues e:all){String k=e.getAsString("kind");if("جلسه".equals(k))meetings++;if("وظیفه".equals(k)){tasks++;if("انجام‌شده".equals(e.getAsString("status")))done++;else if(!val(e,"date").isEmpty()&&val(e,"date").compareTo(today)<0)overdue++;}if("رویداد".equals(k))events++;}
  body.addView(text("امروز  •  "+Jalali.display(today),18,purple,true));
  LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  metric(row,"جلسات",meetings,"▣",0xfffff1e5);metric(row,"وظایف",tasks,"☑",0xffe9f8f1);body.addView(row);
  LinearLayout second=new LinearLayout(this);second.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  metric(second,"رویدادها",events,"◉",0xffeeeafe);metric(second,"معوق",overdue,"!",0xffffe9ef);body.addView(second);
  body.addView(text("پیشرفت وظایف",17,ink,true));
  ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(Math.max(tasks,1));progress.setProgress(done);body.addView(progress);
  body.addView(text(done+" از "+tasks+" وظیفه انجام شده",13,muted,false));
  body.addView(text("برنامه‌های امروز",19,ink,true));int todayCount=0;
  for(ContentValues e:all)if(Recurrence.occurs(e,today)){entryCard(e);todayCount++;}
  if(todayCount==0)card(body,text("امروز برنامه‌ای ثبت نشده است.",15,muted,false));
  body.addView(text("سایر برنامه‌ها",19,ink,true));
  for(ContentValues e:all)if(!Recurrence.occurs(e,today)&&matches(e))entryCard(e);
  Button add=button("+ ثبت برنامه جدید");body.addView(add);add.setOnClickListener(v->chooseType());
  Button backup=button("پشتیبان‌گیری و بازیابی");body.addView(backup);backup.setOnClickListener(v->backupMenu());
 }
 void metric(LinearLayout parent,String label,int count,String icon,int color){
  LinearLayout box=column();box.setPadding(dp(14),dp(10),dp(14),dp(10));box.setBackground(bg(Color.WHITE,16));box.setElevation(dp(2));
  box.addView(text(icon+"   "+count,24,purple,true));box.addView(text(label,13,muted,false));
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(102),1);lp.setMargins(dp(3),dp(4),dp(3),dp(8));parent.addView(box,lp);
 }
 void chooseType(){new AlertDialog.Builder(this).setTitle("نوع برنامه").setItems(new String[]{"جلسه","وظیفه","رویداد"},(d,w)->edit(new String[]{"جلسه","وظیفه","رویداد"}[w],null)).show();}
 void listing(String kind){
  Button add=button("+ افزودن "+kind);body.addView(add);add.setOnClickListener(v->edit(kind,null));
  ArrayList<ContentValues> all=db.all(kind);
  searchBox();
  if(all.isEmpty())card(body,text("موردی در این بخش ثبت نشده است.",15,muted,false));
  for(ContentValues e:all)if(matches(e))entryCard(e);
 }
 Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(bg(purple,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.bottomMargin=dp(12);b.setLayoutParams(p);return b;}
 void entryCard(ContentValues e){
  LinearLayout item=column();
  String title=e.getAsString("title"),kind=e.getAsString("kind");
  item.addView(text(("جلسه".equals(kind)?"▣  ":"وظیفه".equals(kind)?"☑  ":"◉  ")+title,17,ink,true));
  String date=e.getAsString("date"),time=e.getAsString("time"),status=e.getAsString("status");
  item.addView(text(Jalali.display(date)+(time==null||time.isEmpty()?"":"  |  "+time)+(status==null||status.isEmpty()?"":"  |  "+status),13,muted,false));
  card(body,item);item.setOnClickListener(v->detail(e));
 }
 String val(ContentValues e,String k){return e==null?"":(e.getAsString(k)==null?"":e.getAsString(k));}
 void detail(ContentValues e){
  LinearLayout l=column();l.setPadding(dp(16),dp(8),dp(16),dp(8));
  String[][] fields={{"reminder_minutes","یادآوری (دقیقه قبل)"},{"kind","نوع"},{"title","عنوان"},{"date","تاریخ شمسی"},{"time","ساعت شروع"},{"endtime","ساعت پایان"},{"notes","توضیحات"},{"location","مکان"},{"people","شرکت‌کنندگان / مسئول"},{"priority","اولویت"},{"status","وضعیت"},{"category","دسته‌بندی"},{"repeat_rule","تکرار"},{"checklist","چک‌لیست / زیر‌وظایف"},{"decisions","مصوبات و اقدامات بعدی"}};
  for(String[] f:fields){String value=f[0].equals("date")?Jalali.display(val(e,"date")):val(e,f[0]);if(!value.isEmpty())l.addView(text(f[1]+":  "+value,15,ink,false));}
  ScrollView sc=new ScrollView(this);sc.addView(l);
  if("جلسه".equals(val(e,"kind"))){Button convert=button("تبدیل مصوبه به وظیفه");l.addView(convert);convert.setOnClickListener(v->createAction(e));}
  new AlertDialog.Builder(this).setTitle("جزئیات برنامه").setView(sc).setPositiveButton("ویرایش",(d,w)->edit(val(e,"kind"),e)).setNeutralButton("حذف",(d,w)->new AlertDialog.Builder(this).setMessage("این مورد حذف شود؟").setPositiveButton("بله",(a,b)->{cancelReminder(e.getAsLong("id"));db.delete(e.getAsLong("id"));show(tab);}).setNegativeButton("خیر",null).show()).setNegativeButton("بستن",null).show();
 }
 void edit(String kind,ContentValues old){
  ScrollView sc=new ScrollView(this);LinearLayout fields=column();fields.setPadding(dp(18),dp(8),dp(18),dp(12));sc.addView(fields);
  LinkedHashMap<String,EditText> inputs=new LinkedHashMap<>();
  addField(fields,inputs,"title","عنوان *",old,false);
  addDate(fields,inputs,old);
  addField(fields,inputs,"time","ساعت شروع (مثلاً 09:30)",old,false);
  if(!kind.equals("وظیفه"))addField(fields,inputs,"endtime","ساعت پایان",old,false);
  addField(fields,inputs,"notes","توضیحات",old,true);
  if(!kind.equals("وظیفه"))addField(fields,inputs,"location","مکان / لینک جلسه",old,false);
  addField(fields,inputs,"people",kind.equals("جلسه")?"شرکت‌کنندگان":kind.equals("وظیفه")?"مسئول":"افراد مرتبط",old,false);
  addField(fields,inputs,"category","دسته‌بندی / پروژه",old,false);
  if(kind.equals("وظیفه")){addChoice(fields,inputs,"priority","اولویت",new String[]{"کم","متوسط","زیاد","فوری"},old);addChoice(fields,inputs,"status","وضعیت",new String[]{"انجام‌نشده","درحال انجام","انجام‌شده","متوقف"},old);addField(fields,inputs,"checklist","چک‌لیست / زیر‌وظایف (هر خط یک مورد)",old,true);}
  addChoice(fields,inputs,"repeat_rule","تکرار (اطلاعات تکرار ثبت می‌شود)",new String[]{"بدون تکرار","روزانه","هفتگی","ماهانه","سالانه"},old);
  addChoice(fields,inputs,"reminder_minutes","زمان یادآوری قبل از شروع",new String[]{"0","5","10","15","30","60","1440"},old);
  if(kind.equals("جلسه"))addField(fields,inputs,"decisions","دستور جلسه، مصوبات و اقدامات بعدی",old,true);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle((old==null?"ثبت ":"ویرایش ")+kind).setView(sc).setPositiveButton("ذخیره",null).setNegativeButton("انصراف",null).create();
  dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{
   if(inputs.get("title").getText().toString().trim().isEmpty()){inputs.get("title").setError("عنوان الزامی است");return;}
   String tm=inputs.get("time").getText().toString().trim();
   if(!tm.isEmpty()&&!validTime(tm)){inputs.get("time").setError("ساعت باید به صورت 09:30 باشد");return;}
   if(inputs.containsKey("endtime")){String end=inputs.get("endtime").getText().toString().trim();if(!end.isEmpty()&&!validTime(end)){inputs.get("endtime").setError("ساعت پایان نامعتبر است");return;}}
   ContentValues data=new ContentValues();data.put("kind",kind);
   for(Map.Entry<String,EditText> it:inputs.entrySet()){String value=it.getValue().getText().toString().trim();if(it.getKey().equals("date")&&!value.isEmpty()){try{String[] p=value.split("/");int jy=Integer.parseInt(p[0]),jm=Integer.parseInt(p[1]),jd=Integer.parseInt(p[2]);
     if(jy<1300||jy>1500||jm<1||jm>12||jd<1||jd>Jalali.monthLength(jy,jm))throw new IllegalArgumentException("Invalid Jalali date");
     value=Jalali.iso(jy,jm,jd);}catch(Exception ex){Toast.makeText(this,"تاریخ نامعتبر است",Toast.LENGTH_SHORT).show();return;}}data.put(it.getKey(),value);}
   long saved=db.save(data,old==null?-1:old.getAsLong("id"));cancelReminder(saved);ReminderScheduler.schedule(this,saved,data);dialog.dismiss();show(tab);
  }));dialog.show();
 }
 boolean validTime(String value){if(!value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))return false;return true;}
 void addField(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,ContentValues old,boolean multiline){
  parent.addView(text(label,14,ink,true));EditText e=new EditText(this);e.setText(val(old,key));e.setTextSize(15);e.setSingleLine(!multiline);e.setTextDirection(View.TEXT_DIRECTION_RTL);e.setGravity(Gravity.RIGHT|Gravity.TOP);e.setBackground(bg(0xfff0eff7,10));e.setPadding(dp(12),dp(12),dp(12),dp(12));if(multiline)e.setMinLines(3);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);parent.addView(e,p);inputs.put(key,e);
 }
 void addChoice(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,String[] opts,ContentValues old){
  addField(parent,inputs,key,label+" (برای انتخاب لمس کنید)",old,false);
  EditText e=inputs.get(key);e.setFocusable(false);e.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(label).setItems(opts,(d,w)->e.setText(opts[w])).show());
 }
 void cancelReminder(long id){
  Intent intent=new Intent(this,ReminderReceiver.class);
  PendingIntent pi=PendingIntent.getBroadcast(this,(int)id,intent,PendingIntent.FLAG_NO_CREATE|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
  if(pi!=null){((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(pi);pi.cancel();}
 }
 void scheduleReminder(long id,ContentValues data){
  cancelReminder(id);String date=data.getAsString("date"),time=data.getAsString("time");
  if(date==null||date.isEmpty()||time==null||!time.matches("[0-2]?[0-9]:[0-5][0-9]"))return;
  try{java.text.SimpleDateFormat fmt=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);fmt.setLenient(false);long when=fmt.parse(date+" "+time).getTime();int minutes=0;try{minutes=Integer.parseInt(data.getAsString("reminder_minutes"));}catch(Exception ignored){}when-=minutes*60000L;if(when<=System.currentTimeMillis())return;
   Intent intent=new Intent(this,ReminderReceiver.class);intent.putExtra("id",(int)id);intent.putExtra("title",data.getAsString("title"));
   PendingIntent pi=PendingIntent.getBroadcast(this,(int)id,intent,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
   ((AlarmManager)getSystemService(ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
  }catch(Exception ex){android.util.Log.w("Zamanyar","Reminder could not be scheduled",ex);}
 }
 void addDate(LinearLayout parent,LinkedHashMap<String,EditText> inputs,ContentValues old){
  addField(parent,inputs,"date","تاریخ شمسی (برای انتخاب لمس کنید)",old,false);
  EditText e=inputs.get("date");e.setFocusable(false);e.setText(val(old,"date").isEmpty()?"":Jalali.display(val(old,"date")));
  e.setOnClickListener(v->jalaliPicker(e));
 }
 void jalaliPicker(EditText e){
  Calendar now=Calendar.getInstance();int[] today=Jalali.fromGregorian(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
  String current=e.getText().toString();if(current.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}")){String[] q=current.split("/");today=new int[]{Integer.parseInt(q[0]),Integer.parseInt(q[1]),Integer.parseInt(q[2])};}
  final int[] chosen=today;LinearLayout wrap=column();wrap.setPadding(dp(20),dp(10),dp(20),dp(10));
  NumberPicker year=new NumberPicker(this),month=new NumberPicker(this),day=new NumberPicker(this);
  year.setMinValue(1300);year.setMaxValue(1500);year.setValue(chosen[0]);
  month.setMinValue(1);month.setMaxValue(12);month.setDisplayedValues(new String[]{"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"});month.setValue(chosen[1]);
  day.setMinValue(1);day.setMaxValue(31);day.setValue(chosen[2]);
  LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  line.addView(day,new LinearLayout.LayoutParams(0,dp(170),1));line.addView(month,new LinearLayout.LayoutParams(0,dp(170),2));line.addView(year,new LinearLayout.LayoutParams(0,dp(170),1));wrap.addView(line);
  new AlertDialog.Builder(this).setTitle("انتخاب تاریخ شمسی").setView(wrap).setPositiveButton("تأیید",(d,w)->{
   int y=year.getValue(),m=month.getValue();int max=Jalali.monthLength(y,m);int dd=Math.min(day.getValue(),max);
   e.setText(String.format(Locale.US,"%04d/%02d/%02d",y,m,dd));
  }).setNegativeButton("انصراف",null).show();
 }
 boolean matches(ContentValues e){String q=search.trim().toLowerCase(Locale.ROOT);if(q.isEmpty())return true;return (val(e,"title")+" "+val(e,"notes")+" "+val(e,"people")+" "+val(e,"category")).toLowerCase(Locale.ROOT).contains(q);}
 void searchBox(){EditText field=new EditText(this);field.setSingleLine(true);field.setHint("جست‌وجوی عنوان، توضیح، مسئول یا پروژه");field.setText(search);field.setTextDirection(View.TEXT_DIRECTION_RTL);field.setPadding(dp(12),dp(10),dp(12),dp(10));field.setBackground(bg(Color.WHITE,12));body.addView(field);field.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();}public void afterTextChanged(android.text.Editable x){}});
  Button find=button("نمایش نتایج جست‌وجو");body.addView(find);find.setOnClickListener(v->show(tab));
 }
 void calendarPage(){
  Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,weekOffset*7);int dow=(c.get(Calendar.DAY_OF_WEEK)+1)%7;c.add(Calendar.DAY_OF_YEAR,-dow);
  Button switcher=button("نمایش تقویم ماهانه");body.addView(switcher);switcher.setOnClickListener(v->{monthMode=true;show("تقویم");});
  LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  Button prev=button("هفته قبل");Button next=button("هفته بعد");line.addView(prev,new LinearLayout.LayoutParams(0,dp(48),1));line.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));body.addView(line);
  prev.setOnClickListener(v->{weekOffset--;show("تقویم");});next.setOnClickListener(v->{weekOffset++;show("تقویم");});
  ArrayList<ContentValues> entries=db.all("همه");String[] weekdays={"شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه"};
  for(int i=0;i<7;i++){
   String iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());
   LinearLayout section=column();section.addView(text(weekdays[i]+"  •  "+Jalali.display(iso),17,purple,true));
   int count=0;for(ContentValues e:entries){if(Recurrence.occurs(e,iso)){section.addView(text("• "+val(e,"title")+"  "+val(e,"time"),15,ink,false));count++;}}
   if(count==0)section.addView(text("برنامه‌ای ثبت نشده",13,muted,false));card(body,section);c.add(Calendar.DAY_OF_YEAR,1);
  }
 }
 void monthPage(){
  Button switcher=button("نمایش برنامه هفتگی");body.addView(switcher);switcher.setOnClickListener(v->{monthMode=false;show("تقویم");});
  Calendar now=Calendar.getInstance();int[] j=Jalali.fromGregorian(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
  int total=j[0]*12+j[1]-1+monthOffset;int year=Math.floorDiv(total,12),month=Math.floorMod(total,12)+1;
  String[] names={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};
  LinearLayout controls=new LinearLayout(this);controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  Button previous=button("ماه قبل"),next=button("ماه بعد");controls.addView(previous,new LinearLayout.LayoutParams(0,dp(48),1));controls.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));body.addView(controls);
  previous.setOnClickListener(v->{monthOffset--;show("تقویم");});next.setOnClickListener(v->{monthOffset++;show("تقویم");});
  body.addView(text(names[month-1]+" "+year,22,purple,true));
  int[] g=Jalali.toGregorian(year,month,1);Calendar first=Calendar.getInstance();first.clear();first.set(g[0],g[1]-1,g[2]);int offset=(first.get(Calendar.DAY_OF_WEEK)+1)%7;
  int count=month<=6?31:month<=11?30:29;
  if(month==12){java.util.Calendar end=Calendar.getInstance();int[] nextYear=Jalali.toGregorian(year+1,1,1);int[] start=Jalali.toGregorian(year,12,1);Calendar a=Calendar.getInstance();a.clear();a.set(start[0],start[1]-1,start[2]);end.clear();end.set(nextYear[0],nextYear[1]-1,nextYear[2]);count=(int)((end.getTimeInMillis()-a.getTimeInMillis())/86400000L);}
  ArrayList<ContentValues> all=db.all("همه");String[] weekdays={"ش","ی","د","س","چ","پ","ج"};
  LinearLayout headers=new LinearLayout(this);headers.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);for(String w:weekdays){TextView h=text(w,13,muted,true);h.setGravity(Gravity.CENTER);headers.addView(h,new LinearLayout.LayoutParams(0,dp(32),1));}body.addView(headers);
  int cells=((offset+count+6)/7)*7;LinearLayout row=null;
  for(int i=0;i<cells;i++){
   if(i%7==0){row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);body.addView(row);}
   int day=i-offset+1;TextView cell=text(day>0&&day<=count?""+day:"",15,ink,false);cell.setGravity(Gravity.CENTER);cell.setBackground(bg(Color.WHITE,9));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(54),1);cp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,cp);
   if(day>0&&day<=count){String iso=Jalali.iso(year,month,day);int events=0;for(ContentValues e:all)if(Recurrence.occurs(e,iso))events++;if(events>0){cell.setText(day+"\n• "+events);cell.setTextColor(purple);}cell.setOnClickListener(v->dayDetails(iso));}
  }
 }
 void dayDetails(String iso){LinearLayout list=column();list.setPadding(dp(12),dp(10),dp(12),dp(10));int count=0;for(ContentValues e:db.all("همه")){if(Recurrence.occurs(e,iso)){TextView item=text(val(e,"kind")+" • "+val(e,"title")+"  "+val(e,"time"),16,ink,true);list.addView(item);item.setOnClickListener(v->detail(e));count++;}}if(count==0)list.addView(text("برنامه‌ای برای این روز وجود ندارد",14,muted,false));new AlertDialog.Builder(this).setTitle(Jalali.display(iso)).setView(list).setPositiveButton("بستن",null).show();}

 void createAction(ContentValues meeting){
  final EditText title=new EditText(this);title.setHint("عنوان اقدام");title.setSingleLine(true);
  final EditText owner=new EditText(this);owner.setHint("مسئول اقدام");
  LinearLayout panel=column();panel.setPadding(dp(20),dp(10),dp(20),dp(10));panel.addView(title);panel.addView(owner);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("اقدام مرتبط با جلسه").setView(panel).setPositiveButton("ثبت وظیفه",null).setNegativeButton("انصراف",null).create();
  dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String t=title.getText().toString().trim();if(t.isEmpty()){title.setError("عنوان الزامی است");return;}
   ContentValues task=new ContentValues();task.put("kind","وظیفه");task.put("title",t);task.put("people",owner.getText().toString().trim());task.put("date",val(meeting,"date"));task.put("status","انجام‌نشده");task.put("priority","متوسط");task.put("notes","مرتبط با جلسه: "+val(meeting,"title")+" (شناسه "+meeting.getAsLong("id")+")");
   db.save(task,-1);dialog.dismiss();Toast.makeText(this,"وظیفه ایجاد شد",Toast.LENGTH_SHORT).show();show("وظیفه");
  }));dialog.show();
 }
 void reportPage(){
  ArrayList<ContentValues> all=db.all("همه");int total=0,done=0,overdue=0,meetings=0,events=0;long meetingMinutes=0;
  String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
  for(ContentValues e:all){String kind=val(e,"kind");if(kind.equals("وظیفه")){total++;if(val(e,"status").equals("انجام‌شده"))done++;else if(!val(e,"date").isEmpty()&&val(e,"date").compareTo(today)<0)overdue++;}
   else if(kind.equals("جلسه")){meetings++;try{String[] a=val(e,"time").split(":"),b=val(e,"endtime").split(":");long diff=(Integer.parseInt(b[0])*60+Integer.parseInt(b[1]))-(Integer.parseInt(a[0])*60+Integer.parseInt(a[1]));if(diff>0)meetingMinutes+=diff;}catch(Exception ignored){}}
   else if(kind.equals("رویداد"))events++;
  }
  card(body,text("پیشرفت وظایف: "+(total==0?0:(done*100/total))+"٪  ("+done+" از "+total+")",20,purple,true));
  ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(Math.max(total,1));progress.setProgress(done);body.addView(progress);
  card(body,text("وظایف معوق: "+overdue+"\nجلسات ثبت‌شده: "+meetings+"\nمدت جلسات ثبت‌شده: "+(meetingMinutes/60)+" ساعت و "+(meetingMinutes%60)+" دقیقه\nرویدادها: "+events,17,ink,false));
  body.addView(text("این گزارش براساس موارد ثبت‌شده است؛ تکرارهای مجازی جداگانه شمارش نمی‌شوند.",13,muted,false));
 }

 void backupMenu(){new AlertDialog.Builder(this).setTitle("پشتیبان‌گیری آفلاین").setItems(new String[]{"ذخیره نسخه پشتیبان JSON","بازیابی از فایل JSON"},(d,n)->{Intent i=new Intent(n==0?Intent.ACTION_CREATE_DOCUMENT:Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");if(n==0)i.putExtra(Intent.EXTRA_TITLE,"zamanyar-backup.json");startActivityForResult(i,n==0?BACKUP_CREATE:BACKUP_OPEN);}).show();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
  try{if(request==BACKUP_CREATE){org.json.JSONArray arr=new org.json.JSONArray();for(ContentValues v:db.all("همه")){org.json.JSONObject obj=new org.json.JSONObject();for(String key:v.keySet())if(!key.equals("id"))obj.put(key,v.getAsString(key));arr.put(obj);}try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(arr.toString(2).getBytes("UTF-8"));}Toast.makeText(this,"پشتیبان ذخیره شد",Toast.LENGTH_LONG).show();}
  else if(request==BACKUP_OPEN){java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=getContentResolver().openInputStream(data.getData())){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){buffer.write(b,0,n);if(buffer.size()>10_000_000)throw new Exception("فایل بیش از حد بزرگ است");}}org.json.JSONArray arr=new org.json.JSONArray(buffer.toString("UTF-8"));new AlertDialog.Builder(this).setTitle("بازیابی اطلاعات").setMessage("تعداد "+arr.length()+" مورد به اطلاعات موجود اضافه شود؟ ممکن است موارد تکراری ایجاد شوند.").setPositiveButton("بازیابی",(d,w)->{try{int saved=0;for(int i=0;i<arr.length();i++){org.json.JSONObject obj=arr.getJSONObject(i);String kind=obj.optString("kind");if(!kind.equals("جلسه")&&!kind.equals("وظیفه")&&!kind.equals("رویداد"))continue;ContentValues values=new ContentValues();for(java.util.Iterator<String> keys=obj.keys();keys.hasNext();){String key=keys.next();if(!key.equals("id"))values.put(key,obj.optString(key));}long id=db.save(values,-1);scheduleReminder(id,values);saved++;}Toast.makeText(this,saved+" مورد بازیابی شد",Toast.LENGTH_LONG).show();show("داشبورد");}catch(Exception ex){Toast.makeText(this,"خطا در بازیابی",Toast.LENGTH_LONG).show();}}).setNegativeButton("انصراف",null).show();}}
  catch(Exception ex){Toast.makeText(this,"خطا: "+ex.getMessage(),Toast.LENGTH_LONG).show();}
 }

}
