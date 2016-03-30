package assignment.paulay.percisely;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TaskAdd extends Activity {

    private SQLiteDatabase db;
    private MediaRecorder audioRecorder = null;
    private MediaPlayer audioPlayer = null;
    private String recordPath = null;//path of records
    private String photoPath = null;//path of photos
    private File photoDirectory = null;
    private boolean playing = false;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add);

        //initialize the database
        db = openOrCreateDatabase("percisely.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks(name varchar(128) primary key, estimatedtime smallint not null,notification Datetime not null, deadline Datetime not null, bonus int not null, photopath text, recordpath text, description text)");

        //initialize widgets
        final EditText titleBox = (EditText) findViewById(R.id.taskTitle);
        final Button saveTitle = (Button) findViewById(R.id.addTaskConfirmTitle);
        final View contents = findViewById(R.id.addtask_contents);
        contents.setVisibility(View.GONE);
        final EditText descriptionBox = (EditText) findViewById(R.id.taskDescription);

        final Button recordButton = (Button) findViewById(R.id.addRecord);
        final View recordPanel = findViewById(R.id.addtaskRecordPanel);
        recordPanel.setVisibility(View.GONE);
        final Button recordRecordButton = (Button) findViewById(R.id.addTaskRecordRecord);
        final Button recordPlayButton = (Button) findViewById(R.id.addTaskRecordPlay);
        final Button recordDeleteButton = (Button) findViewById(R.id.addTaskRecordDelete);
        recordPlayButton.setEnabled(false);
        recordDeleteButton.setEnabled(false);

        final Button photoButton = (Button) findViewById(R.id.addPhoto);
        final View photoPanel = findViewById(R.id.addtaskPhotoPanel);
        photoPanel.setVisibility(View.GONE);
        final ImageView photoPreview = (ImageView) findViewById(R.id.addTaskPhotoPreview);
        if (photoPreview.getDrawable() == null) {//hide the
            photoPreview.setVisibility(View.GONE);
        }
        Button photoRetakeButton = (Button) findViewById(R.id.addTaskPhotoRetake);
        final Button photoDeleteButton = (Button) findViewById(R.id.addTaskPhotoDelete);
        photoDeleteButton.setEnabled(false);


        final TimePicker deadlineTimepicker = (TimePicker) findViewById(R.id.deadlineTimePicker);
        final DatePicker deadlineDatepicker = (DatePicker) findViewById(R.id.deadlineDatePicker);
        Button saveButton = (Button) findViewById(R.id.taskButtonSave);
        final Button cancelButton = (Button) findViewById(R.id.taskButtonCancel);
