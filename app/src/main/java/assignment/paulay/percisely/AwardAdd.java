package assignment.paulay.percisely;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;


public class AwardAdd extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award_add);

        //initialize widgets
        final EditText title = (EditText) findViewById(R.id.awardTitle);
        final EditText description = (EditText) findViewById(R.id.awardDescription);
        final EditText moneycost = (EditText) findViewById(R.id.awardMoneyCost);
        Button save = (Button) findViewById(R.id.awardSave);
        Button cancel = (Button) findViewById(R.id.awardCancel);


        //time cost picker
        final NumberPicker hourPicker = (NumberPicker) findViewById(R.id.awardHourPicker);
        final NumberPicker minPicker = (NumberPicker) findViewById(R.id.awardMinPicker);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(99);
        hourPicker.setValue(0);
        minPicker.setMaxValue(59);
        minPicker.setMinValue(0);
        minPicker.setValue(1);
        NumberPicker.OnValueChangeListener carryover = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (oldVal == 59 && newVal == 0) {
                    hourPicker.setValue(hourPicker.getValue() + 1);
                } else if (newVal == 59 && oldVal == 0) {
                    hourPicker.setValue(hourPicker.getValue() - 1);
                }
            }
        };
        minPicker.setOnValueChangedListener(carryover);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
                db.execSQL("CREATE TABLE IF NOT EXISTS awards(name varchar(128) primary key, timecost smallint not null, moneycost int not null, description text, bonus int not null)");
                String name = title.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.addtask_warning_emptytitle, Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                } else {
                    if (db.query("awards", new String[]{"name"}, "name = '" + name + "'", null, null, null, null).getCount() != 0) {//duplication check
                        Toast.makeText(AwardAdd.this, R.string.award_add_duplicatedtitle, Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("name", title.getText().toString());
                        int timecost = hourPicker.getValue() * 60 + minPicker.getValue();
                        int money = Integer.parseInt(moneycost.getText().toString());
                        cv.put("timecost", timecost);
                        cv.put("moneycost", money);
                        cv.put("description", description.getText().toString());
                        SharedPreferences weight = getSharedPreferences("settings", MODE_PRIVATE);
                        float weightValue = weight.getInt("weight", -1);
                        if (weightValue == -1) {
                            weightValue = 50f;
                        }
                        weightValue = weightValue / 100f;
                        int requiredPoint = (int) (timecost * 10 * weightValue + money * 10 * (1 - weightValue));
                        cv.put("bonus", requiredPoint);
                        db.insert("awards", null, cv);
                        db.close();
                        Toast.makeText(AwardAdd.this, R.string.award_add_added, Toast.LENGTH_SHORT).show();
                        Intent backToList = new Intent(AwardAdd.this, AwardListActivity.class);
                        startActivity(backToList);
                        finish();
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cancel = new Intent(AwardAdd.this, AwardListActivity.class);
                startActivity(cancel);
                finish();
            }
        });
    }
}
