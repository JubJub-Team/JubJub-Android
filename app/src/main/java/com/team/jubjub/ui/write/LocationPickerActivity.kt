package com.team.jubjub.ui.write

import android.os.Build
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.team.jubjub.R
import com.team.jubjub.databinding.ActivityLocationPickerBinding
import java.util.Locale

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private var naverMap: NaverMap? = null
    private val marker = Marker()
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. MapFragment 얻어오기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        // 2. 비동기로 NaverMap 객체 요청
        mapFragment.getMapAsync(this)

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
        this.naverMap = map

        // 초기 위치 설정 (서울시청)
        val init = LatLng(37.5666102, 126.9783881)
        map.moveCamera(CameraUpdate.scrollTo(init))

        // 지도 클릭 리스너
        map.setOnMapClickListener { _, latLng ->
            selectedLatLng = latLng
            marker.position = latLng
            marker.map = map

            // 주소 변환 호출
            reverseGeocode(latLng)
        }
    }

    private fun reverseGeocode(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.KOREA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                val address = addresses.firstOrNull()?.getAddressLine(0)
                selectedAddress = address

                // 필요 시 메인 스레드에서 UI 업데이트 가능
                runOnUiThread {
                    if (address != null) {
                        // Toast.makeText(this, "선택된 주소: $address", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                selectedAddress = results?.firstOrNull()?.getAddressLine(0)
            } catch (e: Exception) {
                selectedAddress = null
            }
        }
    }
}
