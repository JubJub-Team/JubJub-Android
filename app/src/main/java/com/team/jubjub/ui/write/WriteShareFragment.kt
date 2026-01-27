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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentWriteShareBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WriteShareFragment : Fragment(R.layout.fragment_write_share) {

    private var _binding: FragmentWriteShareBinding? = null
    private val binding get() = _binding!!

    // 업로드 담당
    private val viewModel: WriteShareViewModel by viewModels()

    // AI 태그 담당
    private val aiViewModel: WriteViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    // AI 태그 결과 임시 저장 (업로드 때 keywords로 합칠 예정)
    private var aiTags: List<String> = emptyList()

    // AI가 본문에 마지막으로 붙인 블록(교체/삭제 용)
    private var lastAiHashtagBlock: String? = null

    /**
     * 현재 게시판 타입
     * - SHARING / LOSTFOUND(=LOST)
     *
     * NOTE:
     * "게시판 선택"이 실제로 어디서 결정되는지에 따라 아래 값 세팅만 맞추면 됨.
     * 예) arguments, sharedViewModel, Activity에서 전달 등
     */
    private var currentPostType: PostType = PostType.SHARING

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult

            // Uri 저장
            selectedImageUri = uri

            // 이미지 접근 가능한지 체크
            if (!canOpenUri(uri)) {
                Toast.makeText(requireContext(), "선택한 이미지에 접근할 수 없음", Toast.LENGTH_SHORT).show()
                selectedImageUri = null
                return@registerForActivityResult
            }

            Toast.makeText(requireContext(), "사진 1장 선택됨", Toast.LENGTH_SHORT).show()

            // 이미지 변경 시: 이전 AI 태그 제거 + aiTags 초기화(덮어쓰기 준비)
            removeAiTagsFromContentIfAny()
            aiTags = emptyList()

            // 선택 즉시 UI 미리보기 + AI 분석 실행
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

                // 게시판 타입에 따라 분석기 달라지도록 postType 전달
                aiViewModel.analyzeImage(bmp, currentPostType)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 게시판 선택값을 arguments로 받는 예시
         * - 호출하는 쪽에서:
         *   WriteShareFragment().apply { arguments = bundleOf("postType" to PostType.SHARING.name)
         */
        val argType = arguments?.getString("postType")?.uppercase()
        currentPostType = when (argType) {
            PostType.LOST.name -> PostType.LOST
            PostType.SHARING.name -> PostType.SHARING
            else -> PostType.SHARING
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWriteShareBinding.bind(view)

        setupBack()
        setupTopActions()
        setupPhotoPreviewActions()
        setupCategory()
        setupCondition()
        setupCount()
        setupMethod()
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
        inputType: Int,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            setText(currentValue)
            hint = title
            this.inputType = inputType
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
       공통 선택 다이얼로그
    ------------------------ */
    private fun showSelectDialog(
        title: String,
        items: Array<String>,
        onSelect: (String) -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(items) { _, which ->
                onSelect(items[which])
            }
            .show()
    }

    /* ------------------------
       공통 멀티 선택 다이얼로그
    ------------------------ */
    private fun showMultiSelectDialog(
        title: String,
        items: Array<String>,
        currentText: String,
        onConfirm: (List<String>) -> Unit
    ) {
        val currentSelected = currentText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        val checked = BooleanArray(items.size) { idx -> items[idx] in currentSelected }
        val selected = currentSelected.toMutableSet()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                if (isChecked) selected.add(items[which]) else selected.remove(items[which])
            }
            .setPositiveButton("확인") { _, _ ->
                onConfirm(selected.toList())
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /* ------------------------
       사진 / 장소
    ------------------------ */
    private fun setupTopActions() {
        binding.layoutAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.layoutAddLocation.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
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
       카테고리
    ------------------------ */
    private fun setupCategory() {
        val categories = arrayOf("의류", "식품", "전자기기", "도서", "기타")

        binding.layoutCategory.setOnClickListener {
            showSelectDialog("카테고리 선택", categories) {
                binding.tvCategory.text = it
            }
        }
    }

    /* ------------------------
       물품 상태
    ------------------------ */
    private fun setupCondition() {
        val conditions = arrayOf(
            "새상품",
            "사용감 없음",
            "사용감 적음",
            "사용감 많음",
            "고장 및 파손"
        )

        binding.layoutCondition.setOnClickListener {
            showSelectDialog("물품 상태", conditions) {
                binding.tvCondition.text = it
            }
        }
    }

    /* ------------------------
       수량
    ------------------------ */
    private fun setupCount() {
        binding.layoutCount.setOnClickListener {
            showInputDialog(
                title = "수량",
                currentValue = binding.tvCount.text.toString(),
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            ) {
                binding.tvCount.text = it
            }
        }
    }

    /* ------------------------
       나눔 방법 (복수선택)
    ------------------------ */
    private fun setupMethod() {
        val methods = arrayOf("직거래", "택배", "사물함")

        binding.layoutMethod.setOnClickListener {
            showMultiSelectDialog(
                title = "나눔 방법(복수 선택 가능)",
                items = methods,
                currentText = binding.tvMethod.text?.toString().orEmpty()
            ) { selected ->
                binding.tvMethod.text = selected.joinToString(", ")
            }
        }
    }

    /* ------------------------
       완료 버튼 → 업로드
       - 해시태그 + AI태그 합쳐서 keywords로 전송
       생성된 태그는 Post.keywords로 저장됨
    ------------------------ */
    private fun setupDone() {
        binding.tvDone.setOnClickListener {
            val title = binding.etTitle.text?.toString().orEmpty()
            val content = binding.etContent.text?.toString().orEmpty()

            val imageUri = selectedImageUri
            if (imageUri != null && !canOpenUri(imageUri)) {
                Toast.makeText(requireContext(), "선택한 이미지에 접근할 수 없음", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryText = binding.tvCategory.text?.toString().orEmpty()
            val conditionText = binding.tvCondition.text?.toString().orEmpty()
            val countText = binding.tvCount.text?.toString().orEmpty()
            val methodText = binding.tvMethod.text?.toString().orEmpty()
            val hopeLocation = binding.tvLocation.text?.toString().orEmpty()

            val quantity: Int? = countText.trim().toIntOrNull()

            val methodTexts = methodText
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val hashtagKeywords = extractHashtags("$title $content")

            // 여기에 aiTags가 합쳐져서 백엔드 keywords로 저장됨
            val mergedKeywords = (hashtagKeywords + aiTags)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            // 현재 화면이 "나눔"이라면 기존 업로드 함수 그대로
            // 만약 LOST 업로드도 한 프래그먼트에서 처리하려면 viewModel 분기 필요
            viewModel.uploadSharingPostWithImage(
                title = title,
                content = content,
                categoryText = categoryText,
                conditionText = conditionText,
                quantity = quantity,
                methodTexts = methodTexts,
                hopeLocation = hopeLocation,
                imageUri = imageUri,
                keywords = mergedKeywords
            )
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { e ->
                when (e) {
                    is WriteShareViewModel.Event.UploadSuccess -> {
                        Toast.makeText(requireContext(), "업로드 완료", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    is WriteShareViewModel.Event.Validation -> {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                    is WriteShareViewModel.Event.Fail -> {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * AI 결과 관찰
     * - "이미지 바꾸면 태그 덮어쓰기"를 위해,
     *   새 태그가 오면 기존 AI 블록 제거 후 새 블록으로 교체
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

            // 기존 AI 블록이 있으면 먼저 제거(덮어쓰기)
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

            // 새 블록으로 갱신
            lastAiHashtagBlock = toAppend

            edit.append(toAppend)
            edit.setSelection(edit.text?.length ?: 0)
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
                } else decoded
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(resolver, uri)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
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

    companion object {
        // arguments로 postType 넘겨주는 헬퍼
        fun newInstance(postType: PostType): WriteShareFragment {
            return WriteShareFragment().apply {
                arguments = bundleOf("postType" to postType.name)
            }
        }
    }
}
