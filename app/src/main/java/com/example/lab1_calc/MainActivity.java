package com.example.lab1_calc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText calc_input;
    private TextView calc_output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        this.calc_output = findViewById(R.id.calc_output);
        this.calc_input = findViewById(R.id.calc_input);
        this.calc_input.setSelection(0);
    }

    public void onCalcButton(View v){
        Button b = (Button) v;
        /* TODO : [Can this be done in separate class handling the expression ?]
            * Add check to prevent chaining operators (e.g. '+x') => Replace operator at selection with new operator
            * Add check to prevent adding operators to start (e.g. '1+1' -x-> '+1+1')
         */
        int start = Math.max(this.calc_input.getSelectionStart(), 0);
        int end = Math.max(this.calc_input.getSelectionEnd(), 0);
        this.calc_input.getText().replace(Math.min(start, end), Math.max(start, end), b.getText(), 0, b.getText().length()); // Add text to current cursor position
    }

    public void onDeleteButton(View v){
        int start = this.calc_input.getSelectionStart();
        int end = this.calc_input.getSelectionEnd();
        String current_input = this.calc_input.getText().toString();

        // TODO : Same as line 30, check when deleting
        this.calc_input.setText(current_input.substring(0, Math.max(start - 1, 0)) + current_input.substring(end, this.calc_input.length()));
        this.calc_input.setSelection(Math.max(start - 1, 0));
    }

    public void onClearButton(View v){
        this.calc_input.setText("");
        this.calc_output.setText("");
    }

    public void onEqualButton(View v){
        // TODO : Add sliding animation for output text to input text
        this.calc_input.setText(this.calc_output.getText());
        this.calc_output.setText("");
    }
}