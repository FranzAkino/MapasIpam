package sv.edu.uesocc.mapasipam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


public class MainActivity extends ActionBarActivity  implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mapa;
    private EditText latitud, longitud;
    private static final int CONFIGURACION_RESULTADO = 1;
    private UiSettings uiSettings;
    CircleOptions circleOptions;
    boolean markerClicked;
    PolygonOptions polygonOptions;
    Polygon polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMap();
        latitud = (EditText) findViewById(R.id.latitud);
        longitud = (EditText) findViewById(R.id.longitud);

        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapa.addMarker(new MarkerOptions().position(latLng).
                        icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .draggable(true));

            }
        });
        mapa.setOnMarkerDragListener(this);

        markerClicked = false;
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
                /*double longitud = Double.valueOf(longitud.getText().toString());
                double latitud = Double.valueOf(latitud.getText().toString());
                mapa.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud)).title("Punto Mapa"));*/
                break;
            case R.id.menu_clear:
                mapa.clear();
                break;
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, Preferencias.class);
                startActivityForResult(i, CONFIGURACION_RESULTADO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMap() {

        if(mapa == null){
            mapa = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        if (mapa != null)
        {
            mapa.setMyLocationEnabled(true);
            uiSettings = mapa.getUiSettings();
            mapa.setOnMapLongClickListener(this);
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

        mapa.setMapType(Integer.parseInt(sharedPrefs.getString("tipoMapa", "1")));

        uiSettings.setZoomControlsEnabled(sharedPrefs.getBoolean("Zoomcontroll", false));
        uiSettings.setRotateGesturesEnabled(sharedPrefs.getBoolean("Rotategesture", false));
        uiSettings.setScrollGesturesEnabled(sharedPrefs.getBoolean("Scrollgesture", false));
        uiSettings.setZoomGesturesEnabled(sharedPrefs.getBoolean("Zoom Gesture", false));

        circleOptions.radius(sharedPrefs.getFloat("radioCirculo",1000));
//        circleOptions.fillColor(sharedPrefs.getInt("colorCirculo",));



    }

    @Override
    public void onMapLongClick(LatLng point) {
        Toast.makeText(this, "Point Long Click: "+ point, Toast.LENGTH_LONG).show();
        mapa.clear();
        circleOptions = new CircleOptions();
        circleOptions.center(point);
        circleOptions.fillColor(Color.HSVToColor(75, new float[]{Color.BLUE, 1, 1}));
        circleOptions.radius(1000);
        circleOptions.strokeWidth(1);
        Circle circle = mapa.addCircle(circleOptions);
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        latitud.setText("Marker " + marker.getId() + " DragStart");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        latitud.setText("Marker " + marker.getId() + " Drag@" + marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitud.setText("Marker " + marker.getId() + " DragEnd");
    }
}
