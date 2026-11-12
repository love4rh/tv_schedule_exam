const API_BASE_URL = 'http://localhost:8282/api/v1'; // 'http://10.157.74.18:8282/api/v1';
let allChannels = []; // 전체 채널 데이터 저장

// ============================================================
// 표시용 시간대 설정
// ------------------------------------------------------------
// 서버는 GMT(UTC) 기준으로 시간을 저장/조회/반환한다.
// 화면에 표시할 때만 아래 오프셋(시간 단위)만큼 더해 변환한다.
// KST(한국 표준시)는 GMT+9 이므로 기본값 9.
// 다른 시간대로 보고 싶으면 이 값만 바꾸면 전체 표시가 변경된다.
// ============================================================
const DISPLAY_TZ_OFFSET_HOURS = 9; // GMT 기준 오프셋 (KST = +9)

/**
 * 서버가 내려준 GMT 기준 시각 문자열(예: "2026-11-08T15:35:00")을
 * 표시용 시간대(기본 KST)로 변환한 Date 객체를 돌려준다.
 *
 * 반환된 Date 는 "UTC 필드"에 표시할 벽시계 시각이 담겨 있으므로,
 * 포맷 시 반드시 timeZone:'UTC' 로 출력해야 브라우저 로컬 시간대의
 * 영향 없이 일관되게 표시된다.
 */
function toDisplayDate(serverDateTimeString) {
    if (!serverDateTimeString) return null;
    // 타임존 표기가 없으면 GMT(UTC)로 간주하기 위해 'Z' 를 붙인다.
    let s = String(serverDateTimeString);
    const hasZone = /[zZ]$|[+-]\d{2}:?\d{2}$/.test(s);
    const utcDate = new Date(hasZone ? s : s + 'Z');
    if (isNaN(utcDate.getTime())) return null;
    // 오프셋만큼 이동시켜, UTC 필드가 표시용 벽시계 시각이 되도록 한다.
    return new Date(utcDate.getTime() + DISPLAY_TZ_OFFSET_HOURS * 3600 * 1000);
}

/**
 * 표시용 시간대(KST 등) 기준의 하루(dateStr: "YYYY-MM-DD")를
 * 서버 조회용 GMT 시각 범위 [start, end) 로 변환한다.
 * 반환 형식은 ISO LocalDateTime 문자열("YYYY-MM-DDTHH:mm:ss").
 *
 * 예) KST 2026-11-10 → { start: "2026-11-09T15:00:00", end: "2026-11-10T15:00:00" }
 */
function displayDateToGmtRange(dateStr) {
    // 표시용 자정(그 날 00:00)을 UTC 기준으로 만든 뒤, 오프셋만큼 빼면 GMT 시각이 된다.
    const dayStartUtc = new Date(dateStr + 'T00:00:00Z').getTime();
    const startGmt = new Date(dayStartUtc - DISPLAY_TZ_OFFSET_HOURS * 3600 * 1000);
    const endGmt = new Date(startGmt.getTime() + 24 * 3600 * 1000);
    return { start: toIsoLocal(startGmt), end: toIsoLocal(endGmt) };
}

// Date 의 UTC 필드를 "YYYY-MM-DDTHH:mm:ss" (타임존 표기 없는 ISO LocalDateTime) 로 변환
function toIsoLocal(date) {
    const p = n => String(n).padStart(2, '0');
    return `${date.getUTCFullYear()}-${p(date.getUTCMonth() + 1)}-${p(date.getUTCDate())}` +
           `T${p(date.getUTCHours())}:${p(date.getUTCMinutes())}:${p(date.getUTCSeconds())}`;
}

/**
 * 날짜 입력(type=date)의 값을 deltaDays 만큼 이동시킨 뒤,
 * 지정한 조회 함수(reloadFn)를 호출해 바로 편성표를 다시 조회한다.
 * 값이 비어 있으면 오늘 날짜를 기준으로 설정한다.
 */
function shiftDate(inputId, deltaDays, reloadFn) {
    const input = document.getElementById(inputId);
    if (!input) return;

    const base = input.value
        ? new Date(input.value + 'T00:00:00Z')
        : new Date(new Date().toISOString().split('T')[0] + 'T00:00:00Z');
    base.setUTCDate(base.getUTCDate() + deltaDays);

    const p = n => String(n).padStart(2, '0');
    input.value = `${base.getUTCFullYear()}-${p(base.getUTCMonth() + 1)}-${p(base.getUTCDate())}`;

    if (typeof reloadFn === 'function') {
        reloadFn();
    }
}

