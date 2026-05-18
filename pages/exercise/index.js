const app = getApp();
const { API_BASE } = require('../../config/api');

Page({
  data: {
    exerciseDate: '',
    exerciseTypes: [
      '跑步', '快走', '走路', '慢跑', '游泳', '骑行', '跳绳',
      '健身训练', '力量训练', 'HIIT', '瑜伽', '普拉提', '拉伸',
      '爬山', '篮球', '足球', '羽毛球', '乒乓球', '网球', '排球',
      '舞蹈', '广场舞', '健身操', '动感单车', '椭圆机', '划船机',
      '太极', '八段锦', '拳击', '格斗', '攀岩', '滑雪', '滑冰',
      '登山', '徒步', '散步', '爬坡', '爬楼梯', '楼梯机', '其他'
    ],
    exerciseTypeIndex: 0,
    exerciseSearch: '',
    filteredExerciseTypes: [],
    showExerciseDropdown: false,
    duration: '',
    calories: '',
    startTime: '',
    endTime: '',
    note: '',
    exerciseHistory: [],
    exerciseHasMore: false,
    todaySummary: null,
    editingId: null
  },

  onLoad: function() {
    const now = new Date();
    const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    this.setData({ exerciseDate: dateStr });
    this.loadExerciseData();
  },

  onDateChange: function(e) {
    this.setData({ exerciseDate: e.detail.value });
  },

  onExerciseTypeChange: function(e) {
    this.setData({ exerciseTypeIndex: e.detail.value, showExerciseDropdown: false });
  },

  toggleExerciseDropdown: function() {
    const { showExerciseDropdown, exerciseTypes, exerciseTypeIndex } = this.data;
    if (!showExerciseDropdown) {
      // 打开时初始化过滤列表（显示所有选项）
      const filtered = exerciseTypes.map((name, index) => ({ name, originalIndex: index }));
      this.setData({ showExerciseDropdown: true, filteredExerciseTypes: filtered, exerciseSearch: '' });
    } else {
      this.setData({ showExerciseDropdown: false });
    }
  },

  onSearchBlur: function() {
    // 延迟关闭以便点击选项
    setTimeout(() => {
      this.setData({ showExerciseDropdown: false });
    }, 200);
  },

  onExerciseSearchInput: function(e) {
    const search = e.detail.value;
    const { exerciseTypes } = this.data;
    if (!search) {
      // 空搜索时显示全部
      const filtered = exerciseTypes.map((name, index) => ({ name, originalIndex: index }));
      this.setData({ exerciseSearch: '', filteredExerciseTypes: filtered });
      return;
    }
    const filtered = exerciseTypes
      .map((name, index) => ({ name, originalIndex: index }))
      .filter(item => item.name.includes(search));
    this.setData({ exerciseSearch: search, filteredExerciseTypes: filtered });
  },

  selectExerciseType: function(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ exerciseTypeIndex: index, showExerciseDropdown: false, exerciseSearch: '', filteredExerciseTypes: [] });
  },

  onDurationInput: function(e) {
    this.setData({ duration: e.detail.value });
  },

  onCaloriesInput: function(e) {
    this.setData({ calories: e.detail.value });
  },

  onStartTimeChange: function(e) {
    this.setData({ startTime: e.detail.value });
  },

  onEndTimeChange: function(e) {
    this.setData({ endTime: e.detail.value });
  },

  onNoteInput: function(e) {
    this.setData({ note: e.detail.value });
  },

  saveExercise: function() {
    if (!this.data.duration) {
      wx.showToast({ title: '请输入时长', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const exerciseType = this.data.exerciseTypes[this.data.exerciseTypeIndex];

    const record = {
      userId: userInfo.id,
      type: exerciseType,
      duration: parseInt(this.data.duration),
      calories: this.data.calories ? parseInt(this.data.calories) : 0,
      recordDate: this.data.exerciseDate,
      startTime: this.data.startTime || null,
      endTime: this.data.endTime || null,
      note: this.data.note || ''
    };

    let url = API_BASE + '/exercise';
    let method = 'POST';

    if (this.data.editingId) {
      url = API_BASE + '/exercise/' + this.data.editingId;
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
          wx.showToast({ title: '保存成功', icon: 'success' });
          this.resetForm();
          this.loadExerciseData();
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  resetForm: function() {
    const now = new Date();
    const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    this.setData({
      exerciseDate: dateStr,
      exerciseTypeIndex: 0,
      duration: '',
      calories: '',
      startTime: '',
      endTime: '',
      note: '',
      editingId: null
    });
  },

  loadExerciseData: function() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/exercise/' + userInfo.id + '?days=7',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success) {
          const today = new Date().toISOString().split('T')[0];
          const todayRecords = res.data.data.filter(r => r.recordDate === today);
          const todaySummary = todayRecords.length > 0 ? {
            totalDuration: todayRecords.reduce((sum, r) => sum + (r.duration || 0), 0),
            totalCalories: todayRecords.reduce((sum, r) => sum + (r.calories || 0), 0)
          } : null;

          this.setData({
            exerciseHistory: res.data.data || [],
            exerciseHasMore: res.data.data && res.data.data.length >= 7,
            todaySummary: todaySummary
          });
        }
      }
    });
  },

  loadMoreExerciseHistory: function() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const { exerciseHistory, exerciseHasMore } = this.data;
    if (!token || !userInfo || !userInfo.id || !exerciseHasMore) return;

    wx.request({
      url: API_BASE + '/exercise/' + userInfo.id + '?days=30',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const newRecords = res.data.data.filter(r => !exerciseHistory.some(existing => existing.id === r.id));
          this.setData({
            exerciseHistory: [...exerciseHistory, ...newRecords],
            exerciseHasMore: newRecords.length >= 7
          });
        }
      }
    });
  },

  editExercise: function(e) {
    const item = e.currentTarget.dataset.item;
    const typeIndex = this.data.exerciseTypes.indexOf(item.type);
    this.setData({
      exerciseDate: item.recordDate,
      exerciseTypeIndex: typeIndex >= 0 ? typeIndex : 0,
      duration: String(item.duration),
      calories: String(item.calories || ''),
      startTime: item.startTime || '',
      endTime: item.endTime || '',
      note: item.note || '',
      editingId: item.id
    });
    wx.pageScrollTo({ scrollTop: 0 });
  },

  deleteExercise: function(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条运动记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/exercise/' + id,
            method: 'DELETE',
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadExerciseData();
              }
            }
          });
        }
      }
    });
  }
});
