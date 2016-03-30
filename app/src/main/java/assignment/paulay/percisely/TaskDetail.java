package assignment.paulay.percisely;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

public class TaskDetail extends AppCompatActivity {

    private boolean playingRecord = false;
    private MediaPlayer mp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        final SQLiteDatabase db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks(name varchar(128) primary key, estimatedtime smallint not null,notification Datetime not null, deadline Datetime not null, bonus int not null, photopath text, recordpath text, description text)");
        Bundle bundle = getIntent().getExtras();
        Map data = null;
        if (bundle.getSerializable("elements") != null) {
            data = (Map) (((SerializedMap) bundle.getSerializable("elements")).getMap());
        }


        //get values
        final int bonus = Integer.parseInt(((String) (data != null ? data.get("bonus") : null)).split(" ")[0]);
        final String title = (String) (data != null ? data.get("name") : null);
        final int estimatedtime = (int) (data != null ? data.get("estimatedtime") : null);
        String estimatetime = "Estimate time cost: " + estimatedtime + " minutes";//TODO id required
        String deadline = "Deadline: " + (data != null ? data.get("deadline") : null);
        String photoPath = (String) (data != null ? data.get("photopath") : null);
        final String recordPath = (String) (data != null ? data.get("recordpath") : null);
        String description = "Description: " + (data != null ? data.get("description") : null);

        //initialize widgets
        TextView titleText = (TextView) findViewById(R.id.detailTaskTitle);
        TextView estimateText = (TextView) findViewById(R.id.detailTaskEstimateTime);
        TextView deadlineText = (TextView) findViewById(R.id.detailTaskDeadline);
        TextView descriptionText = (TextView) findViewById(R.id.detailTaskDescription);
        final Button playRecord = (Button) findViewById(R.id.detailTaskPlayRecord);
        Button startWorking = (Button) findViewById(R.id.detailTaskStartWorking);
        if (description.substring(13).isEmpty()) {
            descriptionText.setVisibility(View.GONE);
        }
        if (recordPath == null || recordPath.isEmpty()) {
            playRecord.setVisibility(View.GONE);
        }

        titleText.setText(title);
        estimateText.setText(estimatetime);
        deadlineText.setText(deadline);
        descriptionText.setText(description);
        playRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playingRecord) {
                    mp = new MediaPlayer();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playRecord.setText(R.string.detailtask_playrecord);
                            playingRecord = false;
                        }
                    });
                    try {
                        mp.setDataSource(recordPath);
                        mp.prepare();
                        mp.start();
                        playRecord.setText(R.string.addtask_record_stop);
                        playingRecord = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mp.stop();
                    mp.release();
                    mp = null;
                    playRecord.setText(R.string.detailtask_playrecord);
                    playingRecord = false;
                }
            }
        });

        startWorking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences workingprogress = getSharedPreferences("progress", MODE_PRIVATE);
                SharedPreferences.Editor editor = workingprogress.edit();
                editor.putString("title", title);
                editor.putLong("starttime", Calendar.getInstance().getTimeInMillis());
                editor.putInt("estimatetime", estimatedtime);
                editor.putInt("bonus", bonus);
                editor.apply();
                startActivity(new Intent(TaskDetail.this, WorkInProgress.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TaskDetail.this, TaskListActivity.class));
        finish();
    }
}