// 탭 전환 기능
function showTab(tabName, btnEl) {
    // 모든 탭 비활성화
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 선택된 탭 활성화
    document.getElementById(tabName).classList.add('active');
    const activeBtn = btnEl || (typeof event !== 'undefined' && event ? event.target.closest('.nav-btn') : null);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
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

    // 채널 선택 상태 반영 (검색 입력 + 숨김 필드)
    setPickerSelection('schedule', channelId, channelName);

    // 편성표 자동 로드
    loadSchedule();

    // 사용자에게 알림
    const scheduleContainer = document.getElementById('schedule-list');
    scheduleContainer.innerHTML = `<div class="loading">📺 ${channelName} 편성표를 불러오는 중...</div>`;
}

// ============================================================
// 채널 선택기 (채널명 입력 → 200ms 디바운스 자동 검색 → 옵션 리스트 선택)
// ============================================================
// 탭별 엘리먼트 ID 매핑
const CHANNEL_PICKER = {
    'schedule':      { search: 'channelNameSearch',     options: 'channelOptions',     hidden: 'channelId' },
    'date-schedule': { search: 'dateChannelNameSearch', options: 'dateChannelOptions', hidden: 'dateChannelId' }
};

// 탭별 디바운스 타이머
const channelPickerTimers = {};

// 입력 이벤트: 200ms 동안 변화가 없으면 검색 실행
function onChannelPickerInput(tabName) {
    const cfg = CHANNEL_PICKER[tabName];

    // 입력이 바뀌면 이전 선택(채널 ID)은 무효화
    document.getElementById(cfg.hidden).value = '';

    // 이전 타이머 취소 후 재설정
    if (channelPickerTimers[tabName]) {
        clearTimeout(channelPickerTimers[tabName]);
    }
    channelPickerTimers[tabName] = setTimeout(() => {
        searchChannelPicker(tabName);
    }, 200);
}

// 채널명(또는 ID) 문자열로 검색하여 옵션 리스트를 채움
async function searchChannelPicker(tabName) {
    const cfg = CHANNEL_PICKER[tabName];
    const keyword = document.getElementById(cfg.search).value.trim();
    const optionsEl = document.getElementById(cfg.options);

    if (!keyword) {
        hideChannelOptions(tabName);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/channels/search?keyword=${encodeURIComponent(keyword)}`);
        const data = await response.json();

        // 응답 도착 전 입력이 비워졌으면 무시
        if (!document.getElementById(cfg.search).value.trim()) {
            hideChannelOptions(tabName);
            return;
        }

        if (data.success && data.data.length > 0) {
            optionsEl.innerHTML = data.data.map(ch => `
                <div class="channel-option" onclick="pickChannelOption('${tabName}', '${ch.channelId}', '${escapeAttr(ch.channelName)}')">
                    ${ch.channelName} <span class="channel-option-id">(${ch.channelId})</span>
                </div>
            `).join('');
            optionsEl.classList.add('open');
        } else if (data.success) {
            optionsEl.innerHTML = '<div class="channel-option empty-option">검색 결과가 없습니다</div>';
            optionsEl.classList.add('open');
        } else {
            optionsEl.innerHTML = '<div class="channel-option empty-option">검색에 실패했습니다</div>';
            optionsEl.classList.add('open');
        }
    } catch (error) {
        optionsEl.innerHTML = '<div class="channel-option empty-option">검색에 실패했습니다</div>';
        optionsEl.classList.add('open');
        console.error('Error:', error);
    }
}

// 옵션 리스트에서 채널 선택
function pickChannelOption(tabName, channelId, channelName) {
    const cfg = CHANNEL_PICKER[tabName];
    document.getElementById(cfg.search).value = `${channelName} (${channelId})`;
    document.getElementById(cfg.hidden).value = channelId;
    hideChannelOptions(tabName);
}

// 옵션 리스트 닫기
function hideChannelOptions(tabName) {
    const cfg = CHANNEL_PICKER[tabName];
    const optionsEl = document.getElementById(cfg.options);
    optionsEl.classList.remove('open');
    optionsEl.innerHTML = '';
}

// 특정 채널을 선택기에 세팅 (채널 목록에서 클릭했을 때 등)
function setPickerSelection(tabName, channelId, channelName) {
    const cfg = CHANNEL_PICKER[tabName];
    document.getElementById(cfg.search).value = `${channelName} (${channelId})`;
    document.getElementById(cfg.hidden).value = channelId;
    hideChannelOptions(tabName);
}

// 속성 값에 들어가는 문자열의 따옴표 이스케이프
function escapeAttr(str) {
    return String(str).replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

// 탭 전환 (프로그래밍 방식)
function showTabByName(tabName) {
    // 모든 탭 비활성화
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 선택된 탭 활성화
    document.getElementById(tabName).classList.add('active');
    const btn = document.querySelector(`.nav-btn[onclick^="showTab('${tabName}'"]`);
    if (btn) {
        btn.classList.add('active');
    }
}

// 채널 편성표 조회
async function loadSchedule() {
    const channelId = document.getElementById('channelId').value.trim();
    const container = document.getElementById('schedule-list');
    
    if (!channelId) {
        container.innerHTML = '<div class="error">⚠️ 채널을 검색하여 선택해주세요.</div>';
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
        container.innerHTML = '<div class="error">⚠️ 채널을 검색하여 선택해주세요.</div>';
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

// ============================================================
// 즐겨찾기 채널 편집 기능
// ============================================================
let currentFavUserId = ''; // 현재 조회 중인 사용자 ID

// 사용자의 즐겨찾기 목록 조회
async function loadFavorites() {
    const userId = document.getElementById('favUserId').value.trim();
    const container = document.getElementById('favorites-list');
    const actions = document.getElementById('favActions');

    if (!userId) {
        container.innerHTML = '<div class="error">⚠️ 사용자 ID를 입력해주세요.</div>';
        actions.style.display = 'none';
        return;
    }

    currentFavUserId = userId;
    container.innerHTML = '<div class="loading">⭐ 즐겨찾기 목록을 불러오는 중...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/users/${encodeURIComponent(userId)}/favorite-channels/with-details`);
        const data = await response.json();

        if (data.success) {
            actions.style.display = 'flex';
            displayFavorites(data.data);
        } else {
            container.innerHTML = `<div class="error">❌ ${data.message || '조회에 실패했습니다.'}</div>`;
            actions.style.display = 'none';
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 즐겨찾기 목록을 불러오는데 실패했습니다.</div>';
        actions.style.display = 'none';
        console.error('Error:', error);
    }
}

