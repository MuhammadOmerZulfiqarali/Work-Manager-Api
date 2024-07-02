package com.example.work_manager_api

import android.Manifest
import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.work_manager_api.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    lateinit var databinding: ActivityMainBinding

    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        setLocationListener()
        //setSupportActionBar(databinding.toolbar)

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

        try {
            if (isWorkScheduled(WorkManager.getInstance().getWorkInfosByTag(TAG).get())) {
                databinding.appCompatButtonStart.text = getString(R.string.button_text_stop)
            } else {
                databinding.appCompatButtonStart.text = getString(R.string.button_text_start)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        databinding.appCompatButtonStart.setOnClickListener {
            if (databinding.appCompatButtonStart.text.toString()
                    .equals(getString(R.string.button_text_start), true)
            ) {
                // START Worker
                val periodicWork =
                    PeriodicWorkRequest.Builder(MyWorker::class.java, 3, TimeUnit.HOURS)
                        .addTag(TAG)
                        .build()
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    "Location",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWork)
                Toast.makeText(
                    this@MainActivity,
                    "Location Worker Started : " + periodicWork.id,
                    Toast.LENGTH_SHORT).show()
                databinding.appCompatButtonStart.text = getString(R.string.button_text_stop)
                //setLocationListener()
            } else {
                WorkManager.getInstance().cancelAllWorkByTag(TAG)
                databinding.appCompatButtonStart.text = getString(R.string.button_text_start)
                //setLocationListener()
            }
        }
    }

    private fun isWorkScheduled(workInfos: List<WorkInfo>?): Boolean {
        var running = true
        if (workInfos == null || workInfos.isEmpty()) return false
        for (workStatus in workInfos) {
            running =
                workStatus.state == WorkInfo.State.RUNNING || workStatus.state == WorkInfo.State.ENQUEUED
        }
        return running
    }

    private fun checkLocationPermission(): Boolean {
        val result3 = ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        val result4 = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        return result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (coarseLocation && fineLocation) Toast.makeText(
                    this,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show() else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        private const val TAG = "LocationUpdate"
    }

    private fun setLocationListener(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(500).setFastestInterval(100)
            .setPriority(android.location.LocationRequest.QUALITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

            Looper.getMainLooper()

    }
}