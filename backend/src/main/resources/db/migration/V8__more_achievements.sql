-- V8: Add more achievements

INSERT INTO achievements (code, name, description, category, condition_type, condition_value, icon, exp_reward, token_reward) VALUES
    -- More study milestones
    ('FOCUS_500H',      'Master Learner',      'Accumulate 500 hours of focus time',         'STUDY',   'FOCUS_TOTAL_SEC', 1800000, 'master',       500, 250),
    ('FOCUS_1000H',     'Eternal Scholar',     'Accumulate 1000 hours of focus time',        'STUDY',   'FOCUS_TOTAL_SEC', 3600000, 'eternal',      1000, 500),
    
    -- More checkin achievements
    ('CHECKIN_100',     'Loyal',               'Complete 100 daily check-ins',               'STUDY',   'CHECKIN_COUNT',   100,    'heart',        100, 50),
    ('CHECKIN_365',     'Year Long',           'Complete 365 daily check-ins',               'STUDY',   'CHECKIN_COUNT',   365,    'sun',          500, 200),
    
    -- More level achievements
    ('LEVEL_20',        'Pet Expert',          'Reach pet level 20',                         'SPECIAL', 'LEVEL',           20,     'crown',        200, 100),
    ('LEVEL_50',        'Pet King',            'Reach pet level 50',                         'SPECIAL', 'LEVEL',           50,     'king',         1000, 500),
    
    -- Daily task achievements (using FOCUS_TOTAL_SEC as proxy)
    ('DAILY_WORKER',    'Daily Worker',        'Complete 7 consecutive days of tasks',       'STREAK',  'STREAK_DAYS',     7,      'briefcase',    50,  25),
    ('DAILY_MASTER',    'Daily Master',        'Complete 30 consecutive days of tasks',      'STREAK',  'STREAK_DAYS',     30,     'medal',        200, 100),
    
    -- Pet care achievements (using level as proxy)
    ('PET_LOVER',       'Pet Lover',           'Reach pet level 5',                          'SPECIAL', 'LEVEL',           5,      'paw',          30,  15),
    ('PET_BEST_FRIEND', 'Best Friend',         'Reach pet level 15',                         'SPECIAL', 'LEVEL',           15,     'heart-paw',    150, 75);
