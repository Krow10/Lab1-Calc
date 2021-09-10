package com.example.lab1_calc;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private EditText calc_input;
    private TextView calc_output;
    private String last_error;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        this.last_error = "";
        this.res = getResources();
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
                if (calc_input.length() == res.getInteger(R.integer.edittext_max_length)) {
                    showError("Can't enter more than " + res.getInteger(R.integer.edittext_max_length) + " characters.");
                } else {
                    int value_id = 0;

                    // Scale down text size based on number of characters in the input for better visibility
                    if (12 <= calc_input.length() && calc_input.length() <= 16)
                        value_id = R.dimen.anim_input_first_scale_down_factor;
                    else if (calc_input.length() > 16)
                        value_id = R.dimen.anim_input_second_scale_down_factor;
                    else
                        scaleInputText(1);

                    if (value_id != 0) {
                        TypedValue animScaleFactor = new TypedValue();
                        res.getValue(value_id, animScaleFactor, true);

                        scaleInputText(animScaleFactor.getFloat());
                    }
                }

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

    public void onCalcButton(View v) {
        Button b = (Button) v;
        final int start = Math.max(this.calc_input.getSelectionStart(), 0);
        final int end = Math.max(this.calc_input.getSelectionEnd(), 0);
        SpannableString symbol = new SpannableString(b.getText().toString());

        // Make operators colored in the EditText input
        if (this.isOperator(symbol.charAt(0)))
            symbol.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(this.res, R.color.button_text_operation_color, null)), 0, symbol.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add text to current cursor position replacing selected text if needed
        this.calc_input.getText().replace(Math.min(start, end), Math.max(start, end), symbol, 0, symbol.length());
    }

    public void onDeleteButton(View v) {
        final int start = this.calc_input.getSelectionStart();
        final int end = this.calc_input.getSelectionEnd();
        String current_input = this.calc_input.getText().toString();

        // Remove character to the left of cursor
        SpannableString new_colored_input = new SpannableString(current_input.substring(0, Math.max(start - 1, 0)) + current_input.substring(end, this.calc_input.length()));
        for (int i = 0; i < new_colored_input.length(); ++i){ // Color back operators
            if (this.isOperator(new_colored_input.charAt(i))){
                new_colored_input.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(this.res, R.color.button_text_operation_color, null)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        this.calc_input.setText(new_colored_input);
        this.calc_input.setSelection(Math.max(start - 1, 0));
    }

    public void onClearButton(View v) {
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

    private boolean isOperator(char c){
        return c < 48 || c > 57; // 0-9 character range
    }

    private void showError(String msg) {
        Context context = getApplicationContext();
        final int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration); // Toast for notification : https://developer.android.com/guide/topics/ui/notifiers/toasts
        toast.show();
    }

    private void scaleInputText(float scaleFactor) {
        final float screen_density = this.res.getDisplayMetrics().density;
        final float startSize = this.calc_input.getTextSize() / screen_density; // Get text size in sp
        final float endSize = (float) ((this.res.getDimension(R.dimen.edittext_input_text_size) / screen_density)*scaleFactor);
        final int animationDuration = this.res.getInteger(R.integer.anim_input_scale_duration); // Animation duration in ms

        if (startSize != endSize) {
            ValueAnimator animator = ObjectAnimator.ofFloat(this.calc_input, "textSize", startSize, endSize);
            animator.setDuration(animationDuration);

            if (!animator.isRunning())
                animator.start();
        }
    }
}