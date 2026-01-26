package com.team.jubjub.ui.write

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentWriteBinding

class WriteFragment : Fragment() {
    private var _binding: FragmentWriteBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: WriteViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }

            // 이미지 로드
            val testBitmap = BitmapFactory.decodeResource(resources, R.drawable.test, options)

            // null 체크
            if (testBitmap != null) {
                Log.d("JubJub_AI", "이미지 로드 성공: ${testBitmap.width}x${testBitmap.height}")
                viewModel.analyzeImage(testBitmap, PostType.LOST)
            } else {
                Log.e("JubJub_AI", "이미지 로드 실패")
            }

        } catch (e: Exception) {
            Log.e("JubJub_AI", "Fragment 에러 발생: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}