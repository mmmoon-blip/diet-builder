const app = getApp();
const { API_BASE } = require('../../config/api');

Page({
  data: {
    userInfo: null,

    // 当前激活的图表类型
    activeChart: 'weight',

    // 喝水量相关
    showWaterModal: false,
    todayWater: 0,
    waterGoal: 2000,
    customWaterAmount: '',

    // 体重趋势图
    weightChartDays: 7,
    weightChartData: [],
    weightChartLabels: [],
    weightChartLoaded: false,
    weightChartMin: 0,
    weightChartMax: 100,
    weightChartMid: 50,
    lineHeight: 200,
    lineScaleY: 1,
    targetLineBottom: 0,
    summaryChange: 0,
    summaryAvg: 0,
    summaryToGoal: null,

    // 维度趋势图
    measurementChartType: 'waist',
    measurementChartTypeName: '腰围',
    measurementChartCount: 30,
    measurementChartData: [],
    measurementChartLabels: [],
    measurementChartLoaded: false,
    measurementChartMin: 0,
    measurementChartMax: 100,
    measurementChartMid: 50,
    measurementStats: {
      current: '--',
      change: 0,
      avg: '--'
    },
    measurementChartUnit: 'cm',

    // 体重弹窗
    showWeightModal: false,
    weightDate: '',
    weight: '',
    weightSleepStart: '',
    weightSleepEnd: '',
    weightHistory: [],
    editingWeightId: null,
    // 体重导入
    showImport: false,
    importStartDate: '',
    importStartWeight: '',
    importTargetWeight: '',
    importRows: [],
    hasGenerated: false,

    // 维度弹窗
    showMeasurementModal: false,
    measurementDate: '',
    measurementBust: '',
    measurementWaist: '',
    measurementHip: '',
    measurementArm: '',
    measurementThigh: '',
    measurementUpperArm: '',
    measurementForearm: '',
    measurementCalf: '',
    measurementRecords: [],
    measurementHasMore: false,
    weightHistory: [],
    weightHasMore: false,

    // 经期弹窗
    showMenstrualModal: false,
    calendarYear: new Date().getFullYear(),
    calendarMonth: new Date().getMonth(),
    calendarPickerDate: '',
    weekdays: ['日', '一', '二', '三', '四', '五', '六'],
    calendarDays: [],
    menstrualRecords: [],
    menstrualPhaseInfo: '',
    // 经期记录状态
    periodStartDate: '',
    periodEndDate: '',
    periodDays: 0,
    flowLevelOptions: ['请选择', '少', '中', '多'],
    flowLevelMap: { 'light': '少', 'medium': '中', 'heavy': '多' },
    flowLevelIndex: 0,
    menstrualFlowLevel: null,
    // 新增经期详细字段
    menstrualCurrentPhase: '',
    menstrualCycleDay: null,
    menstrualPhaseDay: null,
    menstrualNextPeriod: '',
    menstrualTip: '',
    menstrualIsInPeriod: false,
    menstrualEndDate: '',
    painLevel: 0,
    menstrualSymptoms: [],
    menstrualMood: '',
    menstrualOtherInfo: '',

    // 档案弹窗
    showProfileModal: false,
    profileNickname: '',
    profileAge: '',
    profileGenderIndex: 0,
    genderOptions: ['请选择', '男', '女'],
    profileHeight: '',
    profileInitialWeight: '',
    profileTargetWeight: '',
    profileWeightLossPeriod: '',
    profileStartWeightDate: '',
    profileDietaryTaboo: '',
    profileSleepStart: '',
    profileSleepEnd: '',
    // 新增字段
    profileUserTypeIndex: 0,
    userTypeOptions: ['请选择', '减重', '塑形', '维持', '增肌'],
    userTypeMap: { '减重': 'weight_loss', '塑形': 'shaping', '维持': 'maintenance', '增肌': 'muscle_gain' },
    profileExerciseFrequency: '',
    profileExercisePreferenceIndex: 0,
    exercisePreferenceOptions: ['请选择', '居家', '健身房', '户外', '游泳'],
    profileFitnessLevelIndex: 0,
    fitnessLevelOptions: ['请选择', '新手', '中级', '高级'],
    profileHasKneeIssue: null,
    profileDietPreferenceIndex: 0,
    dietPreferenceOptions: ['请选择', '外卖多', '自己做饭', '清淡', '重口'],
    profileBreakfastHabit: true,
    profileConstitutionTags: '',
    profileWorkPressure: '',
    profileWaterIntake: '',
    profileTargetAreas: '',

    // 体脂估算
    showBfpModal: false,
    bfpWaist: '',
    bfpHip: '',
    bfpWeight: '',
    bfpResult: null,
    bfpCategory: '',
    bfpAdvice: ''
  },

  onLoad() {
    this.loadUserInfo();
    this.loadData();
  },

  onShow() {
    this.loadUserInfo();
    this.loadData();
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({ userInfo: userInfo || {} });
  },

  loadData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({ userInfo: userInfo || {} });
    if (!token || !userInfo || !userInfo.id) return;

    this.loadWeightData();
    this.loadMeasurementData();
    this.loadMenstrualData();
    this.loadTodayWater();
    this.loadMenstrualPhase();
    this.loadWeightChartData();
  },

  // ============ 体重趋势图 ============
  loadWeightChartData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    const days = this.data.weightChartDays || 7;
    wx.request({
      url: API_BASE + '/weight/history?userId=' + userInfo.id + '&days=' + days,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success && res.data.data) {
          this.processWeightChartData(res.data.data);
        } else {
          this.setData({ weightChartData: [], weightChartLoaded: true });
        }
      },
      fail: () => {
        this.setData({ weightChartLoaded: true });
      }
    });
    // 同时加载维度数据
    this.loadMeasurementChartData();
  },

  processWeightChartData(records) {
    if (!records || records.length === 0) {
      this.setData({ weightChartData: [], weightChartLoaded: true });
      return;
    }

    // 按日期排序（从早到晚）
    const sorted = [...records].sort((a, b) => {
      return new Date(a.recordDate) - new Date(b.recordDate);
    });

    const weights = sorted.map(r => r.weight);
    const minWeight = Math.min(...weights);
    const maxWeight = Math.max(...weights);
    const range = maxWeight - minWeight || 2;

    // 计算Y轴范围（留出上下10%边距）
    const chartMin = minWeight - range * 0.15;
    const chartMax = maxWeight + range * 0.15;
    const chartRange = chartMax - chartMin || 2;
    const midValue = (chartMin + chartMax) / 2;

    // 计算目标线位置
    const userInfo = wx.getStorageSync('userInfo') || {};
    let targetLineBottom = 0;
    if (userInfo.targetWeight) {
      targetLineBottom = ((userInfo.targetWeight - chartMin) / chartRange) * 100;
      this.setData({ targetLineBottom });
    }

    // 生成X轴标签（最多显示5个日期）
    const weightChartLabels = this.generateXAxisLabels(sorted, this.data.weightChartDays);

    // 处理每个数据点
    const chartData = sorted.map((r, index) => {
      // X位置：均匀分布，首尾留边距
      const xPercent = sorted.length === 1 ? 50 : (8 + (index / Math.max(sorted.length - 1, 1)) * 84);
      // Y位置：转换为百分比 (0=top, 100=bottom)，weight越大yPercent越大（越靠下）
      const yPercent = ((r.weight - chartMin) / chartRange) * 100;
      // 格式化日期显示
      const date = new Date(r.recordDate);
      const dateLabel = `${date.getMonth() + 1}/${date.getDate()}`;

      // 计算连接线属性
      let lineLength = 0;
      let lineAngle = 0;
      if (index < sorted.length - 1) {
        const nextR = sorted[index + 1];
        const nextXPercent = 8 + ((index + 1) / Math.max(sorted.length - 1, 1)) * 84;
        const nextYPercent = ((nextR.weight - chartMin) / chartRange) * 100;
        const dx = nextXPercent - xPercent;
        const dy = nextYPercent - yPercent;
        lineLength = Math.sqrt(dx * dx + dy * dy);
        lineAngle = Math.atan2(dy, dx) * 180 / Math.PI;
      }

      return {
        ...r,
        xPercent,
        yPercent,
        dateLabel,
        isToday: r.recordDate === new Date().toISOString().split('T')[0],
        isLowest: r.weight === minWeight,
        isHighest: r.weight === maxWeight,
        isLast: index === sorted.length - 1,
        lineLength,
        lineAngle
      };
    });

    // 计算统计摘要
    const latestWeight = sorted[sorted.length - 1].weight;
    const lastWeight = sorted.length > 1 ? sorted[sorted.length - 2].weight : latestWeight;
    const initialWeight = userInfo.initialWeight ? parseFloat(userInfo.initialWeight) : (sorted.length > 0 ? sorted[0].weight : latestWeight);
    const totalChange = latestWeight - initialWeight;
    const changeFromLast = latestWeight - lastWeight;
    const avgWeight = weights.reduce((a, b) => a + b, 0) / weights.length;
    let toGoal = null;
    let progressPercent = null;
    if (userInfo.targetWeight) {
      toGoal = latestWeight - userInfo.targetWeight;
      const totalToLose = initialWeight - userInfo.targetWeight;
      const totalLost = initialWeight - latestWeight;
      if (totalToLose > 0) {
        progressPercent = Math.round((totalLost / totalToLose) * 100);
        progressPercent = Math.max(0, Math.min(100, progressPercent));
      }
    }

    this.setData({
      weightChartData: chartData,
      weightChartLabels,
      weightChartMin: chartMin.toFixed(1),
      weightChartMax: chartMax.toFixed(1),
      weightChartMid: midValue.toFixed(1),
      lineHeight: 200,
      lineScaleY: 1,
      summaryChange: (latestWeight - initialWeight).toFixed(1),
      summaryChangeAbs: Math.abs(latestWeight - initialWeight).toFixed(1),
      summaryChangeFromLast: changeFromLast.toFixed(1),
      summaryAvg: avgWeight.toFixed(1),
      summaryLatest: latestWeight.toFixed(1),
      summaryToGoal: toGoal !== null ? toGoal.toFixed(1) : null,
      progressPercent: progressPercent,
      weightChartLoaded: true
    });
  },

  generateXAxisLabels(sorted, days) {
    if (!sorted || sorted.length === 0) return [];
    const labels = [];
    const maxLabels = 5;
    const step = Math.max(1, Math.floor(sorted.length / maxLabels));

    for (let i = 0; i < sorted.length; i += step) {
      const date = new Date(sorted[i].recordDate);
      labels.push({
        xPercent: sorted.length === 1 ? 50 : (8 + (i / Math.max(sorted.length - 1, 1)) * 84),
        label: `${date.getMonth() + 1}/${date.getDate()}`
      });
    }

    // 确保最后一个点有标签
    if (sorted.length > 1) {
      const lastIdx = sorted.length - 1;
      const lastDate = new Date(sorted[lastIdx].recordDate);
      // 检查是否重复
      if (labels.length === 0 || labels[labels.length - 1].xPercent < 90) {
        labels.push({
          xPercent: 92,
          label: `${lastDate.getMonth() + 1}/${lastDate.getDate()}`
        });
      }
    }

    return labels;
  },

  switchWeightChart(e) {
    const days = parseInt(e.currentTarget.dataset.days);
    this.setData({ weightChartDays: days, weightChartLoaded: false });
    this.loadWeightChartData();
  },

  switchActiveChart(e) {
    const chart = e.currentTarget.dataset.chart;
    this.setData({ activeChart: chart });
  },

  goToWeightChart() {
    // 点击图表区域可以跳转或展示更详细图表（预留）
  },

  // ============ 维度趋势图 ============
  loadMeasurementChartData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    const type = this.data.measurementChartType;
    const count = this.data.measurementChartCount || 30;

    // 获取体重历史或维度历史
    if (type === 'weight') {
      // 使用体重数据
      wx.request({
        url: API_BASE + '/weight/history?userId=' + userInfo.id + '&days=' + count,
        method: 'GET',
        header: { 'Authorization': 'Bearer ' + token },
        success: (res) => {
          if (res.data.success && res.data.data) {
            this.processMeasurementChartData(res.data.data, 'weight', 'kg');
          } else {
            this.setData({ measurementChartData: [], measurementChartLoaded: true });
          }
        },
        fail: () => {
          this.setData({ measurementChartLoaded: true });
        }
      });
    } else {
      // 获取维度数据 - 使用limit参数按记录数获取
      wx.request({
        url: API_BASE + '/measurements/' + userInfo.id + '?limit=' + count,
        method: 'GET',
        header: { 'Authorization': 'Bearer ' + token },
        success: (res) => {
          if (res.data.success && res.data.data) {
            if (type === 'arm' || type === 'leg') {
              this.processMultiMeasurementChartData(res.data.data, type, 'cm');
            } else {
              this.processMeasurementChartData(res.data.data, type, 'cm');
            }
          } else {
            this.setData({ measurementChartData: [], measurementChartLoaded: true });
          }
        },
        fail: () => {
          this.setData({ measurementChartLoaded: true });
        }
      });
    }
  },

  processMeasurementChartData(records, type, unit) {
    if (!records || records.length === 0) {
      this.setData({ measurementChartData: [], measurementChartLoaded: true });
      return;
    }

    // 根据type提取对应的值
    const getValue = (record) => {
      if (type === 'waist') return record.waist;
      if (type === 'hip') return record.hip;
      if (type === 'chest') return record.chest;
      if (type === 'upperArm') return record.upperArm;
      if (type === 'forearm') return record.forearm;
      if (type === 'thigh') return record.thigh;
      if (type === 'calf') return record.calf;
      return null;
    };

    // 过滤有效记录
    const validRecords = records.filter(r => getValue(r) != null);
    if (validRecords.length === 0) {
      this.setData({ measurementChartData: [], measurementChartLoaded: true });
      return;
    }

    // 按日期排序
    const sorted = [...validRecords].sort((a, b) => {
      return new Date(a.recordDate) - new Date(b.recordDate);
    });

    const values = sorted.map(r => getValue(r));
    const minVal = Math.min(...values);
    const maxVal = Math.max(...values);
    const range = maxVal - minVal || 10;

    // 计算Y轴刻度
    const chartMin = minVal - range * 0.15;
    const chartMax = maxVal + range * 0.15;
    const chartRange = chartMax - chartMin || 2;
    const midValue = (chartMin + chartMax) / 2;

    // 生成X轴标签
    const measurementChartLabels = this.generateMeasurementXAxisLabels(sorted);

    // 处理数据点
    const chartData = sorted.map((r, index) => {
      const xPercent = sorted.length === 1 ? 50 : (8 + (index / Math.max(sorted.length - 1, 1)) * 84);
      const value = getValue(r);
      const yPercent = ((value - chartMin) / chartRange) * 100;
      const date = new Date(r.recordDate);
      const dateLabel = `${date.getMonth() + 1}/${date.getDate()}`;

      // 计算连接线属性
      let lineLength = 0;
      let lineAngle = 0;
      if (index < sorted.length - 1) {
        const nextR = sorted[index + 1];
        const nextXPercent = 8 + ((index + 1) / Math.max(sorted.length - 1, 1)) * 84;
        const nextYPercent = ((getValue(nextR) - chartMin) / chartRange) * 100;
        const dx = nextXPercent - xPercent;
        const dy = nextYPercent - yPercent;
        lineLength = Math.sqrt(dx * dx + dy * dy);
        lineAngle = Math.atan2(dy, dx) * 180 / Math.PI;
      }

      return {
        ...r,
        xPercent,
        yPercent,
        value: value.toFixed(1),
        dateLabel,
        isLowest: value === minVal,
        isHighest: value === maxVal,
        isLast: index === sorted.length - 1,
        lineLength,
        lineAngle
      };
    });

    // 计算统计
    const latestValue = getValue(sorted[sorted.length - 1]);
    const firstValue = getValue(sorted[0]);
    const totalChange = latestValue - firstValue;
    const avgValue = values.reduce((a, b) => a + b, 0) / values.length;

    const typeName = type === 'weight' ? '体重' : type === 'waist' ? '腰围' : type === 'hip' ? '臀围' : type === 'bust' ? '胸围' : type === 'upperArm' ? '大臂围' : type === 'forearm' ? '小臂围' : type === 'thigh' ? '大腿围' : type === 'calf' ? '小腿围' : '';

    this.setData({
      measurementChartData: chartData,
      measurementChartLabels,
      measurementChartMin: chartMin.toFixed(1),
      measurementChartMax: chartMax.toFixed(1),
      measurementChartMid: midValue.toFixed(1),
      measurementChartLoaded: true,
      measurementStats: {
        current: latestValue.toFixed(1),
        change: totalChange.toFixed(1),
        avg: avgValue.toFixed(1)
      },
      measurementChartTypeName: typeName
    });
  },

  processMultiMeasurementChartData(records, type, unit) {
    if (!records || records.length === 0) {
      this.setData({ measurementChartData: [], measurementChartLoaded: true, measurementChartData2: [], measurementChartLoaded2: true });
      return;
    }

    const isArm = type === 'arm';
    const field1 = isArm ? 'upperArm' : 'thigh';
    const field2 = isArm ? 'forearm' : 'calf';

    // 分别提取两组数据
    const processField = (field) => {
      const validRecords = records.filter(r => r[field] != null);
      if (validRecords.length === 0) return null;

      const sorted = [...validRecords].sort((a, b) => new Date(a.recordDate) - new Date(b.recordDate));
      const values = sorted.map(r => r[field]);
      const minVal = Math.min(...values);
      const maxVal = Math.max(...values);
      const range = maxVal - minVal || 10;
      const chartMin = minVal - range * 0.15;
      const chartMax = maxVal + range * 0.15;
      const chartRange = chartMax - chartMin || 2;
      const midValue = (chartMin + chartMax) / 2;

      const chartData = sorted.map((r, index) => {
        const xPercent = sorted.length === 1 ? 50 : (8 + (index / Math.max(sorted.length - 1, 1)) * 84);
        const value = r[field];
        const yPercent = ((value - chartMin) / chartRange) * 100;
        const date = new Date(r.recordDate);
        const dateLabel = `${date.getMonth() + 1}/${date.getDate()}`;

        // 计算连接线属性
        let lineLength = 0;
        let lineAngle = 0;
        if (index < sorted.length - 1) {
          const nextR = sorted[index + 1];
          const nextXPercent = 8 + ((index + 1) / Math.max(sorted.length - 1, 1)) * 84;
          const nextYPercent = ((nextR[field] - chartMin) / chartRange) * 100;
          const dx = nextXPercent - xPercent;
          const dy = nextYPercent - yPercent;
          lineLength = Math.sqrt(dx * dx + dy * dy);
          lineAngle = Math.atan2(dy, dx) * 180 / Math.PI;
        }

        return { ...r, xPercent, yPercent, value: value.toFixed(1), dateLabel, isLowest: value === minVal, isHighest: value === maxVal, isLast: index === sorted.length - 1, lineLength, lineAngle };
      });

      const latestValue = values[values.length - 1];
      const firstValue = values[0];
      return {
        chartData,
        chartMin: chartMin.toFixed(1),
        chartMax: chartMax.toFixed(1),
        chartMid: midValue.toFixed(1),
        stats: {
          current: latestValue.toFixed(1),
          change: (latestValue - firstValue).toFixed(1),
          avg: (values.reduce((a, b) => a + b, 0) / values.length).toFixed(1)
        }
      };
    };

    const data1 = processField(field1);
    const data2 = processField(field2);

    const typeName = isArm ? '臂围' : '腿围';
    const subLabel = isArm ? '大臂/小臂' : '大腿/小腿';

    this.setData({
      measurementChartData: data1 ? data1.chartData : [],
      measurementChartLabels: data1 ? this.generateMeasurementXAxisLabels(data1.chartData) : [],
      measurementChartMin: data1 ? data1.chartMin : '0',
      measurementChartMax: data1 ? data1.chartMax : '100',
      measurementChartMid: data1 ? data1.chartMid : '50',
      measurementChartLoaded: true,
      measurementChartData2: data2 ? data2.chartData : [],
      measurementChartMin2: data2 ? data2.chartMin : '0',
      measurementChartMax2: data2 ? data2.chartMax : '100',
      measurementChartMid2: data2 ? data2.chartMid : '50',
      measurementChartLoaded2: true,
      measurementStats: data1 ? data1.stats : { current: '--', change: '--', avg: '--' },
      measurementChartTypeName: typeName,
      measurementChartSubLabel: subLabel
    });
  },

  generateMeasurementXAxisLabels(sorted) {
    if (!sorted || sorted.length === 0) return [];
    const labels = [];
    const maxLabels = 5;
    const step = Math.max(1, Math.floor(sorted.length / maxLabels));

    for (let i = 0; i < sorted.length; i += step) {
      const date = new Date(sorted[i].recordDate);
      labels.push({
        xPercent: sorted.length === 1 ? 50 : (8 + (i / Math.max(sorted.length - 1, 1)) * 84),
        label: `${date.getMonth() + 1}/${date.getDate()}`
      });
    }

    if (sorted.length > 1) {
      const lastIdx = sorted.length - 1;
      const lastDate = new Date(sorted[lastIdx].recordDate);
      if (labels.length === 0 || labels[labels.length - 1].xPercent < 90) {
        labels.push({
          xPercent: 92,
          label: `${lastDate.getMonth() + 1}/${lastDate.getDate()}`
        });
      }
    }

    return labels;
  },

  switchMeasurementChart(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      measurementChartType: type,
      measurementChartLoaded: false,
      measurementChartData: []
    });
    this.loadMeasurementChartData();
  },

  switchMeasurementCount(e) {
    const count = parseInt(e.currentTarget.dataset.count);
    this.setData({
      measurementChartCount: count,
      measurementChartLoaded: false,
      measurementChartData: []
    });
    this.loadMeasurementChartData();
  },

  goToMeasurementChart() {
    // 点击维度图表
  },

  loadWeightData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    // 加载体重记录 - 分页加载，每次7条
    wx.request({
      url: API_BASE + '/weight/history?userId=' + userInfo.id + '&days=7',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success) {
          this.setData({
            weightHistory: (res.data.data || []).reverse(),
            weightHasMore: res.data.data && res.data.data.length >= 7
          });
        }
      }
    });
  },

  loadMoreWeightHistory() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const { weightHistory } = this.data;
    if (!token || !userInfo || !userInfo.id || !this.data.weightHasMore) return;

    const lastDate = weightHistory.length > 0 ? weightHistory[weightHistory.length - 1].recordDate : null;
    wx.request({
      url: API_BASE + '/weight/recent?userId=' + userInfo.id + '&days=10',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const newRecords = res.data.data.filter(r => !weightHistory.some(existing => existing.id === r.id));
          this.setData({
            weightHistory: [...weightHistory, ...newRecords.reverse()],
            weightHasMore: newRecords.length >= 7
          });
        }
      }
    });
  },

  loadMeasurementData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    // 加载维度记录 - 查询所有记录
    wx.request({
      url: API_BASE + '/measurements/' + userInfo.id,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success) {
          this.setData({
            measurementRecords: res.data.data || [],
            measurementHasMore: false
          });
        }
      }
    });
  },

  loadMoreMeasurementHistory() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const { measurementRecords } = this.data;
    if (!token || !userInfo || !userInfo.id || !this.data.measurementHasMore) return;

    const lastDate = measurementRecords.length > 0 ? measurementRecords[measurementRecords.length - 1].recordDate : null;
    wx.request({
      url: API_BASE + '/measurements/' + userInfo.id + '?days=30',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const newRecords = res.data.data.filter(r => !measurementRecords.some(existing => existing.id === r.id));
          this.setData({
            measurementRecords: [...measurementRecords, ...newRecords],
            measurementHasMore: newRecords.length >= 7
          });
        }
      }
    });
  },

  loadMenstrualData() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/menstrual/' + userInfo.id,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success) {
          const records = res.data.data || [];
          this.setData({ menstrualRecords: records });

          // 检查是否有进行中的经期
          const ongoing = records.find(r => !r.cycleEndDate && r.cycleStartDate);
          if (ongoing) {
            let startDate = ongoing.cycleStartDate;
            if (typeof startDate === 'string' && startDate.includes('T')) {
              startDate = startDate.split('T')[0];
            }
            this.setData({
              menstrualIsInPeriod: true,
              menstrualEndDate: '',
              periodStartDate: startDate
            });
          } else {
            this.setData({
              menstrualIsInPeriod: false,
              menstrualEndDate: ''
            });
          }

          this.renderCalendar();
        }
      }
    });
  },

  loadMenstrualPhase() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/menstrual/' + userInfo.id + '/phase',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const p = res.data.data;
          // 设置状态卡片数据
          this.setData({
            menstrualCurrentPhase: p.phase || '未知',
            menstrualCycleDay: p.cycleDay || null,
            menstrualPhaseDay: p.phaseDay || null,
            menstrualNextPeriod: p.nextPeriodDate || '',
            menstrualTip: p.bodyStatus || '',
            menstrualIsInPeriod: p.isInPeriod || false,
            predictedNextPeriodDate: p.nextPeriodDate || null,
            // 保留旧字段兼容
            menstrualPhaseInfo: `📍 ${p.phase}${p.cycleDay ? '（第' + p.cycleDay + '天）' : ''}${p.daysUntilNextPeriod ? '\n距离下次经期约' + p.daysUntilNextPeriod + '天' : ''}\n\n💪 运动：${p.exerciseRecommendation}\n\n🍽️ 饮食：${p.dietRecommendation}\n\n😊 状态：${p.bodyStatus}`
          });
        }
      }
    });
  },

  // ============ 喝水量 ============
  showWaterModal() {
    this.setData({ showWaterModal: true });
    this.loadTodayWater();
  },

  hideWaterModal() {
    this.setData({ showWaterModal: false });
  },

  loadTodayWater() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    const waterGoal = wx.getStorageSync('waterGoal') || 2000;
    this.setData({ waterGoal });

    wx.request({
      url: API_BASE + '/water/today?userId=' + userInfo.id,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success) {
          this.setData({ todayWater: res.data.data.total || 0 });
        }
      }
    });
  },

  addWater(e) {
    const amount = parseInt(e.currentTarget.dataset.amount);
    this.doAddWater(amount);
  },

  addCustomWater() {
    const amount = parseInt(this.data.customWaterAmount);
    if (!amount || amount <= 0) {
      wx.showToast({ title: '请输入有效数值', icon: 'none' });
      return;
    }
    this.doAddWater(amount);
    this.setData({ customWaterAmount: '' });
  },

  doAddWater(amount) {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/water?userId=' + userInfo.id + '&amount=' + amount,
      method: 'POST',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success) {
          this.setData({ todayWater: (this.data.todayWater || 0) + amount });
          wx.showToast({ title: '已添加', icon: 'success' });
        }
      }
    });
  },

  onCustomWaterInput(e) {
    this.setData({ customWaterAmount: e.detail.value });
  },

  onWaterGoalInput(e) {
    this.setData({ waterGoal: parseInt(e.detail.value) || 2000 });
  },

  saveWaterGoal() {
    const goal = this.data.waterGoal;
    wx.setStorageSync('waterGoal', goal);
    wx.showToast({ title: '目标已保存', icon: 'success' });
  },

  loadMenstrualPhase() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/menstrual/' + userInfo.id + '/phase',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const p = res.data.data;
          // 设置状态卡片数据
          this.setData({
            menstrualCurrentPhase: p.phase || '未知',
            menstrualCycleDay: p.cycleDay || null,
            menstrualPhaseDay: p.phaseDay || null,
            menstrualNextPeriod: p.nextPeriodDate || '',
            menstrualTip: p.bodyStatus || '',
            menstrualIsInPeriod: p.isInPeriod || false,
            predictedNextPeriodDate: p.nextPeriodDate || null,
            // 保留旧字段兼容
            menstrualPhaseInfo: `📍 ${p.phase}${p.cycleDay ? '（第' + p.cycleDay + '天）' : ''}${p.daysUntilNextPeriod ? '\n距离下次经期约' + p.daysUntilNextPeriod + '天' : ''}\n\n💪 运动：${p.exerciseRecommendation}\n\n🍽️ 饮食：${p.dietRecommendation}\n\n😊 状态：${p.bodyStatus}`
          });
        }
      }
    });
  },

  // 页面跳转
  goToPage(e) {
    const page = e.currentTarget.dataset.page;
    wx.navigateTo({ url: page });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要切换用户吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          if (token) {
            wx.request({
              url: API_BASE + '/auth/logout',
              method: 'POST',
              header: { 'Authorization': 'Bearer ' + token },
              success: () => {
                this.clearLoginData();
              },
              fail: () => {
                this.clearLoginData();
              }
            });
          } else {
            this.clearLoginData();
          }
        }
      }
    });
  },

  clearLoginData() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('isGuest');
    app.globalData.token = null;
    app.globalData.userInfo = null;
    app.globalData.isLoggedIn = false;
    app.globalData.isGuest = false;
    wx.redirectTo({ url: '/pages/login/index' });
  },

  // ============ 体重弹窗 ============
  showWeightModal() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({
      showWeightModal: true,
      weightDate: new Date().toISOString().split('T')[0],
      weight: '',
      weightSleepStart: '',
      weightSleepEnd: '',
      editingWeightId: null,
      showImport: false,
      importStartDate: userInfo.startWeightDate || new Date().toISOString().split('T')[0],
      importStartWeight: userInfo.initialWeight || '',
      importTargetWeight: userInfo.targetWeight || '',
      importRows: [],
      hasGenerated: false
    });
  },

  hideWeightModal() {
    this.setData({ showWeightModal: false, editingWeightId: null });
  },

  onWeightDateChange(e) {
    this.setData({ weightDate: e.detail.value });
  },

  onWeightInput(e) {
    this.setData({ weight: e.detail.value });
  },

  onSleepStartChange(e) {
    this.setData({ weightSleepStart: e.detail.value });
  },

  onSleepEndChange(e) {
    this.setData({ weightSleepEnd: e.detail.value });
  },

  saveWeight() {
    const { weightDate, weight, weightSleepStart, weightSleepEnd, editingWeightId, userInfo } = this.data;
    if (!weight) {
      wx.showToast({ title: '请输入体重', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    const record = {
      recordDate: weightDate,
      weight: parseFloat(weight),
      sleepStart: weightSleepStart,
      sleepEnd: weightSleepEnd
    };

    let url = API_BASE + '/weight/add?userId=' + userInfo.id;
    let method = 'POST';
    if (editingWeightId) {
      url = API_BASE + '/weight/' + editingWeightId;
      method = 'PUT';
    }

    wx.request({
      url: url,
      method: method,
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      data: record,
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: editingWeightId ? '更新成功' : '记录成功', icon: 'success' });
          this.setData({ showWeightModal: false, editingWeightId: null });
          this.loadData();
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  editWeightRecord(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showWeightModal: true,
      weightDate: item.recordDate || '',
      weight: item.weight ? String(item.weight) : '',
      weightSleepStart: item.sleepStart || '',
      weightSleepEnd: item.sleepEnd || '',
      editingWeightId: item.id
    });
  },

  deleteWeightRecord(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条体重记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/weight/' + id,
            method: 'DELETE',
            header: { 'Authorization': 'Bearer ' + token },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadData();
              }
            }
          });
        }
      }
    });
  },

  // 体重导入
  toggleImport() {
    this.setData({ showImport: !this.data.showImport });
  },

  onImportStartDateChange(e) {
    this.setData({ importStartDate: e.detail.value });
  },

  onImportStartWeightInput(e) {
    this.setData({ importStartWeight: e.detail.value });
  },

  onImportTargetWeightInput(e) {
    this.setData({ importTargetWeight: e.detail.value });
  },

  generateImportDates() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    const profileStartDate = userInfo.startWeightDate || this.data.importStartDate;
    const profileInitialWeight = userInfo.initialWeight || this.data.importStartWeight;
    const profileTargetWeight = userInfo.targetWeight || this.data.importTargetWeight;

    if (!profileStartDate || !profileInitialWeight || !profileTargetWeight) {
      wx.showToast({ title: '请填写开始日期、初始体重和目标体重', icon: 'none' });
      return;
    }

    const startWeight = parseFloat(profileInitialWeight);
    const targetWeight = parseFloat(profileTargetWeight);
    if (startWeight <= targetWeight) {
      wx.showToast({ title: '初始体重需大于目标体重', icon: 'none' });
      return;
    }

    const start = new Date(profileStartDate);
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    const diffDays = Math.floor((today - start) / (1000 * 60 * 60 * 24));
    if (diffDays <= 0) {
      wx.showToast({ title: '开始日期不能晚于今天', icon: 'none' });
      return;
    }

    const weightLoss = startWeight - targetWeight;
    const rows = [];

    for (let i = 0; i <= diffDays; i++) {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      const recordDate = date.toISOString().split('T')[0];
      // 线性插值计算每天体重
      const weight = startWeight - (weightLoss * i / diffDays);
      const jin = Math.round(weight * 2 * 10) / 10;
      rows.push({
        recordDate,
        weight: Math.round(weight * 10) / 10,
        jin
      });
    }

    this.setData({ importRows: rows, hasGenerated: true });
  },

  removeImportRow(e) {
    const index = e.currentTarget.dataset.index;
    const { importRows } = this.data;
    importRows.splice(index, 1);
    this.setData({ importRows: importRows });
  },

  onImportRowWeightChange(e) {
    const index = e.currentTarget.dataset.index;
    const weight = e.detail.value;
    const { importRows } = this.data;
    importRows[index].weight = parseFloat(weight) || 0;
    importRows[index].jin = Math.round(importRows[index].weight * 2 * 10) / 10;
    this.setData({ importRows: importRows });
  },

  submitImport() {
    const { importRows, userInfo } = this.data;
    if (importRows.length === 0) return;

    const token = wx.getStorageSync('token');
    let success = 0;

    wx.showLoading({ title: '导入中...' });

    const tasks = importRows.map(row => {
      return new Promise((resolve) => {
        wx.request({
          url: API_BASE + '/weight/add?userId=' + userInfo.id,
          method: 'POST',
          header: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
          },
          data: row,
          success: (res) => {
            if (res.data.success) success++;
            resolve();
          },
          fail: () => resolve()
        });
      });
    });

    Promise.all(tasks).then(() => {
      wx.hideLoading();
      wx.showToast({ title: `成功导入${success}条`, icon: 'success' });
      this.setData({ importRows: [] });
      this.loadData();
      this.hideWeightModal();
    });
  },

  // ============ 维度弹窗 ============
  showMeasurementModal() {
    this.setData({
      showMeasurementModal: true,
      measurementDate: new Date().toISOString().split('T')[0],
      measurementBust: '',
      measurementWaist: '',
      measurementHip: '',
      measurementArm: '',
      measurementThigh: '',
      measurementUpperArm: '',
      measurementForearm: '',
      measurementCalf: '',
      editingMeasurementId: null
    });
    this.loadMeasurementData();
  },

  hideMeasurementModal() {
    this.setData({ showMeasurementModal: false });
  },

  onMeasurementDateChange(e) {
    this.setData({ measurementDate: e.detail.value });
  },

  onMeasurementBustInput(e) {
    this.setData({ measurementBust: e.detail.value });
  },

  onMeasurementWaistInput(e) {
    this.setData({ measurementWaist: e.detail.value });
  },

  onMeasurementHipInput(e) {
    this.setData({ measurementHip: e.detail.value });
  },

  onMeasurementArmInput(e) {
    this.setData({ measurementArm: e.detail.value });
  },

  onMeasurementThighInput(e) {
    this.setData({ measurementThigh: e.detail.value });
  },

  onMeasurementUpperArmInput(e) {
    this.setData({ measurementUpperArm: e.detail.value });
  },

  onMeasurementForearmInput(e) {
    this.setData({ measurementForearm: e.detail.value });
  },

  onMeasurementCalfInput(e) {
    this.setData({ measurementCalf: e.detail.value });
  },

  saveMeasurement() {
    const { measurementDate, measurementBust, measurementWaist, measurementHip, measurementArm, measurementThigh, measurementUpperArm, measurementForearm, measurementCalf, editingMeasurementId, userInfo } = this.data;

    if (!measurementBust && !measurementWaist && !measurementHip && !measurementArm && !measurementThigh && !measurementUpperArm && !measurementForearm && !measurementCalf) {
      wx.showToast({ title: '请至少输入一项维度', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    let url = API_BASE + '/measurements';
    let method = 'POST';
    let data = {
      userId: userInfo.id,
      recordDate: measurementDate,
      chest: parseFloat(measurementBust) || null,
      waist: parseFloat(measurementWaist) || null,
      hip: parseFloat(measurementHip) || null,
      upperArm: parseFloat(measurementArm) || null,
      forearm: parseFloat(measurementForearm) || null,
      thigh: parseFloat(measurementThigh) || null,
      calf: parseFloat(measurementCalf) || null
    };

    if (editingMeasurementId) {
      url = API_BASE + '/measurements/' + editingMeasurementId;
      method = 'PUT';
      data = {
        waist: parseFloat(measurementWaist) || null,
        hip: parseFloat(measurementHip) || null,
        chest: parseFloat(measurementBust) || null,
        upperArm: parseFloat(measurementArm) || null,
        forearm: parseFloat(measurementForearm) || null,
        thigh: parseFloat(measurementThigh) || null,
        calf: parseFloat(measurementCalf) || null,
        recordDate: measurementDate
      };
    }

    wx.request({
      url: url,
      method: method,
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      data: data,
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: editingMeasurementId ? '更新成功' : '记录成功', icon: 'success' });
          this.setData({ showMeasurementModal: false, editingMeasurementId: null });
          this.loadData();
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  editMeasurementRecord(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showMeasurementModal: true,
      measurementDate: item.recordDate || '',
      measurementBust: item.bust ? String(item.bust) : '',
      measurementWaist: item.waist ? String(item.waist) : '',
      measurementHip: item.hip ? String(item.hip) : '',
      measurementArm: item.upperArm ? String(item.upperArm) : '',
      measurementThigh: item.thigh ? String(item.thigh) : '',
      measurementUpperArm: item.upperArm ? String(item.upperArm) : '',
      measurementForearm: item.forearm ? String(item.forearm) : '',
      measurementCalf: item.calf ? String(item.calf) : '',
      editingMeasurementId: item.id
    });
  },

  deleteMeasurementRecord(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条维度记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/measurements/' + id,
            method: 'DELETE',
            header: { 'Authorization': 'Bearer ' + token },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadData();
              }
            }
          });
        }
      }
    });
  },

  // ============ 经期弹窗 ============
  showMenstrualModal() {
    const now = new Date();
    this.setData({
      showMenstrualModal: true,
      calendarYear: now.getFullYear(),
      calendarMonth: now.getMonth(),
      periodStartDate: '',
      periodEndDate: '',
      periodDays: 0,
      flowLevelIndex: 0,
      menstrualFlowLevel: null,
      painLevel: 0,
      menstrualSymptoms: [],
      menstrualMood: '',
      menstrualOtherInfo: ''
    });
    this.loadMenstrualData();
    this.loadMenstrualPhase();
    // 检查是否有进行中的经期
    this.checkOngoingPeriod();
  },

  // 检查是否有进行中的经期，询问是否结束
  checkOngoingPeriod() {
    const { menstrualRecords } = this.data;
    if (!menstrualRecords || menstrualRecords.length === 0) return;

    // 查找最新的没有结束日期的记录
    const ongoing = menstrualRecords.find(r => !r.cycleEndDate && r.cycleStartDate);
    if (!ongoing) return;

    let recordDate = ongoing.cycleStartDate;
    if (typeof recordDate === 'string' && recordDate.includes('T')) {
      recordDate = recordDate.split('T')[0];
    }

    // 计算天数
    const today = new Date();
    const startDate = new Date(recordDate);
    const days = Math.floor((today - startDate) / (1000 * 60 * 60 * 24)) + 1;

    // 用户选择"继续"时不设置periodStartDate，让用户重新选择
    wx.showModal({
      title: '经期提醒',
      content: `检测到您有经期记录（开始于${recordDate}，已进行${days}天）是否要结束经期？`,
      confirmText: '结束经期',
      cancelText: '继续',
      success: (res) => {
        if (res.confirm) {
          const todayStr = today.toISOString().split('T')[0];
          this.setData({
            periodStartDate: recordDate,
            periodEndDate: todayStr,
            periodDays: days,
            menstrualIsInPeriod: true
          }, () => {
            this.saveMenstrualRecord();
          });
        }
        // 用户选择继续时，不设置periodStartDate，让用户重新选择
      }
    });
  },

  hideMenstrualModal() {
    this.setData({ showMenstrualModal: false });
  },

  changeCalendarMonth(e) {
    const dir = parseInt(e.currentTarget.dataset.dir);
    let { calendarYear, calendarMonth } = this.data;
    calendarMonth += dir;
    if (calendarMonth < 0) {
      calendarMonth = 11;
      calendarYear--;
    } else if (calendarMonth > 11) {
      calendarMonth = 0;
      calendarYear++;
    }
    this.setData({ calendarYear, calendarMonth }, () => {
      this.renderCalendar();
    });
  },

  onCalendarPickerChange(e) {
    const value = e.detail.value;
    const parts = value.split('-');
    const year = parseInt(parts[0]);
    const month = parseInt(parts[1]) - 1;
    this.setData({ calendarYear: year, calendarMonth: month }, () => {
      this.renderCalendar();
    });
  },

  renderCalendar() {
    const { calendarYear, calendarMonth, menstrualRecords, periodStartDate, periodEndDate, menstrualIsInPeriod } = this.data;
    const firstDay = new Date(calendarYear, calendarMonth, 1);
    const lastDay = new Date(calendarYear, calendarMonth + 1, 0);
    const startDay = firstDay.getDay();
    const days = [];
    const today = new Date().toISOString().split('T')[0];

    // 上个月的部分
    const prevMonthLastDay = new Date(calendarYear, calendarMonth, 0).getDate();
    for (let i = startDay - 1; i >= 0; i--) {
      const day = prevMonthLastDay - i;
      days.push({ day, dateStr: '', class: 'other-month', recordId: null, isOvulation: false });
    }

    // 当前月的部分
    for (let d = 1; d <= lastDay.getDate(); d++) {
      const dateStr = `${calendarYear}-${String(calendarMonth + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      let cls = 'current-month';
      let recordId = null;
      let isOvulation = false;

      // 检查是否是历史经期日期（根据历史记录）
      for (const record of menstrualRecords) {
        if (!record.cycleStartDate) continue;
        let recordStartDate = record.cycleStartDate;
        if (typeof recordStartDate === 'string' && recordStartDate.includes('T')) {
          recordStartDate = recordStartDate.split('T')[0];
        }
        let recordEndDate = record.cycleEndDate;
        if (recordEndDate && typeof recordEndDate === 'string' && recordEndDate.includes('T')) {
          recordEndDate = recordEndDate.split('T')[0];
        }
        // 只有完整的经期记录才显示为历史经期
        if (recordEndDate && dateStr >= recordStartDate && dateStr <= recordEndDate) {
          cls = 'period';
          recordId = record.id;
          break;
        }
      }

      // 检查是否在当前选中范围内（结束日期已确定时）
      if (periodStartDate && periodEndDate && dateStr >= periodStartDate && dateStr <= periodEndDate) {
        if (dateStr === periodStartDate) cls += ' period-start';
        else if (dateStr === periodEndDate) cls += ' period-end';
        else cls += ' period-range';
      } else if (periodStartDate && !periodEndDate) {
        // 经期进行中，计算预测结束日期
        let predictedEndDate = null;
        const avgPeriodLength = this.calculateAvgPeriodLength();
        if (avgPeriodLength > 0 && periodStartDate) {
          const start = new Date(periodStartDate);
          start.setDate(start.getDate() + avgPeriodLength - 1);
          predictedEndDate = start.toISOString().split('T')[0];
        }

        // 标记范围：从开始日期到预测结束日期或今天（取较小的）
        let endDate = predictedEndDate && predictedEndDate < today ? predictedEndDate : today;
        if (dateStr >= periodStartDate && dateStr <= endDate) {
          if (dateStr === periodStartDate) cls += ' period-start';
          else if (dateStr === endDate) cls += ' period-end';
          else cls += ' period-range';
        } else if (dateStr === periodStartDate) {
          cls += ' period-start-only';
        }
      } else if (dateStr === periodStartDate) {
        cls += ' period-start-only';
      }

      days.push({ day: d, dateStr, class: cls, recordId, isOvulation });
    }

    // 下个月的部分
    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      days.push({ day: i, dateStr: '', class: 'other-month', recordId: null });
    }

    this.setData({ calendarDays: days });

    // 检查是否需要弹出经期结束提醒
    this.checkPeriodEndReminder();
  },

  // 计算平均经期长度
  calculateAvgPeriodLength() {
    const { menstrualRecords } = this.data;
    if (!menstrualRecords || menstrualRecords.length === 0) return 5; // 默认5天

    let totalDays = 0;
    let count = 0;
    for (const record of menstrualRecords) {
      if (record.cycleLength && record.cycleLength > 0) {
        totalDays += record.cycleLength;
        count++;
      } else if (record.cycleStartDate && record.cycleEndDate) {
        let start = record.cycleStartDate;
        let end = record.cycleEndDate;
        if (typeof start === 'string' && start.includes('T')) start = start.split('T')[0];
        if (typeof end === 'string' && end.includes('T')) end = end.split('T')[0];
        const days = (new Date(end) - new Date(start)) / (1000 * 60 * 60 * 24) + 1;
        if (days > 0 && days <= 15) {
          totalDays += days;
          count++;
        }
      }
    }
    return count > 0 ? Math.round(totalDays / count) : 5;
  },

  // 检查是否需要弹出经期结束提醒
  checkPeriodEndReminder() {
    const { periodStartDate, periodEndDate, menstrualIsInPeriod, lastPeriodEndReminder } = this.data;
    if (!periodStartDate || periodEndDate || !menstrualIsInPeriod) return;

    const avgPeriodLength = this.calculateAvgPeriodLength();
    const start = new Date(periodStartDate);
    const today = new Date();
    const predictedEnd = new Date(start);
    predictedEnd.setDate(predictedEnd.getDate() + avgPeriodLength - 1);

    // 如果今天是预测结束日或之后，且今天还没提醒过
    const todayStr = today.toISOString().split('T')[0];
    if (today >= predictedEnd && lastPeriodEndReminder !== todayStr) {
      const actualDays = Math.floor((today - start) / (1000 * 60 * 60 * 24)) + 1;
      wx.showModal({
        title: '经期结束提醒',
        content: `您从 ${periodStartDate} 开始经期，至今已 ${actualDays} 天，经期是否已结束？`,
        confirmText: '已结束',
        cancelText: '继续',
        success: (res) => {
          if (res.confirm) {
            this.setData({ periodEndDate: todayStr, periodDays: actualDays }, () => {
              this.saveMenstrualRecord();
            });
          }
          this.setData({ lastPeriodEndReminder: todayStr });
        }
      });
    }
  },

  // 日历日期点击
  onCalendarDayTap(e) {
    const dateStr = (e.currentTarget.dataset.date || '').trim();
    const day = e.currentTarget.dataset.day;
    const recordId = e.currentTarget.dataset.recordid;
    if (!dateStr || !day) return;

    const { periodStartDate, periodEndDate, predictedNextPeriodDate, menstrualRecords, menstrualIsInPeriod } = this.data;
    const storedStart = (periodStartDate || '').trim();
    const storedEnd = (periodEndDate || '').trim();

    console.log('=== Calendar Tap ===');
    console.log('dateStr:', dateStr, 'day:', day, 'recordId:', recordId);
    console.log('periodStartDate:', storedStart, 'periodEndDate:', storedEnd);
    console.log('match start:', dateStr === storedStart);
    console.log('match end:', dateStr === storedEnd);

    // 如果点击的日期是历史经期记录的一部分，提供删除选项
    if (recordId) {
      console.log('TAP: clicking on historical record, offering delete');
      wx.showModal({
        title: '删除经期记录',
        content: '该日期属于已记录的经期，确定要删除这条经期记录吗？',
        confirmText: '删除',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            const token = wx.getStorageSync('token');
            wx.request({
              url: API_BASE + '/menstrual/' + recordId,
              method: 'DELETE',
              header: { 'Authorization': 'Bearer ' + token },
              success: (res) => {
                if (res.data.success) {
                  wx.showToast({ title: '已删除', icon: 'success' });
                  this.loadMenstrualData();
                  this.loadMenstrualPhase();
                } else {
                  wx.showToast({ title: res.data.message || '删除失败', icon: 'none' });
                }
              },
              fail: () => {
                wx.showToast({ title: '网络错误', icon: 'none' });
              }
            });
          }
        }
      });
      return;
    }

    // 如果点击已选中的开始日期，取消选择
    if (dateStr === storedStart) {
      console.log('TAP: cancelling start date');
      wx.showModal({
        title: '取消选择',
        content: '确定要取消经期开始日期吗？',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.setData({ periodStartDate: '', periodEndDate: '', periodDays: 0, menstrualIsInPeriod: false });
            this.renderCalendar();
          }
        }
      });
      return;
    }

    // 如果点击已选中的结束日期，取消选择
    if (dateStr === storedEnd) {
      console.log('TAP: cancelling end date');
      wx.showModal({
        title: '取消选择',
        content: '确定要取消经期结束日期吗？',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.setData({ periodEndDate: '', periodDays: 0 });
            this.renderCalendar();
          }
        }
      });
      return;
    }

    // 如果正在经期中（menstrualIsInPeriod=true）但还没有设置periodStartDate，点击应显示取消选择
    if (menstrualIsInPeriod && !storedStart) {
      console.log('TAP: in period but no start date, showing cancel option');
      wx.showModal({
        title: '取消选择',
        content: '确定要取消当前经期记录吗？',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.setData({ menstrualIsInPeriod: false, periodStartDate: '', periodEndDate: '', periodDays: 0 });
            this.renderCalendar();
          }
        }
      });
      return;
    }

    if (!storedStart) {
      wx.showModal({
        title: '预测经期提醒',
        content: `系统预测下次经期开始日期为${dateStr}，经期是否已来？`,
        confirmText: '经期来了',
        cancelText: '还没来',
        success: (res) => {
          if (res.confirm) {
            // 用户选择"经期来了"，使用点击的日期作为开始日期
            this.setData({ periodStartDate: dateStr, periodEndDate: '', periodDays: 0, menstrualIsInPeriod: true });
            this.renderCalendar();
          } else {
            // 用户选择"还没来"，记录预测日期但不标记为经期
            this.setData({ periodStartDate: dateStr, periodEndDate: '', periodDays: 0, menstrualIsInPeriod: false });
            this.renderCalendar();
          }
        }
      });
    } else if (!storedEnd) {
      // 选中结束日期 - 确认对话框
      // 合理性校验：不能跨月选择
      const startMonth = storedStart.substring(0, 7);
      const endMonth = dateStr.substring(0, 7);
      if (startMonth !== endMonth) {
        wx.showToast({ title: '请选择同一月份内', icon: 'none' });
        return;
      }
      // 合理性校验：经期时长不超过15天
      const days = (new Date(dateStr) - new Date(storedStart)) / (1000 * 60 * 60 * 24) + 1;
      if (days > 15) {
        wx.showToast({ title: '经期时长不能超过15天', icon: 'none' });
        return;
      }
      wx.showModal({
        title: '设置经期结束',
        content: `是否将 ${dateStr} 设为经期结束日期？（共${days}天）`,
        confirmText: '确认',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.setData({ periodEndDate: dateStr, periodDays: days }, () => {
              this.saveMenstrualRecord();
            });
          }
        }
      });
    } else {
      // 已有两个日期，点击重置 - 确认对话框
      wx.showModal({
        title: '重新选择',
        content: '确定要重新选择经期日期吗？',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.setData({ periodStartDate: dateStr, periodEndDate: '', periodDays: 0 });
            this.renderCalendar();
          }
        }
      });
    }
  },

  // 更新经期天数
  updatePeriodDays() {
    const { periodStartDate, periodEndDate } = this.data;
    if (periodStartDate && periodEndDate) {
      const days = (new Date(periodEndDate) - new Date(periodStartDate)) / (1000 * 60 * 60 * 24) + 1;
      this.setData({ periodDays: days });
    }
  },

  // 清除开始日期
  clearPeriodStart() {
    this.setData({ periodStartDate: '', periodEndDate: '', periodDays: 0 });
  },

  // 清除结束日期
  clearPeriodEnd() {
    this.setData({ periodEndDate: '', periodDays: 0 });
  },

  // 日历日期长按 - 删除历史经期记录
  onCalendarDayLongPress(e) {
    const recordId = e.currentTarget.dataset.recordid;
    if (!recordId) return;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条经期记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/menstrual/' + recordId,
            method: 'DELETE',
            header: { 'Authorization': 'Bearer ' + token },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadMenstrualData();
                this.loadMenstrualPhase();
                this.renderCalendar();
              }
            }
          });
        }
      }
    });
  },

  // 结束当前经期
  endCurrentPeriod() {
    const today = new Date().toISOString().split('T')[0];
    this.setData({ periodEndDate: today }, () => {
      this.updatePeriodDays();
      this.saveMenstrualRecord();
    });
  },

  // 保存经期记录
  saveMenstrualRecord() {
    const { periodStartDate, periodEndDate, periodDays, menstrualFlowLevel, hasPain, menstrualOtherInfo } = this.data;

    if (!periodStartDate) {
      wx.showToast({ title: '请选择经期开始日期', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');

    // 保存前校验：结束日期不能早于开始日期
    if (periodEndDate && periodEndDate < periodStartDate) {
      wx.showToast({ title: '结束日期不能早于开始日期', icon: 'none' });
      return;
    }

    wx.request({
      url: API_BASE + '/menstrual',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      data: {
        userId: userInfo.id,
        cycleStartDate: periodStartDate,
        cycleEndDate: periodEndDate || null,
        cycleLength: periodEndDate ? periodDays : null,
        flowLevel: menstrualFlowLevel || null,
        hasPain: hasPain,
        otherInfo: menstrualOtherInfo || null
      },
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: '保存成功', icon: 'success' });
          this.setData({
            periodStartDate: '',
            periodEndDate: '',
            periodDays: 0,
            flowLevelIndex: 0,
            menstrualFlowLevel: null,
            hasPain: null,
            menstrualOtherInfo: ''
          });
          this.loadMenstrualData();
          this.loadMenstrualPhase();
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  // 删除经期记录
  deleteMenstrualRecord(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条经期记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/menstrual/' + id,
            method: 'DELETE',
            header: { 'Authorization': 'Bearer ' + token },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadMenstrualData();
                this.loadMenstrualPhase();
              }
            }
          });
        }
      }
    });
  },

  onFlowLevelChange(e) {
    const index = parseInt(e.detail.value);
    let flowLevel = null;
    if (index === 1) flowLevel = 'light';
    else if (index === 2) flowLevel = 'medium';
    else if (index === 3) flowLevel = 'heavy';
    this.setData({
      flowLevelIndex: index,
      menstrualFlowLevel: flowLevel
    });
  },

  onHasPainChange(e) {
    this.setData({ hasPain: e.detail.value === 'true' });
  },

  onMenstrualOtherInfoInput(e) {
    this.setData({ menstrualOtherInfo: e.detail.value });
  },

  // 切换症状选择
  toggleSymptom(e) {
    const symptom = e.currentTarget.dataset.symptom;
    const { menstrualSymptoms } = this.data;
    const index = menstrualSymptoms.indexOf(symptom);
    if (index > -1) {
      menstrualSymptoms.splice(index, 1);
    } else {
      menstrualSymptoms.push(symptom);
    }
    this.setData({ menstrualSymptoms });
  },

  // 检查症状是否选中
  symptomSelected(symptom) {
    return this.data.menstrualSymptoms.includes(symptom);
  },

  // 疼痛等级选择
  onPainLevelChange(e) {
    const level = parseInt(e.currentTarget.dataset.level);
    this.setData({ painLevel: level });
  },

  // 心情选择
  onMoodChange(e) {
    const mood = e.currentTarget.dataset.mood;
    this.setData({ menstrualMood: mood });
  },

  // 快捷操作：记录经期开始
  togglePeriodStart() {
    const today = new Date().toISOString().split('T')[0];
    if (this.data.menstrualIsInPeriod) {
      // 已经在经期，显示当前状态
      return;
    }
    wx.showModal({
      title: '记录经期',
      content: '经期是否今日开始？',
      confirmText: '今天开始',
      cancelText: '选择日期',
      success: (res) => {
        if (res.confirm) {
          this.setData({ periodStartDate: today, periodEndDate: '', periodDays: 0 });
        } else {
          // 用户选择日期，暂时不做处理，等待用户在日历上选择
          wx.showToast({ title: '请在日历选择开始日期', icon: 'none' });
        }
      }
    });
  },

  // 快捷操作：结束经期
  togglePeriodEnd() {
    if (!this.data.menstrualIsInPeriod) return;
    if (this.data.menstrualEndDate) return; // 已经结束了

    const today = new Date().toISOString().split('T')[0];
    wx.showModal({
      title: '结束经期',
      content: '经期是否今日结束？',
      confirmText: '今天结束',
      cancelText: '选择日期',
      success: (res) => {
        if (res.confirm) {
          this.setData({ periodEndDate: today }, () => {
            this.updatePeriodDays();
            this.saveMenstrualRecord();
          });
        }
      }
    });
  },

  // 显示症状记录（暂时未用）
  showMenstrualSymptoms() {
    // 可以展开症状表单
  },

  // ============ 档案弹窗 ============
  showProfileModal() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    console.log('showProfileModal - constitutionTags from userInfo:', userInfo.constitutionTags);
    console.log('showProfileModal - profileConstitutionTags before setData:', this.data.profileConstitutionTags);
    let genderIndex = 0;
    if (userInfo.gender === 1) genderIndex = 1;
    else if (userInfo.gender === 2) genderIndex = 2;

    // 用户类型映射
    let userTypeIndex = 0;
    const userTypeMap = { 'weight_loss': '减重', 'shaping': '塑形', 'maintenance': '维持', 'muscle_gain': '增肌' };
    if (userInfo.userType && userTypeMap[userInfo.userType]) {
      userTypeIndex = this.data.userTypeOptions.indexOf(userTypeMap[userInfo.userType]);
      if (userTypeIndex < 0) userTypeIndex = 0;
    }

    // 运动偏好映射
    let exercisePreferenceIndex = 0;
    if (userInfo.exercisePreference) {
      exercisePreferenceIndex = this.data.exercisePreferenceOptions.indexOf(userInfo.exercisePreference);
      if (exercisePreferenceIndex < 0) exercisePreferenceIndex = 0;
    }

    // 健身水平映射
    let fitnessLevelIndex = 0;
    if (userInfo.fitnessLevel) {
      fitnessLevelIndex = this.data.fitnessLevelOptions.indexOf(userInfo.fitnessLevel);
      if (fitnessLevelIndex < 0) fitnessLevelIndex = 0;
    }

    // 饮食偏好映射
    let dietPreferenceIndex = 0;
    if (userInfo.dietPreference) {
      dietPreferenceIndex = this.data.dietPreferenceOptions.indexOf(userInfo.dietPreference);
      if (dietPreferenceIndex < 0) dietPreferenceIndex = 0;
    }

    this.setData({
      showProfileModal: true,
      profileNickname: userInfo.nickname || '',
      profileAge: userInfo.age || '',
      profileGenderIndex: genderIndex,
      profileHeight: userInfo.height || '',
      profileInitialWeight: userInfo.initialWeight || '',
      profileTargetWeight: userInfo.targetWeight || '',
      profileWeightLossPeriod: userInfo.weightLossPeriod || '',
      profileStartWeightDate: userInfo.startWeightDate || '',
      profileDietaryTaboo: userInfo.dietaryTaboo || '',
      profileSleepStart: userInfo.sleepStart || '',
      profileSleepEnd: userInfo.sleepEnd || '',
      // 新增字段
      profileUserTypeIndex: userTypeIndex,
      profileExerciseFrequency: userInfo.exerciseFrequency || '',
      profileExercisePreferenceIndex: exercisePreferenceIndex,
      profileFitnessLevelIndex: fitnessLevelIndex,
      profileHasKneeIssue: userInfo.hasKneeIssue,
      profileDietPreferenceIndex: dietPreferenceIndex,
      profileBreakfastHabit: userInfo.breakfastHabit !== false,
      profileConstitutionTags: userInfo.constitutionTags || '',
      profileWorkPressure: userInfo.workPressure || '',
      profileWaterIntake: userInfo.waterIntake || '',
      profileTargetAreas: userInfo.targetAreas || ''
    });
    console.log('showProfileModal - profileConstitutionTags after setData:', this.data.profileConstitutionTags);
  },

  hideProfileModal() {
    this.setData({ showProfileModal: false });
  },

  onProfileNicknameInput(e) {
    this.setData({ profileNickname: e.detail.value });
  },

  onProfileAgeInput(e) {
    this.setData({ profileAge: e.detail.value });
  },

  onGenderChange(e) {
    this.setData({ profileGenderIndex: parseInt(e.detail.value) });
  },

  onUserTypeChange(e) {
    this.setData({ profileUserTypeIndex: parseInt(e.detail.value) });
  },

  onProfileHeightInput(e) {
    this.setData({ profileHeight: e.detail.value });
  },

  onProfileExerciseFrequencyInput(e) {
    this.setData({ profileExerciseFrequency: e.detail.value });
  },

  onExercisePreferenceChange(e) {
    this.setData({ profileExercisePreferenceIndex: parseInt(e.detail.value) });
  },

  onFitnessLevelChange(e) {
    this.setData({ profileFitnessLevelIndex: parseInt(e.detail.value) });
  },

  onHasKneeIssueChange(e) {
    this.setData({ profileHasKneeIssue: e.detail.value === 'true' });
  },

  onDietPreferenceChange(e) {
    this.setData({ profileDietPreferenceIndex: parseInt(e.detail.value) });
  },

  onBreakfastHabitChange(e) {
    this.setData({ profileBreakfastHabit: e.detail.value === 'true' });
  },

  toggleConstitutionTag(e) {
    const tag = e.currentTarget.dataset.tag;
    console.log('toggleConstitutionTag called, tag:', tag);
    let tagsStr = this.data.profileConstitutionTags || '';
    let tags = tagsStr ? tagsStr.split(',').filter(t => t && t.trim()) : [];
    console.log('current tags:', tags);

    const index = tags.indexOf(tag);
    if (index >= 0) {
      tags.splice(index, 1);
    } else {
      tags.push(tag);
    }
    console.log('new tags:', tags);
    this.setData({ profileConstitutionTags: tags.join(',') });
  },

  onProfileWorkPressureInput(e) {
    this.setData({ profileWorkPressure: e.detail.value });
  },

  onProfileWaterIntakeInput(e) {
    this.setData({ profileWaterIntake: e.detail.value });
  },

  onProfileInitialWeightInput(e) {
    this.setData({ profileInitialWeight: e.detail.value });
  },

  onProfileTargetWeightInput(e) {
    this.setData({ profileTargetWeight: e.detail.value });
  },

  onProfileWeightLossPeriodInput(e) {
    this.setData({ profileWeightLossPeriod: e.detail.value });
  },

  onProfileStartWeightDateChange(e) {
    this.setData({ profileStartWeightDate: e.detail.value });
  },

  onProfileDietaryTabooInput(e) {
    this.setData({ profileDietaryTaboo: e.detail.value });
  },

  onProfileSleepStartChange(e) {
    this.setData({ profileSleepStart: e.detail.value });
  },

  onProfileSleepEndChange(e) {
    this.setData({ profileSleepEnd: e.detail.value });
  },

  saveProfile() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo') || {};
    const userId = userInfo.id;

    if (!userId) {
      wx.showToast({ title: '用户信息错误', icon: 'none' });
      return;
    }

    const { profileGenderIndex, profileUserTypeIndex, profileExercisePreferenceIndex, profileFitnessLevelIndex, profileDietPreferenceIndex } = this.data;
    let gender = null;
    if (profileGenderIndex === 1) gender = 1;
    else if (profileGenderIndex === 2) gender = 2;

    // 用户类型映射
    const userTypeOptions = ['weight_loss', 'shaping', 'maintenance', 'muscle_gain'];
    const exercisePreferenceOptions = ['居家', '健身房', '户外', '游泳'];
    const fitnessLevelOptions = ['新手', '中级', '高级'];
    const dietPreferenceOptions = ['外卖多', '自己做饭', '清淡', '重口'];

    const profileData = {
      nickname: this.data.profileNickname,
      age: parseInt(this.data.profileAge) || null,
      gender: gender,
      height: parseFloat(this.data.profileHeight) || null,
      initialWeight: parseFloat(this.data.profileInitialWeight) || null,
      targetWeight: parseFloat(this.data.profileTargetWeight) || null,
      weightLossPeriod: parseInt(this.data.profileWeightLossPeriod) || null,
      startWeightDate: this.data.profileStartWeightDate || null,
      dietaryTaboo: this.data.profileDietaryTaboo || null,
      sleepStart: this.data.profileSleepStart || null,
      sleepEnd: this.data.profileSleepEnd || null,
      // 新增字段
      userType: profileUserTypeIndex > 0 ? userTypeOptions[profileUserTypeIndex - 1] : 'weight_loss',
      exerciseFrequency: parseInt(this.data.profileExerciseFrequency) || null,
      exercisePreference: profileExercisePreferenceIndex > 0 ? exercisePreferenceOptions[profileExercisePreferenceIndex - 1] : null,
      fitnessLevel: profileFitnessLevelIndex > 0 ? fitnessLevelOptions[profileFitnessLevelIndex - 1] : null,
      hasKneeIssue: this.data.profileHasKneeIssue,
      dietPreference: profileDietPreferenceIndex > 0 ? dietPreferenceOptions[profileDietPreferenceIndex - 1] : null,
      breakfastHabit: this.data.profileBreakfastHabit,
      constitutionTags: this.data.profileConstitutionTags || null,
      workPressure: parseInt(this.data.profileWorkPressure) || null,
      waterIntake: parseInt(this.data.profileWaterIntake) || null,
      targetAreas: this.data.profileTargetAreas || null
    };

    wx.request({
      url: API_BASE + '/user/' + userId,
      method: 'PUT',
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      data: profileData,
      success: (res) => {
        if (res.data.success) {
          const updatedUserInfo = { ...userInfo, ...profileData };
          wx.setStorageSync('userInfo', updatedUserInfo);
          this.setData({ showProfileModal: false, userInfo: updatedUserInfo });
          wx.showToast({ title: '保存成功', icon: 'success' });
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  // ============ 体脂估算 ============
  showBfpModal() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({
      showBfpModal: true,
      bfpWaist: userInfo.lastWaist || '',
      bfpHip: userInfo.lastHip || '',
      bfpWeight: userInfo.lastWeight || '',
      bfpResult: null,
      bfpCategory: '',
      bfpAdvice: ''
    });
  },

  hideBfpModal() {
    this.setData({ showBfpModal: false });
  },

  onBfpWaistInput(e) {
    this.setData({ bfpWaist: e.detail.value });
  },

  onBfpHipInput(e) {
    this.setData({ bfpHip: e.detail.value });
  },

  onBfpWeightInput(e) {
    this.setData({ bfpWeight: e.detail.value });
  },

  calculateBfp() {
    const { bfpWaist, bfpHip, bfpWeight, userInfo } = this.data;
    const waist = parseFloat(bfpWaist);
    const hip = parseFloat(bfpHip);
    const weight = parseFloat(bfpWeight);
    const height = parseFloat(userInfo.height) || 0;
    const gender = userInfo.gender;

    if (!waist || !hip || !weight) {
      wx.showToast({ title: '请输入完整数据', icon: 'none' });
      return;
    }

    // 使用体脂公式估算
    // BMI法：体脂率 = 1.20 × BMI + 0.23 × 年龄 - 10.8 × 性别 - 5.4
    // 其中性别：男性=1，女性=0
    let bmi = weight / ((height / 100) * (height / 100));
    let age = userInfo.age || 25;
    let genderFactor = gender === 1 ? 1 : 0;

    let bfp = 1.20 * bmi + 0.23 * age - 10.8 * genderFactor - 5.4;

    // 性别专用公式（更准确）
    if (gender === 1) {
      // 男性体脂公式（基于腰围）
      bfp = -98.42 + 4.15 * waist - 0.082 * weight;
    } else {
      // 女性体脂公式（基于腰围和臀围）
      bfp = -83.82 + 1.93 * waist - 0.09 * weight + 0.18 * (hip || waist);
    }

    bfp = Math.max(3, Math.min(50, bfp)); // 限制范围
    bfp = bfp.toFixed(1);

    // 判断体脂类别和建议
    let category = '';
    let advice = '';

    if (gender === 1) {
      if (bfp < 10) {
        category = '偏低（运动员水平）';
        advice = '体脂偏低，可能影响激素水平，建议适当增肌';
      } else if (bfp < 20) {
        category = '标准（健康范围）';
        advice = '体脂在健康范围内，继续保持！';
      } else if (bfp < 25) {
        category = '偏高';
        advice = '体脂偏高于标准，建议增加有氧运动';
      } else {
        category = '过高';
        advice = '体脂过高，建议控制饮食并增加运动';
      }
    } else {
      if (bfp < 18) {
        category = '偏低（运动员水平）';
        advice = '体脂偏低，可能影响激素水平，建议适当增肌';
      } else if (bfp < 28) {
        category = '标准（健康范围）';
        advice = '体脂在健康范围内，继续保持！';
      } else if (bfp < 33) {
        category = '偏高';
        advice = '体脂偏高于标准，建议增加有氧运动';
      } else {
        category = '过高';
        advice = '体脂过高，建议控制饮食并增加运动';
      }
    }

    this.setData({
      bfpResult: bfp,
      bfpCategory: category,
      bfpAdvice: advice
    });
  },

  noop() {}
});