// 즐겨찾기 목록 표시 (체크박스로 다중 선택 가능)
function displayFavorites(favorites) {
    const container = document.getElementById('favorites-list');

    if (!favorites || favorites.length === 0) {
        container.innerHTML = `<div class="empty">📭 <strong>${currentFavUserId}</strong> 님의 즐겨찾기 채널이 없습니다. "채널 추가"로 등록해보세요.</div>`;
        return;
    }

    container.innerHTML = `
        <table class="fav-table">
            <thead>
                <tr>
                    <th style="width:48px;"><input type="checkbox" onclick="toggleAllFavorites(this)"></th>
                    <th>채널 ID</th>
                    <th>채널명</th>
                    <th>그룹</th>
                </tr>
            </thead>
            <tbody>
                ${favorites.map(fav => `
                    <tr>
                        <td><input type="checkbox" class="fav-check" value="${fav.channelId}"></td>
                        <td>${fav.channelId}</td>
                        <td>📺 ${fav.channelName}</td>
                        <td><span class="group-badge">${fav.channelGroup || '미분류'}</span></td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

// 전체 선택/해제
function toggleAllFavorites(headerCheckbox) {
    document.querySelectorAll('.fav-check').forEach(cb => {
        cb.checked = headerCheckbox.checked;
    });
}

// 선택된 즐겨찾기 채널 삭제
async function deleteSelectedFavorites() {
    const selected = Array.from(document.querySelectorAll('.fav-check:checked')).map(cb => cb.value);

    if (selected.length === 0) {
        alert('삭제할 채널을 선택해주세요.');
        return;
    }

    if (!confirm(`선택한 ${selected.length}개 채널을 즐겨찾기에서 삭제하시겠습니까?`)) {
        return;
    }

    try {
        await Promise.all(selected.map(channelId =>
            fetch(`${API_BASE_URL}/users/${encodeURIComponent(currentFavUserId)}/favorite-channels/${encodeURIComponent(channelId)}`, {
                method: 'DELETE'
            })
        ));
        // 목록 새로고침
        loadFavorites();
    } catch (error) {
        alert('삭제 중 오류가 발생했습니다.');
        console.error('Error:', error);
    }
}

// ============================================================
// 채널 검색 팝업 (즐겨찾기 추가)
// ============================================================
function openChannelSearchModal() {
    if (!currentFavUserId) {
        alert('먼저 사용자 ID를 조회해주세요.');
        return;
    }
    document.getElementById('channelSearchModal').classList.add('open');
    document.getElementById('channelSearchKeyword').value = '';
    const statusEl = document.getElementById('channelAddStatus');
    if (statusEl) { statusEl.textContent = ''; statusEl.className = 'modal-status'; }
    document.getElementById('channelSearchResults').innerHTML =
        '<div class="empty">채널명, 그룹명 또는 ID로 검색하세요.</div>';
    document.getElementById('channelSearchKeyword').focus();
}

function closeChannelSearchModal() {
    document.getElementById('channelSearchModal').classList.remove('open');
}

function handleChannelSearchKeyup(e) {
    if (e.key === 'Enter') {
        searchChannelsForAdd();
    }
}

// 채널 검색 API 호출
async function searchChannelsForAdd() {
    const keyword = document.getElementById('channelSearchKeyword').value.trim();
    const resultsEl = document.getElementById('channelSearchResults');

    if (!keyword) {
        resultsEl.innerHTML = '<div class="error">⚠️ 검색어를 입력해주세요.</div>';
        return;
    }

    resultsEl.innerHTML = '<div class="loading">🔍 검색 중...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/channels/search?keyword=${encodeURIComponent(keyword)}&includeGroup=true`);
        const data = await response.json();

        if (data.success && data.data.length > 0) {
            resultsEl.innerHTML = data.data.map(ch => `
                <div class="search-result-item">
                    <div class="search-result-info">
                        <strong>📺 ${ch.channelName}</strong>
                        <span class="search-result-sub">${ch.channelId} · ${ch.channelGroup || '미분류'}</span>
                    </div>
                    <button class="btn btn-add btn-sm" onclick="addFavorite('${ch.channelId}')">추가</button>
                </div>
            `).join('');
        } else if (data.success) {
            resultsEl.innerHTML = '<div class="empty">🔍 검색 결과가 없습니다.</div>';
        } else {
            resultsEl.innerHTML = `<div class="error">❌ ${data.message}</div>`;
        }
    } catch (error) {
        resultsEl.innerHTML = '<div class="error">❌ 검색에 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 즐겨찾기 추가 (팝업을 닫지 않고 연속 추가 가능)
async function addFavorite(channelId) {
    const statusEl = document.getElementById('channelAddStatus');
    try {
        const response = await fetch(`${API_BASE_URL}/users/${encodeURIComponent(currentFavUserId)}/favorite-channels`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ channelId: channelId })
        });

        if (response.status === 409) {
            // 이미 추가된 채널
            showAddStatus(`⚠️ 이미 추가된 채널입니다 (${channelId})`, 'warn');
        } else if (response.ok) {
            showAddStatus(`✅ 추가되었습니다 (${channelId})`, 'ok');
            // 뒤쪽 즐겨찾기 목록도 최신 상태로 갱신 (팝업은 유지)
            loadFavorites();
        } else {
            let message = '추가에 실패했습니다.';
            try {
                const data = await response.json();
                message = (data.error && data.error.message) || data.message || message;
            } catch (_) { /* 응답 파싱 실패 시 기본 메시지 */ }
            showAddStatus(`❌ ${message}`, 'err');
        }
    } catch (error) {
        showAddStatus('❌ 추가 중 오류가 발생했습니다.', 'err');
        console.error('Error:', error);
    }
}

