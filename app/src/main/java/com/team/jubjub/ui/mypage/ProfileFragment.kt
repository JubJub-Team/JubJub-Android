package com.team.jubjub.ui.mypage

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 중복확인 완료 플래그
    private var isIdChecked = false
    private var isNicknameChecked = false
    private var isEmailChecked = false
    private var isPhoneChecked = false

    // 변경 여부 판단용 최초 로딩값
    private var originalUsername: String = ""
    private var originalNickname: String = ""
    private var originalEmail: String = ""
    private var originalPhone: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        // 더미 데이터로 화면 채우기 + 원본값 저장
        val profile = fakeProfile()
        fillProfile(profile)
        cacheOriginals(profile)

        // 리스너 세팅
        setListeners()

        binding.btnProfileSave.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            // "변경된 항목"만 중복확인 강제 (프로필 수정 특성 유지)
            if (!validateDupChecksIfChanged()) return@setOnClickListener

            // TODO: 서버 붙으면 여기서 update API 호출
            // (요청대로 중복확인쪽 Toast는 제거. 저장 결과는 필요하면 TextView나 Dialog로 통일 가능)
            // 여기서는 일단 아무 피드백 없으면 허전하니, 원하면 저장 성공도 TextView로 통일해줄게.
        }

        binding.btnProfileCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * 입력 변경 시 중복확인 무효화 + 메시지 숨김
     */
    private fun setListeners() = with(binding) {

        // 아이디 변경되면 중복확인 다시 하게 만들기
        etProfileUsername.doAfterTextChanged {
            isIdChecked = false
            tvProfileUsernameMsg.visibility = View.GONE
        }

        // 비밀번호 실시간 검증
        etProfilePassword.doAfterTextChanged { validatePassword() }
        etProfilePasswordConfirm.doAfterTextChanged { validatePasswordCheck() }

        // 닉네임 변경되면 중복확인 다시함
        etProfileNickname.doAfterTextChanged {
            isNicknameChecked = false
            tvProfileNicknameMsg.visibility = View.GONE
        }

        // 이메일 변경되면 중복확인 다시함
        etProfileEmail.doAfterTextChanged {
            isEmailChecked = false
            tvProfileEmailMsg.visibility = View.GONE
        }

        // 전화번호 변경되면 중복확인 다시함
        etProfilePhone.doAfterTextChanged {
            isPhoneChecked = false
            tvProfilePhoneMsg.visibility = View.GONE
        }

        // TextView로 출력
        btnProfileUsernameCheck.setOnClickListener { checkIdDuplicate() }
        btnProfileNicknameCheck.setOnClickListener { checkNicknameDuplicate() }
        btnProfileEmailCheck.setOnClickListener { checkEmailDuplicate() }
        btnProfilePhoneCheck.setOnClickListener { checkPhoneDuplicate() }
    }

    // ----------------------------
    // 더미 데이터 / 초기 세팅
    // ----------------------------
    private fun fakeProfile(): Map<String, String> = mapOf(
        "username" to "Swu0520",
        "name"     to "김슈니",
        "school"   to "서울여자대학교",
        "nickname" to "슈니",
        "email"    to "swu0520@ac.kr",
        "phone"    to "010-0000-0000",
        "birth"    to "2000.01.01"
    )

    private fun cacheOriginals(profile: Map<String, String>) {
        originalUsername = profile["username"].orEmpty()
        originalNickname = profile["nickname"].orEmpty()
        originalEmail = profile["email"].orEmpty()
        originalPhone = profile["phone"].orEmpty()
    }

    private fun fillProfile(profile: Map<String, String>) = with(binding) {
        etProfileUsername.setText(profile["username"].orEmpty())
        etProfileName.setText(profile["name"].orEmpty())
        etProfileSchool.setText(profile["school"].orEmpty())
        etProfileNickname.setText(profile["nickname"].orEmpty())
        etProfileEmail.setText(profile["email"].orEmpty())
        etProfilePhone.setText(profile["phone"].orEmpty())
        etProfileBirth.setText(profile["birth"].orEmpty())
        etProfilePassword.setText("")
        etProfilePasswordConfirm.setText("")

        // 프로필 수정에서는 "원래 값"은 이미 내 값이니까 중복확인을 필수로 강제하지 않는 게 자연스러움.
        // 다만, 변경하면 다시 체크하도록 위 doAfterTextChanged에서 false로 돌림.
        isIdChecked = true
        isNicknameChecked = true
        isEmailChecked = true
        isPhoneChecked = true
    }

    private fun checkIdDuplicate() = with(binding) {
        val id = etProfileUsername.text?.toString()?.trim().orEmpty()

        if (id.length < 4) {
            showMessage(tvProfileUsernameMsg, "*아이디는 4자 이상이어야 합니다.", false)
            isIdChecked = false
            return
        }

        // TODO: 아이디 중복 확인 API
        if (id == "admin" || id == "test") {
            showMessage(tvProfileUsernameMsg, "*이미 사용 중인 아이디입니다.", false)
            isIdChecked = false
        } else {
            showMessage(tvProfileUsernameMsg, "*사용 가능한 아이디입니다.", true)
            isIdChecked = true
        }
    }

    private fun validatePassword() = with(binding) {
        val password = etProfilePassword.text?.toString().orEmpty()
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")

        // 프로필 수정 특성: 비번 안 바꾸면 메시지 숨김
        if (password.isEmpty()) {
            tvProfilePasswordMsg.visibility = View.GONE
            return
        }

        if (!regex.matches(password)) {
            showMessage(tvProfilePasswordMsg, "*영문, 숫자 조합 8~16자를 입력해 주세요.", false)
        } else {
            showMessage(tvProfilePasswordMsg, "*사용 가능한 비밀번호입니다.", true)
        }
    }

    private fun validatePasswordCheck() = with(binding) {
        val password = etProfilePassword.text?.toString().orEmpty()
        val passwordCheck = etProfilePasswordConfirm.text?.toString().orEmpty()

        // 프로필 수정 특성: 확인칸 비면 숨김
        if (passwordCheck.isEmpty()) {
            tvProfilePasswordConfirmMsg.visibility = View.GONE
            return
        }

        if (password != passwordCheck) {
            showMessage(tvProfilePasswordConfirmMsg, "*비밀번호가 일치하지 않습니다.", false)
        } else {
            showMessage(tvProfilePasswordConfirmMsg, "*비밀번호가 일치합니다.", true)
        }
    }

    private fun checkNicknameDuplicate() = with(binding) {
        val nickname = etProfileNickname.text?.toString()?.trim().orEmpty()

        // TODO: 닉네임 중복 확인 API
        if (nickname.isEmpty()) {
            showMessage(tvProfileNicknameMsg, "*닉네임을 입력해 주세요.", false)
            isNicknameChecked = false
        } else if (nickname == "관리자") {
            showMessage(tvProfileNicknameMsg, "*이미 사용 중인 닉네임입니다.", false)
            isNicknameChecked = false
        } else {
            showMessage(tvProfileNicknameMsg, "*사용 가능한 닉네임입니다.", true)
            isNicknameChecked = true
        }
    }

    private fun checkEmailDuplicate() = with(binding) {
        val email = etProfileEmail.text?.toString()?.trim().orEmpty()

        // TODO: 이메일 중복 확인 API
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(tvProfileEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            isEmailChecked = false
        } else {
            showMessage(tvProfileEmailMsg, "*사용 가능한 이메일입니다.", true)
            isEmailChecked = true
        }
    }

    private fun checkPhoneDuplicate() = with(binding) {
        val phone = etProfilePhone.text?.toString()?.trim().orEmpty()

        // TODO: 전화번호 중복 확인 API
        if (phone.length < 10) {
            showMessage(tvProfilePhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            isPhoneChecked = false
        } else {
            showMessage(tvProfilePhoneMsg, "*사용 가능한 전화번호입니다.", true)
            isPhoneChecked = true
        }
    }

    /**
     * 프로필 수정 기능 유지 포인트:
     * - "변경된 항목"만 중복확인을 강제
     * - 변경 안 했으면 기존 값은 OK 처리
     */
    private fun validateDupChecksIfChanged(): Boolean = with(binding) {
        val username = etProfileUsername.text?.toString()?.trim().orEmpty()
        val nickname = etProfileNickname.text?.toString()?.trim().orEmpty()
        val email = etProfileEmail.text?.toString()?.trim().orEmpty()
        val phone = etProfilePhone.text?.toString()?.trim().orEmpty()

        // 변경된 것만 체크 강제
        if (username != originalUsername && !isIdChecked) {
            showMessage(tvProfileUsernameMsg, "*아이디 중복 확인을 해 주세요.", false)
            return false
        }
        if (nickname != originalNickname && !isNicknameChecked) {
            showMessage(tvProfileNicknameMsg, "*닉네임 중복 확인을 해 주세요.", false)
            return false
        }
        if (email != originalEmail && !isEmailChecked) {
            showMessage(tvProfileEmailMsg, "*이메일 중복 확인을 해 주세요.", false)
            return false
        }
        if (phone != originalPhone && !isPhoneChecked) {
            showMessage(tvProfilePhoneMsg, "*전화번호 중복 확인을 해 주세요.", false)
            return false
        }

        true
    }

    // ----------------------------
    // 기존 입력 검증(최소) 유지
    // ----------------------------
    private fun validateInputs(): Boolean = with(binding) {
        tvProfileUsernameMsg.visibility = View.GONE
        tvProfilePasswordMsg.visibility = View.GONE
        tvProfilePasswordConfirmMsg.visibility = View.GONE
        tvProfileNicknameMsg.visibility = View.GONE
        tvProfileEmailMsg.visibility = View.GONE
        tvProfilePhoneMsg.visibility = View.GONE

        val username = etProfileUsername.text?.toString()?.trim().orEmpty()
        val pw = etProfilePassword.text?.toString().orEmpty()
        val pw2 = etProfilePasswordConfirm.text?.toString().orEmpty()
        val nickname = etProfileNickname.text?.toString()?.trim().orEmpty()
        val email = etProfileEmail.text?.toString()?.trim().orEmpty()
        val phone = etProfilePhone.text?.toString()?.trim().orEmpty()

        var ok = true

        if (username.length < 4) {
            showMessage(tvProfileUsernameMsg, "*아이디는 4자 이상이어야 합니다.", false)
            ok = false
        }

        // 비번 변경 시에만 검사
        if (pw.isNotEmpty() || pw2.isNotEmpty()) {
            val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")
            if (!regex.matches(pw)) {
                showMessage(tvProfilePasswordMsg, "*영문, 숫자 조합 8~16자를 입력해 주세요.", false)
                ok = false
            }
            if (pw != pw2) {
                showMessage(tvProfilePasswordConfirmMsg, "*비밀번호가 일치하지 않습니다.", false)
                ok = false
            }
        }

        if (nickname.isEmpty()) {
            showMessage(tvProfileNicknameMsg, "*닉네임을 입력해 주세요.", false)
            ok = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage(tvProfileEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            ok = false
        }

        if (phone.length < 10) {
            showMessage(tvProfilePhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            ok = false
        }

        ok
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
