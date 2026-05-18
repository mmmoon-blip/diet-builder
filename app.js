const { API_BASE } = require('./config/api');

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false
  },

  onLaunch() {
    // 初始化本地存储
    this.initStorage();

    // 检查登录状态
    this.checkLoginStatus();
  },

  initStorage() {
    // 初始化用户数据
    const userData = wx.getStorageSync('userData');
    if (!userData) {
      wx.setStorageSync('userData', {
        nickname: '',
        age: null,
        gender: null,
        height: null,
        initialWeight: null,
        targetWeight: null,
        weightLossPeriod: null,
        startWeightDate: '',
        dietaryTaboo: '',
        sleepStart: '',
        sleepEnd: '',
        reminderEnabled: false,
        reminderIntervalHours: 24
      });
    }

    // 初始化聊天记录
    const chatHistory = wx.getStorageSync('chatHistory');
    if (!chatHistory) {
      wx.setStorageSync('chatHistory', []);
    }

    // 初始化体重记录
    const weightRecords = wx.getStorageSync('weightRecords');
    if (!weightRecords) {
      wx.setStorageSync('weightRecords', []);
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');

    if (token) {
      // 验证token有效性
      this.validateToken(token);
    } else {
      // 未登录，跳转到登录页
      this.goToLogin();
    }
  },

  validateToken(token) {
    wx.request({
      url: API_BASE + '/auth/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.success) {
          this.globalData.token = token;
          this.globalData.userInfo = res.data.data;
          this.globalData.isLoggedIn = true;
        } else {
          this.clearLoginData();
          this.goToLogin();
        }
      },
      fail: (err) => {
        console.error('validateToken failed', err);
        // 网络错误时保留本地数据，允许继续使用
        this.globalData.token = token;
        this.globalData.userInfo = wx.getStorageSync('userInfo');
        this.globalData.isLoggedIn = true;
      }
    });
  },

  clearLoginData() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
  },

  goToLogin() {
    // 跳转到登录页（如果不在登录页）
    const pages = getCurrentPages();
    if (pages.length === 0) {
      // 小程序刚启动，没有任何页面，直接跳转到登录页
      wx.redirectTo({
        url: '/pages/login/index'
      });
      return;
    }
    const currentPage = pages[pages.length - 1];
    if (currentPage && currentPage.route !== 'pages/login/index') {
      wx.redirectTo({
        url: '/pages/login/index'
      });
    }
  },

  // 退出登录 - 无状态JWT，前端清除token即可
  logout() {
    this.clearLoginData();
    this.goToLogin();
  }
})