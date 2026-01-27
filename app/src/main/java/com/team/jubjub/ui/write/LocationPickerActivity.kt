package com.team.jubjub.ui.write

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.team.jubjub.databinding.ActivityLocationPickerBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import java.util.Locale

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null

    private val marker = Marker()
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.btnConfirm.setOnClickListener {
            val latLng = selectedLatLng
            if (latLng == null) {
                Toast.makeText(this, "지도를 눌러 위치를 선택해줘!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = Intent().apply {
                putExtra("lat", latLng.latitude)
                putExtra("lng", latLng.longitude)
                putExtra("address", selectedAddress.orEmpty())
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 초기 위치(원하면 학교/기본 중심좌표로)
        val init = LatLng(37.5666102, 126.9783881) // 서울시청
        map.moveCamera(CameraUpdate.scrollTo(init))

        map.setOnMapClickListener { _, latLng ->
            selectedLatLng = latLng
            marker.position = latLng
            marker.map = map

            // 주소는 선택 사항: Android Geocoder로 간단히 변환(기기/환경 따라 실패 가능)
            selectedAddress = reverseGeocode(latLng)
        }
    }

    private fun reverseGeocode(latLng: LatLng): String? {
        return try {
            val geocoder = Geocoder(this, Locale.KOREA)
            val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            results?.firstOrNull()?.getAddressLine(0)
        } catch (t: Throwable) {
            null
        }
    }

    // MapView 생명주기 연결
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
