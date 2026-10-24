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
 Typeface iranRegular, iranMedium, iranBold, iranBlack, iranLight;
 Typeface loadFont(String filename, Typeface fallback){
  try {return Typeface.createFromAsset(getAssets(),"fonts/"+filename);}catch(Exception ignored){return fallback;}
 }
 void initFonts(){
  iranRegular=loadFont("IRANSansMobile.ttf",Typeface.DEFAULT);
  iranMedium=loadFont("IRANSansMobile_Medium.ttf",Typeface.DEFAULT);
  iranBold=loadFont("IRANSansMobile_Bold.ttf",Typeface.DEFAULT_BOLD);
  iranBlack=loadFont("IRANSansMobile_Black.ttf",Typeface.DEFAULT_BOLD);
  iranLight=loadFont("IRANSansMobile_Light.ttf",Typeface.DEFAULT);
 }
 void styleInput(TextView view){view.setTypeface(iranRegular!=null?iranRegular:Typeface.DEFAULT);}

 int ink=Color.rgb(33,37,62),purple=Color.rgb(92,71,194),muted=Color.rgb(115,120,140);
 int surface=0xfff6f5fc; boolean dark=true; android.content.SharedPreferences prefs;
 String categoryFilter="";String reportPeriod="همه";
 String en(String value){if(value==null)return "";StringBuilder b=new StringBuilder();for(char ch:value.toCharArray()){if(ch>='۰'&&ch<='۹')ch=(char)('0'+ch-'۰');else if(ch>='٠'&&ch<='٩')ch=(char)('0'+ch-'٠');b.append(ch);}return b.toString();}
 AlertDialog.Builder dialogBuilder(){return new AlertDialog.Builder(this,dark?android.R.style.Theme_Material_Dialog_Alert:android.R.style.Theme_Material_Light_Dialog_Alert);}
 String todayIso(){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
 String nextDate(ContentValues entry,String fromIso){
  String start=val(entry,"date");if(start.isEmpty())return null;
  if(start.compareTo(fromIso)>0)return start;
  return Recurrence.next(entry,fromIso);
 }
 boolean overdue(ContentValues e,String today){return !"انجام‌شده".equals(val(e,"status"))&&!val(e,"date").isEmpty()&&val(e,"date").compareTo(today)<0;}

 int panel(){return dark?0xff17211e:Color.WHITE;} int fieldColor(){return dark?0xff25342e:0xffedf3ef;}
 String fa(String value){return value.replace('0','۰').replace('1','۱').replace('2','۲').replace('3','۳').replace('4','۴').replace('5','۵').replace('6','۶').replace('7','۷').replace('8','۸').replace('9','۹');}
 String[] categories(){String raw=prefs.getString("categories","مالی\nکاری\nمارکتینگ\nخرید\nشخصی");return raw.split("\n");}
 void settings(){
  LinearLayout content=column();content.setPadding(dp(18),dp(12),dp(18),dp(12));
  Switch mode=new Switch(this);styleInput(mode);mode.setText("حالت تیره");mode.setTextColor(ink);mode.setChecked(dark);content.addView(mode);
  Switch green=new Switch(this);styleInput(green);green.setText("رنگ اصلی سبز (به‌جای بنفش)");green.setTextColor(ink);green.setChecked(prefs.getBoolean("accent_green",true));content.addView(green);
  content.addView(text("دسته‌بندی‌ها (هر سطر یک دسته)",16,ink,true));
  EditText list=new EditText(this);styleInput(list);list.setText(prefs.getString("categories","مالی\nکاری\nمارکتینگ\nخرید\nشخصی"));list.setMinLines(6);list.setTextColor(ink);list.setHintTextColor(muted);list.setBackground(bg(fieldColor(),12));list.setPadding(dp(12),dp(12),dp(12),dp(12));content.addView(list);
  dialogBuilder().setTitle("تنظیمات زمان‌یار").setView(content).setPositiveButton("ذخیره تنظیمات",(d,w)->{
   LinkedHashSet<String> names=new LinkedHashSet<>();for(String name:list.getText().toString().split("\n")){name=name.trim();if(!name.isEmpty())names.add(name);}
   prefs.edit().putBoolean("dark",mode.isChecked()).putBoolean("accent_green",green.isChecked()).putString("categories",android.text.TextUtils.join("\n",names)).apply();dark=mode.isChecked();applyTheme();show(tab);
  }).setNegativeButton("بستن",null).show();
 }
 void applyTheme(){
  surface=dark?0xff0c1012:0xfff4f8f6;ink=dark?0xfff2f9f5:0xff1a3029;muted=dark?0xffb0c1bb:0xff60776f;
  boolean green=prefs==null||prefs.getBoolean("accent_green",true);
  purple=green?0xff16845f:0xff6751c3;
  getWindow().setStatusBarColor(surface);getWindow().setNavigationBarColor(surface);
  getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 }
 void timePicker(EditText field){Calendar now=Calendar.getInstance();int h=now.get(Calendar.HOUR_OF_DAY),m=now.get(Calendar.MINUTE);String current=en(field.getText().toString());if(validTime(current)){String[] t=current.split(":");h=Integer.parseInt(t[0]);m=Integer.parseInt(t[1]);}new TimePickerDialog(this,(v,hour,minute)->field.setText(fa(String.format(Locale.US,"%02d:%02d",hour,minute))),h,m,true).show();}
 void checklistDialog(ContentValues e){String[] lines=val(e,"checklist").split("\n");ArrayList<String> items=new ArrayList<>();ArrayList<Boolean> checks=new ArrayList<>();for(String line:lines){if(line.trim().isEmpty())continue;boolean done=line.startsWith("[x] ");checks.add(done);items.add(line.replaceFirst("^\\[(?:x| )\\] ",""));}if(items.isEmpty()){Toast.makeText(this,"زیر‌وظیفه‌ای ثبت نشده است",Toast.LENGTH_SHORT).show();return;}boolean[] selected=new boolean[items.size()];for(int i=0;i<selected.length;i++)selected[i]=checks.get(i);dialogBuilder().setTitle("چک‌لیست: "+val(e,"title")).setMultiChoiceItems(items.toArray(new String[0]),selected,(d,which,on)->selected[which]=on).setPositiveButton("ذخیره",(d,w)->{StringBuilder b=new StringBuilder();for(int i=0;i<items.size();i++){if(i>0)b.append("\n");b.append(selected[i]?"[x] ": "[ ] ").append(items.get(i));}e.put("checklist",b.toString());db.save(e,e.getAsLong("id"));show(tab);}).setNegativeButton("انصراف",null).show();}

 String[] tabs={"داشبورد","تقویم","جلسه","وظیفه","رویداد","گزارش"};
 String search=""; int weekOffset=0,monthOffset=0; boolean monthMode=true; static final int BACKUP_CREATE=501,BACKUP_OPEN=502;
 int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(purple);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);prefs=getSharedPreferences("zamanyar_ui",MODE_PRIVATE);dark=prefs.getBoolean("dark",true);applyTheme();db=new Db(this);initFonts();if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},12);show("داشبورد");}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(fa(s));t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);t.setIncludeFontPadding(false);t.setPadding(dp(4),dp(5),dp(4),dp(5));t.setTypeface(bold?iranBold:iranRegular);return t;}
 LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 void show(String t){
  tab=t;base=column();base.setBackgroundColor(surface);setContentView(base);
  LinearLayout head=column();head.setPadding(dp(18),dp(12),dp(18),dp(12));head.setBackgroundColor(surface);
  LinearLayout headline=new LinearLayout(this);headline.setGravity(Gravity.CENTER_VERTICAL);headline.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  TextView title=text("زمان‌یار",30,ink,true);headline.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  TextView gear=text("⚙",25,purple,true);gear.setGravity(Gravity.CENTER);headline.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(48)));gear.setOnClickListener(v->settings());
  head.addView(headline);TextView subtitle=text("برنامه‌ریزی دقیق، ذهن آسوده",13,muted,false);head.addView(subtitle);base.addView(head);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);body=column();body.setPadding(dp(14),dp(8),dp(14),dp(22));scroll.addView(body);base.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(t.equals("داشبورد"))dashboard();else if(t.equals("تقویم")){if(monthMode)monthPage();else calendarPage();}else if(t.equals("گزارش"))reportPage();else if(t.equals("دسته‌بندی"))listing("همه");else listing(t);
  nav=new LinearLayout(this);nav.setElevation(dp(9));nav.setPadding(dp(5),dp(5),dp(5),dp(7));nav.setBackgroundColor(panel());nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  String[] names={"داشبورد","تقویم","افزودن","رویداد","وظیفه","جلسه","گزارش"};String[] icons={"⌂","▦","+","◎","☑","▣","▥"};
  for(int i=0;i<names.length;i++){final String s=names[i];LinearLayout cell=column();cell.setGravity(Gravity.CENTER);TextView icon=text(icons[i],i==2?32:22,i==2?Color.WHITE:s.equals(t)?purple:muted,true);icon.setGravity(Gravity.CENTER);cell.addView(icon);if(i!=2){TextView label=text(s,10,s.equals(t)?purple:muted,s.equals(t));label.setGravity(Gravity.CENTER);cell.addView(label);}else{cell.setBackground(bg(purple,28));}LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(72),i==2?1.15f:1);cp.setMargins(dp(2),0,dp(2),0);nav.addView(cell,cp);cell.setOnClickListener(v->{if(s.equals("افزودن"))chooseType();else show(s);});}
  base.addView(nav);
 }
 void card(LinearLayout parent,View content){
  LinearLayout box=column();box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackground(bg(panel(),16));box.setElevation(dp(2));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);parent.addView(box,p);box.addView(content);
 }
 int nowYear(){return Calendar.getInstance().get(Calendar.YEAR);} int nowMonth(){return Calendar.getInstance().get(Calendar.MONTH)+1;} int nowDay(){return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);} String jalaliMonth(int m){String[] months={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};return months[Math.max(1,Math.min(12,m))-1];}
 void dashboard(){
  ArrayList<ContentValues> all=db.all("همه");int meetings=0,tasks=0,events=0,done=0,overdue=0;
  String today=todayIso();
  for(ContentValues e:all){String k=val(e,"kind");if("جلسه".equals(k))meetings++;if("وظیفه".equals(k)){tasks++;if("انجام‌شده".equals(val(e,"status")))done++;else if(overdue(e,today))overdue++;}if("رویداد".equals(k))events++;}
  int[] jt=Jalali.fromGregorian(nowYear(),nowMonth(),nowDay());
  LinearLayout cal=column();cal.setPadding(dp(12),dp(12),dp(12),dp(14));cal.setBackground(bg(panel(),22));
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  Calendar anchor=Calendar.getInstance();anchor.add(Calendar.DAY_OF_YEAR,weekOffset*7);
  int[] shownJ=Jalali.fromGregorian(anchor.get(Calendar.YEAR),anchor.get(Calendar.MONTH)+1,anchor.get(Calendar.DAY_OF_MONTH));
  TextView month=text(weekOffset==0?"امروز: "+jalaliMonth(jt[1])+" "+jt[0]+" • "+jt[2]:"هفته "+jalaliMonth(shownJ[1])+" "+shownJ[0],18,ink,true);header.addView(month,new LinearLayout.LayoutParams(0,dp(43),1));
  TextView previous=centerText("❮",22,muted,true),next=centerText("❯",22,muted,true);header.addView(previous,new LinearLayout.LayoutParams(dp(40),dp(43)));header.addView(next,new LinearLayout.LayoutParams(dp(40),dp(43)));cal.addView(header);
  previous.setOnClickListener(v->{weekOffset--;show("داشبورد");});next.setOnClickListener(v->{weekOffset++;show("داشبورد");});
  LinearLayout modes=new LinearLayout(this);modes.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  for(String m:new String[]{"امروز","هفته","ماه"}){TextView chip=centerText(m,14,m.equals("امروز")?Color.WHITE:muted,true);chip.setBackground(bg(m.equals("امروز")?purple:fieldColor(),14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(43),1);lp.setMargins(dp(3),dp(2),dp(3),dp(10));modes.addView(chip,lp);chip.setOnClickListener(v->{if(m.equals("امروز")){weekOffset=0;show("داشبورد");}else{monthMode=m.equals("ماه");show("تقویم");}});}cal.addView(modes);
  LinearLayout week=new LinearLayout(this);week.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);String[] days={"شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه"};
  Calendar now=Calendar.getInstance();now.add(Calendar.DAY_OF_YEAR,weekOffset*7);int index=now.get(Calendar.DAY_OF_WEEK)%7;
  for(int i=0;i<7;i++){Calendar c=(Calendar)now.clone();c.add(Calendar.DAY_OF_YEAR,i-index);int[] j=Jalali.fromGregorian(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));boolean active=weekOffset==0&&i==index;String iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());LinearLayout day=column();day.setGravity(Gravity.CENTER);day.setBackground(bg(active?purple:fieldColor(),15));day.addView(centerText(days[i],10,active?Color.WHITE:muted,false));day.addView(centerText(String.valueOf(j[2]),18,active?Color.WHITE:ink,true));TextView dot=centerText("•",15,active?Color.WHITE:purple,true);day.addView(dot);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(84),1);lp.setMargins(dp(2),0,dp(2),0);week.addView(day,lp);day.setOnClickListener(v->dayDetails(iso));}cal.addView(week);body.addView(cal);space(13);
  LinearLayout stats=new LinearLayout(this);stats.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);metric(stats,"جلسات",meetings,"▣",purple);metric(stats,"وظایف",tasks,"☑",0xff37d6a0);metric(stats,"رویداد",events,"◎",0xffffa050);metric(stats,"معوق",overdue,"!",0xffff607b);body.addView(stats);space(13);
  LinearLayout agenda=column();agenda.setPadding(dp(12),dp(10),dp(12),dp(12));agenda.setBackground(bg(panel(),21));sectionHeaderIn(agenda,"برنامه‌های امروز","مشاهده همه",()->show("تقویم"));int todayCount=0;for(ContentValues e:all)if(Recurrence.occurs(e,today)){agendaRow(agenda,e);todayCount++;}if(todayCount==0)agenda.addView(emptyState("امروز برنامه‌ای ثبت نشده است."));body.addView(agenda);space(13);
  LinearLayout taskList=column();taskList.setPadding(dp(12),dp(10),dp(12),dp(12));taskList.setBackground(bg(panel(),21));sectionHeaderIn(taskList,"وظایف امروز ("+done+"/"+tasks+")","+ افزودن وظیفه",()->edit("وظیفه",null));int taskShown=0;for(ContentValues e:all)if("وظیفه".equals(val(e,"kind"))&&(Recurrence.occurs(e,today)||val(e,"date").isEmpty())){taskRow(taskList,e);taskShown++;}if(taskShown==0)taskList.addView(emptyState("وظیفه‌ای برای امروز ثبت نشده است."));body.addView(taskList);space(13);
  LinearLayout eventsBox=column();eventsBox.setPadding(dp(12),dp(10),dp(12),dp(12));eventsBox.setBackground(bg(panel(),21));
  sectionHeaderIn(eventsBox,"رویدادهای ثبت‌شده","مشاهده همه",()->show("رویداد"));
  ArrayList<ContentValues> futureEvents=new ArrayList<>();
  for(ContentValues e:all)if("رویداد".equals(val(e,"kind"))&&nextDate(e,today)!=null)futureEvents.add(e);
  Collections.sort(futureEvents,(a,b)->nextDate(a,today).compareTo(nextDate(b,today)));
  int eventShown=0;for(ContentValues e:futureEvents){eventRow(eventsBox,e,nextDate(e,today));if(++eventShown==5)break;}
  if(eventShown==0)eventsBox.addView(emptyState("رویداد آینده‌ای ثبت نشده است. از بخش رویداد می‌توانید موارد گذشته را هم ببینید."));body.addView(eventsBox);space(13);
  LinearLayout catBox=column();catBox.setPadding(dp(12),dp(10),dp(12),dp(14));catBox.setBackground(bg(panel(),21));sectionHeaderIn(catBox,"دسته‌بندی‌ها","مدیریت دسته‌ها",()->settings());
  for(String name:categories()){if(name.trim().isEmpty())continue;int n=0,ne=0;for(ContentValues e:all)if(name.equals(val(e,"category"))){n++;if("رویداد".equals(val(e,"kind")))ne++;}
   LinearLayout item=new LinearLayout(this);item.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(12),dp(6),dp(12),dp(6));item.setBackground(bg(fieldColor(),13));
   TextView label=text("▤  "+name,15,ink,true);item.addView(label,new LinearLayout.LayoutParams(0,dp(43),1));item.addView(text(n+" برنامه • "+ne+" رویداد",12,purple,true));
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(6);catBox.addView(item,lp);item.setOnClickListener(v->{categoryFilter=name;show("دسته‌بندی");});
  }body.addView(catBox);space(12);
  TextView backup=centerText("پشتیبان‌گیری و بازیابی",13,purple,true);body.addView(backup);backup.setOnClickListener(v->backupMenu());
 }
 TextView emptyState(String message){TextView t=text(message,13,muted,false);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(66));t.setBackground(bg(fieldColor(),15));return t;}
 void sectionHeaderIn(LinearLayout parent,String name,String action,Runnable run){LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);TextView title=text(name,18,ink,true);line.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));TextView link=text(action,12,purple,true);line.addView(link);link.setOnClickListener(v->run.run());parent.addView(line);}
 TextView centerText(String s,int size,int color,boolean bold){TextView t=text(s,size,color,bold);t.setGravity(Gravity.CENTER);return t;}
 void space(int height){Space sp=new Space(this);body.addView(sp,new LinearLayout.LayoutParams(1,dp(height)));}
 void sectionHeader(String name,String action,Runnable run){LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);line.setGravity(Gravity.CENTER_VERTICAL);TextView heading=text(name,18,ink,true);line.addView(heading,new LinearLayout.LayoutParams(0,dp(52),1));TextView link=text(action,12,purple,true);line.addView(link);link.setOnClickListener(v->run.run());body.addView(line);}
 void agendaRow(LinearLayout parent,ContentValues e){LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row.setGravity(Gravity.CENTER_VERTICAL);TextView marker=centerText("▌",23,"وظیفه".equals(val(e,"kind"))?0xff55a4ff:purple,true);row.addView(marker,new LinearLayout.LayoutParams(dp(26),dp(60)));LinearLayout details=column();details.addView(text(val(e,"title"),15,ink,true));details.addView(text(val(e,"category")+"  •  "+val(e,"kind"),12,muted,false));row.addView(details,new LinearLayout.LayoutParams(0,-2,1));row.addView(text(val(e,"time"),13,purple,true));parent.addView(row);row.setOnClickListener(v->detail(e));}
 void eventRow(LinearLayout parent,ContentValues e,String iso){
  LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(6),dp(6),dp(6),dp(6));
  TextView badge=centerText("◎",23,purple,true);row.addView(badge,new LinearLayout.LayoutParams(dp(32),dp(55)));
  LinearLayout info=column();info.addView(text(val(e,"title"),15,ink,true));info.addView(text(val(e,"category").isEmpty()?"بدون دسته‌بندی":val(e,"category"),12,muted,false));row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
  row.addView(text(Jalali.display(iso)+(val(e,"time").isEmpty()?"":" • "+val(e,"time")),12,purple,true));parent.addView(row);row.setOnClickListener(v->detail(e));
 }
 void taskRow(LinearLayout parent,ContentValues e){LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);line.setGravity(Gravity.CENTER_VERTICAL);CheckBox cb=new CheckBox(this);styleInput(cb);boolean checked="انجام‌شده".equals(val(e,"status"));cb.setChecked(checked);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(purple));line.addView(cb);TextView name=text(val(e,"title"),14,checked?muted:ink,false);if(checked)name.setPaintFlags(name.getPaintFlags()|android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);line.addView(name,new LinearLayout.LayoutParams(0,dp(55),1));TextView tag=text(val(e,"category"),11,purple,true);line.addView(tag);parent.addView(line);cb.setOnCheckedChangeListener((v,on)->{e.put("status",on?"انجام‌شده":"انجام‌نشده");db.save(e,e.getAsLong("id"));show(tab);});name.setOnClickListener(v->detail(e));}
 void metric(LinearLayout parent,String label,int count,String symbol,int color){LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setPadding(dp(3),dp(8),dp(3),dp(8));box.setBackground(bg(panel(),18));TextView icon=centerText(symbol,24,color,true);box.addView(icon);box.addView(centerText(String.valueOf(count),23,ink,true));box.addView(centerText(label,12,muted,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(113),1);lp.setMargins(dp(2),0,dp(2),0);parent.addView(box,lp);}
 void chooseType(){dialogBuilder().setTitle("نوع برنامه").setItems(new String[]{"جلسه","وظیفه","رویداد"},(d,w)->edit(new String[]{"جلسه","وظیفه","رویداد"}[w],null)).show();}
 void listing(String kind){
  if(kind.equals("همه"))body.addView(text("دسته‌بندی: "+categoryFilter,20,purple,true));
  Button add=button(kind.equals("همه")?"+ افزودن برنامه":"+ افزودن "+kind);body.addView(add);add.setOnClickListener(v->{if(kind.equals("همه"))chooseType();else edit(kind,null);});
  ArrayList<ContentValues> all=db.all(kind);
  searchBox();
  int visible=0;for(ContentValues e:all)if(matches(e)&&(kind.equals("همه")?categoryFilter.equals(val(e,"category")):true)){entryCard(e);visible++;}
  if(visible==0)card(body,text("برنامه‌ای مطابق فیلتر انتخاب‌شده پیدا نشد.",15,muted,false));
 }
 Button button(String label){Button b=new Button(this);styleInput(b);b.setText(fa(label));b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(bg(purple,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.bottomMargin=dp(12);b.setLayoutParams(p);return b;}
 void entryCard(ContentValues e){
  LinearLayout item=column();
  String title=e.getAsString("title"),kind=e.getAsString("kind");
  item.addView(text(("جلسه".equals(kind)?"▣  ":"وظیفه".equals(kind)?"☑  ":"◉  ")+title,17,ink,true));
  String date=e.getAsString("date"),time=e.getAsString("time"),status=e.getAsString("status");
  item.addView(text(Jalali.display(date)+(time==null||time.isEmpty()?"":"  |  "+time)+(status==null||status.isEmpty()?"":"  |  "+status),13,muted,false));
  if("جلسه".equals(kind)){
   LinearLayout controls=new LinearLayout(this);controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
   Button state=button("وضعیت: "+(val(e,"status").isEmpty()?"برنامه‌ریزی‌شده":val(e,"status")));
   controls.addView(state,new LinearLayout.LayoutParams(0,dp(46),1));state.setOnClickListener(v->meetingStatus(e));item.addView(controls);
  }
  if("وظیفه".equals(kind)){
   LinearLayout controls=new LinearLayout(this);controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
   CheckBox done=new CheckBox(this);styleInput(done);done.setText("انجام شد");done.setTextColor(ink);done.setChecked("انجام‌شده".equals(status));controls.addView(done);
   done.setOnCheckedChangeListener((v,checked)->{e.put("status",checked?"انجام‌شده":"انجام‌نشده");db.save(e,e.getAsLong("id"));show(tab);});
   if(!val(e,"checklist").isEmpty()){Button checklist=button("☑ چک‌لیست");controls.addView(checklist,new LinearLayout.LayoutParams(0,dp(48),1));checklist.setOnClickListener(v->checklistDialog(e));}
   item.addView(controls);
  }
  card(body,item);item.setOnClickListener(v->detail(e));
 }
 void meetingStatus(ContentValues e){
  String[] states={"برنامه‌ریزی‌شده","برگزارشده","به‌تعویق‌افتاده","لغوشده"};
  dialogBuilder().setTitle("وضعیت جلسه").setSingleChoiceItems(states,java.util.Arrays.asList(states).indexOf(val(e,"status")),(d,w)->{
   e.put("status",states[w]);db.save(e,e.getAsLong("id"));d.dismiss();show(tab);
  }).setNegativeButton("انصراف",null).show();
 }
 String val(ContentValues e,String k){return e==null?"":(e.getAsString(k)==null?"":e.getAsString(k));}
 void detail(ContentValues e){
  LinearLayout l=column();l.setPadding(dp(16),dp(8),dp(16),dp(8));
  String[][] fields={{"reminder_minutes","زمان یادآوری"},{"kind","نوع"},{"title","عنوان"},{"date","تاریخ شمسی"},{"time","ساعت شروع"},{"endtime","ساعت پایان"},{"notes","توضیحات"},{"location","مکان"},{"people","شرکت‌کنندگان / مسئول"},{"priority","اولویت"},{"status","وضعیت"},{"category","دسته‌بندی"},{"repeat_rule","تکرار"},{"checklist","چک‌لیست / زیر‌وظایف"},{"decisions","مصوبات و اقدامات بعدی"}};
  for(String[] f:fields){String value=f[0].equals("date")?Jalali.display(val(e,"date")):val(e,f[0]);if(f[0].equals("reminder_minutes"))value=reminderLabel(value);if(!value.isEmpty())l.addView(text(f[1]+":  "+value,15,ink,false));}
  ScrollView sc=new ScrollView(this);sc.addView(l);
  if("جلسه".equals(val(e,"kind"))){Button convert=button("تبدیل مصوبه به وظیفه");l.addView(convert);convert.setOnClickListener(v->createAction(e));}
  dialogBuilder().setTitle("جزئیات برنامه").setView(sc).setPositiveButton("ویرایش",(d,w)->edit(val(e,"kind"),e)).setNeutralButton("حذف",(d,w)->dialogBuilder().setMessage("این مورد حذف شود؟").setPositiveButton("بله",(a,b)->{cancelReminder(e.getAsLong("id"));db.delete(e.getAsLong("id"));show(tab);}).setNegativeButton("خیر",null).show()).setNegativeButton("بستن",null).show();
 }
 void edit(String kind,ContentValues old){
  ScrollView sc=new ScrollView(this);LinearLayout fields=column();fields.setPadding(dp(18),dp(8),dp(18),dp(12));sc.addView(fields);
  LinkedHashMap<String,EditText> inputs=new LinkedHashMap<>();
  addField(fields,inputs,"title","عنوان *",old,false);
  addDate(fields,inputs,old);
  addTime(fields,inputs,"time","ساعت شروع",old);
  if(!kind.equals("وظیفه"))addTime(fields,inputs,"endtime","ساعت پایان",old);
  addField(fields,inputs,"notes","توضیحات",old,true);
  if(!kind.equals("وظیفه"))addField(fields,inputs,"location","مکان / لینک جلسه",old,false);
  addField(fields,inputs,"people",kind.equals("جلسه")?"شرکت‌کنندگان":kind.equals("وظیفه")?"مسئول":"افراد مرتبط",old,false);
  addChoice(fields,inputs,"category","دسته‌بندی",categories(),old);
  if(kind.equals("وظیفه")){addChoice(fields,inputs,"priority","اولویت",new String[]{"کم","متوسط","زیاد","فوری"},old);addChoice(fields,inputs,"status","وضعیت",new String[]{"انجام‌نشده","درحال انجام","انجام‌شده","متوقف"},old);addField(fields,inputs,"checklist","زیر‌وظایف (هر خط یک مورد؛ پس از ذخیره قابل تیک‌زدن)",old,true);}
  if(kind.equals("جلسه"))addChoice(fields,inputs,"status","وضعیت جلسه",new String[]{"برنامه‌ریزی‌شده","برگزارشده","به‌تعویق‌افتاده","لغوشده"},old);
  addChoice(fields,inputs,"repeat_rule","تکرار (اطلاعات تکرار ثبت می‌شود)",new String[]{"بدون تکرار","روزانه","هفتگی","ماهانه","سالانه"},old);
  addReminderChoice(fields,inputs,old);
  if(kind.equals("جلسه"))addField(fields,inputs,"decisions","دستور جلسه، مصوبات و اقدامات بعدی",old,true);
  AlertDialog dialog=dialogBuilder().setTitle((old==null?"ثبت ":"ویرایش ")+kind).setView(sc).setPositiveButton("ذخیره",null).setNegativeButton("انصراف",null).create();
  dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{
   if(inputs.get("title").getText().toString().trim().isEmpty()){inputs.get("title").setError("عنوان الزامی است");return;}
   String tm=en(inputs.get("time").getText().toString().trim());
   if(!tm.isEmpty()&&!validTime(tm)){inputs.get("time").setError("ساعت باید به صورت 09:30 باشد");return;}
   if(inputs.containsKey("endtime")){String end=en(inputs.get("endtime").getText().toString().trim());if(!end.isEmpty()&&!validTime(end)){inputs.get("endtime").setError("ساعت پایان نامعتبر است");return;}}
   ContentValues data=new ContentValues();data.put("kind",kind);
   for(Map.Entry<String,EditText> it:inputs.entrySet()){String value=en(it.getValue().getText().toString().trim());if(it.getKey().equals("date")&&!value.isEmpty()){try{String[] p=value.split("/");int jy=Integer.parseInt(p[0]),jm=Integer.parseInt(p[1]),jd=Integer.parseInt(p[2]);
     if(jy<1300||jy>1500||jm<1||jm>12||jd<1||jd>Jalali.monthLength(jy,jm))throw new IllegalArgumentException("Invalid Jalali date");
     value=Jalali.iso(jy,jm,jd);}catch(Exception ex){Toast.makeText(this,"تاریخ نامعتبر است",Toast.LENGTH_SHORT).show();return;}}if(it.getKey().equals("reminder_minutes"))value=reminderValue(value);data.put(it.getKey(),value);}
   long saved=db.save(data,old==null?-1:old.getAsLong("id"));cancelReminder(saved);ReminderScheduler.schedule(this,saved,data);dialog.dismiss();show(tab);
  }));dialog.show();
 }
 boolean validTime(String value){if(!value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))return false;return true;}
 void addField(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,ContentValues old,boolean multiline){
  parent.addView(text(label,14,ink,true));EditText e=new EditText(this);styleInput(e);e.setText(fa(val(old,key)));e.setTextSize(15);e.setSingleLine(!multiline);e.setTextDirection(View.TEXT_DIRECTION_RTL);e.setGravity(Gravity.RIGHT|Gravity.TOP);e.setBackground(bg(fieldColor(),10));e.setTextColor(ink);e.setHintTextColor(muted);e.setPadding(dp(12),dp(12),dp(12),dp(12));if(multiline)e.setMinLines(3);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);parent.addView(e,p);inputs.put(key,e);
 }
 void addTime(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,ContentValues old){addField(parent,inputs,key,label+" (برای انتخاب لمس کنید)",old,false);EditText e=inputs.get(key);e.setFocusable(false);e.setOnClickListener(v->timePicker(e));}
 void addChoice(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,String[] opts,ContentValues old){
  addField(parent,inputs,key,label+" (برای انتخاب لمس کنید)",old,false);
  EditText e=inputs.get(key);e.setFocusable(false);e.setOnClickListener(v->dialogBuilder().setTitle(label).setItems(opts,(d,w)->e.setText(opts[w])).show());
 }
 String reminderLabel(String value){switch(en(value)){
  case "-1":return "بدون یادآوری";case "0":return "زمان شروع";case "60":return "۱ ساعت قبل";case "180":return "۳ ساعت قبل";case "1440":return "۱ روز قبل";case "2880":return "۲ روز قبل";
  case "":return "بدون یادآوری";default:return fa(en(value))+" دقیقه قبل";
 }}
 String reminderValue(String value){String v=en(value);if(v.equals("بدون یادآوری"))return "-1";if(v.equals("زمان شروع"))return "0";if(v.equals("۱ ساعت قبل")||v.equals("1 ساعت قبل"))return "60";
  if(v.equals("۳ ساعت قبل")||v.equals("3 ساعت قبل"))return "180";if(v.equals("۱ روز قبل")||v.equals("1 روز قبل"))return "1440";if(v.equals("۲ روز قبل")||v.equals("2 روز قبل"))return "2880";
  if(v.matches("[0-9]+ دقیقه قبل"))return v.substring(0,v.indexOf(' '));return v;
 }
 void addReminderChoice(LinearLayout parent,LinkedHashMap<String,EditText> inputs,ContentValues old){
  addField(parent,inputs,"reminder_minutes","یادآوری قبل از شروع (برای انتخاب لمس کنید)",old,false);
  EditText e=inputs.get("reminder_minutes");e.setText(reminderLabel(val(old,"reminder_minutes").isEmpty()?"-1":val(old,"reminder_minutes")));e.setFocusable(false);
  String[] labels={"بدون یادآوری","زمان شروع","۱ ساعت قبل","۳ ساعت قبل","۱ روز قبل","۲ روز قبل"};
  e.setOnClickListener(v->dialogBuilder().setTitle("زمان یادآوری").setItems(labels,(d,w)->e.setText(labels[w])).show());
 }
 void cancelReminder(long id){
  Intent intent=new Intent(this,ReminderReceiver.class);
  PendingIntent pi=PendingIntent.getBroadcast(this,(int)id,intent,PendingIntent.FLAG_NO_CREATE|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
  if(pi!=null){((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(pi);pi.cancel();}
 }
 void scheduleReminder(long id,ContentValues data){cancelReminder(id);ReminderScheduler.schedule(this,id,data);}
 void addDate(LinearLayout parent,LinkedHashMap<String,EditText> inputs,ContentValues old){
  addField(parent,inputs,"date","تاریخ شمسی (برای انتخاب لمس کنید)",old,false);
  EditText e=inputs.get("date");e.setFocusable(false);e.setText(val(old,"date").isEmpty()?"":fa(Jalali.display(val(old,"date"))));
  e.setOnClickListener(v->jalaliPicker(e));
 }
 void jalaliPicker(EditText e){
  Calendar now=Calendar.getInstance();int[] today=Jalali.fromGregorian(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
  String current=en(e.getText().toString());if(current.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}")){String[] q=current.split("/");today=new int[]{Integer.parseInt(q[0]),Integer.parseInt(q[1]),Integer.parseInt(q[2])};}
  final int[] chosen=today;LinearLayout wrap=column();wrap.setPadding(dp(20),dp(10),dp(20),dp(10));
  Context pickerContext=new android.view.ContextThemeWrapper(this,dark?android.R.style.Theme_Material_Dialog_Alert:android.R.style.Theme_Material_Light_Dialog_Alert);
  NumberPicker year=new NumberPicker(pickerContext),month=new NumberPicker(pickerContext),day=new NumberPicker(pickerContext);
  year.setMinValue(1300);year.setMaxValue(1500);year.setFormatter(n->fa(String.valueOf(n)));year.setValue(chosen[0]);
  month.setMinValue(1);month.setMaxValue(12);month.setDisplayedValues(new String[]{"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"});month.setValue(chosen[1]);
  day.setMinValue(1);day.setMaxValue(31);day.setFormatter(n->fa(String.valueOf(n)));day.setValue(chosen[2]);
  LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  line.addView(day,new LinearLayout.LayoutParams(0,dp(170),1));line.addView(month,new LinearLayout.LayoutParams(0,dp(170),2));line.addView(year,new LinearLayout.LayoutParams(0,dp(170),1));wrap.addView(line);
  dialogBuilder().setTitle("انتخاب تاریخ شمسی").setView(wrap).setPositiveButton("تأیید",(d,w)->{
   int y=year.getValue(),m=month.getValue();int max=Jalali.monthLength(y,m);int dd=Math.min(day.getValue(),max);
   e.setText(fa(String.format(Locale.US,"%04d/%02d/%02d",y,m,dd)));
  }).setNegativeButton("انصراف",null).show();
 }
 boolean matches(ContentValues e){String q=en(search.trim()).toLowerCase(Locale.ROOT);if(q.isEmpty())return true;return (val(e,"title")+" "+val(e,"notes")+" "+val(e,"people")+" "+val(e,"category")).toLowerCase(Locale.ROOT).contains(q);}
 void searchBox(){EditText field=new EditText(this);styleInput(field);field.setSingleLine(true);field.setHint("جست‌وجوی عنوان، توضیح، مسئول یا پروژه");field.setText(fa(search));field.setHintTextColor(muted);field.setTextDirection(View.TEXT_DIRECTION_RTL);field.setPadding(dp(12),dp(10),dp(12),dp(10));field.setBackground(bg(panel(),12));field.setTextColor(ink);body.addView(field);field.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();}public void afterTextChanged(android.text.Editable x){}});
  Button find=button("نمایش نتایج جست‌وجو");body.addView(find);find.setOnClickListener(v->show(tab));
 }
 boolean isFriday(String iso){
  try{SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd",Locale.US);sdf.setLenient(false);Calendar c=Calendar.getInstance();c.setTime(sdf.parse(iso));return c.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY;}catch(Exception e){return false;}
 }
 void calendarPage(){
  Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,weekOffset*7);int dow=c.get(Calendar.DAY_OF_WEEK)%7;c.add(Calendar.DAY_OF_YEAR,-dow);
  Button switcher=button("نمایش تقویم ماهانه");body.addView(switcher);switcher.setOnClickListener(v->{monthMode=true;show("تقویم");});
  LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  Button prev=button("هفته قبل"),next=button("هفته بعد");line.addView(prev,new LinearLayout.LayoutParams(0,dp(48),1));line.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));body.addView(line);
  prev.setOnClickListener(v->{weekOffset--;show("تقویم");});next.setOnClickListener(v->{weekOffset++;show("تقویم");});
  ArrayList<ContentValues> entries=db.all("همه");String[] weekdays={"شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه"};
  for(int i=0;i<7;i++){
   String iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());int[] j=Jalali.fromGregorian(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));
   boolean today=iso.equals(todayIso());LinearLayout section=column();section.addView(text((today?"● امروز • ":"")+weekdays[i]+"  •  "+Jalali.display(iso),17,today?purple:ink,true));
   for(Occasions.Occasion o:Occasions.forDate(j[0],j[1],j[2]))section.addView(text((o.holiday?"● تعطیل رسمی • ":"◦ ")+o.title,13,o.holiday?0xfff2807e:muted,false));
   int count=0;for(ContentValues e:entries){if(Recurrence.occurs(e,iso)){TextView item=text("• "+val(e,"title")+"  "+val(e,"time"),15,ink,false);section.addView(item);item.setOnClickListener(v->detail(e));count++;}}
   if(count==0)section.addView(text("برنامه‌ای ثبت نشده",13,muted,false));card(body,section);c.add(Calendar.DAY_OF_YEAR,1);
  }
 }
 void monthPage(){
  Button switcher=button("نمایش برنامه هفتگی");body.addView(switcher);switcher.setOnClickListener(v->{monthMode=false;show("تقویم");});
  TextView goToday=text("◎ بازگشت به امروز",14,purple,true);goToday.setGravity(Gravity.CENTER);body.addView(goToday,new LinearLayout.LayoutParams(-1,dp(38)));
  goToday.setOnClickListener(v->{monthOffset=0;weekOffset=0;show("تقویم");});
  Calendar now=Calendar.getInstance();int[] j=Jalali.fromGregorian(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
  int total=j[0]*12+j[1]-1+monthOffset;int year=Math.floorDiv(total,12),month=Math.floorMod(total,12)+1;
  LinearLayout controls=new LinearLayout(this);controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  Button previous=button("ماه قبل"),next=button("ماه بعد");controls.addView(previous,new LinearLayout.LayoutParams(0,dp(48),1));controls.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));body.addView(controls);
  previous.setOnClickListener(v->{monthOffset--;show("تقویم");});next.setOnClickListener(v->{monthOffset++;show("تقویم");});
  TextView heading=text(jalaliMonth(month)+" "+year,22,purple,true);body.addView(heading);
  TextView legend=text("● امروز     • برنامه ثبت‌شده     قرمز: تعطیل",12,muted,false);body.addView(legend);
  int[] g=Jalali.toGregorian(year,month,1);Calendar first=Calendar.getInstance();first.clear();first.set(g[0],g[1]-1,g[2]);int offset=first.get(Calendar.DAY_OF_WEEK)%7;
  int count=Jalali.monthLength(year,month);
  ArrayList<ContentValues> all=db.all("همه");String[] weekdays={"ش","ی","د","س","چ","پ","ج"};
  LinearLayout headers=new LinearLayout(this);headers.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);for(String w:weekdays){TextView h=centerText(w,13,muted,true);headers.addView(h,new LinearLayout.LayoutParams(0,dp(34),1));}body.addView(headers);
  int cells=((offset+count+6)/7)*7;LinearLayout row=null;
  for(int i=0;i<cells;i++){
   if(i%7==0){row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);body.addView(row);}
   int day=i-offset+1;TextView cell=centerText(day>0&&day<=count?""+day:"",15,ink,false);
   LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(62),1);cp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(cell,cp);
   if(day>0&&day<=count){
    String iso=Jalali.iso(year,month,day);int events=0;for(ContentValues e:all)if(Recurrence.occurs(e,iso))events++;
    boolean today=iso.equals(todayIso());boolean holiday=isFriday(iso)||Occasions.holiday(year,month,day);
    cell.setBackground(bg(today?purple:holiday?(dark?0xff4b292d:0xffffe8e6):panel(),11));
    cell.setText(fa(""+day+(events>0?"\n• "+events:"")));
    cell.setTextColor(today?Color.WHITE:holiday?(dark?0xffffb0aa:0xffbf3c38):events>0?purple:ink);
    if(today)cell.setTypeface(iranBold);
    cell.setOnClickListener(v->dayDetails(iso));
   }else cell.setBackground(bg(surface,11));
  }
  body.addView(text("مناسبت‌ها و تعطیلات این ماه",18,ink,true));
  int occasionCount=0;
  for(int d=1;d<=count;d++){
   for(Occasions.Occasion occ:Occasions.forDate(year,month,d)){
    LinearLayout tile=column();tile.addView(text(d+" "+jalaliMonth(month)+"  •  "+occ.title,14,occ.holiday?0xffe67470:ink,occ.holiday));
    if(occ.holiday)tile.addView(text("تعطیل رسمی",12,0xffe67470,false));card(body,tile);occasionCount++;
   }
  }
  if(occasionCount==0)body.addView(text("مناسبت ثابتی برای این ماه در داده‌های آفلاین ثبت نشده است.",13,muted,false));
  if(year!=1405)body.addView(text("توجه: تعطیلات و مناسبت‌های قمری فقط برای سال ۱۴۰۵ در نسخه آفلاین موجود است.",12,muted,false));
  else body.addView(text("مناسبت‌های قمری ممکن است با اعلام رسمی رؤیت هلال تغییر کنند.",12,muted,false));
 }
 void dayDetails(String iso){
  LinearLayout list=column();list.setPadding(dp(12),dp(10),dp(12),dp(10));
  String[] parts=Jalali.display(iso).split("/");
  try{int y=Integer.parseInt(parts[0]),m=Integer.parseInt(parts[1]),d=Integer.parseInt(parts[2]);
   if(isFriday(iso))list.addView(text("● جمعه • تعطیل هفتگی",14,0xffe67470,true));
   for(Occasions.Occasion occ:Occasions.forDate(y,m,d))list.addView(text((occ.holiday?"● تعطیل رسمی • ":"◦ ")+occ.title,15,occ.holiday?0xffe67470:ink,occ.holiday));
  }catch(Exception ignored){}
  int count=0;for(ContentValues e:db.all("همه")){if(Recurrence.occurs(e,iso)){TextView item=text(val(e,"kind")+" • "+val(e,"title")+"  "+val(e,"time"),16,purple,true);list.addView(item);item.setOnClickListener(v->detail(e));count++;}}
  if(count==0)list.addView(text("برنامه‌ای برای این روز وجود ندارد",14,muted,false));
  dialogBuilder().setTitle("برنامه‌های "+fa(Jalali.display(iso))).setView(list).setPositiveButton("بستن",null).show();
 }

 void createAction(ContentValues meeting){
  final EditText title=new EditText(this);styleInput(title);title.setHint("عنوان اقدام");title.setSingleLine(true);title.setTextColor(ink);title.setHintTextColor(muted);title.setBackground(bg(fieldColor(),10));title.setPadding(dp(12),dp(12),dp(12),dp(12));
  final EditText owner=new EditText(this);styleInput(owner);owner.setHint("مسئول اقدام");owner.setTextColor(ink);owner.setHintTextColor(muted);owner.setBackground(bg(fieldColor(),10));owner.setPadding(dp(12),dp(12),dp(12),dp(12));
  LinearLayout panel=column();panel.setPadding(dp(20),dp(10),dp(20),dp(10));panel.addView(title);panel.addView(owner);
  AlertDialog dialog=dialogBuilder().setTitle("اقدام مرتبط با جلسه").setView(panel).setPositiveButton("ثبت وظیفه",null).setNegativeButton("انصراف",null).create();
  dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String t=title.getText().toString().trim();if(t.isEmpty()){title.setError("عنوان الزامی است");return;}
   ContentValues task=new ContentValues();task.put("kind","وظیفه");task.put("title",t);task.put("people",owner.getText().toString().trim());task.put("date",val(meeting,"date"));task.put("status","انجام‌نشده");task.put("priority","متوسط");task.put("notes","مرتبط با جلسه: "+val(meeting,"title")+" (شناسه "+meeting.getAsLong("id")+")");
   db.save(task,-1);dialog.dismiss();Toast.makeText(this,"وظیفه ایجاد شد",Toast.LENGTH_SHORT).show();show("وظیفه");
  }));dialog.show();
 }
 void reportPage(){
  ArrayList<ContentValues> all=db.all("همه");String today=todayIso();
  Calendar startWeek=Calendar.getInstance();startWeek.set(Calendar.HOUR_OF_DAY,0);startWeek.set(Calendar.MINUTE,0);startWeek.set(Calendar.SECOND,0);int dow=startWeek.get(Calendar.DAY_OF_WEEK)%7;startWeek.add(Calendar.DAY_OF_MONTH,-dow);
  String weekStart=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(startWeek.getTime());
  int[] month=Jalali.fromGregorian(nowYear(),nowMonth(),nowDay());String monthStart=Jalali.iso(month[0],month[1],1);
  TextView choose=centerText("بازه گزارش: "+reportPeriod+"  ▾",16,purple,true);choose.setBackground(bg(panel(),12));body.addView(choose,new LinearLayout.LayoutParams(-1,dp(55)));
  choose.setOnClickListener(v->dialogBuilder().setTitle("بازه گزارش").setItems(new String[]{"همه","این ماه","این هفته"},(d,w)->{reportPeriod=new String[]{"همه","این ماه","این هفته"}[w];show("گزارش");}).show());space(12);
  int tasks=0,done=0,inProgress=0,overdue=0,high=0,meetings=0,held=0,postponed=0,canceled=0,events=0,coming=0;
  long meetingMinutes=0;LinkedHashMap<String,Integer> categoryCounts=new LinkedHashMap<>();
  for(String c:categories())if(!c.trim().isEmpty())categoryCounts.put(c,0);
  Calendar next7=Calendar.getInstance();next7.add(Calendar.DATE,7);String weekEnd=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(next7.getTime());
  for(ContentValues e:all){
   String date=val(e,"date");if(reportPeriod.equals("این ماه")&&(date.isEmpty()||date.compareTo(monthStart)<0||date.compareTo(Jalali.iso(month[0],month[1],Jalali.monthLength(month[0],month[1])))>0))continue;
   if(reportPeriod.equals("این هفته")){Calendar c=(Calendar)startWeek.clone();c.add(Calendar.DATE,6);String weekLast=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());if(date.isEmpty()||date.compareTo(weekStart)<0||date.compareTo(weekLast)>0)continue;}
   String category=val(e,"category");if(category.isEmpty())category="بدون دسته‌بندی";
   categoryCounts.put(category,categoryCounts.containsKey(category)?categoryCounts.get(category)+1:1);
   String kind=val(e,"kind"),status=val(e,"status");
   if(kind.equals("وظیفه")){
    tasks++;if(status.equals("انجام‌شده"))done++;else if(status.equals("درحال انجام"))inProgress++;
    if(overdue(e,today))overdue++;if(val(e,"priority").equals("فوری")||val(e,"priority").equals("زیاد"))high++;
   }else if(kind.equals("جلسه")){
    meetings++;if(status.equals("برگزارشده"))held++;else if(status.equals("به‌تعویق‌افتاده"))postponed++;else if(status.equals("لغوشده"))canceled++;
    if(status.equals("برگزارشده")){try{String[] a=en(val(e,"time")).split(":"),b=en(val(e,"endtime")).split(":");long diff=(Integer.parseInt(b[0])*60+Integer.parseInt(b[1]))-(Integer.parseInt(a[0])*60+Integer.parseInt(a[1]));if(diff>0)meetingMinutes+=diff;}catch(Exception ignored){}}
   }else if(kind.equals("رویداد")){events++;String occurrence=nextDate(e,today);if(occurrence!=null&&occurrence.compareTo(weekEnd)<=0)coming++;}
  }
  int total=tasks+meetings+events;int percent=tasks==0?0:done*100/tasks;
  LinearLayout metrics=new LinearLayout(this);metrics.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  metric(metrics,"کل برنامه‌ها",total,"◈",purple);metric(metrics,"وظایف",tasks,"☑",0xff42c59a);metric(metrics,"جلسات",meetings,"▣",0xffe6b264);metric(metrics,"رویدادها",events,"◎",0xff58b1ed);body.addView(metrics);space(10);
  LinearLayout progress=column();progress.addView(text("پیشرفت وظایف",19,ink,true));progress.addView(text(percent+"٪ تکمیل‌شده • "+done+" از "+tasks+" وظیفه",16,purple,true));
  ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);bar.setProgress(percent);bar.setProgressTintList(android.content.res.ColorStateList.valueOf(purple));
  LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(10));bp.setMargins(0,dp(12),0,dp(12));progress.addView(bar,bp);
  progress.addView(text("در حال انجام: "+inProgress+"    •    معوق: "+overdue+"    •    اولویت بالا: "+high,13,muted,false));card(body,progress);
  LinearLayout meetingBox=column();meetingBox.addView(text("وضعیت جلسات",19,ink,true));
  meetingBox.addView(text("برگزارشده: "+held+"    •    برنامه‌ریزی‌شده: "+Math.max(0,meetings-held-postponed-canceled),15,ink,false));
  meetingBox.addView(text("به‌تعویق‌افتاده: "+postponed+"    •    لغوشده: "+canceled,15,ink,false));
  meetingBox.addView(text("مدت جلسات برگزارشده: "+(meetingMinutes/60)+" ساعت و "+(meetingMinutes%60)+" دقیقه",14,purple,true));card(body,meetingBox);
  LinearLayout eventBox=column();eventBox.addView(text("رویدادها",19,ink,true));eventBox.addView(text("رویدادهای این بازه: "+events,15,ink,false));eventBox.addView(text("رویدادهای ۷ روز آینده در همین بازه: "+coming,14,muted,false));card(body,eventBox);
  LinearLayout categoryBox=column();categoryBox.addView(text("گزارش تفکیکی دسته‌بندی‌ها",19,ink,true));
  int max=1;for(int v:categoryCounts.values())if(v>max)max=v;
  for(Map.Entry<String,Integer> item:categoryCounts.entrySet()){
   if(item.getValue()==0)continue;
   categoryBox.addView(text(item.getKey()+"  •  "+item.getValue()+" برنامه",14,ink,false));
   ProgressBar pbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pbar.setMax(max);pbar.setProgress(item.getValue());pbar.setProgressTintList(android.content.res.ColorStateList.valueOf(purple));
   LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(7));pp.bottomMargin=dp(10);categoryBox.addView(pbar,pp);
  }
  if(total==0)categoryBox.addView(text("در این بازه اطلاعاتی ثبت نشده است.",14,muted,false));card(body,categoryBox);
  String summary="گزارش زمان‌یار • "+reportPeriod+"\nکل برنامه‌ها: "+total+"\nوظایف: "+tasks+"\nتکمیل‌شده: "+done+"\nوظایف معوق: "+overdue+"\nجلسات: "+meetings+"\nبرگزارشده: "+held+"\nبه‌تعویق‌افتاده: "+postponed+"\nرویدادها: "+events;
  Button share=button("اشتراک‌گذاری خلاصه گزارش");body.addView(share);share.setOnClickListener(v->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,fa(summary));startActivity(Intent.createChooser(intent,"ارسال گزارش"));});
  body.addView(text("گزارش براساس تاریخ برنامه‌های ثبت‌شده است و دفعات تکرار هر برنامه جداگانه شمارش نشده‌اند.",12,muted,false));
 }

 void backupMenu(){dialogBuilder().setTitle("پشتیبان‌گیری آفلاین").setItems(new String[]{"ذخیره نسخه پشتیبان JSON","بازیابی از فایل JSON"},(d,n)->{Intent i=new Intent(n==0?Intent.ACTION_CREATE_DOCUMENT:Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");if(n==0)i.putExtra(Intent.EXTRA_TITLE,"zamanyar-backup.json");startActivityForResult(i,n==0?BACKUP_CREATE:BACKUP_OPEN);}).show();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
  try{if(request==BACKUP_CREATE){org.json.JSONArray arr=new org.json.JSONArray();for(ContentValues v:db.all("همه")){org.json.JSONObject obj=new org.json.JSONObject();for(String key:v.keySet())if(!key.equals("id"))obj.put(key,v.getAsString(key));arr.put(obj);}try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(arr.toString(2).getBytes("UTF-8"));}Toast.makeText(this,"پشتیبان ذخیره شد",Toast.LENGTH_LONG).show();}
  else if(request==BACKUP_OPEN){java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=getContentResolver().openInputStream(data.getData())){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){buffer.write(b,0,n);if(buffer.size()>10_000_000)throw new Exception("فایل بیش از حد بزرگ است");}}org.json.JSONArray arr=new org.json.JSONArray(buffer.toString("UTF-8"));dialogBuilder().setTitle("بازیابی اطلاعات").setMessage(fa("تعداد "+arr.length()+" مورد به اطلاعات موجود اضافه شود؟ ممکن است موارد تکراری ایجاد شوند.")).setPositiveButton("بازیابی",(d,w)->{try{int saved=0;for(int i=0;i<arr.length();i++){org.json.JSONObject obj=arr.getJSONObject(i);String kind=obj.optString("kind");if(!kind.equals("جلسه")&&!kind.equals("وظیفه")&&!kind.equals("رویداد"))continue;ContentValues values=new ContentValues();for(java.util.Iterator<String> keys=obj.keys();keys.hasNext();){String key=keys.next();if(!key.equals("id"))values.put(key,obj.optString(key));}long id=db.save(values,-1);scheduleReminder(id,values);saved++;}Toast.makeText(this,fa(saved+" مورد بازیابی شد"),Toast.LENGTH_LONG).show();show("داشبورد");}catch(Exception ex){Toast.makeText(this,"خطا در بازیابی",Toast.LENGTH_LONG).show();}}).setNegativeButton("انصراف",null).show();}}
  catch(Exception ex){Toast.makeText(this,"خطا: "+ex.getMessage(),Toast.LENGTH_LONG).show();}
 }

}
