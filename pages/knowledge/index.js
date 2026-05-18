Page({
  data: {
    currentCategory: 'nutrition',
    showArticleModal: false,
    currentArticle: {},

    categories: [
      { id: 'nutrition', name: '营养学', icon: '🍎' },
      { id: 'exercise', name: '运动塑形', icon: '🏃' },
      { id: 'metabolism', name: '医学代谢', icon: '💊' },
      { id: 'psychology', name: '心理行为', icon: '🧠' }
    ],

    articles: []
  },

  onLoad() {
    this.loadArticles();
  },

  switchCategory(e) {
    const category = e.currentTarget.dataset.id;
    this.setData({ currentCategory: category });
    this.loadArticles();
  },

  loadArticles() {
    const category = this.data.currentCategory;
    const allArticles = this.getArticlesByCategory(category);
    this.setData({ articles: allArticles });
  },

  showArticle(e) {
    const id = e.currentTarget.dataset.id;
    const article = this.data.articles.find(a => a.id === id);
    if (article) {
      this.setData({ showArticleModal: true, currentArticle: article });
    }
  },

  hideArticle() {
    this.setData({ showArticleModal: false });
  },

  noop() {},

  getArticlesByCategory(category) {
    const articles = {
      nutrition: [
        {
          id: 'macro-ratio',
          title: '宏量营养素配比指南',
          summary: '蛋白质、碳水、脂肪的最佳配比建议',
          tag: '必读',
          content: `【宏量营养素配比建议】

减脂人群：
• 蛋白质：1.5-2g/kg体重（保护肌肉）
• 碳水：2-3g/kg体重（避免过低导致基础代谢下降）
• 脂肪：0.8-1g/kg体重（维持激素水平）

塑形人群：
• 蛋白质：2-2.5g/kg体重（支持肌肉合成）
• 碳水：3-4g/kg体重（支持训练）
• 脂肪：1-1.2g/kg体重

增肌人群：
• 蛋白质：2-2.5g/kg体重
• 碳水：4-6g/kg体重（热量盈余）
• 脂肪：1-1.5g/kg体重

维持人群：
• 蛋白质：1.2-1.5g/kg体重
• 碳水：3-5g/kg体重（根据活动量调整）
• 脂肪：1g/kg体重`,
          updateDate: '2024-01-15'
        },
        {
          id: 'food-gi',
          title: '食物升糖指数（GI）详解',
          summary: '低GI饮食让你更饱腹、更稳定',
          content: `【食物升糖指数（GI）表】

低GI食物（<55）：适合减脂、血糖管理
• 燕麦：55
• 糙米：55
• 全麦面包：55
• 红薯：55
• 苹果：36
• 牛奶：39
• 豆腐：42

中GI食物（55-70）：
• 荞麦：60
• 玉米：60
• 香蕉（熟）：62

高GI食物（>70）：应减少摄入
• 白米饭：82
• 白面包：75
• 西瓜：76
• 南瓜：75

💡 建议：用糙米、全麦面包替代精制米面，用水果替代果汁。`,
          updateDate: '2024-01-10'
        },
        {
          id: 'food-calories',
          title: '常见食物热量表',
          summary: '每100g食物热量大揭秘',
          content: `【常见食物热量表（每100g）】

主食类：
• 米饭：130kcal
• 面条（煮）：280kcal
• 馒头：223kcal
• 全麦面包：246kcal
• 燕麦：389kcal
• 红薯：99kcal
• 土豆：77kcal

蛋白质类：
• 鸡胸肉：130kcal
• 牛肉（瘦）：143kcal
• 鱼肉（平均）：90kcal
• 鸡蛋：144kcal
• 豆腐：81kcal

蔬菜类：
• 西兰花：35kcal
• 青菜：22kcal
• 番茄：19kcal
• 黄瓜：16kcal

💡 记录饮食时，优先选择低热量的蔬菜和蛋白质。`,
          updateDate: '2024-01-05'
        }
      ],

      exercise: [
        {
          id: 'home-workout',
          title: '居家训练动作库',
          summary: '不需要健身房，居家也能练',
          tag: '实用',
          content: `【居家训练动作库】

核心训练：
• 平板支撑：保持60秒，做3组
• 卷腹：15次×3组（上腹）
• 臀桥：20次×3组（臀部+下背）
• 登山者：30秒×3组

下肢训练：
• 深蹲：15次×3组
• 哑铃罗马尼亚硬拉：12次×3组
• 保加利亚分腿蹲：每侧10次×3组
• 靠墙静蹲：60秒×3组（膝盖友好）

上肢训练：
• 俯卧撑：10-15次×3组
• 哑铃肩推：12次×3组
• 哑铃划船：12次×3组

💡 每次训练控制在40-60分钟。`,
          updateDate: '2024-01-12'
        },
        {
          id: 'gym-workout',
          title: '健身房训练动作库',
          summary: '自由重量训练王牌动作',
          content: `【健身房训练动作库】

胸部：
• 杠铃卧推：主要发展胸大肌
• 哑铃飞鸟：孤立刺激胸肌
• 双杠臂屈伸：发展下胸

背部：
• 引体向上：背阔肌王牌动作
• 硬拉：全身后侧链之王
• 单臂哑铃划船：背部厚度

腿部：
• 杠铃深蹲：股四头肌+臀部
• 腿举：强化股四头肌
• 罗马尼亚硬拉：臀+腿后侧

💡 复合动作优先，8-12次增肌，15-20次塑形。`,
          updateDate: '2024-01-08'
        },
        {
          id: 'gender-training',
          title: '男女训练差异',
          summary: '了解性别差异，科学训练',
          content: `【男女身体差异与训练重点】

女性训练特点：
• 天生肌纤维比例较低，适合多次数轻重量
• 肩部训练避免显得宽阔，重点发展后束
• 臀部训练是重点（审美+功能）
• 重视拉伸和柔韧性

男性训练特点：
• 上半身训练是重点（胸肩背臂）
• 可采用大重量低次数
• 核心训练不能忽视

共同重点：
• 都要训练臀部（久坐导致臀部失活）
• 都要拉伸放松
• 都要重视训练容量和恢复`,
          updateDate: '2024-01-03'
        }
      ],

      metabolism: [
        {
          id: 'metabolism',
          title: '基础代谢波动原理',
          summary: '为什么你比别人瘦得慢？',
          tag: '必读',
          content: `【基础代谢波动原理】

基础代谢（BMR）影响因素：
• 肌肉量：每增加1kg肌肉，每天多消耗约50kcal
• 体温：体温每升高1℃，代谢提高约7%
• 甲状腺功能：甲减使代谢降低20-40%
• 性别：男性基础代谢比女性高5-10%
• 年龄：每增长10岁，基础代谢下降约2%

导致基础代谢下降的因素：
• 睡眠不足：每天<6小时，基础代谢降低5-15%
• 长期节食：热量摄入长期低于1200kcal
• 压力大：皮质醇升高，抑制代谢

提高基础代谢的方法：
• 力量训练增加肌肉量
• 保证充足睡眠7-9小时
• 适量咖啡因摄入`,
          updateDate: '2024-01-14'
        },
        {
          id: 'hormone',
          title: '激素与体重关系',
          summary: '激素如何影响你的身材？',
          content: `【激素与体重关系】

皮质醇（压力激素）：
• 长期高压导致向心性肥胖（腹部堆积）
• 影响食欲，容易暴饮暴食
• 建议：冥想、瑜伽、充足睡眠

胰岛素：
• 进食后降血糖，脂肪细胞储存能量
• 胰岛素抵抗导致减肥困难
• 建议：低GI饮食、间歇性断食

雌激素：
• 女性脂肪分布主要在臀部大腿
• 经期前后体重波动1-3kg（正常）

甲状腺激素：
• 决定基础代谢高低
• 甲减症状：易胖、乏力、怕冷`,
          updateDate: '2024-01-09'
        },
        {
          id: 'special-cases',
          title: '特殊情况判断与处理',
          summary: '水肿、平台期、假性肥胖',
          content: `【特殊情况判断与处理】

经期水肿：
• 经期前1周因激素变化易水肿
• 体重可能增加1-3kg
• 建议：减少盐分摄入，多吃利尿食物如冬瓜

平台期：
• 体重停滞2-4周是正常现象
• 身体在适应新的代谢模式
• 建议：增加力量训练、换运动方式

假性肥胖：
• 水肿：昨晚吃太咸、姨妈快来
• 肌肉增加：最近开始运动
• 便秘：膳食纤维和饮水不足
• 判断方法：看围度变化而非只盯体重

暴食后处理：
• 不要自责或断食惩罚
• 次日正常饮食
• 2-3天后体重会恢复`,
          updateDate: '2024-01-04'
        }
      ],

      psychology: [
        {
          id: 'emotion-handle',
          title: '情绪识别与应对',
          summary: '减肥心态管理大全',
          tag: '必读',
          content: `【情绪识别与应对话术】

平台期了怎么办：
→ 先肯定：平台期说明你之前的减脂是有效的
→ 给建议：增加力量训练，换个运动方式
→ 给希望：平台期一般2-4周就会突破

今天吃多了：
→ 先安慰：偶尔吃多不会让你变胖的
→ 解释原理：体重波动主要是水分，不是脂肪
→ 引导行动：明天正常吃，多喝水

不想减了：
→ 共情：你已经坚持了这么久，真的很不容易！
→ 提醒初心：当初为什么想减脂来着？
→ 降低目标：今天只做一点点就好

不掉秤：
→ 判断是否平台期
→ 建议看围度变化
→ 提醒称重方法：每天同一时间、同一状态`,
          updateDate: '2024-01-13'
        },
        {
          id: 'habit-form',
          title: '习惯养成方法',
          summary: '66天习惯理论',
          content: `【习惯养成方法】

21天习惯理论：
• 前1-7天：需要刻意提醒自己
• 8-14天：需要意志力维持
• 15-21天：开始变得自然

66天习惯理论：
• 研究表明习惯养成需要66天左右
• 不要追求完美，允许偶尔中断
• 习惯养成后会自动坚持

减肥心理建设：
• 设定小目标：先减5kg，比减30kg更容易成功
• 即时奖励：达成小目标后给自己一个奖励
• 记录进步：用照片、数据记录变化
• 不要比较：每个人的身体和速度不一样

减肥失败常见原因：
• 目标设定太高
• 过度节食导致暴食
• 过度依赖意志力
• 睡眠不足`,
          updateDate: '2024-01-07'
        }
      ]
    };

    return articles[category] || [];
  }
});