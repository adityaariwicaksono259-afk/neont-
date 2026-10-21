package app.neont.api;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class ButtonHelper {

    // Pasang logika dinamis (abu-abu -> biru) & spinner
    public static void setup(Button button, ProgressBar spinner, EditText... inputs) {
        // Default: abu-abu, disabled
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

    // Tampilkan spinner (sembunyiin tombol)
    public static void showSpinner(Button button, ProgressBar spinner) {
        button.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
    }

    // Sembunyiin spinner (munculin tombol lagi)
    public static void hideSpinner(Button button, ProgressBar spinner) {
        spinner.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
    }
}
