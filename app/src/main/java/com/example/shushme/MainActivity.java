package com.example.shushme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;

    private static final int PLACE_PICKER_REQUEST = 100;


    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        GoogleApiClient client = new GoogleApiClient.Builder(this)
                                    .addConnectionCallbacks(this)
                                    .addOnConnectionFailedListener(this)
                                    .addApi(LocationServices.API)
                                    .addApi(Places.GEO_DATA_API)
                                    .enableAutoManage(this,this)
                                    .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "API Client Connection Successful!");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "API Client Connection Suspended!");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "API Client Connection Failed!");
    }

    public void onAddPlaceButton(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }else{
            Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG).show();


            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent i = null;
                i = builder.build(this);
                Log.d(TAG,"startActivityForResult i: "+i);
                startActivityForResult(i,PLACE_PICKER_REQUEST);
                Log.d(TAG,"startActivityForResult called");
            } catch (GooglePlayServicesRepairableException e) {
                Log.d(TAG,"GooglePlayServicesRepairableException e: "+e);
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.d(TAG,"GooglePlayServicesNotAvailableException e: "+e);
                e.printStackTrace();
            } catch (Exception e){
                Log.d(TAG,"Exception e: "+e);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult requestCode: "+requestCode+" resultCode: "+resultCode+" data: "+data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            Log.d(TAG,"onActivityResult place: ");
            Place place = PlacePicker.getPlace(this,data);

            if (place==null){
                Log.d(TAG,"No place added");
                return;
            }

            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID,placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckBox locationPermissions = (CheckBox) findViewById(R.id.location_permission_checkbox);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            locationPermissions.setChecked(false);
        }else{
            locationPermissions.setChecked(true);
            locationPermissions.setEnabled(false);
        }
    }

    public void onLocationPermissionClicked(View view){
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_FINE_LOCATION);
    }
}
