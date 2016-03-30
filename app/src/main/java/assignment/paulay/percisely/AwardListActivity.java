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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwardListActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award_list);

        final SharedPreferences profile = getSharedPreferences("statistics", MODE_PRIVATE);
        final TextView bonusindicator = (TextView) findViewById(R.id.award_list_menu_bonus);
        bonusindicator.setText(String.valueOf(profile.getInt("bonus", 0)));

        db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS awards(name varchar(128) primary key, timecost smallint not null, moneycost int not null, description text, bonus int not null)");
        //prepare data
        final List<Map<String, Object>> awards = new ArrayList<>();
        Cursor cursor = db.query("awards", new String[]{"name", "description", "bonus"}, null, null, null, null, "bonus");
        while (cursor.moveToNext()) {
            Map<String, Object> award = new HashMap<>();
            award.put("name", cursor.getString(cursor.getColumnIndex("name")));
            award.put("bonus", cursor.getInt(cursor.getColumnIndex("bonus")));
            award.put("description", cursor.getString(cursor.getColumnIndex("description")));
            awards.add(award);
        }
        cursor.close();
        ListView awardlist = (ListView) findViewById(R.id.award_list);
        String[] from = {"name", "description", "bonus"};
        int[] to = {R.id.listcontentAwardTitle, R.id.listcontentAwardDescription, R.id.listcontentAwardNeededBonus};
        final SimpleAdapter adapter = new SimpleAdapter(this, awards, R.layout.award_list_content, from, to);
        awardlist.setAdapter(adapter);


        awardlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder buy = new AlertDialog.Builder(AwardListActivity.this);
                final int price = (int) awards.get(position).get("bonus");

                buy.setMessage(R.string.award_list_buy_prompt);
                buy.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (price > Integer.parseInt(bonusindicator.getText().toString())) {
                            Toast.makeText(AwardListActivity.this, R.string.award_list_buy_insufficient, Toast.LENGTH_SHORT).show();
                        } else {
                            if (db.delete("awards", "name = '" + awards.get(position).get("name") + "'", null) == 1) {
                                awards.remove(position);//refresh listview
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(AwardListActivity.this, R.string.award_list_buy_error, Toast.LENGTH_SHORT).show();
                            }
                            editor = profile.edit();
                            editor.putInt("bonus", Integer.parseInt(bonusindicator.getText().toString()) - price);
                            editor.putInt("awardCount", profile.getInt("awardCount", 0) + 1);//award count +1
                            editor.apply();
                            Toast.makeText(AwardListActivity.this, R.string.award_list_successful, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                buy.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                buy.show();

                return true;
            }
        });

        Button drawer_addaward = (Button) findViewById(R.id.award_list_menu_newaward);
        Button drawer_tasklist = (Button) findViewById(R.id.award_list_menu_tasklist);
        Button drawer_achievements = (Button) findViewById(R.id.award_list_menu_achievements);
        Button drawer_settings = (Button) findViewById(R.id.award_list_menu_settings);
        Button drawer_exit = (Button) findViewById(R.id.award_list_menu_exit);


        drawer_addaward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addaward = new Intent(AwardListActivity.this, AwardAdd.class);
                startActivity(addaward);
                finish();
            }
        });
        drawer_tasklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent al = new Intent(AwardListActivity.this, TaskListActivity.class);
                startActivity(al);
                finish();
            }
        });
        drawer_achievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ac = new Intent(AwardListActivity.this, AchievementListActivity.class);
                startActivity(ac);
                finish();
            }
        });
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(AwardListActivity.this, Settings.class);
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
}
