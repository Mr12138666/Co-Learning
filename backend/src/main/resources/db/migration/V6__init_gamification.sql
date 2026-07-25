-- V6: Gamification module - experience, tokens, pets, achievements

-- user_exp: user experience points, level, and token balance
CREATE TABLE user_exp (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    total_exp       INT NOT NULL DEFAULT 0,
    level           INT NOT NULL DEFAULT 1,
    tokens          INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- pets: each user has one companion pet
CREATE TABLE pets (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name                VARCHAR(50) NOT NULL,
    species             VARCHAR(30) NOT NULL DEFAULT 'CAT',  -- CAT | DOG | RABBIT | OWL
    level               INT NOT NULL DEFAULT 1,
    exp                 INT NOT NULL DEFAULT 0,
    mood                INT NOT NULL DEFAULT 100,            -- 0-100, decreases over time
    hunger              INT NOT NULL DEFAULT 100,            -- 0-100, decreases over time
    last_fed_at         TIMESTAMPTZ,
    last_interacted_at  TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pets_user ON pets(user_id);

-- pet_items: shop catalog of items for pets
CREATE TABLE pet_items (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    item_type       VARCHAR(20) NOT NULL,                    -- FOOD | TOY | ACCESSORY
    effect_type     VARCHAR(30) NOT NULL,                    -- MOOD_BOOST | HUNGER_RESTORE | EXP_BOOST
    effect_value    INT NOT NULL DEFAULT 0,
    price           INT NOT NULL DEFAULT 0,                  -- in tokens
    icon            VARCHAR(50),                             -- emoji or icon name
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- user_items: items owned by users
CREATE TABLE user_items (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id         BIGINT NOT NULL REFERENCES pet_items(id) ON DELETE CASCADE,
    quantity        INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, item_id)
);
CREATE INDEX idx_user_items_user ON user_items(user_id);

-- achievements: achievement definitions
CREATE TABLE achievements (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,         -- e.g., FIRST_FOCUS, STREAK_7
    name                VARCHAR(100) NOT NULL,
    description         VARCHAR(500) NOT NULL,
    category            VARCHAR(30) NOT NULL,                -- STUDY | STREAK | SOCIAL | SPECIAL
    condition_type      VARCHAR(50) NOT NULL,                -- FOCUS_TOTAL_SEC | STREAK_DAYS | CHECKIN_COUNT | LEVEL
    condition_value     INT NOT NULL,
    icon                VARCHAR(50),                         -- emoji
    exp_reward          INT NOT NULL DEFAULT 0,
    token_reward        INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- user_achievements: achievements unlocked by users
CREATE TABLE user_achievements (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id      BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    unlocked_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, achievement_id)
);
CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);

-- ===== Seed data: pet items shop =====
INSERT INTO pet_items (name, description, item_type, effect_type, effect_value, price, icon) VALUES
    ('Fish Snack',      'A tasty fish snack that restores hunger',  'FOOD',      'HUNGER_RESTORE', 30,  5,  'fish'),
    ('Premium Meal',    'A nutritious premium meal',                'FOOD',      'HUNGER_RESTORE', 60,  15, 'rice'),
    ('Feather Toy',     'A fun feather toy to play with',           'TOY',       'MOOD_BOOST',     30,  8,  'feather'),
    ('Ball Toy',        'A bouncy ball toy',                        'TOY',       'MOOD_BOOST',     50,  20, 'ball'),
    ('Exp Potion',      'A magical potion that grants pet EXP',     'ACCESSORY', 'EXP_BOOST',      100, 30, 'potion'),
    ('Mega Feast',      'A grand feast that fully restores hunger', 'FOOD',      'HUNGER_RESTORE', 100, 40, 'feast'),
    ('Lucky Charm',     'A charm that boosts mood significantly',   'ACCESSORY', 'MOOD_BOOST',     80,  35, 'charm'),
    ('Exp Catalyst',    'A rare catalyst for massive EXP gain',     'ACCESSORY', 'EXP_BOOST',      500, 100,'catalyst');

-- ===== Seed data: achievements =====
INSERT INTO achievements (code, name, description, category, condition_type, condition_value, icon, exp_reward, token_reward) VALUES
    -- Study achievements
    ('FIRST_FOCUS',     'First Steps',         'Complete your first focus session',           'STUDY',   'FOCUS_TOTAL_SEC', 600,    'baby',         10,  5),
    ('FOCUS_1H',        'Getting Started',     'Accumulate 1 hour of focus time',             'STUDY',   'FOCUS_TOTAL_SEC', 3600,   'clock',        20,  10),
    ('FOCUS_10H',       'Dedicated Learner',   'Accumulate 10 hours of focus time',           'STUDY',   'FOCUS_TOTAL_SEC', 36000,  'book',         50,  25),
    ('FOCUS_100H',      'Century Scholar',     'Accumulate 100 hours of focus time',          'STUDY',   'FOCUS_TOTAL_SEC', 360000, 'graduation',   200, 100),
    -- Streak achievements
    ('STREAK_3',        'On a Roll',           'Maintain a 3-day check-in streak',            'STREAK',  'STREAK_DAYS',     3,      'fire',         15,  8),
    ('STREAK_7',        'Weekly Warrior',      'Maintain a 7-day check-in streak',            'STREAK',  'STREAK_DAYS',     7,      'flame',        30,  15),
    ('STREAK_30',       'Unstoppable',         'Maintain a 30-day check-in streak',           'STREAK',  'STREAK_DAYS',     30,     'crown',        100, 50),
    ('STREAK_100',      'Legend',              'Maintain a 100-day check-in streak',          'STREAK',  'STREAK_DAYS',     100,    'trophy',       500, 200),
    -- Checkin achievements
    ('CHECKIN_10',      'Consistent',          'Complete 10 daily check-ins',                 'STUDY',   'CHECKIN_COUNT',   10,     'calendar',     20,  10),
    ('CHECKIN_50',      'Habit Master',        'Complete 50 daily check-ins',                 'STUDY',   'CHECKIN_COUNT',   50,     'calendar-check',50, 25),
    -- Level achievements
    ('LEVEL_5',         'Rising Star',         'Reach pet level 5',                           'SPECIAL', 'LEVEL',           5,      'star',         30,  15),
    ('LEVEL_10',        'Pet Whisperer',       'Reach pet level 10',                          'SPECIAL', 'LEVEL',           10,     'star-fill',    80,  40);
