package app.neont.api;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class ButtonHelper {

    public static void setup(Button button, ProgressBar spinner, EditText... inputs) {
        button.setEnabled(false);
        button.setBackgroundResource(R.drawable.bg_button_disabled);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean allFilled = true;
                for (EditText input : inputs) {
                    if (TextUtils.isEmpty(input.getText().toString().trim())) {
                        allFilled = false;
                        break;
                    }
                }
                button.setEnabled(allFilled);
                button.setBackgroundResource(allFilled ? R.drawable.bg_button_active : R.drawable.bg_button_disabled);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        for (EditText input : inputs) {
            input.addTextChangedListener(watcher);
        }
    }

    // Ganti teks tombol jadi "MEMUAT..." (2 parameter aja)
    public static void showSpinner(Button button, ProgressBar spinner) {
        button.setEnabled(false);
        button.setText("MEMUAT...");
        if (spinner != null) spinner.setVisibility(View.GONE);
    }

    // Balikin teks tombol (2 parameter aja, pake tag)
    public static void hideSpinner(Button button, ProgressBar spinner) {
        button.setEnabled(true);
        // Balikin teks asli dari tag (default: "SIMPAN")
        String originalText = (String) button.getTag();
        if (originalText != null) {
            button.setText(originalText);
        } else {
            button.setText("SIMPAN"); // Fallback
        }
        if (spinner != null) spinner.setVisibility(View.GONE);
    }
}
