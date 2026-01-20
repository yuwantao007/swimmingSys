-- 插入文章资料分类数据
INSERT INTO article_categories (category_name, description, sort_order) VALUES
('游泳技巧', '游泳技能和技巧分享', 1),
('健康知识', '游泳健身和健康相关知识', 2),
('场馆公告', '游泳馆重要通知和公告', 3),
('活动通知', '游泳馆活动安排和通知', 4),
('安全须知', '游泳安全知识和注意事项', 5);

-- 插入文章资料数据
INSERT INTO articles (title, content, summary, cover_image, category_id, author_id, status, view_count, like_count, comment_count, is_top, is_featured) VALUES
(
  '夏季游泳安全须知',
  '夏季是游泳的高峰期，但同时也是溺水事故的高发期。为了确保每一位游泳者的安全，我们特别提醒大家注意以下几点：\n\n1. 选择正规游泳场所，避免野外游泳\n2. 游泳前做好充分的热身运动\n3. 不要在饱餐或饥饿状态下游泳\n4. 儿童游泳时必须有成人陪同\n5. 注意水质卫生，佩戴合适的泳镜和泳帽\n6. 如身体不适，应暂停游泳活动',
  '夏季游泳高峰期安全注意事项，包括选择正规场所、热身运动、饮食安排等重要安全须知',
  'uploads/summer_swimming_safety.jpg',
  5,
  1,
  1,
  1250,
  89,
  23,
  1,
  1
),
(
  '蛙泳入门技巧详解',
  '蛙泳是最受欢迎的游泳姿势之一，适合初学者学习。以下是蛙泳的基本技巧：\n\n手臂动作：\n- 外划：双手向外划水，手掌向外倾斜\n- 内划：双手向内划水，产生推进力\n- 前伸：双臂伸直向前，准备下一次划水\n\n腿部动作：\n- 收腿：膝盖弯曲，小腿靠近大腿\n- 翻脚：脚尖外翻，为蹬水做准备\n- 蹬夹：用力向后蹬水，然后并拢双腿\n\n呼吸技巧：\n- 在手臂内划时抬头吸气\n- 在手臂前伸时低头呼气\n- 保持呼吸节奏稳定',
  '详细介绍蛙泳的基本技巧，包括手臂动作、腿部动作和呼吸技巧',
  'uploads/frog_swim_tips.jpg',
  1,
  1,
  1,
  2100,
  156,
  42,
  0,
  1
),
(
  '游泳对健康的益处',
  '游泳是一项全身性的运动，对身体健康有着诸多益处：\n\n心血管系统：\n- 提高心脏功能，增强心肌收缩力\n- 促进血液循环，降低血压\n- 改善血管弹性\n\n肌肉骨骼系统：\n- 增强肌肉力量和耐力\n- 改善关节灵活性\n- 有助于维持理想体重\n\n呼吸系统：\n- 增强肺活量\n- 改善呼吸效率\n- 增强呼吸肌力量\n\n心理健康：\n- 缓解压力，改善情绪\n- 提高睡眠质量\n- 增强大脑活力',
  '详细阐述游泳对心血管、肌肉骨骼、呼吸系统以及心理健康的多重益处',
  'uploads/swimming_health_benefits.jpg',
  2,
  1,
  1,
  1800,
  134,
  31,
  0,
  1
),
(
  '7月份课程安排更新',
  '亲爱的会员朋友们，我们很高兴地宣布7月份的游泳课程安排更新：\n\n成人初级班（周一、周三、周五 19:00-20:00）\n- 适合零基础学员\n- 教授基本游泳姿势\n- 强化水中安全意识\n\n青少年提高班（周二、周四、周六 16:00-17:30）\n- 针对已有基础的青少年\n- 提升游泳技术和速度\n- 准备参加游泳比赛\n\n亲子游泳课（周日 10:00-11:00）\n- 增进亲子关系\n- 培养孩子水中安全意识\n- 学习基础游泳技能\n\n请注意：课程名额有限，建议提前预约报名。',
  '7月份游泳课程安排更新，包括成人班、青少年班和亲子游泳课程',
  'uploads/july_course_schedule.jpg',
  3,
  1,
  1,
  980,
  67,
  18,
  0,
  0
),
(
  '游泳馆设施升级公告',
  '尊敬的会员朋友们：\n\n为了给大家提供更好的游泳体验，我们将于7月15日至7月25日进行设施升级改造，期间将暂停开放部分区域。\n\n升级内容包括：\n1. 泳池水质净化系统升级\n2. 更衣室设施更新\n3. 休息区座椅更换\n4. 增设儿童专用浅水区\n5. 安装全新音响系统\n\n预计7月26日起恢复正常营业，届时将以全新的面貌迎接大家的到来。\n\n给您带来的不便，敬请谅解！如有疑问，请联系前台服务人员。',
  '游泳馆设施升级改造公告，包括升级内容和时间安排',
  'uploads/facility_upgrade_announcement.jpg',
  4,
  1,
  1,
  750,
  45,
  12,
  0,
  0
);

-- 插入文章资料附件数据
INSERT INTO article_attachment (article_id, file_name, file_path, file_size, file_type, mime_type) VALUES
-- 夏季游泳安全须知附件
(1, 'water_safety_poster.jpg', 'uploads/images/water_safety_poster.jpg', 1024000, 'image', 'image/jpeg'),
(1, 'swimming_equipment_guide.pdf', 'uploads/docs/swimming_equipment_guide.pdf', 2048000, 'document', 'application/pdf'),

-- 蛙泳入门技巧详解附件
(2, 'frog_swim_diagram.jpg', 'uploads/images/frog_swim_diagram.jpg', 1536000, 'image', 'image/jpeg'),
(2, 'swimming_technique_video.mp4', 'uploads/videos/swimming_technique_video.mp4', 10485760, 'video', 'video/mp4'),

-- 游泳对健康的益处附件
(3, 'health_statistics_chart.jpg', 'uploads/images/health_statistics_chart.jpg', 2048000, 'image', 'image/jpeg'),
(3, 'fitness_research_paper.pdf', 'uploads/docs/fitness_research_paper.pdf', 3072000, 'document', 'application/pdf'),

-- 7月份课程安排更新附件
(4, 'course_schedule_table.jpg', 'uploads/images/course_schedule_table.jpg', 1280000, 'image', 'image/jpeg'),
(4, 'registration_form.docx', 'uploads/docs/registration_form.docx', 102400, 'document', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'),

-- 游泳馆设施升级公告附件
(5, 'upgrade_plan_diagram.jpg', 'uploads/images/upgrade_plan_diagram.jpg', 2560000, 'image', 'image/jpeg'),
(5, 'construction_timeline.pdf', 'uploads/docs/construction_timeline.pdf', 1536000, 'document', 'application/pdf');