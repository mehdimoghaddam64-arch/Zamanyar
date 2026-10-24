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
 int panel(){return dark?0xff1c202b:Color.WHITE;} int fieldColor(){return dark?0xff282d3a:0xfff0eff7;}
 String fa(String value){return value.replace('0','۰').replace('1','۱').replace('2','۲').replace('3','۳').replace('4','۴').replace('5','۵').replace('6','۶').replace('7','۷').replace('8','۸').replace('9','۹');}
 String[] categories(){String raw=prefs.getString("categories","مالی\nکاری\nمارکتینگ\nخرید\nشخصی");return raw.split("\n");}
 void settings(){
  LinearLayout panel=column();panel.setPadding(dp(18),dp(12),dp(18),dp(12));
  Switch mode=new Switch(this);styleInput(mode);mode.setText("حالت تیره");mode.setTextColor(ink);mode.setChecked(dark);panel.addView(mode);
  mode.setOnCheckedChangeListener((v,checked)->{prefs.edit().putBoolean("dark",checked).apply();dark=checked;applyTheme();show("داشبورد");});
  panel.addView(text("دسته‌بندی‌ها (هر سطر یک دسته)",16,ink,true));
  EditText list=new EditText(this);styleInput(list);list.setText(prefs.getString("categories","مالی\nکاری\nمارکتینگ\nخرید\nشخصی"));list.setMinLines(6);list.setTextColor(ink);list.setBackground(bg(fieldColor(),12));list.setPadding(dp(12),dp(12),dp(12),dp(12));panel.addView(list);
  new AlertDialog.Builder(this).setTitle("تنظیمات زمان‌یار").setView(panel).setPositiveButton("ذخیره دسته‌بندی‌ها",(d,w)->{LinkedHashSet<String> names=new LinkedHashSet<>();for(String name:list.getText().toString().split("\n")){name=name.trim();if(!name.isEmpty())names.add(name);}prefs.edit().putString("categories",android.text.TextUtils.join("\n",names)).apply();show(tab);}).setNegativeButton("بستن",null).show();
 }
 void applyTheme(){surface=dark?0xff101319:0xfff6f5fc;ink=dark?0xfff1efff:0xff21253e;muted=dark?0xffa5a8b8:0xff73788c;purple=dark?0xff895cff:0xff5c47c2;getWindow().setStatusBarColor(surface);getWindow().setNavigationBarColor(surface);getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);}
 void timePicker(EditText field){Calendar now=Calendar.getInstance();int h=now.get(Calendar.HOUR_OF_DAY),m=now.get(Calendar.MINUTE);String current=field.getText().toString();if(validTime(current)){String[] t=current.split(":");h=Integer.parseInt(t[0]);m=Integer.parseInt(t[1]);}new TimePickerDialog(this,(v,hour,minute)->field.setText(String.format(Locale.US,"%02d:%02d",hour,minute)),h,m,true).show();}
 void checklistDialog(ContentValues e){String[] lines=val(e,"checklist").split("\n");ArrayList<String> items=new ArrayList<>();ArrayList<Boolean> checks=new ArrayList<>();for(String line:lines){if(line.trim().isEmpty())continue;boolean done=line.startsWith("[x] ");checks.add(done);items.add(line.replaceFirst("^\\[(?:x| )\\] ",""));}if(items.isEmpty()){Toast.makeText(this,"زیر‌وظیفه‌ای ثبت نشده است",Toast.LENGTH_SHORT).show();return;}boolean[] selected=new boolean[items.size()];for(int i=0;i<selected.length;i++)selected[i]=checks.get(i);new AlertDialog.Builder(this).setTitle("چک‌لیست: "+val(e,"title")).setMultiChoiceItems(items.toArray(new String[0]),selected,(d,which,on)->selected[which]=on).setPositiveButton("ذخیره",(d,w)->{StringBuilder b=new StringBuilder();for(int i=0;i<items.size();i++){if(i>0)b.append("\n");b.append(selected[i]?"[x] ": "[ ] ").append(items.get(i));}e.put("checklist",b.toString());db.save(e,e.getAsLong("id"));show(tab);}).setNegativeButton("انصراف",null).show();}

 String[] tabs={"داشبورد","تقویم","جلسه","وظیفه","رویداد","گزارش"};
 String search=""; int weekOffset=0,monthOffset=0; boolean monthMode=true; static final int BACKUP_CREATE=501,BACKUP_OPEN=502;
 int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(purple);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);prefs=getSharedPreferences("zamanyar_ui",MODE_PRIVATE);dark=prefs.getBoolean("dark",true);applyTheme();db=new Db(this);initFonts();if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},12);show("داشبورد");}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(fa(s));t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);t.setTextDirection(View.TEXT_DIRECTION_RTL);t.setPadding(dp(4),dp(6),dp(4),dp(6));t.setTypeface(bold?iranBold:iranRegular);return t;}
 LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 void show(String t){
  tab=t;base=column();base.setBackgroundColor(surface);setContentView(base);
  LinearLayout head=column();head.setPadding(dp(20),dp(12),dp(20),dp(14));head.setBackgroundColor(surface);
  LinearLayout headline=new LinearLayout(this);headline.setGravity(Gravity.CENTER_VERTICAL);headline.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  TextView title=text("زمان‌یار",28,ink,true);headline.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  TextView gear=text("⚙",25,purple,true);gear.setGravity(Gravity.CENTER);headline.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(48)));gear.setOnClickListener(v->settings());
  head.addView(headline);head.addView(text("برنامه‌ریزی دقیق، ذهن آسوده",13,muted,false));base.addView(head);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);body=column();body.setPadding(dp(16),dp(5),dp(16),dp(20));scroll.addView(body);base.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(t.equals("داشبورد"))dashboard();else if(t.equals("تقویم")){if(monthMode)monthPage();else calendarPage();}else if(t.equals("گزارش"))reportPage();else listing(t);
  nav=new LinearLayout(this);nav.setElevation(dp(9));nav.setPadding(dp(5),dp(5),dp(5),dp(7));nav.setBackgroundColor(panel());nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  String[] names={"داشبورد","تقویم","افزودن","وظیفه","جلسه","گزارش"};String[] icons={"⌂","▦","+","☑","▣","▥"};
  for(int i=0;i<names.length;i++){final String s=names[i];LinearLayout cell=column();cell.setGravity(Gravity.CENTER);TextView icon=text(icons[i],i==2?32:23,i==2?Color.WHITE:s.equals(t)?purple:muted,true);icon.setGravity(Gravity.CENTER);cell.addView(icon);if(i!=2){TextView label=text(s,10,s.equals(t)?purple:muted,s.equals(t));label.setGravity(Gravity.CENTER);cell.addView(label);}else{cell.setBackground(bg(purple,22));}LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(59),i==2?1.15f:1);cp.setMargins(dp(2),0,dp(2),0);nav.addView(cell,cp);cell.setOnClickListener(v->{if(s.equals("افزودن"))chooseType();else show(s);});}
  base.addView(nav);
 }
 void card(LinearLayout parent,View content){
  LinearLayout box=column();box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackground(bg(panel(),16));box.setElevation(dp(2));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);parent.addView(box,p);box.addView(content);
 }
 void dashboard(){
  ArrayList<ContentValues> all=db.all("همه");int meetings=0,tasks=0,events=0,done=0,overdue=0;
  String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
  for(ContentValues e:all){String k=val(e,"kind");if("جلسه".equals(k))meetings++;if("وظیفه".equals(k)){tasks++;if("انجام‌شده".equals(val(e,"status")))done++;else if(!val(e,"date").isEmpty()&&val(e,"date").compareTo(today)<0)overdue++;}if("رویداد".equals(k))events++;}
  LinearLayout calendar=column();calendar.setPadding(dp(12),dp(10),dp(12),dp(10));calendar.setBackground(bg(panel(),18));
  LinearLayout calTop=new LinearLayout(this);calTop.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);calTop.setGravity(Gravity.CENTER_VERTICAL);
  TextView date=text(Jalali.display(today),17,ink,true);calTop.addView(date,new LinearLayout.LayoutParams(0,-2,1));TextView calButton=text("تقویم  ◀",13,purple,true);calTop.addView(calButton);calButton.setOnClickListener(v->show("تقویم"));calendar.addView(calTop);
  LinearLayout modes=new LinearLayout(this);modes.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);for(String m:new String[]{"امروز","هفته","ماه"}){TextView chip=text(m,13,m.equals("امروز")?Color.WHITE:muted,true);chip.setGravity(Gravity.CENTER);chip.setBackground(bg(m.equals("امروز")?purple:fieldColor(),12));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(38),1);cp.setMargins(dp(3),dp(6),dp(3),dp(6));modes.addView(chip,cp);chip.setOnClickListener(v->{if(m.equals("امروز"))show("داشبورد");else{monthMode=m.equals("ماه");show("تقویم");}});}calendar.addView(modes);
  LinearLayout week=new LinearLayout(this);week.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);String[] dayNames={"ش","ی","د","س","چ","پ","ج"};Calendar now=Calendar.getInstance();int weekday=now.get(Calendar.DAY_OF_WEEK);int idx=weekday%7;for(int i=0;i<7;i++){int shift=i-idx;Calendar c=(Calendar)now.clone();c.add(Calendar.DAY_OF_YEAR,shift);String iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());String jd=Jalali.display(iso);String[] parts=jd.split("/");LinearLayout day=column();day.setGravity(Gravity.CENTER);day.setBackground(bg(shift==0?purple:fieldColor(),12));day.addView(centerText(dayNames[i],11,shift==0?Color.WHITE:muted,false));day.addView(centerText(parts.length>2?parts[2]:"",15,shift==0?Color.WHITE:ink,true));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(2),0,dp(2),0);week.addView(day,lp);day.setOnClickListener(v->dayDetails(iso));}calendar.addView(week);body.addView(calendar);space(12);
  LinearLayout stats=new LinearLayout(this);stats.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);metric(stats,"جلسات",meetings,"▣",0xff9566ff);metric(stats,"وظایف",tasks,"☑",0xff33d69b);metric(stats,"رویداد",events,"◉",0xffff994f);metric(stats,"معوق",overdue,"!",0xffff627e);body.addView(stats);space(10);
  sectionHeader("برنامه‌های امروز","مشاهده همه",()->show("تقویم"));LinearLayout agenda=column();agenda.setPadding(dp(9),dp(8),dp(9),dp(8));agenda.setBackground(bg(panel(),18));int todayCount=0;for(ContentValues e:all)if(Recurrence.occurs(e,today)){agendaRow(agenda,e);todayCount++;}if(todayCount==0)agenda.addView(text("امروز برنامه‌ای ثبت نشده است.",14,muted,false));body.addView(agenda);space(14);
  sectionHeader("وظایف امروز ("+done+"/"+tasks+")","+ افزودن وظیفه",()->edit("وظیفه",null));LinearLayout taskList=column();taskList.setPadding(dp(10),dp(8),dp(10),dp(8));taskList.setBackground(bg(panel(),18));int taskShown=0;for(ContentValues e:all)if("وظیفه".equals(val(e,"kind"))&&(Recurrence.occurs(e,today)||val(e,"date").isEmpty())){taskRow(taskList,e);taskShown++;}if(taskShown==0)taskList.addView(text("وظیفه‌ای برای امروز ثبت نشده است.",14,muted,false));body.addView(taskList);space(14);
  sectionHeader("دسته‌بندی‌ها","مدیریت دسته‌ها",()->settings());LinearLayout cats=new LinearLayout(this);cats.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);cats.setGravity(Gravity.CENTER_VERTICAL);String[] names=categories();int[] colors={0xff31d69a,0xff55a4ff,0xffff9a42,0xffa16bff,0xffff657e};for(int i=0;i<Math.min(names.length,4);i++){final String name=names[i];TextView chip=centerText(name,12,ink,true);chip.setBackground(bg(panel(),13));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(49),1);cp.setMargins(dp(3),0,dp(3),0);cats.addView(chip,cp);chip.setOnClickListener(v->show("وظیفه"));}body.addView(cats);space(12);
  Button backup=button("پشتیبان‌گیری و بازیابی");body.addView(backup);backup.setOnClickListener(v->backupMenu());
 }
 TextView centerText(String s,int size,int color,boolean bold){TextView t=text(s,size,color,bold);t.setGravity(Gravity.CENTER);return t;}
 void space(int height){Space sp=new Space(this);body.addView(sp,new LinearLayout.LayoutParams(1,dp(height)));}
 void sectionHeader(String name,String action,Runnable run){LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);line.setGravity(Gravity.CENTER_VERTICAL);TextView heading=text(name,18,ink,true);line.addView(heading,new LinearLayout.LayoutParams(0,dp(52),1));TextView link=text(action,12,purple,true);line.addView(link);link.setOnClickListener(v->run.run());body.addView(line);}
 void agendaRow(LinearLayout parent,ContentValues e){LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row.setGravity(Gravity.CENTER_VERTICAL);TextView marker=centerText("▌",23,"وظیفه".equals(val(e,"kind"))?0xff55a4ff:purple,true);row.addView(marker,new LinearLayout.LayoutParams(dp(26),dp(60)));LinearLayout details=column();details.addView(text(val(e,"title"),15,ink,true));details.addView(text(val(e,"category")+"  •  "+val(e,"kind"),12,muted,false));row.addView(details,new LinearLayout.LayoutParams(0,-2,1));row.addView(text(val(e,"time"),13,purple,true));parent.addView(row);row.setOnClickListener(v->detail(e));}
 void taskRow(LinearLayout parent,ContentValues e){LinearLayout line=new LinearLayout(this);line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);line.setGravity(Gravity.CENTER_VERTICAL);CheckBox cb=new CheckBox(this);styleInput(cb);boolean checked="انجام‌شده".equals(val(e,"status"));cb.setChecked(checked);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(purple));line.addView(cb);TextView name=text(val(e,"title"),14,checked?muted:ink,false);if(checked)name.setPaintFlags(name.getPaintFlags()|android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);line.addView(name,new LinearLayout.LayoutParams(0,dp(55),1));TextView tag=text(val(e,"category"),11,purple,true);line.addView(tag);parent.addView(line);cb.setOnCheckedChangeListener((v,on)->{e.put("status",on?"انجام‌شده":"انجام‌نشده");db.save(e,e.getAsLong("id"));show(tab);});name.setOnClickListener(v->detail(e));}
 void metric(LinearLayout parent,String label,int count,String icon,int color){LinearLayout box=column();box.setGravity(Gravity.CENTER);box.setPadding(dp(3),dp(8),dp(3),dp(8));box.setBackground(bg(panel(),17));TextView symbol=centerText(icon,21,color,true);box.addView(symbol);TextView number=centerText(String.valueOf(count),25,ink,true);box.addView(number);box.addView(centerText(label,11,muted,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(115),1);lp.setMargins(dp(2),0,dp(2),0);parent.addView(box,lp);}
 void chooseType(){new AlertDialog.Builder(this).setTitle("نوع برنامه").setItems(new String[]{"جلسه","وظیفه","رویداد"},(d,w)->edit(new String[]{"جلسه","وظیفه","رویداد"}[w],null)).show();}
 void listing(String kind){
  Button add=button("+ افزودن "+kind);body.addView(add);add.setOnClickListener(v->edit(kind,null));
  ArrayList<ContentValues> all=db.all(kind);
  searchBox();
  if(all.isEmpty())card(body,text("موردی در این بخش ثبت نشده است.",15,muted,false));
  for(ContentValues e:all)if(matches(e))entryCard(e);
 }
 Button button(String label){Button b=new Button(this);styleInput(b);b.setText(fa(label));b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(bg(purple,12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.bottomMargin=dp(12);b.setLayoutParams(p);return b;}
 void entryCard(ContentValues e){
  LinearLayout item=column();
  String title=e.getAsString("title"),kind=e.getAsString("kind");
  item.addView(text(("جلسه".equals(kind)?"▣  ":"وظیفه".equals(kind)?"☑  ":"◉  ")+title,17,ink,true));
  String date=e.getAsString("date"),time=e.getAsString("time"),status=e.getAsString("status");
  item.addView(text(Jalali.display(date)+(time==null||time.isEmpty()?"":"  |  "+time)+(status==null||status.isEmpty()?"":"  |  "+status),13,muted,false));
  if("وظیفه".equals(kind)){
   LinearLayout controls=new LinearLayout(this);controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
   CheckBox done=new CheckBox(this);styleInput(done);done.setText("انجام شد");done.setTextColor(ink);done.setChecked("انجام‌شده".equals(status));controls.addView(done);
   done.setOnCheckedChangeListener((v,checked)->{e.put("status",checked?"انجام‌شده":"انجام‌نشده");db.save(e,e.getAsLong("id"));show(tab);});
   if(!val(e,"checklist").isEmpty()){Button checklist=button("☑ چک‌لیست");controls.addView(checklist,new LinearLayout.LayoutParams(0,dp(48),1));checklist.setOnClickListener(v->checklistDialog(e));}
   item.addView(controls);
  }
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
  addTime(fields,inputs,"time","ساعت شروع",old);
  if(!kind.equals("وظیفه"))addTime(fields,inputs,"endtime","ساعت پایان",old);
  addField(fields,inputs,"notes","توضیحات",old,true);
  if(!kind.equals("وظیفه"))addField(fields,inputs,"location","مکان / لینک جلسه",old,false);
  addField(fields,inputs,"people",kind.equals("جلسه")?"شرکت‌کنندگان":kind.equals("وظیفه")?"مسئول":"افراد مرتبط",old,false);
  addChoice(fields,inputs,"category","دسته‌بندی",categories(),old);
  if(kind.equals("وظیفه")){addChoice(fields,inputs,"priority","اولویت",new String[]{"کم","متوسط","زیاد","فوری"},old);addChoice(fields,inputs,"status","وضعیت",new String[]{"انجام‌نشده","درحال انجام","انجام‌شده","متوقف"},old);addField(fields,inputs,"checklist","زیر‌وظایف (هر خط یک مورد؛ پس از ذخیره قابل تیک‌زدن)",old,true);}
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
  parent.addView(text(label,14,ink,true));EditText e=new EditText(this);styleInput(e);e.setText(val(old,key));e.setTextSize(15);e.setSingleLine(!multiline);e.setTextDirection(View.TEXT_DIRECTION_RTL);e.setGravity(Gravity.RIGHT|Gravity.TOP);e.setBackground(bg(fieldColor(),10));e.setTextColor(ink);e.setHintTextColor(muted);e.setPadding(dp(12),dp(12),dp(12),dp(12));if(multiline)e.setMinLines(3);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);parent.addView(e,p);inputs.put(key,e);
 }
 void addTime(LinearLayout parent,LinkedHashMap<String,EditText> inputs,String key,String label,ContentValues old){addField(parent,inputs,key,label+" (برای انتخاب لمس کنید)",old,false);EditText e=inputs.get(key);e.setFocusable(false);e.setOnClickListener(v->timePicker(e));}
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
 void searchBox(){EditText field=new EditText(this);styleInput(field);field.setSingleLine(true);field.setHint("جست‌وجوی عنوان، توضیح، مسئول یا پروژه");field.setText(search);field.setTextDirection(View.TEXT_DIRECTION_RTL);field.setPadding(dp(12),dp(10),dp(12),dp(10));field.setBackground(bg(panel(),12));field.setTextColor(ink);body.addView(field);field.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();}public void afterTextChanged(android.text.Editable x){}});
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
   int day=i-offset+1;TextView cell=text(day>0&&day<=count?""+day:"",15,ink,false);cell.setGravity(Gravity.CENTER);cell.setBackground(bg(panel(),9));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(54),1);cp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,cp);
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
