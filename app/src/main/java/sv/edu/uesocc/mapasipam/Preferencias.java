package sv.edu.uesocc.mapasipam;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by akino on 05-26-15.
 */
public class Preferencias extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
    }

}
