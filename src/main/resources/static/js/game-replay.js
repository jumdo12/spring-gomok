// 상수
const BOARD_SIZE = 19;
const CELL_SIZE = 30;
const STONE_RADIUS = 13;

// DOM 요소
const canvas = document.getElementById('replayBoard');
const ctx = canvas.getContext('2d');
const gameIdDisplay = document.getElementById('gameIdDisplay');
const moveInfo = document.getElementById('moveInfo');
const moveSlider = document.getElementById('moveSlider');
const firstBtn = document.getElementById('firstBtn');
const prevBtn = document.getElementById('prevBtn');
const playBtn = document.getElementById('playBtn');
const nextBtn = document.getElementById('nextBtn');
const lastBtn = document.getElementById('lastBtn');
const backBtn = document.getElementById('backBtn');
const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorMessage');
const closeModalBtn = document.getElementById('closeModalBtn');

// 게임 상태
let gameId = null;
let placeResults = []; // 착수 기록 배열
let currentMoveIndex = 0; // 현재 표시 중인 착수 인덱스 (0: 빈 판, 1~: 착수 순서)
let board = Array(BOARD_SIZE).fill(null).map(() => Array(BOARD_SIZE).fill('EMPTY'));
let isPlaying = false; // 자동 재생 중인지 여부
let playInterval = null; // 자동 재생 타이머

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

// URL에서 게임 ID 추출
function getGameIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('gameId');
}

// 게임 기록 조회
async function loadGameRecord() {
    try {
        const response = await fetch(`/api/games/${gameId}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '게임 기록을 불러올 수 없습니다.' }));
            throw new Error(errorData.message || '게임 기록을 불러올 수 없습니다.');
        }

        placeResults = await response.json();

        // 착수 순서대로 정렬
        placeResults.sort((a, b) => a.moveOrder - b.moveOrder);

        // 슬라이더 설정
        moveSlider.max = placeResults.length;
        moveSlider.value = 0;

        // 초기 화면 렌더링
        gameIdDisplay.textContent = `게임 ID: ${gameId}`;
        updateMoveInfo();
        drawBoard();
    } catch (error) {
        showError(error.message);
    }
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

    // 화점
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
    for (let i = 0; i < currentMoveIndex; i++) {
        const move = placeResults[i];
        const isLastMove = (i === currentMoveIndex - 1);
        drawStone(move.placeRow, move.placeCol, move.stone, isLastMove);
    }
}

// 돌 그리기
function drawStone(row, col, color, isLastMove = false) {
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

    // 마지막 착수 위치 표시 (노란색 테두리)
    if (isLastMove) {
        ctx.beginPath();
        ctx.arc(x, y, STONE_RADIUS + 2, 0, 2 * Math.PI);
        ctx.strokeStyle = 'rgba(255, 215, 0, 0.8)';
        ctx.lineWidth = 3;
        ctx.stroke();
    }
}

// 착수 정보 업데이트
function updateMoveInfo() {
    if (currentMoveIndex === 0) {
        moveInfo.textContent = '게임 시작 전';
    } else if (currentMoveIndex <= placeResults.length) {
        const move = placeResults[currentMoveIndex - 1];
        const stoneText = move.stone === 'BLACK' ? '흑돌 ⚫' : '백돌 ⚪';
        moveInfo.textContent = `${currentMoveIndex}수 - ${stoneText} (${move.placeRow}, ${move.placeCol})`;
    }
}

// 특정 착수로 이동
function goToMove(moveIndex) {
    currentMoveIndex = Math.max(0, Math.min(moveIndex, placeResults.length));
    moveSlider.value = currentMoveIndex;
    updateMoveInfo();
    drawBoard();
}

// 처음으로
function goToFirst() {
    stopAutoPlay();
    goToMove(0);
}

// 이전 착수
function goToPrev() {
    stopAutoPlay();
    goToMove(currentMoveIndex - 1);
}

// 다음 착수
function goToNext() {
    stopAutoPlay();
    goToMove(currentMoveIndex + 1);
}

// 마지막으로
function goToLast() {
    stopAutoPlay();
    goToMove(placeResults.length);
}

// 자동 재생 시작/중지
function toggleAutoPlay() {
    if (isPlaying) {
        stopAutoPlay();
    } else {
        startAutoPlay();
    }
}

// 자동 재생 시작
function startAutoPlay() {
    if (currentMoveIndex >= placeResults.length) {
        currentMoveIndex = 0;
    }

    isPlaying = true;
    playBtn.textContent = '⏸ 일시정지';

    playInterval = setInterval(() => {
        if (currentMoveIndex >= placeResults.length) {
            stopAutoPlay();
            return;
        }
        goToMove(currentMoveIndex + 1);
    }, 800); // 0.8초마다 다음 착수
}

// 자동 재생 중지
function stopAutoPlay() {
    isPlaying = false;
    playBtn.textContent = '▶ 재생';

    if (playInterval) {
        clearInterval(playInterval);
        playInterval = null;
    }
}

// 슬라이더 변경
function onSliderChange() {
    stopAutoPlay();
    goToMove(parseInt(moveSlider.value));
}

// 전적 목록으로 돌아가기
function goBack() {
    stopAutoPlay();
    window.location.href = '/game-history';
}

// 이벤트 리스너 등록
firstBtn.addEventListener('click', goToFirst);
prevBtn.addEventListener('click', goToPrev);
playBtn.addEventListener('click', toggleAutoPlay);
nextBtn.addEventListener('click', goToNext);
lastBtn.addEventListener('click', goToLast);
moveSlider.addEventListener('input', onSliderChange);
backBtn.addEventListener('click', goBack);
closeModalBtn.addEventListener('click', closeModal);

// 모달 외부 클릭 시 닫기
errorModal.addEventListener('click', (event) => {
    if (event.target === errorModal) {
        closeModal();
    }
});

// 페이지 로드 시 실행
gameId = getGameIdFromUrl();
if (!gameId) {
    showError('게임 ID가 없습니다.');
} else {
    loadGameRecord();
}

// 페이지 언로드 시 타이머 정리
window.addEventListener('beforeunload', () => {
    stopAutoPlay();
});
