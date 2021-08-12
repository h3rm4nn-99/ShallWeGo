package com.shallwego.client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntroSlideShow extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWizardMode(true);
        showStatusBar(false);
        setColorTransitionsEnabled(true);
        setSystemBackButtonLocked(true);
        setTransformer(AppIntroPageTransformerType.Fade.INSTANCE);
        AppIntroFragment slide1 = AppIntroFragment.newInstance(
                "Benvenuto in ShallWeGo!",
                "Crowdsourcing mobility in the digital era!",
                R.drawable.ic_near_me_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide2 = AppIntroFragment.newInstance(
                "Come funziona?",
                "ShallWeGo ti permette di aiutare gli altri utenti nella loro esperienza quotidiana coi mezzi pubblici",
                R.drawable.ic_favorite_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide3 = AppIntroFragment.newInstance(
                "Cosa puoi fare?",
                "Puoi segnalare fermate dei mezzi pubblici nella tua città e verificare le segnalazioni degli altri utenti se ti sono state assegnate. Guadagna karma e contribuisci a far crescere la community!",
                R.drawable.ic_add_location_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide4 = AppIntroFragment.newInstance(
                "Un po' di concetti di base",
                "Puoi effettuare una segnalazione rapida nella tua posizione usando il pulsante in basso a destra e scegliendo l'opzione 'Segnalazione rapida'",
                R.drawable.fast_report,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide5 = AppIntroFragment.newInstance(
                "Un po' di concetti di base",
                "Se invece vuoi effettuare la segnalazione di una fermata in un altro punto, tieni premuto quel punto sulla mappa nella schermata principale",
                R.drawable.ic_map_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide6 = AppIntroFragment.newInstance(
                "Un'ultima cosa...",
                "Data la natura dell'app è necessario concedere i permessi per la Geolocalizzazione e per l'Archiviazione",
                R.drawable.ic_gps_fixed_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        AppIntroFragment slide7 = AppIntroFragment.newInstance(
                "Tutto pronto!",
                "Ci auguriamo che l'esperienza sia di tuo gradimento!",
                R.drawable.ic_favorite_white_48dp,
                Color.DKGRAY,
                0,
                0,
                0,
                0,
                0
        );

        addSlide(slide1);
        addSlide(slide2);
        addSlide(slide3);
        addSlide(slide4);
        addSlide(slide5);
        addSlide(slide6);
        addSlide(slide7);

        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 6, true);
    }

    @Override
    protected void onIntroFinished() {
        super.onIntroFinished();
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("skipIntro", true);
        editor.commit();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    protected void onUserDeniedPermission(@NotNull String permissionName) {
        Toast.makeText(this, "Se vuoi usare l'app, devi concedere i permessi richiesti!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUserDisabledPermission(@NotNull String permissionName) {
        new AlertDialog.Builder(this)
                .setMessage("Quest'app non può funzionare senza questi permessi. Se vuoi utilizzarla riattiva i permessi dalle impostazioni.\nL'app verrà ora chiusa")
                .setPositiveButton("Ho capito!", (dialog, which) -> IntroSlideShow.this.finishAffinity()).show();
    }
}
