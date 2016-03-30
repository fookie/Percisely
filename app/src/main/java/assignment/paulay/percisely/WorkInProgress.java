package assignment.paulay.percisely;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class WorkInProgress extends AppCompatActivity {

    private SharedPreferences.Editor editor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workinprogress);
        final SharedPreferences statistics = getSharedPreferences("statistics", MODE_PRIVATE);
        if (!statistics.contains("bonus")) {
            editor = statistics.edit();
            editor.putInt("bonus", 0);
            editor.apply();
        }
        final SharedPreferences progress = getSharedPreferences("progress", MODE_PRIVATE);

        //initialize widgets
        final TextView taskTitle = (TextView) findViewById(R.id.progressTaskTitle);
        final TextView currentbonus = (TextView) findViewById(R.id.progressBonus);
        Button progressbreak = (Button) findViewById(R.id.progressBreak);
        Button complete = (Button) findViewById(R.id.progressComplete);

        currentbonus.setText(String.valueOf(progress.getInt("bonus", 0)));

        //initialize variables
        final int fullbonus = progress.getInt("bonus", 0);
        final int estimatetime = progress.getInt("estimatetime", 0);

        taskTitle.setText(String.valueOf(statistics.getInt("bonus", 0)));
        taskTitle.setText(progress.getString("title", "?"));

        progressbreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long currenttime = Calendar.getInstance().getTimeInMillis() - progress.getLong("starttime", 0) + progress.getLong("break", 0);
                final int breakfee = (int) (1.5 * Math.sqrt(currenttime)) * 20;
                final AlertDialog.Builder fine = new AlertDialog.Builder(WorkInProgress.this);
                fine.setMessage(getString(R.string.working_break_confirmation) + breakfee + " points now.");
                fine.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (statistics.getInt("bonus", 0) < breakfee) {
                            Toast.makeText(WorkInProgress.this, R.string.working_break_insufficientbonus, Toast.LENGTH_SHORT).show();
                        } else {
                            editor = statistics.edit();
                            editor.putInt("bonus", statistics.getInt("bonus", 0) - breakfee);
                            taskTitle.setText(String.valueOf(statistics.getInt("bonus", 0)));
                            editor = progress.edit();
                            editor.putLong("break", progress.getLong("break", 0) + 20 * 60 * 1000);
                            editor.apply();
                            Toast.makeText(WorkInProgress.this, R.string.working_breakbought, Toast.LENGTH_SHORT).show();
                            currentbonus.setText(statistics.getInt("bonus", 0) - breakfee);
                        }
                    }
                });
                fine.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                fine.show();

            }
        });

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmation = new AlertDialog.Builder(WorkInProgress.this);
                confirmation.setMessage(R.string.working_finish_prompt);
                confirmation.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor = statistics.edit();
                        long currenttime = Calendar.getInstance().getTimeInMillis() - progress.getLong("starttime", 0) + progress.getLong("break", 0);
                        int earn;
                        if (currenttime < estimatetime) {
                            earn = (int) (Math.pow(currenttime * 1.0d / (estimatetime * 1.0d), 2) * fullbonus);
                        } else {
                            earn = (int) (fullbonus * (1 - Math.pow((currenttime - estimatetime) * 1.0d / (estimatetime * 1.0d), 1 / 3)));
                        }
                        if (earn <= 0) {
                            earn = 1;
                        }
                        editor.putInt("bonus", statistics.getInt("bonus", 0) + earn);
                        editor.putInt("taskfinished", statistics.getInt("taskfinished", 0) + 1);
                        editor.apply();
                        editor = progress.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(WorkInProgress.this, getString(R.string.working_finish_youveearned) + earn + getString(R.string.working_finish_pointsgoodjob), Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
                        db.delete("tasks", "name = '" + taskTitle.getText().toString() + "'", null);
                        startActivity(new Intent(WorkInProgress.this, TaskListActivity.class));
                        finish();
                    }
                });
                confirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                confirmation.show();
            }
        });
    }
}
