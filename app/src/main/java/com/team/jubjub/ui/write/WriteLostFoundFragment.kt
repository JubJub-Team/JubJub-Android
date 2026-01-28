package com.team.jubjub.ui.write

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentWriteLostFoundBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@AndroidEntryPoint
class WriteLostFoundFragment : Fragment(R.layout.fragment_write_lost_found) {

    private var _binding: FragmentWriteLostFoundBinding? = null
    private val binding get() = _binding!!

    // 업로드 담당
    private val viewModel: WriteLostFoundViewModel by viewModels()

    // AI 태그 담당
    private val aiViewModel: WriteViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    // AI 태그 결과 임시 저장 (업로드 때 keywords로 합칠 예정)
    private var aiTags: List<String> = emptyList()

    // AI가 본문에 마지막으로 붙인 블록(교체/삭제용)
    private var lastAiHashtagBlock: String? = null

    // 이 프래그먼트는 LOST 전용
    private val postType: PostType = PostType.LOST

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult

            // Uri 저장 + 접근 가능 체크
            if (!canOpenUri(uri)) {
                Toast.makeText(requireContext(), "선택한 이미지에 접근할 수 없음", Toast.LENGTH_SHORT).show()
                selectedImageUri = null
                return@registerForActivityResult
            }

            selectedImageUri = uri
            Toast.makeText(requireContext(), "사진 1장 선택됨", Toast.LENGTH_SHORT).show()

            // 이미지 교체 시: 이전 AI 태그 제거 + aiTags 초기화(덮어쓰기 준비)
            removeAiTagsFromContentIfAny()
            aiTags = emptyList()

