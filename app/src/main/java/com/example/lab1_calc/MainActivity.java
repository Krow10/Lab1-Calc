package com.example.lab1_calc;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText calc_input;
    private TextView calc_output;

    private ImageButton delete_button;
    private Handler delete_action_handler;
    private Runnable delete_action_runnable;

    private String last_error;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        this.last_error = "";
        this.res = getResources();

        this.delete_action_handler = new Handler();
        this.delete_button = findViewById(R.id.delete_button);
        this.delete_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                delete_action_handler.postDelayed(delete_action_runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (calc_input.length() > 0) {
                            onDeleteButton(v);
                            delete_action_handler.postDelayed(delete_action_runnable, res.getInteger(R.integer.delete_button_long_press_speed)); // Re-run method to keep deleting
                        }
                    }
                }, res.getInteger(R.integer.delete_button_long_press_speed));

                return true;
            }
        });
        this.delete_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    delete_action_handler.removeCallbacksAndMessages(null); // Stop deleting when button is released
                }
                return false;
            }
        });

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
                if (!s.toString().matches("^([0-9]|[\\.+*\\/\\-÷×−])*$")){ // Sanitize user input
                    showError("Invalid format used.");
                    calc_input.setText("");
                    return;
                } else if (s.toString().matches("^([0-9]|\\.)+$")){ // Prevent evaluating expression if it's only one number
                    return;
                }

                final int input_length = calc_input.length();

                if (input_length == res.getInteger(R.integer.edittext_max_length)) {
                    showError("Can't enter more than " + res.getInteger(R.integer.edittext_max_length) + " characters.");
                } else {
                    // Setup scale factors value from constants
                    TypedValue animScaleFactor = new TypedValue();
                    List<Float> scale_factors = Arrays.asList(new Float[3]);
                    scale_factors.set(0, 1.f); // No scaling (default)
                    res.getValue(R.dimen.anim_input_first_scale_down_factor, animScaleFactor, true);
                    scale_factors.set(1, animScaleFactor.getFloat());
                    res.getValue(R.dimen.anim_input_second_scale_down_factor, animScaleFactor, true);
                    scale_factors.set(2, animScaleFactor.getFloat());

                    // Calculate for each scale factor the size of the input text on screen
                    TextPaint t = new TextPaint(calc_input.getPaint());
                    List<Float> input_text_measures = Arrays.asList(new Float[3]);
                    for (int i = 0; i < 3; ++i) {
                        t.setTextSize((float) ((res.getDimension(R.dimen.edittext_input_text_size)) * scale_factors.get(i)));
                        input_text_measures.set(i, t.measureText(calc_input.getText() + "1", 0, input_length + 1));
                    }

                    int value_id = 0;
                    final int edittext_width = calc_input.getWidth();
                    // Get scale factor based on text width
                    if (input_text_measures.get(0) < edittext_width*0.9){
                        scaleInputText(1); // Default text size fits
                    } else if (input_text_measures.get(1) < edittext_width*0.95){
                        value_id = R.dimen.anim_input_first_scale_down_factor;
                    } else {
                        value_id = R.dimen.anim_input_second_scale_down_factor;
                    }

                    // Start the scale animation for better readability
                    if (value_id != 0) {
                        res.getValue(value_id, animScaleFactor, true);
                        scaleInputText(animScaleFactor.getFloat());
                    }
                }

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

        // Add color to operator symbol
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
        for (int i = 0; i < new_colored_input.length(); ++i){ // Recolor operators in input text
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