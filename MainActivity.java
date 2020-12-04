package jp.codeforfun.dbtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


        static final String TAG = "SQLiteTest1";
        static final int MENUITEM_ID_DELETE = 1;
        ListView itemListView;
        EditText noteEditText;
        ImageButton saveButton;
        TextView tvDay;
        ImageView ivWeather;
        static DBAdapter dbAdapter;
        static NoteListAdapter listAdapter;
        String day;
        String time;
        static String icon;
        static List<Note> noteList = new ArrayList<Note>();
        Activity MainActivity;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            findViews();


            CalendarView calendarView = findViewById(R.id.upper_half);
            calendarView.setDate(System.currentTimeMillis());
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
                @Override
                public void onSelectedDayChange(CalendarView view ,int year, int month, int dayOfMonth){
                    Log.d("you",year+"年"+(month+1)+"月"+dayOfMonth+"日");
                    day = year+""+(month+1)+""+dayOfMonth;
                    tvDay = findViewById(R.id.tv_day);

                    tvDay.setText((month+1)+"月"+dayOfMonth+"日");
                    loadNote();
                }
            });



            setListeners();

            dbAdapter = new DBAdapter(this);
            listAdapter = new NoteListAdapter();
            itemListView.setAdapter(listAdapter);

        }

        protected void findViews(){
            itemListView = (ListView)findViewById(R.id.toDo);
            noteEditText = (EditText)findViewById(R.id.task);
            saveButton = (ImageButton)findViewById(R.id.bt_new);
        }

        protected void loadNote(){
            noteList.clear();

            dbAdapter.open();

            Cursor c = dbAdapter.getAllNotes();

            startManagingCursor(c);

            if(c.moveToFirst()){
                do {
                    Note note = new Note(
                            c.getInt(c.getColumnIndex(DBAdapter.COL_ID)),
                            c.getString(c.getColumnIndex(DBAdapter.COL_NOTE)),
                            c.getString(c.getColumnIndex(DBAdapter.DAY)),
                            c.getString(c.getColumnIndex(DBAdapter.TIME))

                    );
                    if (day.equals(c.getString(c.getColumnIndex(DBAdapter.DAY)))){
                    noteList.add(note);
                    }
                } while(c.moveToNext());
            }

            stopManagingCursor(c);
            dbAdapter.close();

            listAdapter.notifyDataSetChanged();
        }


        protected void saveItem(){

            Spinner sp_hours = (Spinner) findViewById(R.id.sp_hours);
            Spinner sp_minutes = (Spinner) findViewById(R.id.sp_minutes);
            String hours = sp_hours.getSelectedItem().toString();
            String minutes = sp_minutes.getSelectedItem().toString();
            time = hours+":"+minutes;
            Log.d("Time",time);

            dbAdapter.open();
            dbAdapter.saveNote(noteEditText.getText().toString(),day,time);
            dbAdapter.close();
            noteEditText.setText("");
            sp_hours.setSelection(0);
            sp_minutes.setSelection(0);
            loadNote();
        }


        protected void setListeners(){
            saveButton.setOnClickListener(this);

            itemListView.setOnCreateContextMenuListener(
                    new View.OnCreateContextMenuListener(){
                        @Override
                        public void onCreateContextMenu(
                                ContextMenu menu,
                                View v,
                                ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, MENUITEM_ID_DELETE, 0, "Delete");
                        }
                    });
        }


        @Override
        public boolean onContextItemSelected(MenuItem item) {

            switch(item.getItemId()){
                case MENUITEM_ID_DELETE:
                    AdapterView.AdapterContextMenuInfo menuInfo
                            = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

                    Note note = noteList.get(menuInfo.position);
                    final int noteId = note.getId();

                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.ic_launcher_foreground)
                            .setTitle("Are you sure you want to delete this note?")
                            .setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dbAdapter.open();
                                            if(dbAdapter.deleteNote(noteId)){
                                                Toast.makeText(
                                                        getBaseContext(),
                                                        "The note was successfully deleted.",
                                                        Toast.LENGTH_SHORT).show();
                                                loadNote();
                                            }
                                            dbAdapter.close();
                                        }
                                    })
                            .setNegativeButton(
                                    "Cancel",
                                    null)
                            .show();

                    return true;
            }
            return super.onContextItemSelected(item);
        }


        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.bt_new:
                    saveItem();
                    break;
            }
        }


        private class NoteListAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return noteList.size();
            }

            @Override
            public Object getItem(int position) {
                return noteList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView noteTextView;
                TextView TimeTextView;
                View v = convertView;
                if(v==null){
                    LayoutInflater inflater =
                            (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.row, null);
                }
                Note note = (Note)getItem(position);
                if(note != null){
                    noteTextView = (TextView)v.findViewById(R.id.noteTextView);
                    TimeTextView = (TextView)v.findViewById(R.id.TimeTextView);
                    noteTextView.setText(note.getNote());
                    TimeTextView.setText(note.getTime());
                    v.setTag(R.id.noteTextView, note);
                }
                return v;
            }
        }


}

