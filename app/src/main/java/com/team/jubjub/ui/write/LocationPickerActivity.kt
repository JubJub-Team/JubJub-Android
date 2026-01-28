package com.team.jubjub.ui.write

import android.os.Build
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // MapFragment 얻어오기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        // 비동기로 NaverMap 객체 요청
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

        // Intent로 학교 이름 받기
        val schoolName = intent.getStringExtra("school")

        // 학교 이름으로 위치 검색 후 이동 (비동기 처리)
        if (!schoolName.isNullOrBlank()) {
            searchLocationFromName(schoolName)
        } else {
            // 학교 이름이 없으면 기본값(서울시청) 이동
            moveToLocation(LatLng(37.5666102, 126.9783881))
        }

        // 지도 클릭 리스너
        map.setOnMapClickListener { _, latLng ->
            selectedLatLng = latLng
            marker.position = latLng
            marker.map = map
            reverseGeocode(latLng)
        }
    }

    private fun searchLocationFromName(name: String) {
        lifecycleScope.launch(Dispatchers.IO) { // 네트워크 작업은 IO 스레드에서
            try {
                val geocoder = Geocoder(this@LocationPickerActivity, Locale.KOREA)

                // 이름으로 주소 목록 가져오기 (최대 1개)
                // Android 13(Tiramisu) 이상과 이하 버전에 따라 방식이 조금 다르지만,
                // 간단한 구현을 위해 동기식 메서드를 Coroutine 안에서 호출
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(name, 1)

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    // UI 업데이트는 Main 스레드에서
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LocationPickerActivity, "$name 위치로 이동합니다.", Toast.LENGTH_SHORT).show()
                        moveToLocation(latLng)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LocationPickerActivity, "학교 위치를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                        // 못 찾으면 기본값 이동
                        moveToLocation(LatLng(37.5666102, 126.9783881))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LocationPickerActivity, "위치 검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 카메라 이동 함수 분리
    private fun moveToLocation(latLng: LatLng) {
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(latLng, 16.0)
        naverMap?.moveCamera(cameraUpdate)
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
