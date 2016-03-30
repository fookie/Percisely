package assignment.paulay.percisely;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementListActivity extends Activity {
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_list);


        db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS achievements(title varchar(127) primary key, description text, bonus smallint not null, icon varchar(127), achieved tinyint)");// achieved is the mark of completion
        addAchievements();//initialize list
        //List
        List<Map<String, Object>> achievements = new ArrayList<>();
        Cursor cursor = db.query("achievements", new String[]{"title", "description", "bonus", "icon", "achieved"}, null, null, null, null, "bonus");
        while (cursor.moveToNext()) {
            Map<String, Object> achievement = new HashMap<>();
            achievement.put("title", cursor.getString(cursor.getColumnIndex("title")));
            achievement.put("description", cursor.getString(cursor.getColumnIndex("description")));
            achievement.put("bonus", cursor.getInt(cursor.getColumnIndex("bonus")));
            achievement.put("icon", cursor.getString(cursor.getColumnIndex("bonus")));
            achievements.add(achievement);
        }
        ListView achievementlist = (ListView) findViewById(R.id.achievement_list);
        String[] from = {"title", "description", "bonus"};
        int[] to = {R.id.achievement_list_content_title, R.id.achievement_list_content_description, R.id.achievement_list_content_bonus};
        SimpleAdapter adapter = new SimpleAdapter(this, achievements, R.layout.achievement_list_content, from, to);
        achievementlist.setAdapter(adapter);
        //completion
//        for (int i = 0; i < achievementlist.getCount(); i++) {
//            cursor.moveToPosition(i);
//            if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1) {
//
//                ((RelativeLayout) achievementlist.getItemAtPosition(i)).setBackgroundColor(Color.YELLOW);
//            }
//        }
        cursor.close();
        //menu
        Button drawer_tasklist = (Button) findViewById(R.id.achievement_list_menu_tasklist);
        Button drawer_awardlist = (Button) findViewById(R.id.achievement_list_menu_awardlist);
        Button drawer_settings = (Button) findViewById(R.id.achievement_list_menu_settings);
        Button drawer_exit = (Button) findViewById(R.id.achievement_list_menu_exit);

        drawer_tasklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ta = new Intent(AchievementListActivity.this, TaskListActivity.class);
                startActivity(ta);
                finish();
            }
        });
        drawer_awardlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aw = new Intent(AchievementListActivity.this, AwardListActivity.class);
                startActivity(aw);
                finish();
            }
        });
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent set = new Intent(AchievementListActivity.this, Settings.class);
                startActivity(set);
            }
        });
        drawer_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    private void addAnAchievement(String title, String description, int bonus) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("bonus", bonus);
        cv.put("achieved", 0);
        db.insertWithOnConflict("achievements", null, cv, SQLiteDatabase.CONFLICT_IGNORE);

    }

    private void addAchievements() {
        addAnAchievement(getString(R.string.achievements_takingoff), getString(R.string.achievements_takingoff_des), 1);
        addAnAchievement(getString(R.string.achievement_dreamcometrue), getString(R.string.achievement_dreamcometrue_des), 1);
        addAnAchievement(getString(R.string.achievement_questadventurer), getString(R.string.achievement_questadventurer_des), 500);
    }

    public static void Achieved(SQLiteDatabase db, String title) {//module malfunction
        ContentValues cv = new ContentValues();
        cv.put("achieved", 1);
        db.update("achievements", cv, "title =", new String[]{title});
    }
}