// 팝업 상단에 추가 결과 메시지 표시
function showAddStatus(message, type) {
    const statusEl = document.getElementById('channelAddStatus');
    if (!statusEl) return;
    const cls = type === 'ok' ? 'status-ok' : (type === 'warn' ? 'status-warn' : 'status-err');
    statusEl.className = `modal-status ${cls}`;
    statusEl.textContent = message;
}

// ============================================================
// 사용자 TV 편성표 (타임라인 그리드)
// ============================================================
const GUIDE_START_HOUR = 0;   // 표시 시작 시각 (0시)
const GUIDE_END_HOUR = 24;    // 표시 종료 시각 (24시)
const GUIDE_PX_PER_HOUR = 120; // 1시간당 픽셀 너비

async function loadUserGuide() {
    const userId = document.getElementById('guideUserId').value.trim();
    const date = document.getElementById('guideDate').value;
    const container = document.getElementById('user-guide-view');

    if (!userId) {
        container.innerHTML = '<div class="error">⚠️ 사용자 ID를 입력해주세요.</div>';
        return;
    }
    if (!date) {
        container.innerHTML = '<div class="error">⚠️ 날짜를 선택해주세요.</div>';
        return;
    }

    container.innerHTML = '<div class="loading">🗓️ 편성표를 불러오는 중...</div>';

    try {
        // 표시용(KST) 날짜를 서버 조회용 GMT 시각 범위로 변환하여 요청
        const range = displayDateToGmtRange(date);
        const response = await fetch(`${API_BASE_URL}/schedules/users/${encodeURIComponent(userId)}/schedules/with-details/range`
            + `?start=${encodeURIComponent(range.start)}&end=${encodeURIComponent(range.end)}`);
        const data = await response.json();

        if (data.success && data.data.length > 0) {
            renderUserGuide(data.data, date);
        } else if (data.success) {
            container.innerHTML = '<div class="empty">📭 해당 날짜에 즐겨찾기 채널의 편성표가 없습니다.</div>';
        } else {
            container.innerHTML = `<div class="error">❌ ${data.message}</div>`;
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 편성표를 불러오는데 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 편성표를 채널별 타임라인 그리드로 렌더링
function renderUserGuide(schedules, dateStr) {
    const container = document.getElementById('user-guide-view');

    // 채널별로 그룹화 (순서 유지)
    const channelMap = new Map();
    schedules.forEach(s => {
        if (!channelMap.has(s.channelId)) {
            channelMap.set(s.channelId, { channelId: s.channelId, channelName: s.channelName, items: [] });
        }
        channelMap.get(s.channelId).items.push(s);
    });

    const totalHours = GUIDE_END_HOUR - GUIDE_START_HOUR;
    const timelineWidth = totalHours * GUIDE_PX_PER_HOUR;
    // 선택한 날짜의 자정을 UTC 기준으로 잡는다.
    // (프로그램 시각도 toDisplayDate 로 변환하면 UTC 필드가 표시용 벽시계 시각이 되므로
    //  둘을 같은 공간에서 비교할 수 있다.)
    const baseDate = new Date(dateStr + 'T00:00:00Z');

    // 시간 눈금 헤더
    let hourMarkers = '';
    for (let h = GUIDE_START_HOUR; h < GUIDE_END_HOUR; h++) {
        hourMarkers += `<div class="guide-hour" style="width:${GUIDE_PX_PER_HOUR}px;">${String(h).padStart(2, '0')}:00</div>`;
    }

    // 날짜 기준으로 특정 시각의 x 위치(px)를 계산 (표시용 시간대로 변환하여 비교)
    function timeToX(dateTimeStr) {
        const t = toDisplayDate(dateTimeStr);
        if (!t) return 0;
        let hoursFromBase = (t - baseDate) / (1000 * 60 * 60);
        // 조회 날짜 범위 밖이면 경계로 클램프
        if (hoursFromBase < GUIDE_START_HOUR) hoursFromBase = GUIDE_START_HOUR;
        if (hoursFromBase > GUIDE_END_HOUR) hoursFromBase = GUIDE_END_HOUR;
        return (hoursFromBase - GUIDE_START_HOUR) * GUIDE_PX_PER_HOUR;
    }

    // 채널별 행 생성
    let rows = '';
    channelMap.forEach(channel => {
        let blocks = '';
        channel.items.forEach(item => {
            const left = timeToX(item.startTime);
            const right = timeToX(item.endTime);
            const width = Math.max(right - left, 2); // 최소 너비 보장
            blocks += `
                <div class="guide-program" style="left:${left}px; width:${width}px;"
                     title="${item.programName} (${formatTime(item.startTime)} - ${formatTime(item.endTime)})">
                    <div class="guide-program-title">${item.programName}</div>
                    <div class="guide-program-time">${formatTime(item.startTime)} - ${formatTime(item.endTime)}</div>
                </div>`;
        });
        rows += `
            <div class="guide-row">
                <div class="guide-channel-label">
                    <strong>${channel.channelName}</strong>
                    <span class="guide-channel-id">${channel.channelId}</span>
                </div>
                <div class="guide-track" style="width:${timelineWidth}px;">
                    ${blocks}
                </div>
            </div>`;
    });

    container.innerHTML = `
        <h3>🗓️ ${formatDate(dateStr)} 편성표</h3>
        <div class="guide-scroll">
            <div class="guide-grid">
                <div class="guide-header-row">
                    <div class="guide-channel-label guide-corner">채널 \\ 시간</div>
                    <div class="guide-hours" style="width:${timelineWidth}px;">${hourMarkers}</div>
                </div>
                ${rows}
            </div>
        </div>
    `;
}

// ============================================================
// 현재/다음 방송 (seq_num 기반)
// ============================================================
async function loadCurrentNext() {
    const userId = document.getElementById('cnUserId').value.trim();
    const container = document.getElementById('current-next-view');

    if (!userId) {
        container.innerHTML = '<div class="error">⚠️ 사용자 ID를 입력해주세요.</div>';
        return;
    }

    container.innerHTML = '<div class="loading">⏱️ 현재/다음 방송을 불러오는 중...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/schedules/users/${encodeURIComponent(userId)}/current-next`);
        const data = await response.json();

        if (data.success && data.data.length > 0) {
            renderCurrentNext(data.data, userId);
        } else if (data.success) {
            container.innerHTML = `<div class="empty">📭 <strong>${userId}</strong> 님의 즐겨찾기 채널이 없습니다.</div>`;
        } else {
            container.innerHTML = `<div class="error">❌ ${data.message}</div>`;
        }
    } catch (error) {
        container.innerHTML = '<div class="error">❌ 현재/다음 방송을 불러오는데 실패했습니다.</div>';
        console.error('Error:', error);
    }
}

// 채널별 현재/다음 방송 카드 렌더링
function renderCurrentNext(items, userId) {
    const container = document.getElementById('current-next-view');

    // 한 방송(현재/다음) 셀 내용 HTML
    function programCell(sch) {
        if (!sch) {
            return '<span class="cn-empty">편성 정보 없음</span>';
        }
        return `
            <div class="cn-cell-title">${sch.programName}</div>
            <div class="cn-cell-time">🕐 ${formatTime(sch.startTime)} - ${formatTime(sch.endTime)}</div>
            ${sch.genre ? `<div class="cn-cell-genre">${sch.genre}</div>` : ''}
        `;
    }

    const rows = items.map(ch => `
        <tr>
            <td class="cn-td-channel">📺 ${ch.channelName}</td>
            <td><span class="group-badge">${ch.channelGroup || '미분류'}</span></td>
            <td class="cn-td-current">${programCell(ch.current)}</td>
            <td class="cn-td-next">${programCell(ch.next)}</td>
        </tr>
    `).join('');

    container.innerHTML = `
        <div class="cn-header">👤 <strong>${userId}</strong> 님의 즐겨찾기 채널 · 기준 시각 ${formatTime(new Date().toISOString())}</div>
        <table class="cn-table">
            <thead>
                <tr>
                    <th>채널명</th>
                    <th>그룹명</th>
                    <th>지금 방송</th>
                    <th>다음 방송</th>
                </tr>
            </thead>
            <tbody>${rows}</tbody>
        </table>
    `;
}

// 시간 포맷팅 함수
function formatTime(dateTimeString) {
    const date = toDisplayDate(dateTimeString);
    if (!date) return '';
    return date.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: 'UTC' // toDisplayDate 에서 이미 오프셋을 적용했으므로 UTC 로 출력
    });
}

