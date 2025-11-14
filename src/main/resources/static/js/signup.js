// DOM 요소
const userIdInput = document.getElementById('userId');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const signupBtn = document.getElementById('signupBtn');
const loginBtn = document.getElementById('loginBtn');
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

// 회원가입 처리
async function handleSignup() {
    const userId = userIdInput.value.trim();
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    if (!userId || !username || !password) {
        showError('모든 필드를 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/users/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId, password, username })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '회원가입에 실패했습니다.' }));
            throw new Error(errorData.message || '회원가입에 실패했습니다.');
        }

        const data = await response.json();
        console.log('회원가입 성공:', data);

        // 회원가입 성공 시 로그인 페이지로 이동
        alert('회원가입이 완료되었습니다. 로그인해주세요.');
        window.location.href = '/login.html';
    } catch (error) {
        showError(error.message);
    }
}

// 로그인 페이지로 이동
function goToLogin() {
    window.location.href = '/login.html';
}

// 엔터 키로 회원가입
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        handleSignup();
    }
}

// 이벤트 리스너 등록
signupBtn.addEventListener('click', handleSignup);
loginBtn.addEventListener('click', goToLogin);
closeModalBtn.addEventListener('click', closeModal);
userIdInput.addEventListener('keypress', handleKeyPress);
usernameInput.addEventListener('keypress', handleKeyPress);
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
            window.location.href = '/room-list.html';
        } else {
            // 로그인 안되어 있음 → 회원가입 화면 표시
            userIdInput.focus();
        }
    } catch (error) {
        // 로그인 안되어 있음 → 회원가입 화면 표시
        userIdInput.focus();
    }
}

// 페이지 로드 시 세션 체크
checkSession();
