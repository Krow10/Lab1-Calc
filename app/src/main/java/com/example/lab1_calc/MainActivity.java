package com.example.lab1_calc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private EditText calc_input; // TODO : Add character limit + limit number size (15)
    private TextView calc_output;
    private String last_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        this.calc_output = findViewById(R.id.calc_output);
        this.calc_input = findViewById(R.id.calc_input);
        this.last_error = "";

        this.calc_input.setSelection(0);
        this.calc_input.requestFocus();
        this.calc_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("^([0-9]|\\.)+$"))
                    return;

                try {
                    Expression e = new ExpressionBuilder(s.toString().replace('÷', '/').replace('×', '*').replace('−', '-')).build();
                    calc_output.setText("");
                    last_error = "";

                    try {
                        System.out.println("Expression : " + e.getVariableNames());
                        double result = e.evaluate();
                        DecimalFormat f = new DecimalFormat("0.####");
                        calc_output.setText(f.format(result));
                    } catch (ArithmeticException ex) {
                        last_error = ex.getMessage();
                    }
                } catch (IllegalArgumentException ex) {
                    last_error = ex.getMessage();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    public void onEqualButton(View v) {
        /* TODO :
         * Add sliding animation for output text to input text
         * Add error notification
        */

        if (!last_error.isEmpty()) {
            this.showError(this.last_error);
        } else if (!this.calc_output.getText().toString().isEmpty()) {
            this.calc_input.setText(this.calc_output.getText());
            this.calc_input.setSelection(this.calc_input.length());
            this.calc_output.setText("");
        }
    }

    private void showError(String msg){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration);
        View view = toast.getView();

        view.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.dark_grey, null), PorterDuff.Mode.SRC_IN);

        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

        toast.show();
    }
}