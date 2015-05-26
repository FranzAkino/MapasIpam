package sv.edu.uesocc.mapasipam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity  implements GoogleMap.OnMapLongClickListener {

    private  GoogleMap mMap;
    private EditText latitud, longitud;
    private static final int CONFIGURACION_RESULTADO = 1;
    private UiSettings mUiSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMap();
        latitud = (EditText) findViewById(R.id.latitud);
        longitud = (EditText) findViewById(R.id.longitud);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(item.getItemId()){
            case R.id.menu_location:
              //  double longitud = Double.valueOf(longitud.getText().toString());
              //  double latitud = Double.valueOf(latitud.getText().toString());
              //  mMap.addMarker(new MarkerOptions().position(new LatLng(latitud,longitud)).title("Punto Mapa"));
                break;
            case R.id.menu_clear:
                mMap.clear();
                break;
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, Preferencias.class);
                startActivityForResult(i, CONFIGURACION_RESULTADO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }






    private void setUpMap() {

        if(mMap == null){
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        if (mMap != null)
        {
            mMap.setMyLocationEnabled(true);
            mUiSettings = mMap.getUiSettings();
            mMap.setOnMapLongClickListener(this);
            DefinirConfiguracion();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CONFIGURACION_RESULTADO)
        {
            DefinirConfiguracion();
        }
    }





    private void DefinirConfiguracion()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mMap.setMapType(Integer.parseInt(sharedPrefs.getString("tipoMapa", "1")));

        mUiSettings.setZoomControlsEnabled(sharedPrefs.getBoolean("Zoomcontroll", false));
        mUiSettings.setRotateGesturesEnabled(sharedPrefs.getBoolean("Rotategesture", false));
        mUiSettings.setScrollGesturesEnabled(sharedPrefs.getBoolean("Scrollgesture", false));
        mUiSettings.setZoomGesturesEnabled(sharedPrefs.getBoolean("Zoom Gesture", false));

    }

    @Override
    public void onMapLongClick(LatLng point) {
        Toast.makeText(this, "Point Long Click: "+ point, Toast.LENGTH_LONG).show();
        mMap.clear();
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(point);
        circleOptions.fillColor(Color.HSVToColor(75, new float[]{Color.BLUE, 1, 1}));
        circleOptions.radius(1000);
        circleOptions.strokeWidth(1);
        Circle circle = mMap.addCircle(circleOptions);
    }




}
