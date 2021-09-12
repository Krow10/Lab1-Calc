package com.example.lab1_calc;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import mehdi.sakout.aboutpage.AboutPage;

public class MainActivity extends AppCompatActivity {
    private EditText calc_input;
    private String input_before_change;
    private TextView calc_output;
    private Toolbar app_toolbar;

    private ImageButton delete_button;
    private Handler delete_action_handler;
    private Runnable delete_action_runnable;

    private String last_expression_error;
    private Resources res;

    // Show the menu icon in the app toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_toolbar, menu);
        return true;
    }

    // Respond to user actions from the toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO : Implement view for settings (color, ...)
                return true;

            case R.id.action_about:
                // Stylize copyright notice in description
                SpannableString desc = new SpannableString(getString(R.string.about_description));
                desc.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(res, R.color.button_text_operation_color, null)), desc.toString().indexOf('\n'), desc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                desc.setSpan(new StyleSpan(Typeface.BOLD), desc.toString().indexOf('\n'), desc.length(), 0);

                // AboutPage library from @medyo (https://github.com/medyo/android-about-page)
                View aboutPage = new AboutPage(this)
                        .isRTL(false)
                        .setImage(R.drawable.ic_logo_ets_sanstypo_fr_2)
                        .setDescription(desc)
                        .addEmail("etienne.donneger.1@ens.etsmtl.ca", getString(R.string.about_contact_email_label))
                        .addGitHub("Krow10", getString(R.string.about_contact_github_label))
                        .create();

                showPopup(aboutPage);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); // Disable keyboard popup for app
        setContentView(R.layout.activity_main);

        last_expression_error = "";
        res = getResources();
        input_before_change = "";

        app_toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(app_toolbar);

        delete_action_handler = new Handler();
        delete_button = findViewById(R.id.delete_button);
        delete_button.setOnLongClickListener(new View.OnLongClickListener() {
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
        delete_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    delete_action_handler.removeCallbacksAndMessages(null); // Stop deleting when button is released
                }
                return false;
            }
        });

        calc_output = findViewById(R.id.calc_output);
        calc_input = findViewById(R.id.calc_input);
        calc_input.setSelection(0);
        calc_input.requestFocus(); // Show cursor when starting app
        calc_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input_before_change = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("^([0-9]|[.+*/\\-÷×−])*$")) { // Sanitize user input
                    showError(getString(R.string.error_invalid_format_msg));
                    calc_input.setText(highlightOperators(input_before_change));
                    return;
                }

                final int input_length = calc_input.length();

                if (input_length == res.getInteger(R.integer.edittext_max_length)) { // Maximum input length reached
                    showError(String.format(res.getString(R.string.error_maximum_length_reached_msg), res.getInteger(R.integer.edittext_max_length)));
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
                    if (input_text_measures.get(0) < edittext_width*0.9) {
                        scaleInputText(1); // Default text size fits
                    } else if (input_text_measures.get(1) < edittext_width*0.95) {
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

                if (s.toString().matches("^([0-9]|\\.)+$")) { // Prevent evaluating expression if it's only one number
                    calc_output.setText("");
                    return;
                }

                try {
                    // Replace display characters with operators from exp4js
                    Expression e = new ExpressionBuilder(s.toString().replace('÷', '/').replace('×', '*').replace('−', '-')).build();
                    calc_output.setText("");
                    last_expression_error = "";

                    try {
                        double result = e.evaluate();
                        DecimalFormat f = new DecimalFormat("0.####"); // Format to 4 digits decimals and remove decimals if it's integer
                        calc_output.setText(f.format(result));
                    } catch (ArithmeticException ex) {
                        last_expression_error = ex.getMessage();
                    }
                } catch (IllegalArgumentException ex) {
                    last_expression_error = ex.getMessage();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onCalcButton(View v) {
        Button b = (Button) v;
        final int start = Math.max(calc_input.getSelectionStart(), 0);
        final int end = Math.max(calc_input.getSelectionEnd(), 0);
        SpannableString symbol = new SpannableString(b.getText().toString());

        // Add color to operator symbol
        if (isOperator(symbol.charAt(0)))
            symbol.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(res, R.color.button_text_operation_color, null)), 0, symbol.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add text to current cursor position replacing selected text if needed
        calc_input.getText().replace(Math.min(start, end), Math.max(start, end), symbol, 0, symbol.length());
    }

    public void onDeleteButton(View v) {
        final int start = calc_input.getSelectionStart();
        final int end = calc_input.getSelectionEnd();
        String current_input = calc_input.getText().toString();

        // Remove character to the left of cursor
        calc_input.setText(highlightOperators(current_input.substring(0, Math.max(start - 1, 0)) + current_input.substring(end, calc_input.length())));
        calc_input.setSelection(Math.max(start - 1, 0));
    }

    public void onClearButton(View v) {
        calc_input.setText("");
        calc_output.setText("");
        last_expression_error = "";
    }

    public void onEqualButton(View v) {
        if (!last_expression_error.isEmpty()) {
            showError(last_expression_error);
        } else if (!calc_output.getText().toString().isEmpty()) {
            calc_input.setText(calc_output.getText());
            calc_input.setSelection(calc_input.length());
            calc_output.setText("");
        }
    }

    private boolean isOperator(char c) {
        return c < 48 || c > 57; // 0-9 character range
    }

    private SpannableString highlightOperators(String s) {
        SpannableString new_colored_input = new SpannableString(s);
        for (int i = 0; i < new_colored_input.length(); ++i) { // Recolor operators in input text
            if (isOperator(new_colored_input.charAt(i)))
                new_colored_input.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(res, R.color.button_text_operation_color, null)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return new_colored_input;
    }

    private void showError(String msg) {
        Context context = getApplicationContext();
        final int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration); // Toast for notification : https://developer.android.com/guide/topics/ui/notifiers/toasts
        toast.show();
    }

    private void scaleInputText(float scaleFactor) {
        final float screen_density = res.getDisplayMetrics().density;
        final float startSize = calc_input.getTextSize() / screen_density; // Get text size in sp
        final float endSize = (float) ((res.getDimension(R.dimen.edittext_input_text_size) / screen_density)*scaleFactor);
        final int animationDuration = res.getInteger(R.integer.anim_input_scale_duration); // Animation duration in ms

        if (startSize != endSize) {
            ValueAnimator animator = ObjectAnimator.ofFloat(calc_input, "textSize", startSize, endSize);
            animator.setDuration(animationDuration);

            if (!animator.isRunning())
                animator.start();
        }
    }

    // TODO : Switch to dialog for dark surroundings + animations ?
    // Adapted popup snippet from : https://stackoverflow.com/questions/5944987/how-to-create-a-popup-window-popupwindow-in-android (@suragch)
    private void showPopup(View popupView){
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(ResourcesCompat.getColor(res, R.color.white, null))); // Set background for shadow to be visible
        popupWindow.setElevation(24);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }
}