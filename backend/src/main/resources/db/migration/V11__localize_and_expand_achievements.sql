-- V11: 将成就内容改为中文，并添加更多成就

-- 更新现有成就为中文
UPDATE achievements SET name = '初出茅庐', description = '完成第一次专注学习' WHERE code = 'FIRST_FOCUS';
UPDATE achievements SET name = '渐入佳境', description = '累计专注1小时' WHERE code = 'FOCUS_1H';
UPDATE achievements SET name = '勤奋学子', description = '累计专注10小时' WHERE code = 'FOCUS_10H';
UPDATE achievements SET name = '百时学者', description = '累计专注100小时' WHERE code = 'FOCUS_100H';
UPDATE achievements SET name = '博学大师', description = '累计专注500小时' WHERE code = 'FOCUS_500H';
UPDATE achievements SET name = '永恒学者', description = '累计专注1000小时' WHERE code = 'FOCUS_1000H';
UPDATE achievements SET name = '小试牛刀', description = '连续打卡3天' WHERE code = 'STREAK_3';
UPDATE achievements SET name = '周坚持者', description = '连续打卡7天' WHERE code = 'STREAK_7';
UPDATE achievements SET name = '势不可挡', description = '连续打卡30天' WHERE code = 'STREAK_30';
UPDATE achievements SET name = '传奇人物', description = '连续打卡100天' WHERE code = 'STREAK_100';
UPDATE achievements SET name = '坚持打卡', description = '累计完成10次每日打卡' WHERE code = 'CHECKIN_10';
UPDATE achievements SET name = '习惯大师', description = '累计完成50次每日打卡' WHERE code = 'CHECKIN_50';
UPDATE achievements SET name = '忠诚打卡', description = '累计完成100次每日打卡' WHERE code = 'CHECKIN_100';
UPDATE achievements SET name = '全年坚持', description = '累计完成365次每日打卡' WHERE code = 'CHECKIN_365';
UPDATE achievements SET name = '冉冉新星', description = '达到5级' WHERE code = 'LEVEL_5';
UPDATE achievements SET name = '成长伙伴', description = '达到10级' WHERE code = 'LEVEL_10';
UPDATE achievements SET name = '宠物专家', description = '达到20级' WHERE code = 'LEVEL_20';
UPDATE achievements SET name = '宠物之王', description = '达到50级' WHERE code = 'LEVEL_50';
UPDATE achievements SET name = '日常劳模', description = '连续7天完成任务' WHERE code = 'DAILY_WORKER';
UPDATE achievements SET name = '日常大师', description = '连续30天完成任务' WHERE code = 'DAILY_MASTER';
UPDATE achievements SET name = '宠物爱好者', description = '达到5级' WHERE code = 'PET_LOVER';
UPDATE achievements SET name = '最佳伙伴', description = '达到15级' WHERE code = 'PET_BEST_FRIEND';

-- 新增更多成就
INSERT INTO achievements (code, name, description, category, condition_type, condition_value, icon, exp_reward, token_reward) VALUES
    -- 专注里程碑
    ('FOCUS_30H',       '专注达人',     '累计专注30小时',                'STUDY',   'FOCUS_TOTAL_SEC', 108000,  'medal',        80,  40),
    ('FOCUS_200H',      '学海无涯',     '累计专注200小时',               'STUDY',   'FOCUS_TOTAL_SEC', 720000,  'ocean',        300, 150),
    -- 连续打卡
    ('STREAK_14',       '半月坚持',     '连续打卡14天',                  'STREAK',  'STREAK_DAYS',     14,      'calendar',     40,  20),
    ('STREAK_60',       '两月不懈',     '连续打卡60天',                  'STREAK',  'STREAK_DAYS',     60,      'diamond',      150, 75),
    -- 打卡里程碑
    ('CHECKIN_30',      '月度打卡',     '累计完成30次每日打卡',          'STUDY',   'CHECKIN_COUNT',   30,      'calendar-day', 40,  20),
    -- 等级里程碑
    ('LEVEL_3',         '初露锋芒',     '达到3级',                       'SPECIAL', 'LEVEL',           3,       'seedling',     15,  8),
    ('LEVEL_15',        '成长之路',     '达到15级',                      'SPECIAL', 'LEVEL',           15,      'tree',         120, 60),
    ('LEVEL_30',        '资深伙伴',     '达到30级',                      'SPECIAL', 'LEVEL',           30,      'mountain',     400, 200);
