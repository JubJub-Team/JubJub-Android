package com.team.jubjub.ui.write

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.util.ImageAnalyzer
import kotlinx.coroutines.launch

class WriteViewModel(application: Application) : AndroidViewModel(application) {
    private val imageAnalyzer by lazy {
        ImageAnalyzer(getApplication<Application>().applicationContext)
    }

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun analyzeImage(bitmap: Bitmap, type: PostType) {
        if (_isLoading.value == true) return

        _isLoading.value = true
        Log.d("JubJub_AI", "분석 요청: $type 모드")

        viewModelScope.launch {
            try {
                imageAnalyzer.analyze(bitmap, type) { resultList ->
                    _isLoading.value = false
                    _tags.value = resultList
                    Log.d("JubJub_AI", "최종 결과: $resultList")
                }
            } catch (e: Exception) {
                Log.e("JubJub_AI", "오류 발생: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // initialized 여부 확인 후 닫음
        if (isAnalyzerInitialized()) {
            imageAnalyzer.close()
            Log.d("JubJub_AI", "ViewModel 종료 & AI 리소스 해제")
        }
    }

    // lazy 객체가 초기화되었는지 확인
    private fun isAnalyzerInitialized(): Boolean {
        return try {
            imageAnalyzer.toString()
            true
        } catch (e: UninitializedPropertyAccessException) {
            false
        } catch (e: Exception) {
            true
        }
    }
}