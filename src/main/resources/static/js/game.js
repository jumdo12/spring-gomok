// 상수
const BOARD_SIZE = 19;
const CELL_SIZE = 30;
const STONE_RADIUS = 13;

// DOM 요소
const waitingScreen = document.getElementById('waitingScreen');
const readyScreen = document.getElementById('readyScreen');
const playingScreen = document.getElementById('playingScreen');
const finishedScreen = document.getElementById('finishedScreen');

const roomNameWaiting = document.getElementById('roomNameWaiting');
const roomNameReady = document.getElementById('roomNameReady');
const roomNamePlaying = document.getElementById('roomNamePlaying');
const blackPlayerName = document.getElementById('blackPlayerName');
const whitePlayerName = document.getElementById('whitePlayerName');
const turnIndicator = document.getElementById('turnIndicator');
const resultMessage = document.getElementById('resultMessage');
const winnerInfo = document.getElementById('winnerInfo');

const switchStoneBtn = document.getElementById('switchStoneBtn');
const startGameBtn = document.getElementById('startGameBtn');
const leaveBtn1 = document.getElementById('leaveBtn1');
const leaveBtn2 = document.getElementById('leaveBtn2');
const leaveBtn3 = document.getElementById('leaveBtn3');
const backToListBtn = document.getElementById('backToListBtn');

const canvas = document.getElementById('gameBoard');
const ctx = canvas.getContext('2d');

const waitingCanvas = document.getElementById('waitingBoard');
const waitingCtx = waitingCanvas.getContext('2d');
const myStoneWaiting = document.getElementById('myStoneWaiting');

const readyCanvas = document.getElementById('readyBoard');
const readyCtx = readyCanvas.getContext('2d');
const myStoneReady = document.getElementById('myStoneReady');
const myNameReady = document.getElementById('myNameReady');
const opponentStoneReady = document.getElementById('opponentStoneReady');
const opponentNameReady = document.getElementById('opponentNameReady');

const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorMessage');
const closeModalBtn = document.getElementById('closeModalBtn');

// 채팅 요소
const chatMessagesWaiting = document.getElementById('chatMessagesWaiting');
const chatInputWaiting = document.getElementById('chatInputWaiting');
const chatSendBtnWaiting = document.getElementById('chatSendBtnWaiting');

const chatMessagesReady = document.getElementById('chatMessagesReady');
const chatInputReady = document.getElementById('chatInputReady');
const chatSendBtnReady = document.getElementById('chatSendBtnReady');

const chatMessagesPlaying = document.getElementById('chatMessagesPlaying');
const chatInputPlaying = document.getElementById('chatInputPlaying');
const chatSendBtnPlaying = document.getElementById('chatSendBtnPlaying');

// 게임 상태
let roomId = null;
let roomInfo = null;
let eventSource = null;
let board = Array(BOARD_SIZE).fill(null).map(() => Array(BOARD_SIZE).fill('EMPTY'));
let myStone = null;
let opponentStone = null;
let currentTurn = 'BLACK';
let gameStatus = null;
let isHost = false;
let totalMoves = 0;

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

// 방 목록으로 이동
function goToRoomList() {
    window.location.href = '/room-list';
}

