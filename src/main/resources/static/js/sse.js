let sseConnection = null;
let currentRoomId = null;

function connectSSE(roomId) {
    if (sseConnection) {
        sseConnection.close();
    }

    currentRoomId = roomId;
    sseConnection = new EventSource(`/api/rooms/${roomId}/subscribe`);

    // 방 업데이트 이벤트
    sseConnection.addEventListener('room-update', (event) => {
        const data = JSON.parse(event.data);
        handleRoomUpdate(data);
    });

    // 오목 착수 이벤트
    sseConnection.addEventListener('move', (event) => {
        const data = JSON.parse(event.data);
        handleMove(data);
    });

    // 게임 종료 이벤트
    sseConnection.addEventListener('game-end', (event) => {
        const data = JSON.parse(event.data);
        handleGameEnd(data);
    });

    sseConnection.onerror = () => {
        console.log('SSE 연결 끊김, 재연결 시도...');
        setTimeout(() => connectSSE(roomId), 3000);
    };
}

function disconnectSSE() {
    if (sseConnection) {
        sseConnection.close();
        sseConnection = null;
    }
    currentRoomId = null;
}

function handleRoomUpdate(data) {
    console.log('방 업데이트:', data);

    switch(data.type) {
        case 'PARTICIPANT_JOINED':
            alert(`${data.userName}님이 입장했습니다`);
            if (currentScreen === 'room-screen') {
                loadRoomInfo();
            }
            break;
        case 'PARTICIPANT_LEFT':
            alert(`${data.userName}님이 퇴장했습니다`);
            if (currentScreen === 'room-screen') {
                loadRoomInfo();
            }
            break;
        case 'GAME_STARTED':
            showScreen('game-screen');
            initGame();
            break;
    }
}

function handleMove(data) {
    console.log('상대방 착수:', data);
    drawStone(data.row, data.col, false);

    if (data.gameStatus === 'FINISHED') {
        showResult(data.winner);
    }
}

function handleGameEnd(data) {
    showResult(data.winner);
}