//        TextView bonusDisplayer = (TextView) findViewById(R.id.addTaskBonusEstimated);//useless
        final NumberPicker hourPicker = (NumberPicker) findViewById(R.id.taskCostHourPicker);
        final NumberPicker minPicker = (NumberPicker) findViewById(R.id.taskCostMinPicker);

        //save title
        saveTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleBox.getText().toString().isEmpty()) {//prevent empty title
                    Toast.makeText(getApplicationContext(), R.string.addtask_warning_emptytitle, Toast.LENGTH_SHORT).show();
                } else {
                    title = titleBox.getText().toString();
                    if (db.query("tasks", new String[]{"name"}, "name = '" + title + "'", null, null, null, null).getCount() != 0) {//prevent duplicated title
                        Toast.makeText(TaskAdd.this, R.string.task_add_duplicatedtitle, Toast.LENGTH_SHORT).show();
                    } else {
                        contents.setVisibility(View.VISIBLE);
                        titleBox.setEnabled(false);
                        saveTitle.setEnabled(false);
                    }
                }
            }
        });

        //record precessing
        recordButton.setOnClickListener(new View.OnClickListener() {//expand record options
            @Override
            public void onClick(View v) {
                if (recordPanel.getVisibility() == View.GONE) {
                    recordPanel.setVisibility(View.VISIBLE);
                    recordButton.setEnabled(false);
                }
            }
        });
        recordRecordButton.setOnClickListener(new View.OnClickListener() {//record
            private boolean recording = false;

            @Override
            public void onClick(View v) {

                if (!recording) {
                    audioRecorder = new MediaRecorder();
                    audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
                    audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                    File recordfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Percisely/records/");
                    if (!recordfile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        recordfile.mkdirs();
                    }
                    recordPath = recordfile.getPath() + File.separator + titleBox.getText().toString() + ".amr";
                    audioRecorder.setOutputFile(recordPath);
                    try {
                        audioRecorder.prepare();
                        audioRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recordRecordButton.setText(R.string.addtask_record_stop);
                    recording = !recording;
                } else {
                    audioRecorder.stop();
                    audioRecorder.release();
                    audioRecorder = null;
                    recordRecordButton.setText(R.string.addtask_record_record);
                    recordDeleteButton.setEnabled(true);//enable other buttons
                    recordPlayButton.setEnabled(true);
                    recordRecordButton.setEnabled(false);//disable this button
                    recording = !recording;
                }
            }

        });
        recordPlayButton.setOnClickListener(new View.OnClickListener() {//previewing record
            @Override
            public void onClick(View v) {
                if (!playing) {
                    audioPlayer = new MediaPlayer();
                    recordDeleteButton.setEnabled(false);//prevent from deleting during playing
                    audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            recordPlayButton.setText(R.string.addtask_record_play);
                            recordDeleteButton.setEnabled(true);
                            playing = false;
                        }
                    });
                    try {
                        audioPlayer.setDataSource(recordPath);
                        audioPlayer.prepare();
                        audioPlayer.start();
                        recordPlayButton.setText(R.string.addtask_record_stop);
                        playing = !playing;
                    } catch (IOException e) {//generally this is due to file loss
                        Toast.makeText(getApplicationContext(), R.string.task_add_recordlost, Toast.LENGTH_SHORT).show();
                        recordRecordButton.setEnabled(true);
                        recordPlayButton.setEnabled(false);
                        recordDeleteButton.setEnabled(false);
                        e.printStackTrace();
                    }
                } else {
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                    recordDeleteButton.setEnabled(true);
                    recordPlayButton.setText(R.string.addtask_record_play);
                    playing = !playing;
                }
            }
        });

        recordDeleteButton.setOnClickListener(new View.OnClickListener() {//delete record
            @Override
            public void onClick(View v) {
                File recordFile = new File(recordPath);
                recordFile.delete();
                recordRecordButton.setEnabled(true);
                recordPlayButton.setEnabled(false);
                recordDeleteButton.setEnabled(false);
                recordPath = "";
            }
        });

        //photo taking
        View.OnClickListener photoListener = new View.OnClickListener() {//expand photoDirectory panel
            @Override
            public void onClick(View v) {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(getApplicationContext(), R.string.addtask_sdcard_unavailable, Toast.LENGTH_SHORT).show();
                } else {
                    photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Percisely/photos/" + titleBox.getText().toString() + ".jpg";
                    photoDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Percisely/photos/");
                    photoDirectory.mkdirs();
                    File photo = new File(photoDirectory, titleBox.getText().toString() + ".jpg");
                    Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoDeleteButton.setEnabled(true);
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    startActivityForResult(takePic, 1);
                }
            }
        };
        photoButton.setOnClickListener(photoListener);
        photoRetakeButton.setOnClickListener(photoListener);

        //photo deleting
        photoDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photo = new File(photoPath);
                ((ImageView) findViewById(R.id.addTaskPhotoPreview)).setImageBitmap(null);
                photo.delete();
                photoPath = "";
                photoDeleteButton.setEnabled(false);
            }
        });
        //costPicker
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

        //get confirmed values
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat deadlineFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");

        //save in database

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                int estimatedtime = hourPicker.getValue() * 60 + minPicker.getValue();
                GregorianCalendar deadline = new GregorianCalendar(deadlineDatepicker.getYear(), deadlineDatepicker.getMonth(), deadlineDatepicker.getDayOfMonth(), deadlineTimepicker.getCurrentHour(), deadlineTimepicker.getCurrentMinute());
                cv.put("deadline", deadlineFormatter.format(deadline.getTime()));
                deadline.add(Calendar.MINUTE, -estimatedtime);
                String description = descriptionBox.getText().toString();
                cv.put("name", title);
                cv.put("estimatedtime", estimatedtime);
                cv.put("notification", deadlineFormatter.format(deadline.getTime()));

                //generate bonus
                int bonus = (int) Math.pow(estimatedtime, 1.5);

                cv.put("bonus", bonus);
                cv.put("photopath", "");
                cv.put("recordpath", recordPath);
                cv.put("description", description);
                db.insert("tasks", null, cv);
                db.close();
                Toast.makeText(TaskAdd.this, R.string.task_add_added, Toast.LENGTH_SHORT).show();
                Intent backtoList = new Intent(TaskAdd.this, TaskListActivity.class);
                startActivity(backtoList);
                finish();
            }

        });

        //cancel action
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete unsaved record and photo
                File photo = new File(photoPath);
                File record = new File(recordPath);
                if (photo.exists()) {
                    photo.delete();
                }
                if (record.exists()) {
                    record.delete();
                }
                Intent task = new Intent(TaskAdd.this, TaskListActivity.class);
                startActivity(task);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ((ImageView) findViewById(R.id.addTaskPhotoPreview)).setImageBitmap(BitmapFactory.decodeFile(photoPath));
            (findViewById(R.id.addtaskPhotoPanel)).setVisibility(View.VISIBLE);
            (findViewById(R.id.addPhoto)).setEnabled(false);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
