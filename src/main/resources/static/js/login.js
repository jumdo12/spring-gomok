// DOM 요소
const userIdInput = document.getElementById('userId');
const passwordInput = document.getElementById('password');
const loginBtn = document.getElementById('loginBtn');
const signupBtn = document.getElementById('signupBtn');
const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorMessage');
const closeModalBtn = document.getElementById('closeModalBtn');

// 에러 모달 표시
function showError(message) {
    errorMessage.textContent = message;
    errorModal.style.display = 'flex';
}

// 에러 모달 닫기
function closeModal() {
    errorModal.style.display = 'none';
}

// 로그인 처리
async function handleLogin() {
    const userId = userIdInput.value.trim();
    const password = passwordInput.value.trim();

    if (!userId || !password) {
        showError('아이디와 비밀번호를 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId, password }),
            credentials: 'include' // 세션 쿠키 포함
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '로그인에 실패했습니다.' }));
            throw new Error(errorData.message || '로그인에 실패했습니다.');
        }

        const data = await response.json();
        console.log('로그인 성공:', data);

        // 로그인 성공 시 방 목록 페이지로 이동
        window.location.href = '/room-list';
    } catch (error) {
        showError(error.message);
    }
}

// 회원가입 페이지로 이동
function goToSignup() {
    window.location.href = '/signup';
}

// 엔터 키로 로그인
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        handleLogin();
    }
}

// 이벤트 리스너 등록
loginBtn.addEventListener('click', handleLogin);
signupBtn.addEventListener('click', goToSignup);
closeModalBtn.addEventListener('click', closeModal);
userIdInput.addEventListener('keypress', handleKeyPress);
passwordInput.addEventListener('keypress', handleKeyPress);

// 모달 외부 클릭 시 닫기
errorModal.addEventListener('click', (event) => {
    if (event.target === errorModal) {
        closeModal();
    }
});

// 세션 체크 (이미 로그인되어 있는지 확인)
async function checkSession() {
    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            // 이미 로그인되어 있음 → 방 목록으로 이동
            console.log('이미 로그인되어 있습니다. 방 목록으로 이동합니다.');
            window.location.href = '/room-list';
        } else {
            // 로그인 안되어 있음 → 로그인 화면 표시
            userIdInput.focus();
        }
    } catch (error) {
        // 로그인 안되어 있음 → 로그인 화면 표시
        userIdInput.focus();
    }
}

// 페이지 로드 시 세션 체크
checkSession();
