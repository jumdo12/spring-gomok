// src/main/resources/static/js/app.js
let currentScreen = 'login-screen';
let roomRefreshInterval = null;

// 화면 전환
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.style.display = 'none';
    });
    document.getElementById(screenId).style.display = 'block';
    currentScreen = screenId;
}

// 회원가입 화면 표시
function showRegister() {
    showScreen('register-screen');
}

// 회원가입
async function register() {
    const username = document.getElementById('reg-username').value;
    const userId = document.getElementById('reg-userid').value;
    const password = document.getElementById('reg-password').value;

    if (!username || !userId || !password) {
        alert('모든 항목을 입력하세요');
        return;
    }

    try {
        await registerAPI(username, userId, password);
        alert('회원가입 성공! 로그인해주세요.');
        showScreen('login-screen');
    } catch (error) {
        alert('회원가입 실패');
    }
}

// 로그인
async function login() {
    const userId = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!userId || !password) {
        alert('아이디와 비밀번호를 입력하세요');
        return;
    }

    const result = await loginAPI(userId, password);

    if (result) {
        document.getElementById('user-info').textContent = `${result.nickname}님`;
        showScreen('lobby-screen');
        loadRooms();
        startRoomRefresh();
    } else {
        alert('로그인 실패');
    }
}

// 방 목록 로드
async function loadRooms() {
    const rooms = await getRoomsAPI();
    const roomList = document.getElementById('room-list');

    if (!rooms || rooms.length === 0) {
        roomList.innerHTML = '<p style="text-align:center; padding:40px; color:#999;">대기중인 방이 없습니다</p>';
        return;
    }

    roomList.innerHTML = rooms.map(room => `
        <div class="room-item" onclick="joinRoom(${room.roomId})">
            <h3>${room.roomName}</h3>
            <p>방장: ${room.hostName}</p>
            <p>인원: ${room.participantCount}/2</p>
        </div>
    `).join('');
}

// 방 목록 자동 새로고침
function startRoomRefresh() {
    roomRefreshInterval = setInterval(() => {
        if (currentScreen === 'lobby-screen') {
            loadRooms();
        }
    }, 3000);
}

function stopRoomRefresh() {
    if (roomRefreshInterval) {
        clearInterval(roomRefreshInterval);
        roomRefreshInterval = null;
    }
}

// 방 만들기
async function createRoom() {
    const roomName = prompt('방 이름을 입력하세요');

    if (!roomName) return;

    const room = await createRoomAPI(roomName);

    if (room) {
        currentRoomId = room.roomId;
        connectSSE(currentRoomId);
        showScreen('room-screen');
        loadRoomInfo();
    }
}

// 방 입장
async function joinRoom(roomId) {
    const result = await joinRoomAPI(roomId);

    if (result) {
        currentRoomId = result.roomId;
        connectSSE(currentRoomId);
        showScreen('room-screen');
        loadRoomInfo();
    }
}

// 방 정보 로드
async function loadRoomInfo() {
    const roomInfo = await getRoomInfoAPI(currentRoomId);

    if (!roomInfo) return;

    document.getElementById('room-name').textContent = roomInfo.roomName;

    // TODO: 유저 정보 API로 이름 가져오기
    document.getElementById('host-name').textContent = '방장';

    if (roomInfo.opponentId) {
        document.getElementById('participant-name').textContent = '참가자';
        document.getElementById('start-btn').disabled = false;
    } else {
        document.getElementById('participant-name').textContent = '대기중...';
        document.getElementById('start-btn').disabled = true;
    }
}

// 방 나가기
async function leaveRoom() {
    await leaveRoomAPI(currentRoomId);
    disconnectSSE();
    showScreen('lobby-screen');
    loadRooms();
}

// 게임 시작
async function startGame() {
    await startGameAPI(currentRoomId);
}

// 게임 나가기
async function leaveGame() {
    await leaveRoomAPI(currentRoomId);
    disconnectSSE();
    showScreen('lobby-screen');
    loadRooms();
}

// 초기화
window.onload = () => {
    initCanvas();
    showScreen('login-screen');
};
