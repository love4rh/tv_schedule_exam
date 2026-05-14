const API_BASE_URL = 'http://localhost:8282/api/v1'; // 'http://10.157.74.18:8282/api/v1';
let allChannels = []; // 전체 채널 데이터 저장

// 탭 전환 기능
function showTab(tabName) {
    // 모든 탭 비활성화
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 선택된 탭 활성화
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
}

// 채널 목록 조회
async function loadChannels() {
    const container = document.getElementById('channels-list');
    container.innerHTML = '<div class="loading">📡 채널 목록을 불러오는 중...</div>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/channels`);
        const data = await response.json();
        
        if (data.success && data.data.length > 0) {
            allChannels = data.data; // 전체 채널 데이터 저장
            displayChannels(allChannels);
        } else {
            container.innerHTML = '<div class="empty">📭 등록된 채널이 없습니다.</div>';
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 채널 목록을 불러오는데 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 채널 목록 표시
function displayChannels(channels) {
    const container = document.getElementById('channels-list');
    container.innerHTML = channels.map(channel => `
        <div class="channel-item" onclick="selectChannel('${channel.channelId}', '${channel.channelName}')">
            <h3>📺 ${channel.channelName}</h3>
            <div class="channel-info">
                <strong>ID:</strong> ${channel.channelId} | 
                <strong>그룹:</strong> ${channel.channelGroup || '미분류'}
            </div>
        </div>
    `).join('');
}

// 채널 검색 필터링
function filterChannels() {
    const searchTerm = document.getElementById('channelSearch').value.toLowerCase();
    
    if (searchTerm === '') {
        displayChannels(allChannels);
        return;
    }
    
    const filteredChannels = allChannels.filter(channel => 
        channel.channelName.toLowerCase().includes(searchTerm) ||
        (channel.channelGroup && channel.channelGroup.toLowerCase().includes(searchTerm)) ||
        channel.channelId.toLowerCase().includes(searchTerm)
    );
    
    displayChannels(filteredChannels);
    
    if (filteredChannels.length === 0) {
        document.getElementById('channels-list').innerHTML = 
            '<div class="empty">🔍 검색 결과가 없습니다.</div>';
    }
}

// 채널 선택 시 편성표 탭으로 이동
function selectChannel(channelId, channelName) {
    // 편성표 탭으로 전환
    showTabByName('schedule');
    
    // 채널 ID 입력
    document.getElementById('channelId').value = channelId;
    
    // 편성표 자동 로드
    loadSchedule();
    
    // 사용자에게 알림
    const scheduleContainer = document.getElementById('schedule-list');
    scheduleContainer.innerHTML = `<div class="loading">📺 ${channelName} 편성표를 불러오는 중...</div>`;
}

// 탭 전환 (프로그래밍 방식)
function showTabByName(tabName) {
    // 모든 탭 비활성화
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 선택된 탭 활성화
    document.getElementById(tabName).classList.add('active');
    document.querySelector(`[onclick="showTab('${tabName}')"]`).classList.add('active');
}

// 채널 편성표 조회
async function loadSchedule() {
    const channelId = document.getElementById('channelId').value.trim();
    const container = document.getElementById('schedule-list');
    
    if (!channelId) {
        container.innerHTML = '<div class="error">⚠️ 채널 ID를 입력해주세요.</div>';
        return;
    }
    
    container.innerHTML = '<div class="loading">📅 편성표를 불러오는 중...</div>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/channels/${channelId}/schedules`);
        const data = await response.json();
        
        if (data.success && data.data.length > 0) {
            container.innerHTML = data.data.map(schedule => `
                <div class="schedule-item">
                    <div class="schedule-time">
                        🕐 ${formatDate(schedule.startTime)} ${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}
                    </div>
                    <div class="program-title">📺 ${schedule.programName}</div>
                    <div class="program-genre">${schedule.genre || '일반'}</div>
                    ${schedule.summary ? `<div class="program-description">${schedule.summary}</div>` : ''}
                </div>
            `).join('');
        } else if (data.success) {
            container.innerHTML = '<div class="empty">📭 편성표가 없습니다.</div>';
        } else {
            container.innerHTML = `<div class="error">❌ ${data.message}</div>`;
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 편성표를 불러오는데 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 날짜별 편성표 조회
async function loadDateSchedule() {
    const channelId = document.getElementById('dateChannelId').value.trim();
    const date = document.getElementById('scheduleDate').value;
    const container = document.getElementById('date-schedule-list');
    
    if (!channelId) {
        container.innerHTML = '<div class="error">⚠️ 채널 ID를 입력해주세요.</div>';
        return;
    }
    
    if (!date) {
        container.innerHTML = '<div class="error">⚠️ 날짜를 선택해주세요.</div>';
        return;
    }
    
    container.innerHTML = '<div class="loading">📅 편성표를 불러오는 중...</div>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/channels/${channelId}/schedules?date=${date}`);
        const data = await response.json();
        
        if (data.success && data.data.length > 0) {
            container.innerHTML = `
                <h3>📅 ${formatDate(date)} ${data.data[0].channelName} 편성표</h3>
                ${data.data.map(schedule => `
                    <div class="schedule-item">
                        <div class="schedule-time">
                            🕐 ${formatDate(schedule.startTime)} ${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}
                        </div>
                        <div class="program-title">📺 ${schedule.programName}</div>
                        <div class="program-genre">${schedule.genre || '일반'}</div>
                        ${schedule.summary ? `<div class="program-description">${schedule.summary}</div>` : ''}
                    </div>
                `).join('')}
            `;
        } else if (data.success) {
            container.innerHTML = '<div class="empty">📭 해당 날짜에 편성표가 없습니다.</div>';
        } else {
            container.innerHTML = `<div class="error">❌ ${data.message}</div>`;
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 편성표를 불러오는데 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 시간 포맷팅 함수
function formatTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleTimeString('ko-KR', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false 
    });
}

// 날짜 포맷팅 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
    });
}

// 페이지 로드 시 오늘 날짜 설정
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('scheduleDate').value = today;
    
    // 초기 채널 목록 로드
    loadChannels();
});

// Enter 키 이벤트 처리
document.getElementById('channelId').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        loadSchedule();
    }
});

document.getElementById('dateChannelId').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        loadDateSchedule();
    }
});

document.getElementById('scheduleDate').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        loadDateSchedule();
    }
});
