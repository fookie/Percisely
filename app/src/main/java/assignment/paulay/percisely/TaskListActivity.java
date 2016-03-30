package assignment.paulay.percisely;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskListActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Cursor cursor;
    private SerializedMap serializedMap = new SerializedMap();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //if there's an unfinished progressing work, jump to it
        SharedPreferences progressingWork = getSharedPreferences("progress", MODE_PRIVATE);
        if (!progressingWork.getAll().isEmpty()) {
            startActivity(new Intent(TaskListActivity.this, WorkInProgress.class));
            finish();
        }

        SharedPreferences profile = getSharedPreferences("statistics", MODE_PRIVATE);
        TextView bonusindicator = (TextView) findViewById(R.id.task_list_menu_bonus);
        bonusindicator.setText(String.valueOf(profile.getInt("bonus", 0)));

        //prepare data
        final List<Map<String, Object>> tasks = new ArrayList<>();
        db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS achievements(title varchar(127) primary key, description text, bonus smallint not null, icon varchar(127), achieved tinyint)");//initialize achievements
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks(name varchar(128) primary key, estimatedtime smallint not null,notification Datetime not null, deadline Datetime not null, bonus int not null, photopath text, recordpath text, description text)");
        cursor = db.query("tasks", new String[]{"name", "estimatedtime", "notification", "deadline", "bonus", "photopath", "recordpath", "description"}, null, null, null, null, "deadline");
        while (cursor.moveToNext()) {
            Map<String, Object> task = new HashMap<>();
            task.put("name", cursor.getString(cursor.getColumnIndex("name")));
            task.put("estimatedtime", cursor.getInt(cursor.getColumnIndex("estimatedtime")));
            task.put("notification", cursor.getString(cursor.getColumnIndex("notification")));
            task.put("deadline", cursor.getString(cursor.getColumnIndex("deadline")).substring(0, 16));
            task.put("bonus", cursor.getInt(cursor.getColumnIndex("bonus")) + " pts");
            task.put("photopath", cursor.getString(cursor.getColumnIndex("photopath")));
            task.put("recordpath", cursor.getString(cursor.getColumnIndex("recordpath")));
            task.put("description", cursor.getString(cursor.getColumnIndex("description")));
            Timestamp dlTimestamp = Timestamp.valueOf(task.get("deadline") + ":00");
            task.put("remain", ((dlTimestamp.getTime() - Calendar.getInstance().getTimeInMillis()) / 86400000) + " days left");
            tasks.add(task);
        }
        cursor.close();
        //initialize list
        final ListView tasklist = (ListView) findViewById(R.id.task_list);
        String[] from = {"name", "deadline", "description", "remain", "estimatedtime", "bonus"};
        int[] to = {R.id.listcontentTaskTitle, R.id.listcontentDeadline, R.id.listcontentTaskDescription, R.id.listcontentTaskDayRemaining, R.id.listcontentTaskEstimateTime, R.id.listcontentTaskBonus};
        final SimpleAdapter tasklistAdapter = new SimpleAdapter(this, tasks, R.layout.task_list_content, from, to);
        tasklist.setAdapter(tasklistAdapter);
        //list listener

        tasklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle pack = new Bundle();
                serializedMap.setMap(tasks.get(position));
                pack.putSerializable("elements", serializedMap);//transfer a serialized map
                Intent jumpToDetails = new Intent(TaskListActivity.this, TaskDetail.class);
                jumpToDetails.putExtras(pack);
                startActivity(jumpToDetails);
                finish();
            }
        });
        tasklist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder delete = new AlertDialog.Builder(TaskListActivity.this);
                delete.setMessage(R.string.task_list_delete_prompt);
                delete.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (db.delete("tasks", "name = '" + tasks.get(position).get("name") + "'", null) == 1) {
                            tasks.remove(position);//refresh listview
                            tasklistAdapter.notifyDataSetChanged();
                            Toast.makeText(TaskListActivity.this, R.string.task_list_deletion_successful, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskListActivity.this, R.string.task_list_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                delete.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                delete.show();
                return true;
            }
        });

        //Assign all widgets
        Button drawer_addtask = (Button) findViewById(R.id.task_list_menu_newtask);
        Button drawer_awardlist = (Button) findViewById(R.id.task_list_menu_awardlist);
        Button drawer_achievements = (Button) findViewById(R.id.task_list_menu_achievements);
        Button drawer_settings = (Button) findViewById(R.id.task_list_menu_settings);
        Button drawer_exit = (Button) findViewById(R.id.task_list_menu_exit);

        drawer_addtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addtask = new Intent(TaskListActivity.this, TaskAdd.class);
                startActivity(addtask);
                finish();
            }
        });
        drawer_awardlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent al = new Intent(TaskListActivity.this, AwardListActivity.class);
                startActivity(al);
                finish();
            }
        });
        drawer_achievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ac = new Intent(TaskListActivity.this, AchievementListActivity.class);
                startActivity(ac);
                finish();
            }
        });
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(TaskListActivity.this, Settings.class);
                startActivity(settings);
            }
        });
        drawer_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);//kill the app
            }
        });


    }

    @Override
    protected void onDestroy() {
        db.close();
        cursor.close();
        super.onDestroy();
    }
}
