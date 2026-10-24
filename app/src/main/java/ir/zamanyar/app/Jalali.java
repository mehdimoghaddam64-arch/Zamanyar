package ir.zamanyar.app;
public class Jalali {
 public static int[] fromGregorian(int gy,int gm,int gd){
  int[] gdm={0,31,59,90,120,151,181,212,243,273,304,334};
  int gy2=gm>2?gy+1:gy;
  int days=355666+365*gy+(gy2+3)/4-(gy2+99)/100+(gy2+399)/400+gd+gdm[gm-1];
  int jy=-1595+33*(days/12053);days%=12053;
  jy+=4*(days/1461);days%=1461;
  if(days>365){jy+=(days-1)/365;days=(days-1)%365;}
  int jm=days<186?1+days/31:7+(days-186)/30;
  int jd=1+(days<186?days%31:(days-186)%30);
  return new int[]{jy,jm,jd};
 }
 public static String display(String iso){
  if(iso==null||iso.length()<10)return "بدون تاریخ";
  try{String[] p=iso.split("-");int[] j=fromGregorian(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2]));
   return String.format(java.util.Locale.US,"%04d/%02d/%02d",j[0],j[1],j[2]);
  }catch(Exception e){return iso;}
 }
 public static int[] toGregorian(int jy,int jm,int jd){
  jy+=1595;long days=-355668L+365L*jy+(jy/33)*8+((jy%33+3)/4)+jd+(jm<7?(jm-1)*31:(jm-7)*30+186);
  int gy=400*(int)(days/146097);days%=146097;
  if(days>36524){gy+=100*(int)(--days/36524);days%=36524;if(days>=365)days++;}
  gy+=4*(int)(days/1461);days%=1461;
  if(days>365){gy+=(int)((days-1)/365);days=(days-1)%365;}
  int gd=(int)days+1;int[] months={31,((gy%4==0&&gy%100!=0)||gy%400==0)?29:28,31,30,31,30,31,31,30,31,30,31};
  int gm=1;for(int n:months){if(gd<=n)break;gd-=n;gm++;}return new int[]{gy,gm,gd};
 }
 public static int monthLength(int jy,int jm){
  if(jm<1||jm>12)throw new IllegalArgumentException("month");
  if(jm<=6)return 31;if(jm<=11)return 30;
  int[] start=toGregorian(jy,1,1),end=toGregorian(jy+1,1,1);
  java.util.Calendar a=java.util.Calendar.getInstance();a.clear();a.set(start[0],start[1]-1,start[2]);
  java.util.Calendar b=java.util.Calendar.getInstance();b.clear();b.set(end[0],end[1]-1,end[2]);
  return (b.getTimeInMillis()-a.getTimeInMillis())/86400000L==366?30:29;
 }
 public static String iso(int jy,int jm,int jd){int[] g=toGregorian(jy,jm,jd);return String.format(java.util.Locale.US,"%04d-%02d-%02d",g[0],g[1],g[2]);}
}
