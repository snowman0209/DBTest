package jp.codeforfun.dbtest;

public class Note {
    protected int id;
    protected String note;
    protected  String day;
    protected String time;

    public Note(int id, String note, String day, String time){
        this.id = id;
        this.note = note;
        this.day = day;
        this.time = time;
    }

    public String getNote(){
        return note;
    }

    public String getDay(){
        return day;
    }

    public String getTime(){
        return time;
    }


    public int getId(){
        return id;
    }

}