// 방 정보 조회
async function loadRoomInfo() {
    try {
        const response = await fetch(`/api/rooms/${roomId}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '방 정보를 불러올 수 없습니다.' }));
            throw new Error(errorData.message || '방 정보를 불러올 수 없습니다.');
        }

        roomInfo = await response.json();

        // SSE 구독 시작 (최초 한 번만)
        if (!eventSource) {
            subscribeToEvents();
        }

        // 게임 상태에 따라 화면 렌더링
        myStone = roomInfo.myStone;
        opponentStone = myStone === 'BLACK' ? 'WHITE' : 'BLACK';
        isHost = roomInfo.isHost;

        switch (roomInfo.gameStatus) {
            case 'WAITING':
                renderWaitingScreen();
                break;
            case 'READY':
                renderReadyScreen();
                break;
            case 'PLAYING':
                renderPlayingScreen();
                break;
            case 'FINISHED':
                // 게임 종료 상태는 서버에서 winner 정보가 필요
                renderFinishedScreen(null);
                break;
        }
    } catch (error) {
        showError(error.message);
    }
}

// SSE 이벤트 구독
function subscribeToEvents() {
    eventSource = new EventSource(`/api/rooms/${roomId}/subscribe`, {
        withCredentials: true
    });

    eventSource.addEventListener('room-update', (event) => {
        const data = JSON.parse(event.data);
        handleRoomUpdateEvent(data);
    });

    eventSource.addEventListener('move', (event) => {
        const data = JSON.parse(event.data);
        handleMoveEvent(data);
    });

    eventSource.onerror = (error) => {
        // SSE 에러 처리
    };
}

// RoomUpdateEvent 처리
function handleRoomUpdateEvent(data) {
    switch (data.type) {
        case 'PARTICIPANT_JOINED':
        case 'GAME_STARTED':
        case 'STONE_SWITCHED':
            // 방 정보 다시 조회 (자동으로 올바른 화면으로 전환됨)
            loadRoomInfo();
            break;
        case 'PARTICIPANT_LEFT':
            // 방장이면 대기 화면으로, 방장이 아니면 방 목록으로 이동
            if (isHost) {
                alert('상대방이 방을 나갔습니다.');
                loadRoomInfo(); // 대기 화면으로 돌아감
            } else {
                alert('방장이 방을 나갔습니다.');
                goToRoomList();
            }
            break;
        case 'CHAT_MESSAGE':
            // 채팅 메시지 수신
            receiveChatMessage(data.data);
            break;
    }
}

// MoveEvent 처리
function handleMoveEvent(data) {
    // 상대방 착수
    board[data.row][data.col] = opponentStone;
    totalMoves++;
    currentTurn = currentTurn === 'BLACK' ? 'WHITE' : 'BLACK';

    drawBoard();
    updateTurnIndicator();

    // 게임 종료 확인
    if (data.gameStatus === 'FINISHED') {
        gameStatus = 'FINISHED';
        renderFinishedScreen(data.winner);
    }
}

// 대기 화면 렌더링
function renderWaitingScreen() {
    gameStatus = 'WAITING';
    hideAllScreens();
    waitingScreen.style.display = 'block';
    roomNameWaiting.textContent = roomInfo.roomName || '방 이름';

    // 내 돌 색깔 표시
    if (roomInfo.myStone === 'BLACK') {
        myStoneWaiting.textContent = '●';
        myStoneWaiting.style.color = '#000';
    } else {
        myStoneWaiting.textContent = '○';
        myStoneWaiting.style.color = '#666';
    }

    // 대기 화면 오목판 그리기
    drawEmptyBoard(waitingCtx, waitingCanvas.width, waitingCanvas.height, 20);
}

// 준비 화면 렌더링
function renderReadyScreen() {
    gameStatus = 'READY';
    hideAllScreens();
    readyScreen.style.display = 'block';
    roomNameReady.textContent = roomInfo.roomName || '방 이름';

    // 왼쪽: 나
    myNameReady.textContent = '나';
    if (roomInfo.myStone === 'BLACK') {
        myStoneReady.textContent = '●';
        myStoneReady.style.color = '#000';
    } else {
        myStoneReady.textContent = '○';
        myStoneReady.style.color = '#666';
    }

    // 오른쪽: 상대방
    if (roomInfo.opponentId && roomInfo.opponentName) {
        opponentNameReady.textContent = roomInfo.opponentName;
        if (roomInfo.myStone === 'BLACK') {
            opponentStoneReady.textContent = '○';
            opponentStoneReady.style.color = '#666';
        } else {
            opponentStoneReady.textContent = '●';
            opponentStoneReady.style.color = '#000';
        }
    }

    // 방장만 버튼 표시
    if (roomInfo.isHost) {
        switchStoneBtn.style.display = 'inline-block';
        startGameBtn.style.display = 'inline-block';
    } else {
        switchStoneBtn.style.display = 'none';
        startGameBtn.style.display = 'none';
    }

    // 준비 화면 오목판 그리기
    drawEmptyBoard(readyCtx, readyCanvas.width, readyCanvas.height, 20);
}

// 게임 화면 렌더링
function renderPlayingScreen() {
    gameStatus = 'PLAYING';
    hideAllScreens();
    playingScreen.style.display = 'block';
    roomNamePlaying.textContent = roomInfo.roomName || '방 이름';

    // 보드 초기화 및 그리기 (최초 진입 시만)
    if (totalMoves === 0) {
        board = Array(BOARD_SIZE).fill(null).map(() => Array(BOARD_SIZE).fill('EMPTY'));
        currentTurn = 'BLACK';
    }

    drawBoard();
    updateTurnIndicator();
}

// 게임 종료 화면 렌더링
function renderFinishedScreen(winner) {
    hideAllScreens();
    finishedScreen.style.display = 'block';

    if (winner === myStone) {
        resultMessage.textContent = '승리했습니다!';
        resultMessage.style.color = '#27ae60';
    } else if (winner === opponentStone) {
        resultMessage.textContent = '패배했습니다!';
        resultMessage.style.color = '#e74c3c';
    } else {
        resultMessage.textContent = '무승부입니다!';
        resultMessage.style.color = '#95a5a6';
    }

    winnerInfo.textContent = winner === 'BLACK' ? '흑돌 승리' : '백돌 승리';
}

// 모든 화면 숨기기
function hideAllScreens() {
    waitingScreen.style.display = 'none';
    readyScreen.style.display = 'none';
    playingScreen.style.display = 'none';
    finishedScreen.style.display = 'none';
}

// 빈 오목판 그리기 (대기 화면용)
function drawEmptyBoard(context, width, height, cellSize) {
    const boardSize = 19;

    // 배경
    context.fillStyle = '#deb887';
    context.fillRect(0, 0, width, height);

    // 격자선
    context.strokeStyle = '#000';
    context.lineWidth = 1;

    for (let i = 0; i < boardSize; i++) {
        // 세로선
        context.beginPath();
        context.moveTo(cellSize * (i + 0.5), cellSize * 0.5);
        context.lineTo(cellSize * (i + 0.5), cellSize * (boardSize - 0.5));
        context.stroke();

        // 가로선
        context.beginPath();
        context.moveTo(cellSize * 0.5, cellSize * (i + 0.5));
        context.lineTo(cellSize * (boardSize - 0.5), cellSize * (i + 0.5));
        context.stroke();
    }

    // 화점
    const starPoints = [
        [3, 3], [3, 9], [3, 15],
        [9, 3], [9, 9], [9, 15],
        [15, 3], [15, 9], [15, 15]
    ];

    context.fillStyle = '#000';
    starPoints.forEach(([row, col]) => {
        context.beginPath();
        context.arc(cellSize * (col + 0.5), cellSize * (row + 0.5), 2, 0, 2 * Math.PI);
        context.fill();
    });
}

// 오목판 그리기
function drawBoard() {
    // 배경
    ctx.fillStyle = '#deb887';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 격자선
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 1;

    for (let i = 0; i < BOARD_SIZE; i++) {
        // 세로선
        ctx.beginPath();
        ctx.moveTo(CELL_SIZE * (i + 0.5), CELL_SIZE * 0.5);
        ctx.lineTo(CELL_SIZE * (i + 0.5), CELL_SIZE * (BOARD_SIZE - 0.5));
        ctx.stroke();

        // 가로선
        ctx.beginPath();
        ctx.moveTo(CELL_SIZE * 0.5, CELL_SIZE * (i + 0.5));
        ctx.lineTo(CELL_SIZE * (BOARD_SIZE - 0.5), CELL_SIZE * (i + 0.5));
        ctx.stroke();
    }

    // 화점 (5개: 중앙, 4개 모서리 근처)
    const starPoints = [
        [3, 3], [3, 9], [3, 15],
        [9, 3], [9, 9], [9, 15],
        [15, 3], [15, 9], [15, 15]
    ];

    ctx.fillStyle = '#000';
    starPoints.forEach(([row, col]) => {
        ctx.beginPath();
        ctx.arc(CELL_SIZE * (col + 0.5), CELL_SIZE * (row + 0.5), 3, 0, 2 * Math.PI);
        ctx.fill();
    });

    // 돌 그리기
    for (let row = 0; row < BOARD_SIZE; row++) {
        for (let col = 0; col < BOARD_SIZE; col++) {
            if (board[row][col] === 'BLACK') {
                drawStone(row, col, 'BLACK');
            } else if (board[row][col] === 'WHITE') {
                drawStone(row, col, 'WHITE');
            }
        }
    }
}

// 돌 그리기
function drawStone(row, col, color) {
    const x = CELL_SIZE * (col + 0.5);
    const y = CELL_SIZE * (row + 0.5);

    ctx.beginPath();
    ctx.arc(x, y, STONE_RADIUS, 0, 2 * Math.PI);

    if (color === 'BLACK') {
        ctx.fillStyle = '#000';
        ctx.fill();
    } else {
        ctx.fillStyle = '#fff';
        ctx.fill();
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 1;
        ctx.stroke();
    }
}

// 턴 표시 업데이트
function updateTurnIndicator() {
    if (currentTurn === myStone) {
        turnIndicator.textContent = '내 차례입니다';
        turnIndicator.style.color = '#27ae60';
    } else {
        turnIndicator.textContent = '상대방 차례입니다';
        turnIndicator.style.color = '#95a5a6';
    }
}

// 캔버스 클릭 이벤트 (돌 놓기)
canvas.addEventListener('click', async (event) => {
    if (gameStatus !== 'PLAYING') return;
    if (currentTurn !== myStone) {
        showError('상대방의 차례입니다.');
        return;
    }

    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const col = Math.round(x / CELL_SIZE - 0.5);
    const row = Math.round(y / CELL_SIZE - 0.5);

    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
    if (board[row][col] !== 'EMPTY') {
        showError('이미 돌이 놓여있습니다.');
        return;
    }

    try {
        const response = await fetch(`/api/game/${roomId}/place`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({ row, col })
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '착수에 실패했습니다.' }));
            throw new Error(errorData.message || '착수에 실패했습니다.');
        }

        const data = await response.json();

        // 내 착수 반영
        board[row][col] = myStone;
        totalMoves++;
        currentTurn = currentTurn === 'BLACK' ? 'WHITE' : 'BLACK';

        drawBoard();
        updateTurnIndicator();

        // 게임 종료 확인
        if (data.gameStatus === 'FINISHED') {
            gameStatus = 'FINISHED';
            renderFinishedScreen(data.winner);
        }
    } catch (error) {
        showError(error.message);
    }
});

// 돌 바꾸기
async function switchStone() {
    try {
        const response = await fetch(`/api/rooms/${roomId}/switch-stone`, {
            method: 'POST',
            credentials: 'include'
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '돌 바꾸기에 실패했습니다.' }));
            throw new Error(errorData.message || '돌 바꾸기에 실패했습니다.');
        }

        // 성공하면 방 정보 다시 조회 (화면 업데이트)
        await loadRoomInfo();
    } catch (error) {
        showError(error.message);
    }
}

// 게임 시작
async function startGame() {
    try {
        const response = await fetch(`/api/rooms/${roomId}/start`, {
            method: 'POST',
            credentials: 'include'
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '게임 시작에 실패했습니다.' }));
            throw new Error(errorData.message || '게임 시작에 실패했습니다.');
        }

        // 성공하면 방 정보 다시 조회 (화면 업데이트)
        await loadRoomInfo();
    } catch (error) {
        showError(error.message);
    }
}

// 방 나가기
async function leaveRoom() {
    try {
        const response = await fetch(`/api/rooms/${roomId}/leave`, {
            method: 'POST',
            credentials: 'include'
        });

        // 성공/실패 상관없이 방 목록으로
        goToRoomList();
    } catch (error) {
        goToRoomList();
    }
}

// 채팅 메시지 전송
async function sendChatMessage(inputElement, chatMessagesElement) {
    const content = inputElement.value.trim();

    if (!content) {
        return;
    }

    try {
        const response = await fetch(`/api/rooms/${roomId}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({ content })
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '메시지 전송에 실패했습니다.' }));
            throw new Error(errorData.message || '메시지 전송에 실패했습니다.');
        }

        // 내 메시지 표시
        const chatMessage = {
            from: '나',
            content: content,
            sendAt: new Date().toISOString()
        };

        displayChatMessage(chatMessagesElement, chatMessage, true);
        inputElement.value = '';
    } catch (error) {
        showError(error.message);
    }
}

