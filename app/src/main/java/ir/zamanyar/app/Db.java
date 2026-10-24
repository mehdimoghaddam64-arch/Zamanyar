package ir.zamanyar.app;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class Db extends SQLiteOpenHelper {
 public Db(Context c){super(c,"zamanyar.db",null,2);}
 public void onCreate(SQLiteDatabase d){
  d.execSQL("CREATE TABLE entries(id INTEGER PRIMARY KEY AUTOINCREMENT,kind TEXT NOT NULL,title TEXT NOT NULL,notes TEXT,date TEXT,time TEXT,endtime TEXT,location TEXT,people TEXT,priority TEXT,status TEXT,category TEXT,repeat_rule TEXT,checklist TEXT,decisions TEXT,reminder_minutes TEXT)");
 }
 public void onUpgrade(SQLiteDatabase d,int old,int now){if(old<2)d.execSQL("ALTER TABLE entries ADD COLUMN reminder_minutes TEXT");}
 public long save(ContentValues v,long id){
  SQLiteDatabase d=getWritableDatabase();
  if(id<0)return d.insertOrThrow("entries",null,v);
  d.update("entries",v,"id=?",new String[]{""+id});return id;
 }
 public void delete(long id){getWritableDatabase().delete("entries","id=?",new String[]{""+id});}
 public ArrayList<ContentValues> all(String kind){
  ArrayList<ContentValues> a=new ArrayList<>();
  String sel=kind.equals("همه")?null:"kind=?";
  String[] args=sel==null?null:new String[]{kind};
  try(Cursor c=getReadableDatabase().query("entries",null,sel,args,null,null,"date ASC,time ASC,id DESC")){
   while(c.moveToNext()){
    ContentValues v=new ContentValues();
    for(int i=0;i<c.getColumnCount();i++){
     String key=c.getColumnName(i);
     if(key.equals("id"))v.put(key,c.getLong(i));else v.put(key,c.getString(i));
    }a.add(v);
   }
  }return a;
 }
}
