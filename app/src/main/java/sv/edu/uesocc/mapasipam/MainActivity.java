package sv.edu.uesocc.mapasipam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleMap.OnMapLongClickListener,GoogleMap.OnMarkerDragListener{


    private GoogleMap mapa;
    private EditText Latitud, Longitud;
    private static final int CONFIGURACION_RESULTADO = 1;
    private UiSettings uiSettings;
    CircleOptions circleOptions;
    boolean markerClicked;
    PolygonOptions polygonOptions;
    Polygon polygon;
    Circle circle;
    ArrayList<LatLng> markerPoints;
    TextView distTiempo;
    MarkerOptions markerOptions;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMap();
        Latitud = (EditText) findViewById(R.id.Latitud);
        Longitud = (EditText) findViewById(R.id.Longitud);
        distTiempo = (TextView) findViewById(R.id.dist_tiempo);

        // Inicializar
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        mapa = fm.getMap();
        // Getting reference to Button
        Button btnBus = (Button) findViewById(R.id.btn_bus);

        if (mapa != null) {

            // Enable MyLocation Button in the Map
            mapa.setMyLocationEnabled(true);
            ////////////////////////////////Autocompletar---búsqueda//////////////////////////////////
            OnClickListener findClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Getting reference to EditText to get the user input location
                    EditText etLocation = (EditText) findViewById(R.id.lugarnombre);

                    // Getting user input location
                    String location = etLocation.getText().toString();

                    if(location!=null && !location.equals("")){
                        new GeocoderTask().execute(location);
                    }
                }
            };

            // Setting button click event listener for the find button
            btnBus.setOnClickListener(findClickListener);


            //////////////////////////////////////////////////////////////////////////////////////////
            mapa.setOnMapClickListener(new OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {

                    // Already two locations
                    if(markerPoints.size()>1){
                        markerPoints.clear();
                        mapa.clear();
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(point);

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(point);

                    /**
                     * For the start location, the color of marker is GREEN and
                     * for the end location, the color of marker is RED.
                     */

                    if(markerPoints.size()==1){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).draggable(true);
                    }else if(markerPoints.size()==2){
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true);
                    }

                    // Add new marker to the Google Map Android API V2
                    mapa.addMarker(options);

                    // Checks, whether start and end locations are captured
                    if(markerPoints.size() >= 2){
                        LatLng origin = markerPoints.get(0);
                        LatLng dest = markerPoints.get(1);

                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);

                        DownloadTask downloadTask = new DownloadTask();

                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            });
            // Restoring the markers on configuration changes
            if(savedInstanceState!=null){
                if(savedInstanceState.containsKey("points")){
                    markerPoints = savedInstanceState.getParcelableArrayList("points");
                    if(markerPoints!=null){
                        for(int i=0;i<markerPoints.size();i++){
                            drawMarker(markerPoints.get(i));
                        }
                    }
                }
            }
        }


    }
    private void drawMarker(LatLng point){

        MarkerOptions mOptions = new MarkerOptions();
        mOptions.position(point);
        if(markerPoints.size()==1){
            mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).draggable(true);
        }else if(markerPoints.size()==2){
            mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true);
        }
        mapa.addMarker(mOptions);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Adding the pointList arraylist to Bundle
        outState.putParcelableArrayList("points", markerPoints);

        // Saving the bundle
        super.onSaveInstanceState(outState);
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
                double longitud = Double.valueOf(Longitud.getText().toString());
                double latitud = Double.valueOf(Latitud.getText().toString());
                mapa.addMarker(new MarkerOptions().position(new LatLng(latitud,longitud)).title("Punto Mapa"));
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
            mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            //SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
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

        if(circleOptions!=null) {
            circle.setRadius(Float.valueOf(sharedPrefs.getString("radioCirculo", "1000")));
            circle.setFillColor(sharedPrefs.getInt("colorCirculo", Color.RED));
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Toast.makeText(this, "Point Long Click: " + point, Toast.LENGTH_LONG).show();
        mapa.clear();
        circleOptions = new CircleOptions();
        circleOptions.center(point);
        circleOptions.fillColor(Color.HSVToColor(75, new float[]{Color.BLUE, 1, 1}));
        circleOptions.radius(1000);
        circleOptions.strokeWidth(1);
        circle = mapa.addCircle(circleOptions);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng pos = marker.getPosition();

        // Updating the infowindow contents with the new marker coordinates
        marker.setSnippet(pos.latitude + "," + pos.longitude);

        // Updating the infowindow for the user
        marker.showInfoWindow();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    ///////////////////////////////////para mostrar ruta entre dos puntos/////////////////////////////////////////
    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Error downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
                    ParserTask parserTask = new ParserTask();
                    parserTask.execute(result);

            // Invokes the thread for parsing the JSON data

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(3);
                lineOptions.color(Color.MAGENTA);
            }

            distTiempo.setText("Distancia:"+distance + ", Tiempo:"+duration);

            // Drawing polyline in the Google Map for the i-th route
            mapa.addPolyline(lineOptions);
        }
    }

    ////////////////////////////////////////////Busqueda//////////////////////////////////////////////////////////////
// An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            mapa.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mapa.addMarker(markerOptions);

                // Locate the first location
                if(i==0)
                    mapa.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
    //@Override
    /*public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu., menu);
        return true;
    }*/
}




