// DOM 요소
const roomsList = document.getElementById('roomsList');
const createRoomBtn = document.getElementById('createRoomBtn');
const createRoomModal = document.getElementById('createRoomModal');
const roomNameInput = document.getElementById('roomName');
const confirmCreateBtn = document.getElementById('confirmCreateBtn');
const cancelCreateBtn = document.getElementById('cancelCreateBtn');
const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorMessage');
const closeModalBtn = document.getElementById('closeModalBtn');

// 자동 갱신 타이머
let refreshTimer = null;

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

// 방 목록 조회
async function loadRooms() {
    try {
        const response = await fetch('/api/rooms/waitings', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401) {
            // 세션 만료 시 로그인 페이지로
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '방 목록을 불러올 수 없습니다.' }));
            throw new Error(errorData.message || '방 목록을 불러올 수 없습니다.');
        }

        const rooms = await response.json();
        renderRooms(rooms);
    } catch (error) {
        showError(error.message);
    }
}

// 방 목록 렌더링
function renderRooms(rooms) {
    if (rooms.length === 0) {
        roomsList.innerHTML = '<div class="empty-message">대기중인 방이 없습니다.</div>';
        return;
    }

    roomsList.innerHTML = rooms.map(room => `
        <div class="room-item">
            <div class="room-info">
                <div class="room-name">${escapeHtml(room.roomName)}</div>
                <div class="room-participants">${room.participantCount}/2</div>
            </div>
            <button class="btn btn-primary btn-join" data-room-id="${room.roomId}">입장</button>
        </div>
    `).join('');

    // 입장 버튼 이벤트 리스너 등록
    document.querySelectorAll('.btn-join').forEach(btn => {
        btn.addEventListener('click', () => joinRoom(btn.dataset.roomId));
    });
}

// HTML 이스케이프 (XSS 방지)
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 방 만들기 모달 열기
function openCreateRoomModal() {
    roomNameInput.value = '';
    createRoomModal.style.display = 'flex';
    roomNameInput.focus();
}

// 방 만들기 모달 닫기
function closeCreateRoomModal() {
    createRoomModal.style.display = 'none';
}

// 방 만들기
async function createRoom() {
    const roomName = roomNameInput.value.trim();

    if (!roomName) {
        showError('방 이름을 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({ roomName })
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '방 생성에 실패했습니다.' }));
            throw new Error(errorData.message || '방 생성에 실패했습니다.');
        }

        const room = await response.json();
        console.log('방 생성 성공:', room);

        // 방 생성 성공 시 게임 페이지로 이동
        window.location.href = `/game?roomId=${room.roomId}`;
    } catch (error) {
        showError(error.message);
    }
}

// 방 입장
async function joinRoom(roomId) {
    try {
        const response = await fetch(`/api/rooms/${roomId}/join`, {
            method: 'POST',
            credentials: 'include'
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '방 입장에 실패했습니다.' }));
            throw new Error(errorData.message || '방 입장에 실패했습니다.');
        }

        const roomInfo = await response.json();
        console.log('방 입장 성공:', roomInfo);

        // 방 입장 성공 시 게임 페이지로 이동
        window.location.href = `/game?roomId=${roomId}`;
    } catch (error) {
        showError(error.message);
    }
}

// 로그아웃
async function logout() {
    try {
        const response = await fetch('/api/users/logout', {
            method: 'POST',
            credentials: 'include'
        });

        // 로그아웃은 성공/실패 상관없이 로그인 페이지로
        redirectToLogin();
    } catch (error) {
        // 에러가 나도 로그인 페이지로
        redirectToLogin();
    }
}

// 자동 갱신 시작
function startAutoRefresh() {
    // 3초마다 방 목록 갱신
    refreshTimer = setInterval(() => {
        loadRooms();
    }, 3000);
}

// 자동 갱신 중지
function stopAutoRefresh() {
    if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
    }
}

// 엔터 키로 방 만들기
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        createRoom();
    }
}

// 이벤트 리스너 등록
const logoutBtn = document.getElementById('logoutBtn');
createRoomBtn.addEventListener('click', openCreateRoomModal);
confirmCreateBtn.addEventListener('click', createRoom);
cancelCreateBtn.addEventListener('click', closeCreateRoomModal);
closeModalBtn.addEventListener('click', closeModal);
logoutBtn.addEventListener('click', logout);
roomNameInput.addEventListener('keypress', handleKeyPress);

// 모달 외부 클릭 시 닫기
createRoomModal.addEventListener('click', (event) => {
    if (event.target === createRoomModal) {
        closeCreateRoomModal();
    }
});

errorModal.addEventListener('click', (event) => {
    if (event.target === errorModal) {
        closeModal();
    }
});

// 페이지 로드 시 실행
loadRooms();
startAutoRefresh();

// 페이지 언로드 시 타이머 정리
window.addEventListener('beforeunload', stopAutoRefresh);