            viewLifecycleOwner.lifecycleScope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    uriToBitmap(uri, maxSize = 1024)
                }

                if (bmp == null) {
                    Toast.makeText(requireContext(), "이미지 로드 실패", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 미리보기 표시
                showSelectedPhotoPreview(bmp)

                // 게시판 타입(LOST)으로 분석
                aiViewModel.analyzeImage(bmp, postType)
            }
        }

    private val pickLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != android.app.Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            val lat = data.getDoubleExtra("lat", 0.0)
            val lng = data.getDoubleExtra("lng", 0.0)
            val address = data.getStringExtra("address").orEmpty()

            binding.tvLocation.text = if (address.isNotBlank()) address else "$lat, $lng"
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWriteLostFoundBinding.bind(view)

        setupBack()
        setupInputFields()
        setupTopActions()
        setupPhotoPreviewActions() // 추가
        setupDone()
        observeEvents()
        observeAi()
    }

    /* ------------------------
       뒤로가기
    ------------------------ */
    private fun setupBack() {
        binding.icBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /* ------------------------
       공통 입력 다이얼로그
    ------------------------ */
    private fun showInputDialog(
        title: String,
        currentValue: String,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            setText(currentValue)
            hint = title
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotBlank()) onConfirm(input)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /* ------------------------
       추가 정보 입력 영역
    ------------------------ */
    private fun setupInputFields() {

        binding.layoutFoundPlace.setOnClickListener {
            showInputDialog(
                title = "발견 장소",
                currentValue = binding.tvFoundPlace.text.toString()
            ) { binding.tvFoundPlace.text = it }
        }

        binding.layoutPlaceDetail.setOnClickListener {
            showInputDialog(
                title = "상세 장소",
                currentValue = binding.tvPlaceDetail.text.toString()
            ) { binding.tvPlaceDetail.text = it }
        }

        binding.layoutFoundDatetime.setOnClickListener {
            showInputDialog(
                title = "발견 일시(예: 20260127 또는 2026-01-27)",
                currentValue = binding.tvFoundDatetime.text.toString()
            ) { raw ->
                val normalized = normalizeDateToYYYYMMDD(raw)
                if (normalized == null) {
                    Toast.makeText(requireContext(), "날짜는 8자리(YYYYMMDD)로 입력", Toast.LENGTH_SHORT).show()
                    return@showInputDialog
                }
                binding.tvFoundDatetime.text = normalized
            }
        }

        binding.layoutStoragePlace.setOnClickListener {
            showInputDialog(
                title = "위탁 장소",
                currentValue = binding.tvStoragePlace.text.toString()
            ) { binding.tvStoragePlace.text = it }
        }
    }

    /* ------------------------
       사진 / 장소 상단 액션
    ------------------------ */
    private fun setupTopActions() {
        binding.layoutAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.layoutAddLocation.setOnClickListener {
            // ViewModel의 값이 없으면 "서울시청" 사용
            val schoolName = viewModel.userSchool.value ?: "서울시청"

            val intent = Intent(requireContext(), LocationPickerActivity::class.java).apply {
                putExtra("school", schoolName)
            }
            pickLocationLauncher.launch(intent)
        }
    }

    /* ------------------------
       미리보기 클릭/삭제
    ------------------------ */
    private fun setupPhotoPreviewActions() {
        // 미리보기 클릭 → 크게 보기
        binding.ivPhotoPreview.setOnClickListener {
            val uri = selectedImageUri ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching { startActivity(intent) }
                .onFailure {
                    Toast.makeText(requireContext(), "이미지를 열 수 없어요.", Toast.LENGTH_SHORT).show()
                }
        }

        // X 버튼 → 사진 제거
        binding.btnRemovePhoto.setOnClickListener {
            clearSelectedPhoto(removeAiTagsFromContent = true)
        }
    }

    private fun showSelectedPhotoPreview(bmp: Bitmap) {
        binding.layoutPhotoPreview.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageBitmap(bmp)
    }

    private fun clearSelectedPhoto(removeAiTagsFromContent: Boolean) {
        selectedImageUri = null

        // UI 초기화
        binding.ivPhotoPreview.setImageDrawable(null)
        binding.layoutPhotoPreview.visibility = View.GONE

        // AI 태그 상태 초기화(업로드 keywords에서도 빠지게)
        aiTags = emptyList()

        if (removeAiTagsFromContent) {
            removeAiTagsFromContentIfAny()
        }

        Toast.makeText(requireContext(), "사진이 제거됐어요.", Toast.LENGTH_SHORT).show()
    }

    private fun removeAiTagsFromContentIfAny() {
        val block = lastAiHashtagBlock ?: return
        val cur = binding.etContent.text?.toString().orEmpty()
        val newText = cur.replace(block, "").trimEnd()
        binding.etContent.setText(newText)
        binding.etContent.setSelection(binding.etContent.text?.length ?: 0)
        lastAiHashtagBlock = null
    }

    /* ------------------------
       완료 버튼 → 업로드
       - 해시태그 + AI태그 합쳐서 keywords로 전송
       생성된 태그는 Post.keywords로 저장됨
    ------------------------ */
    private fun setupDone() {
        binding.tvDone.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            // 날짜 정규화 → Timestamp
            val rawDate = binding.tvFoundDatetime.text?.toString().orEmpty().trim()
            val normalizedDate = normalizeDateToYYYYMMDD(rawDate)

            if (rawDate.isNotEmpty() && normalizedDate == null) {
                Toast.makeText(requireContext(), "발견 일시는 20260127 또는 2026-01-27 형식으로 입력", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foundDateTimestamp: Timestamp? = normalizedDate?.let {
                yyyymmddToTimestampOrNull(it)
            }

            if (normalizedDate != null && foundDateTimestamp == null) {
                Toast.makeText(requireContext(), "존재하지 않는 날짜야(예: 20260230)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이미지 Uri 유효성 체크
            val imageUri = selectedImageUri
            if (imageUri != null && !canOpenUri(imageUri)) {
                Toast.makeText(requireContext(), "선택한 이미지에 접근할 수 없어. 다시 선택해줘!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashtagKeywords = extractHashtags("$title $content")

            // aiTags가 mergedKeywords에 합쳐져서 백엔드 keywords로 저장됨
            val mergedKeywords = (hashtagKeywords + aiTags)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            viewModel.uploadLostPostWithImage(
                title = title,
                content = content,
                foundLocation = binding.tvFoundPlace.text?.toString(),
                foundDetailLocation = binding.tvPlaceDetail.text?.toString(),
                foundDate = foundDateTimestamp,
                storageLocation = binding.tvStoragePlace.text?.toString(),
                imageUri = imageUri,
                keywords = mergedKeywords
            )
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { e ->
                when (e) {
                    is WriteLostFoundViewModel.Event.UploadSuccess -> {
                        Toast.makeText(requireContext(), "업로드 완료", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    is WriteLostFoundViewModel.Event.Validation -> {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                    is WriteLostFoundViewModel.Event.Fail -> {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * AI 결과 관찰
     * - 새 태그가 오면 기존 AI 블록 제거 후 새 블록 붙임
     */
    private fun observeAi() {
        aiViewModel.tags.observe(viewLifecycleOwner) { list ->
            aiTags = list ?: emptyList()
            if (aiTags.isEmpty()) return@observe

            val hashtags = aiTags
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { if (it.startsWith("#")) it else "#$it" }
                .distinct()
                .joinToString(" ")

            val edit = binding.etContent
            val current = edit.text?.toString().orEmpty()

            //기존 AI 블록 제거(덮어쓰기)
            lastAiHashtagBlock?.let { block ->
                if (current.contains(block)) {
                    val cleaned = current.replace(block, "").trimEnd()
                    edit.setText(cleaned)
                    edit.setSelection(edit.text?.length ?: 0)
                }
            }

            val afterClean = edit.text?.toString().orEmpty()

            val toAppend = buildString {
                if (afterClean.isNotBlank() && !afterClean.endsWith("\n")) append("\n")
                if (afterClean.isNotBlank()) append("\n")
                append(hashtags)
            }

            // 새 블록 저장
            lastAiHashtagBlock = toAppend

            edit.append(toAppend)
            edit.setSelection(edit.text?.length ?: 0)
        }

        aiViewModel.isLoading.observe(viewLifecycleOwner) {
            // 원하면 로딩 UI 처리
        }
    }

    private fun extractHashtags(text: String): List<String> =
        Regex("#([\\p{L}0-9_]+)")
            .findAll(text)
            .map { it.groupValues[1] }
            .distinct()
            .toList()

    /**
     * Uri -> Bitmap 변환 (OOM 방지용 축소 디코딩)
     */
    private fun uriToBitmap(uri: Uri, maxSize: Int = 1024): Bitmap? {
        return try {
            val resolver = requireContext().contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(resolver, uri)
                val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val w = info.size.width
                    val h = info.size.height

                    val scale = (maxOf(w, h).toFloat() / maxSize.toFloat())
                    if (scale > 1f) {
                        decoder.setTargetSize(
                            (w / scale).toInt().coerceAtLeast(1),
                            (h / scale).toInt().coerceAtLeast(1)
                        )
                    }

                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }

                if (decoded.config != Bitmap.Config.ARGB_8888) {
                    decoded.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    decoded
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(resolver, uri)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    // 숫자만 남겨 YYYYMMDD로 정규화
    private fun normalizeDateToYYYYMMDD(input: String): String? {
        val digits = input.filter { it.isDigit() }
        return when (digits.length) {
            8 -> digits
            6 -> "20$digits" // YYMMDD -> 20YYMMDD 가정
            else -> null
        }
    }

    // YYYYMMDD -> Timestamp(00:00:00). 존재하지 않는 날짜면 null
    private fun yyyymmddToTimestampOrNull(yyyymmdd: String): Timestamp? {
        return try {
            if (yyyymmdd.length != 8) return null
            val y = yyyymmdd.substring(0, 4).toInt()
            val m = yyyymmdd.substring(4, 6).toInt()
            val d = yyyymmdd.substring(6, 8).toInt()

            if (m !in 1..12) return null
            if (d !in 1..31) return null

            val cal = Calendar.getInstance().apply {
                isLenient = false
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            Timestamp(cal.time)
        } catch (t: Throwable) {
            null
        }
    }

    private fun canOpenUri(uri: Uri): Boolean {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (t: Throwable) {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
