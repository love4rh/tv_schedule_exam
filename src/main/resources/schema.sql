-- 채널 테이블
CREATE TABLE IF NOT EXISTS Channel (
    channel_id VARCHAR(50) PRIMARY KEY,
    channel_name VARCHAR(100) NOT NULL,
    channel_group VARCHAR(50)
);

-- 프로그램 테이블
CREATE TABLE IF NOT EXISTS Program (
    program_id VARCHAR(50) PRIMARY KEY,
    program_name VARCHAR(200) NOT NULL,
    genre VARCHAR(50),
    description TEXT
);

-- 편성표 테이블
CREATE TABLE IF NOT EXISTS Schedule (
    schedule_id VARCHAR(50) PRIMARY KEY,
    channel_id VARCHAR(50) NOT NULL,
    program_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id)
);

-- 즐겨찾기 채널 테이블
CREATE TABLE IF NOT EXISTS favorite_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    channel_id VARCHAR(50) NOT NULL,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id),
    UNIQUE KEY unique_user_channel (user_id, channel_id)
);

-- 샘플 데이터
INSERT IGNORE INTO channels (channel_id, channel_name, channel_group) VALUES
('KBS1', 'KBS 1TV', '지상파'),
('KBS2', 'KBS 2TV', '지상파'),
('MBC', 'MBC', '지상파'),
('SBS', 'SBS', '지상파'),
('TVN', 'tvN', '케이블');

INSERT IGNORE INTO programs (program_name, genre, description) VALUES
('뉴스데스크', '뉴스', 'KBS 메인 뉴스'),
('무한도전', '예능', '인기 예능 프로그램'),
('드라마 스페셜', '드라마', '특별 드라마'),
('아침마당', '교양', '아침 정보 프로그램'),
('스포츠 뉴스', '스포츠', '스포츠 전문 뉴스');

INSERT IGNORE INTO schedules (channel_id, program_id, start_time, end_time) VALUES
('KBS1', 1, '2024-11-25 21:00:00', '2024-11-25 22:00:00'),
('KBS1', 4, '2024-11-25 08:00:00', '2024-11-25 09:00:00'),
('KBS2', 2, '2024-11-25 18:00:00', '2024-11-25 19:00:00'),
('MBC', 3, '2024-11-25 20:00:00', '2024-11-25 21:00:00'),
('SBS', 5, '2024-11-25 23:00:00', '2024-11-25 23:30:00');
