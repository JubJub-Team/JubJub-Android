package com.team.jubjub.ui.mypage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.team.jubjub.R
import com.team.jubjub.data.model.User
import com.team.jubjub.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    // 중복확인 완료 플래그
    private var isIdChecked = false
    private var isNicknameChecked = false
    private var isEmailChecked = false
    private var isPhoneChecked = false

    // 변경 여부 판단용 최초 로딩값
    private var originalCustomId: String = ""
    private var originalNickname: String = ""
    private var originalEmail: String = ""
    private var originalPhone: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        Log.d("ProfileFragment", "onViewCreated() called") // 무조건 찍힘

        setListeners()
        observeViewModel()

        val userId = getCurrentUserId()
        Log.d("ProfileFragment", "current uid = $userId") // uid 확인

        if (userId.isNotBlank()) {
            viewModel.loadProfile(userId)
        } else {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfileSave.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            if (!validateDupChecksIfChanged()) return@setOnClickListener

            val prev = viewModel.user.value
            val input = buildUserFromInputs(userId)

            // 기존 값 유지 + 입력값만 반영(빈값 덮어쓰기 방지)
            val toSave = (prev ?: User(userId = userId)).copy(
                customId = input.customId,
                nickname = input.nickname,
                email = input.email,
                phone = input.phone,
                name = input.name,
                school = input.school,
                birthDate = input.birthDate
            )

            viewModel.saveProfile(toSave)
        }

        binding.btnProfileCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 프로필 수신
                launch {
                    viewModel.user.collect { user ->
                        Log.d("ProfileFragment", "collect user = $user") // ✅ 여기 꼭 찍혀야 함
                        if (user == null) return@collect

                        fillProfileFromUser(user)
                        cacheOriginalsFromUser(user)

                        // 처음 로딩된 값은 기존 값이므로 중복확인 OK 처리
                        isIdChecked = true
                        isNicknameChecked = true
                        isEmailChecked = true
                        isPhoneChecked = true
                    }
                }

                // 중복확인 결과
                launch {
                    viewModel.idAvailable.collect { available ->
                        if (available == null) return@collect
                        if (available) {
                            showMessage(binding.tvProfileUsernameMsg, "*사용 가능한 아이디입니다.", true)
                            isIdChecked = true
                        } else {
                            showMessage(binding.tvProfileUsernameMsg, "*이미 사용 중인 아이디입니다.", false)
                            isIdChecked = false
                        }
                    }
                }

                launch {
                    viewModel.nicknameAvailable.collect { available ->
                        if (available == null) return@collect
                        if (available) {
                            showMessage(binding.tvProfileNicknameMsg, "*사용 가능한 닉네임입니다.", true)
                            isNicknameChecked = true
                        } else {
                            showMessage(binding.tvProfileNicknameMsg, "*이미 사용 중인 닉네임입니다.", false)
                            isNicknameChecked = false
                        }
                    }
                }

                launch {
                    viewModel.emailAvailable.collect { available ->
                        if (available == null) return@collect
                        if (available) {
                            showMessage(binding.tvProfileEmailMsg, "*사용 가능한 이메일입니다.", true)
                            isEmailChecked = true
                        } else {
                            showMessage(binding.tvProfileEmailMsg, "*이미 사용 중인 이메일입니다.", false)
                            isEmailChecked = false
                        }
                    }
                }

                launch {
                    viewModel.phoneAvailable.collect { available ->
                        if (available == null) return@collect
                        if (available) {
                            showMessage(binding.tvProfilePhoneMsg, "*사용 가능한 전화번호입니다.", true)
                            isPhoneChecked = true
                        } else {
                            showMessage(binding.tvProfilePhoneMsg, "*이미 사용 중인 전화번호입니다.", false)
                            isPhoneChecked = false
                        }
                    }
                }

                // 메시지(저장 성공/실패 등) -> 토스트 + 저장 성공이면 뒤로가기
                launch {
                    viewModel.message.collect { msg ->
                        Log.d("ProfileFragment", "message = $msg")
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                        if (msg.contains("저장 완료")) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        }
    }

    /**
     * 입력 변경 시 중복확인 무효화 + 메시지 숨김
     */
    private fun setListeners() {
        binding.etProfileUsername.doAfterTextChanged {
            isIdChecked = false
            binding.tvProfileUsernameMsg.visibility = View.GONE
        }

        binding.etProfilePassword.doAfterTextChanged { validatePassword() }
        binding.etProfilePasswordConfirm.doAfterTextChanged { validatePasswordCheck() }

        binding.etProfileNickname.doAfterTextChanged {
            isNicknameChecked = false
            binding.tvProfileNicknameMsg.visibility = View.GONE
        }

        binding.etProfileEmail.doAfterTextChanged {
            isEmailChecked = false
            binding.tvProfileEmailMsg.visibility = View.GONE
        }

        binding.etProfilePhone.doAfterTextChanged {
            isPhoneChecked = false
            binding.tvProfilePhoneMsg.visibility = View.GONE
        }

        binding.btnProfileUsernameCheck.setOnClickListener { checkIdDuplicate() }
        binding.btnProfileNicknameCheck.setOnClickListener { checkNicknameDuplicate() }
        binding.btnProfileEmailCheck.setOnClickListener { checkEmailDuplicate() }
        binding.btnProfilePhoneCheck.setOnClickListener { checkPhoneDuplicate() }
    }

    // ----------------------------
    // 서버 중복확인 호출
    // ----------------------------
    private fun checkIdDuplicate() {
        val customId = binding.etProfileUsername.text?.toString()?.trim().orEmpty()

        if (customId.length < 4) {
            showMessage(binding.tvProfileUsernameMsg, "*아이디는 4자 이상이어야 합니다.", false)
            isIdChecked = false
            return
        }
        viewModel.checkCustomId(customId)
    }

    private fun checkNicknameDuplicate() {
        val nickname = binding.etProfileNickname.text?.toString()?.trim().orEmpty()

        if (nickname.isEmpty()) {
            showMessage(binding.tvProfileNicknameMsg, "*닉네임을 입력해 주세요.", false)
            isNicknameChecked = false
            return
        }
        viewModel.checkNickname(nickname)
    }

    private fun checkEmailDuplicate() {
        val email = binding.etProfileEmail.text?.toString()?.trim().orEmpty()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(binding.tvProfileEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            isEmailChecked = false
            return
        }
        viewModel.checkEmail(email)
    }

    private fun checkPhoneDuplicate() {
        val phone = binding.etProfilePhone.text?.toString()?.trim().orEmpty()

        if (phone.length < 10) {
            showMessage(binding.tvProfilePhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            isPhoneChecked = false
            return
        }
        viewModel.checkPhone(phone)
    }

    // ----------------------------
    // 서버 User -> 화면 채우기
    // ----------------------------
    private fun fillProfileFromUser(user: User) {
        binding.etProfileUsername.setText(user.customId)
        binding.etProfileName.setText(user.name)
        binding.etProfileSchool.setText(user.school)
        binding.etProfileNickname.setText(user.nickname)
        binding.etProfileEmail.setText(user.email)
        binding.etProfilePhone.setText(user.phone)
        binding.etProfileBirth.setText(user.birthDate)
        binding.etProfilePassword.setText("")
        binding.etProfilePasswordConfirm.setText("")
    }

    private fun cacheOriginalsFromUser(user: User) {
        originalCustomId = user.customId
        originalNickname = user.nickname
        originalEmail = user.email
        originalPhone = user.phone
    }

    // ----------------------------
    // 저장용 User 만들기
    // ----------------------------
    private fun buildUserFromInputs(userId: String): User {
        return User(
            userId = userId,
            customId = binding.etProfileUsername.text.toString().trim(),
            name = binding.etProfileName.text.toString().trim(),
            school = binding.etProfileSchool.text.toString().trim(),
            nickname = binding.etProfileNickname.text.toString().trim(),
            email = binding.etProfileEmail.text.toString().trim(),
            phone = binding.etProfilePhone.text.toString().trim(),
            birthDate = binding.etProfileBirth.text.toString().trim()
        )
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    /**
     * "변경된 항목"만 중복확인을 강제
     */
    private fun validateDupChecksIfChanged(): Boolean {
        val customId = binding.etProfileUsername.text?.toString()?.trim().orEmpty()
        val nickname = binding.etProfileNickname.text?.toString()?.trim().orEmpty()
        val email = binding.etProfileEmail.text?.toString()?.trim().orEmpty()
        val phone = binding.etProfilePhone.text?.toString()?.trim().orEmpty()

        if (customId != originalCustomId && !isIdChecked) {
            showMessage(binding.tvProfileUsernameMsg, "*아이디 중복 확인을 해 주세요.", false)
            return false
        }
        if (nickname != originalNickname && !isNicknameChecked) {
            showMessage(binding.tvProfileNicknameMsg, "*닉네임 중복 확인을 해 주세요.", false)
            return false
        }
        if (email != originalEmail && !isEmailChecked) {
            showMessage(binding.tvProfileEmailMsg, "*이메일 중복 확인을 해 주세요.", false)
            return false
        }
        if (phone != originalPhone && !isPhoneChecked) {
            showMessage(binding.tvProfilePhoneMsg, "*전화번호 중복 확인을 해 주세요.", false)
            return false
        }
        return true
    }

    // ----------------------------
    // 입력 검증(필수: 이름/학교/생년월일 포함)
    // ----------------------------
    private fun validateInputs(): Boolean {
        // 기존 메시지들 숨김
        binding.tvProfileUsernameMsg.visibility = View.GONE
        binding.tvProfilePasswordMsg.visibility = View.GONE
        binding.tvProfilePasswordConfirmMsg.visibility = View.GONE
        binding.tvProfileNicknameMsg.visibility = View.GONE
        binding.tvProfileEmailMsg.visibility = View.GONE
        binding.tvProfilePhoneMsg.visibility = View.GONE

        val customId = binding.etProfileUsername.text?.toString()?.trim().orEmpty()
        val pw = binding.etProfilePassword.text?.toString().orEmpty()
        val pw2 = binding.etProfilePasswordConfirm.text?.toString().orEmpty()

        val name = binding.etProfileName.text?.toString()?.trim().orEmpty()
        val school = binding.etProfileSchool.text?.toString()?.trim().orEmpty()
        val birthDate = binding.etProfileBirth.text?.toString()?.trim().orEmpty()

        val nickname = binding.etProfileNickname.text?.toString()?.trim().orEmpty()
        val email = binding.etProfileEmail.text?.toString()?.trim().orEmpty()
        val phone = binding.etProfilePhone.text?.toString()?.trim().orEmpty()

        var ok = true

        if (customId.length < 4) {
            showMessage(binding.tvProfileUsernameMsg, "*아이디는 4자 이상이어야 합니다.", false)
            ok = false
        }

        // 이름/학교/생년월일
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            ok = false
        }
        if (school.isEmpty()) {
            Toast.makeText(requireContext(), "학교를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            ok = false
        }
        if (birthDate.isEmpty()) {
            Toast.makeText(requireContext(), "생년월일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            ok = false
        }

        // 비번 변경 시에만 검사
        if (pw.isNotEmpty() || pw2.isNotEmpty()) {
            val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")
            if (!regex.matches(pw)) {
                showMessage(binding.tvProfilePasswordMsg, "*영문, 숫자 조합 8~16자를 입력해 주세요.", false)
                ok = false
            }
            if (pw != pw2) {
                showMessage(binding.tvProfilePasswordConfirmMsg, "*비밀번호가 일치하지 않습니다.", false)
                ok = false
            }
        }

        if (nickname.isEmpty()) {
            showMessage(binding.tvProfileNicknameMsg, "*닉네임을 입력해 주세요.", false)
            ok = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(binding.tvProfileEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            ok = false
        }

        if (phone.length < 10) {
            showMessage(binding.tvProfilePhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            ok = false
        }

        return ok
    }

    // ----------------------------
    // 비밀번호 실시간 검증
    // ----------------------------
    private fun validatePassword() {
        val password = binding.etProfilePassword.text?.toString().orEmpty()
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")

        if (password.isEmpty()) {
            binding.tvProfilePasswordMsg.visibility = View.GONE
            return
        }

        if (!regex.matches(password)) {
            showMessage(binding.tvProfilePasswordMsg, "*영문, 숫자 조합 8~16자를 입력해 주세요.", false)
        } else {
            showMessage(binding.tvProfilePasswordMsg, "*사용 가능한 비밀번호입니다.", true)
        }
    }

    private fun validatePasswordCheck() {
        val password = binding.etProfilePassword.text?.toString().orEmpty()
        val passwordCheck = binding.etProfilePasswordConfirm.text?.toString().orEmpty()

        if (passwordCheck.isEmpty()) {
            binding.tvProfilePasswordConfirmMsg.visibility = View.GONE
            return
        }

        if (password != passwordCheck) {
            showMessage(binding.tvProfilePasswordConfirmMsg, "*비밀번호가 일치하지 않습니다.", false)
        } else {
            showMessage(binding.tvProfilePasswordConfirmMsg, "*비밀번호가 일치합니다.", true)
        }
    }

    // ----------------------------
    // 메시지 출력
    // ----------------------------
    private fun showMessage(textView: TextView, message: String, isSuccess: Boolean) {
        textView.text = message
        textView.setTextColor(
            if (isSuccess) Color.parseColor("#20CD7C")
            else Color.parseColor("#E53935")
        )
        textView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
