-- 将宠物商店商品中文化，并让大份道具更具性价比。
UPDATE pet_items
SET name = CASE id
        WHEN 1 THEN '香酥小鱼干'
        WHEN 2 THEN '营养猫饭'
        WHEN 3 THEN '逗猫羽毛'
        WHEN 4 THEN '弹力玩具球'
        WHEN 5 THEN '经验药水'
        WHEN 6 THEN '豪华盛宴'
        WHEN 7 THEN '幸运护符'
        WHEN 8 THEN '经验催化剂'
    END,
    description = CASE id
        WHEN 1 THEN '香酥可口的小鱼干，恢复30点饱食度'
        WHEN 2 THEN '营养均衡的美味猫饭，恢复60点饱食度'
        WHEN 3 THEN '轻盈有趣的羽毛玩具，提升30点心情'
        WHEN 4 THEN '弹力十足的玩具球，提升50点心情'
        WHEN 5 THEN '蕴含成长能量的药水，增加100点宠物经验'
        WHEN 6 THEN '丰盛豪华的大餐，恢复100点饱食度'
        WHEN 7 THEN '带来愉快心情的护符，提升80点心情'
        WHEN 8 THEN '稀有的成长催化剂，增加500点宠物经验'
    END,
    price = CASE id
        WHEN 1 THEN 5
        WHEN 2 THEN 9
        WHEN 3 THEN 6
        WHEN 4 THEN 9
        WHEN 5 THEN 25
        WHEN 6 THEN 14
        WHEN 7 THEN 13
        WHEN 8 THEN 100
    END
WHERE id BETWEEN 1 AND 8;
