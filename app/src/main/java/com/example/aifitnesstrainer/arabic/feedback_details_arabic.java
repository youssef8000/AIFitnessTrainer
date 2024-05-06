package com.example.aifitnesstrainer.arabic;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.user_feedback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.NumberFormat;
import java.util.Locale;
public class feedback_details_arabic extends Fragment {
    private int id;
    private TranslatorOptions translatorOptions;
    private Translator translator;
    private boolean isTranslating = false;
    private TextView exercise_feedback;

    public feedback_details_arabic() {
        // Required empty public constructor
    }
    public feedback_details_arabic(int id) {
        this.id=id;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_details_arabic, container, false);
        TextView exercise_name = view.findViewById(R.id.exercise_name);
        TextView exercise_goal = view.findViewById(R.id.exercise_goal);
        TextView exercise_correct = view.findViewById(R.id.exercise_correct);
        TextView exercise_incorrect = view.findViewById(R.id.exercise_incorrect);
        TextView exercise_accuracy = view.findViewById(R.id.exercise_accuracy);
        exercise_feedback = view.findViewById(R.id.exercise_feedback);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        user_feedback feedback = databaseHelper.getFeedbackById(id);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ar"));
        String arabicGoal = numberFormat.format(feedback.getGoal());
        String arabicCorrectScore = numberFormat.format(feedback.getCorrect_score());
        String arabicIncorrectScore = numberFormat.format(feedback.getIncorrect_score());
        String arabicAccuracy = numberFormat.format(feedback.getAccuracy() * 100);

        // Set the Arabic text to the TextViews
        exercise_name.setText("اسم التمرين: " + feedback.getEx_name());
        exercise_goal.setText("هدفك: " + arabicGoal);
        exercise_correct.setText("عدد المرات الصحيحة: " + arabicCorrectScore);
        exercise_incorrect.setText("عدد المرات الخاطئة: " + arabicIncorrectScore);
        exercise_accuracy.setText("الدقة: " + (String.format("%s%%", arabicAccuracy)));
        String feedbackText = feedback.getWorkoutFeedback();

        translateFeedback(feedbackText);

        exercise_feedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isTranslating) {
                    String newText = editable.toString();
                    translateFeedback(newText);
                }
            }
        });
        exercise_feedback.setText("ملاحظاتنا: ");

        return view;
    }

    private void translateFeedback(String textToTranslate) {
        isTranslating = true;
        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.ARABIC)
                .build();

        // Initialize Translator
        translator = Translation.getClient(translatorOptions);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        aVoid -> {
                            translator.translate(textToTranslate)
                                    .addOnSuccessListener(
                                            translatedText -> {
                                                // Set translated text to TextView
                                                exercise_feedback.setText("ملاحظاتنا: " + translatedText);
                                                isTranslating = false;
                                            })
                                    .addOnFailureListener(
                                            e -> {
                                                // Handle translation failure
                                                Log.e("Translation", "Translation failed: " + e.getMessage());
                                                isTranslating = false;
                                            });
                        })
                .addOnFailureListener(
                        e -> {
                            // Handle download failure
                            Log.e("Translation", "Model download failed: " + e.getMessage());
                            isTranslating = false;
                        });
    }
}