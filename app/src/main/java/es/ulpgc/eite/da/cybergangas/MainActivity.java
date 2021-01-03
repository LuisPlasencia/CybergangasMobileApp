package es.ulpgc.eite.da.cybergangas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.TextView;

import com.google.android.gms.common.api.Response;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    // SENSOR GPS
    TextView tvlongitud = null;
    TextView tvlatitud = null;
    // GPS
    LocationManager lm = null;
    Location location = null;

    //NFC
    long idOrden = 0;

    Button actualize = null;


    //Intialize attributes
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    final static String TAG = "nfc_test";


    public static final String MIME_TEXT_PLAIN = "text/plain";

    private static final String URL_JSON = "/CheckFromNfc/";



    TextView tvLongitud;
    TextView tvLatitud;
    TextView tvMensaje;
    TextView readTag;

    double longitude = 0;
    double latitude = 0;

    LocationManager locationManager;

    //Firebase
    DatabaseReference reference;


    // GPS
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                tvLongitud.setText(String.valueOf((double)Math.round(longitude*1000)/1000));
                tvLatitud.setText(String.valueOf((double)Math.round(latitude*1000)/1000));
            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvLongitud = (TextView) findViewById(R.id.tvSensoresLongitud);
        tvLatitud = (TextView) findViewById(R.id.tvSensoresLatitud);
        tvMensaje = (TextView) findViewById(R.id.tvMensaje);
        actualize = findViewById(R.id.btn_actualizar);
        actualize.setEnabled(false);

        tvMensaje = (TextView) findViewById(R.id.tvMensaje);

//        if(mAdapter == null){
//            tvMensaje.setText(R.string.aviso_lecturaBad);
//            return;
//        }
//        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        //GPS
        //Con esto pedimos permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                tvLongitud.setText(String.valueOf((double)Math.round(longitude*1000)/1000));
                tvLatitud.setText(String.valueOf((double)Math.round(latitude*1000)/1000));
                Log.d("comprueba", "Longitud: " + String.valueOf(longitude));
                Log.d("comprueba",  "Latitud: " + String.valueOf(latitude));
            }



            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }


        //NFC

        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null){
            Toast.makeText(this,"El dispositivo no soporta NFC.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.
        //PendingIntent.getActivity(Context,requestcode(identifier for
        //                           intent),intent,int)
        pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);


//        if (mNfcAdapter == null) {
//            // Es necesario nfc
//            Toast.makeText(this, "El dispositivo no soporta NFC.", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//
//        }
//
//        if (!mNfcAdapter.isEnabled()) {
//            tvMensaje.setText("NFC is disabled.");
//            Toast.makeText(this, "Activa NFC para continuar.", Toast.LENGTH_LONG).show();
//
//        }



        actualize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (idOrden == 0) {
                    tvMensaje.setText(R.string.aviso_nolectura);
                }else if(latitude == 0 || longitude == 0){
                    tvMensaje.setText(R.string.aviso_noGPS);
                } else{
                    //FIREBASE
                    reference = FirebaseDatabase.getInstance().getReference("Orden");
                    reference.child(String.valueOf(idOrden)).child("latitud").setValue(latitude);
                    reference.child(String.valueOf(idOrden)).child("longitud").setValue(longitude);
                }
            }

        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        //nfcAdapter.enableForegroundDispatch(context,pendingIntent,
        //                                    intentFilterArray,
        //                                    techListsArray)
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            byte[] payload = detectTagData(tag).getBytes();
        }
    }
    private String detectTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

        readTag = (TextView) findViewById(R.id.textView7);
        readTag.setText(String.valueOf(toDec(id)));
        actualize.setEnabled(true);
        tvMensaje.setText(R.string.success_message);
        this.idOrden = toDec(id);

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }





        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.v("test",sb.toString());
        return sb.toString();
    }
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }






}