const API_BASE = '';
let authToken = null;
let currentUser = null;

// API 호출 헬퍼
async function apiCall(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }

    const response = await fetch(API_BASE + url, {
        ...options,
        headers
    });

    if (!response.ok && response.status === 401) {
        alert('로그인이 필요합니다');
        showScreen('login-screen');
        return null;
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

async function registerAPI(username, userId, password) {
    return await apiCall('/api/users/signup', {
        method: 'POST',
        body: JSON.stringify({ username, userId, password })
    });
}

async function loginAPI(userId, password) {
    const data = await apiCall('/api/users/login', {
        method: 'POST',
        body: JSON.stringify({ userId, password })
    });

    if (data) {
        currentUser = data;
    }

    return data;
}

// 방 목록 API
async function getRoomsAPI() {
    return await apiCall('/api/rooms/waitings');
}

// 방 만들기 API
async function createRoomAPI(roomName) {
    return await apiCall('/api/rooms', {
        method: 'POST',
        body: JSON.stringify({ roomName })
    });
}

// 방 정보 조회 API
async function getRoomInfoAPI(roomId) {
    return await apiCall(`/api/rooms/${roomId}`);
}

// 방 입장 API
async function joinRoomAPI(roomId) {
    return await apiCall(`/api/rooms/${roomId}/join`, {
        method: 'POST'
    });
}

// 방 나가기 API
async function leaveRoomAPI(roomId) {
    return await apiCall(`/api/rooms/${roomId}/leave`, {
        method: 'POST'
    });
}

// 게임 시작 API
async function startGameAPI(roomId) {
    return await apiCall(`/api/rooms/${roomId}/start`, {
        method: 'POST'
    });
}

// 오목 착수 API
async function placeStoneAPI(roomId, row, col) {
    return await apiCall(`/api/game/${roomId}/place`, {
        method: 'POST',
        body: JSON.stringify({ row, col })
    });
}