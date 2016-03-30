package assignment.paulay.percisely;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        toolbar.setTitle(R.string.settings_title);
        setSupportActionBar(toolbar);

        SeekBar weight = (SeekBar) findViewById(R.id.seekBar);
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        int savedWeight = settings.getInt("weight", -1);
        if (savedWeight != -1) {
            weight.setProgress(savedWeight);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings_confirm_button) {
            //TODO save settings
            SeekBar weight = (SeekBar) findViewById(R.id.seekBar);

            SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
            SharedPreferences.Editor preferenceEditor = settings.edit();
            int weightValue = weight.getProgress();
            if (weightValue == 0) {
                weightValue++;
            }
            preferenceEditor.putInt("weight", weightValue);
            preferenceEditor.apply();
            Toast.makeText(Settings.this, "Settings saved", Toast.LENGTH_SHORT).show();//TODO ID
            finish();
        } else if (id == R.id.settings_cancel_button) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Are you sure? Your unsaved changes will be discard.");//TODO ID needed
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {//TODO ID needed
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }
        return true;
    }
}
