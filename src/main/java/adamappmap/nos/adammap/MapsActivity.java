package adamappmap.nos.adammap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;


import static adamappmap.nos.adammap.R.id.info;
import static adamappmap.nos.adammap.R.id.informationslabel;
import static adamappmap.nos.adammap.R.id.map;
import static adamappmap.nos.adammap.R.id.scrollView;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterClickListener<MyItem>,ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>
{

    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;
    static String USERAGENT = "ADAM for Android 0.2";
    static String APIKEY = "SECURED";

    public boolean onceloaded = false;
    public Spinner spinner;





    protected void activateSpinner()
    {
        spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, scrollView); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }
    protected void deactivateSpinner()
    {
        spinner.clearAnimation();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);


        mapFragment.getMapAsync(this);




    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public String removeFirstChar(String s) {
        return s.substring(1);
    }

    public String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    protected GoogleMap getMap()
    {
        return mMap;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!onceloaded) {

            activateSpinner();

            onceloaded = true;
            mMap = googleMap;

            setUpClusterer();
            if (mMap != null) {
                mClusterManager = new ClusterManager<MyItem>(this, mMap);
                mClusterManager.setRenderer(new InfoMarkerRenderer(this, mMap, mClusterManager));
            }

            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    mClusterManager.cluster();
                }
            });

            final CameraPosition[] mPreviousCameraPosition = {null};
            getMap().setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    CameraPosition position = getMap().getCameraPosition();
                    if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                        mPreviousCameraPosition[0] = getMap().getCameraPosition();
                        mClusterManager.cluster();

                    }
                }
            });
            mClusterManager.setOnClusterClickListener(this);


            mClusterManager
                    .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                        @Override
                        public boolean onClusterClick(final Cluster<MyItem> cluster) {

                            Log.d("PROTOCOLL", "CLICKED CLASS " + cluster.getClass().toString());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    cluster.getPosition(), (float) Math.floor(mMap
                                            .getCameraPosition().zoom + 1)), 300,
                                    null);
                            return true;
                        }
                    });
            // ClusterManager clusterManager = new ClusterManager<MyItem>(this, googleMap);
            InfoMarkerRenderer customRenderer = new InfoMarkerRenderer(this, googleMap, mClusterManager);
            mClusterManager.setRenderer(customRenderer);

            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
                @Override
                public boolean onClusterItemClick(MyItem myItem) {
                    Log.d("HALLO", "WELT");
                    return true;
                }

            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    Log.d("DEFAULT", "UNREC");

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(marker.getPosition())
                            .zoom(17).build();

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    // GET STATION NAME https://api.deutschebahn.com/fasta/v1/stations/

                    final String URLName = "https://api.deutschebahn.com/fasta/v1/stations/" + marker.getSnippet();
                    Log.d("START:", "URL: " + URLName);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("Authorization", APIKEY);
                    client.addHeader("User-Agent", USERAGENT);

                    client.setLoggingEnabled(true);
                    client.setLoggingLevel(3);

                    client.get(URLName, new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            Log.d("USING", "URL: " + URLName);

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                            String namestring = new String(response);
                            Log.d("RESPONSE:", ":" + namestring);
                            try {
                                JSONObject namestationjson = new JSONObject(namestring);
                                String name_of_statipn = namestationjson.getString("name");

                                Log.d("Name:", "Station: " + name_of_statipn);
                                final AlertDialog.Builder alertadd = new AlertDialog.Builder(MapsActivity.this);
                                LayoutInflater factory = LayoutInflater.from(MapsActivity.this);
                                final View view = factory.inflate(R.layout.sample, null);
                                final TextView informationstext = (TextView) findViewById(informationslabel);


                                alertadd.setView(view);
                                alertadd.setTitle("Objekt bei Station: " + name_of_statipn);

                                alertadd.setMessage("Dieser Aufzug funktioniert nichz.");
                                if (marker.isFlat())
                                {
                                    alertadd.setMessage("Dieser Aufzug funktioniert.");
                                }

                                final String descurl = "https://api.deutschebahn.com/fasta/v1/facilities/" + marker.getTitle();
                                alertadd.show();


                                AsyncHttpClient client = new AsyncHttpClient();
                                client.addHeader("Authorization", APIKEY);
                                client.addHeader("User-Agent", USERAGENT);

                                client.setLoggingEnabled(true);
                                client.setLoggingLevel(3);

                                client.get(descurl, new AsyncHttpResponseHandler() {

                                    @Override
                                    public void onStart() {
                                        Log.d("USING", "URL: " + URLName);

                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                                        String jsonstringobject = new String(response);
                                        Log.d("RESPONSE", "DESC" + jsonstringobject);

                                        JSONObject objectfromstring = null;
                                        try {
                                            objectfromstring = new JSONObject(jsonstringobject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            assert objectfromstring != null;
                                            if (objectfromstring.has("description") && !objectfromstring.get("description").equals(null)) {
                                                String informationString = "";
                                                try {
                                                    informationString = objectfromstring.getString("description");
                                                    if (informationstext != null) {
                                                        informationstext.setText(informationString);

                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onProgress(long bytesWritten, long totalSize) {

                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }

                                    @Override
                                    public void onRetry(int retryNo) {
                                    }

                                });

                                alertadd.setNeutralButton("Dieser Aufzug ist bei " + name_of_statipn, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {
                                        dlg.dismiss();
                                    }
                                });


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onProgress(long bytesWritten, long totalSize) {
                            long progressPercentage = (long) 100 * bytesWritten / totalSize;
                            Log.d("DOING", "WELL");

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d("ERROR:", "Description: " + error.getLocalizedMessage());

                        }

                        @Override
                        public void onRetry(int retryNo) {
                            Log.d("RETRY", "REQUEST");

                        }

                    });

                    return false;
                }
            });
        }





    }


    public JSONObject downloadData() throws Exception {
        String Url = "https://api.deutschebahn.com/fasta/v1/facilities";

        HttpsURLConnection conn = null;

        StringBuilder jsonResults = new StringBuilder();
        URL url = new URL(Url);
        Log.d("DOWNLOAD:", Url);
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USERAGENT);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Authorization", APIKEY);

        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        // Load the results into a StringBuilder
        // Authorization: Bearer 0f94dc391cf289a9e6aaf48e76f8eedd

        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            jsonResults.append(buff, 0, read);
        }
        if (conn != null) {
            conn.disconnect();
        }

        JSONObject object = new JSONObject(jsonResults.toString());
        return object;
    }

    // Declare a variable for the cluster manager.

    private void setUpClusterer() {
        // Position the map.

        // Latitude: 48°46′56″ N
        // Longitude: 9°10′37″ E


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.4656, 9.1037), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, getMap());



        getMap().setOnMarkerClickListener(mClusterManager);


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.


        addItems();

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                new MyCustomAdapterForItems());
    }

    private void addItems() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", APIKEY);
        client.addHeader("User-Agent", USERAGENT);
        client.get("https://api.deutschebahn.com/fasta/v1/facilities", new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.d("STATUS:", "ALL GOOD!");
                String elevatorString = new String(response);

                Log.d("OUTPUT:", "RESULT: " + elevatorString);
                Log.d("OUTPUT:", "HEADERS: " + headers.toString());

                // elevatorString = removeFirstChar(elevatorString);
                // elevatorString = removeLastChar(elevatorString);

                try {
                    JSONArray objectelevators = new JSONArray(elevatorString);
                    Log.d("JSON ARRAY:", "RESULT: " + objectelevators.toString());


                    for (int i = 0; i < objectelevators.length(); i++) {
                        JSONObject row = objectelevators.getJSONObject(i);

                        double X;
                        double Y;
                        int equipment;
                        String stationNumber = "";

                        X = 1;
                        Y = 1;
                        boolean ya = false;
                        boolean xa = false;

                        if (row.has("geocoordY") && !row.get("geocoordY").equals(null)) {
                            Y = row.getDouble("geocoordY");
                            ya = true;
                        }

                        if (row.has("geocoordX") && !row.get("geocoordX").equals(null)) {
                            X = row.getDouble("geocoordX");
                            xa = true;
                        }


                        if (xa && ya) {

                            equipment = row.getInt("equipmentnumber");



                            String eqstring = row.getString("equipmentnumber");

                            LatLng coord = new LatLng(X, Y);

                            // mMap.addMarker(new MarkerOptions().position(coord).title(equipment));

                            MyItem offsetItem = new MyItem(Y, X, equipment);


                            offsetItem.setIdenfikation(eqstring);
                            offsetItem.setSTATE(false);
                            if (row.has("stationnumber") && !row.get("stationnumber").equals(null)) {
                                stationNumber = row.getString("stationnumber");
                                offsetItem.setgetstationnumer(stationNumber);
                            }

                            if (row.has("state") && !row.get("state").equals(null)) {
                                String active = row.getString("state");
                                boolean isactive = false;
                                if (active.equals("ACTIVE"))
                                {
                                    isactive = true;
                                }
                                offsetItem.setSTATE(isactive);
                            }

                            mClusterManager.addItem(offsetItem);
                            deactivateSpinner();

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }

    public boolean onMarkerClick(MyItem itemmarker)
    {
        Log.d("CLICKED","I WAS CLICKED: " + itemmarker.getClass().toString());
        Log.d("ASD ASD ASD  DS","ASDS A");


        return true;
    }

    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.



        return true;
    }


    @Override
    public boolean onClusterItemClick(MyItem myItem) {
        Log.d("CLICK RECEIVED","Das ist die ID: " + myItem.getIdenfikation());

        return true;
    }

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        Log.d("WORK","IT'S WORKING");
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyItem myItem) {
        Log.d("TEST","CLICKED THERE");

    }

    public class InfoMarkerRenderer extends DefaultClusterRenderer<MyItem> implements ClusterManager.OnClusterClickListener<MyItem>,ClusterManager.OnClusterItemClickListener<MyItem> {

        final Drawable activE;
        final Drawable inactivE;
        Bitmap bitmapactive;
        Bitmap bitmapinactive;
        BitmapDescriptor descactive;
        BitmapDescriptor descainctive;


        public InfoMarkerRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);

            Resources res = getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            activE = ContextCompat.getDrawable(MapsActivity.this, R.drawable.elevatoractive);
            inactivE = ContextCompat.getDrawable(MapsActivity.this, R.drawable.elevatorinactive);

            // inactivE = res.getDrawable(R.drawable.elevatorinactive);

            bitmapactive = ((BitmapDrawable)activE).getBitmap();
            bitmapinactive = ((BitmapDrawable)inactivE).getBitmap();

            descactive = getMarkerIconFromDrawable(activE);
            descainctive = getMarkerIconFromDrawable(inactivE);


            //constructor
        }

        @Override
        protected void onBeforeClusterItemRendered(final MyItem infomarker, MarkerOptions markerOptions) {

            MyItem singleItem = infomarker.getMarker();
            markerOptions.flat(false);
            if (infomarker.getState()) {
                markerOptions.flat(true);
            }
            markerOptions.title(infomarker.getIdenfikation());
            markerOptions.snippet(infomarker.getstationnumer());

            // Log.d("STRING","HALLO: " +singleItem.getIdenfikation());

            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);


            if (singleItem.getState())
            {
                markerOptions.icon(descactive);

            }
            else {
                markerOptions.icon(descainctive);
            }

        }

        private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()/2, drawable.getIntrinsicHeight()/2, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()/2, drawable.getIntrinsicHeight()/2);
            drawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        @Override
        protected void onClusterRendered(Cluster<MyItem> cluster, Marker marker) {

        }
        @Override
        public boolean onClusterClick(Cluster<MyItem> cluster) {
            // Show a toast with some info when the cluster is clicked.
            String firstName = cluster.getItems().iterator().next().getIdenfikation();

            Log.d("CLICKED","YOU CLICKED A MARKER");


            // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
            // inside of bounds, then animate to center of the bounds.

            // Create the builder to collect all essential cluster items for the bounds.
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (ClusterItem item : cluster.getItems()) {
                builder.include(item.getPosition());
            }
            // Get the LatLngBounds
            final LatLngBounds bounds = builder.build();

            // Animate camera to the bounds
            try {
                getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        public boolean onClusterItemClick(MyItem item) {
            Log.d("CLICK RECEIVED","Das ist die ID: " + item.getIdenfikation());

            return true;
        }


        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 3; // if markers <=5 then not clustering
        }



    }
    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyCustomAdapterForItems() {
            myContentsView = getLayoutInflater().inflate(
                    R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }



}