// 날짜 포맷팅 함수
function formatDate(dateString) {
    const date = toDisplayDate(dateString);
    if (!date) return '';
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long',
        timeZone: 'UTC'
    });
}

// 페이지 로드 시 오늘 날짜 설정
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    const scheduleDateEl = document.getElementById('scheduleDate');
    if (scheduleDateEl) scheduleDateEl.value = today;

    const guideDateEl = document.getElementById('guideDate');
    if (guideDateEl) guideDateEl.value = today;

    // 초기 채널 목록 로드
    loadChannels();

    // 모달 바깥 영역 클릭 시 닫기
    const modal = document.getElementById('channelSearchModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeChannelSearchModal();
            }
        });
    }

    // 채널 선택기 바깥 클릭 시 옵션 리스트 닫기
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.channel-picker')) {
            Object.keys(CHANNEL_PICKER).forEach(tabName => hideChannelOptions(tabName));
        }
    });
});

// Enter 키 이벤트 처리 (해당 요소가 존재할 때만 바인딩)
function bindEnter(elementId, handler) {
    const el = document.getElementById(elementId);
    if (el) {
        el.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') handler();
        });
    }
}

bindEnter('scheduleDate', loadDateSchedule);
bindEnter('favUserId', loadFavorites);
bindEnter('guideUserId', loadUserGuide);
bindEnter('guideDate', loadUserGuide);
bindEnter('cnUserId', loadCurrentNext);