// 채팅 메시지 수신
function receiveChatMessage(chatMessage) {
    // 현재 화면에 맞는 채팅 영역에 표시
    let chatMessagesElement;

    if (gameStatus === 'WAITING') {
        chatMessagesElement = chatMessagesWaiting;
    } else if (gameStatus === 'READY') {
        chatMessagesElement = chatMessagesReady;
    } else if (gameStatus === 'PLAYING') {
        chatMessagesElement = chatMessagesPlaying;
    }

    if (chatMessagesElement) {
        displayChatMessage(chatMessagesElement, chatMessage, false);
    }
}

// 채팅 메시지 표시
function displayChatMessage(chatMessagesElement, chatMessage, isMine) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${isMine ? 'mine' : 'other'}`;

    const senderDiv = document.createElement('div');
    senderDiv.className = 'chat-message-sender';
    senderDiv.textContent = chatMessage.from;

    const contentDiv = document.createElement('div');
    contentDiv.className = 'chat-message-content';
    contentDiv.textContent = chatMessage.content;

    const timeDiv = document.createElement('div');
    timeDiv.className = 'chat-message-time';
    timeDiv.textContent = formatTime(chatMessage.sendAt);

    messageDiv.appendChild(senderDiv);
    messageDiv.appendChild(contentDiv);
    messageDiv.appendChild(timeDiv);

    chatMessagesElement.appendChild(messageDiv);

    // 스크롤을 맨 아래로
    chatMessagesElement.scrollTop = chatMessagesElement.scrollHeight;
}

// 시간 포맷팅
function formatTime(dateString) {
    const date = new Date(dateString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
}

// 이벤트 리스너
switchStoneBtn.addEventListener('click', switchStone);
startGameBtn.addEventListener('click', startGame);
leaveBtn1.addEventListener('click', leaveRoom);
leaveBtn2.addEventListener('click', leaveRoom);
leaveBtn3.addEventListener('click', leaveRoom);
backToListBtn.addEventListener('click', goToRoomList);
closeModalBtn.addEventListener('click', closeModal);

// 채팅 전송 버튼
chatSendBtnWaiting.addEventListener('click', () => sendChatMessage(chatInputWaiting, chatMessagesWaiting));
chatSendBtnReady.addEventListener('click', () => sendChatMessage(chatInputReady, chatMessagesReady));
chatSendBtnPlaying.addEventListener('click', () => sendChatMessage(chatInputPlaying, chatMessagesPlaying));

// 채팅 입력 엔터키
chatInputWaiting.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendChatMessage(chatInputWaiting, chatMessagesWaiting);
});
chatInputReady.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendChatMessage(chatInputReady, chatMessagesReady);
});
chatInputPlaying.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendChatMessage(chatInputPlaying, chatMessagesPlaying);
});

// 모달 외부 클릭 시 닫기
errorModal.addEventListener('click', (event) => {
    if (event.target === errorModal) {
        closeModal();
    }
});

// 페이지 언로드 시 SSE 종료
window.addEventListener('beforeunload', () => {
    if (eventSource) {
        eventSource.close();
    }
});

// 초기화
(function init() {
    const urlParams = new URLSearchParams(window.location.search);
    roomId = urlParams.get('roomId');

    if (!roomId) {
        showError('방 ID가 없습니다.');
        setTimeout(goToRoomList, 2000);
        return;
    }

    loadRoomInfo();
})();
