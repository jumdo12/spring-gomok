// DOM 요소
const historyList = document.getElementById('historyList');
const backBtn = document.getElementById('backBtn');
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

// 로그인 페이지로 리다이렉트
function redirectToLogin() {
    window.location.href = '/login';
}

// 게임 전적 목록 조회
async function loadGameHistory() {
    try {
        const response = await fetch('/api/games', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401) {
            // 세션 만료 시 로그인 페이지로
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '전적을 불러올 수 없습니다.' }));
            throw new Error(errorData.message || '전적을 불러올 수 없습니다.');
        }

        const gameHistories = await response.json();
        renderGameHistory(gameHistories);
    } catch (error) {
        showError(error.message);
    }
}

// 게임 전적 목록 렌더링
function renderGameHistory(gameHistories) {
    if (gameHistories.length === 0) {
        historyList.innerHTML = '<div class="empty-message">플레이한 게임 기록이 없습니다.</div>';
        return;
    }

    historyList.innerHTML = gameHistories.map((history, index) => {
        const startTime = new Date(history.startTime);
        const formattedTime = formatDateTime(startTime);

        return `
            <div class="room-item" data-game-id="${escapeHtml(history.gameId)}">
                <div class="room-info">
                    <div class="room-name">게임 #${gameHistories.length - index}</div>
                    <div class="room-participants">${formattedTime}</div>
                </div>
                <button class="btn btn-primary btn-join">재생</button>
            </div>
        `;
    }).join('');

    // 재생 버튼 이벤트 리스너 등록
    document.querySelectorAll('.room-item').forEach(item => {
        item.addEventListener('click', () => {
            const gameId = item.dataset.gameId;
            viewGameReplay(gameId);
        });
    });
}

// 날짜 포맷팅
function formatDateTime(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// HTML 이스케이프 (XSS 방지)
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 게임 재생 페이지로 이동
function viewGameReplay(gameId) {
    window.location.href = `/game-replay?gameId=${encodeURIComponent(gameId)}`;
}

// 방 목록으로 돌아가기
function goBack() {
    window.location.href = '/room-list';
}

// 이벤트 리스너 등록
backBtn.addEventListener('click', goBack);
closeModalBtn.addEventListener('click', closeModal);

// 모달 외부 클릭 시 닫기
errorModal.addEventListener('click', (event) => {
    if (event.target === errorModal) {
        closeModal();
    }
});

// 페이지 로드 시 실행
loadGameHistory();
