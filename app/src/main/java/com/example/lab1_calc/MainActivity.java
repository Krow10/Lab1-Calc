package com.example.lab1_calc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
    private EditText calc_input;
    private TextView calc_output;
    private String last_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        this.last_error = "";
        this.calc_output = findViewById(R.id.calc_output);
        this.calc_input = findViewById(R.id.calc_input);

        this.calc_input.setSelection(0);
        this.calc_input.requestFocus(); // Show cursor when starting app
        this.calc_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("^([0-9]|\\.)+$")) // Prevent evaluating expression if it's only a single number
                    return;

                try {
                    // Replace display characters with operators from exp4js
                    Expression e = new ExpressionBuilder(s.toString().replace('÷', '/').replace('×', '*').replace('−', '-')).build();
                    calc_output.setText("");
                    last_error = "";

                    try {
                        double result = e.evaluate();
                        DecimalFormat f = new DecimalFormat("0.####"); // Format to 4 digits decimals and remove decimals if it's integer
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
        if (calc_input.length() == getResources().getInteger(R.integer.edittext_max_length))
            showError("Can't enter more than " + getResources().getInteger(R.integer.edittext_max_length) + " characters.");

        Button b = (Button) v;
        int start = Math.max(this.calc_input.getSelectionStart(), 0);
        int end = Math.max(this.calc_input.getSelectionEnd(), 0);
        SpannableString symbol = new SpannableString(b.getText().toString());

        // Make operators colored in the EditText input
        if (!symbol.toString().matches("^[0-9]$"))
            symbol.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.button_text_operation_color, null)), 0, symbol.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add text to current cursor position replacing selected text if needed
        this.calc_input.getText().replace(Math.min(start, end), Math.max(start, end), symbol, 0, symbol.length());
    }

    public void onDeleteButton(View v){
        int start = this.calc_input.getSelectionStart();
        int end = this.calc_input.getSelectionEnd();
        String current_input = this.calc_input.getText().toString();

        // Remove character to the left of cursor
        this.calc_input.setText(current_input.substring(0, Math.max(start - 1, 0)) + current_input.substring(end, this.calc_input.length()));
        this.calc_input.setSelection(Math.max(start - 1, 0));
    }

    public void onClearButton(View v){
        this.calc_input.setText("");
        this.calc_output.setText("");
        this.last_error = "";
    }

    public void onEqualButton(View v) {
        if (!this.last_error.isEmpty()) {
            showError(this.last_error);
        } else if (!this.calc_output.getText().toString().isEmpty()) {
            this.calc_input.setText(this.calc_output.getText());
            this.calc_input.setSelection(this.calc_input.length());
            this.calc_output.setText("");
        }
    }

    private void showError(String msg){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration); // Toast for notification : https://developer.android.com/guide/topics/ui/notifiers/toasts
        View view = toast.getView();

        // Set toast background color and text color for error message
        view.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.dark_grey, null), PorterDuff.Mode.SRC_IN);

        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

        toast.show();
    }
}