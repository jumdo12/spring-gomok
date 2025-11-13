const BOARD_SIZE = 19;
const CELL_SIZE = 30;
const STONE_RADIUS = 13;

let canvas, ctx;
let currentRoomId = null;
let isMyTurn = false;
let myStone = null;

function initCanvas() {
    canvas = document.getElementById('board');
    ctx = canvas.getContext('2d');
    drawBoard();

    canvas.addEventListener('click', handleBoardClick);
}

function drawBoard() {
    ctx.fillStyle = '#daa520';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.strokeStyle = '#000';
    ctx.lineWidth = 1;

    for (let i = 0; i < BOARD_SIZE; i++) {
        // 가로선
        ctx.beginPath();
        ctx.moveTo(CELL_SIZE, CELL_SIZE + i * CELL_SIZE);
        ctx.lineTo(canvas.width - CELL_SIZE, CELL_SIZE + i * CELL_SIZE);
        ctx.stroke();

        // 세로선
        ctx.beginPath();
        ctx.moveTo(CELL_SIZE + i * CELL_SIZE, CELL_SIZE);
        ctx.lineTo(CELL_SIZE + i * CELL_SIZE, canvas.height - CELL_SIZE);
        ctx.stroke();
    }

    // 화점
    const stars = [3, 9, 15];
    ctx.fillStyle = '#000';
    for (let x of stars) {
        for (let y of stars) {
            ctx.beginPath();
            ctx.arc(CELL_SIZE + x * CELL_SIZE, CELL_SIZE + y * CELL_SIZE, 4, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}

function drawStone(row, col, isMine) {
    const x = CELL_SIZE + col * CELL_SIZE;
    const y = CELL_SIZE + row * CELL_SIZE;

    ctx.beginPath();
    ctx.arc(x, y, STONE_RADIUS, 0, Math.PI * 2);

    const isBlack = (isMine && myStone === 'BLACK') || (!isMine && myStone === 'WHITE');
    ctx.fillStyle = isBlack ? '#000' : '#fff';
    ctx.fill();

    ctx.strokeStyle = '#000';
    ctx.lineWidth = 1;
    ctx.stroke();
}

async function handleBoardClick(event) {
    if (!isMyTurn) {
        alert('상대방의 차례입니다');
        return;
    }

    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const col = Math.round((x - CELL_SIZE) / CELL_SIZE);
    const row = Math.round((y - CELL_SIZE) / CELL_SIZE);

    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
        return;
    }

    try {
        const result = await placeStoneAPI(currentRoomId, row, col);
        drawStone(row, col, true);
        isMyTurn = false;

        if (result.gameStatus === 'FINISHED') {
            showResult(result.winner);
        }
    } catch (error) {
        alert('착수 실패: ' + error.message);
    }
}

function showResult(winner) {
    const modal = document.getElementById('result-modal');
    const resultText = document.getElementById('result-text');

    if (winner === myStone) {
        resultText.textContent = '🎉 승리했습니다!';
    } else if (winner === 'EMPTY') {
        resultText.textContent = '무승부입니다';
    } else {
        resultText.textContent = '😢 패배했습니다';
    }

    modal.style.display = 'flex';
}

function closeResultModal() {
    document.getElementById('result-modal').style.display = 'none';
    leaveGame();
}

async function initGame() {
    drawBoard();

    const roomInfo = await getRoomInfoAPI(currentRoomId);

    document.getElementById('game-room-name').textContent = roomInfo.roomName;
    document.getElementById('my-name').textContent = currentUser.name;

    // 상대방 이름 가져오기
    const opponentId = roomInfo.opponentId;
    // TODO: 유저 정보 API로 상대방 이름 가져오기
    document.getElementById('opponent-name').textContent = '상대방';

    // 내가 방장이면 흑돌
    myStone = roomInfo.myId === roomInfo.hostId ? 'BLACK' : 'WHITE';
    isMyTurn = myStone === 'BLACK';

    updateTurnIndicator();
}

function updateTurnIndicator() {
    const indicator = document.getElementById('turn-indicator');
    indicator.textContent = isMyTurn ? '내 차례' : '상대 차례';
    indicator.style.background = isMyTurn ? '#4CAF50' : '#ff4757';
}
