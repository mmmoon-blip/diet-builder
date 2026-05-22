// pages/login/index.js
const app = getApp();
const { API_BASE } = require('../../config/api');

Page({
  data: {
    errorMsg: ''
  },

  onLoad: function() {
    // 检查是否已登录（静默检查token有效性）
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus: function() {
    const token = wx.getStorageSync('token');
    if (token) {
      // 验证token是否有效
      wx.request({
        url: API_BASE + '/auth/me',
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          if (res.data.success) {
            // token有效，跳转到首页
            this.goHome();
          }
        },
        fail: () => {
          // token无效或网络错误，清除并显示登录页面
          this.clearAuth();
        }
      });
    }
  },

  // 清除认证信息
  clearAuth: function() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },

  // 微信登录 - button 点击触发，直接获取用户信息
  onGetUserInfo: function(e) {
    const userInfo = e.detail.userInfo;

    if (!userInfo) {
      // 用户拒绝授权
      wx.showToast({ title: '需要授权才能登录', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...' });

    // 先获取 code
    wx.login({
      success: (loginRes) => {
        // 调试模式：使用DEBUG_前缀的openid绕过微信验证
        const code = 'DEBUG_odvt03d7a2IYh4_XJubc6kkEFkoE';
        // const code = loginRes.code;  // 正式模式
        if (!code) {
          wx.hideLoading();
          wx.showToast({ title: '获取code失败', icon: 'none' });
          return;
        }

        // 带上用户信息登录
        this.doLogin(code, userInfo.nickName, userInfo.avatarUrl);
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '微信登录失败', icon: 'none' });
        console.error('wx.login failed', err);
      }
    });
  },

  // 执行登录请求
  doLogin: function(code, nickname, avatarUrl) {
    wx.request({
      url: API_BASE + '/auth/login',
      method: 'POST',
      data: {
        code: code,
        nickname: nickname || '减减用户',
        avatarUrl: avatarUrl || ''
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.success) {
          const data = res.data.data;

          // 保存 token 和用户信息
          wx.setStorageSync('token', data.token);
          wx.setStorageSync('userInfo', data.user);

          this.goHome();
        } else {
          wx.showToast({ title: res.data.message || '登录失败', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
        console.error('login request failed', err);
      }
    });
  },

  // 跳转到首页
  goHome: function() {
    wx.redirectTo({
      url: '/pages/index/index'
    });
  },

  // 显示用户协议
  showAgreement: function() {
    wx.showModal({
      title: '用户协议',
      content: '这里是用户协议内容...',
      showCancel: false
    });
  },

  // 显示隐私政策
  showPrivacy: function() {
    wx.showModal({
      title: '隐私政策',
      content: '这里是隐私政策内容...',
      showCancel: false
    });
  }
});