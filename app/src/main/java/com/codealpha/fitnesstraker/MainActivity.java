package com.codealpha.fitnesstraker;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;

    EditText etType, etMinutes, etCalories, etSteps;
    Button btnSave;

    TextView tvTodaySummary, tvWeeklySummary, tvHistory;

    ProgressBar progressSteps, progressCalories, progressWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        etType = findViewById(R.id.etType);
        etMinutes = findViewById(R.id.etMinutes);
        etCalories = findViewById(R.id.etCalories);
        etSteps = findViewById(R.id.etSteps);

        btnSave = findViewById(R.id.btnSave);

        tvTodaySummary = findViewById(R.id.tvTodaySummary);
        tvWeeklySummary = findViewById(R.id.tvWeeklySummary);
        tvHistory = findViewById(R.id.tvHistory);

        progressSteps = findViewById(R.id.progressSteps);
        progressCalories = findViewById(R.id.progressCalories);
        progressWorkout = findViewById(R.id.progressWorkout);

        btnSave.setOnClickListener(v -> saveActivity());

        updateDashboard();
    }

    private void saveActivity() {

        String type = etType.getText().toString().trim();
        String minutesText = etMinutes.getText().toString().trim();
        String caloriesText = etCalories.getText().toString().trim();
        String stepsText = etSteps.getText().toString().trim();

        if (type.isEmpty() || minutesText.isEmpty()
                || caloriesText.isEmpty() || stepsText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int minutes = Integer.parseInt(minutesText);
            int calories = Integer.parseInt(caloriesText);
            int steps = Integer.parseInt(stepsText);

            if (minutes < 0 || calories < 0 || steps < 0) {
                Toast.makeText(this, "Enter valid positive values",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String date = java.text.DateFormat
                    .getDateInstance()
                    .format(new java.util.Date());

            boolean inserted = dbHelper.addActivity(
                    type, minutes, calories, steps, date
            );

            if (inserted) {
                Toast.makeText(this, "Activity saved!",
                        Toast.LENGTH_SHORT).show();

                etType.setText("");
                etMinutes.setText("");
                etCalories.setText("");
                etSteps.setText("");

                updateDashboard();

            } else {
                Toast.makeText(this, "Unable to save activity",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter numbers correctly",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDashboard() {

        int steps = 0;
        int calories = 0;
        int minutes = 0;

        String today = java.text.DateFormat
                .getDateInstance()
                .format(new java.util.Date());

        Cursor todayCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(steps), SUM(calories), SUM(minutes) " +
                        "FROM activities WHERE date = ?",
                new String[]{today}
        );

        if (todayCursor.moveToFirst()) {
            steps = todayCursor.getInt(0);
            calories = todayCursor.getInt(1);
            minutes = todayCursor.getInt(2);
        }

        todayCursor.close();

        tvTodaySummary.setText(
                "TODAY'S FITNESS\n\n" +
                        "Steps: " + steps + "\n" +
                        "Calories burned: " + calories + " kcal\n" +
                        "Workout time: " + minutes + " minutes"
        );

        progressSteps.setProgress(
                Math.min(100, steps * 100 / 10000)
        );

        progressCalories.setProgress(
                Math.min(100, calories * 100 / 500)
        );

        progressWorkout.setProgress(
                Math.min(100, minutes * 100 / 60)
        );

        Cursor weeklyCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(steps), SUM(calories), SUM(minutes) " +
                        "FROM activities " +
                        "WHERE date IN (" +
                        "SELECT date FROM activities ORDER BY id DESC LIMIT 7)",
                null
        );

        int weeklySteps = 0;
        int weeklyCalories = 0;
        int weeklyMinutes = 0;

        if (weeklyCursor.moveToFirst()) {
            weeklySteps = weeklyCursor.getInt(0);
            weeklyCalories = weeklyCursor.getInt(1);
            weeklyMinutes = weeklyCursor.getInt(2);
        }

        weeklyCursor.close();

        tvWeeklySummary.setText(
                "WEEKLY PROGRESS\n\n" +
                        "Steps: " + weeklySteps + "\n" +
                        "Calories: " + weeklyCalories + " kcal\n" +
                        "Workout: " + weeklyMinutes + " minutes"
        );

        Cursor historyCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT type, minutes, calories, steps, date " +
                        "FROM activities ORDER BY id DESC LIMIT 10",
                null
        );

        StringBuilder history = new StringBuilder();

        while (historyCursor.moveToNext()) {
            history.append("Activity: ")
                    .append(historyCursor.getString(0))
                    .append("\nTime: ")
                    .append(historyCursor.getInt(1))
                    .append(" minutes\nCalories: ")
                    .append(historyCursor.getInt(2))
                    .append(" kcal\nSteps: ")
                    .append(historyCursor.getInt(3))
                    .append("\nDate: ")
                    .append(historyCursor.getString(4))
                    .append("\n\n");
        }

        historyCursor.close();

        if (history.length() == 0) {
            history.append("No activities recorded yet.");
        }

        tvHistory.setText(history.toString());
    }
}