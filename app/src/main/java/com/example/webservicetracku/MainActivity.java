package com.example.webservicetracku;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.webservicetracku.database.core.TrackUDatabaseManager;
import com.example.webservicetracku.database.entities.Geolocation;
import com.example.webservicetracku.database.entities.User;
import com.example.webservicetracku.gps.GPSManager;
import com.example.webservicetracku.gps.GPSManagerInterface;
import com.example.webservicetracku.networking.WebServiceManager;
import com.example.webservicetracku.networking.WebServiceManagerInterface;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GPSManagerInterface, Dialog_Choose_User.DialogChooserInterface, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, WebServiceManagerInterface, View.OnClickListener {

    String Path = "http://10.20.39.193:8888/WebServiceREST/webresources";
    Activity thisActivity=this;
    org.osmdroid.views.MapView map;
    GPSManager gpsManager;
    double latitude;
    double longitude;
    User newUser;
    User loginUser;

    static TrackUDatabaseManager INSTANCE;
    private FloatingActionButton floatingActionButton;
    private String email;
    private Handler mHandlerThread;
    private Thread thread;
    private Geolocation geo;

    //CONEXION A LA BASE DE DATOS.
    static TrackUDatabaseManager getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TrackUDatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE= Room.databaseBuilder(context,
                            TrackUDatabaseManager.class, "DbApp").
                            allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }

    //HACE LOGIN PRIMERAMENTE CON EL SERVER, EN BASE AL CUAL REALIZARA OTRAS ACCIONES.
    public void userRegistrationServer(String userName, String password){

        loginUser = new User();
        loginUser.email = userName;
        loginUser.passwordHash = password;

        String newuser = userName+"-"+md5(password);

        WebServiceManager.CallWebServiceOperation(this, Path,
                "user",
                "login",
                "PUT",
                newuser,
                "Login");
    }


    // HACE EL RESGITRO EN PRIMERA INSTANCIA CON EL SERVER, EN BASE A LA RESPUESTA EJECUTARA LA SIGUIENTE ACCIÓN
    public void userRegistration(String userName,String password){
        try{
            newUser = new User();
            newUser.email=userName;
            newUser.passwordHash=md5(password);

            String newuser = userName+"-"+md5(password);

            WebServiceManager.CallWebServiceOperation(this, Path,
                    "user",
                    "register",
                    "PUT",
                    newuser,
                    "signUp");

        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    //EN CASO DE QUE LOGIN CON SERVER FALLE, VERIFICA LOCALMENTE.
    public boolean userAuth(String userName,String password){
        try{
            List<User> usersFound=getDatabase(this).userDao().getUserByEmail(userName);
            if(usersFound.size()>0){
                if(usersFound.get(0).passwordHash.equals(md5(password))){
                    return true;
                }
            }else{
                return false;
            }
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
        return false;
    }


    //HACE EL RESGITRO DE LAS LOCALIZACIONES LOCALMENTE Y LUEGO SUBE AL WEB SERVICE.
    public void registerLocation(String email, double latitude, double longitude){
        try {
            Geolocation geolocation = new Geolocation();
            geolocation.email=email;
            geolocation.latitude=latitude;
            geolocation.longitude=longitude;
            geolocation.time=(System.currentTimeMillis()/1000);
            geo = geolocation;
            //INSTANCE.geoDao().insertGeolocation(geolocation);

            ArrayList<Geolocation> geolist = new ArrayList<>();
            geolist.add(geolocation);
            Gson gson = new Gson();
            String location = gson.toJson(geolist);
            System.out.println(location);

            WebServiceManager.CallWebServiceOperation(this, Path,
                    "location",
                    "savelocation",
                    "PUT",
                    location,
                    "SaveLocation");


        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDatabase(this);

        String callType=getIntent().getStringExtra("callType");
        if(callType.equals("userLogin")) {

            String userName = getIntent().getStringExtra("userName");
            String password = getIntent().getStringExtra("password");
            userRegistrationServer(userName,password);

        }else if(callType.equals("userRegistration")) {

            String userName = getIntent().getStringExtra("userName");
            String password = getIntent().getStringExtra("password");
            userRegistration( userName, password);

        }else{
            finish();
        }

        mHandlerThread = new Handler();
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        map = findViewById(R.id.map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setVisibility(View.INVISIBLE);


        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    try{
                        getDatabase();
                        List<Geolocation> geo = INSTANCE.geoDao().getAllGeolocation();

                        if(geo.size()>0){

                            final Message message = new Message();
                            message.what = 100;
                            message.arg1 = geo.size();
                            message.obj = geo;

                            System.out.println("tamaño-> "+message.arg1);

                            mHandlerThread.sendMessage(message);
                        }

                        Thread.sleep(10000);
                    }catch (InterruptedException e) {
                        System.out.println("EXPLOTO");
                    }
                }

            }


            TrackUDatabaseManager INSTANCE;
            private TrackUDatabaseManager getDatabase() {
                if (INSTANCE == null) {
                    synchronized (TrackUDatabaseManager.class) {
                        if (INSTANCE == null) {
                            INSTANCE= Room.databaseBuilder(getApplicationContext(),
                                    TrackUDatabaseManager.class, "DbApp").
                                    allowMainThreadQueries().build();
                        }
                    }
                }
                return INSTANCE;
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(map==null) {
            map = findViewById(R.id.map);
            if (map != null) {
                map.setTileSource(TileSourceFactory.MAPNIK);
                map.onResume();
            }
        }else{
            map.onResume();
        }
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        MyLocationNewOverlay myLocationNewOverlay=
                new MyLocationNewOverlay(
                        new GpsMyLocationProvider(this),map);
        myLocationNewOverlay.enableMyLocation();

        mHandlerThread = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 100) {

                    List<User> user = (List<User>) msg.obj;
                    Gson gson = new Gson();
                    String location = gson.toJson(user);

                    System.out.println("HolaDESDEHANDLE");
                    WebServiceManager.CallWebServiceOperation((WebServiceManagerInterface) thisActivity, Path,
                            "location",
                            "savelocation",
                            "PUT",
                            location,
                            "synchronize");

                    Toast.makeText(getApplicationContext(), "ENVIANDO SINCRONIZACIOB", Toast.LENGTH_LONG).show();
                    System.out.println("SE ENVIO");
                }
            }
        };
        //this.map.getOverlays().add(0,myLocationNewOverlay);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //CAMBIAR EL ESTADO DE ACTIVO DEL USUARIO.
            super.onBackPressed();
            WebServiceManager.CallWebServiceOperation(this, Path,
                    "user",
                    "logout",
                    "PUT",
                    loginUser.email,
                    "LogOut");

            Toast.makeText(getApplicationContext(),"Sesión cerrada!, vuelve pronto",Toast.LENGTH_SHORT).show();
            
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            getApplicationContext().deleteDatabase("DbApp");
            /*WebServiceManager.CallWebServiceOperation(this, Path,
                    "maincontroller",
                    "operation",
                    "PUT",
                    "normal",
                    "si");*/

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            floatingActionButton.setVisibility(View.VISIBLE);
            WebServiceManager.CallWebServiceOperation(this, Path,
                    "location",
                    "lastlocation",
                    "PUT",
                    "last location",
                    "lastLocation");

        } else if (id == R.id.nav_gallery) {

            if( map.getOverlayManager().overlays().size() > 0){
                map.getOverlayManager().remove(0);
            }

            floatingActionButton.setVisibility(View.INVISIBLE);

            WebServiceManager.CallWebServiceOperation(this, Path,
                    "user",
                    "getUsers",
                    "PUT",
                    "get users",
                    "getUsers");


        } else if (id == R.id.logOut){

            WebServiceManager.CallWebServiceOperation(this, Path,
                    "user",
                    "logout",
                    "PUT",
                    loginUser.email,
                    "LogOut");
            Toast.makeText(getApplicationContext(),"Sesión cerrada!, vuelve pronto",Toast.LENGTH_SHORT).show();

            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void BuildMarker(String Json,int action) {


        if( map.getOverlayManager().overlays().size() > 0){
            map.getOverlayManager().remove(0);
        }

        System.out.println(map.getOverlayManager().overlays().size());

        Gson gson = new Gson();
        JsonArray jsonArray=null;
        JsonArray jsonEstate=null;
        ArrayList<String> estado = new ArrayList<>();

        if(action==1){
            ArrayList<String> arraylist = new ArrayList<>();
            arraylist = gson.fromJson(Json,ArrayList.class);

            ArrayList<String> geo = new ArrayList<>();
            geo = gson.fromJson(arraylist.get(0),ArrayList.class);

            String a = gson.toJson(geo);
            JsonArray dataGeolocation = gson.fromJson(a,JsonArray.class);

            estado = gson.fromJson(arraylist.get(1),ArrayList.class);
            System.out.println(estado);
            jsonArray = dataGeolocation;

        }else {
            jsonArray = gson.fromJson(Json,JsonArray.class);

        }
        System.out.println(jsonArray.size());
        //Polyline line = new Polyline(map);
        //line.setWidth(10f);
        //line.setColor(Color.RED);
        //List<GeoPoint> pts = new ArrayList<>();
        ArrayList<OverlayItem> puntos = new ArrayList<OverlayItem>();

        for(int i=0; i<(jsonArray.size());i++) {
            System.out.println("Contador->"+i);
            Geolocation geoData = gson.fromJson(jsonArray.get(i), Geolocation.class);

            GeoPoint newCenter = new GeoPoint(geoData.latitude, geoData.longitude);

            long yourmilliseconds = (geoData.time*1000);
            SimpleDateFormat sdf = new SimpleDateFormat("E d MMM yyyy HH:mm");
            Date resultdate = new Date(yourmilliseconds);
            System.out.println(sdf.format(resultdate));

            OverlayItem overlayItem = new OverlayItem("title",geoData.email+"\n\n"+"Fecha de ultima Actualización: "+sdf.format(resultdate),newCenter);
            Drawable drawable;
            if(action==1){
                if (estado.get(i).equals("1")){

                    drawable = this.getResources().getDrawable(R.drawable.activated_round);

                }else if(estado.get(i).equals("0")){

                    drawable = this.getResources().getDrawable(R.drawable.no_activated_round);

                }else{
                    drawable=null;
                }
            }else{

                 drawable = this.getResources().getDrawable(R.drawable.location32);
            //     pts.add(newCenter);
            }
            overlayItem.setMarker(drawable);

            puntos.add(overlayItem);
        }
        //line.setPoints(pts);
        //line.setGeodesic(true);
        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> tap = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemLongPress(int arg0, OverlayItem arg1) {
                return true;
            }

            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                BuildSimpleDialog(item.getSnippet(),"Datos");
                return true;
            }
        };

        ItemizedOverlayWithFocus<OverlayItem> capa = new ItemizedOverlayWithFocus<OverlayItem>(this, puntos,tap);
        capa.setDescriptionBoxPadding(10);
        map.getOverlays().add(0,capa);
        //map.getOverlayManager().add(1,line);
    }

    @Override
    public void onPause(){
        super.onPause();
        map.onPause();
    }


    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage(
                    "We need the GPS location to track U and other permissions, please grant all the permissions...");
            builder.setTitle("Permissions granting");
            builder.setPositiveButton(R.string.accept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(thisActivity,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1227);
                        }
                    });
            AlertDialog dialog=builder.create();
            dialog.show();
            return;
        }else{
            this.gpsManager=new GPSManager(this,this);
            gpsManager.InitLocationManager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1227){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setMessage(
                        "The permissions weren't granted, then the app will be close");
                builder.setTitle("Permissions granting");
                builder.setPositiveButton(R.string.accept,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
            }else{
                this.gpsManager=new GPSManager(this,this);
                gpsManager.InitLocationManager();
            }
        }
    }

    public void BuildSimpleDialog(String message, String title){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(
                message);
        builder.setPositiveButton(R.string.accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    @Override
    public void LocationReceived(double latitude, double longitude) {
        this.latitude=latitude;
        this.longitude=longitude;
        setCenter(latitude,longitude);
        registerLocation(loginUser.email, latitude,longitude);
    }

    public void setCenter(double latitude, double longitude){
        IMapController mapController = map.getController();
        mapController.setZoom(8);
        GeoPoint newCenter = new GeoPoint(latitude, longitude);
        mapController.setCenter(newCenter);
    }

    @Override
    public void GPSManagerException(Exception error) {}

    ///PARA Q
    int year, month, dayOfMonth, hourOfDay, minute;
    long unix1, unix2;
    User userChooser;
    int bucle=1;


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year; this.month=month; this.dayOfMonth=dayOfMonth;
        new TimeDialog().show(getSupportFragmentManager(),"TimeDialog");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hourOfDay=hourOfDay; this.minute=minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);
        long unix = calendar.getTimeInMillis()/1000;
        long unixTotal = calendar.getTimeInMillis();
        Toast.makeText(getApplicationContext(),unix+"",Toast.LENGTH_SHORT).show();

        if(bucle==1){
            bucle++;
            this.unix1=unix;
            DateDialog dateDialog = new DateDialog();
            dateDialog.blocked=true;
            dateDialog.hourBloked=unixTotal;
            dateDialog.show(getSupportFragmentManager(),"Dialoghour2");
            Toast.makeText(getApplicationContext(),"Elegir Fecha y Hora de Finalización",Toast.LENGTH_SHORT).show();

        }else{
            this.unix2=unix;
            if(unix2<unix1){
                Toast.makeText(getApplicationContext(),"La hora de finalizacion es mas pequeña que la inicial, vuelva a intentar",Toast.LENGTH_SHORT).show();
                DateDialog dateDialog = new DateDialog();
                dateDialog.blocked=true;
                dateDialog.hourBloked=unixTotal;
                dateDialog.show(getSupportFragmentManager(),"Dialoghour2");
            }else{
                bucle=1;
                //GetHistory();
                GetHistoryWithServer();
                this.year=0; this.month=0; this.dayOfMonth=0; this.hourOfDay=0; this.minute=0;
                this.unix1=0; this.unix2=0;
                this.userChooser=null;
            }
        }
    }


    public void GetHistoryWithServer(){
        //Toast.makeText(getApplicationContext(),this.email+""+unix1+""+unix2,Toast.LENGTH_SHORT).show();
        WebServiceManager.CallWebServiceOperation(this, Path,
                "location",
                "getHistory",
                "PUT",
                this.email+"-"+unix1+"-"+unix2,
                "getHistory");

    }

    public void GetHistory(){
        List<Geolocation> histoory = INSTANCE.geoDao().getHistory(this.email,unix1, unix2);
        Gson gson2 = new Gson();
        String b = gson2.toJson(histoory);
        BuildSimpleDialog(b,"Geolocations");
        BuildMarker(b,2);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("SE DETUVO");
    }

    @Override
    public void OnClickListener(View view, int position, long id, String user) {
        this.email=user;
        new DateDialog().show(getSupportFragmentManager(),"DateDialog");
        Toast.makeText(getApplicationContext(),"Elegir Fecha y Hora de Inicio",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void WebServiceMessageReceived(final String userState, final String message) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (userState=="signUp"){

                    System.out.println("SignUP");
                    System.out.println("-"+message+"-");

                    if(message.equals("1")){
                        //Toast.makeText(getApplicationContext(),"Voy a registrar en el servidor",Toast.LENGTH_SHORT).show();
                       try {
                           INSTANCE.userDao().insertUser(newUser);
                           Toast.makeText(getApplicationContext(),"Registro Exitoso!",Toast.LENGTH_SHORT).show();
                           //BuildSimpleDialog("Registro","Registro Satisfactorio");
                           finish();
                       }catch(Exception e){
                           Toast.makeText(getApplicationContext(),"Error en el Registro Local!",Toast.LENGTH_SHORT).show();
                           //BuildSimpleDialog("Registro","Registro Satisfactorio en el Servidor. Sin embargo, no pudo guardarse el registro localmente");
                           finish();
                       }

                    }else if(message.equals("0")){
                        System.out.println("Fallo resgistro");
                        Toast.makeText(getApplicationContext(),"Error durante el Registo en el Servidor, Posible email duplicado",Toast.LENGTH_SHORT).show();
                        finish();
                    }else if (message.equals("Down")){
                        Toast.makeText(getApplicationContext(),"Servidor Sin Respuesta",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"No hice nada en el servidor",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                if (userState=="Login"){
                    System.out.println("Login");
                    if(message.equals("1")){
                        System.out.println("Entro y cambio el estado");
                        checkPermissions();
                        thread.start();
                        BuildSimpleDialog("Bienvenidos a TRACKU","TRACKU");
                        //Toast.makeText(getApplicationContext(),"BIENVENIDO A TRACKU",Toast.LENGTH_SHORT).show();
                    }else if(message.equals("0")){
                        System.out.println("Fallo Login");
                        Toast.makeText(getApplicationContext(),"Ocurrio un Error durante el login",Toast.LENGTH_SHORT).show();
                        finish();
                    }else if (message.equals("Down")){
                        //verificacion con Base de datos local
                        if (!userAuth(loginUser.email, loginUser.passwordHash)) {
                            Toast.makeText(getApplicationContext(), "Usuario no encontrado!!", Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            BuildSimpleDialog("Bienvenidos a TRACKU, hubo un error durante el inicio de sesion con el servidor. Sin embargo esto no fue impedimento para iniciar de manera local","TRACKU");
                            //Toast.makeText(getApplicationContext(),"Sesión iniciada con credenciales locales.",Toast.LENGTH_SHORT).show();
                            checkPermissions();
                            thread.start();

                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"No hice nada en el servidor login",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                    if (userState=="SaveLocation"){
                        if (message.equals("0")){

                            INSTANCE.geoDao().insertGeolocation(geo);
                            System.out.println("Fallo en el guardado de geolocalizaciones.");

                        }else if(message.equals("1")){

                            System.out.println("Registro Exitoso");

                        }else if(message.equals("Down")){

                            INSTANCE.geoDao().insertGeolocation(geo);
                            System.out.println("No se tiene respuesta del servidor");
                        }
                    }

                    if (userState=="synchronize"){
                        if (message.equals("0")){

                            System.out.println("Fallo en el guardado de geolocalizaciones.");

                        }else if(message.equals("1")){

                            System.out.println("Registro Exitoso");
                            Toast.makeText(getApplicationContext(),"SINCRONIZACIÓN EXITOSA",Toast.LENGTH_SHORT).show();
                            List<Geolocation> geo = INSTANCE.geoDao().getAllGeolocation();
                            for(int i=0; i<geo.size(); i++){
                                INSTANCE.geoDao().deleteGeolocation(geo.get(i));
                            }

                        }else if(message.equals("Down")){

                            System.out.println("NO SE SINCRONIZARON DATOS.");
                            Toast.makeText(getApplicationContext(),"No se tiene respuesta del servidor",Toast.LENGTH_SHORT).show();
                        }
                    }

                if (userState=="lastLocation"){
                    if (message.equals("0")){
                        System.out.println("Fallo last location");
                        Toast.makeText(getApplicationContext(),"Error al obtener localizaciones",Toast.LENGTH_SHORT).show();
                        finish();
                    }else if(message.equals("Down")) {
                        Toast.makeText(getApplicationContext(), "Servidor Sin Respuesta", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Localizaciones refrescadas...",Toast.LENGTH_SHORT).show();
                        System.out.println(message);
                        BuildMarker(message,1);
                    }
                }

                if (userState=="getUsers"){
                    if (message.equals("0")){
                        System.out.println("Fallo last location");
                        Toast.makeText(getApplicationContext(),"Error al obtener usuarios",Toast.LENGTH_SHORT).show();
                        finish();
                    }else if(message.equals("Down")){
                        Toast.makeText(getApplicationContext(), "Servidor Sin Respuesta", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Cargando usuarios...",Toast.LENGTH_SHORT).show();
                        //BuildMarker(message,1);
                        Gson gson = new Gson();
                        Dialog_Choose_User a = new Dialog_Choose_User();
                        a.userEmail=gson.fromJson(message,List.class);
                        a.show(getSupportFragmentManager(),"Users");
                    }
                }


                if (userState=="getHistory"){
                    if (message.equals("0")){
                        System.out.println("Fallo last location");
                        Toast.makeText(getApplicationContext(),"Error al cargar historicos",Toast.LENGTH_SHORT).show();
                        finish();
                    }else if(message.equals("Down")){
                        Toast.makeText(getApplicationContext(), "Servidor Sin Respuesta", Toast.LENGTH_SHORT).show();
                    }else {
                        //Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),"Cargando historicos...",Toast.LENGTH_SHORT).show();
                        BuildMarker(message,2);
                    }
                }

                if(userState=="Error")
                {
                    Toast.makeText(getApplication(),message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:

                WebServiceManager.CallWebServiceOperation(this, Path,
                        "location",
                        "lastlocation",
                        "PUT",
                        "last location",
                        "lastLocation");
                break;
        }
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
