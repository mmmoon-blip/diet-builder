const app = getApp();
const { API_BASE } = require('../../config/api');

Page({
  data: {
    dietDate: '',
    mealTypes: ['早餐', '午餐', '晚餐', '加餐'],
    mealTypeIndex: 0,
    foods: '',
    calories: '',
    startTime: '',
    endTime: '',
    note: '',
    dietHistory: [],
    dietHasMore: false,
    todaySummary: null,
    editingId: null
  },

  onLoad: function() {
    const now = new Date();
    const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    this.setData({ dietDate: dateStr });
    this.loadDietData();
  },

  onDateChange: function(e) {
    this.setData({ dietDate: e.detail.value });
  },

  onMealTypeChange: function(e) {
    this.setData({ mealTypeIndex: e.detail.value });
  },

  onFoodsInput: function(e) {
    this.setData({ foods: e.detail.value });
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

  saveDiet: function() {
    if (!this.data.foods) {
      wx.showToast({ title: '请输入食物描述', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const mealType = this.data.mealTypes[this.data.mealTypeIndex];

    const record = {
      userId: userInfo.id,
      mealType: mealType,
      foods: this.data.foods,
      calories: this.data.calories ? parseInt(this.data.calories) : null,
      recordDate: this.data.dietDate,
      startTime: this.data.startTime || null,
      endTime: this.data.endTime || null,
      note: this.data.note || ''
    };

    let url = API_BASE + '/diet';
    let method = 'POST';

    if (this.data.editingId) {
      url = API_BASE + '/diet/' + this.data.editingId;
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
          this.loadDietData();
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
      dietDate: dateStr,
      mealTypeIndex: 0,
      foods: '',
      calories: '',
      startTime: '',
      endTime: '',
      note: '',
      editingId: null
    });
  },

  loadDietData: function() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (!token || !userInfo || !userInfo.id) return;

    wx.request({
      url: API_BASE + '/diet/' + userInfo.id + '?days=7',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success) {
          const today = new Date().toISOString().split('T')[0];
          const todayRecords = res.data.data.filter(r => r.recordDate === today);
          const todaySummary = todayRecords.length > 0 ? {
            totalCalories: todayRecords.reduce((sum, r) => sum + (r.calories || 0), 0)
          } : null;

          this.setData({
            dietHistory: res.data.data || [],
            dietHasMore: res.data.data && res.data.data.length >= 7,
            todaySummary: todaySummary
          });
        }
      }
    });
  },

  loadMoreDietHistory: function() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const { dietHistory, dietHasMore } = this.data;
    if (!token || !userInfo || !userInfo.id || !dietHasMore) return;

    wx.request({
      url: API_BASE + '/diet/' + userInfo.id + '?days=30',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success && res.data.data) {
          const newRecords = res.data.data.filter(r => !dietHistory.some(existing => existing.id === r.id));
          this.setData({
            dietHistory: [...dietHistory, ...newRecords],
            dietHasMore: newRecords.length >= 7
          });
        }
      }
    });
  },

  editDiet: function(e) {
    const item = e.currentTarget.dataset.item;
    const mealTypeIndex = this.data.mealTypes.indexOf(item.mealType);
    this.setData({
      dietDate: item.recordDate,
      mealTypeIndex: mealTypeIndex >= 0 ? mealTypeIndex : 0,
      foods: item.foods,
      calories: String(item.calories || ''),
      startTime: item.startTime || '',
      endTime: item.endTime || '',
      note: item.note || '',
      editingId: item.id
    });
    wx.pageScrollTo({ scrollTop: 0 });
  },

  deleteDiet: function(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条饮食记录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          wx.request({
            url: API_BASE + '/diet/' + id,
            method: 'DELETE',
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (res) => {
              if (res.data.success) {
                wx.showToast({ title: '已删除', icon: 'success' });
                this.loadDietData();
              }
            }
          });
        }
      }
    });
  }
});
